#!/bin/sh
set -eu
# The PostgreSQL image invokes this only for an empty data directory.
if [ -f /opt/security/import/security_backup.backup ]; then
  pg_restore --exit-on-error --single-transaction --no-owner --no-privileges \
    --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" /opt/security/import/security_backup.backup
else
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /opt/security/schema.sql
fi
