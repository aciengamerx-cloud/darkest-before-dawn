# Darkest Before Dawn (Android Remake)

A modern survival thriller remake of the classic horror game **Darkest Before Dawn**, fully rewritten using **Kotlin** and **Jetpack Compose (Material Design 3)** for Android devices.

## Core Gameplay Mechanics

-   **Stay in the Light:** The player's survival relies entirely on proximity to warm yellow light sources. 
    -   **Within Security Radius (<60dp):** The player is safe. Health regenerates steadily at `1.8` points per second.
    -   **In Darkness (>60dp):** The screen dynamic vignettes into pitch-black and blood-red. Health drains continuously; the further you venture into the dark, the faster you perish.
-   **Elevator Scaling:** Scale through 4 progressive stages representing your escape route:
    1.  **The Tube** (Pitch black metro tunnels)
    2.  **The Street** (Dilapidated alleyways under moonlight)
    3.  **The Outdoors** (A starry night forest)
    4.  **The Above** (Scaling skyscrapers to meet the morning sun)
-   **The Harpies (Shadow Beasts):** 
    -   Creepy flying mists of shadow sense your fear. When you step into the dark, they pursue and relentlessly attack you.
    -   If you enter the light, they become frightened and flee!
-   **Dynamic Morning Progression:** The sky background color transitions dynamically from midnight blue to an orange gold sunbreak as you climb on elevators towards the dawn.

## Features

-   **Polished Material 3 UI:** Seamlessly integrates original assets and high-contrast styling.
-   **Local Preferences:** Native SharedPreferences save toggle settings for Graphics Quality (Fastest, Average, Beautiful) and Gamma Correction (Darkest, Average, Brightest).
-   **Local Achievements System:** Unlocks achievements and timestamps them securely as you conquer levels and elevators.
-   **Touch D-Pad Controls:** Optimized large touch-target buttons (>= 48.dp) for fluid and highly accessible mobile play.
-   **Built-in Stage Skip Panel:** Easily skip between stages for immediate testing and exploration.

## Tech Stack & Architecture

-   **Language:** Kotlin
-   **UI Framework:** Jetpack Compose (Material Design 3)
-   **Architecture:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow
-   **Build System:** Gradle (Kotlin DSL), Android SDK 35, AGP 9.1.1 with native Kotlin compilation support
-   **State Management:** Kotlin Coroutines & Flow (StateFlow)

## License

Code and assets are licensed under GNU GPL >= 2.0 (Michalis Kamburelis remake).
