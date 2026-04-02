# MusicAI

A modern Android music player that integrates with the iTunes Search API, featuring song discovery, audio preview playback, and album exploration — built with a scalable Clean Architecture.

---

## Screens & Features

### Songs Screen
The home screen of the application.

- **Recent songs list** — displays the last tracks played, kept in sync via a local Room database
- **Search** — toggleable search bar that queries the iTunes API as the user types and confirms
- **Pull-to-refresh** — refreshes recent songs or re-fetches search results, invalidating the local cache
- **Infinite pagination** — automatically loads more results as the user scrolls near the end of the list
- **Song options sheet** — bottom sheet triggered via the "more" icon, with a "View Album" shortcut
- **Offline feedback** — toasts informing the user when connectivity is unavailable

### Player Screen
Opened when tapping any song.

- **Audio preview** — streams the 30-second iTunes preview via Android's `MediaPlayer`
- **Playback controls** — play/pause, skip to next/previous song in the current queue, loop toggle
- **Progress slider** — seekable scrubber showing elapsed and remaining time
- **Album artwork** — high-resolution cover loaded via Coil (automatically upgraded from 100px to 600px)
- **Queue navigation** — next/previous arrows jump through the full list from which the song was opened
- **Album shortcut** — navigates directly to the album screen from the player
- **Error feedback** — toasts for unavailable previews, playback errors, and connectivity issues

### Album Screen
Opened from the song options sheet or the player.

- **Album header** — large artwork, collection name, and artist name
- **Full track list** — all songs from the album, each with thumbnail and duration
- **Retry** — visible when the album fails to load, allowing the user to try again without leaving the screen
- **Offline feedback** — toasts when connectivity is unavailable

---

## Architecture

MusicAI is built on **Clean Architecture** with a strict separation into three layers, combined with the **MVVM** pattern in the presentation layer. This separation makes each layer independently testable, replaceable, and extensible — so adding new features does not require touching unrelated code.

```
┌─────────────────────────────────────┐
│         Presentation Layer          │  Compose screens + ViewModels
├─────────────────────────────────────┤
│           Domain Layer              │  Use cases + Domain models
├─────────────────────────────────────┤
│            Data Layer               │  Repository + Room + Retrofit
└─────────────────────────────────────┘
```

### Why This Architecture Scales

- **Domain layer has zero Android dependencies** — use cases depend only on Kotlin and domain interfaces, making them trivially unit-testable and portable
- **Repository pattern abstracts the data source** — swapping the remote API or the local database requires changes in only one class, without any impact on the domain or presentation layers
- **ViewModel interfaces decouple screens from implementations** — tests inject fakes, production uses Hilt-injected implementations; the screen never knows the difference
- **Separated event flows** — `navigationEvents` (screen transitions) and `messageEvents` (toasts/snackbars) are distinct `SharedFlow`s, so each concern is handled independently and never interferes with the other
- **Feature-first package structure** — each feature (`songs`, `player`, `album`) is fully self-contained, so teams can work in parallel and features can be extracted into modules with minimal friction
- **Hilt modules per layer** — `DataModule`, `DomainModule`, `PlayerModule`, and `UtilsModule` each own their bindings; adding a new feature only requires touching the relevant module

---

### Folder & File Layout

