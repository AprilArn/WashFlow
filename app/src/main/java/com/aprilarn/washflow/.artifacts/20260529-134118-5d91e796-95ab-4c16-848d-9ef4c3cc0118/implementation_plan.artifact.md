# Implementing Metadata Counters for Optimized Firestore Reads

This plan outlines the implementation of a metadata counter system to optimize Firestore reads, especially for the "Data Table" screen which currently fetches entire collections just to display their counts.

## Proposed Changes

### [Data Models]

#### [FirebaseCollection.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/model/FirebaseCollection.kt)

- Add `WorkspaceMetadata` data class to represent the counts of various collections.

```kotlin
data class WorkspaceMetadata(
    val customerCount: Int = 0,
    val serviceCount: Int = 0,
    val itemCount: Int = 0,
    val orderCount: Int = 0
)
```

---

### [Repositories]

#### [WorkspaceRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/WorkspaceRepository.kt)

- Add `getMetadataRealtime(workspaceId: String)` to listen to the metadata document.
- Add `syncMetadata(workspaceId: String)` to initialize or recalculate counts by performing a one-time full count.

#### [CustomerRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/CustomerRepository.kt)

- Update `addCustomer` to use a Firestore Batch to increment `customerCount` in the metadata document.
- Update `deleteCustomer` to use a Firestore Batch to decrement `customerCount`.

#### [ServiceRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/ServiceRepository.kt)

- Update `addService` to use a Firestore Batch to increment `serviceCount`.
- Update `deleteService` to use a Firestore Batch to decrement `serviceCount`. Note: When deleting a service, items are also deleted, so `itemCount` must be decremented by the number of items removed.

#### [ItemRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/ItemRepository.kt)

- Update `addItem` to use a Firestore Batch to increment `itemCount`.
- Update `deleteItems` to use a Firestore Batch to decrement `itemCount`.

#### [OrderRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/OrderRepository.kt)

- Update `createOrder` to increment `orderCount`.
- Update `deleteOrder` to decrement `orderCount`.

---

### [UI / ViewModels]

#### [TableDataViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/tabledata/TableDataViewModel.kt)

- Replace the `combine` logic that fetches all lists with a single listener to `getMetadataRealtime`.
- Add a trigger to call `syncMetadata` if the metadata document doesn't exist or as a maintenance action.

## Verification Plan

### Automated Tests
- I will verify the implementation by checking the logs and UI behavior.

### Manual Verification
1.  **Initial Sync:** Verify that the counts are correctly initialized for an existing workspace.
2.  **Add/Delete Operations:**
    - Add a customer and verify `customerCount` increments.
    - Delete a customer and verify it decrements.
    - Repeat for Services and Items.
    - Specifically check Service deletion: deleting a service should decrement `serviceCount` by 1 and `itemCount` by the number of linked items.
3.  **Real-time Update:** Open the app on two "simulated" instances (or just check if UI updates immediately after a write) to ensure `TableDataScreen` reflects changes made by writes.
4.  **Cost Verification:** (Simulated) Observe that moving to `TableDataScreen` no longer triggers multiple read logs for every document in the collections.
