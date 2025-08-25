import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import Profile from './pages/Profile';
import PetNew from './pages/PetNew';
import Sanctuary from './pages/Sanctuary';
import PetDetail from './pages/PetDetail';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* públicas */}
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* privadas */}
          <Route path="/app" element={<PrivateRoute><Home /></PrivateRoute>} />
          <Route path="/app/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
          <Route path="/app/pets/new" element={<PrivateRoute><PetNew /></PrivateRoute>} />
          <Route path="/app/sanctuary" element={<PrivateRoute><Sanctuary /></PrivateRoute>} />
          <Route path="/app/pets/:id" element={<PrivateRoute><PetDetail /></PrivateRoute>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
