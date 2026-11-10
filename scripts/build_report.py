"""Build the English course report and vector UML figures with ReportLab.

Usage: python scripts/build_report.py
Dependencies: reportlab, Pillow. Uses repository source and verification evidence.
"""
from pathlib import Path
from html import escape
import json
import textwrap

from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.utils import ImageReader
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table,
    TableStyle, PageBreak, Image, Preformatted, KeepTogether)
from reportlab.graphics.shapes import Drawing, Rect, Line, String, Polygon
from reportlab.graphics import renderSVG, renderPDF
from reportlab.pdfbase.pdfmetrics import stringWidth

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'output' / 'pdf'
UML = ROOT / 'docs' / 'uml'
INK = colors.HexColor('#173454')
MUTED = colors.HexColor('#526174')
PALE = colors.HexColor('#edf3f8')
WIDTH = 483
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='ReportTitle', fontName='Helvetica-Bold', fontSize=30,
    leading=35, textColor=INK, spaceAfter=18))
styles.add(ParagraphStyle(name='Subtitle', fontSize=13, leading=19, textColor=MUTED, spaceAfter=15))
styles.add(ParagraphStyle(name='Body', fontSize=10, leading=14, spaceAfter=9))
styles.add(ParagraphStyle(name='SmallText', fontSize=8, leading=11, spaceAfter=5))
styles.add(ParagraphStyle(name='Cell', fontSize=8, leading=11))
styles.add(ParagraphStyle(name='ReportCode', fontName='Courier', fontSize=8, leading=11,
    textColor=INK, backColor=PALE, borderPadding=8, spaceAfter=10))
styles.add(ParagraphStyle(name='WhiteCell', parent=styles['Cell'], textColor=colors.white,
    fontName='Helvetica-Bold'))
styles['Heading1'].textColor=INK
styles['Heading1'].fontSize=19
styles['Heading1'].leading=24
styles['Heading1'].spaceAfter=14
styles['Heading2'].textColor=INK
styles['Heading2'].fontSize=12
styles['Heading2'].spaceBefore=10
story=[]

def p(text, style='Body'):
    return Paragraph(text, styles[style])

def para(text, style='Body'):
    story.append(p(text, style))

def heading(text):
    story.append(p(text, 'Heading1'))

def section(text):
    story.append(p(text, 'Heading2'))

def page(title):
    if story:
        story.append(PageBreak())
    heading(title)

def table(rows, widths):
    data=[[p(escape(str(c)), 'WhiteCell' if i==0 else 'Cell') for c in row] for i,row in enumerate(rows)]
    t=Table(data, colWidths=widths, repeatRows=1, hAlign='LEFT')
    t.setStyle(TableStyle([
        ('BACKGROUND',(0,0),(-1,0),INK), ('TEXTCOLOR',(0,0),(-1,0),colors.white),
        ('VALIGN',(0,0),(-1,-1),'TOP'), ('LEFTPADDING',(0,0),(-1,-1),7),
        ('RIGHTPADDING',(0,0),(-1,-1),7), ('TOPPADDING',(0,0),(-1,-1),7),
        ('BOTTOMPADDING',(0,0),(-1,-1),7), ('ROWBACKGROUNDS',(0,1),(-1,-1),[PALE,colors.white]),
        ('LINEBELOW',(0,0),(-1,0),0.6,INK)]))
    story.append(t)
    story.append(Spacer(1,10))

