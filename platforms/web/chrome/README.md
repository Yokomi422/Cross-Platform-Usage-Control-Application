# Chrome Extension - Usage Control

Chrome extension for cross-platform usage control and monitoring.

## Features

- **Real-time Usage Tracking**: Monitor time spent on websites
- **Site Blocking**: Block access to specific domains after time limits
- **Daily Limits**: Set custom time restrictions per domain
- **Override System**: Temporary access with 5-minute override
- **Cloud Sync**: Synchronize data with Firebase and CloudKit
- **Progressive UI**: Clean, native-feeling interface

## Installation

1. Open Chrome and navigate to `chrome://extensions/`
2. Enable "Developer mode" in the top right
3. Click "Load unpacked" and select this directory
4. The extension icon will appear in the toolbar

## Project Structure

```
chrome/
├── manifest.json           # Extension configuration
├── scripts/
│   ├── background.js      # Background service worker
│   └── sync.js           # Cloud synchronization
├── popup/
│   ├── popup.html        # Extension popup UI
│   └── popup.js          # Popup functionality
├── content/
│   └── content.js        # Content script for blocking
├── icons/                # Extension icons (to be added)
└── README.md            # This file
```

## Usage

### Setting Restrictions

1. Click the extension icon in the toolbar
2. Enter a domain (e.g., "youtube.com")
3. Set daily time limit in minutes
4. Click "Add Restriction"

### Viewing Usage

- Current day's usage is displayed in the popup
- Time is shown in minutes or hours + minutes format
- Sites are sorted by usage time (highest first)

### Override System

When a site is blocked:
- A full-screen overlay appears with restriction details
- Click "Override (5 minutes)" for temporary access
- Click "Close Tab" to navigate away

## Technical Details

### Background Script
- Tracks active tabs and time spent
- Monitors URL changes and tab switches
- Enforces daily time limits
- Handles data persistence

### Content Script
- Injects blocking overlay when limits are exceeded
- Provides override functionality
- Prevents navigation during blocks

### Cloud Sync
- Integrates with existing Firebase and CloudKit services
- Syncs restrictions and usage data across devices
- Handles conflict resolution and data merging

## Integration with Main Project

This Chrome extension integrates with the broader cross-platform usage control system:

- **Shared Data Models**: Compatible with iOS, Android, and other platforms
- **Cloud Services**: Uses same Firebase/CloudKit backend
- **Progressive Levels**: Supports the main app's level-based restriction system
- **User Accounts**: Syncs with existing user profiles

## Development

### Testing
1. Make changes to the code
2. Go to `chrome://extensions/`
3. Click the refresh icon on the extension card
4. Test functionality

### Debugging
- Use `chrome://extensions/` to view errors
- Open Developer Tools on the popup for popup debugging
- Check the background page for service worker logs

## Future Enhancements

- [ ] Add extension icons
- [ ] Implement CloudKit Web Services integration
- [ ] Add statistics and charts
- [ ] Implement progressive restriction levels
- [ ] Add whitelisting functionality
- [ ] Export/import settings
- [ ] Productivity metrics and insights