package com.tradeshop.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Base for every TradeShop screen. Every slot (including the player's own
 * inventory rows shown at the bottom of the chest UI) is intercepted here -
 * no click ever performs real vanilla item movement. This is what lets the
 * mod run with zero client-side code: vanilla clients already know how to
 * render a GENERIC_9x6 container.
 */
public abstract class ShopMenu extends ChestMenu {
	protected static final int SIZE = 54;

	/**
	 * Paginated menus lay their entries out in the 7x4 block interior to
	 * {@link #fillBackground()}'s border (row 0, row 5, and the left/right
	 * columns of rows 1-4 are reserved for decoration and navigation).
	 */
	protected static final int CONTENT_PAGE_SIZE = 28;

	protected final ServerPlayer player;
	private final SimpleContainer buttons;
	private final Runnable[] actions = new Runnable[SIZE];

	protected ShopMenu(int containerId, ServerPlayer player) {
		this(containerId, player, new SimpleContainer(SIZE));
	}

	private ShopMenu(int containerId, ServerPlayer player, SimpleContainer container) {
		super(MenuType.GENERIC_9x6, containerId, player.getInventory(), container, 6);
		this.player = player;
		this.buttons = container;
	}

	protected void setButton(int slot, ItemStack icon, Runnable action) {
		buttons.setItem(slot, icon);
		actions[slot] = action;
	}

	protected void clearButton(int slot) {
		buttons.setItem(slot, ItemStack.EMPTY);
		actions[slot] = null;
	}

	/** Sets a slot's icon without any click action - for labels and decoration. */
	protected void setDisplay(int slot, ItemStack icon) {
		buttons.setItem(slot, icon);
		actions[slot] = null;
	}

	/** Resets every slot to a plain filler pane. Call at the start of render() before drawing content. */
	protected void fillBackground() {
		for (int i = 0; i < SIZE; i++) {
			setDisplay(i, Icons.of(new ItemStack(Items.GRAY_STAINED_GLASS_PANE), " "));
		}
	}

	/** Maps a 0-based content index to a slot inside the 7x4 interior grid (rows 1-4, columns 1-7). */
	protected static int contentSlot(int index) {
		int row = index / 7;
		int col = index % 7;
		return (row + 1) * 9 + (col + 1);
	}

	protected void refresh() {
		broadcastChanges();
	}

	/**
	 * Runs a container-switching action (opening another menu, or closing this
	 * one) on the next server tick instead of immediately. Switching
	 * {@code player.containerMenu} while the server is still in the middle of
	 * processing this click's network packet crashes the server, since the
	 * vanilla click-handling code that runs right after {@link #clicked} still
	 * expects the menu it started with. Deferring by one tick avoids that.
	 */
	protected void openLater(Runnable action) {
		player.level().getServer().execute(action);
	}

	/**
	 * Called when the player clicks one of their own real inventory slots
	 * (shown below the button grid). Default is a no-op; only the bundle
	 * builder screen cares about this.
	 */
	protected void onPlayerInventorySlotClicked(int playerSlot, ItemStack stack) {
	}

	@Override
	public void clicked(int slotId, int clickData, ContainerInput containerInput, Player clicker) {
		if (slotId < 0) {
			return;
		}
		if (slotId < SIZE) {
			Runnable action = actions[slotId];
			if (action != null) {
				action.run();
			}
		} else {
			int playerSlot = slotId - SIZE;
			ItemStack stack = getSlot(slotId).getItem();
			if (!stack.isEmpty()) {
				onPlayerInventorySlotClicked(playerSlot, stack);
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
