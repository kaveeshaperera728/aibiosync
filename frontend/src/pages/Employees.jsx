import React, { useState, useEffect } from 'react';
import { Users, Plus, MoreVertical, Search, Trash2, Edit2, Shield, RefreshCw, ChevronLeft, ChevronRight, Fingerprint, KeyRound, CreditCard, ScanFace, X } from 'lucide-react';
import { getEmployees, syncUsersFromDevice } from '../services/api';
import api from '../services/api';

export default function Employees() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Modals
  const [showModal, setShowModal] = useState(false);
  const [editingEmp, setEditingEmp] = useState(null);
  const [formData, setFormData] = useState({
    firstName: '', lastName: '', employeeNumber: '', department: '', designation: '', status: 'ACTIVE'
  });

  const rowsPerPage = 50;

  const loadEmployees = async () => {
    try {
      setLoading(true);
      const res = await getEmployees();
      setEmployees(res.data);
    } catch (error) {
      console.error("Failed to load employees", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEmployees();
  }, []);

  const handleSync = async () => {
    try {
      setSyncing(true);
      await syncUsersFromDevice();
      setTimeout(loadEmployees, 2000);
      setTimeout(loadEmployees, 5000);
      setTimeout(loadEmployees, 10000);
    } catch (e) {
      console.error("Failed to queue sync command", e);
    } finally {
      setTimeout(() => setSyncing(false), 2000);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this employee?')) {
      try {
        await api.delete(`/employees/${id}`);
        loadEmployees();
      } catch (e) {
        console.error('Failed to delete', e);
      }
    }
  };

  const handleSave = async () => {
    try {
      if (editingEmp) {
        await api.put(`/employees/${editingEmp.id}`, formData);
      } else {
        await api.post('/employees', formData);
      }
      setShowModal(false);
      loadEmployees();
    } catch (e) {
      console.error('Failed to save', e);
    }
  };

  const openModal = (emp = null) => {
    if (emp) {
      setEditingEmp(emp);
      setFormData({ ...emp });
    } else {
      setEditingEmp(null);
      setFormData({
        firstName: '', lastName: '', employeeNumber: '', department: '', designation: '', status: 'ACTIVE'
      });
    }
    setShowModal(true);
  };

  const StatusBadge = ({ status }) => {
    const isActive = status?.toLowerCase() === 'active';
    return (
      <div className={`inline-flex items-center px-2 py-0.5 rounded-full border border-transparent ${isActive ? 'bg-teal/10 text-teal' : 'bg-surface-2 text-text-dim border-border'}`}>
        <span className="text-[11px] font-medium tracking-wide uppercase">{status}</span>
      </div>
    );
  };

  const filteredEmployees = employees.filter(e => 
    (e.firstName?.toLowerCase() || '').includes(searchQuery.toLowerCase()) || 
    (e.employeeNumber || '').includes(searchQuery) ||
    (e.department?.toLowerCase() || '').includes(searchQuery.toLowerCase())
  );

  const totalPages = Math.ceil(filteredEmployees.length / rowsPerPage);
  const paginatedEmployees = filteredEmployees.slice((currentPage - 1) * rowsPerPage, currentPage * rowsPerPage);

  return (
    <div className="space-y-6 pb-12 relative">
      {/* Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <p className="text-text-dim text-[13px]">Manage personnel records, departments, and access levels.</p>
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
              placeholder="Search employees..."
            />
          </div>
          <button onClick={handleSync} disabled={syncing} className="flex-shrink-0 flex items-center px-3 py-[7px] border border-border rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50">
            <RefreshCw className={`w-[14px] h-[14px] mr-2 text-text-dim ${syncing ? 'animate-spin' : ''}`} />
            Sync from Device
          </button>
          <button onClick={() => openModal()} className="flex-shrink-0 flex items-center px-4 py-[7px] bg-teal rounded-lg text-[13px] font-semibold text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm">
            <Plus className="w-[16px] h-[16px] mr-1" strokeWidth={2.5} />
            Add Employee
          </button>
        </div>
      </div>

      {/* Main Table Panel */}
      <div className="panel flex flex-col">
        <div className="p-5 border-b border-border-soft flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <Users className="w-5 h-5 text-teal" />
            <h2 className="heading-panel">Employee Directory</h2>
          </div>
          <span className="bg-surface-2 text-text-dim px-2.5 py-0.5 rounded-full text-[12px] font-medium border border-border">
            Total: {filteredEmployees.length}
          </span>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead className="bg-surface-2">
              <tr>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">ID Number</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Department & Role</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Auth Types</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-soft">
              {filteredEmployees.length === 0 && (
                <tr>
                  <td colSpan="6" className="py-8 text-center text-text-dim text-[13px]">
                    {loading ? "Loading employees..." : "No employees found matching filter."}
                  </td>
                </tr>
              )}
              {paginatedEmployees.map((emp) => (
                <tr key={emp.id} className="hover:bg-surface-2/40 transition-colors group">
                  <td className="py-4 px-5">
                    <div className="flex items-center">
                      <div className="w-9 h-9 rounded-full bg-surface-2 border border-border flex items-center justify-center mr-3 text-[13px] font-bold text-text-dim">
                        {emp.firstName ? emp.firstName[0] : '?'}
                      </div>
                      <div className="text-[14px] font-medium text-text-primary">
                        {`${emp.firstName || ''} ${emp.lastName || ''}`.trim() || 'Unknown'}
                      </div>
                    </div>
                  </td>
                  <td className="py-4 px-5">
                    <div className="data-value text-[13px] text-text-dim">{emp.employeeNumber}</div>
                  </td>
                  <td className="py-4 px-5">
                    <div className="text-[13px] text-text-primary">{emp.department || '---'}</div>
                    <div className="text-[12px] text-text-faint mt-0.5">{emp.designation || '---'}</div>
                  </td>
                  <td className="py-4 px-5">
                    <div className="flex items-center space-x-2 text-text-faint">
                      {emp.hasFingerprint && <Fingerprint className="w-4 h-4 text-teal" title="Fingerprint Enrolled" />}
                      {emp.hasFace && <ScanFace className="w-4 h-4 text-teal" title="Face Enrolled" />}
                      {emp.hasCard && <CreditCard className="w-4 h-4 text-teal" title="RFID Enrolled" />}
                      {emp.hasPassword && <KeyRound className="w-4 h-4 text-teal" title="Password Enrolled" />}
                      {!emp.hasFingerprint && !emp.hasFace && !emp.hasCard && !emp.hasPassword && <span className="text-[11px]">None</span>}
                    </div>
                  </td>
                  <td className="py-4 px-5">
                    <StatusBadge status={emp.status} />
                  </td>
                  <td className="py-4 px-5">
                    <div className="flex items-center justify-end space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openModal(emp)} className="p-1.5 text-text-dim hover:text-teal hover:bg-teal/10 rounded-md transition-colors" title="Edit">
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDelete(emp.id)} className="p-1.5 text-text-dim hover:text-red hover:bg-red/10 rounded-md transition-colors" title="Delete">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
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
              Showing <span className="text-text-primary font-medium">{(currentPage - 1) * rowsPerPage + 1}</span> to <span className="text-text-primary font-medium">{Math.min(currentPage * rowsPerPage, filteredEmployees.length)}</span> of <span className="text-text-primary font-medium">{filteredEmployees.length}</span> employees
            </div>
            <div className="flex space-x-2">
              <button 
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="p-1.5 rounded-lg border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50 disabled:hover:bg-transparent"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button 
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="p-1.5 rounded-lg border border-border text-text-dim hover:text-text-primary hover:bg-surface-2 transition-colors disabled:opacity-50 disabled:hover:bg-transparent"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-[#0c1714]/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-xl w-full max-w-md shadow-2xl">
            <div className="flex justify-between items-center p-5 border-b border-border-soft">
              <h3 className="text-[16px] font-semibold text-text-primary">{editingEmp ? 'Edit Employee' : 'Add Employee'}</h3>
              <button onClick={() => setShowModal(false)} className="text-text-dim hover:text-text-primary transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-5 space-y-4">
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">First Name</label>
                <input type="text" value={formData.firstName} onChange={e => setFormData({...formData, firstName: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">Last Name</label>
                <input type="text" value={formData.lastName} onChange={e => setFormData({...formData, lastName: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">Employee/ID Number</label>
                <input type="text" value={formData.employeeNumber} onChange={e => setFormData({...formData, employeeNumber: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">Department</label>
                <input type="text" value={formData.department} onChange={e => setFormData({...formData, department: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">Designation</label>
                <input type="text" value={formData.designation} onChange={e => setFormData({...formData, designation: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
            </div>
            <div className="p-5 border-t border-border-soft flex justify-end space-x-3">
              <button onClick={() => setShowModal(false)} className="px-4 py-2 rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 transition-colors border border-transparent">
                Cancel
              </button>
              <button onClick={handleSave} className="px-4 py-2 rounded-lg text-[13px] font-semibold bg-teal text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm">
                Save Employee
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
