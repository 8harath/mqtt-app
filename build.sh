#!/bin/bash

# Car Crash Detection MQTT App - Build Script
# This script helps build and run the Android application

echo "🚗 Car Crash Detection MQTT App - Build Script"
echo "=============================================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists java; then
    echo "❌ Java is not installed. Please install Java 11 or later."
    exit 1
fi

if ! command_exists adb; then
    echo "⚠️  ADB not found. Make sure Android SDK is installed and in PATH."
    echo "   You can still build the APK without ADB."
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build the app
echo "🔨 Building the application..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
    
    # Check if device is connected
    if command_exists adb; then
        echo "📱 Checking for connected devices..."
        adb devices | grep -q "device$"
        if [ $? -eq 0 ]; then
            echo "📱 Installing on connected device..."
            adb install -r app/build/outputs/apk/debug/app-debug.apk
            if [ $? -eq 0 ]; then
                echo "✅ App installed successfully!"
                echo "🚀 Launching app..."
                adb shell am start -n com.example.mqtt_app/.MainActivity
            else
                echo "❌ Failed to install app"
            fi
        else
            echo "⚠️  No Android device connected. APK is ready for manual installation."
        fi
    else
        echo "⚠️  ADB not available. Please install the APK manually."
    fi
else
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "🎉 Build process completed!"
echo ""
echo "📋 Next steps:"
echo "1. Install the APK on your Android device"
echo "2. Configure MQTT broker settings in the app"
echo "3. Set up Google Maps API key if using maps features"
echo "4. Test the emergency alert functionality"
echo ""
echo "📚 For more information, see README.md" 