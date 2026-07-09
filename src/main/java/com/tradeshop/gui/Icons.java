package com.tradeshop.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class Icons {
	private Icons() {
	}

	/** A player head showing the given player's skin (resolved lazily, so this works even while they're offline). */
	static ItemStack head(UUID playerId, String name, String... lore) {
		ItemStack head = new ItemStack(Items.PLAYER_HEAD);
		head.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(playerId));
		return of(head, name, lore);
	}

	static ItemStack of(ItemStack base, String name, String... lore) {
		ItemStack stack = base.copy();
		if (stack.isEmpty()) {
			return stack;
		}
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(style -> style.withItalic(false)));
		if (lore.length > 0) {
			List<Component> lines = new ArrayList<>();
			for (String line : lore) {
				if (line != null && !line.isEmpty()) {
					lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
				}
			}
			stack.set(DataComponents.LORE, new ItemLore(lines));
		}
		return stack;
	}

	static String summarize(List<ItemStack> items) {
		StringBuilder sb = new StringBuilder();
		for (ItemStack stack : items) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(stack.getCount()).append("x ").append(stack.getHoverName().getString());
		}
		return sb.toString();
	}
}
