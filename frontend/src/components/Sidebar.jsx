import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, Fingerprint, History, Settings, FileText, CalendarDays } from 'lucide-react';

export default function Sidebar() {
  const monitorItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Live Events', path: '/attendance', icon: History },
    { name: 'Attendance', path: '/daily-attendance', icon: CalendarDays },
    { name: 'Reports', path: '/reports', icon: FileText },
  ];

  const manageItems = [
    { name: 'Devices', path: '/devices', icon: Fingerprint, badge: '5' },
    { name: 'Employees', path: '/employees', icon: Users },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  const renderNavItems = (items) => (
    <div className="space-y-1">
      {items.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.name}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center justify-between px-3 py-2 text-[13px] font-medium rounded-lg transition-colors duration-200 group ${
                isActive
                  ? 'bg-teal/10 text-teal'
                  : 'text-text-dim hover:bg-surface-2 hover:text-text-primary'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <div className="flex items-center">
                  <Icon
                    className={`mr-3 w-[18px] h-[18px] transition-colors duration-200 ${
                      isActive ? 'text-teal' : 'text-text-faint group-hover:text-text-dim'
                    }`}
                    strokeWidth={2}
                  />
                  {item.name}
                </div>
                {item.badge && (
                  <span className={`px-2 py-0.5 text-[10px] font-mono rounded-full ${
                    isActive ? 'bg-teal/20 text-teal' : 'bg-surface-2 text-text-faint border border-border-soft'
                  }`}>
                    {item.badge}
                  </span>
                )}
              </>
            )}
          </NavLink>
        );
      })}
    </div>
  );

  return (
    <aside className="w-[230px] h-screen fixed left-0 top-0 bg-surface border-r border-border flex flex-col z-20">
      {/* Brand Header */}
      <div className="h-[60px] flex items-center px-5 border-b border-border-soft">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-teal to-teal-dim flex items-center justify-center mr-3 shadow-sm">
          <Fingerprint className="text-[#0c1714] w-5 h-5" strokeWidth={2} />
        </div>
        <h1 className="heading-panel text-[16px] tracking-tight">AiBioSync</h1>
      </div>
      
      {/* Navigation */}
      <div className="flex-1 overflow-y-auto py-5 px-3 flex flex-col gap-6">
        <div>
          <p className="px-3 label-caps mb-2">Monitor</p>
          {renderNavItems(monitorItems)}
        </div>
        
        <div>
          <p className="px-3 label-caps mb-2">Manage</p>
          {renderNavItems(manageItems)}
        </div>
      </div>
      
      {/* User Pill Pinned to Bottom */}
      <div className="p-4 mt-auto border-t border-border-soft">
        <div className="flex items-center px-3 py-2 rounded-xl bg-surface-2 border border-border transition-colors hover:border-text-faint cursor-pointer">
          <div className="w-8 h-8 rounded-full bg-border flex items-center justify-center text-xs font-bold text-text-primary mr-3">
            SA
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-[13px] font-medium text-text-primary truncate">Super Admin</p>
            <p className="text-[11px] text-text-faint truncate">HQ Branch</p>
          </div>
        </div>
      </div>
    </aside>
  );
}
