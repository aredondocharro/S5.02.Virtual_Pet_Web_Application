import { useEffect, useRef, useState } from 'react';
import { apiGet, apiPost } from '../api/client';
import { useParams } from 'react-router-dom';

type Pet = {
  id:number; name:string; color:string;
  hunger:number; stamina:number; happiness:number;
  level:number; xpInLevel:number; stage:string; ownerEmail:string; imageUrl?:string;
};

export default function PetDetail() {
  const { id } = useParams();
  const [pet, setPet] = useState<Pet | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const timerRef = useRef<number | null>(null);

  const load = async () => {
    try {
      const data = await apiGet<Pet>(`/api/pets/${id}`);
      setPet(data);
      setErr(null);
    } catch (e: any) {
      // opcional: muestra error al cargar
      setErr(e?.message ?? 'Error loading pet');
    }
  };

  useEffect(() => {
    // carga inicial
    load();

    // polling cada 15s SOLO cuando la pestaña está visible
    const startPolling = () => {
      if (timerRef.current != null) return;
      timerRef.current = window.setInterval(() => {
        if (document.visibilityState === 'visible') {
          load();
        }
      }, 5000);
    };

    const stopPolling = () => {
      if (timerRef.current != null) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };

    // Arranca polling y gestiona visibilidad de pestaña
    startPolling();
    const onVisibility = () => {
      if (document.visibilityState === 'hidden') stopPolling();
      else startPolling();
    };
    document.addEventListener('visibilitychange', onVisibility);

    // cleanup al cambiar id o desmontar
    return () => {
      document.removeEventListener('visibilitychange', onVisibility);
      stopPolling();
    };
  }, [id]);

  async function act(action: 'FEED'|'PLAY'|'TRAIN'|'REST') {
    try {
      await apiPost(`/api/pets/${id}/actions`, { action });
      await load(); // refresco inmediato tras la acción
    } catch (e: any) {
      // Muestra el mensaje del backend (por ejemplo, “está agotado”, “demasiada hambre para jugar”, etc.)
      const backendMsg =
        e?.response?.data?.message ||
        e?.response?.data?.error ||
        e?.message ||
        'Action failed';
      setErr(backendMsg);
    }
  }

  if (!pet) return <p style={{ padding:24 }}>Cargando...</p>;

  return (
    <div style={{ padding:24 }}>
      <h2>{pet.name}</h2>
      <p>Color: {pet.color}</p>
      <p>Hambre: {pet.hunger} | Energía: {pet.stamina} | Felicidad: {pet.happiness}</p>
      <p>Nivel: {pet.level} (XP {pet.xpInLevel}) | Etapa: {pet.stage}</p>

      {err && (
        <div style={{ color:'#b00020', marginBottom:12 }}>
          {err}
        </div>
      )}

      <div style={{ display:'flex', gap:8 }}>
        <button onClick={() => act('FEED')}>Alimentar</button>
        <button onClick={() => act('PLAY')}>Jugar</button>
        <button onClick={() => act('TRAIN')}>Entrenar</button>
        <button onClick={() => act('REST')}>Descansar</button>
      </div>
    </div>
  );
}

