package com.tradeshop.gui;

import com.tradeshop.TradeShop;
import com.tradeshop.config.TradeShopConfig;
import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MainMenu extends ShopMenu {
	public MainMenu(int containerId, ServerPlayer player) {
		super(containerId, player);
		render();
	}

	private void render() {
		fillBackground();

		setDisplay(4, Icons.of(new ItemStack(Items.EMERALD), "Trade Shop", "Buy, sell, and trade items with other players"));

		ShopState state = ShopState.get(player.level().getServer());
		int myListings = state.listingsByOwner(player.getUUID()).size();
		int myOffers = state.activeOffersByOfferer(player.getUUID()).size();
		int maxListings = TradeShopConfig.get().maxActiveListingsPerPlayer;

		if (myListings >= maxListings) {
			setButton(19, Icons.of(new ItemStack(Items.CHEST), "Add Listing", "You already have " + myListings + "/" + maxListings + " active listing(s)",
							"Cancel one first to list something else"),
					() -> player.sendSystemMessage(Component.literal("You already have the maximum number of active listings (" + maxListings + ").")));
		} else {
			setButton(19, Icons.of(new ItemStack(Items.CHEST), "Add Listing", "List an item for other players to offer on"),
					() -> openLater(() -> BundleBuilderMenu.openForListing(player)));
		}
		setButton(21, Icons.of(new ItemStack(Items.COMPASS), "Browse Listings", "Make an offer on someone else's listing"),
				() -> openLater(() -> BrowseListingsMenu.open(player, 0)));
		setButton(23, Icons.of(new ItemStack(Items.BOOK), "My Listings", myListings + " active listing(s)", "View offers and manage your listings"),
				() -> openLater(() -> MyListingsMenu.open(player, 0)));
		setButton(25, Icons.of(new ItemStack(Items.PAPER), "My Offers", myOffers + " active offer(s)", "View, confirm, or withdraw your offers"),
				() -> openLater(() -> MyOffersMenu.open(player, 0)));

		if (TradeShop.isOp(player)) {
			setButton(40, Icons.of(new ItemStack(Items.NETHER_STAR), "Admin: Manage Listings", "Force-delete any player's listing"),
					() -> openLater(() -> AdminListingsMenu.open(player, 0)));
		}

		refresh();
	}

	public static void open(ServerPlayer player) {
		player.openMenu(new SimpleMenuProvider((id, inv, p) -> new MainMenu(id, player), Component.literal("Trade Shop")));
	}
}