def code(relative, start, end, include_end=True):
    text=(ROOT / relative).read_text(encoding='utf-8')
    a=text.index(start)
    b=text.index(end,a)+(len(end) if include_end else 0)
    excerpt=text[a:b]
    # Add whitespace only at Java token boundaries, preserving quoted strings and identifiers.
    lines=[]
    for original in textwrap.dedent(excerpt).strip().splitlines():
        line=''; quoted=False; escaped=False
        for char in original.strip():
            if not quoted and char.isspace() and len(line)>70:
                lines.append(line);line='    ';continue
            if not quoted and char=='.' and len(line)>70:
                lines.append(line);line='    '
            line+=char
            if char=='"' and not escaped:
                quoted=not quoted
            if not quoted and (char==';' or (char==',' and len(line)>65)):
                lines.append(line);line='    '
            elif not quoted and char==',':
                line+=' '
            escaped=char=='\\' and not escaped
        if line.strip():lines.append(line)
    para(escape(relative), 'SmallText')
    assert max(stringWidth(line,'Courier',8) for line in lines)<=WIDTH, f'Code exceeds page width: {relative}'
    story.append(Preformatted('\n'.join(lines),styles['ReportCode']))

def shot(name, caption, max_height=245):
    path=ROOT/'docs'/'screenshots'/f'{name}.png'
    w,h=ImageReader(str(path)).getSize()
    scale=min(WIDTH/w, max_height/h)
    story.append(Image(str(path),width=w*scale,height=h*scale,hAlign='CENTER'))
    para(caption,'SmallText')
    story.append(Spacer(1,8))

def label(d,x,y,text,size=8,color=INK,font='Helvetica'):
    d.add(String(x,y,text,fontName=font,fontSize=size,fillColor=color))

def box(d,x,y,w,h,title,lines=(),kind='class'):
    d.add(Rect(x,y,w,h,rx=4,ry=4,fillColor=PALE,strokeColor=INK,strokeWidth=0.8))
    label(d,x+7,y+h-14,('<<interface>> ' if kind=='interface' else '')+title,8,INK,'Helvetica-Bold')
    if lines:
        d.add(Line(x,y+h-21,x+w,y+h-21,strokeColor=INK,strokeWidth=0.5))
        for i,line in enumerate(lines):
            label(d,x+7,y+h-34-i*12,line,7)

def arrow(d,x1,y1,x2,y2,text='',dashed=False,inherit=False):
    import math
    d.add(Line(x1,y1,x2,y2,strokeColor=MUTED,strokeWidth=0.8,strokeDashArray=[3,2] if dashed else None))
    angle=math.atan2(y2-y1,x2-x1)
    points=[x2,y2,x2-7*math.cos(angle-0.4),y2-7*math.sin(angle-0.4),x2-7*math.cos(angle+0.4),y2-7*math.sin(angle+0.4)]
    d.add(Polygon(points,fillColor=colors.white if inherit else MUTED,strokeColor=MUTED,strokeWidth=0.8))
    if text:
        label(d,(x1+x2)/2+3,(y1+y2)/2+5,text,7,MUTED)

def export_figure(d,name):
    renderSVG.drawToFile(d,str(UML/f'{name}.svg'))
    renderPDF.drawToFile(d,str(UML/f'{name}.pdf'))

def core_classes():
    d=Drawing(WIDTH,590)
    label(d,0,577,'Strategy: user-selected search',11,INK,'Helvetica-Bold')
    box(d,0,487,145,62,'StudentService',['+ search(name, class, mode)'])
    box(d,215,487,265,62,'StudentSearchStrategy',['+ key(): String','+ search(): List<Student>'],'interface')
    arrow(d,145,519,215,519,'selects')
    box(d,215,390,125,52,'ContainsSearch',['+ search()'])
    box(d,355,390,125,52,'PrefixSearch',['+ search()'])
    arrow(d,277,442,277,487,dashed=True,inherit=True)
    arrow(d,417,442,417,487,dashed=True,inherit=True)
    label(d,0,357,'Builder: validated QR plan',11,INK,'Helvetica-Bold')
    box(d,0,274,125,62,'QrService',['+ create(actor, options)'])
    box(d,171,274,147,62,'QrIssuePlan.Builder',['+ validForMinutes(n)','+ build(): QrIssuePlan'])
    box(d,359,274,121,62,'QrIssuePlan',['+ issue(token): QrCode'])
    arrow(d,125,304,171,304,'uses')
    arrow(d,318,304,359,304,'builds')
    label(d,0,239,'Observer: dispatch after transaction commit',11,INK,'Helvetica-Bold')
    box(d,0,150,145,62,'VerificationService',['+ review(id, decision)'])
    box(d,215,150,265,62,'ReviewPublisher',['+ publishAfterCommit(event)','+ subscribe() / unsubscribe()'])
    arrow(d,145,181,215,181,'publishes')
    box(d,215,63,265,50,'ReviewSubscriber',['+ onReview(event)'],'interface')
    arrow(d,349,150,349,113,'notifies 0..*')
    box(d,0,0,180,42,'ReviewInbox',['+ forGuardian(id)'])
    box(d,300,0,180,42,'ReviewAuditSubscriber',['+ onReview(event)'])
    arrow(d,180,23,238,63,dashed=True,inherit=True)
    arrow(d,389,42,389,63,dashed=True,inherit=True)
    return d

