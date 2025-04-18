# WaZi Minecraft Launcher

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Repository-blue?logo=github)](https://github.com/WaZixwx/WaZi-Minecraft-Launcher)
[![Project Page](https://img.shields.io/badge/Project%20Page-mc.wazixwx.com-brightgreen)](https://mc.wazixwx.com)
[![Build Status](https://img.shields.io/badge/Build-Pending-lightgrey.svg)](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/actions)
[![Latest Release](https://img.shields.io/github/v/release/WaZixwx/WaZi-Minecraft-Launcher?display_name=tag&logo=github&color=orange)](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/releases/latest)

WaZi Minecraft Launcher is an open-source Minecraft launcher built with Kotlin and Compose Multiplatform. It aims to provide a modern, cross-platform solution for launching Minecraft.

## Table of Contents

*   [Features](#features)
*   [Technology Stack](#technology-stack)
*   [System Requirements](#system-requirements)
*   [Getting Started](#getting-started)
*   [Building the Project](#building-the-project)
*   [Contributing](#contributing)
*   [Code of Conduct](#code-of-conduct)
*   [License](#license)
*   [Contact](#contact)

## Features

*   **Version Management**:
    *   Fetches and displays available game versions (Release, Snapshot, etc.) from Mojang's official API.
    *   Automatically scans for locally installed game versions.
    *   Provides version filtering and sorting capabilities.
*   **Game Download**:
    *   Downloads the core files (Client Jar) for the specified Minecraft version.
    *   Downloads required library files (Libraries).
    *   Downloads the asset index file (Asset Index).
    *   Downloads game asset files (Asset Objects) based on the asset index.
    *   Supports download progress display (overall and file-level).
    *   Supports basic download resumption.
    *   Performs SHA1 hash verification on downloaded files to ensure integrity.
    *   Optimizes download efficiency and stability through concurrency control.
*   **Game Launch**:
    *   Automatically extracts required native library files (Natives).
    *   Constructs the correct game Classpath based on version information.
    *   Assembles JVM and game arguments according to official specifications.
    *   Launches the game process using the system's installed Java environment.
*   **User Interface**:
    *   Modern graphical user interface built with Compose Multiplatform.
    *   Clear display of the version list.
    *   Intuitive download/start buttons and status feedback.
    *   Download progress bar display.

## Technology Stack

*   **Programming Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Compose Multiplatform (Desktop)](https://github.com/JetBrains/compose-multiplatform)
*   **Build Tool**: [Gradle](https://gradle.org/) (Version 8.7)
*   **Networking**: [Ktor Client](https://ktor.io/docs/client-create-new-application.html)
*   **JSON Parsing**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
*   **Asynchronous Operations**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## System Requirements

*   **Operating System**: Windows, macOS, Linux (Tested on Windows 10)
*   **Java Development Kit (JDK)**: Version 21 or higher. Ensure `JAVA_HOME` environment variable is configured correctly or the `java` command is available in the system PATH.

## Getting Started

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/WaZixwx/WaZi-Minecraft-Launcher.git
    cd WaZi-Minecraft-Launcher
    ```
2.  **Run the Launcher**:
    Execute the following command in the project root directory:
    *   Linux / macOS:
        ```bash
        ./gradlew :app:run
        ```
    *   Windows:
        ```bash
        gradlew.bat :app:run
        ```
    Gradle will automatically download dependencies and start the application.

## Building the Project

You can build and package the project using Gradle.

*   **Build**:
    ```bash
    # Linux / macOS
    ./gradlew build
    # Windows
    gradlew.bat build
    ```
*   **Package as Executable Jar (Example)**:
    (Specific packaging tasks might need configuration in `build.gradle.kts`, e.g., using `shadowJar` or `compose.desktop.packageUberJarForCurrentOS` tasks)
    ```bash
    # Linux / macOS (if UberJar task is configured)
    ./gradlew packageUberJarForCurrentOS 
    # Windows (if UberJar task is configured)
    gradlew.bat packageUberJarForCurrentOS 
    ```
    The packaged file is typically located in the `app/build/compose/jars/` directory.

## Contributing

Contributions are welcome! Whether it's reporting bugs, suggesting features, or submitting code, your input is valuable to the project's development. Please refer to our [Contributing Guidelines](CONTRIBUTING.md) for detailed information.

You can check the current bugs and feature requests on the [GitHub Issues](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/issues) page and participate in discussions.

## Code of Conduct

To foster an open and welcoming community environment, we expect all participants to adhere to the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

*   **GitHub Repository**: [https://github.com/WaZixwx/WaZi-Minecraft-Launcher](https://github.com/WaZixwx/WaZi-Minecraft-Launcher)
*   **Project Page**: [https://mc.wazixwx.com](https://mc.wazixwx.com)
*   **Issue Tracker**: [GitHub Issues](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/issues) 