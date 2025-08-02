@echo off
REM Car Crash Detection MQTT App - Build Script for Windows
REM This script helps build and run the Android application

echo 🚗 Car Crash Detection MQTT App - Build Script
echo ==============================================

REM Check if we're in the right directory
if not exist "app\build.gradle.kts" (
    echo ❌ Error: Please run this script from the project root directory
    pause
    exit /b 1
)

REM Check prerequisites
echo 📋 Checking prerequisites...

java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed. Please install Java 11 or later.
    pause
    exit /b 1
)

adb version >nul 2>&1
if errorlevel 1 (
    echo ⚠️  ADB not found. Make sure Android SDK is installed and in PATH.
    echo    You can still build the APK without ADB.
)

REM Clean previous builds
echo 🧹 Cleaning previous builds...
call gradlew.bat clean

REM Build the app
echo 🔨 Building the application...
call gradlew.bat assembleDebug

if errorlevel 1 (
    echo ❌ Build failed!
    pause
    exit /b 1
)

echo ✅ Build successful!
echo 📱 APK location: app\build\outputs\apk\debug\app-debug.apk

REM Check if device is connected
adb version >nul 2>&1
if not errorlevel 1 (
    echo 📱 Checking for connected devices...
    adb devices | findstr "device$" >nul
    if not errorlevel 1 (
        echo 📱 Installing on connected device...
        adb install -r app\build\outputs\apk\debug\app-debug.apk
        if not errorlevel 1 (
            echo ✅ App installed successfully!
            echo 🚀 Launching app...
            adb shell am start -n com.example.mqtt_app/.MainActivity
        ) else (
            echo ❌ Failed to install app
        )
    ) else (
        echo ⚠️  No Android device connected. APK is ready for manual installation.
    )
) else (
    echo ⚠️  ADB not available. Please install the APK manually.
)

echo.
echo 🎉 Build process completed!
echo.
echo 📋 Next steps:
echo 1. Install the APK on your Android device
echo 2. Configure MQTT broker settings in the app
echo 3. Set up Google Maps API key if using maps features
echo 4. Test the emergency alert functionality
echo.
echo 📚 For more information, see README.md
pause 