```
app/src/main/kotlin/com/musicai/
│
├── ui/                                 # Presentation layer
│   ├── songs/                          # Songs feature
│   │   ├── SongsScreen.kt
│   │   ├── MoreOptionsSheet.kt
│   │   └── model/
│   │       ├── SongsViewModel.kt       # Interface + implementation
│   │       ├── SongsState.kt
│   │       ├── SongsNavigationEvent.kt
│   │       └── SongsMessageEvent.kt
│   │
│   ├── player/                         # Player feature
│   │   ├── PlayerScreen.kt
│   │   └── model/
│   │       ├── PlayerViewModel.kt
│   │       ├── PlayerState.kt
│   │       ├── PlayerNavigationEvent.kt
│   │       └── PlayerMessagesEvent.kt
│   │
│   ├── album/                          # Album feature
│   │   ├── AlbumScreen.kt
│   │   └── model/
│   │       ├── AlbumViewModel.kt
│   │       ├── AlbumState.kt
│   │       └── AlbumMessageEvent.kt
│   │
│   ├── shared/                         # Cross-feature code
│   │   ├── components/                 # Reusable Compose components
│   │   ├── model/
│   │   │   └── ConsumableEvent.kt
│   │   ├── navigation/
│   │   │   ├── AppNavHost.kt
│   │   │   └── Routes.kt
│   │   └── PlayerController.kt         # In-memory queue manager
│   │
│   └── theme/                          # Design system
│       ├── Color.kt
│       ├── Theme.kt
│       ├── MusicTokens.kt
│       ├── Typography.kt
│       └── MusicAIRoot.kt
│
├── domain/                             # Domain layer
│   ├── model/                          # Pure Kotlin models
│   │   ├── Song.kt
│   │   ├── Album.kt
│   │   └── PaginatedSearch.kt
│   ├── repository/
│   │   └── SongRepository.kt           # Interface only
│   ├── usecase/
│   │   ├── SearchSongsUseCase.kt
│   │   ├── GetRecentSongsUseCase.kt
│   │   ├── GetAlbumSongsUseCase.kt
│   │   └── SaveRecentSongUseCase.kt
│   └── di/
│       └── DomainModule.kt
│
├── data/                               # Data layer
│   ├── api/
│   │   ├── remote/
│   │   │   └── ItunesApiService.kt     # Retrofit interface
│   │   └── local/
│   │       ├── SongDao.kt
│   │       └── RecentSongDao.kt
│   ├── model/
│   │   ├── SongEntity.kt               # Room entity
│   │   ├── RecentSongEntity.kt
│   │   ├── SearchSession.kt            # In-memory pagination buffer
│   │   └── ...                         # Response + mapper models
│   ├── repository/
│   │   └── SongRepositoryImpl.kt
│   └── di/
│       └── DataModule.kt
│
└── plugin/                             # Infrastructure utilities
    ├── audioPlayer/
    │   ├── AudioPlayer.kt              # Interface
    │   └── MediaAudioPlayer.kt         # MediaPlayer implementation
    ├── utils/
    │   ├── ConnectivityManager.kt
    │   ├── Logger.kt
    │   └── DurationUtils.kt
    └── di/
        ├── PlayerModule.kt
        └── UtilsModule.kt
```

---

## Design Organization

### Color Palette

The app uses a single dark theme defined in `Color.kt`:

| Token | Hex | Usage |
|-------|-----|-------|
| `ColorBackground` | `#0D0F14` | Screen backgrounds |
| `ColorSurface` | `#1A1D23` | Cards and surfaces |
| `ColorSurfaceVariant` | `#252830` | Inputs, secondary surfaces |
| `ColorOnBackground` | `#FFFFFF` | Primary text |
| `ColorOnSurfaceVariant` | `#8B8B8B` | Secondary/muted text |
| `ColorSheetBackground` | `#262626` | Bottom sheets |

### Design Tokens

All spacing, sizing, and shape values live in `MusicTokens.kt` and are exposed through a `CompositionLocal` provider (`MusicTheme`), so any composable can access them without hardcoding values. This creates a single source of truth for the entire design system.

**Spacing scale:**
`none` · `xSmall (4dp)` · `small (8dp)` · `medium (16dp)` · `intermediate (20dp)` · `large (24dp)` · `xLarge (32dp)` · `xxLarge (40dp)` · `xxxLarge (52dp)`

**Radius scale:**
`none` · `extraSmall (4dp)` · `small (8dp)` · `medium (12dp)` · `large (20dp)` · `full (100dp)`

**Icon sizes:**
`small (24dp)` · `medium (36dp)` · `large (48dp)` · `xLarge (72dp)`

**Component tokens** (semantic aliases for common UI measurements):

| Token | Value | Usage |
|-------|-------|-------|
| `topBarHeight` | 72dp | All screen top bars |
| `albumArtworkSize` | 120dp | Album screen header |
| `listItemArtworkSize` | 52dp | Song list thumbnails |
| `trackThumbnailSize` | 44dp | Player track thumbnails |
| `horizontalDividerThickness` | 0.5dp | List separators |

Usage example in any composable:
```kotlin
Modifier.height(MusicTheme.component.topBarHeight)
Modifier.padding(horizontal = MusicTheme.spacing.medium)
```

### Component Library

Shared components live in `ui/shared/components/` and are used across all three features:

| Component | Purpose |
|-----------|---------|
| `ContentStateWrapper` | Unified loading → error → content state machine |
| `AppLoadingIndicator` | Centered circular progress indicator |
| `AppErrorState` | Error message with optional retry button |
| `SongListItem` | Song row with artwork, track info, and "more" icon |
| `SongLoadingItem` | Skeleton placeholder while paginating |
| `RoundedArtwork` | Coil image loader with rounded corners and fallback |
| `SongInfoDisplay` | Track name + artist name stacked label |
| `ScreenTopBar` | Back-button app bar with title |

---

## Pagination Logic

### The iTunes API Problem

