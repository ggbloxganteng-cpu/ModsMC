# Baritone Scheduler (Fabric, MC 1.21.6)

Mod client-side yang menjalankan command Baritone secara terjadwal berdasarkan file config JSON —
misalnya "mine diamond_ore setiap 5 menit selama 1 menit, lalu berhenti total setelah 2 jam".

## Cara kerja
Mod ini TIDAK memanggil internal API Baritone. Sebagai gantinya, ia mengirim pesan chat lokal
yang diawali `#` (contoh `#mine diamond_ore`), persis seperti kalau kamu mengetiknya sendiri di chat.
Baritone meng-intercept pesan berawalan `#` sebelum terkirim ke server, jadi ini aman dan kompatibel
dengan hampir semua versi Baritone tanpa perlu depend ke jar-nya langsung.

**Prasyarat:** Baritone (build Fabric, client-side) harus sudah terpasang terpisah di folder `mods/`,
versi yang cocok dengan Minecraft 1.21.6.

## Build
1. Install JDK 21.
2. Cek ulang versi di `gradle.properties` (yarn_mappings, loader_version, fabric_version) lewat
   https://fabricmc.net/develop/ — versi di file ini adalah perkiraan, WAJIB dicek ulang karena
   rilis Fabric untuk versi Minecraft baru sering di-update.
3. Jalankan:
   ```
   gradle wrapper --gradle-version 8.10
   ./gradlew build
   ```
4. Hasil jar ada di `build/libs/baritone-scheduler-1.0.0.jar`.
5. Taruh jar itu + jar Baritone di folder `mods/` instalasi Fabric kamu.

## Config
Saat pertama kali dijalankan, mod otomatis membuat file:
```
.minecraft/config/baritone-scheduler.json
```
Contoh isinya ada di `example-config/baritone-scheduler.json` pada project ini. Field:

- `enabled`: master on/off.
- `tasks[]`: daftar jadwal.
  - `target`: nama block (samakan dengan argumen yang biasa kamu ketik setelah `#mine`, bisa lebih dari satu dipisah spasi).
  - `command`: default `"mine"`, bisa diganti command Baritone lain seperti `"farm"`.
  - `intervalMinutes`: setiap berapa menit task ini dipicu ulang.
  - `durationSeconds`: berapa lama mining berjalan sebelum otomatis `#stop` (0 = tidak dibatasi).
  - `enabled`: on/off per task.
- `stopRule`: kapan SELURUH scheduler berhenti otomatis.
  - `mode`: `"NONE"` | `"AFTER_MINUTES"` | `"AT_CLOCK"`.
  - `afterMinutes`: dipakai kalau mode `AFTER_MINUTES` (durasi sejak scheduler di-start).
  - `atClock`: dipakai kalau mode `AT_CLOCK`, format `"HH:mm"` 24 jam berdasarkan jam sistem/real-life, bukan jam in-game.
- `verboseChatLog`: tampilkan log status di chat game.

## Command in-game
- `/bscheduler start` — mulai scheduler (baca config saat itu).
- `/bscheduler stop` — hentikan scheduler + kirim `#stop` ke Baritone.
- `/bscheduler reload` — muat ulang config dari file tanpa restart game.
- `/bscheduler status` — tampilkan status & daftar task aktif.

## Catatan penting
- Ini SEPENUHNYA client-side dan otomatisasi seperti ini biasanya melanggar aturan server survival
  multiplayer (dianggap sejenis auto-mining/cheat). Pakai di server yang memang mengizinkan Baritone,
  atau di singleplayer/LAN.
- `AT_CLOCK` memakai jam sistem komputer kamu (real-life), bukan waktu in-game (siang/malam Minecraft).
  Kalau maksudmu "stop pas malam in-game", bilang saja — logikanya beda dan gampang ditambahkan.
