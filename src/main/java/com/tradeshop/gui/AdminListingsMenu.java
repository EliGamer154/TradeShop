package com.tradeshop.gui;

import com.tradeshop.data.Listing;
import com.tradeshop.data.ShopState;
import com.tradeshop.trade.TradeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** OP-only screen: every open listing from every player, with a force-delete action. */
public class AdminListingsMenu extends ShopMenu {
	private final int page;

	private AdminListingsMenu(int containerId, ServerPlayer player, int page) {
		super(containerId, player);
		this.page = page;
		render();
	}

	public static void open(ServerPlayer player, int page) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new AdminListingsMenu(id, player, page), Component.literal("Admin: All Listings")));
	}

	private void render() {
		fillBackground();
		ShopState state = ShopState.get(player.level().getServer());
		List<Listing> listings = state.allOpenListings();
		int start = page * CONTENT_PAGE_SIZE;
		for (int i = 0; i < CONTENT_PAGE_SIZE; i++) {
			int index = start + i;
			if (index < listings.size()) {
				Listing listing = listings.get(index);
				ItemStack icon = listing.items.isEmpty() ? new ItemStack(Items.CHEST) : listing.items.get(0);
				ItemStack displayIcon = Icons.of(icon, "Listing by " + listing.ownerName,
						Icons.summarize(listing.items), "Click to force-delete", Icons.peekHint(icon));
				setItemButton(contentSlot(i), displayIcon,
						() -> forceDelete(listing),
						() -> AdminListingsMenu.open(player, page));
			}
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(() -> MainMenu.open(player)));
		setDisplay(49, Icons.of(new ItemStack(Items.PAPER), "Page " + (page + 1)));
		if (page > 0) {
			setButton(46, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Previous Page"), () -> openLater(() -> AdminListingsMenu.open(player, page - 1)));
		}
		if (start + CONTENT_PAGE_SIZE < listings.size()) {
			setButton(52, Icons.of(new ItemStack(Items.SPECTRAL_ARROW), "Next Page"), () -> openLater(() -> AdminListingsMenu.open(player, page + 1)));
		}
		refresh();
	}

	private void forceDelete(Listing listing) {
		ShopState state = ShopState.get(player.level().getServer());
		boolean deleted = state.forceCancelListing(listing.id);
		if (!deleted) {
			player.sendSystemMessage(Component.literal("Couldn't delete that listing."));
			openLater(() -> AdminListingsMenu.open(player, 0));
			return;
		}
		ServerPlayer owner = player.level().getServer().getPlayerList().getPlayer(listing.ownerId);
		if (owner != null) {
			TradeService.giveAll(owner, listing.items);
			owner.sendSystemMessage(Component.literal("An admin removed your listing. Your item has been returned."));
			player.sendSystemMessage(Component.literal("Listing deleted and item returned to " + listing.ownerName + "."));
		} else {
			player.sendSystemMessage(Component.literal("Listing deleted. " + listing.ownerName + " was offline, so the item could not be returned and was forfeited."));
		}
		openLater(() -> AdminListingsMenu.open(player, 0));
	}
}
