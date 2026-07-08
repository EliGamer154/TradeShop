package com.tradeshop;

import com.tradeshop.command.ShopCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeShop implements ModInitializer {
	public static final String MOD_ID = "tradeshop";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> ShopCommand.register(dispatcher));
		LOGGER.info("TradeShop initialized");
	}
}
