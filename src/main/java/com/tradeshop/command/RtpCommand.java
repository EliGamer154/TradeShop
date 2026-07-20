package com.tradeshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RtpCommand {
	/** Cooldown between uses, in milliseconds (5 minutes). */
	private static final long COOLDOWN_MS = 5 * 60 * 1000L;
	/** How far inside the border edge we keep the destination, in blocks. */
	private static final double BORDER_INSET = 16.0;
	/** How many random points to try before giving up. */
	private static final int MAX_ATTEMPTS = 24;

	private static final Map<UUID, Long> LAST_USE = new HashMap<>();

	private RtpCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("rtp").executes(context -> {
			ServerPlayer player = context.getSource().getPlayerOrException();
			return run(player);
		}));
	}

	private static int run(ServerPlayer player) {
		long now = System.currentTimeMillis();
		Long last = LAST_USE.get(player.getUUID());
		if (last != null) {
			long remaining = COOLDOWN_MS - (now - last);
			if (remaining > 0) {
				long seconds = (remaining + 999) / 1000;
				player.sendSystemMessage(Component.literal(
						"You must wait " + formatDuration(seconds) + " before using /rtp again."));
				return 0;
			}
		}

		ServerLevel level = (ServerLevel) player.level();
		BlockPos destination = findSafeDestination(level);
		if (destination == null) {
			player.sendSystemMessage(Component.literal("Couldn't find a safe spot to teleport to. Try again."));
			return 0;
		}

		// Only start the cooldown once we've actually teleported the player.
		LAST_USE.put(player.getUUID(), now);

		double x = destination.getX() + 0.5;
		double y = destination.getY();
		double z = destination.getZ() + 0.5;
		player.teleportTo(level, x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);
		player.sendSystemMessage(Component.literal(
				"Teleported to " + destination.getX() + ", " + destination.getY() + ", " + destination.getZ() + "."));
		return Command.SINGLE_SUCCESS;
	}

	private static BlockPos findSafeDestination(ServerLevel level) {
		WorldBorder border = level.getWorldBorder();
		double minX = border.getMinX() + BORDER_INSET;
		double maxX = border.getMaxX() - BORDER_INSET;
		double minZ = border.getMinZ() + BORDER_INSET;
		double maxZ = border.getMaxZ() - BORDER_INSET;
		if (minX >= maxX || minZ >= maxZ) {
			return null;
		}

		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			int x = (int) Math.floor(random.nextDouble(minX, maxX));
			int z = (int) Math.floor(random.nextDouble(minZ, maxZ));

			// Force the chunk to generate/load so the heightmap is accurate.
			ChunkAccess chunk = level.getChunk(x >> 4, z >> 4);
			int surfaceY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15) + 1;

			BlockPos ground = new BlockPos(x, surfaceY - 1, z);
			if (isSafeGround(level, ground)) {
				return new BlockPos(x, surfaceY, z);
			}
		}
		return null;
	}

	private static boolean isSafeGround(ServerLevel level, BlockPos ground) {
		if (ground.getY() <= level.getMinY()) {
			// Nothing generated here (void / open sky over the abyss).
			return false;
		}
		BlockState state = level.getBlockState(ground);
		if (state.isAir()) {
			return false;
		}
		FluidState fluid = state.getFluidState();
		if (!fluid.isEmpty()) {
			// Avoid landing on lava or water surfaces.
			return false;
		}
		// Make sure there's room to stand (two air blocks above the ground).
		BlockPos feet = ground.above();
		return level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir();
	}

	private static String formatDuration(long seconds) {
		long minutes = seconds / 60;
		long secs = seconds % 60;
		if (minutes > 0) {
			return minutes + "m " + secs + "s";
		}
		return secs + "s";
	}
}
