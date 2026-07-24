import {useCallback, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {BASE_URL} from '../api';

type ViewState = 'landing' | 'login' | 'register';

type UseAuthReturn = {
  view: ViewState;
  setView: (view: ViewState) => void;
  email: string;
  setEmail: (email: string) => void;
  password: string;
  setPassword: (password: string) => void;
  firstName: string;
  setFirstName: (firstName: string) => void;
  lastName: string;
  setLastName: (lastName: string) => void;
  error: string | null;
  isLoading: boolean;
  handleAuth: (e: React.FormEvent) => Promise<void>;
};

export function useAuth(): UseAuthReturn {
  const [view, setView] = useState<ViewState>('landing');
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleAuth = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const endpoint = view === 'login' ? '/api/auth/login' : '/api/auth/register';
    const payload = view === 'login'
      ? {email, password}
      : {email, password, firstName, lastName};

    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        navigate('/app/overview');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Authentication failed');
      }
    } catch {
      setError('Network error. Please check if the server is running.');
    } finally {
      setIsLoading(false);
    }
  }, [view, email, password, firstName, lastName, navigate]);

  return {
    view,
    setView,
    email,
    setEmail,
    password,
    setPassword,
    firstName,
    setFirstName,
    lastName,
    setLastName,
    error,
    isLoading,
    handleAuth,
  };
}
