package com.tradeshop.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Base for every TradeShop screen. Every slot (including the player's own
 * inventory rows shown at the bottom of the chest UI) is intercepted here -
 * no click ever performs real vanilla item movement. This is what lets the
 * mod run with zero client-side code: vanilla clients already know how to
 * render a GENERIC_9x6 container.
 */
public abstract class ShopMenu extends ChestMenu {
	protected static final int SIZE = 54;

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

	protected void refresh() {
		broadcastChanges();
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
