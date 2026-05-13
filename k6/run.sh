#!/bin/bash
# k6 테스트 실행 스크립트
# 사용법: ./k6/run.sh <테스트파일명>
# 예시:   ./k6/run.sh tests/my_test.js

set -e

# .env 파일 로드 (있으면) — 공백/특수문자 안전 파싱
if [ -f "k6/.env" ]; then
  while IFS='=' read -r key value; do
    # 빈 줄, 주석 건너뛰기
    [[ -z "$key" || "$key" == \#* ]] && continue
    # 앞뒤 공백 제거
    key="${key// /}"
    value="${value#"${value%%[![:space:]]*}"}"
    value="${value%"${value##*[![:space:]]}"}"
    # 따옴표 제거 (값이 "..." 또는 '...'로 감싸진 경우)
    value="${value#\"}" ; value="${value%\"}"
    value="${value#\'}"  ; value="${value%\'}"
    export "$key=$value"
  done < "k6/.env"
fi

BASE_URL=${BASE_URL:-http://localhost:8080}
TEST_EMAIL=${TEST_EMAIL:-root@instagram.com}
TEST_PASSWORD=${TEST_PASSWORD:-root1234}
TEST_USERNAME=${TEST_USERNAME:-root}
TEST_ROOM_ID=${TEST_ROOM_ID:-1}
FRONTEND_ORIGIN=${FRONTEND_ORIGIN:-http://localhost:3000}

TARGET=${1:-}

if [ -z "$TARGET" ]; then
  echo "사용법: ./k6/run.sh <테스트파일 경로>"
  echo "예시:   ./k6/run.sh k6/tests/my_test.js"
  exit 1
fi

k6 run \
  -e BASE_URL="$BASE_URL" \
  -e TEST_EMAIL="$TEST_EMAIL" \
  -e TEST_PASSWORD="$TEST_PASSWORD" \
  -e TEST_USERNAME="$TEST_USERNAME" \
  -e TEST_ROOM_ID="$TEST_ROOM_ID" \
  -e FRONTEND_ORIGIN="$FRONTEND_ORIGIN" \
  "$TARGET"
