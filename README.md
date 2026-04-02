# Odorik Buddy

> **Disclaimer:** Odorik Buddy is an **unofficial** companion app for [Odorik.cz](https://www.odorik.cz/) services. It is not affiliated with, endorsed by, or sponsored by Odorik.cz.

Odorik Buddy is a modern Android application for interacting with the ODORIK.CZ API. It allows you to manage your account, make calls, send SMS, and more, all from a clean and intuitive interface.

## Features

*   **Dashboard**: View account balance, today's and selected period spending, interactive spending charts with date range picker, and pull-to-refresh functionality.
*   **Call Management**: Initiate callback and one-shot calls through the ODORIK.CZ API, select lines, configure caller ID prefix, and pick contacts from device.
*   **SMS Management**: Send SMS with multipart support, character counting, sender selection, delay scheduling (minutes or specific time), and contact picker integration.
*   **History**: View detailed call and SMS history with advanced filtering (by line, number, event type, direction), contact name resolution, and pull-to-refresh.
*   **Routing Management**: Configure call forwarding rules for shared and own numbers, with options for source/ringing numbers, caller ID prefix, and contact picker.
*   **Settings**: Customize dark/light mode, language selection (English/Czech), history period, personal phone number, auto-update checks, direct call launching, and account management.
*   **Secure Login**: Securely authenticate with ODORIK.CZ API using encrypted storage.
*   **Modern UI**: Responsive design with Material 3 components, draggable tabs, and adaptive layouts.
*   **Dark/Light Mode**: Automatic theme adaptation with manual override option.

## Tech Stack

*   **Kotlin**: The primary programming language.
*   **Jetpack Compose**: For building declarative UI.
*   **Material 3**: For modern UI components and theming.
*   **Navigation Compose**: For in-app navigation and routing.
*   **MVVM Architecture**: For clean separation of concerns and maintainable codebase.
*   **Retrofit**: For REST API communication with ODORIK.CZ, with Gson and Scalars converters.
*   **OkHttp**: HTTP client with logging interceptor for network debugging.
*   **Coroutines & Flow**: For asynchronous operations and reactive data streams.
*   **Hilt**: For dependency injection, including WorkManager and Navigation Compose integration.
*   **Room**: For local SQLite database storage (tiles, history caching).
*   **Jetpack Security**: For encrypted storage of sensitive data like API keys.
*   **WorkManager**: For scheduling background tasks like update checks.
*   **Glance**: For building modern home screen widgets (Balance & Tile widgets).
*   **MPAndroidChart**: For interactive data visualization and charts.
*   **Libphonenumber**: For phone number parsing, formatting, and validation.
*   **Core Library Desugaring**: For modern Java API support on older Android versions.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for more details.


<a href="https://www.buymeacoffee.com/sentello"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" width="200" alt="Buy me a coffee"></a>
