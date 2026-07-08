package com.tradeshop.gui;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Identifies an item "type" (item + exact component/NBT data) while ignoring stack count. */
record ItemKey(Item item, DataComponentPatch components) {
	static ItemKey of(ItemStack stack) {
		return new ItemKey(stack.getItem(), stack.getComponentsPatch());
	}
}
