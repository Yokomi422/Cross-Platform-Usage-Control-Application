class PopupController {
  constructor() {
    this.init();
  }

  async init() {
    await this.loadUsageStats();
    await this.loadRestrictions();
    this.setupEventListeners();
  }

  setupEventListeners() {
    document.getElementById('add-restriction').addEventListener('click', () => {
      this.addRestriction();
    });

    document.getElementById('domain').addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        this.addRestriction();
      }
    });

    document.getElementById('time-limit').addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        this.addRestriction();
      }
    });
  }

  async loadUsageStats() {
    try {
      const response = await chrome.runtime.sendMessage({ type: 'GET_USAGE' });
      this.displayUsageStats(response);
    } catch (error) {
      console.error('Error loading usage stats:', error);
      document.getElementById('usage-stats').innerHTML = '<div class="error">Error loading data</div>';
    }
  }

  displayUsageStats(usageData) {
    const container = document.getElementById('usage-stats');
    
    if (!usageData || usageData.length === 0) {
      container.innerHTML = '<div class="stat-item"><span class="domain">No usage data today</span></div>';
      return;
    }

    const sortedData = usageData.sort((a, b) => b[1] - a[1]);
    
    container.innerHTML = sortedData.map(([domain, timeMs]) => {
      const minutes = Math.round(timeMs / (1000 * 60));
      const timeStr = minutes > 60 ? 
        `${Math.floor(minutes / 60)}h ${minutes % 60}m` : 
        `${minutes}m`;
      
      return `
        <div class="stat-item">
          <span class="domain">${domain}</span>
          <span class="time">${timeStr}</span>
        </div>
      `;
    }).join('');
  }

  async loadRestrictions() {
    try {
      const restrictions = await chrome.runtime.sendMessage({ type: 'GET_RESTRICTIONS' });
      this.displayRestrictions(restrictions);
    } catch (error) {
      console.error('Error loading restrictions:', error);
    }
  }

  displayRestrictions(restrictions) {
    const container = document.getElementById('restrictions-list');
    
    if (!restrictions || restrictions.length === 0) {
      container.innerHTML = '<div style="color: #666; font-size: 12px;">No restrictions set</div>';
      return;
    }

    container.innerHTML = restrictions.map(([domain, restriction]) => {
      const limitMinutes = Math.round(restriction.dailyLimit / (1000 * 60));
      return `
        <div class="restriction-item">
          <span>${domain} (${limitMinutes}m/day)</span>
          <button class="btn-danger" onclick="popupController.removeRestriction('${domain}')">Remove</button>
        </div>
      `;
    }).join('');
  }

  async addRestriction() {
    const domainInput = document.getElementById('domain');
    const timeLimitInput = document.getElementById('time-limit');
    
    const domain = domainInput.value.trim();
    const timeLimit = parseInt(timeLimitInput.value);
    
    if (!domain) {
      alert('Please enter a domain');
      return;
    }
    
    if (!timeLimit || timeLimit <= 0) {
      alert('Please enter a valid time limit');
      return;
    }

    try {
      const cleanDomain = domain.replace(/^https?:\/\//, '').replace(/^www\./, '');
      
      const restriction = {
        dailyLimit: timeLimit * 60 * 1000,
        createdAt: Date.now()
      };

      await chrome.runtime.sendMessage({
        type: 'SET_RESTRICTION',
        domain: cleanDomain,
        restriction: restriction
      });

      domainInput.value = '';
      timeLimitInput.value = '';
      
      await this.loadRestrictions();
      await this.loadUsageStats();
      
    } catch (error) {
      console.error('Error adding restriction:', error);
      alert('Error adding restriction');
    }
  }

  async removeRestriction(domain) {
    try {
      await chrome.runtime.sendMessage({
        type: 'SET_RESTRICTION',
        domain: domain,
        restriction: null
      });
      
      await this.loadRestrictions();
      await this.loadUsageStats();
      
    } catch (error) {
      console.error('Error removing restriction:', error);
      alert('Error removing restriction');
    }
  }

  formatTime(ms) {
    const minutes = Math.round(ms / (1000 * 60));
    if (minutes < 60) {
      return `${minutes}m`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `${hours}h ${remainingMinutes}m`;
  }
}

const popupController = new PopupController();