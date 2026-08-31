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
   gradle wrapper --gradle-version 8.13
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
- `/bscheduler stop` — hentikan scheduler + kirim `#stop` ke Baritone + jalankan `stopRule.afterCommands`.
- `/bscheduler reload` — muat ulang config dari file tanpa restart game.
- `/bscheduler status` — tampilkan status & daftar task aktif.
- `/bscheduler gui` — **buka layar setting** (tambah/edit/hapus task, atur global settings, langsung tersimpan ke file). Ini cara paling gampang kalau kamu main di HP dan gak enak edit JSON manual.

## Command tambahan setelah mining selesai (afterCommands)
Tiap task dan stopRule sekarang punya field `afterCommands` — daftar command yang dijalankan
BERURUTAN begitu event-nya kejadian, bukan cuma diam:

- `task.afterCommands` → jalan begitu `durationSeconds` task itu habis. Contoh: `["#back", "say Selesai!"]`
  supaya karakter otomatis balik ke titik awal (fitur bawaan Baritone `#back`) lalu kirim chat.
- `stopRule.afterCommands` → jalan begitu SELURUH scheduler berhenti (baik manual `/bscheduler stop`
  maupun otomatis kena `stopRule`). Contoh: `["#back", "/home base", "/logout"]`.

Aturan penulisan tiap command dalam list:
- Diawali `#` → dikirim sebagai command Baritone (`#back`, `#stop`, dll).
- Diawali `/` → command normal Minecraft/server (`/home base`, `/tpa spawn`, `/sell all` — tergantung
  plugin server-nya, sama seperti kalau kamu ketik manual).
- Tanpa awalan → dikirim sebagai chat biasa.

Di GUI (`/bscheduler gui`), isi field "command sesudahnya" dipisah pakai `|`, contoh:
`#back | /home base | say selesai`.

## Catatan versi mod
Field `"version"` di `src/main/resources/fabric.mod.json` sekarang di-hardcode (tidak pakai
placeholder `${version}` lagi, karena templating Gradle-nya sempat gagal dan bikin Fabric Loader
error "Incompatible mods found"). Kalau nanti mau naikkan versi mod, update MANUAL di dua tempat:
`gradle.properties` (`mod_version`) DAN `fabric.mod.json` (`"version"`), harus sama persis.

## Catatan penting
- Ini SEPENUHNYA client-side dan otomatisasi seperti ini biasanya melanggar aturan server survival
  multiplayer (dianggap sejenis auto-mining/cheat). Pakai di server yang memang mengizinkan Baritone,
  atau di singleplayer/LAN.
- `AT_CLOCK` memakai jam sistem komputer kamu (real-life), bukan waktu in-game (siang/malam Minecraft).
  Kalau maksudmu "stop pas malam in-game", bilang saja — logikanya beda dan gampang ditambahkan.
