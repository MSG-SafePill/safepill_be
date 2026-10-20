# 팀원 B: 성분 주의 가이드

## 구현 범위

마이약장 `GET /api/mypills` 응답의 각 항목에 `ingredientGuide`를 추가했다.
의약품과 영양제 모두 저장된 성분 목록으로 판정한다.
가이드는 `medicine/guide/IngredientGuideService.java`의 버전 관리되는 고정 목록이다.
DB 스키마·기존 성분·사용자 약장 데이터 변경 및 외부 AI 호출은 없다.
팀원 A와 기준을 합의하면 이 서비스의 명시적 별칭 연결을 변경하면 된다.

| 성분 | 분류 | 공식 근거 |
|---|---|---|
| 칼슘 | 과다섭취 주의 | https://ods.od.nih.gov/factsheets/Calcium-Consumer/ |
| 비타민 C | 과다섭취 주의 | https://ods.od.nih.gov/factsheets/VitaminC-Consumer/ |
| 아세트아미노펜 | 중복복용·간손상 주의 | https://www.fda.gov/drugs/safe-use-over-counter-pain-relievers-and-fever-reducers/acetaminophen |
| 마그네슘 | 과다섭취 주의 | https://ods.od.nih.gov/factsheets/Magnesium-Consumer/ |
| 모르핀 | 의존·호흡억제 주의 | https://medlineplus.gov/druginfo/meds/a682133.html |

자료 확인일: 2026-10-20. 위 자료를 한국어로 요약한 일반 교육용 안내다.
국내 개별 제품 허가사항을 검증한 안내나 개인별 복약 판단이 아니다.
투여 경로·연령·제형별 용량은 제시하지 않는다.
등록 여부로 오남용·의존·중복복용 발생을 진단하지 않는다.
함량 합산, 원소량 환산, 복용 기록 기반 판정, 푸시 알림은 이번 범위에 포함하지 않는다.

## 연결 규칙

- 성분 ID 하드코딩 없이 성분명과 명시적 별칭을 정확히 비교한다.
- 유니코드 NFKC, 대소문자, 공백만 정규화한다.
- 제품명, 부분 문자열, 괄호 제거, 숫자/함량 제거를 이용한 추측 매칭은 하지 않는다.
- 칼슘이 포함된 다른 의약품 염, 스테아르산마그네슘 같은 첨가제를 영양성분으로 오인하지 않는다.
- 같은 가이드의 여러 별칭은 약품 한 개 내에서 한 번만 반환한다.
- 복합제는 연결된 가이드를 모두 반환하고 미확인 성분도 함께 표시한다.
- `AVAILABLE`은 입력된 모든 성분이 이 목록에 연결되었다는 뜻이며 모든 위험을 검사했다는 뜻이 아니다.

## 응답 예

```json
{
  "ingredientGuide": {
    "status": "PARTIAL",
    "warnings": [{
      "code": "ACETAMINOPHEN",
      "ingredientName": "아세트아미노펜",
      "riskType": "DUPLICATE_USE",
      "title": "중복복용·간손상 주의",
      "summary": "감기약·진통제에 같은 성분이 있는지 확인하세요.",
      "detail": "실제 응답은 카탈로그의 전체 안내 문구를 포함합니다.",
      "sourceName": "FDA · Acetaminophen",
      "sourceUrl": "https://www.fda.gov/drugs/safe-use-over-counter-pain-relievers-and-fever-reducers/acetaminophen",
      "reviewedAt": "2026-10-20"
    }],
    "unmatchedIngredients": ["카페인"]
  }
}
```

상태: `AVAILABLE`, `PARTIAL`, `NO_REGISTERED_GUIDE`, `NO_INGREDIENT_DATA`.
구버전 서버가 필드를 보내지 않으면 Flutter는 `UNAVAILABLE`로 처리한다.
어느 상태도 안전 판정으로 표현하지 않는다.

## 실제 로컬 DB 확인 결과

2026-10-20, 읽기 전용 조회. ID는 현황 기록이며 코드에서 사용하지 않는다.

| DB 성분명 | 현재 ID | 의약품 연결 | 영양제 연결 |
|---|---:|---:|---:|
| 칼슘 | 175 | 0 | 2 |
| 비타민C | 82 | 0 | 15 |
| 비타민 C | 90 | 0 | 1 |
| 아세트아미노펜 | 948 | 1 | 0 |
| 마그네슘 | 349 | 0 | 1 |
| 산화마그네슘 | 178 | 0 | 3 |
| 모르핀 | 650 | 0 | 0 |

모르핀은 연결 약품이 없어 실제 약장 시연 전 검증된 약품-성분 연결이 필요하다.
실제 약품에 임의로 모르핀을 연결하거나 사용자 약장을 수정하지 않았다.
모르핀 가이드 분기는 합성 입력으로 자동 테스트한다.

`비타민C 0.4)`, `칼슘함량 25%이상)` 등 손상되거나 함량이 섞인 성분명도 있다.
이름만 보고 자동 연결하지 않으며 팀원 A에게 원본과의 대조·정규화를 요청해야 한다.
지원하지 않는 염·원료명은 `unmatchedIngredients`로 표시되므로 명시적 별칭 추가 전에 원본을 확인한다.

## 통합 시연 확인

1. 백엔드를 재시작하고 Flutter를 다시 실행한다.
2. 위 성분과 연결된 약품·영양제를 검색해서 마이약장에 등록한다.
3. 카드의 `성분 주의 N건`을 펼쳐 유형·문구·출처·확인일을 확인한다.
4. 복합제는 여러 가이드 및 미확인 성분을 함께 표시하는지 확인한다.
5. 미등록 성분과 성분 데이터가 없는 약품의 상태를 구분한다.
6. 삭제 후 해당 카드가 사라지고 재조회 결과에도 남지 않는지 확인한다.
7. 실제 모르핀 시연은 팀원 A와 검증된 제품 연결을 확보한 후 수행한다.

자동 테스트:

```powershell
# safepill_be
.\gradlew.bat --no-daemon --gradle-user-home .gradle test
# safepill_fe
flutter test --no-pub test/ingredient_guide_test.dart
```

## 이번 검증 결과

- 백엔드 전체 8개 테스트 통과: 기존 애플리케이션 로딩 1개, 가이드 판정 6개,
  약장 서비스·HTTP 응답 직렬화·재조회 1개.
- Flutter 신규 테스트 4개 통과: API 파싱, 구버전 응답, 가이드 펼침/출처/부분 연결, 성분 정보 누락.
- 변경한 Flutter 파일과 테스트 5개 정적 분석 오류 없음.
- 실제 PostgreSQL은 성분·약품 연결 현황만 읽기 전용으로 확인했다.
- 앱을 직접 실행해 로그인·등록·삭제까지 수행하는 수동 통합 시연은 아직 수행하지 않았다.
