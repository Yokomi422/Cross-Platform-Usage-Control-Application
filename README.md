# Cross-Platform Usage Control Application

A comprehensive application usage monitoring and restriction system supporting macOS, iOS/iPadOS, Android, and Linux platforms.

## Project Overview

This application provides granular control over app and website usage across multiple platforms, featuring a progressive level-based restriction system that users can configure and advance through upon completion.

## Platform-Specific Implementation

### Apple Platforms (macOS, iOS/iPadOS)

#### macOS Implementation
- **Core Technologies**: Swift + Objective-C with SwiftUI
- **System Control**: System Extensions with Endpoint Security Framework
- **Monitoring**: Application launch and network access monitoring via DeviceActivityFramework
- **Architecture**: Separation of UI (SwiftUI) and system monitoring (dedicated System Extension)

#### iOS/iPadOS Implementation
- **Frameworks**: Screen Time API, DeviceActivityFramework (iOS 15+)
- **UI**: SwiftUI + Combine
- **Restrictions**: ManagedSettings framework for enforcement
- **Analytics**: DeviceActivityReport for usage visualization
- **Requirements**: Family Controls entitlement (requires Apple approval)

#### Data Synchronization
- **Service**: CloudKit for seamless device synchronization
- **Integration**: Apple ID-based user data management
- **Architecture**: Common data models with platform-specific enforcement mechanisms

### Android Platform

#### Core Approaches
1. **Device Admin API**: Comprehensive control with device administrator privileges
2. **Accessibility Service**: Lightweight monitoring and overlay-based blocking
3. **Usage Stats API**: Official usage statistics with additional blocking mechanisms

#### Technology Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Local Data**: Room Database
- **Background Tasks**: WorkManager
- **Cloud Sync**: Firebase Firestore
- **Security**: Multi-layered defense strategy

### Linux Platform

#### Technical Architecture
1. **Kernel Level**: eBPF (Extended Berkeley Packet Filter) for process monitoring
2. **System Call Level**: seccomp and ptrace for system call interception
3. **User Land**: /proc filesystem monitoring and D-Bus integration

#### Technology Stack
- **Primary Language**: Rust
- **UI Framework**: GTK4 or Qt6
- **Display Integration**: X11 and Wayland support
- **Deployment**: Flatpak, Snap, or AppImage packaging
- **Service Management**: systemd integration with fallback support

### Web Platform (Chrome Extension)

#### Core Features
- **Real-time Usage Tracking**: Monitor time spent on websites
- **Site Blocking**: Block access to specific domains after time limits
- **Daily Limits**: Set custom time restrictions per domain
- **Override System**: Temporary access with 5-minute override
- **Cloud Sync**: Synchronize data with Firebase and CloudKit

#### Technology Stack
- **Manifest**: V3 Service Worker architecture
- **Background Script**: Usage tracking and enforcement
- **Content Script**: Full-screen blocking overlay
- **Storage**: Chrome Extension API with cloud synchronization
- **UI**: Native HTML/CSS/JavaScript popup interface

#### Integration
- **Data Models**: Compatible with iOS, Android, and other platforms
- **Cloud Services**: Uses same Firebase/CloudKit backend
- **Progressive Levels**: Supports the main app's level-based restriction system

## Development Strategy

### Recommended Development Order
1. **Start with macOS**: Better debugging tools and clearer development path
2. **Expand to iOS**: Leverage macOS architecture and learnings
3. **Chrome Extension**: Quick implementation for web-based usage control
4. **Android Implementation**: Build on established patterns
5. **Linux Integration**: Most complex due to distribution diversity

### Security Model by Platform
- **Apple Platforms**: System-enforced restrictions via official APIs
- **Android**: Permission-based multi-layered defense
- **Chrome Extension**: Browser API-based restrictions with overlay blocking
- **Linux**: Cooperative restrictions with deliberate bypass complexity

## Key Features
- **Progressive Levels**: User-configurable restriction levels with advancement system
- **Cross-Platform Sync**: Unified settings and progress across all devices
- **Adaptive UI**: Platform-native user interfaces
- **Robust Monitoring**: Deep system integration for effective usage tracking
- **Security Focus**: Platform-appropriate anti-circumvention measures

## Technical Challenges
- **System-Level Access**: Deep OS integration requirements
- **Platform Diversity**: Handling different security models and APIs
- **User Experience**: Balancing restriction effectiveness with usability
- **Synchronization**: Maintaining consistent state across platforms
- **Circumvention Prevention**: Platform-appropriate security measures

## Getting Started

### Prerequisites
- **macOS**: Xcode with Developer Certificate
- **iOS**: Apple Developer Program membership with Family Controls approval
- **Android**: Android Studio with appropriate SDK versions
- **Chrome Extension**: Chrome browser with Developer mode enabled
- **Linux**: Rust toolchain and distribution-specific development tools

### Development Environment Setup
1. Clone the repository
2. Set up platform-specific development environments
3. Configure signing certificates and entitlements
4. Initialize cloud synchronization services

## Architecture Principles
- **Modular Design**: Platform-specific implementations with shared business logic
- **Progressive Enhancement**: Start simple, add complexity gradually
- **Security by Design**: Platform-appropriate security measures from day one
- **User-Centric**: Focus on healthy usage habits rather than absolute restriction

## Contributing
Please ensure all platform implementations maintain architectural consistency while respecting platform-specific best practices and guidelines.

## License
[License information to be added]

## Support
For platform-specific implementation details and troubleshooting, refer to the respective platform documentation in the `/docs` directory.
