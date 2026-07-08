package com.tradeshop.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tradeshop.TradeShop;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopState extends SavedData {
	private static final Codec<ListingStatus> LISTING_STATUS_CODEC =
			Codec.STRING.xmap(ListingStatus::valueOf, Enum::name);
	private static final Codec<OfferStatus> OFFER_STATUS_CODEC =
			Codec.STRING.xmap(OfferStatus::valueOf, Enum::name);

	private static final Codec<Listing> LISTING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(l -> l.id),
			UUIDUtil.CODEC.fieldOf("ownerId").forGetter(l -> l.ownerId),
			Codec.STRING.fieldOf("ownerName").forGetter(l -> l.ownerName),
			ItemStack.CODEC.listOf().fieldOf("items").forGetter(l -> l.items),
			LISTING_STATUS_CODEC.fieldOf("status").forGetter(l -> l.status)
	).apply(instance, Listing::new));

	private static final Codec<Offer> OFFER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(o -> o.id),
			UUIDUtil.CODEC.fieldOf("listingId").forGetter(o -> o.listingId),
			UUIDUtil.CODEC.fieldOf("offererId").forGetter(o -> o.offererId),
			Codec.STRING.fieldOf("offererName").forGetter(o -> o.offererName),
			ItemStack.CODEC.listOf().fieldOf("items").forGetter(o -> o.items),
			OFFER_STATUS_CODEC.fieldOf("status").forGetter(o -> o.status)
	).apply(instance, Offer::new));

	public static final Codec<ShopState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LISTING_CODEC.listOf().fieldOf("listings").forGetter(s -> s.listings),
			OFFER_CODEC.listOf().fieldOf("offers").forGetter(s -> s.offers)
	).apply(instance, ShopState::new));

	public static final SavedDataType<ShopState> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(TradeShop.MOD_ID, "shop_data"), ShopState::new, CODEC, null);

	private final List<Listing> listings;
	private final List<Offer> offers;

	public ShopState() {
		this(new ArrayList<>(), new ArrayList<>());
	}

	private ShopState(List<Listing> listings, List<Offer> offers) {
		this.listings = new ArrayList<>(listings);
		this.offers = new ArrayList<>(offers);
	}

	public static ShopState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public Listing addListing(ServerPlayer player, List<ItemStack> items) {
		Listing listing = new Listing(UUID.randomUUID(), player.getUUID(), player.getGameProfile().name(), items, ListingStatus.OPEN);
		listings.add(listing);
		setDirty();
		return listing;
	}

	public Offer addOffer(ServerPlayer player, Listing listing, List<ItemStack> items) {
		Offer offer = new Offer(UUID.randomUUID(), listing.id, player.getUUID(), player.getGameProfile().name(), items, OfferStatus.PENDING);
		offers.add(offer);
		setDirty();
		return offer;
	}

	public Optional<Listing> findListing(UUID id) {
		return listings.stream().filter(l -> l.id.equals(id)).findFirst();
	}

	public Optional<Offer> findOffer(UUID id) {
		return offers.stream().filter(o -> o.id.equals(id)).findFirst();
	}

	public List<Listing> openListingsExcluding(UUID owner) {
		return listings.stream()
				.filter(l -> l.status == ListingStatus.OPEN && !l.ownerId.equals(owner))
				.collect(Collectors.toList());
	}

	public List<Listing> listingsByOwner(UUID owner) {
		return listings.stream()
				.filter(l -> l.ownerId.equals(owner) && l.status == ListingStatus.OPEN)
				.collect(Collectors.toList());
	}

	public List<Offer> pendingOffersForListing(UUID listingId) {
		return offers.stream()
				.filter(o -> o.listingId.equals(listingId) && o.status == OfferStatus.PENDING)
				.collect(Collectors.toList());
	}

	public List<Offer> offersForListing(UUID listingId) {
		return offers.stream().filter(o -> o.listingId.equals(listingId)).collect(Collectors.toList());
	}

	public List<Offer> activeOffersByOfferer(UUID offerer) {
		return offers.stream()
				.filter(o -> o.offererId.equals(offerer)
						&& (o.status == OfferStatus.PENDING || o.status == OfferStatus.SELLER_ACCEPTED))
				.collect(Collectors.toList());
	}

	public void sellerAccept(UUID offerId) {
		findOffer(offerId).ifPresent(offer -> {
			offer.status = OfferStatus.SELLER_ACCEPTED;
			for (Offer other : offersForListing(offer.listingId)) {
				if (!other.id.equals(offer.id) && other.status == OfferStatus.PENDING) {
					other.status = OfferStatus.CANCELLED;
				}
			}
			setDirty();
		});
	}

	/** Cancels a listing the given player owns, and any pending/accepted offers against it. */
	public boolean cancelListing(UUID listingId, UUID requester) {
		return findListing(listingId)
				.filter(listing -> listing.ownerId.equals(requester) && listing.status == ListingStatus.OPEN)
				.map(listing -> {
					listing.status = ListingStatus.CANCELLED;
					for (Offer offer : offersForListing(listingId)) {
						if (offer.status == OfferStatus.PENDING || offer.status == OfferStatus.SELLER_ACCEPTED) {
							offer.status = OfferStatus.CANCELLED;
						}
					}
					setDirty();
					return true;
				}).orElse(false);
	}

	/** Withdraws an offer the given player made, as long as it hasn't completed or already ended. */
	public boolean cancelOffer(UUID offerId, UUID requester) {
		return findOffer(offerId)
				.filter(offer -> offer.offererId.equals(requester)
						&& (offer.status == OfferStatus.PENDING || offer.status == OfferStatus.SELLER_ACCEPTED))
				.map(offer -> {
					offer.status = OfferStatus.CANCELLED;
					setDirty();
					return true;
				}).orElse(false);
	}
}