The iTunes Search API does not support true offset pagination. A single request returns all results up to the requested `limit` — there is no `offset` or `page` parameter. Requesting "page 2" the naive way (bumping the limit) re-fetches everything from the beginning, which is slow, wasteful, and breaks the user experience for large result sets.

### The Solution: In-Memory Slot Buffering

`SongRepositoryImpl` implements a virtual pagination layer on top of the flat API response using a `SearchSession` model as an in-memory buffer:

```
iTunes API  ──►  SearchSession buffer (up to 200 items)  ──►  UI pages (20 items each)
              └── Room DB (persistent cache for offline recovery)
```

**Key constants:**

```kotlin
DISPLAY_PAGE_SIZE = 20   // Items shown per page in the UI
FETCH_SLOT_SIZE   = 40   // Items fetched from the API per network call
PAGES_PER_SLOT    = 2    // A new network slot is fetched every 2 UI pages consumed
MAX_CACHED_ITEMS  = 200  // Upper ceiling of the in-memory buffer
```

**How a page request flows:**

1. User scrolls near the end of the list → `onLoadMore()` → `searchSongs(query, page = N)`
2. `SearchSession.pageAt(N)` calculates the buffer offset and checks if the items are already available
3. **Buffer hit** → returns the 20-item slice immediately with no network call
4. **Buffer miss** → calls `fetchNextSlot()`, which requests `buffer.size + FETCH_SLOT_SIZE` items from iTunes, deduplicates the new results, appends them to the buffer, and persists them to Room DB
5. The requested page is now served from the refreshed buffer
6. `prefetchIfNeeded()` runs asynchronously in the background every 2 consumed pages, keeping the buffer ahead of the user and ensuring smooth scrolling

**Deduplication strategy (applied in order):**
1. Filter by `kind == "song"` to exclude non-music results that iTunes sometimes includes
2. Deduplicate within the API response itself (iTunes occasionally returns the same track more than once)
3. Deduplicate against the existing session buffer by `trackId`

**Offline recovery:**
If the network call fails, the repository falls back to the Room DB cache for the requested page offset. If the cache is also empty, the error is propagated to the ViewModel, which emits a `messageEvent` so the screen can display appropriate feedback without crashing.

**Refresh (pull-to-refresh):**
`refreshSearch()` clears the `SearchSession` object and deletes the cached rows in Room for that query, then re-fetches from scratch — ensuring the user always gets fresh results when explicitly requesting a refresh.

---

## Testing Approach

### No Mocks — Hand-Written Fakes

The test suite relies entirely on **hand-written fakes** rather than mocking frameworks (Mockito, MockK, etc.). Every external dependency has a dedicated fake class that implements the same interface as the production counterpart.

This approach has several advantages over mocks:
- Fakes are explicit and readable — there is no annotation magic or implicit behavior
- Fakes can hold real state, making multi-step tests (e.g. "call once, change result, call again") straightforward to express
- Compilation catches interface drift immediately — if a production interface changes, every fake that implements it breaks at compile time, not at runtime
- Fakes are reusable across all test classes without any shared setup boilerplate

### Fake Inventory

```
app/src/test/kotlin/
├── com/musicai/ui/utils/fakes/
│   ├── FakeConnectivityChecker.kt
│   ├── FakeSearchSongsUseCase.kt
│   ├── FakeGetRecentSongsUseCase.kt
│   ├── FakeGetAlbumSongsUseCase.kt
│   ├── FakeSaveRecentSongUseCase.kt
│   ├── FakeSongRepository.kt
│   └── MutedLogger.kt
└── com/musicai/data/utils/fakes/
    ├── FakeItunesApiService.kt
    ├── FakeSongDao.kt
    └── FakeRecentSongDao.kt
```

### How Each Fake Works

**`FakeConnectivityChecker`**
Controls the connectivity state via `setConnected(Boolean)`. Tests that verify offline behavior simply call `setConnected(false)` before the action under test.

```kotlin
connectivityChecker.setConnected(false)
viewModel.onSearch()
// assert NoConnectionError event was emitted
```

---

**`FakeSearchSongsUseCase` / `FakeGetAlbumSongsUseCase` / `FakeGetRecentSongsUseCase`**
Each use case fake exposes `setSuccess(...)` and `setError(...)` to control the next result, plus read-only counters (`invokeCalls`, `lastQuery`, `lastCollectionId`, etc.) to assert that the ViewModel called the use case with the expected arguments.

```kotlin
searchSongs.setSuccess(PaginatedSearch(songs, hasMore = true))
viewModel.onSearch()
assertEquals(1, searchSongs.invokeCalls)
assertEquals("Beatles", searchSongs.lastQuery)
```

