import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Sidebar from './components/layout/Sidebar'
import Header from './components/layout/Header'
import Toaster from './components/common/Toaster'
import Dashboard from './pages/Dashboard'
import Articles from './pages/Articles'
import SavedArticles from './pages/SavedArticles'
import Insights from './pages/Insights'
import Saved from './pages/Saved'
import Settings from './pages/Settings'
import Trends from './pages/Trends'
import Radar from './pages/Radar'
import WatchListSettings from './pages/WatchListSettings'
import StrategyReports from './pages/StrategyReports'

export default function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <BrowserRouter>
      <div className="flex min-h-screen bg-gray-50">
        {sidebarOpen && (
          <div
            className="fixed inset-0 bg-black/50 z-20 md:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}
        <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
        <div className="flex-1 flex flex-col min-w-0">
          <Header onMenuClick={() => setSidebarOpen(true)} />
          <main className="flex-1 overflow-auto">
            <Routes>
              <Route path="/" element={<Radar />} />
              <Route path="/strategy-reports" element={<StrategyReports />} />
              <Route path="/legacy/dashboard" element={<Dashboard />} />
              <Route path="/radar" element={<Radar />} />
              <Route path="/articles" element={<Articles />} />
              <Route path="/saved-articles" element={<SavedArticles />} />
              <Route path="/insights" element={<Insights />} />
              <Route path="/saved" element={<Saved />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="/settings/watch-list" element={<WatchListSettings />} />
              <Route path="/trends" element={<Trends />} />
            </Routes>
          </main>
        </div>
      </div>
      <Toaster />
    </BrowserRouter>
  )
}
