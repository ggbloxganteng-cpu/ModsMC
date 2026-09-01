package com.example.baritonescheduler.gui;

import com.example.baritonescheduler.ScheduleEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Edit satu ScheduleEntry. Perubahan langsung ditulis ke object 'entry' saat "Selesai" ditekan. */
public class TaskEditScreen extends Screen {

    private final Screen parent;
    private final ScheduleEntry entry;
    private final Runnable onDone;

    private TextFieldWidget targetField;
    private TextFieldWidget commandField;
    private TextFieldWidget intervalField;
    private TextFieldWidget durationField;
    private TextFieldWidget afterCommandsField;
    private boolean enabledLocal;

    public TaskEditScreen(Screen parent, ScheduleEntry entry, Runnable onDone) {
        super(Text.literal("Edit Task"));
        this.parent = parent;
        this.entry = entry;
        this.onDone = onDone;
        this.enabledLocal = entry.enabled;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;
        int fieldWidth = 220;

        targetField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Target block"));
        targetField.setText(entry.target);
        targetField.setMaxLength(256);
        addDrawableChild(targetField);
        y += 28;

        commandField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Command Baritone"));
        commandField.setText(entry.command);
        addDrawableChild(commandField);
        y += 28;

        intervalField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Interval menit"));
        intervalField.setText(String.valueOf(entry.intervalMinutes));
        addDrawableChild(intervalField);
        y += 28;

        durationField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Durasi detik"));
        durationField.setText(String.valueOf(entry.durationSeconds));
        addDrawableChild(durationField);
        y += 28;

        afterCommandsField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Command sesudahnya"));
        afterCommandsField.setMaxLength(512);
        afterCommandsField.setText(String.join(" | ", entry.afterCommands));
        addDrawableChild(afterCommandsField);
        y += 32;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Enabled: " + (enabledLocal ? "ON" : "OFF")),
                btn -> {
                    enabledLocal = !enabledLocal;
                    btn.setMessage(Text.literal("Enabled: " + (enabledLocal ? "ON" : "OFF")));
                }
        ).dimensions(cx - fieldWidth / 2, y, fieldWidth, 20).build());
        y += 32;

        addDrawableChild(ButtonWidget.builder(Text.literal("Selesai"), btn -> {
            applyAndClose();
        }).dimensions(cx - 105, y, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Batal"), btn -> {
            close();
        }).dimensions(cx + 5, y, 100, 20).build());
    }

    private void applyAndClose() {
        entry.target = targetField.getText().trim();
        entry.command = commandField.getText().trim().isEmpty() ? "mine" : commandField.getText().trim();
        entry.intervalMinutes = parseIntSafe(intervalField.getText(), entry.intervalMinutes);
        entry.durationSeconds = parseIntSafe(durationField.getText(), entry.durationSeconds);
        entry.enabled = enabledLocal;
        String raw = afterCommandsField.getText();
        entry.afterCommands = raw.isBlank()
                ? new java.util.ArrayList<>()
                : Arrays.stream(raw.split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        if (onDone != null) onDone.run();
        close();
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit Task", this.width / 2, 15, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Pisahkan beberapa command sesudahnya pakai '|' , contoh: #back | /home base",
                this.width / 2, this.height - 20, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
