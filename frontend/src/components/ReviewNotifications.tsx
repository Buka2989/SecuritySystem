import { useEffect,useRef,useState } from 'react';
import { apiRequest } from '@/services/api';
import { useAuth } from '@/hooks/useAuth';
interface Notice {id:string;sequence:number;relationship_id:string;status:string;comment:string|null}
export function ReviewNotifications(){
  const {token,user}=useAuth();const seen=useRef(new Set<string>());const [notice,setNotice]=useState<Notice|null>(null);
  useEffect(()=>{
    seen.current.clear();setNotice(null);
    if(!token || !user || !['PARENT','TRUSTED_PERSON'].includes(user.role))return;
    let stopped=false;
    async function poll(){try{const items=await apiRequest<Notice[]>('/notifications',{token});if(stopped)return;
      const fresh=items.filter(n=>!seen.current.has(n.id));seen.current=new Set(items.map(n=>n.id));
      if(fresh.length){setNotice(fresh[fresh.length-1]);window.dispatchEvent(new Event('verification-updated'));}
    }catch{/* Domain state is still loaded normally if notification delivery is unavailable. */}}
    void poll();const timer=window.setInterval(()=>void poll(),3000);return()=>{stopped=true;window.clearInterval(timer);};
  },[token,user?.id]);
  if(!notice)return null;
  return <div className="notice" role="status">{notice.status==='APPROVED'?'Связь с учеником подтверждена.':'Заявка отклонена.'} {notice.comment}<button className="btn-secondary" onClick={()=>setNotice(null)}>Закрыть уведомление</button></div>;
}
