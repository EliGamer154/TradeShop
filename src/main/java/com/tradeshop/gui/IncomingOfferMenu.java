package com.tradeshop.gui;

import com.tradeshop.data.Offer;
import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.UUID;

/** Detail screen for one incoming offer on the seller's listing: full item breakdown, plus Accept. */
public class IncomingOfferMenu extends ShopMenu {
	private final UUID listingId;
	private final UUID offerId;

	private IncomingOfferMenu(int containerId, ServerPlayer player, UUID listingId, UUID offerId) {
		super(containerId, player);
		this.listingId = listingId;
		this.offerId = offerId;
		render();
	}

	public static void open(ServerPlayer player, UUID listingId, UUID offerId) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new IncomingOfferMenu(id, player, listingId, offerId), Component.literal("Offer Details")));
	}

	private void render() {
		fillBackground();
		ShopState state = ShopState.get(player.level().getServer());
		Optional<Offer> offerOpt = state.findOffer(offerId);
		if (offerOpt.isEmpty()) {
			player.sendSystemMessage(Component.literal("That offer no longer exists."));
			openLater(() -> ListingOffersMenu.open(player, listingId, 0));
			return;
		}
		Offer offer = offerOpt.get();

		setDisplay(4, Icons.head(offer.offererId, "Offer from " + offer.offererName));

		int slot = 0;
		for (ItemStack item : offer.items) {
			setDisplay(contentSlot(slot), Icons.of(item, item.getHoverName().getString(), "Amount: " + item.getCount()));
			slot++;
		}

		setButton(48, Icons.of(new ItemStack(Items.LIME_DYE), "Accept Offer", "Accept the offer from " + offer.offererName), () -> accept(offer));
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> ListingOffersMenu.open(player, listingId, 0)));
		refresh();
	}

	private void accept(Offer offer) {
		ShopState state = ShopState.get(player.level().getServer());
		state.sellerAccept(offer.id);
		player.sendSystemMessage(Component.literal("Offer accepted! Waiting for the other player to confirm the trade."));
		state.findOffer(offer.id).ifPresent(updated -> {
			ServerPlayer offerer = player.level().getServer().getPlayerList().getPlayer(updated.offererId);
			if (offerer != null) {
				offerer.sendSystemMessage(Component.literal("Your offer was accepted! Use /shop -> My Offers to confirm the trade."));
			}
		});
		openLater(() -> MyListingsMenu.open(player, 0));
	}
}
