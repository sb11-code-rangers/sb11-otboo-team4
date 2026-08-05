#!/usr/bin/env bash
# push_prop.sh가 실패하는 원인을 진단하기 위한 스크립트.
# git clone/pull/push, 파일 복사 등 어떤 것도 건드리지 않고 .env 로딩 상태와
# Discord 웹훅 전송 가능 여부만 확인한다.
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$REPO_ROOT/.env"

mask() {
  local val="$1"
  local len=${#val}
  if [ "$len" -le 8 ]; then
    echo "$val"
  else
    echo "${val:0:6}...${val: -4} (len=$len)"
  fi
}

echo "=== 1. 실행 환경 ==="
echo "\$0             : $0"
echo "BASH_SOURCE[0] : ${BASH_SOURCE[0]:-<bash가 아님>}"
echo "REPO_ROOT      : $REPO_ROOT"
echo

echo "=== 2. .env 파일 존재 확인 ==="
if [ -f "$ENV_FILE" ]; then
  echo "OK: $ENV_FILE 존재함"
else
  echo "FAIL: $ENV_FILE 없음 (경로 확인 필요)"
  exit 1
fi
echo

echo "=== 3. .env 줄바꿈(CRLF) 확인 ==="
if grep -q $'\r' "$ENV_FILE"; then
  echo "WARN: CRLF(\\r)가 포함된 줄 발견 (값 끝에 ^M 이 붙어 오염될 수 있음)"
  cat -A "$ENV_FILE"
else
  echo "OK: CRLF 없음 (LF만 사용)"
fi
echo

echo "=== 4. .env 로딩 후 변수 값 확인 ==="
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

echo "SECRET_REPO_URL     : $(mask "${SECRET_REPO_URL:-}")"
echo "DISCORD_WEBHOOK_URL : $(mask "${DISCORD_WEBHOOK_URL:-}")"

if [[ "${DISCORD_WEBHOOK_URL:-}" == *$'\r'* ]]; then
  echo "WARN: DISCORD_WEBHOOK_URL 값 안에 \\r 문자가 섞여 있음"
fi
if [ -z "${DISCORD_WEBHOOK_URL:-}" ]; then
  echo "WARN: DISCORD_WEBHOOK_URL이 비어 있음 (.env에 없거나 소싱 실패)"
fi
echo

echo "=== 5. gh CLI 상태 ==="
if command -v gh >/dev/null 2>&1; then
  echo "OK: gh 설치됨 ($(command -v gh))"
  echo "로그인 상태:"
  gh auth status 2>&1 || true
  echo "login: $(gh api user -q .login 2>&1)"
else
  echo "FAIL: gh 명령을 찾을 수 없음 (PATH에 없음)"
fi
echo

echo "=== 6. Discord 웹훅 실제 전송 테스트 ==="
if [ -z "${DISCORD_WEBHOOK_URL:-}" ]; then
  echo "SKIP: DISCORD_WEBHOOK_URL이 비어 있어 전송 테스트를 건너뜀"
else
  HTTP_CODE="$(curl -sS -o /dev/null -w '%{http_code}' -X POST \
    -H "Content-Type: application/json" \
    -d '{"content": "push_prop.sh 진단 스크립트(check_push_prop.sh) 테스트 메시지입니다."}' \
    "$DISCORD_WEBHOOK_URL")"
  echo "HTTP 응답 코드: $HTTP_CODE"
  if [ "$HTTP_CODE" = "204" ]; then
    echo "OK: .env에서 로딩한 값으로 Discord 전송 성공"
  else
    echo "FAIL: 전송 실패 (코드: $HTTP_CODE) - 위 3, 4번 결과 확인"
  fi
fi

echo
echo "=== 진단 완료 (git/파일 변경 없음) ==="