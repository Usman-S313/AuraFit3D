# 🏋️‍♂️ AuraFit 3D — Body Composition & Biometric Analyzer

**AuraFit 3D** is a modern, privacy-first Android application for body composition calculation, biometric tracking, calorie/macro planning, and 2D body silhouette visualization. Built using 100% Kotlin and Jetpack Compose.

---

## 🌟 Key Features

- 📏 **Biometric Input & Validation**:
  - Supports both **Metric** (cm, kg) and **Imperial** (in, lbs) units.
  - Biological sex, age, height, weight, neck, waist, and hip measurements.
  - Real-time input validation and boundary checking.

- 📊 **Biometric Analysis**:
  - Calculates **BMI**, **Body Fat %**, **FFMI** (Fat-Free Mass Index), **WHtR** (Waist-to-Height Ratio), **BMR**, and **TDEE**.
  - Categorizes body fat into healthy reference bands.

- 👤 **2D Body Silhouette Visualization**:
  - Interactive Canvas silhouette rendering.
  - Multiple view modes: **Skin**, **Muscle**, and **Fat Distribution**.
  - Zoom, pan, and interactive controls.

- 🥗 **Calorie & Macro Planner**:
  - Goal-oriented planning (**Fat Loss**, **Maintenance**, **Muscle Gain**).
  - Calculates daily target calories, protein, carbs, and fats.
  - Estimated timeline & weeks projection to reach target body fat.

- 📈 **History & Profile Persistence**:
  - Local tracking of historical measurement logs.
  - Self-check fixture system to verify biometric math precision at startup.

- 🔒 **Privacy & Platform Features**:
  - **Zero Internet Permission** declared (`NO_INTERNET`). All calculations happen on-device.
  - Native Material 3 Dark Theme edge-to-edge layout.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Material 3 BOM `2024.12.01`)
- **Architecture**: MVVM (ViewModel + `StateFlow`)
- **Navigation**: Navigation Compose (`2.8.5`)
- **Persistence**: DataStore Preferences (`1.1.1`)
- **Build System**: Gradle 8.10.2 + Kotlin DSL
- **Compatibility**: Minimum SDK 26 (Android 8.0), Target SDK 35

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1+) or newer
- JDK 17+
- Android SDK 35

### Building from Source

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/<YOUR_USERNAME>/AuraFit3D.xgit
   cd AuraFit3D
   ```

2. **Run Unit Tests**:
   Verify biometric engine calculations:
   ```bash
   ./gradlew test
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew app:assembleDebug
   ```

4. **Run on Emulator or Device**:
   Open in Android Studio and click **Run 'app'** or execute:
   ```bash
   ./gradlew app:installDebug
   ```

---

## 🧪 Testing

The biometric calculation engine is covered by pure Kotlin JVM tests verifying SRS reference values:
```bash
./gradlew app:testDebugUnitTest
```
* **13 unit tests** covering body fat formulas, macro targets, and goal projections.

---

## 📜 License

This project is licensed under the Apache 2.0 License.
