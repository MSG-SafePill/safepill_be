# SafePill Backend

SafePill 백엔드 API 서버입니다. Spring Boot, PostgreSQL, JPA 기반으로 회원 인증, 의약품/영양제 검색, 내 약장, 병용금기 분석 기능을 제공합니다.

## 필요 환경

- Java 17
- PostgreSQL
- Gradle Wrapper 사용

## 로컬 설정

1. PostgreSQL에 `safepill` 데이터베이스를 생성합니다.

```sql
CREATE DATABASE safepill;
```

2. `src/main/resources/application-template.properties`를 참고해 `application.properties`를 생성합니다.

주의: `application.properties`에는 DB 비밀번호와 API 키가 들어가므로 Git에 커밋하지 않습니다.

## 실행

```powershell
.\gradlew.bat bootRun
```

## 검증 순서

1. 서버 실행 확인
2. 회원가입 API 호출
3. 로그인 API 호출 및 JWT 발급 확인
4. 의약품/영양제 데이터 동기화
5. 검색 API 확인
6. 내 약장 등록/조회 확인
7. 병용금기 분석 확인

## MVP API 흐름

- `POST /api/users/signup`: 회원가입
- `POST /api/users/login`: 로그인, JWT 발급
- `GET /api/search?keyword={keyword}`: 의약품/영양제 검색
- `POST /api/mypills`: 내 약장 등록
- `GET /api/mypills`: 내 약장 조회
- `DELETE /api/mypills/{regId}`: 내 약장 삭제
- `POST /api/interactions/analyze`: 등록 약물 병용금기 분석

