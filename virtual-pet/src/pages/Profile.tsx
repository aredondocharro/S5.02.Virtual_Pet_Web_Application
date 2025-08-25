import { useEffect, useState } from 'react';
import { apiGet, apiPut } from '../api/client';
import { useNavigate } from 'react-router-dom';
import './Profile.css';

type UserProfile = {
  id: number;
  email: string;
  username: string;
  avatarUrl?: string;
  bio?: string;
  roles?: string[];
};

export default function Profile() {
  const nav = useNavigate();
  const [data, setData] = useState<UserProfile | null>(null);
  const [edit, setEdit] = useState(false);

  const [username, setUsername] = useState('');
  const [bio, setBio] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiGet<UserProfile>('/users/me').then(res => {
      setData(res);
      setUsername(res.username ?? '');
      setBio(res.bio ?? '');
      setAvatarUrl(res.avatarUrl ?? '');
    });
  }, []);

  async function save() {
    setSaving(true);
    try {
      const updated = await apiPut<UserProfile>('/users/me', { username, bio, avatarUrl });
      setData(updated);
      setEdit(false);
    } finally {
      setSaving(false);
    }
  }

  if (!data) return <p className="loading">Loading profile…</p>;

  return (
    <div className="profile-wrapper">
      {/* banner fijo servido desde /public/banners/banner.jpg */}
      <div
        className="banner"
        style={{ backgroundImage: 'url(/banners/banner.jpg)' }}
      />

      <div className="avatar-container">
        <img
          src={avatarUrl || data.avatarUrl || 'https://via.placeholder.com/120?text=Avatar'}
          alt="avatar"
          className="avatar"
        />
      </div>

      <div className="profile-card">
        {!edit ? (
          <>
            <h2>{data.username}</h2>
            <p className="email">{data.email}</p>
            <p className="bio">{data.bio?.trim() || <i>No bio</i>}</p>
            {data.roles?.length ? <p className="roles">Roles: {data.roles.join(', ')}</p> : null}

            <div className="buttons">
              <button onClick={() => setEdit(true)}>Edit profile</button>
              <button onClick={() => nav('/app')}>Back to main menu</button>
            </div>
          </>
        ) : (
          <form className="edit-form" onSubmit={(e) => { e.preventDefault(); save(); }}>
            <div className="form-field">
              <input
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Username"
              />
            </div>

            <div className="form-field">
              <input
                value={avatarUrl}
                onChange={e => setAvatarUrl(e.target.value)}
                placeholder="Avatar URL"
              />
            </div>

            <div className="form-field">
              <textarea
                value={bio}
                onChange={e => setBio(e.target.value)}
                placeholder="Write your bio…"
                rows={4}
              />
            </div>

            <div className="buttons">
              <button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
              <button type="button" onClick={() => setEdit(false)}>Cancel</button>
              <button type="button" onClick={() => nav('/app')}>Back to main menu</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}




