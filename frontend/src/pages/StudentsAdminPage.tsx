import { useEffect, useState, type FormEvent } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { apiRequest } from '@/services/api';
interface StudentRow { id: string; full_name: string; class_name: string; status: string }
export function StudentsAdminPage() {
  const { token } = useAuth();
  const [rows,setRows]=useState<StudentRow[]>([]);
  const [editing,setEditing]=useState<string|null>(null);
  const [name,setName]=useState(''); const [className,setClassName]=useState('');const [status,setStatus]=useState('ACTIVE');
  const [error,setError]=useState('');const [busy,setBusy]=useState(false);
  async function load(){setRows(await apiRequest<StudentRow[]>('/students/',{token}));}
  useEffect(()=>{void load().catch(()=>setError('Не удалось загрузить учеников'));},[token]);
  function clear(){setEditing(null);setName('');setClassName('');setStatus('ACTIVE');}
  async function save(e:FormEvent){e.preventDefault();setError('');setBusy(true);
    try{await apiRequest(editing?`/students/${editing}`:'/students/',{token,method:editing?'PATCH':'POST',body:{full_name:name.trim(),class_name:className.trim(),status}});await load();clear();}
    catch(e){setError(e instanceof Error?e.message:'Ошибка сохранения');}finally{setBusy(false);}
  }
  return <main className="page"><h1 className="page__title">Ученики</h1>
    <form onSubmit={save} className="detail-card">
      <h2>{editing?'Редактировать ученика':'Добавить ученика'}</h2>
      <div className="field"><label htmlFor="student-name">ФИО ученика</label><input id="student-name" required maxLength={150} value={name} onChange={e=>setName(e.target.value)}/></div>
      <div className="field"><label htmlFor="student-class">Класс ученика</label><input id="student-class" required maxLength={20} value={className} onChange={e=>setClassName(e.target.value)}/></div>
      <div className="field"><label htmlFor="student-status">Статус ученика</label><select id="student-status" value={status} onChange={e=>setStatus(e.target.value)}><option value="ACTIVE">Активен</option><option value="INACTIVE">Неактивен</option></select></div>
      {error&&<p role="alert" className="form-error">{error}</p>}
      <button className="btn-primary" disabled={busy}>Сохранить ученика</button>{editing&&<button type="button" className="btn-secondary" onClick={clear}>Отменить редактирование</button>}
    </form>
    <section className="student-list">{rows.map(s=><article key={s.id} className="detail-card"><h2>{s.full_name}</h2><p>{s.class_name} · {s.status}</p><button className="btn-secondary" onClick={()=>{setEditing(s.id);setName(s.full_name);setClassName(s.class_name);setStatus(s.status);}}>Редактировать</button></article>)}</section>
  </main>;
}
