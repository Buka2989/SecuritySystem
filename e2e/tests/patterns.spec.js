import { test,expect } from '@playwright/test';
import fs from 'node:fs/promises';

test('student editing, selectable search, review notification and export options',async({browser,request})=>{
  expect(process.env.E2E_ALLOW_WRITES).toBe('1');
  const suffix=Date.now().toString();const password='Pattern-test-'+suffix;
  async function api(method,path,token,data,status=200){
    const r=await request.fetch('/api'+path,{method,data,headers:token?{Authorization:'Bearer '+token}:{}});
    expect(r.status(),await r.text()).toBe(status);return r.json();
  }
  const adminToken=(await api('POST','/auth/login',null,{username:process.env.E2E_ADMIN_USERNAME,password:process.env.E2E_ADMIN_PASSWORD})).access_token;
  const parentName='patterns_parent_'+suffix;
  await api('POST','/users/',adminToken,{username:parentName,password,role:'PARENT',full_name:'Demo Guardian'},201);
  async function login(page,name,pw){await page.goto('/login');await page.getByLabel('Имя пользователя').fill(name);await page.getByLabel('Пароль').fill(pw);await page.getByRole('button',{name:'Войти',exact:true}).click();}
  async function capture(page,name){if(process.env.CAPTURE_REPORT==='1'){await fs.mkdir('../docs/screenshots',{recursive:true});await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:`../docs/screenshots/${name}.png`,fullPage:false});}}
  const adminContext=await browser.newContext({viewport:{width:1280,height:900}});const admin=await adminContext.newPage();
  await login(admin,process.env.E2E_ADMIN_USERNAME,process.env.E2E_ADMIN_PASSWORD);
  await admin.getByRole('link',{name:'Ученики',exact:true}).click();
  const studentName='Mira '+suffix;
  await admin.getByLabel('ФИО ученика').fill(studentName);await admin.getByLabel('Класс ученика').fill('7A');
  await admin.getByRole('button',{name:'Сохранить ученика',exact:true}).click();
  const card=admin.locator('article').filter({hasText:studentName});await expect(card).toBeVisible();
  await card.getByRole('button',{name:'Редактировать',exact:true}).click();await admin.getByLabel('Класс ученика').fill('7B');
  await admin.getByRole('button',{name:'Сохранить ученика',exact:true}).click();await expect(card).toContainText('7B');
  await capture(admin,'student-management');
  await api('POST','/students/',adminToken,{full_name:'Family '+studentName,class_name:'7B'},201);
  const parentContext=await browser.newContext({viewport:{width:1280,height:900}});const parent=await parentContext.newPage();
  await login(parent,parentName,password);await parent.getByRole('link',{name:'+ Добавить ребёнка'}).click();
  await parent.getByLabel('ФИО',{exact:true}).fill(studentName);await parent.getByLabel('Класс',{exact:true}).fill('7B');
  await parent.getByRole('button',{name:'Найти',exact:true}).click();await expect(parent.getByRole('button',{name:'Это мой ребёнок'})).toHaveCount(2);
  await parent.getByLabel('Режим поиска').selectOption('prefix');await parent.getByRole('button',{name:'Найти',exact:true}).click();
  await expect(parent.getByRole('button',{name:'Это мой ребёнок'})).toHaveCount(1);await capture(parent,'search-strategies');
  await parent.getByRole('button',{name:'Это мой ребёнок'}).click();await parent.getByText('Мать',{exact:true}).click();
  await parent.getByRole('button',{name:'Отправить заявку'}).click();await expect(parent.getByText(studentName,{exact:true})).toBeVisible();
  await admin.getByRole('link',{name:'Заявки',exact:true}).click();
  await admin.locator('article').filter({hasText:studentName}).getByRole('button',{name:'Одобрить',exact:true}).click();
  await expect(parent.getByRole('status')).toContainText('подтверждена',{timeout:12000});
  await expect(parent.locator('.student-card .status-badge')).toContainText('Подтверждено');await capture(parent,'review-notification');
  await parent.getByText(studentName,{exact:true}).click();await parent.getByLabel('Срок действия QR').selectOption('5');
  await parent.getByRole('button',{name:'Сгенерировать QR',exact:true}).click();await expect(parent.locator('.qr-panel__timer')).toContainText('Действителен ещё');
  await capture(parent,'configured-qr');
  // Populate the report even when this suite runs first against an empty CI deployment.
  const parentToken=(await api('POST','/auth/login',null,{username:parentName,password})).access_token;
  const codes=await api('GET','/qr-codes/',parentToken);
  const guardName='patterns_guard_'+suffix;
  await api('POST','/users/',adminToken,{username:guardName,password,role:'SECURITY',full_name:'Pattern Guard'},201);
  const guardToken=(await api('POST','/auth/login',null,{username:guardName,password})).access_token;
  expect((await api('POST','/qr-codes/verify',guardToken,{token:codes[0].token})).result).toBe('ALLOWED');
  expect((await api('POST','/qr-codes/verify',guardToken,{token:codes[0].token})).reason).toBe('QR_ALREADY_USED');
  await admin.getByRole('link',{name:'Отчёт о доступе',exact:true}).click();await expect(admin.getByText(/Всего:/)).toBeVisible();
  await admin.getByLabel('Формат файла').selectOption('json');
  const downloadPromise=admin.waitForEvent('download');await admin.getByRole('button',{name:'Скачать отчёт'}).click();
  const download=await downloadPromise;expect(download.suggestedFilename()).toBe('access-report.json');
  const downloadPath=await download.path();const entries=JSON.parse(await fs.readFile(downloadPath,'utf8'));
  expect(entries.length).toBeGreaterThanOrEqual(2);
  for(const row of entries){expect(row.guardian).toBe('Hidden');expect(row.id).toBe('');}
  await admin.getByLabel('Формат файла').selectOption('csv');
  const csvPromise=admin.waitForEvent('download');await admin.getByRole('button',{name:'Скачать отчёт'}).click();expect((await csvPromise).suggestedFilename()).toBe('access-report.csv');
  await capture(admin,'access-report');
  await admin.getByLabel('Начало периода').fill('2030-12-01');await admin.getByLabel('Конец периода').fill('2020-01-01');
  await admin.getByRole('button',{name:'Обновить отчёт'}).click();await expect(admin.getByRole('alert')).toContainText('Start date');
  await expect(admin.getByRole('alert')).not.toContainText('"detail"');
  await admin.getByRole('button',{name:'Скачать отчёт'}).click();await expect(admin.getByRole('alert')).toContainText('Start date');
  await expect(admin.getByRole('alert')).not.toContainText('"detail"');
  await adminContext.close();await parentContext.close();
});
