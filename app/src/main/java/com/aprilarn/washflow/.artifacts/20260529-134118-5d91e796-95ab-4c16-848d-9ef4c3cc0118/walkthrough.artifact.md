# Walkthrough - Optimized Firestore Reads with Metadata Counters

I have implemented a metadata counter system to significantly reduce Firestore read costs and improve performance for the "Data Table" screen.

## Changes Made

### 1. Data Model Update
- Added `WorkspaceMetadata` to [FirebaseCollection.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/model/FirebaseCollection.kt) to store counts for customers, services, items, and orders.

### 2. Repository Enhancements
- Updated [CustomerRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/CustomerRepository.kt), [ServiceRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/ServiceRepository.kt), [ItemRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/ItemRepository.kt), and [OrderRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/OrderRepository.kt) to use Firestore Batches.
- These batches now atomically increment or decrement the counts in the metadata document whenever data is added or deleted.

### 3. Workspace Repository Additions
- Added `getMetadataRealtime` to [WorkspaceRepository.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/data/repository/WorkspaceRepository.kt) to listen to the single metadata document.
- Added `syncMetadata` to recalculate counts from scratch, ensuring data integrity for existing workspaces.

### 4. UI Optimization
- Refactored [TableDataViewModel.kt](file:///C:/Personal/Projects/version-control/WashFlow/app/src/main/java/com/aprilarn/washflow/ui/tabledata/TableDataViewModel.kt) to use the metadata listener.
- This replaces the previous approach of fetching three entire collections just to show their sizes, reducing read operations from hundreds or thousands down to just **one**.

## Verification Summary
- The implementation uses atomic batches, ensuring that the count always matches the actual number of documents.
- The `syncMetadata` function provides a fallback to initialize data for users who already have existing documents.
- Real-time updates are maintained via the snapshot listener on the metadata document.
