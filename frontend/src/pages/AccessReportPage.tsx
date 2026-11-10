import { useEffect,useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { apiRequest,API_BASE_URL } from '@/services/api';
import { responseError } from '@/services/api/client';
interface Summary {total:number;allowed:number;denied:number;denial_reasons:Record<string,number>}
export function AccessReportPage(){
  const {token}=useAuth();const [from,setFrom]=useState('');const [to,setTo]=useState('');
  const [format,setFormat]=useState('csv');const [redact,setRedact]=useState(true);
  const [summary,setSummary]=useState<Summary|null>(null);const [error,setError]=useState('');const [busy,setBusy]=useState(false);
  function params(){const p=new URLSearchParams();if(from)p.set('from',from);if(to)p.set('to',to);return p;}
  async function refresh(){setError('');try{setSummary(await apiRequest<Summary>(`/reports/access?${params()}`,{token}));}catch(e){setSummary(null);setError(e instanceof Error?e.message:'Ошибка отчёта');}}
  useEffect(()=>{void refresh();},[token]);
  async function download(){setBusy(true);setError('');try{
    const p=params();p.set('format',format);p.set('redact',String(redact));
    const response=await fetch(`${API_BASE_URL}/reports/access/export?${p}`,{headers:{Authorization:`Bearer ${token}`}});
    if(!response.ok)throw await responseError(response);
    const url=URL.createObjectURL(await response.blob());const a=document.createElement('a');a.href=url;a.download=`access-report.${format}`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
  }catch(e){setError(e instanceof Error?e.message:'Ошибка экспорта');}finally{setBusy(false);}}
  return <main className="page"><h1 className="page__title">Отчёт о доступе</h1><section className="detail-card">
    <div className="field"><label htmlFor="report-from">Начало периода</label><input id="report-from" type="date" value={from} onChange={e=>setFrom(e.target.value)}/></div>
    <div className="field"><label htmlFor="report-to">Конец периода</label><input id="report-to" type="date" value={to} onChange={e=>setTo(e.target.value)}/></div>
    <button className="btn-secondary" onClick={()=>void refresh()}>Обновить отчёт</button>
    {summary&&<div aria-live="polite"><p>Всего: {summary.total} · Разрешено: {summary.allowed} · Отказано: {summary.denied}</p><ul>{Object.entries(summary.denial_reasons).map(([reason,count])=><li key={reason}>{reason}: {count}</li>)}</ul></div>}
    <div className="field"><label htmlFor="report-format">Формат файла</label><select id="report-format" value={format} onChange={e=>setFormat(e.target.value)}><option value="csv">CSV</option><option value="json">JSON</option></select></div>
    <label><input type="checkbox" checked={redact} onChange={e=>setRedact(e.target.checked)}/> Скрыть имена и идентификаторы</label>
    <p><button className="btn-primary" disabled={busy} onClick={()=>void download()}>Скачать отчёт</button></p>
    {error&&<p role="alert" className="form-error">{error}</p>}
  </section></main>;
}
