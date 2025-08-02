# Car Crash Detection MQTT Application

A comprehensive Android application that implements MQTT protocol for car crash detection and emergency response. The app operates in two modes (Publisher/Subscriber) with Apple-quality UI design and professional animations.

## 🚀 Features

### Core Functionality
- **Dual Mode Operation**: Publisher mode for crash victims, Subscriber mode for emergency responders
- **MQTT Communication**: Real-time messaging using Eclipse Paho MQTT client
- **Emergency Alert System**: One-tap emergency alert with 5-minute timer
- **Medical Profile Management**: Comprehensive medical information storage
- **Live Incident Map**: Google Maps integration for incident visualization
- **Data Simulation**: Realistic crash incident data generation for testing

### Apple-Quality Design
- **Modern UI**: Clean, professional interface with smooth animations
- **Gradient Backgrounds**: Beautiful visual design with custom gradients
- **Micro-interactions**: Ripple effects, scale animations, and smooth transitions
- **Responsive Layout**: Optimized for various screen sizes and orientations

### Technical Features
- **Room Database**: Local SQLite storage with automatic cleanup
- **SharedPreferences**: App settings and configuration management
- **ViewBinding**: Type-safe view access
- **Coroutines**: Asynchronous programming with Kotlin
- **Material Design 3**: Latest Material Design components

## 📱 Screenshots

### Main Activity (Publisher Mode)
- Large emergency button with pulsing animation
- Mode indicator and connection status
- Medical profile access
- Settings integration

### Emergency Active Screen
- Full-screen red overlay
- 5-minute countdown timer
- "I'm OK" button for cancellation
- Status indicators for location and messaging

### Settings Activity
- Mode switching with app restart
- MQTT broker configuration
- Connection testing
- App preferences management

## 🛠️ Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0+)
- Google Maps API key
- MQTT broker (Mosquitto recommended)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/car-crash-detection-mqtt.git
   cd car-crash-detection-mqtt
   ```

2. **Configure Google Maps API**
   - Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com/)
   - Replace `YOUR_MAPS_API_KEY` in `app/src/main/AndroidManifest.xml`

3. **Set up MQTT Broker**
   - Install Mosquitto broker on your local network
   - Default configuration: `192.168.1.100:1883`
   - Update broker settings in app settings if needed

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

### MQTT Broker Setup (Mosquitto)

1. **Install Mosquitto**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install mosquitto mosquitto-clients
   
   # macOS
   brew install mosquitto
   
   # Windows
   # Download from https://mosquitto.org/download/
   ```

2. **Configure Mosquitto**
   ```bash
   # Edit mosquitto.conf
   sudo nano /etc/mosquitto/mosquitto.conf
   
   # Add these lines:
   listener 1883
   allow_anonymous true
   ```

3. **Start Mosquitto**
   ```bash
   sudo systemctl start mosquitto
   sudo systemctl enable mosquitto
   ```

## 🏗️ Architecture

### Data Models
- `CrashIncident`: Main incident data structure
- `VictimInfo`: Medical and personal information
- `Location`: GPS coordinates
- `EmergencyContact`: Contact information

### Database Schema
```sql
CREATE TABLE crash_incidents (
    incidentId TEXT PRIMARY KEY,
    timestamp TEXT,
    location TEXT,
    victim TEXT,
    vehicleType TEXT,
    status TEXT,
    responderId TEXT,
    acknowledgedAt TEXT,
    resolvedAt TEXT,
    createdAt INTEGER
);
```

### MQTT Topics
- `crash/alerts/region`: Emergency alert messages
- `crash/status/{incident_id}`: Status updates
- `crash/responses/{incident_id}`: Response confirmations

## 🎯 Usage

### Publisher Mode (Default)
1. **Send Emergency Alert**: Tap the large red emergency button
2. **Emergency Active Screen**: 5-minute timer with "I'm OK" option
3. **Medical Profile**: Set up personal and medical information
4. **Settings**: Configure MQTT broker and app preferences

### Subscriber Mode
1. **View Incidents**: Tap "VIEW INCIDENTS" button
2. **Live Map**: See incident locations on Google Maps
3. **Incident Details**: Tap markers for detailed information
4. **Response Actions**: Acknowledge and resolve incidents

