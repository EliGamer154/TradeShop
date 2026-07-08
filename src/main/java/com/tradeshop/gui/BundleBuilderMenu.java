package com.tradeshop.gui;

import com.tradeshop.data.ShopState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Shared "pick items from your inventory" screen used by both Add Listing and Make Offer. */
public class BundleBuilderMenu extends ShopMenu {
	private static final int MAX_TYPES = 9;

	private final Mode mode;
	private final UUID listingId;
	private final Map<ItemKey, ItemStack> bundle = new LinkedHashMap<>();

	public enum Mode {
		LISTING,
		OFFER
	}

	private BundleBuilderMenu(int containerId, ServerPlayer player, Mode mode, UUID listingId) {
		super(containerId, player);
		this.mode = mode;
		this.listingId = listingId;
		render();
	}

	public static void openForListing(ServerPlayer player) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new BundleBuilderMenu(id, player, Mode.LISTING, null),
				Component.literal("Add Listing - click items below to add")));
	}

	public static void openForOffer(ServerPlayer player, UUID listingId) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new BundleBuilderMenu(id, player, Mode.OFFER, listingId),
				Component.literal("Make Offer - click items below to add")));
	}

	private void render() {
		fillBackground();

		setDisplay(4, Icons.of(new ItemStack(Items.WRITABLE_BOOK), mode == Mode.LISTING ? "New Listing" : "New Offer",
				"Click items in your inventory below", "to add them (up to " + MAX_TYPES + " types)"));

		int slot = 9;
		for (ItemStack stack : bundle.values()) {
			ItemStack captured = stack;
			setButton(slot, Icons.of(captured, captured.getHoverName().getString(), "Amount: " + captured.getCount(), "Click to remove"),
					() -> {
						bundle.remove(ItemKey.of(captured));
						render();
					});
			slot++;
		}
		setButton(45, Icons.of(new ItemStack(Items.BARRIER), "Cancel"), () -> openLater(player::closeContainer));
		setButton(49, Icons.of(new ItemStack(Items.LIME_DYE), "Confirm", bundle.size() + " item type(s) selected"), this::confirm);
		setButton(53, Icons.of(new ItemStack(Items.RED_DYE), "Clear All"), () -> {
			bundle.clear();
			render();
		});
		refresh();
	}

	@Override
	protected void onPlayerInventorySlotClicked(int playerSlot, ItemStack stack) {
		ItemKey key = ItemKey.of(stack);
		if (bundle.size() >= MAX_TYPES && !bundle.containsKey(key)) {
			return;
		}
		ItemStack existing = bundle.get(key);
		if (existing != null) {
			existing.grow(1);
		} else {
			bundle.put(key, stack.copyWithCount(1));
		}
		render();
	}

	private void confirm() {
		if (bundle.isEmpty()) {
			openLater(player::closeContainer);
			return;
		}
		List<ItemStack> items = new ArrayList<>(bundle.values());
		ShopState state = ShopState.get(player.level().getServer());
		if (mode == Mode.LISTING) {
			state.addListing(player, items);
			player.sendSystemMessage(Component.literal("Listing created!"));
		} else {
			state.findListing(listingId).ifPresentOrElse(listing -> {
				state.addOffer(player, listing, items);
				player.sendSystemMessage(Component.literal("Offer submitted!"));
			}, () -> player.sendSystemMessage(Component.literal("That listing no longer exists.")));
		}
		openLater(player::closeContainer);
	}
}
