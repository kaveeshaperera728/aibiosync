import React, { useState, useEffect } from 'react';
import { Fingerprint, CheckCircle2, Clock, ArrowUpRight, ArrowDownRight, Server, XCircle, RefreshCw } from 'lucide-react';
import { getDashboardStats } from '../services/api';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const res = await getDashboardStats();
      setStats(res.data);
    } catch (e) {
      console.error("Failed to fetch dashboard stats", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 10000); // Poll every 10s
    return () => clearInterval(interval);
  }, []);

  // Status dot component with the pulse animation
  const StatusDot = ({ status }) => {
    let colorClass = '';
    if (status === 'online') colorClass = 'bg-teal';
    if (status === 'late') colorClass = 'bg-amber';
    if (status === 'offline') colorClass = 'bg-red';
    if (status === 'break') colorClass = 'bg-blue';

    return (
      <div className="relative flex items-center justify-center w-2.5 h-2.5">
        {status === 'online' && (
          <div className={`absolute w-full h-full rounded-full opacity-75 animate-radar-ping ${colorClass}`} />
        )}
        <div className={`relative w-2 h-2 rounded-full ${colorClass}`} />
      </div>
    );
  };

  const Badge = ({ status, label }) => {
    let colorMap = {
      online: 'bg-teal/10 text-teal',
      late: 'bg-amber/10 text-amber',
      offline: 'bg-red/10 text-red',
      break: 'bg-blue/10 text-blue',
    };
    return (
      <div className={`flex items-center px-2 py-0.5 rounded-full ${colorMap[status] || colorMap['online']} border border-transparent`}>
        <StatusDot status={status || 'online'} />
        <span className="ml-1.5 text-[11px] font-medium tracking-wide uppercase">{label}</span>
      </div>
    );
  };

  const DeviceChip = ({ name, id, status }) => (
    <div className="flex-shrink-0 flex items-center p-2.5 bg-surface-2 border border-border rounded-lg min-w-[180px]">
      <div className="w-8 h-8 rounded-md border border-border-soft bg-surface flex items-center justify-center mr-3">
        <Server className="w-4 h-4 text-text-dim" />
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-0.5">
          <p className="text-[13px] font-medium text-text-primary truncate">{name || 'Unknown'}</p>
          <StatusDot status={status === 'online' ? 'online' : 'offline'} />
        </div>
        <p className="data-value text-[11px] text-text-faint truncate">{id}</p>
      </div>
    </div>
  );

  const StatCard = ({ label, value, delta, isPositive, icon: Icon }) => (
    <div className="panel p-5 relative overflow-hidden group">
      <div className="flex justify-between items-start mb-4">
        <div className="w-10 h-10 rounded-lg bg-surface-2 border border-border-soft flex items-center justify-center">
          <Icon className="w-5 h-5 text-text-dim" strokeWidth={1.5} />
        </div>
        {delta && (
          <div className={`flex items-center text-[12px] font-medium ${isPositive ? 'text-teal' : 'text-red'}`}>
            {isPositive ? <ArrowUpRight className="w-3.5 h-3.5 mr-0.5" /> : <ArrowDownRight className="w-3.5 h-3.5 mr-0.5" />}
            {delta}
          </div>
        )}
      </div>
      <div>
        <h3 className="stat-value mb-1">{value}</h3>
        <p className="text-[13px] text-text-dim">{label}</p>
      </div>
    </div>
  );

  if (!stats) return <div className="p-8 text-center text-text-dim">Loading dashboard...</div>;

  return (
    <div className="space-y-6 pb-12">
      
      {/* Fleet Pulse Strip Hero */}
      <div className="panel p-4 flex flex-col md:flex-row items-center gap-6">
        {/* Left: Overall Count */}
        <div className="flex flex-col items-center md:items-start md:border-r border-border-soft md:pr-6 min-w-[150px]">
          <p className="label-caps mb-1">Fleet Status</p>
          <div className="flex items-baseline space-x-2">
            <span className="stat-value text-teal">{stats.onlineDevices}</span>
            <span className="text-[16px] text-text-dim font-medium">/ {stats.totalDevices}</span>
          </div>
          <p className="text-[12px] text-text-faint mt-1">Devices Online</p>
        </div>

        {/* Center: Horizontally scrolling chips */}
        <div className="flex-1 min-w-0 flex overflow-x-auto pb-2 -mb-2 gap-3 no-scrollbar">
          {stats.deviceHealth.map((d, i) => (
            <DeviceChip key={i} name={d.name} id={d.ip} status={d.status} />
          ))}
          {stats.deviceHealth.length === 0 && <div className="text-[13px] text-text-dim">No devices configured</div>}
        </div>

        {/* Right: Summary Numbers */}
        <div className="flex gap-6 md:pl-6 md:border-l border-border-soft min-w-[200px]">
          <div>
            <p className="label-caps mb-1">Queued Cmds</p>
            <p className={`data-value text-[16px] ${stats.queuedCommands > 0 ? 'text-amber' : 'text-teal'}`}>{stats.queuedCommands}</p>
          </div>
          <div>
            <p className="label-caps mb-1">Errors</p>
            <p className="data-value text-[16px] text-teal">0</p>
          </div>
        </div>
      </div>

      {/* Stat Card Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard label="Total Punches Today" value={stats.totalPunchesToday} isPositive={true} icon={Fingerprint} />
        <StatCard label="On-Time Arrivals" value={stats.onTimeArrivals} isPositive={true} icon={CheckCircle2} />
        <StatCard label="Late Check-ins" value={stats.lateCheckIns} isPositive={false} icon={Clock} />
        <StatCard label="Failed Verifications" value={stats.failedVerifications} isPositive={false} icon={XCircle} />
      </div>

      {/* Panels Grid: 1.55fr / 1fr */}
      <div className="grid grid-cols-1 lg:grid-cols-[1.55fr_1fr] gap-6">
        
        {/* Live Event Table */}
        <div className="panel flex flex-col h-[400px]">
          <div className="p-5 border-b border-border-soft flex justify-between items-center">
            <h2 className="heading-panel">Live Activity Stream</h2>
          </div>
          <div className="flex-1 overflow-auto p-0">
            <table className="w-full text-left border-collapse">
              <thead className="bg-surface-2 sticky top-0 z-10">
                <tr>
                  <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Time</th>
                  <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                  <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Device</th>
                  <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-soft">
                {stats.liveActivity.length === 0 && (
                  <tr><td colSpan="4" className="text-center p-4 text-[13px] text-text-dim">No activity yet</td></tr>
                )}
                {stats.liveActivity.map((row, i) => (
                  <tr key={i} className="hover:bg-surface-2/50 transition-colors">
                    <td className="py-3 px-5 data-value text-[13px] text-text-dim">{row.time}</td>
                    <td className="py-3 px-5">
                      <div className="text-[13px] font-medium text-text-primary">{row.employeeName}</div>
                      <div className="data-value text-[11px] text-text-faint">{row.employeeId}</div>
                    </td>
                    <td className="py-3 px-5 text-[13px] text-text-dim">{row.deviceName}</td>
                    <td className="py-3 px-5"><Badge status={row.status} label={row.label} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Device List */}
        <div className="panel flex flex-col h-[400px]">
          <div className="p-5 border-b border-border-soft flex justify-between items-center">
            <h2 className="heading-panel">Device Health</h2>
            <div className="flex items-center text-[12px] text-text-faint">
              <RefreshCw className={`w-3 h-3 mr-1 ${loading ? 'animate-spin' : ''}`} /> Auto-sync ON
            </div>
          </div>
          <div className="flex-1 overflow-auto p-2">
            <div className="space-y-1">
              {stats.deviceHealth.length === 0 && <div className="p-3 text-[13px] text-text-dim">No devices configured</div>}
              {stats.deviceHealth.map((dev, i) => (
                <div key={i} className="flex items-center justify-between p-3 rounded-lg hover:bg-surface-2 transition-colors cursor-pointer group">
                  <div className="flex items-center">
                    <div className="w-8 h-8 rounded-md bg-surface-2 border border-border flex items-center justify-center mr-3 group-hover:border-text-faint transition-colors">
                      <Fingerprint className="w-4 h-4 text-text-dim" />
                    </div>
                    <div>
                      <p className="text-[13px] font-medium text-text-primary">{dev.name}</p>
                      <p className="data-value text-[11px] text-text-faint">{dev.ip}</p>
                    </div>
                  </div>
                  <div className="flex flex-col items-end">
                    <StatusDot status={dev.status === 'online' ? 'online' : 'offline'} />
                    <span className="data-value text-[10px] text-text-faint mt-1">{dev.ping}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
