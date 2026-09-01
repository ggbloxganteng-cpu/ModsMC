package com.example.baritonescheduler;

/**
 * Mode "AFTER_MINUTES": stop setelah scheduler berjalan sekian menit sejak diaktifkan.
 * Mode "AT_CLOCK": stop pada jam:menit tertentu waktu nyata (real-life clock), contoh 23:30.
 * Mode "NONE": tidak pernah auto-stop (harus /bscheduler stop manual).
 */
public class StopRule {
    public String mode = "AFTER_MINUTES"; // NONE | AFTER_MINUTES | AT_CLOCK

    /** Dipakai jika mode = AFTER_MINUTES */
    public int afterMinutes = 120;

    /** Dipakai jika mode = AT_CLOCK, format 24 jam "HH:mm", contoh "23:30" */
    public String atClock = "23:30";

    /**
     * Command yang dijalankan URUT ketika scheduler berhenti total (auto-stop tercapai).
     * Sama aturannya dengan ScheduleEntry.afterCommands ('#' = Baritone, '/' = command biasa, tanpa awalan = chat).
     * Contoh: ["#back", "/logout"] biar otomatis kembali lalu keluar server setelah 2 jam.
     */
    public java.util.List<String> afterCommands = new java.util.ArrayList<>();
}
