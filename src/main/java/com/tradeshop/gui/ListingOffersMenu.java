package com.tradeshop.gui;

import com.tradeshop.data.Offer;
import com.tradeshop.data.OfferStatus;
import com.tradeshop.data.ShopState;
import com.tradeshop.trade.TradeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

public class ListingOffersMenu extends ShopMenu {
	private final UUID listingId;
	private final int page;

	private ListingOffersMenu(int containerId, ServerPlayer player, UUID listingId, int page) {
		super(containerId, player);
		this.listingId = listingId;
		this.page = page;
		render();
	}

	public static void open(ServerPlayer player, UUID listingId, int page) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new ListingOffersMenu(id, player, listingId, page), Component.literal("Offers on your listing")));
	}

	private void render() {
		fillBackground();
		ShopState state = ShopState.get(player.level().getServer());
		List<Offer> offers = state.pendingOffersForListing(listingId);
		int start = page * CONTENT_PAGE_SIZE;
		for (int i = 0; i < CONTENT_PAGE_SIZE; i++) {
			int index = start + i;
			if (index < offers.size()) {
				Offer offer = offers.get(index);
				setButton(contentSlot(i), Icons.head(offer.offererId, offer.offererName,
								Icons.summarize(offer.items), "Click to view and accept"),
						() -> openLater(() -> IncomingOfferMenu.open(player, listingId, offer.id)));
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> MyListingsMenu.open(player, 0)));
		setButton(49, Icons.of(new ItemStack(Items.LAVA_BUCKET), "Cancel Listing", "Remove this listing entirely", "Any pending offers will be cancelled"),
				this::cancelListing);
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> openLater(() -> ListingOffersMenu.open(player, listingId, page - 1)));
		}
		if (start + CONTENT_PAGE_SIZE < offers.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> openLater(() -> ListingOffersMenu.open(player, listingId, page + 1)));
		}
		refresh();
	}

	private void cancelListing() {
		ShopState state = ShopState.get(player.level().getServer());
		List<Offer> affected = state.offersForListing(listingId).stream()
				.filter(o -> o.status == OfferStatus.PENDING || o.status == OfferStatus.SELLER_ACCEPTED)
				.toList();
		List<ItemStack> escrowedItems = state.findListing(listingId).map(l -> l.items).orElse(List.of());
		boolean cancelled = state.cancelListing(listingId, player.getUUID());
		if (cancelled) {
			TradeService.giveAll(player, escrowedItems);
			player.sendSystemMessage(Component.literal("Listing cancelled. Your items have been returned."));
			for (Offer offer : affected) {
				ServerPlayer offerer = player.level().getServer().getPlayerList().getPlayer(offer.offererId);
				if (offerer != null) {
					offerer.sendSystemMessage(Component.literal("A listing you had an offer on was cancelled by the seller."));
				}
			}
		} else {
			player.sendSystemMessage(Component.literal("Couldn't cancel that listing."));
		}
		openLater(() -> MyListingsMenu.open(player, 0));
	}
}
