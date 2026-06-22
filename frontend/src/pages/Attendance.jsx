import React, { useState, useEffect } from 'react';
import { History, Search, Download, RefreshCw, Server, CheckCircle2, AlertCircle, XCircle, Clock, Filter, Calendar as CalendarIcon, ArrowDownRight, ArrowUpRight, Fingerprint, KeyRound, CreditCard, ScanFace } from 'lucide-react';
import { getAttendanceLogs } from '../services/api';

export default function Attendance() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [dateFilter, setDateFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const rowsPerPage = 50;

  const loadLogs = async () => {
    try {
      setLoading(true);
      const res = await getAttendanceLogs();
      setLogs(res.data);
    } catch (error) {
      console.error("Failed to load attendance logs", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLogs();
    const interval = setInterval(loadLogs, 60000); // 1 minute refresh
    return () => clearInterval(interval);
  }, []);

  const StatusBadge = ({ status }) => {
    let config = {
      'check-in': { color: 'text-teal bg-teal/10', icon: ArrowDownRight, label: 'Check-In' },
      'check-out': { color: 'text-blue bg-blue/10', icon: ArrowUpRight, label: 'Check-Out' },
      'late-in': { color: 'text-amber bg-amber/10', icon: Clock, label: 'Late In' },
      'failed': { color: 'text-red bg-red/10', icon: XCircle, label: 'Failed' },
      'auto': { color: 'text-text-dim bg-surface-2', icon: CheckCircle2, label: 'Auto' },
    }[status] || { color: 'text-text-dim bg-surface-2', icon: AlertCircle, label: status };

    const Icon = config.icon;

    return (
      <div className={`inline-flex items-center px-2 py-0.5 rounded-full border border-transparent ${config.color}`}>
        <Icon className="w-3 h-3 mr-1.5" />
        <span className="text-[11px] font-medium tracking-wide uppercase">{config.label}</span>
      </div>
    );
  };

  const getVerificationIcon = (type) => {
    switch (type) {
      case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
        return <Fingerprint className="w-4 h-4 text-teal" title="Fingerprint" />;
      case 10:
        return <KeyRound className="w-4 h-4 text-amber" title="Password" />;
      case 11:
        return <CreditCard className="w-4 h-4 text-blue" title="RFID Card" />;
      case 20: case 21: case 22: case 23: case 24: case 25: case 26: case 27:
        return <ScanFace className="w-4 h-4 text-purple-400" title="Face Recognition" />;
      default:
        return <Fingerprint className="w-4 h-4 text-text-dim" />;
    }
  };

  const filteredLogs = logs.filter(log => {
    const matchesSearch = 
      (log.employeeName?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
      (log.employeeNumber?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
      (log.deviceName?.toLowerCase() || '').includes(searchQuery.toLowerCase());
    
    // Format punchTime to YYYY-MM-DD for date comparison
    let matchesDate = true;
    if (dateFilter && log.punchTime) {
      const logDate = new Date(log.punchTime).toISOString().split('T')[0];
      matchesDate = logDate === dateFilter;
    }
    
    return matchesSearch && matchesDate;
  });

  const totalPages = Math.ceil(filteredLogs.length / rowsPerPage);
  const paginatedLogs = filteredLogs.slice((currentPage - 1) * rowsPerPage, currentPage * rowsPerPage);

  return (
    <div className="space-y-6 pb-12">
      {/* Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <p className="text-text-dim text-[13px]">View biometric authentication events across the network.</p>
        </div>
        <div className="flex w-full sm:w-auto space-x-3">
          <div className="relative flex-1 sm:w-64">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-[14px] w-[14px] text-text-faint" strokeWidth={2.5} />
            </div>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(1); }}
              className="block w-full pl-9 pr-3 py-[7px] text-[13px] border border-border rounded-lg bg-surface-2 text-text-primary placeholder-text-faint focus:outline-none focus:border-teal focus:ring-1 focus:ring-teal transition-colors"
              placeholder="Search logs..."
            />
          </div>
          <div className="relative flex-shrink-0">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <CalendarIcon className="h-[14px] w-[14px] text-text-faint" strokeWidth={2.5} />
            </div>
            <input
              type="date"
              value={dateFilter}
              onChange={(e) => { setDateFilter(e.target.value); setCurrentPage(1); }}
              className="block pl-9 pr-3 py-[7px] text-[13px] border border-border rounded-lg bg-surface-2 text-text-primary focus:outline-none focus:border-teal focus:ring-1 focus:ring-teal transition-colors"
            />
          </div>
          <button onClick={loadLogs} className="flex-shrink-0 flex items-center px-3 py-[7px] border border-border rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 transition-colors">
            <RefreshCw className={`w-[14px] h-[14px] mr-2 text-text-dim ${loading ? 'animate-spin' : ''}`} />
            Fetch Logs
          </button>
        </div>
      </div>

      {/* Main Table Panel */}
      <div className="panel flex flex-col">
        <div className="p-5 border-b border-border-soft flex justify-between items-center bg-surface sticky top-[72px] z-10 rounded-t-xl">
          <div className="flex items-center space-x-2">
            <History className="w-5 h-5 text-teal" />
            <h2 className="heading-panel">Event Stream</h2>
          </div>
          <span className="bg-surface-2 text-text-dim px-2.5 py-0.5 rounded-full text-[12px] font-medium border border-border">
            Total Logs: {filteredLogs.length}
          </span>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead className="bg-surface-2">
              <tr>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Timestamp</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Device & Location</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Method</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-soft">
              {filteredLogs.length === 0 && (
                <tr>
                  <td colSpan="5" className="py-8 text-center text-text-dim text-[13px]">
                    {loading ? "Loading logs..." : "No attendance logs found matching filters."}
                  </td>
                </tr>
              )}
              {paginatedLogs.map((log) => {
                const dateObj = log.punchTime ? new Date(log.punchTime) : null;
                const isLate = dateObj ? (dateObj.getHours() > 9 || (dateObj.getHours() === 9 && dateObj.getMinutes() > 15)) : false;
                
                return (
                  <tr key={log.id} className="hover:bg-surface-2/40 transition-colors">
                    <td className="py-4 px-5">
                      <div className="data-value text-[13px] text-text-primary font-medium">
                        {dateObj ? dateObj.toLocaleTimeString() : '---'}
                      </div>
                      <div className="data-value text-[11px] text-text-faint mt-0.5">
                        {dateObj ? dateObj.toLocaleDateString() : '---'}
                      </div>
                    </td>
                    <td className="py-4 px-5">
                      <div className="text-[14px] font-medium text-text-primary">
                        {log.employeeName || 'Unknown User'}
                      </div>
                      <div className="data-value text-[11px] text-text-faint mt-0.5">
                        {log.employeeNumber || '---'}
                      </div>
                    </td>
                    <td className="py-4 px-5">
                      <div className="flex items-center text-[13px] text-text-primary">
                        <Server className="w-3.5 h-3.5 mr-1.5 text-text-faint" />
                        {log.deviceName || 'Unknown Device'}
                      </div>
                    </td>
                    <td className="py-4 px-5">
                      <div className="flex items-center space-x-2">
                        {getVerificationIcon(log.verificationType)}
                        <span className="text-[12px] text-text-dim">Code: {log.verificationType ?? 'N/A'}</span>
                      </div>
                    </td>
                    <td className="py-4 px-5">
                      <StatusBadge status={
                        log.direction === 0 ? (isLate ? 'late-in' : 'check-in') : 
                        log.direction === 1 ? 'check-out' : 'auto'
                      } />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        
        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-border-soft flex items-center justify-between">
            <div className="text-[13px] text-text-dim">
              Showing <span className="text-text-primary font-medium">{(currentPage - 1) * rowsPerPage + 1}</span> to <span className="text-text-primary font-medium">{Math.min(currentPage * rowsPerPage, filteredLogs.length)}</span> of <span className="text-text-primary font-medium">{filteredLogs.length}</span> logs
            </div>
            <div className="flex space-x-2">
              <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1} className="px-3 py-1.5 rounded-lg border border-border text-text-dim text-[13px] hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50 disabled:hover:bg-transparent">
                Prev
              </button>
              <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages} className="px-3 py-1.5 rounded-lg border border-border text-text-dim text-[13px] hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50 disabled:hover:bg-transparent">
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
