#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
if [[ -n "${TEST_PHONE:-}" ]]; then
  PHONE="${TEST_PHONE}"
else
  RAND=$(( (RANDOM % 9000) + 1000 ))
  PHONE="1880000${RAND}"
fi

log() {
  printf "\n[%s] %s\n" "$(date +%H:%M:%S)" "$1"
}

json_get() {
  local key="$1"
  python3 -c "import json,sys; raw=sys.stdin.read().strip(); data=json.loads(raw) if raw else {}; data=data.get('data',data) if isinstance(data,dict) else {}; print(data.get('$key','') if isinstance(data,dict) else '')"
}

request() {
  local method="$1"
  local path="$2"
  local data="${3:-}"
  if [[ "$method" == "GET" ]]; then
    curl -s "${BASE_URL}${path}"
  else
    curl -s -H "Content-Type: application/json" -X "$method" -d "$data" "${BASE_URL}${path}"
  fi
}

auth_request() {
  local method="$1"
  local path="$2"
  local data="${3:-}"
  if [[ "$method" == "GET" ]]; then
    curl -s -H "Authorization: Bearer ${TOKEN}" "${BASE_URL}${path}"
  else
    curl -s -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -X "$method" -d "$data" "${BASE_URL}${path}"
  fi
}

log "1. 健康检查"
request GET "/api/health" | tee /tmp/health.json

log "2. 获取验证码"
CAPTCHA_JSON=$(request GET "/api/auth/captcha")
echo "$CAPTCHA_JSON" > /tmp/captcha.json
CAPTCHA_KEY=$(printf "%s" "$CAPTCHA_JSON" | python3 -c "import json,sys;print(json.load(sys.stdin).get('key',''))")

log "3. 发送短信验证码（sms/send 无需图形验证码）"
SMS_JSON=$(request POST "/api/auth/sms/send" "{\"phone\":\"${PHONE}\"}")
if [[ -z "$SMS_JSON" ]]; then
  log "短信发送接口无响应，检查服务是否可访问"
  exit 1
fi
echo "$SMS_JSON" > /tmp/sms.json
DEV_CODE=$(printf "%s" "$SMS_JSON" | json_get devCode)

if [[ -z "$DEV_CODE" ]]; then
  log "短信接口未返回 devCode（可能已开启真实短信），跳过登录测试"
  exit 0
fi

log "4. 短信登录获取 token"
LOGIN_JSON=$(request POST "/api/auth/sms/login" "{\"phone\":\"${PHONE}\",\"code\":\"${DEV_CODE}\"}")
echo "$LOGIN_JSON" > /tmp/login.json
TOKEN=$(printf "%s" "$LOGIN_JSON" | json_get token)
USER_ID=$(printf "%s" "$LOGIN_JSON" | json_get userId)

if [[ -z "$TOKEN" ]]; then
  log "登录未返回 token，停止后续测试"
  exit 1
fi

log "5. 获取个人资料"
auth_request GET "/api/user/profile?userId=${USER_ID}" | tee /tmp/profile.json

log "6. 统计概览"
auth_request GET "/api/statistics/overview?userId=${USER_ID}&period=day" | tee /tmp/overview.json

log "7. 添加运动/步数/饮食/睡眠/体重/血压"
auth_request POST "/api/werun/add" "{\"userId\":${USER_ID},\"steps\":8200,\"date\":\"2026-03-14\"}"
auth_request POST "/api/exercise/add" "{\"userId\":${USER_ID},\"type\":\"步行\",\"steps\":5200,\"duration\":40,\"calories\":280,\"date\":\"2026-03-14\"}"
auth_request POST "/api/diet/add" "{\"userId\":${USER_ID},\"mealType\":\"午餐\",\"foodName\":\"清淡套餐\",\"calories\":520,\"date\":\"2026-03-14\"}"
auth_request POST "/api/sleep/add" "{\"userId\":${USER_ID},\"startTime\":\"2026-03-13T22:30:00\",\"endTime\":\"2026-03-14T06:30:00\",\"quality\":\"良\"}"
auth_request POST "/api/weight/add" "{\"userId\":${USER_ID},\"weight\":62.5,\"bmi\":21.3,\"date\":\"2026-03-14\"}"
auth_request POST "/api/health-record/add" "{\"userId\":${USER_ID},\"systolic\":120,\"diastolic\":80,\"heartRate\":72,\"date\":\"2026-03-14\"}"

log "8. 记录汇总"
auth_request GET "/api/records/summary?userId=${USER_ID}" | tee /tmp/records.json

log "9. 目标列表/新增目标"
auth_request POST "/api/goal/add" "{\"userId\":${USER_ID},\"goalType\":\"步数\",\"targetValue\":10000,\"period\":\"daily\"}"
auth_request GET "/api/goal/list?userId=${USER_ID}" | tee /tmp/goals.json

log "10. 提醒新增/列表"
auth_request POST "/api/reminder/add" "{\"title\":\"步数提醒\",\"type\":\"运动\",\"content\":\"今晚补足步数\",\"remindTime\":\"2026-03-14 20:30\"}"
auth_request GET "/api/reminder/list?userId=${USER_ID}" | tee /tmp/reminders.json

log "11. 药物新增/列表"
auth_request POST "/api/medication/add" "{\"userId\":${USER_ID},\"drugName\":\"维生素D\",\"dosage\":\"1粒\",\"frequency\":\"每周 1 次\",\"remindTime\":\"周日 09:00\",\"stock\":30,\"stockThreshold\":5,\"startDate\":\"2026-03-14\"}"
auth_request GET "/api/medication/list?userId=${USER_ID}" | tee /tmp/meds.json

log "12. 家庭成员新增/列表"
auth_request POST "/api/family/add" "{\"userId\":${USER_ID},\"name\":\"李阿姨\",\"relation\":\"母亲\",\"age\":68,\"conditionText\":\"高血压\",\"role\":\"成员\",\"status\":\"已授权\"}"
auth_request GET "/api/family/list?userId=${USER_ID}" | tee /tmp/family.json

log "13. 授权设置获取/更新"
auth_request GET "/api/privacy/get?userId=${USER_ID}" | tee /tmp/privacy.json
auth_request POST "/api/privacy/update" "{\"userId\":${USER_ID},\"allowCloudSync\":1,\"allowFamilyShare\":1}"

log "14. 报告接口"
auth_request GET "/api/reports/summary?userId=${USER_ID}" | tee /tmp/reports-summary.json
auth_request GET "/api/reports/list?userId=${USER_ID}" | tee /tmp/reports-list.json

log "测试完成"
