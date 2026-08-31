package com.example.baritonescheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SchedulerConfig {

    /** Master switch. Kalau false, mod tidak melakukan apa-apa sama sekali. */
    public boolean enabled = true;

    /** Daftar jadwal mining */
    public List<ScheduleEntry> tasks = new ArrayList<>();

    /** Aturan kapan berhenti total */
    public StopRule stopRule = new StopRule();

    /** Kirim pesan status ke chat game (info debug) */
    public boolean verboseChatLog = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static SchedulerConfig defaultConfig() {
        SchedulerConfig cfg = new SchedulerConfig();
        ScheduleEntry e1 = new ScheduleEntry();
        e1.target = "diamond_ore";
        e1.intervalMinutes = 5;
        e1.durationSeconds = 60;
        cfg.tasks.add(e1);
        return cfg;
    }

    public static SchedulerConfig load(Path path) {
        try {
            if (!Files.exists(path)) {
                SchedulerConfig def = defaultConfig();
                save(def, path);
                return def;
            }
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                SchedulerConfig cfg = GSON.fromJson(r, SchedulerConfig.class);
                return cfg != null ? cfg : defaultConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return defaultConfig();
        }
    }

    public static void save(SchedulerConfig cfg, Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(cfg, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
