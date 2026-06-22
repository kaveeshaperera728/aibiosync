import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopBar from './TopBar';

export default function Layout() {
  return (
    <div className="min-h-screen">
      <Sidebar />
      <div className="ml-[230px] min-h-screen flex flex-col relative z-10">
        <TopBar />
        <main className="flex-1 p-6 overflow-x-hidden">
          <div className="max-w-[1400px] mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
