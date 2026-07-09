package com.tradeshop.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

/** Read-only peek inside a shulker box (or anything else carrying container contents) shown as an icon elsewhere. */
public class ShulkerContentsMenu extends ShopMenu {
	private final ItemStack container;
	private final Runnable onBack;

	private ShulkerContentsMenu(int containerId, ServerPlayer player, ItemStack container, Runnable onBack) {
		super(containerId, player);
		this.container = container;
		this.onBack = onBack;
		render();
	}

	static void open(ServerPlayer player, ItemStack container, Runnable onBack) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new ShulkerContentsMenu(id, player, container, onBack),
				container.getHoverName()));
	}

	private void render() {
		fillBackground();
		ItemContainerContents contents = container.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		int slot = 0;
		for (ItemStack item : contents.nonEmptyItemCopyStream().toList()) {
			if (slot >= CONTENT_PAGE_SIZE) {
				break;
			}
			setDisplay(contentSlot(slot), Icons.of(item, item.getHoverName().getString(), "Amount: " + item.getCount()));
			slot++;
		}
		setButton(45, Icons.of(new ItemStack(Items.ARROW), "Back"), () -> openLater(onBack));
		refresh();
	}
}
