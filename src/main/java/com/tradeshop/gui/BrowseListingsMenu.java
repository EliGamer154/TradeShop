package com.tradeshop.gui;

import com.tradeshop.data.Listing;
import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class BrowseListingsMenu extends ShopMenu {
	private static final int PAGE_SIZE = 45;
	private final int page;

	private BrowseListingsMenu(int containerId, ServerPlayer player, int page) {
		super(containerId, player);
		this.page = page;
		render();
	}

	public static void open(ServerPlayer player, int page) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new BrowseListingsMenu(id, player, page), Component.literal("Browse Listings")));
	}

	private void render() {
		ShopState state = ShopState.get(player.level().getServer());
		List<Listing> listings = state.openListingsExcluding(player.getUUID());
		int start = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE; i++) {
			int index = start + i;
			if (index < listings.size()) {
				Listing listing = listings.get(index);
				ItemStack icon = listing.items.isEmpty() ? new ItemStack(Items.CHEST) : listing.items.get(0);
				setButton(i, Icons.of(icon, "Listing by " + listing.ownerName,
								Icons.summarize(listing.items), "Click to make an offer"),
						() -> BundleBuilderMenu.openForOffer(player, listing.id));
			} else {
				clearButton(i);
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> MainMenu.open(player));
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> BrowseListingsMenu.open(player, page - 1));
		} else {
			clearButton(46);
		}
		if (start + PAGE_SIZE < listings.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> BrowseListingsMenu.open(player, page + 1));
		} else {
			clearButton(52);
		}
		refresh();
	}
}