---

**`FakeSongRepository`**
A sealed class with three concrete variants, covering all behavioral states the repository can be in:

| Variant | Behaviour |
|---------|-----------|
| `FakeSongRepository.Success` | Returns configurable song lists and paginated results |
| `FakeSongRepository.Empty` | Returns success with empty collections |
| `FakeSongRepository.Error` | Returns `Result.failure(throwable)` for every operation |

All variants share a common base that tracks call counts and last arguments, so assertions remain identical regardless of which variant is used:

```kotlin
val repo = FakeSongRepository.Error(IOException("timeout"))
// inject into use case under test
// assert the use case propagates the failure correctly
```

---

**`FakeItunesApiService`**
Supports a **queue of sequential responses** via `addSearchResponse(...)`, which are consumed in order. Once the queue is empty, it falls back to a `defaultSearchResponse`. An `error` property can be set to make every call throw, testing network failure paths at the repository level.

```kotlin
api.addSearchResponse(getMockSearchResponse(count = 40)) // first call
api.addSearchResponse(getMockSearchResponse(count = 10)) // second call (prefetch)
```

---

**`MutedLogger`**
A no-op implementation of the `Logger` interface. Injected into all ViewModels and the repository during tests to suppress log output and satisfy the constructor without side effects.

---

### Test Structure by Layer

| Layer | What is tested | Dependencies replaced by |
|-------|---------------|--------------------------|
| `SongsViewModelTest` | ViewModel state + event emission | `FakeSearchSongsUseCase`, `FakeGetRecentSongsUseCase`, `FakeConnectivityChecker`, `MutedLogger` |
| `AlbumViewModelTest` | ViewModel state + event emission | `FakeGetAlbumSongsUseCase`, `FakeConnectivityChecker`, `MutedLogger` |
| `SongRepositoryImplTest` | Pagination logic, cache recovery, deduplication | `FakeItunesApiService`, `FakeSongDao`, `FakeRecentSongDao`, `MutedLogger` |
| `SearchSongsUseCaseTest` | Use case delegation | `FakeSongRepository` |
| `GetAlbumSongsUseCaseTest` | Use case delegation + Album model mapping | `FakeSongRepository` |
| `GetRecentSongsUseCaseTest` | Use case delegation | `FakeSongRepository` |
| `SaveRecentSongUseCaseTest` | Use case delegation | `FakeSongRepository` |

### Coroutine Testing

All ViewModel and repository tests run inside `runTest` from `kotlinx-coroutines-test` with a `MainDispatcherRule` that replaces the main dispatcher with `UnconfinedTestDispatcher`. This makes coroutines run synchronously and deterministically, without any real delays or threading.

For tests that assert on `SharedFlow` event emissions, the collection is started in a `launch` block before triggering the action, followed by `runCurrent()` to ensure the collector is subscribed, and `advanceUntilIdle()` to drain all pending coroutine work before asserting:

```kotlin
val receivedEvents = mutableListOf<SongsMessageEvent>()
val job = launch { viewModel.messageEvents.collect { receivedEvents.add(it) } }
runCurrent()                  // ensure collector is subscribed
viewModel.onToggleSearch()    // trigger the action
advanceUntilIdle()            // drain all coroutine work

assertTrue(receivedEvents.any { it is SongsMessageEvent.NoConnectionError })
job.cancel()
```

---

## How to Run

### Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35
- An Android device or emulator running Android 7.0+ (API 24)

### Steps

```bash
# 1. Clone the repository
git clone <repository-url>
cd MusicAI

# 2. Open in Android Studio
#    File → Open → select the MusicAI directory

# 3. Let Gradle sync automatically
#    (or: Build → Sync Project with Gradle Files)

# 4. Run the app
#    Select the 'app' run configuration and press Run ▶
```

No API keys are required — the iTunes Search API is public and does not require authentication.

### Build Variants

| Variant | Purpose |
|---------|---------|
| `debug` | Development builds with full logging |
| `release` | Optimized builds with R8 minification |

### Running Tests

```bash
# All unit tests (debug + release)
./gradlew test

# Debug unit tests only
./gradlew testDebugUnitTest
```

---

## Next Steps

- **Default song artwork when offline** — show a local placeholder image when artwork cannot be loaded due to no connectivity, instead of displaying a broken or empty image state
- **Display albums from local cache** — when a user opens an album without internet, serve the track list from the Room DB if it was previously fetched, rather than showing an error screen
- **Clear recent search history** — add a text button ("Clear history") at the bottom of the recent songs list that wipes the `RecentSongEntity` table and resets the home screen to its empty state