def report_classes():
    d=Drawing(WIDTH,595)
    box(d,0,526,175,56,'AccessReportFacade',['+ summary(from, to)','+ export(format, redact)'])
    box(d,290,526,190,56,'ReportSource',['+ entries(): List<ReportEntry>'],'interface')
    arrow(d,175,554,290,554,'reads')
    box(d,0,420,195,60,'AccessLogReportAdapter',['- repositories: existing model','+ entries()'])
    box(d,285,420,195,60,'RedactedReportSource',['- wrapped: ReportSource','+ entries(): redacted copy'])
    arrow(d,130,480,335,526,dashed=True,inherit=True)
    arrow(d,384,480,384,526,dashed=True,inherit=True)
    label(d,7,401,'Adapter: relational records to typed entries',8)
    label(d,288,401,'Decorator: wraps another ReportSource',8)
    box(d,0,300,190,64,'ReportExporter (abstract)',['+ export(entries): Export','# createDocument(): Document'])
    box(d,290,300,190,64,'ReportDocument',['+ render(entries): bytes'],'interface')
    arrow(d,190,332,290,332,'renders')
    box(d,0,205,190,52,'Csv / JsonReportExporter',['# createDocument()'])
    box(d,290,205,190,52,'Csv / JsonReportDocument',['+ render(entries)'])
    arrow(d,95,257,95,300,inherit=True)
    arrow(d,385,257,385,300,dashed=True,inherit=True)
    arrow(d,190,231,290,231,'creates')
    label(d,7,186,'Factory Method: each exporter creates its concrete product',8)
    box(d,0,90,190,60,'ReportEntry',['+ accept(visitor)','Allowed / Denied implement'],'interface')
    box(d,290,90,190,60,'ReportVisitor',['+ visit(Allowed)','+ visit(Denied)'],'interface')
    arrow(d,190,120,290,120,'accept')
    box(d,290,0,190,53,'StatisticsVisitor',['+ summary(): counts, reasons'])
    arrow(d,385,53,385,90,dashed=True,inherit=True)
    label(d,7,29,'Facade coordinates the participants above.',8)
    label(d,7,15,'Visitor dispatches by entry type.',8)
    return d

