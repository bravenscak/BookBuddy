# BookBuddy 📚

**BookBuddy** is an Android application for managing your personal book collection and tracking reading progress, built with Kotlin and Material Design.

## Features

**Book Management**
* Add, edit, and delete books with details (title, author, genre, rating)
* Upload custom book covers or auto-fetch from Google Books API
* Track reading status: Want to Read, Currently Reading, Finished, Abandoned

**Modern UI**
* Material Design 3 with Dark/Light theme support
* Smooth animations and responsive design
* Custom splash screen and navigation drawer

**Data & Sync**
* SQLite database with Content Provider
* Google Books API integration for book search
* Background synchronization with WorkManager
* Offline-first functionality

**Settings**
* Multi-language support (Croatian/English)
* Reading reminder notifications
* Configurable preferences

## Tech Stack

* **Kotlin**
* **MVVM + Repository Pattern**
* **SQLite** with Content Provider
* **Retrofit + Gson** (Google Books API)
* **Material Design 3**
* **Navigation Component**
* **WorkManager** (background sync)
* **BroadcastReceiver** (notifications)

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on Android device/emulator (API 26+)

## Architecture

Clean layered architecture following MVVM pattern with separation of concerns:
* UI Layer (Activities, Fragments, Adapters)
* Repository Layer (Data abstraction)
* Data Layer (SQLite, SharedPreferences, Network)

## Project Context

Developed as coursework for the *Mobile Application Development* course, demonstrating modern Android development practices and architectural patterns.
