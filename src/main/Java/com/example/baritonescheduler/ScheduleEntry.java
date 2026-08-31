package com.example.baritonescheduler;

/**
 * Satu entri jadwal, misal:
 * "Setiap 5 menit, mine diamond_ore selama 60 detik"
 */
public class ScheduleEntry {

    /** Nama block/target yang di-mine, contoh: "diamond_ore" atau "iron_ore,coal_ore" (Baritone bisa multi target dipisah spasi) */
    public String target = "diamond_ore";

    /** Jenis command Baritone yang dipakai: "mine", "mineschematic", "farm", dll. Default "mine". */
    public String command = "mine";

    /** Setiap berapa menit task ini dipicu ulang */
    public int intervalMinutes = 5;

    /** Berapa lama mining berjalan sebelum otomatis di-#stop lagi (dalam detik). 0 = tidak dibatasi (jalan sampai interval berikutnya / stop global) */
    public int durationSeconds = 60;

    /** Aktif/nonaktif entri ini tanpa perlu menghapusnya dari config */
    public boolean enabled = true;

    @Override
    public String toString() {
        return "ScheduleEntry{target=" + target + ", command=" + command +
                ", every=" + intervalMinutes + "m, duration=" + durationSeconds + "s, enabled=" + enabled + "}";
    }
}
