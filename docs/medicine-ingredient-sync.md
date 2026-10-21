# 의약품 성분 데이터 복구와 대체약 추천 (팀원 A)

## 왜 했나
- 공공데이터 성분 상세 API(`getDrugPrdtMcpnDtlInq07`)는 `item_seq` 필터를 무시하고 항상 같은 첫 페이지만 돌려준다.
  이 API로 동기화한 `medicine_ingredient`는 서로 무관한 약에 같은 성분이 수십 개씩 붙어 오염됐고, 정리 후 성분이 있는 약은 3개뿐이었다.
- 필터가 정상 동작하는 **허가정보 상세 API(`getDrugPrdtPrmsnDtlInq06`)** 에 성분(`MATERIAL_NAME`: 성분명·분량·단위),
  허가 상태(`CANCEL_NAME`, `CANCEL_DATE`), ATC 코드(`ATC_CODE`)가 모두 들어 있어 `MedicineService.fetchAndSaveIngredients`를 이 API로 교체했다.
- 이 API에 없는 품목은 식약처 의약품안전나라의 "원료약품 및 분량" 표(공개 페이지)로 보완했다.

## 내 DB에 적용하는 순서
1. 백엔드를 한 번 실행한다(`ddl-auto=update`가 `medicine_master`에 `cancel_name`, `cancel_date`, `atc_code` 컬럼을 추가한다).
2. `docs/merge-duplicate-ingredients.sql` — 같은 이름의 중복 성분 병합(이소트레티노인, 클로르페니라민말레산염).
3. `POST /api/medicines/sync-ingredients` — 약 9분 정도 걸리며 응답이 오기 전에 curl이 끊겨도 서버는 끝까지 처리한다.
4. `docs/seed-medicine-ingredients-nedrug.sql` — 허가정보 API에 없던 183개 약의 성분·함량·허가 상태.
5. `docs/seed-medicine-permit-nedrug.sql` — 제품명으로만 성분을 채웠던 33개 약의 함량·허가 상태.
6. `docs/fix-ingredient-units.sql` — 단위 보정.

```bash
docker exec -i safepill-postgres psql -U postgres -d safepill -v ON_ERROR_STOP=1 < docs/merge-duplicate-ingredients.sql
```
SQL은 모두 다시 실행해도 안전하다. 순서를 바꾸지 말고, 원래 오염된 API(`getDrugPrdtMcpnDtlInq07`)는 더 이상 쓰지 않는다.

## 결과 (2026-10-22, 로컬 DB)
- 성분이 있는 약 3개 → **502개 전부**, 성분 연결 1,607건(함량 1,377건), 성분 1,265종.
- 허가 상태: 정상 275 / 취하 157 / 유효기간만료 64 / 폐업 3 / 행정(취소) 3. **DB의 약 중 45%는 현재 허가가 끝난 제품**이다.
  써스펜데이정은 2023-12-07 취하, 한신타미놀캅셀은 2019-01-01 유효기간만료.

## 대체약 추천 로직 (`MedicineAlternativeService`)
1. 같은 성분의 약(`SAME_INGREDIENT`) → 주성분이 일부 같은 약(`PARTIAL_INGREDIENT`, 최대 5개) → 같은 약효군 ATC(`SAME_EFFICACY_GROUP`, 후보가 3개 미만일 때만; ATC가 `X`로 끝나는 '기타' 분류는 제외).
2. DB 후보가 하나도 없을 때만 Gemini 추천(`AI_SUGGESTED`). 추천 이름은 DB로 실존·허가 여부를 대조하고 기준 약 자신과 거의 같은 이름은 버린다.
3. 허가가 끝난 약, `(수출용)` 제품, 이미 마이약장에 있는 약은 후보에서 뺀다.
4. 마이약장과의 병용 주의 규칙 + 같은 성분이 겹치는 중복 복용을 `conflictReasons`로 알린다(교체 대상인 기준 약 자신과의 비교는 제외).
5. 같은 성분이어도 함량이 다르면 `dosageDiffers`로 표시한다.

## 한계 (발표·시연 때 정직하게)
- 성분명 일부는 식약처 원료 규격 표기(`베타카로틴30%`, `…건조엑스(5→1)`)를 그대로 쓴다. 서로 다른 제품의 같은 원료가 다른 이름으로 들어가 있을 수 있다.
- 대체약은 성분·함량 일치 기준의 **참고 정보**이며 의사·약사 확인을 대체하지 않는다.
- Gemini API 선결제 크레딧이 소진돼(402) AI 추천은 지금 동작하지 않는다. 이 경우 화면은 "AI 추천 서비스에 연결되지 않아요"라고 안내한다.

출처: 식품의약품안전처 의약품 제품 허가정보(공공데이터포털), 의약품안전나라(nedrug.mfds.go.kr). 수집일 2026-10-21.
