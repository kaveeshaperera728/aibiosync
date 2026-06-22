import React from 'react';
import { Search, RefreshCw } from 'lucide-react';
import { useLocation } from 'react-router-dom';

export default function TopBar() {
  const location = useLocation();
  const getPageTitle = () => {
    switch (location.pathname) {
      case '/': return 'Dashboard';
      case '/devices': return 'Device Management';
      case '/employees': return 'Employee Management';
      case '/attendance': return 'Live Events';
      case '/settings': return 'System Settings';
      default: return 'Overview';
    }
  };

  const getSubtitle = () => {
    const d = new Date();
    return `${d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })} • Last sync 2m ago`;
  };

  return (
    <header className="h-[72px] bg-transparent flex items-center justify-between px-8 sticky top-0 z-10 backdrop-blur-md border-b border-border-soft">
      <div className="flex flex-col">
        <h2 className="heading-page">{getPageTitle()}</h2>
        <p className="text-[12px] text-text-dim mt-0.5">{getSubtitle()}</p>
      </div>
      
      <div className="flex items-center space-x-3">
        {/* Search */}
        <div className="relative w-64 hidden md:block">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-[14px] w-[14px] text-text-faint" strokeWidth={2.5} />
          </div>
          <input
            type="text"
            className="block w-full pl-9 pr-3 py-[7px] text-[13px] border border-border rounded-lg bg-surface-2 text-text-primary placeholder-text-faint focus:outline-none focus:border-teal focus:ring-1 focus:ring-teal transition-colors"
            placeholder="Search..."
          />
        </div>
        
        {/* Secondary Button - Ghost */}
        <button className="flex items-center px-3 py-[7px] border border-border rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 hover:border-text-faint transition-colors">
          <RefreshCw className="w-[14px] h-[14px] mr-2 text-text-dim" />
          Sync Now
        </button>
      </div>
    </header>
  );
}
