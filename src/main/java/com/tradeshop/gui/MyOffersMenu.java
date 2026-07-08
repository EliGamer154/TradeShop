package com.tradeshop.gui;

import com.tradeshop.data.Offer;
import com.tradeshop.data.OfferStatus;
import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class MyOffersMenu extends ShopMenu {
	private final int page;

	private MyOffersMenu(int containerId, ServerPlayer player, int page) {
		super(containerId, player);
		this.page = page;
		render();
	}

	public static void open(ServerPlayer player, int page) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new MyOffersMenu(id, player, page), Component.literal("My Offers")));
	}

	private void render() {
		fillBackground();
		ShopState state = ShopState.get(player.level().getServer());
		List<Offer> offers = state.activeOffersByOfferer(player.getUUID());
		int start = page * CONTENT_PAGE_SIZE;
		for (int i = 0; i < CONTENT_PAGE_SIZE; i++) {
			int index = start + i;
			if (index < offers.size()) {
				Offer offer = offers.get(index);
				ItemStack icon = offer.items.isEmpty() ? new ItemStack(Items.PAPER) : offer.items.get(0);
				String statusLine = offer.status == OfferStatus.SELLER_ACCEPTED
						? "Ready to confirm - click for details"
						: "Waiting for seller to accept - click for details";
				setButton(contentSlot(i), Icons.of(icon, "Your offer", Icons.summarize(offer.items), statusLine),
						() -> openLater(() -> OfferDetailMenu.open(player, offer.id)));
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> MainMenu.open(player)));
		setDisplay(49, Icons.of(new ItemStack(Items.PAPER), "Page " + (page + 1)));
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> openLater(() -> MyOffersMenu.open(player, page - 1)));
		}
		if (start + CONTENT_PAGE_SIZE < offers.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> openLater(() -> MyOffersMenu.open(player, page + 1)));
		}
		refresh();
	}
}
