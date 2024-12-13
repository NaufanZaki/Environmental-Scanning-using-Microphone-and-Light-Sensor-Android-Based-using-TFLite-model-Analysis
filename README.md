# Baby Room Environment Monitor 

An Android application designed to monitor and analyze environmental conditions in a baby's room, focusing on sound levels and lighting conditions to ensure optimal comfort for infants.

## Features

### 1. Real-time Environment Monitoring
- Sound level measurement in decibels (dB)
- Light intensity measurement in lux
- Room photo capture capability
- Instant feedback and recommendations

### 2. Analysis & History
- Historical data tracking of all measurements
- Graphical visualization using line charts
- Detailed analysis of environmental trends
- Custom markers for precise data reading
- Photo documentation of room conditions

### 3. Smart Recommendations
- AI-powered comfort level predictions using TensorFlow Lite
- Context-aware suggestions for improvement
- Four-level comfort classification:
  - Excellent
  - Good
  - Poor
  - Very Poor

### 4. User Interface
- Modern Material Design implementation
- Intuitive navigation system
- Animated transitions and feedback
- Gradient-based visual themes
- Responsive layouts
- Camera integration for room documentation

## Technical Details

### Architecture Components
1. **Activities**
   - `MainActivity`: Dashboard and main interface
   - `ScanActivity`: Environment scanning and photo capture interface
   - `HistoryActivity`: Historical measurements view
   - `AnalysisActivity`: Data analysis and charts
   - `ScanDetailActivity`: Detailed view of individual measurements

2. **Database**
   - Room Database implementation
   - `ScanEntity` for measurement data with photo path storage
   - `ScanDao` for database operations

3. **Helpers**
   - `AudioRecorderHelper`: Sound level measurement
   - `TFLiteModelHelper`: ML model integration
   - `CustomMarkerView`: Chart markers
   - `CameraHelper`: Camera operations and image storage

### Key Technologies
- Android Room for data persistence
- TensorFlow Lite for ML predictions
- MPAndroidChart for data visualization
- Material Design Components
- Hardware sensor integration (Audio, Light, Camera)
- File system integration for photo storage

## Requirements

### Hardware Requirements
- Android device with:
  - Microphone
  - Light sensor
  - Camera
  - Storage space for photos

### Software Requirements
- Android SDK 21 or higher
- Gradle 7.0+
- TensorFlow Lite runtime
- MPAndroidChart library

### Permissions
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Add the TensorFlow Lite model file (`comfort_model.tflite`) to the assets folder
5. Build and run the application

## Usage

1. **Main Dashboard**
   - View current environmental conditions
   - Access quick actions
   - See child information

2. **Scanning**
   - Press the scan button
   - Wait for measurements
   - Option to take a room photo
   - Review recommendations

3. **History**
   - Browse past measurements
   - View detailed reports
   - Access room photos
   - Track environmental changes

4. **Analysis**
   - View trend charts
   - Analyze patterns
   - Export data (if implemented)

## Acknowledgments

- MPAndroidChart library for visualization
- TensorFlow Lite for ML capabilities
- Material Design Components for UI
- Android Room for data persistence
