import React, { useState, useEffect } from 'react';
import { Calendar as CalendarIcon, Download, Search, FileText, ChevronLeft, ChevronRight, CheckCircle2, Clock, XCircle } from 'lucide-react';
import { getMonthlyReport } from '../services/api';

export default function Reports() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const currentDate = new Date();
  const [selectedYear, setSelectedYear] = useState(currentDate.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(currentDate.getMonth() + 1);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const rowsPerPage = 50;

  const loadReports = async () => {
    try {
      setLoading(true);
      const res = await getMonthlyReport(selectedYear, selectedMonth);
      setReports(res.data);
    } catch (error) {
      console.error("Failed to load reports", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReports();
  }, [selectedYear, selectedMonth]);

  const StatusBadge = ({ status }) => {
    let config = {
      'Present': { color: 'text-teal bg-teal/10', icon: CheckCircle2 },
      'Absent': { color: 'text-red bg-red/10', icon: XCircle },
      'Late': { color: 'text-amber bg-amber/10', icon: Clock },
    }[status] || { color: 'text-text-dim bg-surface-2', icon: FileText };

    const Icon = config.icon;

    return (
      <div className={`inline-flex items-center px-2 py-0.5 rounded-full border border-transparent ${config.color}`}>
        <Icon className="w-3 h-3 mr-1.5" />
        <span className="text-[11px] font-medium tracking-wide uppercase">{status}</span>
      </div>
    );
  };

  const filteredReports = reports.filter(r => 
    (r.employeeName?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
    (r.employeeNumber || '').includes(searchQuery)
  );

  const totalPages = Math.ceil(filteredReports.length / rowsPerPage);
  const paginatedReports = filteredReports.slice((currentPage - 1) * rowsPerPage, currentPage * rowsPerPage);

  const months = [
    'January', 'February', 'March', 'April', 'May', 'June', 
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <p className="text-text-dim text-[13px]">Analyze daily and monthly aggregated attendance data.</p>
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
              placeholder="Search employee..."
            />
          </div>
          
          <div className="flex items-center space-x-2 bg-surface-2 border border-border rounded-lg px-2 py-[5px]">
            <select 
              value={selectedMonth} 
              onChange={(e) => setSelectedMonth(Number(e.target.value))}
              className="bg-transparent text-[13px] text-text-primary focus:outline-none"
            >
              {months.map((m, i) => (
                <option key={i} value={i + 1} className="bg-surface">{m}</option>
              ))}
            </select>
            <select 
              value={selectedYear} 
              onChange={(e) => setSelectedYear(Number(e.target.value))}
              className="bg-transparent text-[13px] text-text-primary focus:outline-none"
            >
              {[2023, 2024, 2025, 2026].map(y => (
                <option key={y} value={y} className="bg-surface">{y}</option>
              ))}
            </select>
          </div>

          <button className="flex-shrink-0 flex items-center px-4 py-[7px] bg-teal rounded-lg text-[13px] font-semibold text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm">
            <Download className="w-[14px] h-[14px] mr-2" />
            Export CSV
          </button>
        </div>
      </div>

      {/* Main Table Panel */}
      <div className="panel flex flex-col">
        <div className="p-5 border-b border-border-soft flex justify-between items-center bg-surface sticky top-[72px] z-10 rounded-t-xl">
          <div className="flex items-center space-x-2">
            <FileText className="w-5 h-5 text-teal" />
            <h2 className="heading-panel">Monthly Attendance Report</h2>
          </div>
          <span className="bg-surface-2 text-text-dim px-2.5 py-0.5 rounded-full text-[12px] font-medium border border-border">
            Total Records: {filteredReports.length}
          </span>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead className="bg-surface-2">
              <tr>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Date</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-center">First Check-In</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-center">Last Check-Out</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-center">Total Hours</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-soft">
              {filteredReports.length === 0 && (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-text-dim text-[13px]">
                    {loading ? "Generating report..." : "No data available for this month."}
                  </td>
                </tr>
              )}
              {paginatedReports.map((r, i) => (
                <tr key={i} className="hover:bg-surface-2/40 transition-colors">
                  <td className="py-4 px-5">
                    <div className="data-value text-[13px] text-text-primary font-medium">
                      {new Date(r.date).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' })}
                    </div>
                  </td>
                  <td className="py-4 px-5">
                    <div className="text-[14px] font-medium text-text-primary">
                      {r.employeeName || 'Unknown'}
                    </div>
                    <div className="data-value text-[11px] text-text-faint mt-0.5">
                      {r.employeeNumber || '---'}
                    </div>
                  </td>
                  <td className="py-4 px-5 text-center">
                    <div className="data-value text-[13px] font-medium text-text-primary">{r.firstCheckIn}</div>
                  </td>
                  <td className="py-4 px-5 text-center">
                    <div className="data-value text-[13px] font-medium text-text-primary">{r.lastCheckOut}</div>
                  </td>
                  <td className="py-4 px-5 text-center">
                    <div className="data-value text-[13px] font-medium text-text-primary">{r.totalHours}</div>
                  </td>
                  <td className="py-4 px-5">
                    <StatusBadge status={r.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-border-soft flex items-center justify-between">
            <div className="text-[13px] text-text-dim">
              Showing <span className="text-text-primary font-medium">{(currentPage - 1) * rowsPerPage + 1}</span> to <span className="text-text-primary font-medium">{Math.min(currentPage * rowsPerPage, filteredReports.length)}</span> of <span className="text-text-primary font-medium">{filteredReports.length}</span> records
            </div>
            <div className="flex space-x-2">
              <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1} className="p-1.5 rounded-lg border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50 disabled:hover:bg-transparent">
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages} className="p-1.5 rounded-lg border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50 disabled:hover:bg-transparent">
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
