package com.niuhi.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.niuhi.Windswept;
import com.niuhi.particle.player.PlayerParticleTypes;
import com.niuhi.particle.water.WaterParticleTypes;
import com.niuhi.water.RippleSystem;
import com.niuhi.wind.WindSystem;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class DebugCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerWindCommands(dispatcher);
            registerCascadeCommand(dispatcher);
            registerRippleCommand(dispatcher);
            registerDustCloudCommand(dispatcher);
        });
    }

    private static void registerWindCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("windswept")
                .then(literal("wind")
                        .then(literal("set")
                                .then(literal("none")
                                        .executes(context -> setWind(context, WindSystem.WindType.NONE)))
                                .then(literal("soft")
                                        .executes(context -> setWind(context, WindSystem.WindType.SOFT)))
                                .then(literal("normal")
                                        .executes(context -> setWind(context, WindSystem.WindType.NORMAL)))
                                .then(literal("heavy")
                                        .executes(context -> setWind(context, WindSystem.WindType.HEAVY)))
                                .then(literal("storm")
                                        .executes(context -> setWind(context, WindSystem.WindType.STORM))))
                ));
    }

    private static int setWind(CommandContext<FabricClientCommandSource> context, WindSystem.WindType windType) throws CommandSyntaxException {
        if (Windswept.WIND_SYSTEM == null) {
            context.getSource().sendError(Text.literal("Wind system not initialized."));
            return 0;
        }
        Windswept.WIND_SYSTEM.setWind(new Vec3d(1, 0.1, 0), windType);
        context.getSource().sendFeedback(Text.literal("Set wind to " + windType.name()));
        return 1;
    }

    private static void registerCascadeCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("windswept")
                .then(literal("cascade")
                        .then(argument("duration", IntegerArgumentType.integer(1, 60))
                                .executes(context -> spawnCascade(context, IntegerArgumentType.getInteger(context, "duration"))))));
    }

    private static int spawnCascade(CommandContext<FabricClientCommandSource> context, int duration) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            context.getSource().sendError(Text.literal("Player or world not available."));
            return 0;
        }
        BlockPos pos = client.player.getBlockPos();
        Random random = client.world.random;
        int ticks = duration * 20; // Convert seconds to ticks
        float waterfallIntensity = 2.0f; // Moderate intensity
        float scale = 0.5f; // Moderate scale
        // Schedule particle spawning for the duration
        for (int i = 0; i < ticks; i += 5) {
            client.world.addParticleClient(
                    WaterParticleTypes.CASCADE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                    scale,
                    0.0,
                    0.0
            );
        }
        context.getSource().sendFeedback(Text.literal("Spawning cascade particles at player position for " + duration + " seconds."));
        return 1;
    }

    private static void registerRippleCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("windswept")
                .then(literal("ripple")
                        .then(argument("duration", IntegerArgumentType.integer(1, 60))
                                .executes(context -> spawnRipples(context, IntegerArgumentType.getInteger(context, "duration"))))));
    }

    private static int spawnRipples(CommandContext<FabricClientCommandSource> context, int duration) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            context.getSource().sendError(Text.literal("Player or world not available."));
            return 0;
        }
        BlockPos center = client.player.getBlockPos();
        Random random = client.world.random;
        RippleSystem.RippleSettings settings = RippleSystem.RippleSettings.RAINDROP;
        int ticks = duration * 20; // Convert seconds to ticks
        for (int i = 0; i < ticks; i += 5) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (random.nextFloat() < 0.3f) { // 30% chance per position
                        BlockPos pos = center.add(x, 0, z);
                        double waterHeight = pos.getY() + 1.0; // Assume surface level
                        double offsetX = random.nextDouble() * 0.6 - 0.3;
                        double offsetZ = random.nextDouble() * 0.6 - 0.3;
                        client.world.addParticleClient(
                                WaterParticleTypes.RIPPLE,
                                pos.getX() + 0.5 + offsetX,
                                waterHeight + 0.01,
                                pos.getZ() + 0.5 + offsetZ,
                                settings.sizeMultiplier(),
                                settings.maxAge(),
                                settings.animationSpeed()
                        );
                    }
                }
            }
        }
        context.getSource().sendFeedback(Text.literal("Spawning ripples in a 5x5 area around player for " + duration + " seconds."));
        return 1;
    }

    private static void registerDustCloudCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("windswept")
                .then(literal("dustcloud")
                        .then(argument("duration", IntegerArgumentType.integer(1, 60))
                                .executes(context -> spawnDustClouds(context, IntegerArgumentType.getInteger(context, "duration"))))));
    }

    private static int spawnDustClouds(CommandContext<FabricClientCommandSource> context, int duration) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            context.getSource().sendError(Text.literal("Player or world not available."));
            return 0;
        }
        BlockPos center = client.player.getBlockPos();
        Random random = client.world.random;
        int ticks = duration * 20; // Convert seconds to ticks
        for (int i = 0; i < ticks; i += 5) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (random.nextFloat() < 0.3f) { // 30% chance per position
                        BlockPos pos = center.add(x, 0, z);
                        BlockPos belowPos = pos.down();
                        if (!client.world.getBlockState(belowPos).isSolidBlock(client.world, belowPos)) {
                            continue;
                        }
                        double y = pos.getY() + 0.1;
                        double motionX = (random.nextDouble() - 0.5) * 0.015;
                        double motionY = 0.03 + random.nextDouble() * 0.03;
                        double motionZ = (random.nextDouble() - 0.5) * 0.015;
                        client.world.addParticleClient(
                                PlayerParticleTypes.DUST_CLOUD,
                                x, y, z,
                                motionX, motionY, motionZ
                        );
                    }
                }
            }
        }
        context.getSource().sendFeedback(Text.literal("Spawning dust clouds in a 5x5 area around player for " + duration + " seconds."));
        return 1;
    }
}