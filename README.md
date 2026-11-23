# Odorik Buddy

> **Disclaimer:** Odorik Buddy is an **unofficial** companion app for [Odorik.cz](https://www.odorik.cz/) services. It is not affiliated with, endorsed by, or sponsored by Odorik.cz.

Odorik Buddy is an Android app for the ODORIK.CZ API: see your balance and spending, call and send SMS, watch and hang up calls in progress, and manage forwarding, speed dials and your Odorik SIM card. The app is in Czech and English.

## Features

*   **Dashboard**: Balance, today's and selected-period spending, and a spending chart (daily bars, weekly bars for periods over a month) with a date range picker. While a call is in progress a card shows who is calling whom and how long, with a **Hang up** button; balance and spending refresh by themselves when the call ends.
*   **Calls**: Callback calls with a live status (ordered → ringing → connected, with an *End call* button), one-shot forwarded calls through a shared number, favourite tiles, line selection, caller ID prefix and contact picker.
*   **SMS**: Multipart messages with a character counter, sender selection and scheduled sending.
*   **History**: Calls and SMS grouped by day, filters (line, number, type, direction) and contact names. Each call is listed once, even when Odorik logs it as several legs; real forwards to other numbers are shown under their call.
*   **Forwarding**: Routing rules for shared and own numbers.
*   **Speed dials**: Add, edit and delete the account's speed dials (they apply to all lines). Speed dials set on the Odorik website for a single line are shown read-only.
*   **SIM card** (only for accounts with an Odorik mobile SIM): data usage this month and per day, mobile data / LTE / missed-call SMS switches, data speed restore (FUP, paid) and suspend / reactivate — every paid or disruptive action is confirmed.
*   **Widgets**: A balance widget that refreshes itself (logo can be hidden, settings can be changed later on Android 12+) and tile widgets for one-tap calls.
*   **Settings**: Czech / English, light / dark / system mode, Odorik or Material You colours, bottom-bar icon style, tile size, export and import of tiles and settings (never the password), and optional new-version notices.

## Installation

*   Requires **Android 7.0** or newer.
*   Download the latest APK from the [Odorik Buddy thread on the Odorik.cz forum](https://forum.odorik.cz/viewtopic.php?p=48083#p48083) and install it (allow installing apps from that source when Android asks).
*   If you turn on new-version notices, the app tells you when an update is available and opens its download page.

## Logging in

*   Use your **user number** and the **API password** from the Odorik website (Account settings → API password). It is a separate password from the one you use to log in to the website.

## Privacy

*   The app talks only to the Odorik API (`www.odorik.cz`). With new-version notices on, it also reads a small version file from this GitHub repository.
*   No analytics, no ads, no tracking.
*   With *Remember me*, the password is stored encrypted on the device (Android Keystore). Settings exports never contain it.
*   Calls in progress are checked only while the app is open — nothing runs in the background except the optional balance widget refresh and the weekly version check.

## Tech Stack

*   **Kotlin** and **Jetpack Compose** with **Material 3**, **Navigation Compose** and an **MVVM** architecture.
*   **Retrofit** and **OkHttp** (Gson and Scalars converters) for the ODORIK.CZ API.
*   **Coroutines & Flow** for asynchronous work.
*   **Hilt** for dependency injection, including WorkManager and Navigation Compose.
*   **Room** for local storage (tiles, history cache).
*   **Jetpack Security** for encrypted credentials.
*   **WorkManager** for the balance widget refresh and update checks.
*   **Glance** for the home-screen widgets.
*   **MPAndroidChart** for the spending chart.
*   **libphonenumber** for phone number parsing and formatting.
*   **Core Library Desugaring** for modern Java APIs on older Android versions.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for more details.
