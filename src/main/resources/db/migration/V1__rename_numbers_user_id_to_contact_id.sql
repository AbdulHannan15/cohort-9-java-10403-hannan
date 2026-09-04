-- Renames numbers.user_id -> numbers.contact_id to match the updated
-- @JoinColumn(name = "contact_id") on PhoneNumberEntity, preserving every
-- existing contact <-> number association, and points the foreign key at
-- contacts(id) instead of the old users table.
--
-- Written to be a safe no-op in two cases so it can run unconditionally on
-- every environment via Flyway:
--   1. Fresh database: the "numbers" table doesn't exist yet (Hibernate's
--      ddl-auto=update creates it, with contact_id already, right after
--      Flyway runs) -> nothing to migrate.
--   2. Already-migrated database: "numbers" exists but "user_id" is already
--      gone -> nothing to migrate.
-- Only when "numbers.user_id" is actually present does this rename the
-- column, preserve its values, and swap the foreign key.

DELIMITER $$

CREATE PROCEDURE migrate_numbers_user_id_to_contact_id()
BEGIN
    DECLARE table_exists INT DEFAULT 0;
    DECLARE column_exists INT DEFAULT 0;
    DECLARE fk_name VARCHAR(128) DEFAULT NULL;

    SELECT COUNT(*) INTO table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'numbers';

    IF table_exists = 1 THEN
        SELECT COUNT(*) INTO column_exists
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'numbers'
          AND column_name = 'user_id';

        IF column_exists = 1 THEN
            -- Find whatever the existing FK on user_id is actually named,
            -- instead of assuming a fixed constraint name.
            SELECT constraint_name INTO fk_name
            FROM information_schema.key_column_usage
            WHERE table_schema = DATABASE()
              AND table_name = 'numbers'
              AND column_name = 'user_id'
              AND referenced_table_name IS NOT NULL
            LIMIT 1;

            IF fk_name IS NOT NULL THEN
                SET @drop_fk_sql = CONCAT('ALTER TABLE numbers DROP FOREIGN KEY `', fk_name, '`');
                PREPARE stmt FROM @drop_fk_sql;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;
            END IF;

            -- Rename the column in place, keeping every existing value
            -- (and therefore every existing contact <-> number association).
            ALTER TABLE numbers CHANGE COLUMN user_id contact_id BIGINT NOT NULL;

            -- Recreate the foreign key against contacts(id), matching
            -- @JoinColumn(name = "contact_id") on PhoneNumberEntity.
            ALTER TABLE numbers
                ADD CONSTRAINT fk_numbers_contact
                FOREIGN KEY (contact_id) REFERENCES contacts (id);
        END IF;
    END IF;
END$$

DELIMITER ;

CALL migrate_numbers_user_id_to_contact_id();

DROP PROCEDURE migrate_numbers_user_id_to_contact_id;
