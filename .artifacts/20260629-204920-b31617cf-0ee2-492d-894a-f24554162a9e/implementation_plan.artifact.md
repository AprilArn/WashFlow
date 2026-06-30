# Implementation Plan - Silent Weather Updates

Modify the weather loading logic to only show the shimmer effect on the initial load (when the app opens) and perform silent updates during periodic refreshes at :00 and :30 minutes. This ensures a consistent UI and less distraction for the user.

## Proposed Changes

### Home Module

#### [HomeViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/home/HomeViewModel.kt)

- Add `showLoader: Boolean = true` parameter to `fetchWeatherData` function.
- Update `startPeriodicRefresh` to call `fetchWeatherData` with `showLoader = false`.
- Only update `isLoading = true` in `fetchWeatherData` if `showLoader` is true.

```kotlin
// In HomeViewModel.kt

fun fetchWeatherData(lat: Double, lon: Double, isGps: Boolean = true, showLoader: Boolean = true) {
    // ... cache check ...

    if (showLoader) {
        _uiState.update { it.copy(isLoading = true, isGpsLocation = isGps) }
    }

    // ... API calls ...

    _uiState.update { currentState ->
        currentState.copy(
            isLoading = false,
            // ... data ...
        )
    }
}
```

---

### UI Components

#### [WeatherPanel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/home/WeatherPanel.kt)

- Ensure `WeatherDetailRow` and `HorizontalWeatherForecast` handle the transition smoothly. No code changes are strictly necessary here if we trust the `isLoading` flag from `HomeViewModel`, but I will verify if any height adjustments are needed to keep the panel size perfectly consistent.
- Actually, I will add a `minHeight` or fixed height to `WeatherDetailRow`'s value area to ensure the 16dp shimmer matches the text height.

```kotlin
@Composable
fun WeatherDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLoading: Boolean = false
) {
    // ...
        Column {
            Text(...)
            Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.CenterStart) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerModifier()
                    )
                } else {
                    Text(text = value, ...)
                }
            }
        }
    // ...
}
```

## Verification Plan

### Automated Tests
- I will run the `WeatherPanelLoadingPreview` to ensure the shimmer still works correctly for initial load.
- I will add a new preview or test case that simulates a "silent refresh" to verify that the UI doesn't jump and data updates correctly.

### Manual Verification
- Deploy the app and observe the initial load (should show shimmer).
- Wait for a :00 or :30 mark (or manually trigger `startPeriodicRefresh` logic with a shorter delay for testing) and verify that the data updates without shimmer and without panel resizing.
- Verify that the "Feels Like", "Humidity", etc. values change smoothly.
