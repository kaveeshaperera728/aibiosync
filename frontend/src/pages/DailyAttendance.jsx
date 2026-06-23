import React, { useState, useEffect, useCallback } from 'react';
import {
  CalendarDays, Search, Download, RefreshCw, Clock, LogIn, LogOut,
  Users, TrendingUp, AlertCircle, CheckCircle2, Calendar as CalendarIcon,
  ChevronLeft, ChevronRight, Timer
} from 'lucide-react';
import { getDailyAttendance } from '../services/api';

function formatTime(dtStr) {
  if (!dtStr) return '—';
  return new Date(dtStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatHours(hours) {
  if (!hours || hours === 0) return '—';
  const h = Math.floor(hours);
  const m = Math.round((hours - h) * 60);
  return `${h}h ${m}m`;
}

const StatusBadge = ({ status, hasCheckout }) => {
  if (!hasCheckout) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-amber/10 text-amber border border-amber/20">
        <Clock className="w-3 h-3" />
        In Progress
      </span>
    );
  }
  const config = {
    PRESENT: { color: 'bg-teal/10 text-teal border-teal/20', icon: CheckCircle2, label: 'Present' },
    HALF_DAY: { color: 'bg-amber/10 text-amber border-amber/20', icon: AlertCircle, label: 'Half Day' },
  }[status] || { color: 'bg-surface-2 text-text-dim border-border', icon: AlertCircle, label: status };
  const Icon = config.icon;
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium border ${config.color}`}>
      <Icon className="w-3 h-3" />
      {config.label}
    </span>
  );
};

export default function DailyAttendance() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [currentPage, setCurrentPage] = useState(1);
  const rowsPerPage = 50;

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const res = await getDailyAttendance(selectedDate);
      setRecords(res.data);
      setCurrentPage(1);
    } catch (error) {
      console.error('Failed to load daily attendance', error);
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => { loadData(); }, [loadData]);

  const filtered = records.filter(r =>
    (r.employeeName?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
    (r.employeeNumber?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
    (r.department?.toLowerCase() || '').includes(searchQuery.toLowerCase())
  );

  const totalPages = Math.ceil(filtered.length / rowsPerPage);
  const paginated = filtered.slice((currentPage - 1) * rowsPerPage, currentPage * rowsPerPage);

  // Stats
  const presentCount = records.filter(r => r.lastOut).length;
  const inProgressCount = records.filter(r => !r.lastOut).length;
  const avgHours = records.length
    ? (records.reduce((sum, r) => sum + (r.hoursWorked || 0), 0) / records.filter(r => r.hoursWorked > 0).length || 0)
    : 0;

  const goToPrevDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() - 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };
  const goToNextDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };
  const goToToday = () => setSelectedDate(new Date().toISOString().split('T')[0]);

  const exportCsv = () => {
    const header = 'Employee,ID,Department,Date,First In,Last Out,Hours Worked,Status';
    const rows = filtered.map(r =>
      `"${r.employeeName}","${r.employeeNumber}","${r.department || ''}","${r.date}","${formatTime(r.firstIn)}","${formatTime(r.lastOut)}","${formatHours(r.hoursWorked)}","${r.lastOut ? r.status : 'In Progress'}"`
    );
    const blob = new Blob([[header, ...rows].join('\n')], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `attendance_${selectedDate}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const displayDate = new Date(selectedDate + 'T00:00:00').toLocaleDateString([], {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  });

  return (
    <div className="space-y-6 pb-12">

      {/* Header Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <p className="text-text-dim text-[13px]">First In and Last Out times for each employee per day.</p>
        <div className="flex items-center gap-2 flex-wrap">
          {/* Date Navigation */}
          <div className="flex items-center gap-1 bg-surface-2 border border-border rounded-lg p-1">
            <button onClick={goToPrevDay} className="p-1 rounded hover:bg-surface hover:text-text-primary text-text-dim transition-colors">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <div className="relative">
              <CalendarIcon className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-faint pointer-events-none" />
              <input
                type="date"
                value={selectedDate}
                onChange={e => setSelectedDate(e.target.value)}
                className="pl-8 pr-2 py-1 text-[12px] bg-transparent text-text-primary focus:outline-none"
              />
            </div>
            <button onClick={goToNextDay} className="p-1 rounded hover:bg-surface hover:text-text-primary text-text-dim transition-colors">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
          <button onClick={goToToday} className="px-3 py-[7px] text-[12px] font-medium border border-border rounded-lg text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors">
            Today
          </button>
          <button onClick={exportCsv} className="flex items-center px-3 py-[7px] text-[12px] font-medium border border-border rounded-lg text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors">
            <Download className="w-3.5 h-3.5 mr-1.5" />
            Export CSV
          </button>
          <button onClick={loadData} className="flex items-center px-3 py-[7px] text-[12px] font-medium border border-border rounded-lg text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors">
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: 'Total Present', value: records.length, icon: Users, color: 'text-teal', bg: 'bg-teal/10' },
          { label: 'Completed Day', value: presentCount, icon: CheckCircle2, color: 'text-blue', bg: 'bg-blue/10' },
          { label: 'Still In Office', value: inProgressCount, icon: Clock, color: 'text-amber', bg: 'bg-amber/10' },
          { label: 'Avg. Hours', value: formatHours(avgHours), icon: Timer, color: 'text-purple-400', bg: 'bg-purple-400/10' },
        ].map(({ label, value, icon: Icon, color, bg }) => (
          <div key={label} className="panel p-4 flex items-center gap-4">
            <div className={`w-10 h-10 rounded-xl ${bg} flex items-center justify-center shrink-0`}>
              <Icon className={`w-5 h-5 ${color}`} />
            </div>
            <div>
              <p className="text-[12px] text-text-dim">{label}</p>
              <p className={`text-[22px] font-bold ${color} leading-tight`}>{value}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Table Panel */}
      <div className="panel flex flex-col">
        <div className="p-5 border-b border-border-soft flex flex-col sm:flex-row justify-between sm:items-center gap-3 sticky top-[72px] bg-surface z-10 rounded-t-xl">
          <div className="flex items-center gap-2">
            <CalendarDays className="w-5 h-5 text-teal" />
            <div>
              <h2 className="heading-panel">Daily Attendance</h2>
              <p className="text-[11px] text-text-faint mt-0.5">{displayDate}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="relative w-56">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-faint" />
              <input
                type="text"
                value={searchQuery}
                onChange={e => { setSearchQuery(e.target.value); setCurrentPage(1); }}
                placeholder="Search employee..."
                className="w-full pl-9 pr-3 py-[7px] text-[13px] border border-border rounded-lg bg-surface-2 text-text-primary placeholder-text-faint focus:outline-none focus:border-teal focus:ring-1 focus:ring-teal transition-colors"
              />
            </div>
            <span className="bg-surface-2 text-text-dim px-2.5 py-0.5 rounded-full text-[12px] font-medium border border-border whitespace-nowrap">
              {filtered.length} records
            </span>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead className="bg-surface-2">
              <tr>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Department</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">
                  <div className="flex items-center gap-1.5"><LogIn className="w-3.5 h-3.5 text-teal" /> First In</div>
                </th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">
                  <div className="flex items-center gap-1.5"><LogOut className="w-3.5 h-3.5 text-blue" /> Last Out</div>
                </th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Hours Worked</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-soft">
              {filtered.length === 0 && (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-text-dim text-[13px]">
                    {loading ? 'Loading attendance data...' : `No attendance records found for ${displayDate}.`}
                  </td>
                </tr>
              )}
              {paginated.map((r, i) => (
                <tr key={`${r.employeeId}-${r.date}-${i}`} className="hover:bg-surface-2/40 transition-colors">
                  <td className="py-4 px-5">
                    <div className="text-[13px] font-medium text-text-primary">{r.employeeName}</div>
                    <div className="text-[11px] text-text-faint font-mono mt-0.5">{r.employeeNumber}</div>
                  </td>
                  <td className="py-4 px-5 text-[13px] text-text-dim">{r.department || '—'}</td>
                  <td className="py-4 px-5">
                    <div className="flex items-center gap-2">
                      <div className="w-1.5 h-1.5 rounded-full bg-teal shrink-0" />
                      <span className="text-[14px] font-semibold text-teal font-mono">{formatTime(r.firstIn)}</span>
                    </div>
                  </td>
                  <td className="py-4 px-5">
                    {r.lastOut ? (
                      <div className="flex items-center gap-2">
                        <div className="w-1.5 h-1.5 rounded-full bg-blue shrink-0" />
                        <span className="text-[14px] font-semibold text-blue font-mono">{formatTime(r.lastOut)}</span>
                      </div>
                    ) : (
                      <span className="text-[12px] text-text-faint italic">Not checked out</span>
                    )}
                  </td>
                  <td className="py-4 px-5">
                    <span className="text-[13px] font-medium text-text-primary">{formatHours(r.hoursWorked)}</span>
                  </td>
                  <td className="py-4 px-5">
                    <StatusBadge status={r.status} hasCheckout={!!r.lastOut} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="p-4 border-t border-border-soft flex items-center justify-between">
            <div className="text-[13px] text-text-dim">
              Showing <span className="text-text-primary font-medium">{(currentPage - 1) * rowsPerPage + 1}</span> to{' '}
              <span className="text-text-primary font-medium">{Math.min(currentPage * rowsPerPage, filtered.length)}</span> of{' '}
              <span className="text-text-primary font-medium">{filtered.length}</span>
            </div>
            <div className="flex gap-2">
              <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
                className="px-3 py-1.5 rounded-lg border border-border text-text-dim text-[13px] hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-40">
                Prev
              </button>
              <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}
                className="px-3 py-1.5 rounded-lg border border-border text-text-dim text-[13px] hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-40">
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
