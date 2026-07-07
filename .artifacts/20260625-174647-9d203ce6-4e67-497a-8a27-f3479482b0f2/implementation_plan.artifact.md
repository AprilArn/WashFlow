# Improve Fuzzy Matching Lenience

Enhance the customer matching logic to handle more typo cases, specifically for short strings like phone numbers and variations of names.

## Proposed Changes

### Utilities

#### [StringSimilarityUtils.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/utils/StringSimilarityUtils.kt)

- Update `similarityScore` to trim strings before comparison.

### UI Components

#### [AiAgentComponents.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/aiagent/AiAgentComponents.kt)

- Refine `LaunchedEffect` auto-match logic:
    - Add `contact.contains` and `input.contains(name)` checks.
    - Lower fuzzy matching threshold from 0.7 to 0.5 to catch shorter string typos (e.g., "123" vs "124").
    - Ensure both `editedName` and `editedValue` are updated from the match.

## Verification Plan

### Automated Tests
- Update `StringSimilarityUtilsTest.kt` with the new cases:
    - "Raphaels" vs "Raphael"
    - "124" vs "123"
    - "Kirto" vs "Kirito"
- Run tests: `./gradlew :app:testProductionDebugUnitTest --tests "com.aprilarn.washflow.utils.StringSimilarityUtilsTest"`

### Manual Verification
- Deploy the app.
- Test with "hapus 124" and verify it matches "123" (if unique/high score).
- Test with "hapus Raphaels" and verify it matches "Raphael".
- Test with "hapus kirto" and verify it matches "Kirito".
