# Fix VoiceAgentOverlay Wrap Content and Animation

Explain why the Voice Agent overlay is not wrapping content as expected and implement a fix to make it dynamic and smooth.

## User Review Required

> [!IMPORTANT]
> The current implementation uses `Modifier.fillMaxWidth()` inside the `VoiceAgentOverlay`'s `Row`, which forces the overlay to always expand to its maximum allowed width (350.dp). I propose changing this to `wrapContentWidth()` and adding `animateContentSize()` for a better user experience.

## Proposed Changes

### AI Agent Components

#### [AiAgentComponents.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/AiAgentComponents.kt)

- Replace `Modifier.fillMaxWidth()` with `Modifier.wrapContentWidth()` in the main `Row` of `VoiceAgentOverlay`.
- Add `animateContentSize()` to the `Surface` to smoothly transition between sizes when the status or text changes.
- (Optional but recommended) Use `AnimatedContent` for the title and description text to make status transitions feel more "live".

```kotlin
@Composable
fun VoiceAgentOverlay(
    status: VoiceAgentStatus,
    text: String = "",
    modifier: Modifier = Modifier
) {
    // ...
    Surface(
        modifier = modifier
            .widthIn(min = 200.dp, max = 350.dp)
            .wrapContentHeight()
            .animateContentSize(), // Smooth size transition
        // ...
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth() // Changed from fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ...
        }
    }
}
```

## Verification Plan

### Manual Verification
- Use `render_compose_preview` to verify the `VoiceAgentOverlay` behavior with different statuses.
- Create a temporary preview function if necessary to test `LISTENING`, `PROCESSING`, and `ANSWERING` states side-by-side.
- Verify that the width now adjusts based on the content (e.g., "Listening..." should be narrower than a long "ANSWERING" text).
