# Implementation Plan - Speech to Text (STT) for Agent Overlay

Implement Android's built-in Speech-to-Text functionality to allow the user to interact with the Voice Agent using voice. The captured text will be displayed in real-time in the agent overlay's listening mode.

## User Review Required

> [!IMPORTANT]
> The app will now require the `RECORD_AUDIO` permission. A permission request dialog will appear when the user first attempts to use the Voice Agent (via long-press on the AI icon).

## Proposed Changes

### Configuration & Permissions

#### [MODIFY] [AndroidManifest.xml](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.RECORD_AUDIO" />` to allow the app to capture audio for STT.

### STT Logic

#### [NEW] [SpeechToTextManager.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/SpeechToTextManager.kt)
- Create a helper class to manage `android.speech.SpeechRecognizer`.
- Provide methods to start and stop listening.
- Implement `RecognitionListener` to handle:
    - `onPartialResults`: Update preview text.
    - `onResults`: Capture final text and trigger `ANSWERING` mode.
    - `onError`: Handle silence timeout to trigger `IDLE` mode.
- Support Indonesian (`id-ID`) as the primary language.

### UI State & ViewModel

#### [MODIFY] [AiAgentViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/AiAgentViewModel.kt)
- Integrate `SpeechToTextManager`.
- Update `onStartVoiceAgent` to initiate the STT flow.
- Implement flow logic:
    1.  `LISTENING`: Update `voiceAgentText` with partial results.
    2.  `Speech Detected & Finished`: Transition to `ANSWERING`.
    3.  `ANSWERING`: Show dummy response ("Siappp, saya proses ya...") for a few seconds.
    4.  `Transition Back`: Return to `LISTENING`.
    5.  `Silence/Error`: If no speech detected during `LISTENING`, transition to `IDLE`.

### UI Components

#### [MODIFY] [VoiceAgentOverlay.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/component/VoiceAgentOverlay.kt)
- Update the component to display `voiceAgentText` when the status is `LISTENING`.
- Ensure the overlay can display text in both `LISTENING` (preview) and `ANSWERING` (response) modes.

#### [MODIFY] [MainAppScreen.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/MainAppScreen.kt)
- Add a permission launcher for `RECORD_AUDIO`.
- Update the `onAiAgentLongClick` handler in the `Header` to check for permissions and call `aiAgentViewModel.onStartVoiceAgent()`.

## Verification Plan

### Manual Verification
1.  **Permission Request**: Long-press the AI icon. Verify permission dialog.
2.  **Listening Mode**: Speak and verify real-time text preview in the overlay.
3.  **Answering Transition**: After finishing speech, verify it switches to "Aira" with a dummy response.
4.  **Looping**: After the response, verify it returns to "Listening...".
5.  **Auto-Idle**: Stop speaking and wait for silence. Verify the overlay disappears (`IDLE`).
