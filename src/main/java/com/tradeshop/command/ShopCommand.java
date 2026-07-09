package com.tradeshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.tradeshop.TradeShop;
import com.tradeshop.config.TradeShopConfig;
import com.tradeshop.gui.MainMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ShopCommand {
	private ShopCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("shop")
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					MainMenu.open(player);
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.literal("reload").executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					if (!TradeShop.isOp(player)) {
						player.sendSystemMessage(Component.literal("You don't have permission to do that."));
						return 0;
					}
					TradeShopConfig.load();
					player.sendSystemMessage(Component.literal("TradeShop config reloaded from config/tradeshop.json."));
					return Command.SINGLE_SUCCESS;
				})));
	}
}
