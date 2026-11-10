"""Package only tracked and non-ignored project sources, never local credentials."""
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'output' / 'security-system-submission.zip'

def main():
    OUTPUT.parent.mkdir(exist_ok=True)
    files = subprocess.check_output(
        ['git', 'ls-files', '-z', '--cached', '--others', '--exclude-standard'], cwd=ROOT
    ).decode('utf-8').split('\0')
    with zipfile.ZipFile(OUTPUT, 'w', zipfile.ZIP_DEFLATED) as archive:
        for name in sorted(set(files)):
            path = ROOT / name
            if not name or not path.is_file():
                continue
            if name.startswith(('output/', 'tmp/')) and name != 'output/pdf/security-system-report.pdf':
                continue
            if path.name.startswith('.env') and path.name != '.env.example':
                continue
            if path.name == 'pnpm-lock.yaml':
                continue  # npm package-lock.json is the authoritative application lockfile.
            archive.write(path, 'SecuritySystem/' + name)
    with zipfile.ZipFile(OUTPUT) as archive:
        names = archive.namelist()
        assert 'SecuritySystem/output/pdf/security-system-report.pdf' in names
        assert not any('/node_modules/' in n or n.endswith('/.env') or '/target/' in n for n in names)
        assert archive.testzip() is None
    print(f'{OUTPUT}: {len(names)} files, {OUTPUT.stat().st_size:,} bytes')

if __name__ == '__main__':
    main()
