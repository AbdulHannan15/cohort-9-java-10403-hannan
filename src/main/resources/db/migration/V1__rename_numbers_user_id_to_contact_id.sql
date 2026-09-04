-- Manual migration (this project uses spring.jpa.hibernate.ddl-auto=update, not
-- Flyway/Liquibase, so this script is NOT run automatically). Apply it once,
-- by hand, against any existing database BEFORE deploying the build that
-- contains the updated PhoneNumberEntity#contact join column.
--
-- Why: PhoneNumberEntity's @JoinColumn was renamed from "user_id" to
-- "contact_id". On a fresh/empty database, ddl-auto=update will simply create
-- the "numbers" table with the new "contact_id" column and there is nothing
-- to do. On a database that already has a populated "numbers" table with a
-- "user_id" column, Hibernate will instead try to read/write "contact_id",
-- which does not exist yet, and will fail. This script renames the column in
-- place (preserving every existing association) and recreates the foreign
-- key to match.
--
-- Adjust the constraint/table names below if your existing FK/index names
-- differ from these defaults - check with:
--   SHOW CREATE TABLE numbers;

START TRANSACTION;

-- 1. Drop the existing foreign key that points at user_id.
--    (Replace 'numbers_ibfk_1' with your actual FK name if different.)
ALTER TABLE numbers DROP FOREIGN KEY numbers_ibfk_1;

-- 2. Rename the column, keeping its data (and therefore every existing
--    contact <-> number association) intact.
ALTER TABLE numbers CHANGE COLUMN user_id contact_id BIGINT NOT NULL;

-- 3. Recreate the foreign key against the contacts table so it matches the
--    @JoinColumn(name = "contact_id") on PhoneNumberEntity.
ALTER TABLE numbers
    ADD CONSTRAINT fk_numbers_contact
    FOREIGN KEY (contact_id) REFERENCES contacts (id);

COMMIT;
