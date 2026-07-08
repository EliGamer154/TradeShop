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

public class MyOffersMenu extends ShopMenu {
	private static final int PAGE_SIZE = 45;
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
		ShopState state = ShopState.get(player.level().getServer());
		List<Offer> offers = state.activeOffersByOfferer(player.getUUID());
		int start = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE; i++) {
			int index = start + i;
			if (index < offers.size()) {
				Offer offer = offers.get(index);
				ItemStack icon = offer.items.isEmpty() ? new ItemStack(Items.PAPER) : offer.items.get(0);
				boolean ready = offer.status == OfferStatus.SELLER_ACCEPTED;
				String statusLine = ready ? "Click to CONFIRM the trade!" : "Waiting for seller to accept...";
				Runnable action = ready ? () -> confirm(offer) : () -> {
				};
				setButton(i, Icons.of(icon, "Your offer", Icons.summarize(offer.items), statusLine), action);
			} else {
				clearButton(i);
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> MainMenu.open(player));
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> MyOffersMenu.open(player, page - 1));
		} else {
			clearButton(46);
		}
		if (start + PAGE_SIZE < offers.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> MyOffersMenu.open(player, page + 1));
		} else {
			clearButton(52);
		}
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
		MyOffersMenu.open(player, 0);
	}
}