def sequence():
    d=Drawing(WIDTH,585)
    xs=[37,138,239,340,441]
    for x,title in zip(xs,['Users','React UI','Java services','PostgreSQL','Review inbox']):
        box(d,x-36,542,73,31,title)
        d.add(Line(x,20,x,542,strokeColor=MUTED,strokeDashArray=[3,3],strokeWidth=0.5))
    events=[(0,1,'Parent selects pupil, requests relationship'),(1,2,'POST /guardian-students/'),
      (2,3,'Save link + pending request; commit'),(0,1,'Administrator approves'),
      (1,2,'PATCH request /approve'),(2,3,'Lock link; update decision; COMMIT'),
      (2,4,'afterCommit: publish ReviewEvent'),(1,4,'Poll authenticated /notifications'),
      (4,1,'Notice; refresh dashboard'),(0,1,'Parent selects 5-minute QR'),
      (1,2,'POST QR; Builder creates issue plan'),(2,3,'Persist ACTIVE QR; commit'),
      (0,1,'Guard scans QR'),(1,2,'POST /qr-codes/verify'),
      (2,3,'Lock QR; USED + ALLOWED log; commit'),(0,1,'Guard repeats scan'),
      (2,3,'DENIED / QR_ALREADY_USED; commit'),(1,2,'GET report/export?format=json&redact=true'),
      (2,3,'Adapter reads persisted records'),(2,1,'Decorate + factory document; download')]
    for i,(a,b,text) in enumerate(events):
        y=520-i*24
        arrow(d,xs[a],y,xs[b],y)
        # Long messages are printed across the figure, above their own arrow.
        label(d,5,y+5,f'{i+1:02d}  {text}',6.8,INK)
    return d

def footer(canvas,doc):
    canvas.setStrokeColor(colors.HexColor('#ccd6e0'))
    canvas.line(56,43,539,43)
    canvas.setFont('Helvetica',8)
    canvas.setFillColor(MUTED)
    canvas.drawString(56,29,'Security System | Software Design Patterns | design-patterns')
    canvas.drawRightString(539,29,str(doc.page))