### Settings Configuration
1. **Mode Selection**: Switch between Publisher/Subscriber modes
2. **MQTT Configuration**: Set broker URL and port
3. **Connection Testing**: Verify MQTT connectivity
4. **App Preferences**: Notifications and data retention settings

## 🔧 Configuration

### MQTT Broker Settings
- **Default URL**: `192.168.1.100`
- **Default Port**: `1883`
- **Topics**: Auto-subscribe to crash alerts and status updates

### Data Retention
- **Default Period**: 7 days
- **Automatic Cleanup**: Daily background cleanup
- **Manual Clear**: Available in settings

### Permissions Required
- `INTERNET`: MQTT communication
- `ACCESS_FINE_LOCATION`: GPS coordinates
- `POST_NOTIFICATIONS`: Alert notifications
- `WAKE_LOCK`: Keep connection alive

## 🧪 Testing

### Data Simulation
The app includes a comprehensive data simulation system:

```kotlin
val simulator = DataSimulator()
val incident = simulator.generateRandomIncident()
```

### Test Scenarios
1. **Single Emergency**: Send one emergency alert
2. **Multiple Incidents**: Generate multiple incidents for testing
3. **Location Testing**: Test with specific coordinates
4. **Network Testing**: Test MQTT connectivity

### Sample Data
- **Age Distribution**: Normal distribution (mean: 35, std: 15)
- **Blood Groups**: Realistic population distribution
- **Vehicle Types**: Cars (70%), Motorcycles (15%), Trucks (10%), Buses (5%)
- **Location Range**: Bangalore city bounds

## 📊 Performance

### Optimization Features
- **ViewBinding**: Type-safe view access
- **Coroutines**: Efficient asynchronous operations
- **Room Database**: Optimized SQLite operations
- **StateFlow**: Reactive UI updates

### Memory Management
- **Automatic Cleanup**: 7-day data retention
- **Background Processing**: Efficient MQTT handling
- **Image Optimization**: Vector drawables for scalability

## 🔒 Security

### Data Protection
- **Local Storage**: All sensitive data stored locally
- **No Cloud Sync**: Privacy-focused design
- **Permission Management**: Minimal required permissions

### Network Security
- **MQTT over TCP**: Standard MQTT protocol
- **Local Network**: Designed for local deployment
- **No External APIs**: Self-contained system

## 🐛 Troubleshooting

### Common Issues

1. **MQTT Connection Failed**
   - Verify broker URL and port
   - Check network connectivity
   - Ensure Mosquitto is running

2. **Location Permission Denied**
   - Grant location permission in app settings
   - Restart app after permission grant

3. **Maps Not Loading**
   - Verify Google Maps API key
   - Check internet connectivity
   - Ensure API key has Maps SDK enabled

4. **App Crashes**
   - Clear app data and cache
   - Reinstall app
   - Check Android version compatibility

### Debug Information
- **Logs**: Check Android Studio Logcat
- **MQTT Status**: Visible in main activity
- **Database**: Use Android Studio Database Inspector

## 📈 Future Enhancements

### Planned Features
- **Real-time Location Tracking**: Continuous GPS monitoring
- **Voice Commands**: Hands-free emergency activation
- **Offline Mode**: Local incident storage
- **Multi-language Support**: Internationalization
- **Push Notifications**: Real-time alerts
- **Emergency Services Integration**: Direct 911 integration

### Technical Improvements
- **WebSocket Support**: Alternative to MQTT
- **End-to-End Encryption**: Enhanced security
- **Cloud Backup**: Optional cloud storage
- **Analytics Dashboard**: Incident statistics
- **Machine Learning**: Crash prediction algorithms

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support and questions:
- **Issues**: Create an issue on GitHub
- **Documentation**: Check the wiki
- **Email**: support@carcrashdetection.com

## 🙏 Acknowledgments

- **Eclipse Paho**: MQTT client library
- **Google Maps**: Mapping services
- **Material Design**: UI components
- **Android Jetpack**: Modern Android development

---

**Note**: This is an academic prototype demonstrating MQTT communication principles. For production use, additional security, testing, and compliance measures would be required. 