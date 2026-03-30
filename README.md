# MusicAI

An Android Music Player app using the Apple iTunes API, built with modern Android development practices.

## Technologies

- **Kotlin** & **Jetpack Compose**
- **MVVM Architecture**
- **Hilt** for Dependency Injection
- **Room** for Offline Caching
- **Retrofit** for Network Calls
- **Navigation Compose**
- **Coil** for Image Loading
- **Coroutines & Flow**

## Getting Started

1. Clone the repository.
2. Open the project in **Android Studio Hedgehog** or newer.
3. Build and Run the `app` module.

## Architecture

The project follows the MVVM pattern with a clean separation of layers:
- **Presentation**: UI components with Jetpack Compose.
- **Domain**: Use Cases and Models (to be implemented).
- **Data**: Repository pattern with Room/Retrofit (to be implemented).
