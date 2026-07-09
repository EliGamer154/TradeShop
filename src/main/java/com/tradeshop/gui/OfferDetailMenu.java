package com.tradeshop.gui;

import com.tradeshop.data.Listing;
import com.tradeshop.data.Offer;
import com.tradeshop.data.OfferStatus;
import com.tradeshop.data.ShopState;
import com.tradeshop.trade.TradeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.UUID;

/** Detail screen for a single offer the player made: confirm the trade or withdraw it. */
public class OfferDetailMenu extends ShopMenu {
	private final UUID offerId;

	private OfferDetailMenu(int containerId, ServerPlayer player, UUID offerId) {
		super(containerId, player);
		this.offerId = offerId;
		render();
	}

	public static void open(ServerPlayer player, UUID offerId) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new OfferDetailMenu(id, player, offerId), Component.literal("Offer Details")));
	}

	private void render() {
		fillBackground();
		ShopState state = ShopState.get(player.level().getServer());
		Optional<Offer> offerOpt = state.findOffer(offerId);
		if (offerOpt.isEmpty()) {
			player.sendSystemMessage(Component.literal("That offer no longer exists."));
			openLater(() -> MyOffersMenu.open(player, 0));
			return;
		}
		Offer offer = offerOpt.get();
		Optional<Listing> listingOpt = state.findListing(offer.listingId);

		ItemStack offerIcon = offer.items.isEmpty() ? new ItemStack(Items.PAPER) : offer.items.get(0);
		ItemStack offerDisplay = Icons.of(offerIcon, "Your Offer", Icons.summarize(offer.items), Icons.peekHint(offerIcon));
		setItemDisplay(contentSlot(2), offerDisplay, () -> OfferDetailMenu.open(player, offerId));

		ItemStack listingIcon = listingOpt.map(l -> l.items.isEmpty() ? new ItemStack(Items.CHEST) : l.items.get(0))
				.orElse(new ItemStack(Items.BARRIER));
		String listingSummary = listingOpt.map(l -> Icons.summarize(l.items)).orElse("This listing no longer exists");
		ItemStack listingDisplay = Icons.of(listingIcon, "You'll Receive", listingSummary, Icons.peekHint(listingIcon));
		setItemDisplay(contentSlot(4), listingDisplay, () -> OfferDetailMenu.open(player, offerId));

		boolean ready = offer.status == OfferStatus.SELLER_ACCEPTED;
		if (ready) {
			setButton(48, Icons.of(new ItemStack(Items.LIME_DYE), "Confirm Trade", "Complete this trade now"), () -> confirm(offer));
		} else {
			setDisplay(48, Icons.of(new ItemStack(Items.YELLOW_DYE), "Waiting", "Waiting for the seller to accept"));
		}
		setButton(50, Icons.of(new ItemStack(Items.LAVA_BUCKET), "Withdraw Offer", "Cancel this offer"), () -> withdraw(offer));
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> MyOffersMenu.open(player, 0)));
		refresh();
	}

	private void confirm(Offer offer) {
		ShopState state = ShopState.get(player.level().getServer());
		TradeService.Result result = TradeService.buyerConfirm(player.level().getServer(), state, offer.id);
		switch (result) {
			case SUCCESS -> player.sendSystemMessage(Component.literal("Trade completed!"));
			case MISSING_ITEMS ->
					player.sendSystemMessage(Component.literal("Trade failed - one of you no longer has the required items. The listing has been reopened."));
			case OFFLINE ->
					player.sendSystemMessage(Component.literal("The seller must be online to complete this trade. Try again later."));
			case NOT_FOUND -> player.sendSystemMessage(Component.literal("This offer is no longer valid."));
		}
		openLater(() -> MyOffersMenu.open(player, 0));
	}

	private void withdraw(Offer offer) {
		ShopState state = ShopState.get(player.level().getServer());
		boolean withdrawn = state.cancelOffer(offer.id, player.getUUID());
		if (withdrawn) {
			player.sendSystemMessage(Component.literal("Offer withdrawn."));
			state.findListing(offer.listingId).ifPresent(listing -> {
				ServerPlayer seller = player.level().getServer().getPlayerList().getPlayer(listing.ownerId);
				if (seller != null) {
					seller.sendSystemMessage(Component.literal(player.getGameProfile().name() + " withdrew their offer on your listing."));
				}
			});
		} else {
			player.sendSystemMessage(Component.literal("Couldn't withdraw that offer."));
		}
		openLater(() -> MyOffersMenu.open(player, 0));
	}
}
