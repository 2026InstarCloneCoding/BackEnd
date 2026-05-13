#!/bin/bash
# k6 테스트 실행 스크립트
# 사용법: ./k6/run.sh <테스트파일명>
# 예시:   ./k6/run.sh tests/my_test.js

set -e

# .env 파일 로드 (있으면)
if [ -f "k6/.env" ]; then
  export $(grep -v '^#' k6/.env | xargs)
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
  -e BASE_URL=$BASE_URL \
  -e TEST_EMAIL=$TEST_EMAIL \
  -e TEST_PASSWORD=$TEST_PASSWORD \
  -e TEST_USERNAME=$TEST_USERNAME \
  -e TEST_ROOM_ID=$TEST_ROOM_ID \
  -e FRONTEND_ORIGIN=$FRONTEND_ORIGIN \
  "$TARGET"
