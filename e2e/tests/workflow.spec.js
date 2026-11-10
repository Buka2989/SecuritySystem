import { test, expect } from '@playwright/test';

test('all existing pages: parent request, admin approval, real QR decoding, guard history, trusted login', async ({ browser, request }) => {
  expect(process.env.E2E_ALLOW_WRITES, 'Use only an isolated test deployment').toBe('1');
  const adminName=process.env.E2E_ADMIN_USERNAME;
  const adminPassword=process.env.E2E_ADMIN_PASSWORD;
  expect(adminName).toBeTruthy(); expect(adminPassword).toBeTruthy();
  async function api(method, path, token, data, expected=200) {
    const response=await request.fetch('/api'+path,{method,headers: token?{Authorization:'Bearer '+token}:{},data});
    expect(response.status(),await response.text()).toBe(expected);
    return expected===204?null:response.json();
  }
  const admin=(await api('POST','/auth/login',null,{username:adminName,password:adminPassword})).access_token;
  const suffix=Date.now().toString(); const password='E2e-only-'+suffix;
  const names={parent:'parent'+suffix,guard:'guard'+suffix,trusted:'trusted'+suffix};
  for(const [key,role] of [['parent','PARENT'],['guard','SECURITY'],['trusted','TRUSTED_PERSON']])
    await api('POST','/users/',admin,{username:names[key],full_name:'E2E '+key,password,role},201);
  const student=await api('POST','/students/',admin,{full_name:'E2E Child '+suffix,class_name:'5A'},201);
  const errors=[];
  const parentContext=await browser.newContext(); const page=await parentContext.newPage();
  page.on('pageerror',e=>errors.push(e.message));
  async function login(page,name,pw) {
    await page.goto('/login');await page.getByLabel('Имя пользователя').fill(name);await page.getByLabel('Пароль').fill(pw);
    await page.getByRole('button',{name:'Войти',exact:true}).click();
  }
  await login(page,names.parent,password);
  await expect(page).toHaveURL(/\/dashboard$/);
  await page.getByRole('link',{name:'+ Добавить ребёнка'}).click();
  await page.getByLabel('ФИО',{exact:true}).fill(student.full_name);await page.getByLabel('Класс',{exact:true}).fill('5A');
  await page.getByRole('button',{name:'Найти',exact:true}).click();
  await page.getByRole('button',{name:'Это мой ребёнок'}).click();
  await page.getByText('Отец',{exact:true}).click();await page.getByRole('button',{name:'Отправить заявку'}).click();
  await expect(page.getByText(student.full_name,{exact:true})).toBeVisible();
  const adminContext=await browser.newContext();const adminPage=await adminContext.newPage();
  adminPage.on('pageerror',e=>errors.push(e.message));
  await login(adminPage,adminName,adminPassword);await expect(adminPage).toHaveURL(/\/admin$/);
  const card=adminPage.locator('article').filter({hasText:student.full_name});
  await card.getByRole('button',{name:'Одобрить',exact:true}).click();
  await expect(card.getByRole('button',{name:'Одобрить',exact:true})).toHaveCount(0);
  await page.reload(); await page.getByText(student.full_name,{exact:true}).click();
  await expect(page).toHaveURL(/\/students\//);
  await page.getByRole('button',{name:'Сгенерировать QR',exact:true}).click();
  await expect(page.locator('.qr-panel__timer')).toContainText('Действителен ещё');
  const svg=await page.locator('.qr-panel__code svg').evaluate(el=>new XMLSerializer().serializeToString(el));
  const guardContext=await browser.newContext();
  // Feed actual QR pixels to html5-qrcode through a synthetic camera, not a mocked API/decoder.
  await guardContext.addInitScript(({svg})=>{
    navigator.mediaDevices.getUserMedia=async()=>{
      const canvas=document.createElement('canvas');canvas.width=640;canvas.height=480;
      const ctx=canvas.getContext('2d');const img=new Image();
      img.src='data:image/svg+xml;base64,'+btoa(svg);await img.decode();
      const draw=()=>{ctx.fillStyle='white';ctx.fillRect(0,0,640,480);ctx.drawImage(img,220,140,200,200);};
      draw();setInterval(draw,100);const stream=canvas.captureStream(10);
      for(const track of stream.getVideoTracks()) track.applyConstraints=async()=>{};
      return stream;
    };
    navigator.mediaDevices.enumerateDevices=async()=>[{deviceId:'test-camera',kind:'videoinput',label:'Test camera',groupId:'test'}];
  },{svg});
  const guardPage=await guardContext.newPage();guardPage.on('pageerror',e=>errors.push(e.message));
  await login(guardPage,names.guard,password);await expect(guardPage).toHaveURL(/\/security$/);
  await guardPage.getByRole('button',{name:'Начать сканирование'}).click();
  await expect(guardPage.getByText('ВХОД РАЗРЕШЁН',{exact:true})).toBeVisible({timeout:30000});
  await expect(guardPage.locator('.history-table-wrapper')).toContainText('VALID_QR');
  await guardPage.getByRole('button',{name:'Начать сканирование'}).click();
  await expect(guardPage.locator('.verification-result')).toContainText('QR_ALREADY_USED',{timeout:30000});
  const trustedContext=await browser.newContext();const trustedPage=await trustedContext.newPage();
  await login(trustedPage,names.trusted,password);await expect(trustedPage).toHaveURL(/\/dashboard$/);
  await trustedPage.getByRole('button',{name:'Выйти',exact:true}).click();await expect(trustedPage).toHaveURL(/\/login$/);
  await page.getByRole('link',{name:'Ко всем детям'}).click();
  page.once('dialog',d=>d.accept());await page.getByRole('button',{name:'Удалить '+student.full_name,exact:true}).click();
  await expect(page.getByText(student.full_name,{exact:true})).toHaveCount(0);
  expect(errors).toEqual([]);
  await Promise.all([parentContext.close(),adminContext.close(),guardContext.close(),trustedContext.close()]);
});
