import React, { useState, useEffect, useCallback } from 'react';
import {
  FileText, Download, Search, ChevronLeft, ChevronRight,
  CheckCircle2, Clock, XCircle, Filter, Calendar, Users,
  Building2, Server, X, RefreshCw, SlidersHorizontal
} from 'lucide-react';
import { getFlexibleReport, getEmployees, getDevices } from '../services/api';

const months = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

const StatusBadge = ({ status }) => {
  const cfg = {
    Present: { color: 'text-teal bg-teal/10 border-teal/20', icon: CheckCircle2 },
    Late:    { color: 'text-amber bg-amber/10 border-amber/20', icon: Clock },
    Absent:  { color: 'text-red bg-red/10 border-red/20', icon: XCircle },
  }[status] || { color: 'text-text-dim bg-surface-2 border-border', icon: FileText };
  const Icon = cfg.icon;
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium border ${cfg.color}`}>
      <Icon className="w-3 h-3" />
      {status}
    </span>
  );
};

export default function Reports() {
  // Mode: 'month' or 'range'
  const [mode, setMode] = useState('month');

  // Month mode
  const now = new Date();
  const [selectedYear, setSelectedYear]   = useState(now.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(now.getMonth() + 1);

  // Range mode
  const [fromDate, setFromDate] = useState(() => {
    const d = new Date(); d.setDate(1); return d.toISOString().split('T')[0];
  });
  const [toDate, setToDate] = useState(() => new Date().toISOString().split('T')[0]);

  // Filters
  const [employees, setEmployees]           = useState([]);
  const [devices, setDevices]               = useState([]);
  const [departments, setDepartments]       = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [selectedDevice, setSelectedDevice] = useState('');

  // Table state
  const [reports, setReports]     = useState([]);
  const [loading, setLoading]     = useState(false);
  const [searchQuery, setSearch]  = useState('');
  const [currentPage, setPage]    = useState(1);
  const rowsPerPage = 50;

  // Load employees and devices for filter dropdowns
  useEffect(() => {
    getEmployees().then(r => {
      setEmployees(r.data);
      const depts = [...new Set(r.data.map(e => e.department).filter(Boolean))].sort();
      setDepartments(depts);
    }).catch(() => {});
    getDevices().then(r => setDevices(r.data)).catch(() => {});
  }, []);

  const buildParams = useCallback(() => {
    const params = {};
    if (mode === 'month') {
      const ym = new Date(selectedYear, selectedMonth - 1, 1);
      const lastDay = new Date(selectedYear, selectedMonth, 0).getDate();
      params.from = `${selectedYear}-${String(selectedMonth).padStart(2, '0')}-01`;
      params.to   = `${selectedYear}-${String(selectedMonth).padStart(2, '0')}-${lastDay}`;
    } else {
      if (fromDate) params.from = fromDate;
      if (toDate)   params.to   = toDate;
    }
    if (selectedEmployee) params.employeeId = selectedEmployee;
    if (selectedDepartment) params.department = selectedDepartment;
    if (selectedDevice) params.deviceId = selectedDevice;
    return params;
  }, [mode, selectedYear, selectedMonth, fromDate, toDate, selectedEmployee, selectedDepartment, selectedDevice]);

  const loadReports = useCallback(async () => {
    try {
      setLoading(true);
      const res = await getFlexibleReport(buildParams());
      setReports(res.data);
      setPage(1);
    } catch (e) {
      console.error('Failed to load reports', e);
    } finally {
      setLoading(false);
    }
  }, [buildParams]);

  // Auto-load when month/year changes in month mode
  useEffect(() => {
    if (mode === 'month') loadReports();
  }, [selectedYear, selectedMonth, mode]);

  const filtered = reports.filter(r =>
    (r.employeeName?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
    (r.employeeNumber || '').includes(searchQuery) ||
    (r.department?.toLowerCase() || '').includes(searchQuery.toLowerCase())
  );
  const totalPages = Math.ceil(filtered.length / rowsPerPage);
  const paginated  = filtered.slice((currentPage - 1) * rowsPerPage, currentPage * rowsPerPage);

  // Stats from filtered results (excluding absent)
  const presentCount = filtered.filter(r => r.status === 'Present').length;
  const lateCount    = filtered.filter(r => r.status === 'Late').length;
  const absentCount  = filtered.filter(r => r.status === 'Absent').length;

  const exportCsv = () => {
    const header = 'Date,Employee,ID,Department,Device,First In,Last Out,Hours,Status';
    const rows = filtered.map(r =>
      `"${r.date}","${r.employeeName}","${r.employeeNumber}","${r.department || ''}","${r.deviceName || ''}","${r.firstCheckIn}","${r.lastCheckOut}","${r.totalHours}","${r.status}"`
    );
    const blob = new Blob([[header, ...rows].join('\n')], { type: 'text/csv' });
    const a = Object.assign(document.createElement('a'), {
      href: URL.createObjectURL(blob),
      download: `report_${new Date().toISOString().split('T')[0]}.csv`
    });
    a.click();
  };

  return (
    <div className="space-y-6 pb-12">

      {/* Filter Panel */}
      <div className="panel p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <SlidersHorizontal className="w-5 h-5 text-teal" />
            <h2 className="heading-panel">Report Filters</h2>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setMode('month')}
              className={`px-3 py-1.5 rounded-lg text-[12px] font-medium transition-colors border ${mode === 'month' ? 'bg-teal text-[#0c1714] border-teal' : 'border-border text-text-dim hover:text-text-primary hover:bg-surface-2'}`}
            >
              Month View
            </button>
            <button
              onClick={() => setMode('range')}
              className={`px-3 py-1.5 rounded-lg text-[12px] font-medium transition-colors border ${mode === 'range' ? 'bg-teal text-[#0c1714] border-teal' : 'border-border text-text-dim hover:text-text-primary hover:bg-surface-2'}`}
            >
              Date Range
            </button>
          </div>
        </div>

        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Date Filter */}
          {mode === 'month' ? (
            <div className="col-span-2 lg:col-span-1">
              <label className="block text-[11px] font-medium text-text-dim mb-1.5 uppercase tracking-wider">
                <Calendar className="inline w-3 h-3 mr-1" />Month
              </label>
              <div className="flex gap-2">
                <select
                  value={selectedMonth}
                  onChange={e => setSelectedMonth(Number(e.target.value))}
                  className="flex-1 bg-surface-2 border border-border rounded-lg px-2 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal"
                >
                  {months.map((m, i) => <option key={i} value={i + 1} className="bg-surface">{m}</option>)}
                </select>
                <select
                  value={selectedYear}
                  onChange={e => setSelectedYear(Number(e.target.value))}
                  className="w-24 bg-surface-2 border border-border rounded-lg px-2 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal"
                >
                  {[2023, 2024, 2025, 2026, 2027].map(y => <option key={y} value={y} className="bg-surface">{y}</option>)}
                </select>
              </div>
            </div>
          ) : (
            <div className="col-span-2 lg:col-span-1">
              <label className="block text-[11px] font-medium text-text-dim mb-1.5 uppercase tracking-wider">
                <Calendar className="inline w-3 h-3 mr-1" />Date Range
              </label>
              <div className="flex gap-2 items-center">
                <input type="date" value={fromDate} onChange={e => setFromDate(e.target.value)}
                  className="flex-1 bg-surface-2 border border-border rounded-lg px-2 py-2 text-[12px] text-text-primary focus:outline-none focus:border-teal" />
                <span className="text-text-faint text-[11px]">to</span>
                <input type="date" value={toDate} onChange={e => setToDate(e.target.value)}
                  className="flex-1 bg-surface-2 border border-border rounded-lg px-2 py-2 text-[12px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
            </div>
          )}

          {/* Department Filter */}
          <div>
            <label className="block text-[11px] font-medium text-text-dim mb-1.5 uppercase tracking-wider">
              <Building2 className="inline w-3 h-3 mr-1" />Department
            </label>
            <div className="relative">
              <select
                value={selectedDepartment}
                onChange={e => setSelectedDepartment(e.target.value)}
                className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal appearance-none"
              >
                <option value="">All Departments</option>
                {departments.map(d => <option key={d} value={d} className="bg-surface">{d}</option>)}
              </select>
            </div>
          </div>

          {/* Employee Filter */}
          <div>
            <label className="block text-[11px] font-medium text-text-dim mb-1.5 uppercase tracking-wider">
              <Users className="inline w-3 h-3 mr-1" />Employee
            </label>
            <select
              value={selectedEmployee}
              onChange={e => setSelectedEmployee(e.target.value)}
              className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal"
            >
              <option value="">All Employees</option>
              {employees.map(e => (
                <option key={e.id} value={e.id} className="bg-surface">
                  {e.firstName} {e.lastName} ({e.employeeNumber})
                </option>
              ))}
            </select>
          </div>

          {/* Device Filter */}
          <div>
            <label className="block text-[11px] font-medium text-text-dim mb-1.5 uppercase tracking-wider">
              <Server className="inline w-3 h-3 mr-1" />Device
            </label>
            <select
              value={selectedDevice}
              onChange={e => setSelectedDevice(e.target.value)}
              className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal"
            >
              <option value="">All Devices</option>
              {devices.map(d => <option key={d.id} value={d.id} className="bg-surface">{d.name}</option>)}
            </select>
          </div>
        </div>

        {/* Active Filter Tags + Actions */}
        <div className="flex items-center justify-between pt-1 flex-wrap gap-3">
          <div className="flex gap-2 flex-wrap">
            {selectedDepartment && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] bg-teal/10 text-teal border border-teal/20">
                <Building2 className="w-3 h-3" />{selectedDepartment}
                <button onClick={() => setSelectedDepartment('')}><X className="w-3 h-3 ml-0.5" /></button>
              </span>
            )}
            {selectedEmployee && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] bg-blue/10 text-blue border border-blue/20">
                <Users className="w-3 h-3" />{employees.find(e => String(e.id) === String(selectedEmployee))?.firstName || 'Employee'}
                <button onClick={() => setSelectedEmployee('')}><X className="w-3 h-3 ml-0.5" /></button>
              </span>
            )}
            {selectedDevice && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] bg-purple-400/10 text-purple-400 border border-purple-400/20">
                <Server className="w-3 h-3" />{devices.find(d => String(d.id) === String(selectedDevice))?.name || 'Device'}
                <button onClick={() => setSelectedDevice('')}><X className="w-3 h-3 ml-0.5" /></button>
              </span>
            )}
          </div>
          <div className="flex gap-2">
            <button onClick={loadReports} className="flex items-center px-4 py-2 rounded-lg text-[13px] font-medium border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors">
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              Generate Report
            </button>
            <button onClick={exportCsv} className="flex items-center px-4 py-2 rounded-lg text-[13px] font-semibold bg-teal text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm">
              <Download className="w-3.5 h-3.5 mr-1.5" />
              Export CSV
            </button>
          </div>
        </div>
      </div>

      {/* Summary Stats */}
      {reports.length > 0 && (
        <div className="grid grid-cols-3 gap-4">
          {[
            { label: 'Present', count: presentCount, color: 'text-teal', bg: 'bg-teal/10', icon: CheckCircle2 },
            { label: 'Late', count: lateCount, color: 'text-amber', bg: 'bg-amber/10', icon: Clock },
            { label: 'Absent', count: absentCount, color: 'text-red', bg: 'bg-red/10', icon: XCircle },
          ].map(({ label, count, color, bg, icon: Icon }) => (
            <div key={label} className="panel p-4 flex items-center gap-4">
              <div className={`w-10 h-10 rounded-xl ${bg} flex items-center justify-center shrink-0`}>
                <Icon className={`w-5 h-5 ${color}`} />
              </div>
              <div>
                <p className="text-[12px] text-text-dim">{label}</p>
                <p className={`text-[22px] font-bold ${color} leading-tight`}>{count}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Table */}
      <div className="panel flex flex-col">
        <div className="p-5 border-b border-border-soft flex flex-col sm:flex-row justify-between sm:items-center gap-3 sticky top-[72px] bg-surface z-10 rounded-t-xl">
          <div className="flex items-center gap-2">
            <FileText className="w-5 h-5 text-teal" />
            <h2 className="heading-panel">Attendance Report</h2>
          </div>
          <div className="flex items-center gap-3">
            <div className="relative w-56">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-faint" />
              <input
                type="text" value={searchQuery}
                onChange={e => { setSearch(e.target.value); setPage(1); }}
                placeholder="Search name, ID, dept..."
                className="w-full pl-9 pr-3 py-[7px] text-[13px] border border-border rounded-lg bg-surface-2 text-text-primary placeholder-text-faint focus:outline-none focus:border-teal transition-colors"
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
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Date</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Department</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Device</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-center">First In</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-center">Last Out</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-center">Hours</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-soft">
              {filtered.length === 0 && (
                <tr>
                  <td colSpan="8" className="py-12 text-center text-text-dim text-[13px]">
                    {loading ? 'Generating report...' : 'No data. Select filters and click Generate Report.'}
                  </td>
                </tr>
              )}
              {paginated.map((r, i) => (
                <tr key={i} className="hover:bg-surface-2/40 transition-colors">
                  <td className="py-3.5 px-5">
                    <div className="text-[13px] font-medium text-text-primary">
                      {new Date(r.date + 'T00:00:00').toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' })}
                    </div>
                  </td>
                  <td className="py-3.5 px-5">
                    <div className="text-[13px] font-medium text-text-primary">{r.employeeName || 'Unknown'}</div>
                    <div className="text-[11px] text-text-faint font-mono mt-0.5">{r.employeeNumber}</div>
                  </td>
                  <td className="py-3.5 px-5 text-[13px] text-text-dim">{r.department || '—'}</td>
                  <td className="py-3.5 px-5 text-[12px] text-text-dim">{r.deviceName || '—'}</td>
                  <td className="py-3.5 px-5 text-center">
                    <span className={`text-[13px] font-mono font-semibold ${r.firstCheckIn !== '---' ? 'text-teal' : 'text-text-faint'}`}>
                      {r.firstCheckIn}
                    </span>
                  </td>
                  <td className="py-3.5 px-5 text-center">
                    <span className={`text-[13px] font-mono font-semibold ${r.lastCheckOut !== '---' ? 'text-blue' : 'text-text-faint'}`}>
                      {r.lastCheckOut}
                    </span>
                  </td>
                  <td className="py-3.5 px-5 text-center text-[13px] font-medium text-text-primary">{r.totalHours}</td>
                  <td className="py-3.5 px-5"><StatusBadge status={r.status} /></td>
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
              <button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
                className="p-1.5 rounded-lg border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-40">
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}
                className="p-1.5 rounded-lg border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-40">
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