def build():
    OUT.mkdir(parents=True,exist_ok=True)
    UML.mkdir(parents=True,exist_ok=True)
    para('SOFTWARE DESIGN PATTERNS','Subtitle')
    para('School Security System','ReportTitle')
    para('Final project report','Subtitle')
    para('A Java application for verified school pickup, single-use QR access and persistent audit reporting.')
    story.append(Spacer(1,18))
    table([['Team member','Allocated pattern responsibility'],
      ['Aldiyar Adilkhan','Factory Method and Strategy'],
      ['Baktiyar Kaiyrzhan','Builder and Observer'],
      ['Bauyrzhan Muratkhanov','Decorator and Adapter'],
      ['Zhalgas Sarsenov','Facade and Visitor']],[185,298])
    para('Repository: <link href="https://github.com/Buka2989/SecuritySystem/tree/design-patterns" color="#173454">github.com/Buka2989/SecuritySystem - design-patterns</link>')
    section('Introduction')
    para('School staff need a controlled way to confirm which adult may collect a pupil, authorize an entry and retain a reviewable history. This application links guardians to pupils through administrator review. An approved guardian can issue a time-limited QR; security staff validate it once and export the resulting audit records.')
    para('The project extends an existing React and PostgreSQL application with a Java 21 Spring Boot core. Eight application-owned design patterns support configurable behavior, review notifications and report generation. The user journey covers selection, configuration, action, review and export.')
    para('This report explains the implemented design, demonstration scenarios, source excerpts, verification evidence and team responsibilities. Individual oral defense and independent work remain the responsibility of each team member.','SmallText')

    page('Main Body')
    section('Users and scenarios')
    table([['Scenario','Inputs and visible outcome'],
      ['Student administration','ADMIN creates a pupil, changes the class and sets ACTIVE/INACTIVE.'],
      ['Selectable search','PARENT chooses contains/prefix, enters name and class, selects a match.'],
      ['Request and review','Guardian chooses relationship type; ADMIN approves or rejects. Rejected requests can be resubmitted; removal cancels pending requests.'],
      ['Event reaction','After approval commits, the guardian sees a notification and updated relationship status.'],
      ['QR configuration','Approved guardian selects lifetime and issues or revokes a QR.'],
      ['Access control','SECURITY scans QR; one use succeeds, repeated/expired/revoked attempts are denied and logged.'],
      ['Report and persistence','ADMIN/SECURITY filters saved logs, inspects counts and downloads CSV/JSON with optional redaction. Data survives restart.']],[123,360])
    section('Architecture and data')
    para('React pages call authenticated REST controllers through /api. DTO validation and role checks protect the boundary. Java services enforce lifecycle rules and own transaction boundaries. JPA repositories store six entities in PostgreSQL; Nginx serves the production interface. The UI, business decisions and persistence are separate layers.')
    para('User 1..* GuardianStudent *..1 Student; GuardianStudent 1..* VerificationRequest and QrCode; QrCode 1..* AccessLog. Each access log also refers to the security User. Relationship deletion is soft, preserving verification and access history. A named Docker volume retains data across container recreation.')
    para('QR verification locks the relationship and QR in a consistent order. QR consumption and the audit insert commit together. A failed audit insert rolls consumption back. Unknown tokens cannot satisfy the audit table foreign key and are logged in application logs instead.')

    page('Pattern selection and demonstrations')
    rows=[['Pattern / category','Problem and main participants','Demonstration / owner'],
      ['Builder / creational','QrIssuePlan.Builder collects issue options and validates the immutable QrIssuePlan before issuing QrCode.','Select five-minute QR; inspect expiry. Baktiyar Kaiyrzhan.'],
      ['Factory Method / creational','ReportExporter defines export; CsvReportExporter and JsonReportExporter override createDocument, returning ReportDocument products.','Download both formats. Aldiyar Adilkhan.'],
      ['Adapter / structural','AccessLogReportAdapter implements ReportSource to translate existing repositories into typed ReportEntry records.','Scan QR, then inspect report row. Bauyrzhan Muratkhanov.'],
      ['Decorator / structural','RedactedReportSource wraps ReportSource; produces copied entries with names/IDs hidden, preserving original data.','Toggle redaction; compare exports. Bauyrzhan Muratkhanov.'],
      ['Facade / structural','AccessReportFacade coordinates date validation, data source, decorator, exporter and visitor behind summary/export operations.','Filter period and export from one page. Zhalgas Sarsenov.'],
      ['Strategy / behavioral','StudentSearchStrategy is implemented by ContainsSearch and PrefixSearch; StudentService selects the user-requested implementation.','Search Child in Test Child with both modes. Aldiyar Adilkhan.'],
      ['Observer / behavioral','ReviewPublisher owns ReviewSubscriber registrations. ReviewInbox and ReviewAuditSubscriber react only after a review commits.','Approve in a second session; guardian UI updates. Baktiyar Kaiyrzhan.'],
      ['Visitor / behavioral','Allowed and Denied entries accept ReportVisitor; StatisticsVisitor counts outcomes and denial reasons through overloaded visits.','First scan + repeat produce total 2, allowed 1, denied 1. Zhalgas Sarsenov.']]
    table(rows,[99,224,160])
    para('All eight patterns run through the same application. Spring supplies dependency injection and transaction callbacks; the application supplies the pattern participants and interactions. Framework-provided patterns are not counted.','SmallText')

    page('Class diagram for access and review')
    d=core_classes();export_figure(d,'access-classes');story.append(d)
    story.append(Spacer(1,12))
    para('Dashed hollow arrows indicate interface realization. Ordinary arrows indicate use. Concrete search strategies delegate database access to StudentRepository.','SmallText')

    page('Class diagram for reporting')
    d=report_classes();export_figure(d,'report-classes');story.append(d)
    para('Csv / Json labels abbreviate two corresponding concrete classes, listed in the pattern table. The complete editable class diagram is docs/uml/classes.mmd.','SmallText')

    page('Sequence diagram for the main journey')
    d=sequence();export_figure(d,'journey');story.append(d)
    para('Each database operation includes a service transaction. The afterCommit callback is essential: failed or rolled-back reviews must not appear as successful notifications. The editable sequence source is docs/uml/journey.mmd.','SmallText')

    page('Creational pattern implementation')
    section('Builder prepares a valid QR issue plan')
    code('backend/src/main/java/kz/school/security/service/QrService.java','QrCode q=kz.school.security.patterns.qr.QrIssuePlan.builder()', 'return DtoMapper.qr(codes.save(q));')
    para('QrIssuePlan validates that a relationship and issue time exist and the lifetime is between one and 1,440 minutes. The service checks ownership, approval and student activity before construction. Token bytes come from SecureRandom. The plan retains thirty minutes as its default.')
    section('Factory Method creates format-specific products')
    code('backend/src/main/java/kz/school/security/patterns/report/ReportExporter.java','protected abstract ReportDocument createDocument();','document.extension());\n    }')
    code('backend/src/main/java/kz/school/security/patterns/report/JsonReportExporter.java','protected ReportDocument createDocument()','public String format(){return "json";}')
    para('The inherited export workflow asks the overridden factory method for a product, then uses only ReportDocument operations. Adding a format introduces a product and exporter without rewriting the common workflow. The tradeoff is extra classes for a small number of formats.')

    page('Behavioral pattern implementation')
    section('Strategy selects the requested algorithm')
    code('backend/src/main/java/kz/school/security/service/StudentService.java','public List<StudentResponse> search(String name,String className,String mode)', '.map(DtoMapper::student).toList();\n    }')
    section('Observer delivers only committed decisions')
    code('backend/src/main/java/kz/school/security/patterns/events/ReviewPublisher.java','public void publishAfterCommit(ReviewEvent event)', '\n    public void dispatch',False)
    para('The publisher isolates subscriber runtime failures. ReviewInbox keeps at most 1,000 deliveries and only returns notices for the authenticated guardian. React polls every three seconds and dispatches verification-updated to refresh the dashboard. A restart removes notices; the underlying verification decision remains persistent.')
    section('Visitor dispatches on the concrete entry type')
    code('backend/src/main/java/kz/school/security/patterns/report/ReportEntry.java','record Allowed(', 'visitor.visit(this);}')
    para('StatisticsVisitor implements visit(Allowed) and visit(Denied). Adding another aggregation is independent of report rendering; adding a new entry subtype requires every visitor to handle it.')

    page('Structural pattern implementation')
    section('Adapter maps the existing model')
    code('backend/src/main/java/kz/school/security/patterns/report/AccessLogReportAdapter.java','String guardian=', ':new ReportEntry.Denied(log.id.toString(),guardian,student,guard,log.reason,log.createdAt);')
    para('The adapter connects audit rows to their QR, relationship, guardian, pupil and guard. It exposes report entries without altering the inherited schema. The current per-record lookups are adequate for a classroom dataset; larger datasets should use a joined projection and pagination.')
    section('Decorator adds a privacy option')
    code('backend/src/main/java/kz/school/security/patterns/report/RedactedReportSource.java','public class RedactedReportSource', '\n}')
    section('Facade hides orchestration from the controller')
    code('backend/src/main/java/kz/school/security/patterns/report/AccessReportFacade.java','public StatisticsVisitor.Summary summary(', 'return visitor.summary();\n    }')
    para('The export operation chooses a registered exporter, validates the period and applies optional decoration. Redaction hides selected identifiers and names, while reason codes and timestamps remain. CSV cells are quoted and formula-prefixed values are neutralized before download.')

    page('Interface and student workflow')
    shot('student-management','Figure 1. Administrator creates and edits a student through real REST operations.',265)
    shot('search-strategies','Figure 2. Guardian selects the prefix strategy to narrow student matches.',265)

    page('Review notification and configured QR')
    shot('review-notification','Figure 3. A committed administrator review updates the guardian interface.',265)
    shot('configured-qr','Figure 4. The guardian selects a QR lifetime and receives a timed code.',265)

    page('Reporting and test evidence')
    shot('access-report','Figure 5. Authorized staff inspect persisted counts and choose export format and redaction.',295)
    section('Verification scope')
    evidence_path=ROOT/'docs'/'verification.json'
    evidence=json.loads(evidence_path.read_text(encoding='utf-8')) if evidence_path.exists() else {'status':'Verification is running; consult docs/TESTING.md.'}
    for key,value in evidence.items():
        para(f'<b>{escape(key.replace("_"," ").capitalize())}:</b> {escape(str(value))}')
    para('The executable evidence is under backend/target/surefire-reports, backend/target/failsafe-reports and e2e/playwright-report after running the suites. CI uploads these reports. The submission archive excludes caches and generated test traces; the scenario matrix below records the relevant results.')

    page('Test scenarios and actual results')
    para('Automated scenarios exercise real business logic. PostgreSQL API tests cover persistence and transactions; browser tests use the actual interface and endpoints. Inputs, expected results and actual outcomes are recorded below and in docs/TESTING.md.')
    matrix_path=ROOT/'docs'/'test-matrix.json'
    matrix=json.loads(matrix_path.read_text(encoding='utf-8')) if matrix_path.exists() else []
    table([['Scenario and input','Expected result','Actual result']]+[[r['input'],r['expected'],r['actual']] for r in matrix],[166,173,144])
    para('Business logic automation includes JWT and QR policy tests plus Builder, Factory Method, Decorator, Observer, Facade and Visitor tests. Negative cases include invalid lifetime, blank student fields, unsupported search/report modes, reversed dates and unauthorized operations.','SmallText')

    page('Individual contributions and conclusion')
    section('Team responsibilities')
    para('The team confirmed the allocation shown on the title page. Aldiyar owns search and document creation; Baktiyar owns QR planning and committed event delivery; Bauyrzhan owns data adaptation and privacy decoration; Zhalgas owns report orchestration and aggregation. Each member is responsible for running the relevant tests, demonstrating the two patterns and making a small code change during defense. Detailed exercises and limitations are in docs/DEFENSE.md.')
    para('The application inherits the source history of the Java migration and design-pattern extension. Responsibility allocation is not evidence that each student independently authored all allocated code. Weekly progress presentations and individual defense must be completed by the students.')
    section('Conclusion')
    para('The application supports the complete school-access journey: manage a pupil, configure a guardian link, review it, issue a timed QR, record access and export the saved result. Eight patterns provide distinct extension points within one integrated Java application. The interface is connected to persistent business operations, and automated checks cover successful workflows, alternative behavior, events and invalid actions.')
    section('Future Work')
    para('Add durable event delivery for multiple backend instances; replace per-row report lookups with joined, paginated queries; introduce deployment-specific HTTPS and camera testing on phones; add account-management and registration screens; and provide retention controls for audit records. These improvements address scale and deployment needs beyond the classroom demonstration.')
    section('Tools and external materials')
    para('OpenAI Codex assisted with repository analysis, implementation support, regression tests, UML and report preparation. Students must understand, verify and modify the submitted solution in accordance with course policy. No fabricated commit history or independent-authoring claims are used.')
    para('External software: Java/OpenJDK, Spring Boot/Security/Data JPA, PostgreSQL, React, TypeScript, Vite, Nginx, Docker Compose, JUnit/AssertJ/Mockito, Testcontainers, Playwright, html5-qrcode, qrcode.react and ReportLab. Dependency versions are pinned or resolved in pom.xml and package-lock.json. Requirements were taken from the supplied FINAL PROJECT document. The README and source repository provide reproducible launch and test commands.','SmallText')
    SimpleDocTemplate(str(OUT/'security-system-report.pdf'),pagesize=(595.28,841.89),
      rightMargin=56,leftMargin=56,topMargin=49,bottomMargin=58,
      title='School Security System Final Project Report',author='Security System project team').build(story,onFirstPage=footer,onLaterPages=footer)
    print(OUT/'security-system-report.pdf')

if __name__=='__main__':
    build()
