# Bloom: Women's Wellness App (Demo Task)

Bloom is an Android app for women's health and PCOS-related wellness. This project implements the core experience from the provided Figma design (splash, onboarding, and a four-tab home) and integrates **Google AdMob** with an **App Open Ad** and an **Adaptive Banner Ad**.

Built with **Kotlin**, **Jetpack Compose**, and **MVVM**.

---

## Contents

1. [Features implemented](#features-implemented)
2. [App flow](#app-flow)
3. [Setup instructions](#setup-instructions)
4. [Architecture](#architecture)
5. [Ad implementation approach](#ad-implementation-approach)
6. [Test ad IDs used](#test-ad-ids-used)
7. [Analytics (bonus)](#analytics-bonus)
8. [Project structure](#project-structure)
9. [Manual test checklist](#manual-test-checklist)
10. [Assumptions and limitations](#assumptions-and-limitations)

---

## Features implemented

### Splash and onboarding
- Splash screen with the Bloom logo (drawn with Canvas).
- Four onboarding steps with a segmented progress indicator and animated transitions:
  1. **Welcome**: "Get started"
  2. **A little about you**: first-name input (Continue is disabled until a name is entered)
  3. **What would you like to track daily?**: seven toggles (Meals, Movement, Water, Sleep, Relaxation, Cycle, Symptom check-in)
  4. **When should we remind you?**: Morning / Afternoon / Evening presets plus a custom time picker, with "Enter Bloom" and a "Set up later" link
- System back moves to the previous onboarding step.
- Onboarding completion, name, tracked items, and reminder time are stored with **Jetpack DataStore (Preferences)**.
- Save failures are caught and shown to the user.

### Home (bottom navigation with four tabs)
- **Dashboard**
  - Time-aware greeting and a profile shortcut to Settings
  - "Today's Goals" hero card with live completion count, progress bar, and a "Start here" action that opens the first unfinished goal
  - PCOS FAQ pager with a page indicator
  - Cycle row (shows last period, next expected date, and cycle day once logged) and Symptom check-in row
  - Goal Progress list with animated progress rings (Meals, Movement, Water, Relaxation, Sleep), driven by the goals the user chose to track
  - "Today's tip" card that adapts to the user's progress, with "Remind me later" and "Mark as done"
- **Insights**
  - Score card with a Week/Month toggle and a tappable bar chart (today's bar is computed from real progress)
  - Cycle card with cycle day and progress
  - Daily summary with a per-goal breakdown
  - "Try next" suggestion for the least-complete goal
  - Symptom patterns with sparklines
- **Learn**: expandable FAQ grouped into four sections (Diagnosis & basics, Symptoms & body, Managing it day to day, Looking ahead)
- **Settings**
  - Profile card and Edit Profile
  - Tracking toggles (these show or hide Dashboard goals)
  - Daily goal targets with steppers
  - Notifications switch, Reminder time, Live notification preview, app version
  - Sign out (clears local data and returns to onboarding)

### Bottom sheets (from the Figma "Log" frames)
Log Meals, Log Movement, Log Water, Log Relaxation, Log Sleep, Log Cycle (calendar), Today's Symptoms, Edit Profile, Reminder Time, and Live Notification Preview (with a working five-minute countdown demo).

### Monetization
- Google Mobile Ads SDK integration with configurable ad unit IDs
- App Open Ad (first-launch safe, lifecycle-aware, preloaded)
- Adaptive Banner on the Home screen, placed between the content and the bottom navigation

---

## App flow

```
First launch:      Splash → Onboarding → Home            (no App Open Ad)
Returning launch:  Splash → App Open Ad (if ready) → Home
Foreground event:  Background → Foreground → App Open Ad (if eligible)
```

Foreground App Open Ads are enabled only after onboarding is complete, so a first-time user is never interrupted.

---

## Setup instructions

**Requirements**
- Android Studio (recent stable version) with JDK 17
- Android SDK 35 installed
- A device or emulator running Android 7.0 (API 24) or higher, with Google Play services and an internet connection

**Steps**
1. Clone the repository and open the project in Android Studio.
2. Let Gradle sync finish.
3. Run the `app` configuration on a device or emulator.

**Build an APK**
- Android Studio: *Build → Build Bundle(s) / APK(s) → Build APK(s)*
- Or from the terminal: `./gradlew assembleDebug`
- Output: `app/build/outputs/apk/debug/app-debug.apk`

**Configuring ad IDs**
Ad IDs live in `app/build.gradle.kts` inside `buildTypes`:

```kotlin
manifestPlaceholders["admobAppId"] = "<AdMob app ID>"
buildConfigField("String", "APP_OPEN_AD_UNIT_ID", "\"<App Open unit ID>\"")
buildConfigField("String", "BANNER_AD_UNIT_ID", "\"<Banner unit ID>\"")
```

The app ID is injected into `AndroidManifest.xml`, and the unit IDs are read from `BuildConfig`. The code never hardcodes an ID, so switching to production IDs only requires editing the build file (per build type if needed).

---

## Architecture

**Pattern:** single-activity MVVM with unidirectional data flow.

```
UI (Compose screens)  ←  StateFlow<UiState>  ←  ViewModel  ←  Repository (DataStore)
        │                                            ▲
        └──────────────── user events ───────────────┘
```

- **UI layer:** Jetpack Compose with Material 3. Screens are stateless composables that receive a `UiState` and event callbacks. Reusable components (buttons, rows, progress rings, toggles, steppers, sheets) live in `ui/components` and `ui/home`.
- **State management:** each ViewModel exposes an immutable `StateFlow`, collected in the UI with `collectAsStateWithLifecycle`.
  - `SplashViewModel` resolves the start destination
  - `OnboardingViewModel` handles steps, validation, and saving
  - `HomeViewModel` handles goals, targets, tracking, symptoms, cycle, and profile
- **Data layer:** `UserPreferencesRepository` wraps DataStore Preferences and handles `IOException` so a corrupt read falls back to defaults.
- **Navigation:** Navigation Compose with `splash → onboarding → home`. The splash and onboarding routes are removed from the back stack when they finish.
- **Dependency injection:** a small manual `AppContainer`, created in the `Application` class and shared through a `Context.appContainer()` helper. Hilt was left out to keep setup simple for a demo.
- **Responsiveness:** no hardcoded screen dimensions. Layouts use weights, fill-width modifiers, and scrolling containers. The banner width comes from `BoxWithConstraints`.
- **Compatibility:** `minSdk 24`. Date handling avoids `java.time` and uses epoch-day values with `Calendar`.

---

## Ad implementation approach

All ad code is in the `ads` package.

### App Open Ad (`AppOpenAdManager`)
The manager implements `DefaultLifecycleObserver` (attached to `ProcessLifecycleOwner`) and `Application.ActivityLifecycleCallbacks`.

| Concern | How it is handled |
|---|---|
| Preloading | Starts loading in `BloomApp` once the SDK initializes, for returning users only. Reloads after every dismiss or failure. |
| First-time user | Foreground ads stay disabled until onboarding completes, and nothing is preloaded or shown during the first-launch splash and onboarding. |
| Splash (returning user) | Waits for the ad for at most 2.5 seconds after the minimum splash time, then shows it if ready. Navigation to Home always continues, whether the ad was shown, skipped, failed, or timed out. |
| Never block the user | `awaitAd()` uses a timeout. If the ad loads after the timeout, it stays cached for the next foreground event. |
| Ad fails to load / no internet | State moves to `FAILED`, the user continues normally, and the next foreground event retries. |
| Ad fails to show | The ad is cleared, the user continues, and the next ad is reloaded. |
| User dismisses the ad | The ad is cleared, the next ad is preloaded, and navigation continues. |
| Multiple foreground events | An `isShowingAd` guard prevents overlapping ads, and loading is ignored while a request or a fresh ad already exists. |
| App goes to background while loading | Before showing, the manager checks that the process lifecycle is at least `STARTED`. If not, it skips showing and keeps the cached ad. |
| Expired ads | A cached ad older than 4 hours is treated as unavailable and reloaded. |
| Activity lifecycle | The visible activity is tracked through `ActivityLifecycleCallbacks`, ignoring the ad's own activity. Rotations and in-app navigation don't count as foreground events because the observer is process-level. |
| Sign out | Foreground ads are disabled again so the next launch behaves like a first launch. |

### Adaptive Banner (`AdaptiveBanner`)
- Uses `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize` with the available width, so it fits any screen size.
- Placed in the `Scaffold` bottom bar, **between the page content and the navigation bar**, so it never overlaps important content or navigation.
- Collapses to zero height if loading fails, leaving no empty gap.
- The `AdView` is destroyed when the composable leaves the composition.

---

## Test ad IDs used

Google's official sample IDs are used for all build types during development:

| Purpose | ID |
|---|---|
| AdMob App ID | `ca-app-pub-3940256099942544~3347511713` |
| App Open ad unit | `ca-app-pub-3940256099942544/9257395921` |
| Adaptive banner ad unit | `ca-app-pub-3940256099942544/9214589741` |

Replace these with real IDs before publishing (see [Configuring ad IDs](#setup-instructions)).

---

## Analytics (bonus)

Ad events are logged through `AdAnalytics` to Logcat. Filter by the tag **`BloomAds`**.

| Event | Fired when |
|---|---|
| `app_open_ad_requested` | A load request starts |
| `app_open_ad_loaded` | The ad loads |
| `app_open_ad_failed` | Loading or showing fails (includes error details) |
| `app_open_ad_shown` | The ad becomes visible |
| `app_open_ad_dismissed` | The user closes the ad |
| `banner_ad_loaded` | The banner loads |
| `banner_ad_failed` | The banner fails to load |
| `banner_ad_impression` | The banner records an impression |

`AdAnalytics.log` is the single place to swap in Firebase Analytics or another backend later.


## Manual test checklist

| Scenario | Expected result |
|---|---|
| Fresh install / clear app data → launch | Splash → Onboarding. No App Open Ad. |
| Finish onboarding | Home opens. App Open Ad preloads (see Logcat). |
| Kill and relaunch | Splash → App Open Ad → Home. |
| Press Home, wait, reopen | App Open Ad shows on foreground. |
| Airplane mode, cold start | Ad fails gracefully and Home opens within about 3.5 seconds. |
| Rapid background/foreground | Only one ad appears at a time. |
| Dismiss the ad | Navigation continues and the next ad is preloaded. |
| Banner on Home | Appears above the bottom navigation on all tabs and collapses on failure. |
| Settings → Sign out | Local data is cleared and the app returns to onboarding. |
