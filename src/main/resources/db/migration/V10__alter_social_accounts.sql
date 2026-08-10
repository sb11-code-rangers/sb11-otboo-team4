-- social_accounts는 V1에서 provider/provider_id까지만 만들어졌다. OAuth2 로그인을 실제로
-- 연동하면서 provider_email(연동된 소셜 계정의 이메일 — 로그인 매칭에는 쓰지 않고 어떤
-- 소셜 계정이 연동됐는지 확인하는 용도)과 updated_at(다른 테이블들과의 감사 컬럼 일관성)이
-- 필요해져서 추가한다. 지금까지 OAuth2 로그인 기능 자체가 없어서 이 테이블은 항상 비어있는
-- 상태였으므로, provider_email을 기본값 없이 NOT NULL로 바로 추가해도 안전하다.

ALTER TABLE social_accounts
    ADD COLUMN provider_email VARCHAR(255) NOT NULL;

ALTER TABLE social_accounts
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE social_accounts
    ALTER COLUMN updated_at DROP DEFAULT;
