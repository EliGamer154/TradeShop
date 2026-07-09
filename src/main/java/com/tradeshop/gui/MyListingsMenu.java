package com.tradeshop.gui;

import com.tradeshop.data.Listing;
import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class MyListingsMenu extends ShopMenu {
	private final int page;

	private MyListingsMenu(int containerId, ServerPlayer player, int page) {
		super(containerId, player);
		this.page = page;
		render();
	}

	public static void open(ServerPlayer player, int page) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new MyListingsMenu(id, player, page), Component.literal("My Listings")));
	}

	private void render() {
		fillBackground();
		ShopState state = ShopState.get(player.level().getServer());
		List<Listing> listings = state.listingsByOwner(player.getUUID());
		int start = page * CONTENT_PAGE_SIZE;
		for (int i = 0; i < CONTENT_PAGE_SIZE; i++) {
			int index = start + i;
			if (index < listings.size()) {
				Listing listing = listings.get(index);
				ItemStack icon = listing.items.isEmpty() ? new ItemStack(Items.CHEST) : listing.items.get(0);
				int offerCount = state.pendingOffersForListing(listing.id).size();
				ItemStack displayIcon = Icons.of(icon, "Your listing",
						Icons.summarize(listing.items), offerCount + " pending offer(s) - click to view", Icons.peekHint(icon));
				setItemButton(contentSlot(i), displayIcon,
						() -> openLater(() -> ListingOffersMenu.open(player, listing.id, 0)),
						() -> MyListingsMenu.open(player, page));
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> MainMenu.open(player)));
		setDisplay(49, Icons.of(new ItemStack(Items.PAPER), "Page " + (page + 1)));
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> openLater(() -> MyListingsMenu.open(player, page - 1)));
		}
		if (start + CONTENT_PAGE_SIZE < listings.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> openLater(() -> MyListingsMenu.open(player, page + 1)));
		}
		refresh();
	}
}
