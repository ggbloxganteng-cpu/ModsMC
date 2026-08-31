package com.example.baritonescheduler.gui;

import com.example.baritonescheduler.ScheduleEntry;
import com.example.baritonescheduler.SchedulerConfig;
import com.example.baritonescheduler.SchedulerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Layar utama config. Diakses lewat /bscheduler gui */
public class TaskListScreen extends Screen {

    private final SchedulerManager manager;
    // salinan kerja, baru ditulis ke manager.getConfig() saat "Simpan & Terapkan"
    private final List<ScheduleEntry> workingTasks;

    public TaskListScreen(SchedulerManager manager) {
        super(Text.literal("Baritone Scheduler"));
        this.manager = manager;
        this.workingTasks = new ArrayList<>();
        for (ScheduleEntry e : manager.getConfig().tasks) {
            workingTasks.add(copy(e));
        }
    }

    private ScheduleEntry copy(ScheduleEntry src) {
        ScheduleEntry e = new ScheduleEntry();
        e.target = src.target;
        e.command = src.command;
        e.intervalMinutes = src.intervalMinutes;
        e.durationSeconds = src.durationSeconds;
        e.enabled = src.enabled;
        e.afterCommands = new ArrayList<>(src.afterCommands);
        return e;
    }

    @Override
    protected void init() {
        rebuildList();
    }

    private void rebuildList() {
        clearChildren();
        int cx = this.width / 2;
        int y = 35;
        int rowWidth = 260;

        for (int i = 0; i < workingTasks.size(); i++) {
            ScheduleEntry task = workingTasks.get(i);
            int idx = i;
            String label = (task.enabled ? "[ON] " : "[OFF] ") + task.target
                    + "  every " + task.intervalMinutes + "m";
            addDrawableChild(ButtonWidget.builder(Text.literal(label), btn ->
                    client.setScreen(new TaskEditScreen(this, task, this::rebuildList))
            ).dimensions(cx - rowWidth / 2, y, rowWidth - 45, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("X"), btn -> {
                workingTasks.remove(idx);
                rebuildList();
            }).dimensions(cx - rowWidth / 2 + rowWidth - 40, y, 40, 20).build());

            y += 24;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Tambah Task"), btn -> {
            ScheduleEntry fresh = new ScheduleEntry();
            workingTasks.add(fresh);
            client.setScreen(new TaskEditScreen(this, fresh, this::rebuildList));
        }).dimensions(cx - rowWidth / 2, y, rowWidth, 20).build());
        y += 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Global Settings"), btn ->
                client.setScreen(new GlobalSettingsScreen(this, manager.getConfig(), this::rebuildList))
        ).dimensions(cx - rowWidth / 2, y, rowWidth, 20).build());
        y += 34;

        addDrawableChild(ButtonWidget.builder(Text.literal("Simpan & Tutup"), btn -> saveAndClose())
                .dimensions(cx - 105, y, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Batal"), btn -> {
            if (client != null) client.setScreen(null);
        }).dimensions(cx + 5, y, 100, 20).build());
    }

    private void saveAndClose() {
        SchedulerConfig cfg = manager.getConfig();
        cfg.tasks = new ArrayList<>(workingTasks);
        manager.saveToDisk();
        if (client != null) client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "Baritone Scheduler - Config", this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
