# iOS Usage Control Application

## Project Structure

### UsageControlApp/
Main iOS application target using SwiftUI + Combine

- **Views/**: SwiftUI views and components
- **ViewModels/**: MVVM view models with Combine
- **Models/**: Core data models
- **Services/**: Business logic and API services
- **Extensions/**: Swift extensions and utilities
- **Resources/**: Assets, localizations, configuration files

### UsageControlDeviceActivity/
Device Activity Monitor Extension (iOS 15+)
- Monitors app usage and applies restrictions using Screen Time API

### UsageControlSystemExtension/
Optional System Extension for enhanced monitoring
- Advanced system-level monitoring capabilities

### UsageControlAppTests/
Unit and integration tests

## Key Technologies

- **SwiftUI**: Modern declarative UI framework
- **Combine**: Reactive programming for data flow
- **Screen Time API**: DeviceActivityFramework, ManagedSettings
- **CloudKit**: Data synchronization across devices
- **Family Controls**: Parental control capabilities (requires Apple approval)

## Required Entitlements

- Family Controls
- CloudKit
- Device Activity Monitor
- Background App Refresh

## Development Setup

1. Ensure Apple Developer Program membership
2. Request Family Controls entitlement from Apple
3. Configure CloudKit container
4. Set up signing certificates and provisioning profiles