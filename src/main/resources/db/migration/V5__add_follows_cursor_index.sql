-- Spring Batch 6.x 공식 PostgreSQL 스키마(schema-postgresql.sql, spring-batch-core) 그대로 사용.
-- Flyway로 버전 관리하기 위해 Boot 자동 초기화(spring.batch.jdbc.initialize-schema) 대신 마이그레이션으로 관리한다.

CREATE INDEX IDX_follows_follower_created_id
    ON follows (follower_id, created_at DESC, id DESC);

CREATE INDEX IDX_follows_followee_created_id
    ON follows (followee_id, created_at DESC, id DESC);