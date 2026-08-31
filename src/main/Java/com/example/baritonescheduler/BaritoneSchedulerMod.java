package com.example.baritonescheduler;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class BaritoneSchedulerMod implements ClientModInitializer {

    public static final String MOD_ID = "baritone-scheduler";
    private static SchedulerManager manager;

    @Override
    public void onInitializeClient() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("baritone-scheduler.json");
        manager = new SchedulerManager(configPath);

        ClientTickEvents.END_CLIENT_TICK.register(client -> manager.onTick());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("bscheduler")
                    .then(ClientCommandManager.literal("start")
                            .executes(ctx -> {
                                manager.start();
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("stop")
                            .executes(ctx -> {
                                manager.stop();
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("reload")
                            .executes(ctx -> {
                                manager.reload();
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("status")
                            .executes(ctx -> {
                                reportStatus(ctx.getSource());
                                return 1;
                            }))
            );
        });
    }

    private void reportStatus(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
        StringBuilder sb = new StringBuilder();
        sb.append("Running: ").append(manager.isRunning()).append(" | Tasks: ");
        manager.getConfig().tasks.forEach(t -> sb.append(t).append("  "));
        source.sendFeedback(Text.literal("[BaritoneScheduler] " + sb));
    }

    public static SchedulerManager getManager() {
        return manager;
    }
}
