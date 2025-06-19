class UsageTracker {
  constructor() {
    this.sessions = new Map();
    this.dailyUsage = new Map();
    this.restrictions = new Map();
    this.init();
  }

  async init() {
    await this.loadData();
    this.setupEventListeners();
    this.setupAlarms();
  }

  async loadData() {
    const data = await chrome.storage.local.get(['sessions', 'dailyUsage', 'restrictions']);
    this.sessions = new Map(data.sessions || []);
    this.dailyUsage = new Map(data.dailyUsage || []);
    this.restrictions = new Map(data.restrictions || []);
  }

  async saveData() {
    await chrome.storage.local.set({
      sessions: Array.from(this.sessions.entries()),
      dailyUsage: Array.from(this.dailyUsage.entries()),
      restrictions: Array.from(this.restrictions.entries())
    });
  }

  setupEventListeners() {
    chrome.tabs.onActivated.addListener((activeInfo) => {
      this.handleTabChange(activeInfo.tabId);
    });

    chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
      if (changeInfo.status === 'complete' && tab.url) {
        this.handleTabUpdate(tabId, tab.url);
      }
    });

    chrome.tabs.onRemoved.addListener((tabId) => {
      this.endSession(tabId);
    });

    chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
      this.handleMessage(message, sender, sendResponse);
    });
  }

  setupAlarms() {
    chrome.alarms.create('saveData', { periodInMinutes: 1 });
    chrome.alarms.create('resetDaily', { when: this.getNextMidnight() });
    
    chrome.alarms.onAlarm.addListener((alarm) => {
      if (alarm.name === 'saveData') {
        this.saveData();
      } else if (alarm.name === 'resetDaily') {
        this.resetDailyUsage();
        chrome.alarms.create('resetDaily', { when: this.getNextMidnight() });
      }
    });
  }

  getNextMidnight() {
    const now = new Date();
    const midnight = new Date(now);
    midnight.setHours(24, 0, 0, 0);
    return midnight.getTime();
  }

  handleTabChange(tabId) {
    chrome.tabs.get(tabId, (tab) => {
      if (tab && tab.url) {
        this.startSession(tabId, tab.url);
      }
    });
  }

  handleTabUpdate(tabId, url) {
    this.startSession(tabId, url);
  }

  startSession(tabId, url) {
    try {
      const domain = new URL(url).hostname;
      const now = Date.now();
      
      this.endSession(tabId);
      
      this.sessions.set(tabId, {
        domain,
        startTime: now,
        url
      });

      this.checkRestrictions(domain, tabId);
    } catch (error) {
      console.error('Error starting session:', error);
    }
  }

  endSession(tabId) {
    const session = this.sessions.get(tabId);
    if (session) {
      const duration = Date.now() - session.startTime;
      this.updateUsage(session.domain, duration);
      this.sessions.delete(tabId);
    }
  }

  updateUsage(domain, duration) {
    const today = new Date().toDateString();
    const key = `${today}-${domain}`;
    
    const currentUsage = this.dailyUsage.get(key) || 0;
    this.dailyUsage.set(key, currentUsage + duration);
  }

  checkRestrictions(domain, tabId) {
    const restriction = this.restrictions.get(domain);
    if (restriction) {
      const today = new Date().toDateString();
      const usageKey = `${today}-${domain}`;
      const todayUsage = this.dailyUsage.get(usageKey) || 0;
      
      if (todayUsage >= restriction.dailyLimit) {
        chrome.tabs.sendMessage(tabId, {
          type: 'BLOCK_SITE',
          domain: domain,
          reason: 'Daily limit exceeded'
        });
      }
    }
  }

  resetDailyUsage() {
    const today = new Date().toDateString();
    for (const [key] of this.dailyUsage) {
      if (!key.startsWith(today)) {
        this.dailyUsage.delete(key);
      }
    }
  }

  handleMessage(message, sender, sendResponse) {
    switch (message.type) {
      case 'GET_USAGE':
        sendResponse(this.getUsageData());
        break;
      case 'SET_RESTRICTION':
        this.setRestriction(message.domain, message.restriction);
        sendResponse({ success: true });
        break;
      case 'GET_RESTRICTIONS':
        sendResponse(Array.from(this.restrictions.entries()));
        break;
    }
  }

  getUsageData() {
    const today = new Date().toDateString();
    const todayUsage = new Map();
    
    for (const [key, value] of this.dailyUsage) {
      if (key.startsWith(today)) {
        const domain = key.substring(today.length + 1);
        todayUsage.set(domain, value);
      }
    }
    
    return Array.from(todayUsage.entries());
  }

  setRestriction(domain, restriction) {
    this.restrictions.set(domain, restriction);
    this.saveData();
  }
}

// Import sync functionality
importScripts('sync.js');

const tracker = new UsageTracker();
const cloudSync = new CloudSync();