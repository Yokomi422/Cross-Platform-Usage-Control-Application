class ContentBlocker {
  constructor() {
    this.isBlocked = false;
    this.blockOverlay = null;
    this.init();
  }

  init() {
    this.setupMessageListener();
    this.checkCurrentSite();
  }

  setupMessageListener() {
    chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
      if (message.type === 'BLOCK_SITE') {
        this.blockSite(message.domain, message.reason);
      } else if (message.type === 'UNBLOCK_SITE') {
        this.unblockSite();
      }
    });
  }

  checkCurrentSite() {
    const domain = window.location.hostname;
    chrome.runtime.sendMessage({
      type: 'CHECK_RESTRICTION',
      domain: domain
    });
  }

  blockSite(domain, reason) {
    if (this.isBlocked) return;
    
    this.isBlocked = true;
    this.createBlockOverlay(domain, reason);
    this.disablePageInteraction();
  }

  createBlockOverlay(domain, reason) {
    this.blockOverlay = document.createElement('div');
    this.blockOverlay.id = 'usage-control-overlay';
    
    this.blockOverlay.innerHTML = `
      <div class="usage-control-modal">
        <div class="usage-control-icon">
          <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
            <circle cx="32" cy="32" r="28" stroke="#FF3B30" stroke-width="4" fill="none"/>
            <path d="M20 20L44 44M44 20L20 44" stroke="#FF3B30" stroke-width="4" stroke-linecap="round"/>
          </svg>
        </div>
        
        <h2 class="usage-control-title">Access Restricted</h2>
        
        <div class="usage-control-message">
          <p><strong>${domain}</strong> is currently blocked.</p>
          <p class="usage-control-reason">${reason}</p>
        </div>
        
        <div class="usage-control-actions">
          <button id="usage-control-override" class="usage-control-btn usage-control-btn-secondary">
            Override (5 minutes)
          </button>
          <button id="usage-control-close" class="usage-control-btn usage-control-btn-primary">
            Close Tab
          </button>
        </div>
        
        <div class="usage-control-footer">
          <p>Take a break and return to productive activities.</p>
        </div>
      </div>
    `;

    this.blockOverlay.style.cssText = `
      position: fixed !important;
      top: 0 !important;
      left: 0 !important;
      width: 100vw !important;
      height: 100vh !important;
      background: rgba(0, 0, 0, 0.95) !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      z-index: 2147483647 !important;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
    `;

    const style = document.createElement('style');
    style.textContent = `
      .usage-control-modal {
        background: white !important;
        border-radius: 16px !important;
        padding: 32px !important;
        max-width: 400px !important;
        width: 90% !important;
        text-align: center !important;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3) !important;
      }
      
      .usage-control-icon {
        margin-bottom: 20px !important;
      }
      
      .usage-control-title {
        color: #333 !important;
        font-size: 24px !important;
        font-weight: 600 !important;
        margin: 0 0 16px 0 !important;
      }
      
      .usage-control-message {
        color: #666 !important;
        font-size: 16px !important;
        line-height: 1.5 !important;
        margin-bottom: 24px !important;
      }
      
      .usage-control-message p {
        margin: 8px 0 !important;
      }
      
      .usage-control-reason {
        color: #FF3B30 !important;
        font-weight: 500 !important;
      }
      
      .usage-control-actions {
        display: flex !important;
        gap: 12px !important;
        margin-bottom: 20px !important;
      }
      
      .usage-control-btn {
        flex: 1 !important;
        padding: 12px 16px !important;
        border: none !important;
        border-radius: 8px !important;
        font-size: 14px !important;
        font-weight: 500 !important;
        cursor: pointer !important;
        transition: all 0.2s ease !important;
      }
      
      .usage-control-btn-primary {
        background: #007AFF !important;
        color: white !important;
      }
      
      .usage-control-btn-primary:hover {
        background: #0056CC !important;
      }
      
      .usage-control-btn-secondary {
        background: #F2F2F7 !important;
        color: #333 !important;
      }
      
      .usage-control-btn-secondary:hover {
        background: #E5E5EA !important;
      }
      
      .usage-control-footer {
        color: #999 !important;
        font-size: 12px !important;
      }
      
      .usage-control-footer p {
        margin: 0 !important;
      }
    `;

    document.head.appendChild(style);
    document.body.appendChild(this.blockOverlay);

    this.setupOverlayEvents();
  }

  setupOverlayEvents() {
    const overrideBtn = document.getElementById('usage-control-override');
    const closeBtn = document.getElementById('usage-control-close');

    if (overrideBtn) {
      overrideBtn.addEventListener('click', () => {
        this.requestOverride();
      });
    }

    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        window.close();
      });
    }
  }

  requestOverride() {
    chrome.runtime.sendMessage({
      type: 'REQUEST_OVERRIDE',
      domain: window.location.hostname,
      duration: 5 * 60 * 1000
    }, (response) => {
      if (response && response.granted) {
        this.unblockSite();
        this.showOverrideNotification();
      } else {
        alert('Override request denied. Daily limit has been reached.');
      }
    });
  }

  showOverrideNotification() {
    const notification = document.createElement('div');
    notification.style.cssText = `
      position: fixed !important;
      top: 20px !important;
      right: 20px !important;
      background: #34C759 !important;
      color: white !important;
      padding: 12px 16px !important;
      border-radius: 8px !important;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
      font-size: 14px !important;
      font-weight: 500 !important;
      z-index: 2147483647 !important;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
    `;
    
    notification.textContent = '5-minute override granted';
    document.body.appendChild(notification);

    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }
    }, 3000);
  }

  disablePageInteraction() {
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';
  }

  unblockSite() {
    if (!this.isBlocked) return;
    
    this.isBlocked = false;
    
    if (this.blockOverlay && this.blockOverlay.parentNode) {
      this.blockOverlay.parentNode.removeChild(this.blockOverlay);
      this.blockOverlay = null;
    }

    document.body.style.overflow = '';
    document.documentElement.style.overflow = '';
  }

  preventNavigation() {
    window.addEventListener('beforeunload', (e) => {
      if (this.isBlocked) {
        e.preventDefault();
        e.returnValue = '';
        return '';
      }
    });

    const originalPushState = history.pushState;
    const originalReplaceState = history.replaceState;

    history.pushState = (...args) => {
      if (!this.isBlocked) {
        originalPushState.apply(history, args);
      }
    };

    history.replaceState = (...args) => {
      if (!this.isBlocked) {
        originalReplaceState.apply(history, args);
      }
    };

    window.addEventListener('popstate', (e) => {
      if (this.isBlocked) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    new ContentBlocker();
  });
} else {
  new ContentBlocker();
}