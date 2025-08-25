import { useEffect, useState } from 'react';
import { apiDelete, apiGet } from '../api/client';
import { Link } from 'react-router-dom';

type Pet = { id:number; name:string; color:string; level:number; stage:string };

export default function Sanctuary() {
  const [pets, setPets] = useState<Pet[]>([]);
  const load = () => apiGet<Pet[]>('/api/pets').then(setPets);

  useEffect(() => { load(); }, []);

  async function remove(id:number) {
    if (!confirm('¿Eliminar mascota?')) return;
    await apiDelete(`/api/pets/${id}`);
    load();
  }

  return (
    <div style={{ padding:24 }}>
      <h2>Mi santuario</h2>
      <ul>
        {pets.map(p => (
          <li key={p.id}>
            <Link to={`/app/pets/${p.id}`}>{p.name}</Link> — {p.color} — lvl {p.level} ({p.stage})
            <button onClick={() => remove(p.id)} style={{ marginLeft:8 }}>Eliminar</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
