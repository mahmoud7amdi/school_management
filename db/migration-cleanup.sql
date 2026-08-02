-- ============================================================================
--  Schema cleanup for the entity rework
--  ---------------------------------------------------------------------------
--  Run this ONCE against your development database, then start the app.
--
--    mysql -u root school_db < db/migration-cleanup.sql
--
--  WHY THIS IS NEEDED
--  `ddl-auto: update` only ever ADDS things. It never drops a column or renames
--  a table. The database therefore still carries columns and tables from earlier
--  revisions of the entities, and two of them actively break writes:
--
--    * academic_years.active — NOT NULL with no default, and no longer mapped by
--      any entity, so every INSERT fails with
--      "Field 'active' doesn't have a default value".
--
--    * token_blacklist.token — NOT NULL UNIQUE holding the raw JWT. The revoked
--      token store now hashes tokens and lives in `revoked_tokens`, so logout
--      would fail against the old table.
--
--  Everything below is either empty or superseded. Row counts were checked
--  before this was written: the singular tables held 0 rows.
--
--  BACK UP FIRST if this database holds anything you care about:
--    mysqldump -u root school_db > school_db_backup.sql
-- ============================================================================

-- --------------------------------------------------------------------------
-- 1. Drop the orphaned NOT NULL column that blocks academic-year inserts.
-- --------------------------------------------------------------------------
ALTER TABLE academic_years DROP COLUMN active;

-- --------------------------------------------------------------------------
-- 2. Drop the pre-@Table singular tables. These were created when the entities
--    had no explicit @Table(name=...) and Hibernate derived the class name.
--    The plural equivalents are the live ones. All of these were empty.
-- --------------------------------------------------------------------------
DROP TABLE IF EXISTS classroom;
DROP TABLE IF EXISTS subject;
DROP TABLE IF EXISTS grade;
DROP TABLE IF EXISTS academic_year;

-- --------------------------------------------------------------------------
-- 3. Drop the superseded token blacklist. Replaced by `revoked_tokens`, which
--    stores a SHA-256 hash instead of the raw bearer token. Any rows here are
--    revoked sessions that have long since expired on their own.
-- --------------------------------------------------------------------------
DROP TABLE IF EXISTS token_blacklist;

-- --------------------------------------------------------------------------
-- 4. Existing users predate the `full_name` column and were backfilled with an
--    empty string. Seed it from the username so the UI has something to show;
--    the affected accounts can be edited properly afterwards.
-- --------------------------------------------------------------------------
UPDATE users SET full_name = username WHERE full_name IS NULL OR full_name = '';

-- --------------------------------------------------------------------------
-- 5. Verify. Every row returned here is a column that is NOT NULL, has no
--    default, and would break an INSERT if no entity writes it.
--    An empty result means the schema is clean.
-- --------------------------------------------------------------------------
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND IS_NULLABLE = 'NO'
  AND COLUMN_DEFAULT IS NULL
  AND EXTRA NOT LIKE '%auto_increment%'
  AND TABLE_NAME IN ('academic_years', 'grades', 'subjects', 'classrooms',
                     'students', 'schools', 'users', 'revoked_tokens')
ORDER BY TABLE_NAME, COLUMN_NAME;
