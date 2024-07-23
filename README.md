# Multi QR Code Recognition

This project demonstrates the implementation of a multi QR code recognition system using Jetpack Compose, ExoPlayer, and ML Kit. The application captures video frames and processes them to detect QR codes in real-time.

## Features

- Plays video using ExoPlayer.
- Captures frames from the video for QR code detection.
- Detects multiple QR codes using Google's ML Kit.
- Displays detected QR codes on the screen.
- Handles camera permissions.
- Supports both HTTP and HTTPS video links.

## Architecture

- **MVVM (Model-View-ViewModel)**: For structured and maintainable code
- **Jetpack Compose**: For building the UI
- **ExoPlayer**: For video playback
- **ML Kit**: For QR code detection
- **Hilt**: For dependency injection
- **Timber**: For logging

## Setup

### Prerequisites

- Android Studio Arctic Fox or later.
- Minimum SDK level 29.
- Ensure you have an internet connection to download the dependencies.

### Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/shehroz-ameer/MultiQRRecognition.git
    cd MultiQRRecognition
    ```

2. **Open the project in Android Studio.**

3. **Build the project:**

    Android Studio will automatically download the necessary dependencies and build the project.

## Usage

1. **Grant Camera Permission:**

    The app requires camera permission to capture and process video frames for QR code detection. Ensure you grant this permission when prompted.

2. **Run the App:**

    Once the app is running, it will automatically start playing a video and processing frames for QR code detection. Detected QR codes will be displayed on the screen.


## Repository

You can find the source code for this project at [https://github.com/shehroz-ameer/MultiQRRecognition.git](https://github.com/shehroz-ameer/MultiQRRecognition.git).
