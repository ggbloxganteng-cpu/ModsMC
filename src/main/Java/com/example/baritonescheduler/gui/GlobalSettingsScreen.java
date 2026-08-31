package com.example.baritonescheduler.gui;

import com.example.baritonescheduler.SchedulerConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class GlobalSettingsScreen extends Screen {

    private static final String[] MODES = {"NONE", "AFTER_MINUTES", "AT_CLOCK"};

    private final Screen parent;
    private final SchedulerConfig config;
    private final Runnable onDone;

    private int modeIndex;
    private TextFieldWidget afterMinutesField;
    private TextFieldWidget atClockField;
    private TextFieldWidget stopAfterCommandsField;
    private boolean verboseLocal;
    private boolean enabledLocal;
    private ButtonWidget modeButton;

    public GlobalSettingsScreen(Screen parent, SchedulerConfig config, Runnable onDone) {
        super(Text.literal("Global Settings"));
        this.parent = parent;
        this.config = config;
        this.onDone = onDone;
        this.verboseLocal = config.verboseChatLog;
        this.enabledLocal = config.enabled;
        String mode = config.stopRule != null ? config.stopRule.mode : "AFTER_MINUTES";
        this.modeIndex = indexOf(mode);
    }

    private int indexOf(String mode) {
        for (int i = 0; i < MODES.length; i++) if (MODES[i].equalsIgnoreCase(mode)) return i;
        return 1;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;
        int fieldWidth = 220;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Scheduler Master: " + (enabledLocal ? "ON" : "OFF")),
                btn -> {
                    enabledLocal = !enabledLocal;
                    btn.setMessage(Text.literal("Scheduler Master: " + (enabledLocal ? "ON" : "OFF")));
                }
        ).dimensions(cx - fieldWidth / 2, y, fieldWidth, 20).build());
        y += 28;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Chat Log: " + (verboseLocal ? "ON" : "OFF")),
                btn -> {
                    verboseLocal = !verboseLocal;
                    btn.setMessage(Text.literal("Chat Log: " + (verboseLocal ? "ON" : "OFF")));
                }
        ).dimensions(cx - fieldWidth / 2, y, fieldWidth, 20).build());
        y += 28;

        modeButton = ButtonWidget.builder(
                Text.literal("Stop Mode: " + MODES[modeIndex]),
                btn -> {
                    modeIndex = (modeIndex + 1) % MODES.length;
                    btn.setMessage(Text.literal("Stop Mode: " + MODES[modeIndex]));
                }
        ).dimensions(cx - fieldWidth / 2, y, fieldWidth, 20).build();
        addDrawableChild(modeButton);
        y += 28;

        afterMinutesField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Stop setelah N menit"));
        afterMinutesField.setText(String.valueOf(config.stopRule != null ? config.stopRule.afterMinutes : 120));
        addDrawableChild(afterMinutesField);
        y += 28;

        atClockField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Stop di jam HH:mm"));
        atClockField.setText(config.stopRule != null ? config.stopRule.atClock : "23:30");
        addDrawableChild(atClockField);
        y += 28;

        stopAfterCommandsField = new TextFieldWidget(this.textRenderer, cx - fieldWidth / 2, y, fieldWidth, 20, Text.literal("Command saat stop total"));
        stopAfterCommandsField.setMaxLength(512);
        stopAfterCommandsField.setText(config.stopRule != null ? String.join(" | ", config.stopRule.afterCommands) : "");
        addDrawableChild(stopAfterCommandsField);
        y += 32;

        addDrawableChild(ButtonWidget.builder(Text.literal("Selesai"), btn -> applyAndClose())
                .dimensions(cx - 105, y, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Batal"), btn -> close())
                .dimensions(cx + 5, y, 100, 20).build());
    }

    private void applyAndClose() {
        config.enabled = enabledLocal;
        config.verboseChatLog = verboseLocal;
        if (config.stopRule == null) config.stopRule = new com.example.baritonescheduler.StopRule();
        config.stopRule.mode = MODES[modeIndex];
        config.stopRule.afterMinutes = parseIntSafe(afterMinutesField.getText(), config.stopRule.afterMinutes);
        config.stopRule.atClock = atClockField.getText().trim();
        String raw = stopAfterCommandsField.getText();
        config.stopRule.afterCommands = raw.isBlank()
                ? new java.util.ArrayList<>()
                : java.util.Arrays.stream(raw.split("\\|")).map(String::trim)
                    .filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
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
        context.drawCenteredTextWithShadow(this.textRenderer, "Global Settings", this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
