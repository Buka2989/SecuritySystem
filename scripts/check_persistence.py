"""Verify all six demo tables survive container recreation without deleting volumes.

Run only against an isolated test/demo Compose project:
    python scripts/check_persistence.py --project securysys-final
"""
import argparse
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
TABLES=('users','students','guardian_student','verification_requests','qr_codes','access_logs')

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--project',required=True,help='Explicit isolated Compose project name')
    args=parser.parse_args()
    compose=['docker','compose','-p',args.project]
    def snapshot():
        result={}
        for table in TABLES:
            sql=f"select count(*), md5(coalesce(string_agg(row_to_json(t)::text, '' order by id),'')) from {table} t"
            command=compose+['exec','-T','postgres','sh','-c',
                'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -At -F ":" -c "$1"','--',sql]
            row=subprocess.check_output(command,cwd=ROOT,text=True).strip()
            count,digest=row.split(':')
            result[table]={'rows':int(count),'digest':digest}
        return result
    before=snapshot()
    assert all(v['rows']>0 for v in before.values()),'Demonstrate the app before testing persistence'
    subprocess.run(compose+['down'],cwd=ROOT,check=True)
    subprocess.run(compose+['up','-d','--wait','--wait-timeout','240'],cwd=ROOT,check=True)
    after=snapshot()
    assert before==after, f'Data changed across recreation: {before} != {after}'
    output=ROOT/'docs'/'persistence-result.json'
    output.write_text(json.dumps({'result':'PASS','tables':after},indent=2)+'\n',encoding='utf-8')
    print('PASS: all six table counts and complete row checksums survived container recreation')

if __name__=='__main__':
    main()
