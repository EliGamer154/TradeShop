package com.tradeshop.gui;

import com.tradeshop.data.Offer;
import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

public class ListingOffersMenu extends ShopMenu {
	private static final int PAGE_SIZE = 45;
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
		ShopState state = ShopState.get(player.level().getServer());
		List<Offer> offers = state.pendingOffersForListing(listingId);
		int start = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE; i++) {
			int index = start + i;
			if (index < offers.size()) {
				Offer offer = offers.get(index);
				ItemStack icon = offer.items.isEmpty() ? new ItemStack(Items.PAPER) : offer.items.get(0);
				setButton(i, Icons.of(icon, "Offer from " + offer.offererName,
								Icons.summarize(offer.items), "Click to ACCEPT this offer"),
						() -> accept(offer));
			} else {
				clearButton(i);
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> MyListingsMenu.open(player, 0)));
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> openLater(() -> ListingOffersMenu.open(player, listingId, page - 1)));
		} else {
			clearButton(46);
		}
		if (start + PAGE_SIZE < offers.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> openLater(() -> ListingOffersMenu.open(player, listingId, page + 1)));
		} else {
			clearButton(52);
		}
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
