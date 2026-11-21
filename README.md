# Overview

**Note Keeper App** is a native Android mobile application developed in Kotlin using Jetpack Compose. As a software engineer seeking to build practical, user-centric mobile solutions, I designed this app to provide a clean, responsive, and reliable personal note-taking experience.

The application allows users to create, view, edit, and delete notes with custom titles and content. It implements persistent local storage using Android's `SharedPreferences` combined with JSON serialization, ensuring that user data remains intact even after closing the application or restarting the device. Additionally, it features multi-screen navigation between the main note manager and an informational "About" screen.

[Software Demo Video](https://youtu.be/PbLg68VMz4M)

# Development Environment

* **IDE:** Android Studio
* **Programming Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Data Persistence & Serialization:** Android `SharedPreferences` and `org.json` (`JSONArray`, `JSONObject`)
* **Testing Environment:** Android Virtual Device (Pixel 6 Emulator running Android API 35)[cite: 1, 3]
* **Version Control:** Git & GitHub

# Useful Websites

* [Android Developers - Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
* [Android Developers - Save Simple Data with SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences)
* [Kotlin Official Documentation](https://kotlinlang.org/docs/home.html)
* [Android Studio User Guide](https://developer.android.com/studio/intro)

# Future Work

* **Search and Filter Functionality:** Add a search bar to quickly filter saved notes by title or keyword.
* **Categories and Color Tags:** Allow users to organize notes into custom folders or assign color-coded labels.
* **Cloud Synchronization:** Integrate Firebase Firestore or a REST API to sync notes across multiple devices.
* **Timestamps:** Automatically record and display the creation and last-modified dates for each note.
