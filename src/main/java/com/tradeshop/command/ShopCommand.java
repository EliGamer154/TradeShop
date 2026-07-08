package com.tradeshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.tradeshop.gui.MainMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class ShopCommand {
	private ShopCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("shop").executes(context -> {
			ServerPlayer player = context.getSource().getPlayerOrException();
			MainMenu.open(player);
			return Command.SINGLE_SUCCESS;
		}));
	}
}
