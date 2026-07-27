-- users/profiles 테이블 alter

ALTER TABLE users
    RENAME COLUMN locked TO is_locked;

ALTER TABLE users
    ADD COLUMN lock_reason VARCHAR(20) NOT NULL DEFAULT 'NONE'
        CHECK (lock_reason IN ('NONE', 'ADMIN_ACTION'));

ALTER TABLE profiles
    RENAME COLUMN x TO location_x;

ALTER TABLE profiles
    RENAME COLUMN y TO location_y;

ALTER TABLE profiles
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE profiles
    ALTER COLUMN created_at DROP DEFAULT;

ALTER TABLE profiles
    DROP COLUMN name;
