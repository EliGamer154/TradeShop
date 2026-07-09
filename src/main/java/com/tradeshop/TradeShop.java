package com.tradeshop;

import com.tradeshop.command.ShopCommand;
import com.tradeshop.config.TradeShopConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeShop implements ModInitializer {
	public static final String MOD_ID = "tradeshop";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TradeShopConfig.load();
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> ShopCommand.register(dispatcher));
		LOGGER.info("TradeShop initialized");
	}

	public static boolean isOp(ServerPlayer player) {
		return player.level().getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
	}
}
