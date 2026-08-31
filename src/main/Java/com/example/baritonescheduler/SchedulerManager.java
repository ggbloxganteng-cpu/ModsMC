package com.example.baritonescheduler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Mengelola siklus hidup scheduler: kapan mulai mining, kapan berhenti,
 * dan kapan seluruh scheduler mati otomatis (auto-stop rule).
 *
 * Semua interaksi ke Baritone dilakukan lewat "chat command" (#mine, #stop),
 * karena Baritone meng-intercept pesan chat yang diawali '#' sebelum dikirim ke server.
 * Ini sengaja dipilih supaya mod ini tetap kompatibel walau versi internal Baritone berubah.
 */
public class SchedulerManager {

    private final Path configPath;
    private SchedulerConfig config;

    private boolean running = false;
    private long startTick = -1;

    // per-task state: kapan terakhir kali di-trigger, dan kapan harus dihentikan (jika durationSeconds > 0)
    private final Map<ScheduleEntry, Long> lastTriggerTick = new HashMap<>();
    private final Map<ScheduleEntry, Long> stopAtTick = new HashMap<>();

    private long tickCounter = 0;
    private static final int TICKS_PER_SECOND = 20;

    public SchedulerManager(Path configPath) {
        this.configPath = configPath;
        this.config = SchedulerConfig.load(configPath);
    }

    public void reload() {
        this.config = SchedulerConfig.load(configPath);
        log("Config di-reload: " + config.tasks.size() + " task ditemukan.");
    }

    public void start() {
        if (!config.enabled) {
            log("Scheduler nonaktif di config (\"enabled\": false). Ubah config lalu /bscheduler reload.");
            return;
        }
        running = true;
        startTick = tickCounter;
        lastTriggerTick.clear();
        stopAtTick.clear();
        log("Scheduler dimulai. " + config.tasks.size() + " task aktif.");
    }

    public void stop() {
        running = false;
        sendBaritoneCommand("stop");
        log("Scheduler dihentikan.");
    }

    public boolean isRunning() {
        return running;
    }

    public SchedulerConfig getConfig() {
        return config;
    }

    /** Dipanggil setiap client tick (20x/detik) */
    public void onTick() {
        tickCounter++;
        if (!running) return;

        checkGlobalStop();
        if (!running) return; // bisa saja baru saja distop oleh checkGlobalStop()

        for (ScheduleEntry task : config.tasks) {
            if (!task.enabled) continue;
            handleTask(task);
        }
    }

    private void handleTask(ScheduleEntry task) {
        long intervalTicks = (long) task.intervalMinutes * 60L * TICKS_PER_SECOND;
        if (intervalTicks <= 0) return;

        Long last = lastTriggerTick.get(task);
        boolean dueToTrigger = (last == null) || (tickCounter - last >= intervalTicks);

        if (dueToTrigger) {
            triggerTask(task);
            lastTriggerTick.put(task, tickCounter);
            if (task.durationSeconds > 0) {
                stopAtTick.put(task, tickCounter + (long) task.durationSeconds * TICKS_PER_SECOND);
            } else {
                stopAtTick.remove(task);
            }
            return;
        }

        Long stopTick = stopAtTick.get(task);
        if (stopTick != null && tickCounter >= stopTick) {
            sendBaritoneCommand("stop");
            log("Task selesai (durasi habis): " + task.target);
            stopAtTick.remove(task);
        }
    }

    private void triggerTask(ScheduleEntry task) {
        String cmd = task.command + " " + task.target;
        sendBaritoneCommand(cmd);
        log("Trigger task: #" + cmd);
    }

    private void checkGlobalStop() {
        StopRule rule = config.stopRule;
        if (rule == null || "NONE".equalsIgnoreCase(rule.mode)) return;

        if ("AFTER_MINUTES".equalsIgnoreCase(rule.mode)) {
            long limitTicks = (long) rule.afterMinutes * 60L * TICKS_PER_SECOND;
            if (tickCounter - startTick >= limitTicks) {
                log("Batas waktu " + rule.afterMinutes + " menit tercapai. Auto-stop.");
                stop();
            }
        } else if ("AT_CLOCK".equalsIgnoreCase(rule.mode)) {
            try {
                LocalTime target = LocalTime.parse(rule.atClock);
                LocalTime now = LocalTime.now();
                // Trigger sekali saat jam:menit sekarang cocok (toleransi dalam 1 tick window per menit)
                if (now.getHour() == target.getHour() && now.getMinute() == target.getMinute()
                        && now.getSecond() == 0) {
                    log("Sudah jam " + rule.atClock + ". Auto-stop.");
                    stop();
                }
            } catch (Exception ex) {
                log("Format stopRule.atClock salah, gunakan \"HH:mm\" contoh \"23:30\"");
            }
        }
    }

    private void sendBaritoneCommand(String withoutHash) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        // Baritone meng-intercept pesan chat yang diawali '#' sebelum benar-benar terkirim ke server
        client.player.networkHandler.sendChatMessage("#" + withoutHash);
    }

    private void log(String msg) {
        if (!config.verboseChatLog) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("[BaritoneScheduler] " + msg), false);
        }
        System.out.println("[BaritoneScheduler] " + msg);
    }
}
