# Insert Sunrise and Sunset into Forecast Panel

This plan outlines the changes needed to fetch sunrise and sunset data from the Google Weather Daily Forecast API and display them as sorted events in the horizontal forecast panel.

## User Review Required

- **Icons**: I plan to use `Icons.Default.WbSunny` for Sunrise and `Icons.Default.WbTwilight` (from extended icons) for Sunset. Please confirm if these icons are acceptable.
- **Parsing**: I assume the API returns ISO 8601 strings for `sunriseTime` and `sunsetTime`. I will implement a parser to convert these to local time based on the response's timezone.

## Proposed Changes

### Home UI

#### [HomeViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/home/HomeViewModel.kt)

- Update `fetchWeatherData` to call `weatherApiService.getDailyForecastData(lat, lon, days = 2)`.
- Extract and format `sunriseTime` and `sunsetTime` from the response.
- Store formatted sunrise/sunset times in `SharedPreferences` (e.g., `SUNRISE_TIME`, `SUNSET_TIME`).
- Update `injectEvents` to read these values and create `HourlyForecastUiState` objects for them.
- Ensure proper sorting in `injectEvents` including `WS_SUNRISE` and `WS_SUNSET`.

#### [WeatherPanel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/home/WeatherPanel.kt)

- Update `HorizontalForecastItem` to handle `WS_SUNRISE` and `WS_SUNSET` icon URLs.
- Map them to `Icons.Default.WbSunny` and `Icons.Default.WbTwilight`.

## Verification Plan

### Automated Tests
- I will verify the changes by running the app and checking the horizontal forecast panel.
- Since I cannot easily run unit tests that require the API, I will rely on logging to verify the data fetching and parsing.

### Manual Verification
- Deploy the app to a device/emulator.
- Check if "Sunrise" and "Sunset" appear in the forecast panel.
- Verify they are correctly sorted among the hourly weather data.
- Verify the icons and labels are displayed correctly.
