class CloudSync {
  constructor() {
    this.firebaseConfig = null;
    this.syncInterval = 5 * 60 * 1000; // 5 minutes
    this.lastSyncTime = 0;
    this.init();
  }

  async init() {
    await this.loadConfig();
    this.setupSyncTimer();
  }

  async loadConfig() {
    try {
      const config = await chrome.storage.local.get(['firebaseConfig', 'cloudKitConfig']);
      this.firebaseConfig = config.firebaseConfig;
      this.cloudKitConfig = config.cloudKitConfig;
    } catch (error) {
      console.error('Failed to load sync config:', error);
    }
  }

  setupSyncTimer() {
    setInterval(() => {
      this.syncData();
    }, this.syncInterval);

    chrome.storage.onChanged.addListener((changes, namespace) => {
      if (namespace === 'local' && this.shouldTriggerSync(changes)) {
        this.debouncedSync();
      }
    });
  }

  shouldTriggerSync(changes) {
    const syncTriggers = ['restrictions', 'dailyUsage', 'settings'];
    return Object.keys(changes).some(key => syncTriggers.includes(key));
  }

  debouncedSync = this.debounce(() => {
    this.syncData();
  }, 10000);

  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

  async syncData() {
    try {
      const now = Date.now();
      if (now - this.lastSyncTime < 30000) { // Prevent too frequent syncs
        return;
      }

      const localData = await this.getLocalData();
      
      if (this.firebaseConfig) {
        await this.syncToFirebase(localData);
      }
      
      if (this.cloudKitConfig) {
        await this.syncToCloudKit(localData);
      }

      this.lastSyncTime = now;
      
    } catch (error) {
      console.error('Sync failed:', error);
    }
  }

  async getLocalData() {
    const data = await chrome.storage.local.get([
      'restrictions',
      'dailyUsage',
      'settings',
      'userProfile'
    ]);

    return {
      restrictions: data.restrictions || [],
      dailyUsage: data.dailyUsage || [],
      settings: data.settings || {},
      userProfile: data.userProfile || {},
      lastModified: Date.now(),
      deviceId: await this.getDeviceId()
    };
  }

  async getDeviceId() {
    let deviceId = await chrome.storage.local.get(['deviceId']);
    if (!deviceId.deviceId) {
      deviceId = 'chrome-' + this.generateUUID();
      await chrome.storage.local.set({ deviceId });
    }
    return deviceId.deviceId || deviceId;
  }

  generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  async syncToFirebase(localData) {
    if (!this.firebaseConfig) return;

    try {
      const response = await fetch(`${this.firebaseConfig.databaseURL}/users/${localData.userProfile.id || 'anonymous'}/chrome.json`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...localData,
          platform: 'chrome',
          syncedAt: Date.now()
        })
      });

      if (!response.ok) {
        throw new Error('Firebase sync failed');
      }

      // Fetch updates from other devices
      await this.fetchFirebaseUpdates();
      
    } catch (error) {
      console.error('Firebase sync error:', error);
    }
  }

  async fetchFirebaseUpdates() {
    try {
      const userProfile = await chrome.storage.local.get(['userProfile']);
      const userId = userProfile.userProfile?.id || 'anonymous';
      
      const response = await fetch(`${this.firebaseConfig.databaseURL}/users/${userId}.json`);
      
      if (response.ok) {
        const remoteData = await response.json();
        await this.mergeRemoteData(remoteData);
      }
      
    } catch (error) {
      console.error('Failed to fetch Firebase updates:', error);
    }
  }

  async syncToCloudKit(localData) {
    if (!this.cloudKitConfig) return;

    try {
      // CloudKit Web Services implementation would go here
      // This is a placeholder for the CloudKit integration
      console.log('CloudKit sync not yet implemented');
      
    } catch (error) {
      console.error('CloudKit sync error:', error);
    }
  }

  async mergeRemoteData(remoteData) {
    if (!remoteData) return;

    const localData = await this.getLocalData();
    const merged = this.mergeDataSets(localData, remoteData);
    
    await chrome.storage.local.set({
      restrictions: merged.restrictions,
      dailyUsage: merged.dailyUsage,
      settings: merged.settings
    });
  }

  mergeDataSets(local, remote) {
    const merged = {
      restrictions: new Map(),
      dailyUsage: new Map(),
      settings: { ...local.settings }
    };

    // Merge restrictions - remote wins on conflicts
    if (local.restrictions) {
      local.restrictions.forEach(([key, value]) => {
        merged.restrictions.set(key, value);
      });
    }

    if (remote.restrictions) {
      remote.restrictions.forEach(([key, value]) => {
        if (value.lastModified > (merged.restrictions.get(key)?.lastModified || 0)) {
          merged.restrictions.set(key, value);
        }
      });
    }

    // Merge usage data - sum up values
    if (local.dailyUsage) {
      local.dailyUsage.forEach(([key, value]) => {
        merged.dailyUsage.set(key, value);
      });
    }

    if (remote.dailyUsage) {
      remote.dailyUsage.forEach(([key, value]) => {
        const existing = merged.dailyUsage.get(key) || 0;
        merged.dailyUsage.set(key, Math.max(existing, value));
      });
    }

    // Merge settings - remote wins with newer timestamp
    if (remote.settings && remote.settings.lastModified > (local.settings.lastModified || 0)) {
      merged.settings = { ...merged.settings, ...remote.settings };
    }

    return {
      restrictions: Array.from(merged.restrictions.entries()),
      dailyUsage: Array.from(merged.dailyUsage.entries()),
      settings: merged.settings
    };
  }

  async setupCloudSync(provider, config) {
    if (provider === 'firebase') {
      this.firebaseConfig = config;
      await chrome.storage.local.set({ firebaseConfig: config });
    } else if (provider === 'cloudkit') {
      this.cloudKitConfig = config;
      await chrome.storage.local.set({ cloudKitConfig: config });
    }

    await this.syncData();
  }

  async getLastSyncTime() {
    const data = await chrome.storage.local.get(['lastSyncTime']);
    return data.lastSyncTime || 0;
  }

  async setLastSyncTime(timestamp) {
    await chrome.storage.local.set({ lastSyncTime: timestamp });
  }
}

// Export for use in background script
if (typeof module !== 'undefined' && module.exports) {
  module.exports = CloudSync;
} else if (typeof window !== 'undefined') {
  window.CloudSync = CloudSync;
}