package com.tradeshop.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MainMenu extends ShopMenu {
	public MainMenu(int containerId, ServerPlayer player) {
		super(containerId, player);
		setButton(20, Icons.of(new ItemStack(Items.CHEST), "Add Listing", "List items for other players to offer on"),
				() -> BundleBuilderMenu.openForListing(player));
		setButton(22, Icons.of(new ItemStack(Items.EMERALD), "Browse Listings", "Make an offer on someone else's listing"),
				() -> BrowseListingsMenu.open(player, 0));
		setButton(24, Icons.of(new ItemStack(Items.BOOK), "My Listings", "View offers made on your listings"),
				() -> MyListingsMenu.open(player, 0));
		setButton(31, Icons.of(new ItemStack(Items.PAPER), "My Offers", "View and confirm your pending offers"),
				() -> MyOffersMenu.open(player, 0));
		refresh();
	}

	public static void open(ServerPlayer player) {
		player.openMenu(new SimpleMenuProvider((id, inv, p) -> new MainMenu(id, player), Component.literal("Trade Shop")));
	}
}
