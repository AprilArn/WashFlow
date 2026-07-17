# Walkthrough - Speech to Text (STT) Integration

I have integrated Android's native Speech-to-Text (STT) into the Voice Agent overlay. This allows for real-time voice interaction with a continuous loop of listening and answering.

## Changes Made

### Core STT Integration
- **[SpeechToTextManager.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/SpeechToTextManager.kt)**: A new helper class that wraps `android.speech.SpeechRecognizer`. It handles partial results (for real-time preview) and final results.
- **[AndroidManifest.xml](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/AndroidManifest.xml)**: Added `RECORD_AUDIO` permission.

### Logic & State Management
- **[AiAgentViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/AiAgentViewModel.kt)**:
    - Updated `onStartVoiceAgent` to initialize and start the `SpeechToTextManager`.
    - Implemented `handleFinalSpeechResult` to transition to `ANSWERING` mode with a dummy response before looping back to `LISTENING`.
    - Added automatic transition to `IDLE` if no speech is detected (timeout/no match).

### UI Enhancements
- **[VoiceAgentOverlay.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/component/VoiceAgentOverlay.kt)**: Now displays the captured text in real-time during the `LISTENING` phase.
- **[MainAppScreen.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/MainAppScreen.kt)**:
    - Added a `permissionLauncher` to handle the runtime microphone permission request.
    - Updated the long-press action on the AI icon to check for permissions before starting the voice agent.

## Cleanup
- Removed temporary debug logs and unused imports across all modified files to ensure a clean production-ready codebase.

## Verification Results

### Automated Check
- The project structure and imports have been verified.
- The `SpeechRecognizer` API is used following Android standards.

### Manual Verification Steps
To test the feature on a physical device or emulator with microphone support:

1.  **Trigger**: Long-press the AI icon in the top header.
2.  **Permission**: Grant microphone permission if prompted.
3.  **Listening**: Speak into the microphone. You should see your words appearing in real-time within the overlay.
4.  **Answering**: Stop speaking. The overlay should change to "Aira" and show "Siappp, saya proses ya...".
5.  **Loop**: After 2 seconds, it should return to "Listening..." automatically.
6.  **Idle**: Stop speaking and wait. The overlay should disappear automatically once the system detects silence.

> [!TIP]
> The STT is configured to use Indonesian (`id-ID`) for better accuracy with local context.
