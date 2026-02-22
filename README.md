# SmartNotes – AI-Generated Notes & Reminder App

SmartNotes is a premium, modern Android application built with Kotlin and Material Design 3. It leverages the power of Google's Gemini AI to help users generate note content automatically based on their titles.

## Features

- **AI Content Generation**: Powered by Google's Gemini 1.5 Flash model.
- **Modern UI**: Clean, elegant, and premium Material Design 3 interface with soft gradients and rounded corners.
- **Room Database**: Fast and reliable local storage for your notes.
- **Smart Reminders**: Set time-based reminders for your notes with system notifications.
- **Dynamic Grid**: Staggered list view for a modern, high-end productivity app feel.
- **ViewBinding**: Type-safe layout interactions.

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM-lite (with Room & Flow)
- **UI**: XML Layouts with Material Design 3
- **Database**: Room
- **Networking**: OkHttp & Gson
- **AI**: Gemini Pro (1.5 Flash API)
- **Notifications**: AlarmManager & NotificationManager

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/huzaiif/Smart-Notes.git
   ```

2. Add your Gemini API Key:
   Open `local.properties` and add your key:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```

3. Build and run the app in Android Studio.

## License

MIT License - feel free to use and modify for your own projects.
