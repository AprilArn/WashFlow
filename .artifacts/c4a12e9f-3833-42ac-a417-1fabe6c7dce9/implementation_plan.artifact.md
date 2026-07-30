# Sinkronisasi Agent Overlay dan Agent Panel

Rencana ini bertujuan untuk menyinkronkan interaksi antara Agent Overlay (berbasis suara) dan Agent Panel (berbasis teks). Ketika pengguna berbicara melalui Agent Overlay, kueri tersebut akan muncul di Agent Panel, dan status "Thinking" serta jawaban "Typewriter" akan ditampilkan secara bersamaan di kedua komponen tersebut.

## Proposed Changes

### [AiAgentViewModel](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/AiAgentViewModel.kt)

#### [MODIFY] [AiAgentViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/AiAgentViewModel.kt)
- Ekstrak logika pengiriman pesan ke AI dan simulasi typewriter ke dalam fungsi privat baru `executeAiFlow(query: String)`.
- Perbarui fungsi `onSendMessage()` untuk memanggil `executeAiFlow()`.
- Perbarui fungsi `handleFinalSpeechResult(text: String)` untuk:
    - Menambahkan teks hasil STT ke dalam daftar `messages` (Panel).
    - Memperbarui `voiceAgentStatus` ke `THINKING` saat AI sedang memproses.
    - Memanggil `executeAiFlow(text)`.
    - Menampilkan progres typewriter di `voiceAgentText` (Overlay) secara sinkron dengan animasi di Panel.
    - Mengatur alur kembali ke `LISTENING` setelah jawaban selesai diberikan.

### [VoiceAgentOverlay](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/component/VoiceAgentOverlay.kt)

#### [MODIFY] [VoiceAgentOverlay.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/component/VoiceAgentOverlay.kt)
- Pastikan komponen merespons perubahan status `THINKING` dan `ANSWERING` dengan benar. (Sudah ada, tapi perlu dipastikan sinkronisasinya).

## Verification Plan

### Manual Verification
- Jalankan aplikasi.
- Buka Agent Panel.
- Gunakan fitur suara (Voice Agent Overlay) untuk menanyakan sesuatu (misal: "Halo").
- Verifikasi bahwa teks "Halo" muncul di Agent Panel sebagai pesan user.
- Verifikasi bahwa Overlay menunjukkan status "Thinking" saat Panel juga menunjukkan status "Thinking".
- Verifikasi bahwa jawaban AI muncul di Overlay dan Panel secara bersamaan dengan efek typewriter yang sinkron.
- Verifikasi bahwa Overlay kembali ke status "Listening" setelah selesai menjawab.
