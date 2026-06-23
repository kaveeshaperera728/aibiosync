import React, { useState, useEffect } from 'react';
import { Fingerprint, Plus, MoreVertical, Server, Trash2, Edit2, RefreshCw, Users, X, Send, Clock, CheckCheck } from 'lucide-react';
import { getDevices, addDevice, deleteDevice, getMissingUsers, provisionUsers, syncDeviceTime } from '../services/api';
import api from '../services/api';

export default function Devices() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);

  // Device Modal
  const [showDeviceModal, setShowDeviceModal] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [deviceForm, setDeviceForm] = useState({ name: '', serialNumber: '', ipAddress: '', port: 8081, location: '' });

  // Users Modal
  const [showUsersModal, setShowUsersModal] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [missingUsers, setMissingUsers] = useState([]);
  const [selectedUserIds, setSelectedUserIds] = useState(new Set());
  const [provisioning, setProvisioning] = useState(false);

  const loadDevices = async () => {
    try {
      setLoading(true);
      const res = await getDevices();
      setDevices(res.data);
    } catch (error) {
      console.error("Failed to load devices", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDevices();
  }, []);

  const openDeviceModal = (dev = null) => {
    if (dev) {
      setEditingDevice(dev);
      setDeviceForm({ ...dev });
    } else {
      setEditingDevice(null);
      setDeviceForm({ name: '', serialNumber: '', ipAddress: '', port: 8081, location: '' });
    }
    setShowDeviceModal(true);
  };

  const handleSaveDevice = async () => {
    try {
      if (editingDevice) {
        await api.put(`/devices/${editingDevice.id}`, deviceForm);
      } else {
        await addDevice(deviceForm);
      }
      setShowDeviceModal(false);
      loadDevices();
    } catch (e) {
      console.error('Failed to save device', e);
    }
  };

  const handleDeleteDevice = async (id) => {
    if (window.confirm("Are you sure you want to delete this device?")) {
      try {
        await deleteDevice(id);
        loadDevices();
      } catch (e) {
        console.error("Failed to delete", e);
      }
    }
  };

  const openUsersModal = async (dev) => {
    setSelectedDevice(dev);
    setShowUsersModal(true);
    setMissingUsers([]);
    setSelectedUserIds(new Set());
    
    try {
      const res = await getMissingUsers(dev.id);
      setMissingUsers(res.data);
    } catch (e) {
      console.error("Failed to fetch missing users", e);
    }
  };

  const toggleUserSelection = (id) => {
    const newSet = new Set(selectedUserIds);
    if (newSet.has(id)) newSet.delete(id);
    else newSet.add(id);
    setSelectedUserIds(newSet);
  };

  const selectAllMissing = () => {
    if (selectedUserIds.size === missingUsers.length) {
      setSelectedUserIds(new Set());
    } else {
      setSelectedUserIds(new Set(missingUsers.map(u => u.id)));
    }
  };

  const [syncingTime, setSyncingTime] = useState(null);

  const handleSyncTime = async (dev) => {
    try {
      setSyncingTime(dev.id);
      const res = await syncDeviceTime(dev.id);
      alert(`✅ ${res.data.message || 'Time sync sent successfully!'}`);
    } catch (e) {
      const errMsg = e.response?.data?.error || e.response?.data || e.message || 'Unknown error';
      alert(`❌ Time sync failed: ${errMsg}`);
      console.error('Time sync error:', e.response?.data || e);
    } finally {
      setSyncingTime(null);
    }
  };

  const handleProvision = async () => {
    if (selectedUserIds.size === 0) return;
    try {
      setProvisioning(true);
      await provisionUsers(selectedDevice.id, Array.from(selectedUserIds));
      setShowUsersModal(false);
      alert(`Provisioning ${selectedUserIds.size} users to device. They will be pushed momentarily.`);
    } catch (e) {
      console.error('Failed to provision users', e);
    } finally {
      setProvisioning(false);
    }
  };

  const StatusBadge = ({ status }) => {
    const isOnline = status === 'online';
    return (
      <div className={`inline-flex items-center px-2 py-0.5 rounded-full border border-transparent ${isOnline ? 'bg-teal/10 text-teal' : 'bg-red/10 text-red'}`}>
        <div className="relative flex items-center justify-center w-2 h-2 mr-1.5">
          {isOnline && <div className="absolute w-full h-full rounded-full opacity-75 animate-radar-ping bg-teal" />}
          <div className={`relative w-1.5 h-1.5 rounded-full ${isOnline ? 'bg-teal' : 'bg-red'}`} />
        </div>
        <span className="text-[11px] font-medium tracking-wide uppercase">{status}</span>
      </div>
    );
  };

  return (
    <div className="space-y-6 pb-12 relative">
      {/* Action Bar */}
      <div className="flex justify-between items-center">
        <div>
          <p className="text-text-dim text-[13px]">Manage and monitor all connected biometric devices.</p>
        </div>
        <div className="flex space-x-3">
          <button onClick={loadDevices} className="flex items-center px-3 py-[7px] border border-border rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 hover:border-text-faint transition-colors">
            <RefreshCw className={`w-[14px] h-[14px] mr-2 text-text-dim ${loading ? 'animate-spin' : ''}`} />
            Sync All
          </button>
          <button onClick={() => openDeviceModal()} className="flex items-center px-4 py-[7px] bg-teal rounded-lg text-[13px] font-semibold text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm">
            <Plus className="w-[16px] h-[16px] mr-1" strokeWidth={2.5} />
            Add Device
          </button>
        </div>
      </div>

      {/* Main Table Panel */}
      <div className="panel flex flex-col">
        <div className="p-5 border-b border-border-soft flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <Fingerprint className="w-5 h-5 text-teal" />
            <h2 className="heading-panel">Registered Devices</h2>
          </div>
          <span className="bg-surface-2 text-text-dim px-2.5 py-0.5 rounded-full text-[12px] font-medium border border-border">
            Total: {devices.length}
          </span>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead className="bg-surface-2">
              <tr>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft w-10"></th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Device Name</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">IP & Port</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Hardware</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Capacity</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Status</th>
                <th className="py-3 px-5 label-caps font-medium border-b border-border-soft text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-soft">
              {devices.length === 0 && (
                <tr>
                  <td colSpan="8" className="py-8 text-center text-text-dim text-[13px]">
                    {loading ? "Loading devices..." : "No devices registered yet."}
                  </td>
                </tr>
              )}
              {devices.map((dev) => (
                <tr key={dev.id} className="hover:bg-surface-2/40 transition-colors group">
                  <td className="py-4 px-5">
                    <div className="w-8 h-8 rounded-md bg-surface border border-border flex items-center justify-center">
                      <Server className="w-4 h-4 text-text-dim" />
                    </div>
                  </td>
                  <td className="py-4 px-5">
                    <div className="text-[14px] font-medium text-text-primary">{dev.name || 'Unnamed Device'}</div>
                    <div className="data-value text-[11px] text-text-faint mt-0.5">{dev.serialNumber}</div>
                  </td>
                  <td className="py-4 px-5">
                    <div className="data-value text-[13px] text-text-primary">{dev.ipAddress || '---.---.---.---'}</div>
                    <div className="data-value text-[11px] text-text-faint mt-0.5">Port: {dev.port || 8081}</div>
                  </td>
                  <td className="py-4 px-5 text-[13px] text-text-primary">
                    <div>{dev.deviceType || 'Unknown'}</div>
                    <div className="text-[11px] text-text-faint mt-0.5">FW: {dev.firmwareVersion || '---'}</div>
                    <div className="text-[11px] text-text-faint">MAC: {dev.macAddress || '---'}</div>
                  </td>
                  <td className="py-4 px-5 text-[12px] text-text-dim">
                    <div className="flex flex-col space-y-0.5">
                      <div className="flex justify-between w-24"><span>Users:</span> <span className="font-medium text-text-primary">{dev.userCount || 0}</span></div>
                      <div className="flex justify-between w-24"><span>Fingers:</span> <span className="font-medium text-text-primary">{dev.fingerprintCount || 0}</span></div>
                    </div>
                  </td>
                  <td className="py-4 px-5">
                    <StatusBadge status={dev.status || 'offline'} />
                  </td>
                  <td className="py-4 px-5">
                    <div className="flex items-center justify-end space-x-2">
                      <button
                        onClick={() => handleSyncTime(dev)}
                        className="px-2.5 py-1.5 flex items-center bg-surface-2 border border-border rounded-md text-[12px] font-medium text-text-primary hover:border-amber hover:text-amber transition-colors"
                        title="Sync Device Clock"
                      >
                        <Clock className={`w-3.5 h-3.5 mr-1.5 ${syncingTime === dev.id ? 'animate-spin' : ''}`} />
                        Sync Time
                      </button>
                      <button onClick={() => openUsersModal(dev)} className="px-2.5 py-1.5 flex items-center bg-surface-2 border border-border rounded-md text-[12px] font-medium text-text-primary hover:border-teal hover:text-teal transition-colors" title="Manage Users">
                        <Users className="w-3.5 h-3.5 mr-1.5" />
                        Users
                      </button>
                      <button onClick={() => openDeviceModal(dev)} className="p-1.5 text-text-dim hover:text-teal hover:bg-teal/10 rounded-md transition-colors" title="Edit">
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDeleteDevice(dev.id)} className="p-1.5 text-text-dim hover:text-red hover:bg-red/10 rounded-md transition-colors" title="Delete">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add/Edit Device Modal */}
      {showDeviceModal && (
        <div className="fixed inset-0 bg-[#0c1714]/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-xl w-full max-w-md shadow-2xl">
            <div className="flex justify-between items-center p-5 border-b border-border-soft">
              <h3 className="text-[16px] font-semibold text-text-primary">{editingDevice ? 'Edit Device' : 'Add Device'}</h3>
              <button onClick={() => setShowDeviceModal(false)} className="text-text-dim hover:text-text-primary transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-5 space-y-4">
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">Device Name</label>
                <input type="text" value={deviceForm.name} onChange={e => setDeviceForm({...deviceForm, name: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
              <div>
                <label className="block text-[12px] font-medium text-text-dim mb-1.5">Serial Number</label>
                <input type="text" value={deviceForm.serialNumber} onChange={e => setDeviceForm({...deviceForm, serialNumber: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[12px] font-medium text-text-dim mb-1.5">IP Address</label>
                  <input type="text" value={deviceForm.ipAddress} onChange={e => setDeviceForm({...deviceForm, ipAddress: e.target.value})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
                </div>
                <div>
                  <label className="block text-[12px] font-medium text-text-dim mb-1.5">Port</label>
                  <input type="number" value={deviceForm.port} onChange={e => setDeviceForm({...deviceForm, port: parseInt(e.target.value)})} className="w-full bg-surface-2 border border-border rounded-lg px-3 py-2 text-[13px] text-text-primary focus:outline-none focus:border-teal" />
                </div>
              </div>
            </div>
            <div className="p-5 border-t border-border-soft flex justify-end space-x-3">
              <button onClick={() => setShowDeviceModal(false)} className="px-4 py-2 rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 transition-colors border border-transparent">
                Cancel
              </button>
              <button onClick={handleSaveDevice} className="px-4 py-2 rounded-lg text-[13px] font-semibold bg-teal text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm">
                Save Device
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Provision Missing Users Modal */}
      {showUsersModal && (
        <div className="fixed inset-0 bg-[#0c1714]/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-xl w-full max-w-2xl shadow-2xl flex flex-col max-h-[85vh]">
            <div className="flex justify-between items-center p-5 border-b border-border-soft shrink-0">
              <div>
                <h3 className="text-[16px] font-semibold text-text-primary">Provision Missing Users</h3>
                <p className="text-[12px] text-text-dim mt-0.5">Device: <span className="text-text-primary font-medium">{selectedDevice?.name}</span></p>
              </div>
              <button onClick={() => setShowUsersModal(false)} className="text-text-dim hover:text-text-primary transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="flex-1 overflow-auto p-0">
              <table className="w-full text-left border-collapse">
                <thead className="bg-surface-2 sticky top-0 z-10 shadow-sm">
                  <tr>
                    <th className="py-3 px-5 border-b border-border-soft w-12">
                      <input type="checkbox" checked={missingUsers.length > 0 && selectedUserIds.size === missingUsers.length} onChange={selectAllMissing} className="rounded border-border bg-surface-2 text-teal focus:ring-teal" />
                    </th>
                    <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">Employee</th>
                    <th className="py-3 px-5 label-caps font-medium border-b border-border-soft">ID Number</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-soft">
                  {missingUsers.length === 0 ? (
                    <tr>
                      <td colSpan="3" className="py-12 text-center text-text-dim text-[13px]">
                        {missingUsers ? "All users are currently synchronized to this device." : "Loading..."}
                      </td>
                    </tr>
                  ) : missingUsers.map((u) => (
                    <tr key={u.id} className="hover:bg-surface-2/40 cursor-pointer" onClick={() => toggleUserSelection(u.id)}>
                      <td className="py-3 px-5">
                        <input type="checkbox" checked={selectedUserIds.has(u.id)} onChange={() => {}} className="rounded border-border bg-surface-2 text-teal focus:ring-teal" />
                      </td>
                      <td className="py-3 px-5">
                        <div className="text-[13px] font-medium text-text-primary">{`${u.firstName || ''} ${u.lastName || ''}`}</div>
                      </td>
                      <td className="py-3 px-5">
                        <div className="data-value text-[12px] text-text-dim">{u.employeeNumber}</div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="p-5 border-t border-border-soft flex justify-between items-center shrink-0 bg-surface">
              <div className="flex items-center gap-4">
                <div className="text-[13px] text-text-dim">
                  <span className="font-medium text-text-primary">{selectedUserIds.size}</span> of{' '}
                  <span className="font-medium text-text-primary">{missingUsers.length}</span> selected
                </div>
                {missingUsers.length > 0 && selectedUserIds.size < missingUsers.length && (
                  <button
                    onClick={selectAllMissing}
                    className="flex items-center px-3 py-1.5 rounded-lg text-[12px] font-medium border border-teal/40 text-teal hover:bg-teal/10 transition-colors"
                  >
                    <CheckCheck className="w-3.5 h-3.5 mr-1.5" />
                    Select All {missingUsers.length}
                  </button>
                )}
              </div>
              <div className="flex space-x-3">
                <button onClick={() => setShowUsersModal(false)} className="px-4 py-2 rounded-lg text-[13px] font-medium text-text-primary hover:bg-surface-2 transition-colors border border-transparent">
                  Cancel
                </button>
                <button
                  onClick={handleProvision}
                  disabled={selectedUserIds.size === 0 || provisioning}
                  className="flex items-center px-4 py-2 rounded-lg text-[13px] font-semibold bg-teal text-[#0c1714] hover:bg-[#2ebfae] transition-colors shadow-sm disabled:opacity-50 disabled:hover:bg-teal"
                >
                  <Send className={`w-4 h-4 mr-2 ${provisioning ? 'animate-pulse' : ''}`} />
                  {provisioning ? 'Syncing...' : `Sync ${selectedUserIds.size > 0 ? selectedUserIds.size : ''} Users`}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
