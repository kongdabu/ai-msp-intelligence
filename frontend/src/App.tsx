import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Sidebar from './components/layout/Sidebar'
import Header from './components/layout/Header'
import Dashboard from './pages/Dashboard'
import Articles from './pages/Articles'
import Insights from './pages/Insights'
import Competitors from './pages/Competitors'
import Sources from './pages/Sources'

export default function App() {
  return (
    <BrowserRouter>
      <div className="flex min-h-screen bg-gray-50">
        <Sidebar />
        <div className="flex-1 flex flex-col min-w-0">
          <Header />
          <main className="flex-1 overflow-auto">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/articles" element={<Articles />} />
              <Route path="/insights" element={<Insights />} />
              <Route path="/competitors" element={<Competitors />} />
              <Route path="/sources" element={<Sources />} />
            </Routes>
          </main>
        </div>
      </div>
    </BrowserRouter>
  )
}
