# 작업 요약 (2026-03-01, 추가)

## 추가 작업 요약 (헤더 체험공고 이동 + 상세 페이지 동적화)

### 요청 반영
- 헤더 드롭다운의 `체험 정보`, `체험 공고` 클릭 시 `/experience/list`로 이동
- 체험공고 상세 진입 경로를 `/experience/training-program/{id}` 기준으로 통일
- 상세 페이지의 하드코딩 표시값을 제거하고, 프로그램 등록 데이터 기반으로 JS 바인딩
- **HTML/CSS는 변경하지 않고 JS + 백엔드만 수정**

### 수정 파일 (이번 요청 범위)
- `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
  - `/experience/program/{id}` → `/experience/training-program/{id}` 리다이렉트
  - `/experience/training-program/{id}` 상세 뷰 제공
  - `/experience/training-program?id=...` 지원
  - `GET /experience/program/{id}/detail-data` JSON API 추가
    - `program`, `isLoggedIn`, `canApply`, `hasApplied`, `memberPhone`, `memberEmail` 반환
  - 기존 `/experience/detail?id=...`도 `/experience/training-program/{id}`로 통일
- `src/main/resources/static/js/qna/header-event.js`
  - 헤더/드롭다운의 `체험 정보`, `체험 공고` 링크를 `/experience/list`로 통일
- `src/main/resources/static/js/experience/training-program.js`
  - 상세 데이터 API fetch 기반 렌더링으로 재작성
  - 상단 회사명/제목/모달 제목/직무/마감일/근무시간/상세영역(`detail-content`) 동적 반영
  - `즉시 지원`/`지원하기`를 `/api/apply`와 연결
  - 로그인/회원유형/기지원 여부에 따른 버튼 상태 제어

### 동작 정리
- 목록 이동: 헤더 체험 메뉴 어디서 눌러도 `/experience/list`
- 상세 진입: `/experience/program/{id}` 요청 시 상세 표준 경로로 리다이렉트
- 상세 데이터: 하드코딩 문구 대신 등록된 프로그램 데이터로 표시
- 지원 동작: 실제 지원 API 호출 후 성공 시 `지원 완료` 상태 반영

### 검증 결과
- 컴파일:
  - `./gradlew.bat compileJava --no-daemon`
  - 결과: `BUILD SUCCESSFUL`
- 실행 확인:
  - `/experience/list` → `200`
  - `/experience/training-program/5001` → `200`
  - `/experience/program/5001/detail-data` → `200`
  - `/experience/program/5001` → `302` 리다이렉트 확인

### 경로 정정 메모
- `main/list` 페이지는 없음
- 실제 메인 페이지 경로는 `/main/main`

# 작업 요약 (2026-03-01)

## 추가 작업 요약 (experience 페이징/필터 재구현)

### 요청 반영
- `code_summary.md` 확인 후, 기존 레이어 구조(Controller → Service → DAO → Mapper) 유지
- `experience/list`의 필터/정렬/검색/페이지네이션이 실제 DB 쿼리와 연동되도록 재구현
- 변경 내용 본 파일(`summary.md`)에 기록

### 수정 파일
- `src/main/java/com/app/trycatch/common/pagination/Criteria.java`
  - `offset`, `endPage` 계산 시 원본 `page` 대신 보정된 `this.page` 사용
  - `page <= 0` 입력 시 음수 offset이 발생하던 문제 수정
- `src/main/java/com/app/trycatch/service/experience/ExperienceProgramService.java`
  - `status/sort/keyword/job` 정규화 로직 추가
  - 허용 상태: `all/recruiting/draft/closed/cancelled`
  - 허용 정렬: `latest/views/deadline`
  - 총 페이지를 초과한 `page` 요청 시 마지막 페이지로 보정
- `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
  - 요청 파라미터 정규화 로직 추가
  - 화면 모델(`status`, `sort`, `keyword`, `job`)과 실제 조회조건 일치 보장
- `src/main/resources/templates/experience/list.html`
  - 서버 렌더링 기반 목록 템플릿으로 재구성
  - 필터(상태/직무/정렬/검색어), 카드 목록, 빈 결과, 페이지네이션 구현
  - 페이지 이동 시 필터 파라미터 유지
- `src/main/resources/static/js/experience/list.js`
  - 필터 변경/검색/초기화/페이지 버튼 동작 구현
  - 폼 기반 GET 요청으로 항상 서버 페이징 결과와 동기화

### 구현 결과
- 필터 처리
  - 상태 + 직무 + 검색어 + 정렬 조합 검색 가능
  - 검색/필터 변경 시 페이지를 1로 초기화
- 페이징 처리
  - 처음/이전/번호/다음/마지막 이동 지원
  - 필터 상태를 유지한 채 페이지 이동
  - 비정상 page 파라미터(0 이하, 최대 페이지 초과) 보정

## 이번 세션 목적
- `main`, `experience/list`, `experience/training-program` 전체 로직 점검 및 실제 동작 반영
- 페이징/검색/정렬(조회수순, 최신순, 마감임박순) 적용 상태를 코드/DB 기준으로 검증
- 메인 체험공고를 정적 하드코딩이 아닌 체험공고 데이터 기반으로 전환
- 테스트 가능한 더미 체험공고 데이터 삽입

## 코드 변경 사항

### 1) 메인 페이지 체험공고 로직
- `src/main/java/com/app/trycatch/controller/member/MemberController.java`
  - `popularPrograms`, `latestPrograms` 모델 전달
  - 기존 호환을 위해 `featuredPrograms`도 `popularPrograms`로 유지
- `src/main/java/com/app/trycatch/service/main/MainHomeService.java`
  - `getPopularPrograms(int limit)` 추가
  - `getLatestPrograms(int limit)` 추가
  - 파일 첨부 매핑 공통화 `attachProgramFiles(...)` 추가
- `src/main/java/com/app/trycatch/repository/mypage/ExperienceProgramRankDAO.java`
  - `findTopByUpdatedDatetime(int limit)` 추가
- `src/main/java/com/app/trycatch/mapper/mypage/ExperienceProgramRankMapper.java`
  - `selectTopByUpdatedDatetime(@Param("limit") int limit)` 추가
- `src/main/resources/mapper/mypage/experienceProgramRankMapper.xml`
  - 최신 정렬 SQL 추가/정비 (`order by ep.updated_datetime desc, ep.id desc`)
- `src/main/resources/templates/main/main.html`
  - 체험공고 하드코딩 블록 제거
  - `인기공고(조회수)` / `최신공고(업데이트순)` 동적 렌더링 적용

### 2) 체험공고 리스트 페이지 로직
- `src/main/resources/templates/experience/list.html`
  - 서버 렌더링 기준으로 재구성
  - `status`, `job`, `sort`, `keyword` 검색 폼 반영
  - `programWithPaging.programs` 리스트 렌더링
  - 페이지네이션(`startPage~endPage`, 이전/다음) 반영
  - 카드에 직무/조회수/마감일 및 파일 썸네일 연동

### 3) 체험공고 상세 페이지 로직
- `src/main/resources/templates/experience/training-program.html`
  - 정적 더미 페이지 제거
  - `program`, `canApply`, `hasApplied`, `loginMember` 기반 조건 렌더링 적용
  - 즉시지원/이미지원/로그인유도/권한없음 버튼 분기 정상화

## DB 더미 데이터 반영 (로컬)
- `tbl_experience_program`에 테스트 공고 11건 삽입
  - `recruiting` 9건, `closed` 1건, `cancelled` 1건
  - 조회수/업데이트시간/마감일 분포를 다르게 구성하여 정렬 검증 가능

## 검증 결과

### 1) 빌드
- 실행:
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava --no-daemon
```
- 결과: `BUILD SUCCESSFUL`

### 2) 정렬/검색 검증 (DB 쿼리)
- 조회수순 상위 예시:
  - `DevOps 운영 체험 프로그램(140)`
  - `AI 모델 서빙 체험 프로그램(132)`
  - `백엔드 실무 체험 프로그램(120)`
- 최신(업데이트)순 상위 예시:
  - `보안 취약점 점검 체험`
  - `AI 모델 서빙 체험 프로그램`
  - `모바일 앱 QA 체험`
- 마감임박순:
  - `2026-03-08`, `2026-03-09`, `2026-03-11` ... 순으로 정상 확인
- 검색 검증:
  - 제목/회사명 키워드 매칭 및 직무 키워드(`백엔드`) 매칭 확인

## 비고
- 이번 세션에서 DB 더미 데이터는 로컬 DB에 직접 반영됨 (Git 추적 대상 아님)
- 화면 상세 UX/스타일은 기존 CSS 구조를 최대한 유지하면서 서버 데이터 연결을 우선 적용

---

# 작업 요약 (2026-02-24)

## 이번 세션 목적
- `src/mypage/notification`과 인터셉터 기반 헤더 알림(`individualArlam`)만 범위를 제한해 코드 점검
- 수정 작업은 하지 않고, 실제 장애 가능성이 있는 문제를 구조화해서 도출

## 이번 세션에서 확인한 파일(핵심)

### 1) mypage/notification
- `src/main/resources/templates/mypage/notification.html`
- `src/main/resources/static/js/mypage/notification/service.js`
- `src/main/resources/static/js/mypage/notification/event.js`
- `src/main/resources/static/js/mypage/notification/layout.js`
- `src/main/java/com/app/trycatch/controller/mypage/MyPageController.java`
- `src/main/java/com/app/trycatch/service/mypage/MyPageService.java`
- `src/main/java/com/app/trycatch/repository/mypage/MyPageDAO.java`
- `src/main/java/com/app/trycatch/mapper/mypage/MyPageMapper.java`
- `src/main/resources/mapper/mypage/myPageMapper.xml`
- `src/main/java/com/app/trycatch/dto/mypage/MyPageNotificationDTO.java`
- `src/main/java/com/app/trycatch/domain/mypage/MainNotificationVO.java`

### 2) interceptor + header individualArlam
- `src/main/java/com/app/trycatch/config/InterceptorConfig.java`
- `src/main/java/com/app/trycatch/interceptor/IndividualAlramInterceptor.java`
- `src/main/java/com/app/trycatch/controller/alarm/IndividualAlramController.java`
- `src/main/java/com/app/trycatch/service/Alarm/IndividualAlramService.java`
- `src/main/java/com/app/trycatch/repository/alarm/IndividualAlramDAO.java`
- `src/main/java/com/app/trycatch/mapper/alarm/IndividualAlramMapper.java`
- `src/main/resources/mapper/alarm/individualAlramMapper.xml`
- `src/main/java/com/app/trycatch/dto/alarm/AlramDTO.java`
- `src/main/resources/templates/qna/header.html`
- `src/main/resources/static/js/qna/header-event.js`
- `src/main/resources/static/css/qna/header.css`

## 초기 점검 결과 (분석 단계 기록)

### [High] 헤더 알림 최신순 정렬 깨짐
- 증상:
  - 헤더 개인 알림이 전체 시간순이 아니라 `qna -> experience_apply -> skill_log` 순으로 묶여 표시됨
- 원인:
  - 인터셉터에서 타입별로 3개 리스트를 조회해서 템플릿에서 순차 출력
- 영향:
  - 실제 최신 알림이 아래쪽에 밀려 UX 왜곡 발생
- 권장 조치:
  - 단일 리스트(전체 unread + `created_datetime desc`)로 조회/전달 후 템플릿도 단일 루프로 통합

### [Medium] 알림 벨 클릭 시 서버 실패와 무관하게 빨간 점 숨김
- 증상:
  - `fetch("/api/alarm/read", { method: "PUT" })` 성공 여부 확인 없이 dot 숨김
- 영향:
  - 서버 실패 시 UI와 DB 상태 불일치
- 권장 조치:
  - `await fetch` + `response.ok` 검증 후 dot 숨김
  - 실패 시 dot 복구/오류 처리

### [Medium] `createdDatetime` 파싱 포맷 고정으로 렌더링 실패 가능
- 증상:
  - `MyPageNotificationDTO`에서 `yyyy-MM-dd HH:mm:ss` 포맷 하드코딩 파싱
- 영향:
  - DB/매핑 값이 미세하게 달라지면 템플릿 렌더링 시 예외 발생 가능
- 권장 조치:
  - DTO 필드를 `LocalDateTime`으로 매핑하거나
  - 다중 포맷 허용 + fallback 처리

## 이번 세션 반영 결과 (2026-02-24 업데이트)
### 완료된 작업
1. 헤더 개인 알림 단일 리스트 전환 완료
   - `individualAlramMapper.xml`에 `selectUnreadAlramsByMemberId` 통합 조회 추가
   - `IndividualAlramMapper/DAO/Service/Interceptor`를 단일 리스트 전달 구조로 변경
   - `qna/header.html`을 `individualAlrams` 단일 `th:each` 렌더링으로 통합
2. `header-event.js` 읽음 처리 UI를 서버 응답 기반으로 변경 완료
   - `notifyButton` 클릭 로직을 `async/await`로 변경
   - `response.ok`일 때만 `js-alarmDot` 숨김
   - 실패/예외 시 dot 복구 및 콘솔 에러 출력
3. `MyPageNotificationDTO` 날짜 포맷 처리 안정화 완료
   - 단일 고정 포맷 파싱에서 다중 포맷 fallback 파싱으로 변경
   - 지원 순서: `yyyy-MM-dd HH:mm:ss` -> `ISO_LOCAL_DATE_TIME` -> `OffsetDateTime` -> 소수초 절삭 재파싱
   - 파싱 실패 시 날짜/시간 라벨은 `"-"` 반환

### 남은 체크리스트 (수동 확인)
1. 개인회원 헤더
   - 알림이 전체 최신순(`created_datetime desc`)으로 노출되는지 확인
   - 알림 벨 클릭 시 서버 성공/실패에 따라 빨간점 표시가 일치하는지 확인
2. `/mypage/notification`
   - 날짜/시간 라벨 렌더링 예외 없이 표시되는지 확인
   - 유형 필터, 읽음 버튼 동작 확인

### 빌드 검증 결과
- 실행 커맨드:
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`
- 비고: 기존 Lombok 경고만 출력 (신규 컴파일 에러 없음)

### 알림 테스트 데이터 삽입 (2026-02-24 22:43 KST)
- 대상: `tbl_main_notification`, `member_id = 1`
- 목적: 헤더 알림 단일 리스트 최신순 정렬/읽지 않음(dot) 동작 확인
- 삽입 데이터:
  - `[TEST][HEADER] QnA 최신 알림` (`qna`, `created_datetime = now() - 1 minute`)
  - `[TEST][HEADER] 체험지원 상태 알림` (`experience_apply`, `notification_content = document_pass`, `created_datetime = now() - 2 minute`)
  - `[TEST][HEADER] 기술블로그 알림` (`skill_log`, `created_datetime = now() - 3 minute`)
- DB 확인 쿼리:
```sql
select id, notification_type, notification_title, created_datetime
from tbl_main_notification
where member_id = 1
  and notification_is_read = false
  and notification_type in ('qna', 'experience_apply', 'skill_log')
order by created_datetime desc;
```
- 테스트 데이터 정리(삭제) 쿼리:
```sql
delete from tbl_main_notification
where member_id = 1
  and notification_title like '[TEST][HEADER]%';
```

### 카카오 회원가입 중복키 장애 대응 (2026-02-24 22:45~22:50 KST)
- 발생 에러:
  - `Duplicate entry '2' for key 'tbl_individual_member.PRIMARY'`
  - 선행 이슈로 `tbl_oauth` 중복키(`Duplicate entry '1'`)도 확인됨
- 확인된 DB 상태:
  - `tbl_member`는 0건인데 `tbl_individual_member`/`tbl_oauth`는 다수 데이터가 존재하는 비정상 정합성 상태
  - 이 상태에서 카카오 가입 POST가 신규 insert 경로를 타면 PK 충돌 가능

- 적용한 코드 수정:
1. `memberMapper.xml`
   - `insert into tbl_oauth`를 upsert로 변경
   - `on duplicate key update provider = values(provider)`
2. `individualMember.xml`
   - `insert into tbl_individual_member`를 upsert로 변경
   - `on duplicate key update individual_member_birth = values(...), individual_member_gender = values(...)`
3. `IndividualMemberService.kakaoJoin()`
   - `memberEmail` 기준 기존 회원이면 신규 가입 insert를 건너뛰고 `saveOauth()`만 수행 후 종료

- 추가 DB 보정:
  - `tbl_member` AUTO_INCREMENT를 `max(tbl_member.id, tbl_individual_member.id, tbl_oauth.id) + 1`로 상향 조정
  - 점검용 probe insert/rollback에서 다음 ID가 `36`임을 확인

- 검증:
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
  - 결과: `BUILD SUCCESSFUL`

### 마이페이지 랭킹 조회 SQL 오류 대응 (2026-02-24 22:50 KST)
- 발생 에러:
  - `Unknown column 'ep.experience_program_job' in 'field list'`
  - 대상 쿼리: `mapper/mypage/experienceProgramRankMapper.xml`의 `selectTopByViewCount`
- 원인:
  - 실 DB(`try_catch.tbl_experience_program`)에 `experience_program_job` 컬럼이 누락된 스키마 드리프트
  - 코드/매퍼/SQL 정의 파일은 해당 컬럼 사용을 전제로 작성되어 있음

- 조치:
```sql
alter table tbl_experience_program
    add column experience_program_job varchar(255) not null default '미분류' comment '프로그램 직무';
```
  - 컬럼 추가 후 동일 SELECT 쿼리 수동 실행 정상 확인 (결과 0건, 오류 없음)

- 참고:
  - `experience_program_job`은 마이페이지/체험지원현황/기업 공고관리 등 여러 화면에서 공통 사용 중이므로
    매퍼 우회 수정 대신 스키마 복구가 정합한 대응

### 마이페이지 탭 더보기 문구/알림 없음 확인 대응 (2026-02-24 22:55 KST)
- 요청 사항:
  - `/mypage/mypage` 하단 탭 영역의 버튼 문구를 `더보기`로 고정
  - 스크랩 공고 탭 클릭 후 최근 본 공고 탭 복귀 시 문구가 바뀌는 버그 수정
  - 개인회원 알림이 보이지 않는 상태 점검

- 적용한 코드 수정:
1. `src/main/resources/static/js/mypage/main/layout.js`
   - `switchTab()`에서 탭 전환 시 `moreLink.textContent`를 항상 `더보기`로 설정
   - 기존 `최근 본 공고 더보기`/`스크랩 공고 더보기`로 변경되던 동작 제거

- 알림 점검 및 테스트 데이터:
1. 원인 확인
   - 헤더 알림은 로그인한 `member_id` 기준 `notification_is_read = false` 데이터만 노출
   - 특정 계정(`member_id = 1`)만 테스트 데이터가 있던 상태에서는 다른 계정에서 빈 화면 발생 가능
2. 조치
   - `tbl_individual_member` 전체 사용자에게 테스트 알림 1건씩 삽입
   - 제목 패턴: `[TEST][ALARM-CHECK]`
3. 결과
   - `tbl_individual_member` 28명 기준, 테스트 알림 28건 삽입 확인
   - 회원별 unread 1건씩 조회 확인

- 테스트 데이터 정리(삭제) 쿼리:
```sql
delete from tbl_main_notification
where notification_title like '[TEST][ALARM-CHECK]%';
```

- 참고:
  - 알림 벨 클릭 시 현재 구현상 해당 회원의 unread 알림이 읽음 처리됨
  - 테스트 직후 벨을 클릭했다면 dot/리스트가 즉시 사라질 수 있음

## 다음 세션 빠른 진입용 커맨드
```powershell
rg -n "individualAlrams|js-alarmDot|/api/alarm/read|createdDateLabel|createdTimeLabel" src
rg -n "findUnreadAlrams|selectUnreadAlramsByMemberId|IndividualAlramInterceptor|individualAlramMapper" src/main/java src/main/resources/mapper
```

---

# 작업 요약 (2026-02-22)

## 1. 최근 본 공고 (Latest Watch Postings)

### 배경
체험공고 페이지가 미완성이므로, 나중에 실제 클릭 데이터를 연결할 수 있도록 로직만 선제적으로 구현.

### 생성된 파일
| 파일 | 경로 |
|---|---|
| `LatestWatchPostingVO.java` | `domain/mypage/` |
| `LatestWatchPostingDTO.java` | `dto/mypage/` |
| `LatestWatchPostingMapper.java` | `mapper/mypage/` |
| `latestWatchPostingMapper.xml` | `resources/mapper/mypage/` |
| `LatestWatchPostingDAO.java` | `repository/mypage/` |

### 수정된 파일
- `MyPageService.java` — `addLatestWatchPosting()`, `getLatestWatchPostings()` 추가
- `MyPageController.java` — `POST /mypage/latest-watch` 엔드포인트 추가, `goToMyPage()`에 `latestWatchPostings` 모델 추가
- `mypage.html` — `th:each="posting : ${latestWatchPostings}"` 적용

### DB 테이블
- `tbl_latest_watch_posting`: `member_id`, `experience_program_id` (ON DUPLICATE KEY UPDATE)

---

## 2. 스크랩 공고 (Scrap Postings)

### 생성된 파일
| 파일 | 경로 |
|---|---|
| `ScrapPostingVO.java` | `domain/mypage/` |
| `ScrapPostingDTO.java` | `dto/mypage/` |
| `ScrapPostingMapper.java` | `mapper/mypage/` |
| `scrapPostingMapper.xml` | `resources/mapper/mypage/` |
| `ScrapPostingDAO.java` | `repository/mypage/` |

### 수정된 파일
- `MyPageService.java` — `addScrap()`, `toggleScrap()`, `getScrapPostings()` 추가
- `MyPageController.java` — `POST /mypage/scrap`, `POST /mypage/scrap/toggle` 엔드포인트 추가
- `mypage.html` — `th:each="scrap : ${scrapPostings}"` 적용, 스크랩 버튼에 `th:classappend`, `th:data-scrap-id`, `th:data-scrap-status` 바인딩
- `js/mypage/main/layout.js` — `toggleScrap()` UI 업데이트 로직 추가 (`scrapStatus` 토글)
- `js/mypage/main/service.js` — `toggleScrap(scrapId, scrapStatus)` — `POST /mypage/scrap/toggle`
- `js/mypage/main/event.js` — 스크랩 버튼 클릭 이벤트 연결

### 동작 방식
- 버튼 클릭 시 UI 먼저 업데이트(낙관적 업데이트) → 서버 요청
- `Status` enum: `ACTIVE` ↔ `INACTIVE` 토글

---

## 3. 원픽 공고 / 공기업 체험공고 / TOP100 (ExperienceProgramRank)

### 배경
체험공고 페이지 미완성으로 조회수 기반 랭킹 로직만 선구현.

### 생성된 파일
| 파일 | 경로 |
|---|---|
| `ExperienceProgramRankDTO.java` | `dto/mypage/` |
| `ExperienceProgramRankMapper.java` | `mapper/mypage/` |
| `experienceProgramRankMapper.xml` | `resources/mapper/mypage/` |
| `ExperienceProgramRankDAO.java` | `repository/mypage/` |

### 수정된 파일
- `MyPageService.java` — `getTopPostings(int limit)`, `getTopPublicPostings(int limit)` 추가
- `MyPageController.java` — `goToMyPage()`에 아래 3개 모델 추가
  - `topPostings` (limit 10) → 원픽 공고 Swiper
  - `topPublicPostings` (limit 5) → 공기업 체험공고 랭킹
  - `top100Postings` (limit 5) → TOP100 랭킹
- `mypage.html` — 세 섹션의 하드코딩 `<li>` 제거, `th:each` 적용

### SQL 쿼리 전략
- `selectTopByViewCount`: `experience_program_status = 'recruiting'` 조건, 조회수 내림차순
- `selectTopPublicByViewCount`: 위 조건 + `corp_company_type = '공기업'` 추가 필터

---

## 4. 체험 지원 현황 페이지 (Apply List)

### 생성된 파일
| 파일 | 경로 |
|---|---|
| `ApplyListDTO.java` | `dto/mypage/` |
| `ApplyListMapper.java` | `mapper/mypage/` |
| `applyListMapper.xml` | `resources/mapper/mypage/` |
| `ApplyListDAO.java` | `repository/mypage/` |

### 수정된 파일
- `MyPageService.java` — `ApplyListDAO` 주입, `getApplyList(Long memberId)` 추가
- `MyPageController.java` — `goToExperience()`에서 `applies` 리스트 및 상태별 카운트 모델 전달
- `experience.html`
  - `<html>` 태그에 `xmlns:th="http://www.thymeleaf.org"` 추가
  - 상태 카운트 4개에 `th:text` 적용 (`appliedCount`, `documentPassCount`, `documentFailCount`)
  - 하드코딩 `<tr>` 4개 → `th:each="apply : ${applies}"` 단일 반복 템플릿으로 교체
  - 빈 상태 처리: `th:if="${#lists.isEmpty(applies)}"` 로 "내역 없음" 메시지 표시

### applyStatus 매핑
| DB 값 | 화면 표시 | 상단 카운트 |
|---|---|---|
| `applied` | 심사중 | `appliedCount` |
| `document_pass` | 참여중 | `documentPassCount` |
| `document_fail` | 심사완료 | `documentFailCount` |
| `cancelled` | (목록 제외) | — |

### SQL 쿼리
```sql
SELECT a.id AS applyId, a.experience_program_id, a.apply_status,
       a.created_datetime AS applyCreatedDatetime,
       ep.experience_program_title, ep.experience_program_job,
       ep.experience_program_deadline, ep.experience_program_status,
       c.corp_company_name
FROM tbl_apply a
LEFT JOIN tbl_experience_program ep ON a.experience_program_id = ep.id
LEFT JOIN tbl_corp c ON ep.corp_id = c.id
WHERE a.member_id = #{memberId}
  AND a.apply_status != 'cancelled'
ORDER BY a.created_datetime DESC
```

---

## 에러 및 해결 내역

### 1. Port 10000 already in use
- **원인**: 이전 DevTools 프로세스(PID 8744)가 포트를 점유 중
- **해결**: `powershell -Command "Stop-Process -Id 8744 -Force"` 실행 후 서버 재기동

### 2. Python 미설치 환경
- **원인**: Windows 환경에서 `python`, `python3` 명령 없음 (exit code 49)
- **해결**: Node.js로 대용 (파일 조작 스크립트)

### 3. Lombok 경고 (기존 코드)
- **내용**: `@EqualsAndHashCode(callSuper=false)` 미지정 경고 (`MemberVO`, `ExperienceProgramVO` 등)
- **원인**: 기존 코드에서 이미 존재하던 경고, 이번 작업과 무관
- **처리**: 수정 불필요 (빌드/런타임 영향 없음)

### 4. bootrun 로그 이중 프로세스 출력
- **원인**: DevTools가 기존 프로세스를 hot-reload로 재기동하면서 새 `bootRun` 프로세스와 로그가 혼재
- **확인 방법**: PID 기준으로 구분 — 신규 프로세스 에러는 포트 충돌뿐, 기존 프로세스(실제 서버)는 정상

---

## 서버 테스트 결과

| 엔드포인트 | 응답 코드 | 비고 |
|---|---|---|
| `GET /mypage/mypage` | 302 | 로그인 필요, 정상 리다이렉트 |
| `GET /mypage/experience` | 302 | 로그인 필요, 정상 리다이렉트 |
| `GET /mypage/notification` | 302 | 로그인 필요, 정상 리다이렉트 |
| `GET /mypage/change-my-information` | 302 | 로그인 필요, 정상 리다이렉트 |
| `GET /mypage/unsubscribe` | 302 | 로그인 필요, 정상 리다이렉트 |

> 모든 엔드포인트 컴파일 에러 없이 정상 기동 확인

---

## 추가 요청 반영 (2026-02-24 23:15 KST)

### 1) 최근 본 공고 `더보기` 문구 고정
- 대상 파일: `src/main/resources/static/js/mypage/main/layout.js`
- 조치:
  - 탭 전환(`switchTab`) 시 `moreLink.textContent`를
    `스크랩 공고 더보기`/`최근 본 공고 더보기`로 변경하던 로직 제거
  - 항상 `더보기`로 고정

### 2) `QnaController` 롤백
- 요청사항에 따라 `src/main/java/com/app/trycatch/controller/qna/QnaController.java`는
  본 세션에서 추가한 방어 코드 없이 원복 완료
- 현재 상태: 기존 코드 기준 유지

### 3) 알림 테스트 데이터 대량 삽입 (QnA / 체험지원현황 / Skill-log)
- 사전 상태:
  - `tbl_qna`, `tbl_skill_log`, `tbl_experience_program` 모두 0건 확인
  - 상세 이동 테스트를 위해 참조 가능한 QnA/Skill-log 테스트 원천 데이터 생성 필요
- 생성 데이터:
  - `tbl_qna` 테스트 6건 (`[TEST][ALARM-SEED] QnA ...`)
  - `tbl_skill_log` 테스트 6건 (`[TEST][ALARM-SEED] SkillLog ...`)
  - `tbl_main_notification` 알림 총 252건 삽입
    - `qna`: 84건
    - `experience_apply`: 84건
    - `skill_log`: 84건
- 검증 포인트:
  - `member_id = 1` 기준 각 타입 3건씩 존재
  - `qna` 알림은 `qna_id`가 채워진 상태
  - `skill_log` 알림은 `skill_log_id`가 채워진 상태

### 4) `/mypage/notification` 조회 SQL 점검
- 점검 파일: `src/main/resources/mapper/mypage/myPageMapper.xml`
- `selectNotificationsByMemberId` 확인 결과:
  - `notification_type`, `qna_id`, `skill_log_id` 포함 조회
  - `created_datetime desc` 정렬 적용
- 결론:
  - 현재 요청사항 기준으로 추가 SQL 수정 불필요

### 5) 테스트 데이터 정리(삭제) 쿼리
```sql
delete from tbl_main_notification
where notification_title like '[TEST][ALARM-SEED]%'
   or notification_title like '[TEST][ALARM-CHECK]%'
   or notification_title like '[TEST][HEADER]%';

delete from tbl_qna
where qna_title like '[TEST][ALARM-SEED]%';

delete from tbl_skill_log
where skill_log_title like '[TEST][ALARM-SEED]%';
```

### 6) 빌드 검증
- 실행:
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`

## 추가 요청 반영 (2026-02-24 23:20~23:30 KST) - 카카오 로그아웃 URI 이슈

### 1) 발생 증상
- 카카오 로그아웃 시 에러 페이지 노출
- 메시지: `등록하지 않은 로그아웃 리다이렉트 URI`
- 사용 URI: `http://localhost:10000/main/log-out`

### 2) 원인
- 카카오 개발자 콘솔에 등록되지 않은 `logout_redirect_uri`를 사용한 요청 발생
- 코드/템플릿에 카카오 로그아웃 URI가 분산되어 있어 경로 불일치가 재발하기 쉬운 상태

### 3) 코드 조치
- 카카오 로그아웃 리다이렉트 URI를 `http://localhost:10000/main/log-in`으로 통일
- 수정 파일:
  - `src/main/java/com/app/trycatch/controller/mypage/MyPageController.java`
  - `src/main/resources/templates/corporate/header.html`
  - `src/main/resources/templates/qna/header.html`
  - `src/main/resources/templates/experience/list.html`
  - `src/main/resources/templates/main/service-introduce.html`

### 4) 상태
- 사용자 확인: **카카오 로그아웃 해결 완료**

### 5) 운영 참고
- 카카오 앱 설정에서 **로그아웃 리다이렉트 URI** 목록을 코드와 동일하게 유지 필요
- URI 변경 시 템플릿/컨트롤러 하드코딩 값 동시 점검 권장

---

## 세션 인수인계 (2026-03-01) - Spring 강의 패턴 분석/적용 준비

## 이번 세션 목적
- `C:\Users\pigch\Desktop\gb_0090_kyc\Spring\강의자료`의 day1~day12 패턴을 분석해
  현재 프로젝트에서 재사용 가능한 기준을 만들기
- 다음 세션에서 바로 리팩터링을 시작할 수 있게 실행 체크리스트까지 준비하기

## 실제 분석 범위
- 요청 범위: day1~day12
- 실제 존재 폴더: `spring_day02` ~ `spring_day12`
- 참고: `spring_day01` 폴더는 존재하지 않음

## 이번 세션 산출물
1. 강의 코드 패턴 요약 문서 생성
   - 파일: `code_summary.md`
   - 핵심 내용:
     - 3-tier 구조(Controller -> Service -> DAO -> Mapper XML)
     - DTO/VO 분리, 변환 메서드 패턴
     - MyBatis 동적 SQL/페이징, `tbl_*` + snake_case 규칙
     - Thymeleaf + JS(service/layout/event) 분리 패턴
     - day별 진화 흐름(day02~day12)

2. 프로젝트 메모리 규칙 파일 생성
   - 파일: `.claude/rules/spring-lecture-patterns.md`
   - 내용: 앞으로 코드 작성 시 기본 Spring 스타일 규칙

3. 프로젝트 루트 메모리 연결 파일 생성
   - 파일: `CLAUDE.md`
   - 내용: 위 패턴 규칙 및 요약 문서 참조 정보

4. 전역 메모리 반영 완료
   - 파일: `C:\Users\pigch\.claude\rules\spring-lecture-patterns.md`
   - 목적: 다음 세션/다른 프로젝트에서도 동일 패턴 재사용

5. 실행형 리팩터링 체크리스트 작성
   - 파일: `refactoring_checklist.md`
   - 현재 코드베이스 맞춤 이슈 포함:
     - `Alram` 오탈자 정규화
     - `service/Alarm` 패키지 대소문자 정리
     - 중복 DTO/VO/DAO/Mapper 명 정리
     - Mapper 인터페이스-XML 네이밍/namespace 정합화
     - 트랜잭션/예외 처리/설정 클래스 정리

## 확인된 핵심 리팩터링 포인트
- `Alram` / `Alarm` 혼재 (타입명, 패키지명, mapper xml 전반)
- `PointDetails*` 계열 클래스가 `mypage`와 `point` 컨텍스트에 중복 존재
- `WebMvcConfig`(루트) + `InterceptorConfig`(config)로 설정 클래스 분산
- mapper xml 네이밍 규칙 일부 불일치
  - 예: `individualMember.xml` (일관 규칙상 `individualMemberMapper.xml` 권장)

## 다음 세션 즉시 실행 순서 (권장)
1. `Phase 0` 베이스라인 검증
   - `./gradlew clean test`
   - `./gradlew bootJar`
2. `Phase 1` 네이밍 정규화 우선 수행
   - `Alram -> Alarm`
   - `service.Alarm -> service.alarm`
3. 컴파일/테스트 재검증 후 커밋 분리
   - 네이밍 변경 커밋
   - 중복 모델 정리 커밋
   - mapper 정합화 커밋

## 주의사항
- 현재 워크트리는 기존 변경사항이 매우 많음.
- 다음 세션에서 리팩터링 시작 시, 반드시 작은 단위 커밋으로 분리하고
  `git reset --hard` 같은 파괴적 명령은 사용하지 말 것.

---

## 추가 요청 반영 (2026-03-01) - 체험 프로그램 목록 페이지 구현

### 1) 구현 범위
- 우선순위 요청에 따라 `체험 프로그램 목록`부터 구현
- 대상 라우트: `GET /experience/list`

### 2) 적용한 코드 변경
1. Controller/Service/DTO 추가
   - `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
   - `src/main/java/com/app/trycatch/service/experience/ExperienceProgramService.java`
   - `src/main/java/com/app/trycatch/dto/experience/ExperienceProgramListWithPagingDTO.java`
2. DAO/Mapper 확장
   - `src/main/java/com/app/trycatch/repository/experience/ExperienceProgramDAO.java`
     - `countPublic(status, keyword, job)`
     - `findPublic(criteria, status, keyword, job, sort)`
   - `src/main/java/com/app/trycatch/mapper/experience/ExperienceProgramMapper.java`
     - `countPublic(...)`, `selectPublic(...)` 시그니처 추가
   - `src/main/resources/mapper/experience/ExperienceProgramMapper.xml`
     - 공개 목록 필터 SQL (`status/keyword/job`) 추가
     - 공개 목록 개수/목록 조회 쿼리 추가
3. 목록 템플릿/스타일 교체
   - `src/main/resources/templates/experience/list.html`
     - Thymeleaf 기반 동적 렌더링으로 전환
     - 상태/정렬/직무/키워드 검색 폼 적용
     - 카드형 목록 + 페이징 링크 적용
   - `src/main/resources/static/css/experience/program-list.css`
     - 목록 전용 스타일 신규 추가

### 3) 동작 요약
- 필터:
  - 상태(`all/recruiting/draft/closed/cancelled`)
  - 정렬(`latest/views/deadline`)
  - 직무(`job`)
  - 키워드(`program title`, `corp company name`)
- 페이징:
  - 기존 `Criteria` 규칙 사용
  - 페이지 이동 시 필터 파라미터 유지
- 썸네일:
  - 프로그램 파일의 첫 번째 이미지를 대표 썸네일로 노출
  - 파일이 없으면 `NO IMAGE` 플레이스홀더 표시

### 4) 빌드 검증
- 실행:
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`
- 비고: 기존 Lombok 경고 2건만 출력(신규 컴파일 에러 없음)

### 5) 다음 작업(예정)
- 체험 프로그램 상세 페이지 구현
  - 라우트: `/experience/program/{id}`
  - 목록 카드 상세 링크와 데이터 연계 마무리

---

## 추가 요청 반영 (2026-03-01) - 체험 프로그램 상세 + 조회수/지원상태 고도화

### 1) 구현 범위
- 체험 프로그램 상세 페이지 구현
- 상세 진입 시 조회수 증가 처리
- 지원 버튼 상태를 서버 기준으로 분기(이미 지원 여부 반영)
- 중복 지원 방어 로직 추가

### 2) 적용한 코드 변경
1. 상세 페이지 라우트/조회 추가
   - `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
     - `GET /experience/program/{id}` 추가
     - 레거시 링크 호환용 `GET /experience/detail?id=...` -> 신규 경로 리다이렉트 추가
   - `src/main/java/com/app/trycatch/service/experience/ExperienceProgramService.java`
     - `getDetail(Long id)` 추가
     - 파일 목록 결합 조회 + 예외 처리(`ExperienceProgramNotFoundException`)
2. 상세 화면/정적 리소스 추가
   - `src/main/resources/templates/experience/detail.html`
   - `src/main/resources/static/css/experience/program-detail.css`
   - `src/main/resources/static/js/experience/program-detail.js`
3. 예외 처리 추가
   - `src/main/java/com/app/trycatch/common/exception/handler/ExperienceExceptionHandler.java`
   - 체험 프로그램 미존재 시 `/experience/list`로 리다이렉트
4. 메인 추천 링크 경로 정합화
   - `src/main/resources/templates/main/service-introduce.html`
   - 링크를 `/experience/program/{id}`로 변경

### 3) 조회수 증가 처리
- Mapper/DAO 확장:
  - `src/main/java/com/app/trycatch/mapper/experience/ExperienceProgramMapper.java`
    - `increaseViewCount(Long id)` 추가
  - `src/main/resources/mapper/experience/ExperienceProgramMapper.xml`
    - `experience_program_view_count = experience_program_view_count + 1` SQL 추가
  - `src/main/java/com/app/trycatch/repository/experience/ExperienceProgramDAO.java`
    - `increaseViewCount(Long id)` 추가
- 서비스 처리:
  - `ExperienceProgramService.getDetail()` 진입 시 조회수 증가 후 상세 조회 수행

### 4) 지원 상태 서버 반영 및 중복 지원 방어
1. 이미 지원 여부 조회 API 추가
   - `src/main/java/com/app/trycatch/mapper/experience/ApplyMapper.java`
     - `selectExistsByProgramIdAndMemberId(...)` 추가
   - `src/main/resources/mapper/experience/applyMapper.xml`
     - 존재 여부 `count(*)` 조회 SQL 추가
   - `src/main/java/com/app/trycatch/repository/experience/ApplyDAO.java`
     - `existsByProgramIdAndMemberId(...)` 추가
2. 상세 페이지 버튼 분기
   - `ExperienceProgramController`에서 `canApply`, `hasApplied` 모델 전달
   - `detail.html` 분기:
     - 개인회원 + 미지원: `즉시 지원`
     - 개인회원 + 기지원: `이미 지원한 프로그램입니다` (disabled)
     - 비로그인: `로그인 후 지원`
     - 기업회원 등 비개인회원: `개인회원만 지원 가능`
3. 중복 지원 API 방어
   - `src/main/java/com/app/trycatch/controller/experience/ApplyController.java`
   - 저장 전 `existsByProgramIdAndMemberId` 사전 체크
   - DB 유니크키 충돌(`DataIntegrityViolationException`)도 동일 메시지로 방어
     - 응답 메시지: `이미 지원한 프로그램입니다.`

### 5) 빌드 검증
- 실행:
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`

---

## 추가 요청 반영 (2026-03-01) - 체험 목록/상세 오류 디버깅(기동 장애 선해결)

### 1) 문제 배경
- 사용자 제보: 체험 목록/체험 상세에서 오류 발생
- 실제 재현 시점에는 `/experience/list`, `/experience/program/{id}` 진입 전,
  앱 컨텍스트 초기화 단계에서 기동 실패가 먼저 발생

### 2) 재현된 실제 오류(순차 확인)
1. `ConflictingBeanDefinitionException`
   - `PointDetailsDAO` 중복 빈 이름 충돌
   - 대상: `repository.point.PointDetailsDAO` vs `repository.mypage.PointDetailsDAO`
2. `ConflictingBeanDefinitionException`
   - `PointDetailsMapper` 중복 빈 이름 충돌
   - 대상: `mapper.point.PointDetailsMapper` vs `mapper.mypage.PointDetailsMapper`
3. MyBatis alias 충돌
   - `The alias 'PointDetailsDTO' is already mapped ...`
   - 결과적으로 `sqlSessionTemplate` 생성 실패 연쇄 발생

### 3) 적용한 수정
1. DAO 빈 이름 명시 분리
   - `src/main/java/com/app/trycatch/repository/point/PointDetailsDAO.java`
     - `@Repository("pointPointDetailsDAO")`
   - `src/main/java/com/app/trycatch/repository/mypage/PointDetailsDAO.java`
     - `@Repository("mypagePointDetailsDAO")`
2. Mapper 빈 이름 명시 분리
   - `src/main/java/com/app/trycatch/mapper/point/PointDetailsMapper.java`
     - `@Repository("pointPointDetailsMapper")` 추가
   - `src/main/java/com/app/trycatch/mapper/mypage/PointDetailsMapper.java`
     - `@Repository("mypagePointDetailsMapper")` 추가
3. mypage PointDetails 타입 alias 분리
   - `src/main/java/com/app/trycatch/dto/mypage/PointDetailsDTO.java`
     - `@Alias("MyPagePointDetailsDTO")` 추가
   - `src/main/java/com/app/trycatch/domain/mypage/PointDetailsVO.java`
     - `@Alias("MyPagePointDetailsVO")` 추가
   - `src/main/resources/mapper/mypage/pointDetailsMapper.xml`
     - `resultType="MyPagePointDetailsDTO"`로 변경

### 4) 검증 결과
1. 컴파일 검증
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`
2. 런타임 재현 검증
- `bootRun` 정상 기동 확인 (`Tomcat started on port 10000`)
- `GET /experience/list` -> `200`
- `GET /experience/program/1` -> `200` 확인
- 현재 테스트 DB 기준 목록 데이터는 비어 있어 empty state 렌더링(`cardCount=0`) 확인

### 5) 참고
- 이번 수정은 체험 목록/상세 경로 자체 버그라기보다, 해당 화면 진입 전 앱을 중단시키던
  중복 빈/alias 문제를 해소한 작업
- 실제 체험 데이터가 있는 계정/프로그램 ID 기준으로 최종 UX 확인을 추가 권장

---

## 추가 요청 반영 (2026-03-01) - 메인 페이지 구현

### 1) 구현 목표
- `/main/main` 진입 시 더 이상 `qna/list`로 리다이렉트하지 않고 메인 화면을 직접 렌더링
- 메인 화면에 실제 데이터(인기 체험 공고, 최신 QnA, 최신 기술블로그) 연결
- 로그인 후 기본 이동 경로를 메인으로 정렬

### 2) 적용한 코드 변경
1. 메인 라우트 동작 변경
   - `src/main/java/com/app/trycatch/controller/member/MemberController.java`
   - `@GetMapping("main")` 반환을 `RedirectView` -> `String("main/main")`로 변경
2. 메인 데이터 바인딩 추가
   - `featuredPrograms`: `experienceProgramRankDAO.findTopByViewCount(6)`
   - `latestQnas`: `qnaDAO.findLatest(6)`
   - `latestSkillLogs`: `skillLogDAO.findLatest(6)`
   - 개인회원 세션인 경우 `scrapProgramIds`도 함께 주입
3. 템플릿 교체
   - `src/main/resources/templates/main/main.html`
   - 메인 히어로/인기 체험/최신 QnA/최신 기술블로그/CTA 구조로 재구성
4. 메인 전용 스타일 추가
   - `src/main/resources/static/css/main/main-home.css` 신규 생성
5. 로그인 이후 기본 진입 경로 정렬
   - `MemberController.login()` 기본 `re_url`을 `/main/main`으로 변경
   - 카카오 콜백 fallback 경로도 `/main/main`으로 변경

### 3) 검증 결과
1. 컴파일 검증
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`
2. 런타임 검증
- `bootRun` 후 `GET /main/main` 호출 결과 `200`
- 응답 HTML에 메인 핵심 문구(`체험으로 시작하는 취업 준비`) 포함 확인

### 4) 비고
- 기존 정적/대용량 `main.html`을 실제 서비스 데이터 기반 메인 페이지로 치환
- 헤더는 기존 `qna/header.html` fragment를 재사용하여 네비게이션 일관성 유지

---

## 추가 요청 반영 (2026-03-01) - 체험 상세 경로 정리 + 메인페이지 마무리

### 1) 요청 사항
- `summary.md`에 이번 세션 변경사항 기록
- 체험 목록에서 상세 이동 경로를 `experience/training-program` 기준으로 정리
- 메인페이지(`main/main`)의 체험 카드 링크/썸네일 경로를 실제 라우트 및 파일 API 기준으로 마감

### 2) 적용한 코드 변경
1. 체험 상세 라우트 표준화
   - `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
   - 상세 기본 라우트: `GET /experience/training-program/{id}`
   - 호환 라우트 유지:
     - `GET /experience/program/{id}` -> `training-program` 리다이렉트
     - `GET /experience/detail?id=` -> `training-program` 리다이렉트
2. 목록-상세 연결 및 필터 보강
   - `src/main/resources/templates/experience/list.html`
     - 카드 링크를 `/experience/training-program/{id}`로 변경
     - 직무 필터를 자유입력 -> DB 기반 드롭다운으로 변경
   - `src/main/java/com/app/trycatch/service/experience/ExperienceProgramService.java`
   - `src/main/java/com/app/trycatch/repository/experience/ExperienceProgramDAO.java`
   - `src/main/java/com/app/trycatch/mapper/experience/ExperienceProgramMapper.java`
   - `src/main/resources/mapper/experience/ExperienceProgramMapper.xml`
     - `selectDistinctJobs` 추가(직무 필터 옵션 조회)
3. `training-program` 템플릿 동적 상세화
   - `src/main/resources/templates/experience/training-program.html`
   - 기존 정적 덤프형 마크업을 Thymeleaf 데이터 바인딩 상세 템플릿으로 교체
   - 지원 버튼 분기(`canApply/hasApplied`) 및 상세 설명/첨부 이미지 렌더링 연동
4. 메인페이지 마감(링크/썸네일 경로 정합화)
   - `src/main/resources/templates/main/main.html`
   - 체험 카드 상세 링크: `/experience/training-program/{id}`로 정리
   - 썸네일 표시 경로: `/file/display` 경로 오사용 제거, `/api/files/display(filePath,fileName)`로 수정
   - 이미지 타입 체크(`fileContentType == IMAGE`) 조건 적용

### 3) 검증 결과
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`

### 4) 효과
- `experience/list` -> `experience/training-program/{id}` 흐름이 실제 데이터 기반으로 안정 연결됨
- 메인페이지 체험 카드의 상세 이동/썸네일 표시가 현재 백엔드 라우트/파일 API와 정합됨

---

## 추가 요청 반영 (2026-03-01) - 원본 HTML 유지 + 3-tier 적용 재정렬

### 1) 요청 사항
- `summary.md` 확인 후 다시 정리
- 체험 프로그램/메인 페이지 모두 **기존 사용자가 만든 HTML을 유지**하고
  Thymeleaf 바인딩 외 구조 변경 금지
- 체험 프로그램 목록에서 상세 이동 시 발생하던 오류 구간 점검
- 메인도 `main/main.html` 원본을 유지한 채 3-tier 로직만 적용

### 2) 적용한 코드 변경
1. 체험 상세 라우트 기준 재정렬
   - `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
   - 상세 기본 라우트: `GET /experience/program/{id}`
   - 호환 라우트 유지:
     - `GET /experience/training-program/{id}` -> `/experience/program/{id}` 리다이렉트
     - `GET /experience/detail?id=` -> `/experience/program/{id}` 리다이렉트
2. 목록 링크/로그인 복귀 경로 정합화
   - `src/main/resources/templates/experience/list.html`
     - 카드 상세 링크를 `/experience/program/{id}`로 통일
     - 직무 필터를 기존 텍스트 입력 방식으로 복원
   - `src/main/resources/templates/experience/training-program.html`
     - 로그인 복귀 URL `re_url`을 `/experience/program/{id}`로 통일
3. 메인 3-tier 적용
   - `src/main/java/com/app/trycatch/service/main/MainHomeService.java` 신규 생성
     - 메인/서비스소개 데이터 조합 로직(인기 프로그램, 최신 QnA, 최신 기술블로그, 스크랩 ID) 분리
   - `src/main/java/com/app/trycatch/controller/member/MemberController.java`
     - DAO 직접 조회 로직 제거, `MainHomeService` 호출 구조로 변경
4. 메인 HTML 원복
   - `src/main/resources/templates/main/main.html`
   - 사용자가 작성한 기존 원본 구조로 복원하고, 기존 fragment 연결 유지

### 3) 검증 결과
1. 컴파일 검증
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL`
2. 런타임 검증
- `GET /main/main` -> `200`
- `GET /experience/list` -> `200`
- `GET /experience/program/1` -> `302`
- `GET /experience/training-program/1` -> `302` (호환 리다이렉트 동작)

### 4) 비고
- 상세 페이지 응답 코드 `302`는 현재 테스트 DB에서 해당 ID 실데이터 미존재 시
  예외 핸들러/리다이렉트가 정상 동작한 결과이며, 라우트 깨짐으로 인한 서버 오류는 재현되지 않음

---

## 추가 요청 반영 (2026-03-01) - 외부 원본 experience 폴더 재적용 + 목록/상세 재진행

### 1) 요청 사항
- `C:\Users\pigch\Desktop\gb_0090_kyc\TRY-CATCH`의 `experience` 폴더 파일을 현재 프로젝트로 재반영
- 체험 프로그램 목록/상세 화면을 원본 기준으로 다시 교체
- `summary.md` 작업 흐름에 맞춰 이번 변경 내역 기록

### 2) 적용한 파일 반영
1. 원본 템플릿 반영
   - `src/main/resources/templates/experience/list.html`
   - `src/main/resources/templates/experience/training-program.html`
2. 원본 정적 리소스 반영
   - `src/main/resources/static/js/experience/list.js`
   - `src/main/resources/static/js/experience/training-program.js`
   - `src/main/resources/static/css/experience/list.css`
   - `src/main/resources/static/css/experience/training-program.css`
   - `src/main/resources/static/css/experience/icons.c8c8e4ab.woff2`
   - `src/main/resources/static/css/experience/icons.fc26c557.woff2`
3. 스프링 정적 경로 최소 보정
   - `list.html`
     - `/static/css/experience/list.css` -> `/css/experience/list.css`
     - `/static/images/logo.png` -> `/images/logo.png`
     - `/static/js/experience/list.js` -> `/js/experience/list.js`
   - `training-program.html`
     - `../../static/css/experience/training-program.css` -> `/css/experience/training-program.css`
     - `../../static/js/experience/training-program.js` -> `/js/experience/training-program.js`

### 3) 검증 결과
1. 컴파일 검증
```powershell
$env:GRADLE_USER_HOME='C:\\Users\\pigch\\Desktop\\trycatch_copy\\.gradle-user-home'; .\\gradlew.bat compileJava
```
- 결과: `BUILD SUCCESSFUL` (`:compileJava UP-TO-DATE`)

---

## 추가 요청 반영 (2026-03-01) - 메인/목록/상세 더미데이터 + 상세 500 수정

### 1) 요청 사항
- 메인 페이지, 체험프로그램 목록, 체험프로그램 상세가 실제 데이터로 보이도록 더미 데이터 투입
- 메인에서 인기공고(조회수순), 최신공고(업데이트순) 동작 확인
- 목록/상세 썸네일이 보이도록 파일 데이터까지 연결
- 상세 페이지가 열리지 않는(500) 문제 원인 확인 및 수정

### 2) 적용한 변경
1. DB 시드 파일 추가/적용
   - `src/main/resources/sql/dummy_main_experience_seed.sql`
   - 추가 데이터:
     - 회원/기업 기본 더미
     - 체험 프로그램 12건 (상태: recruiting/draft/closed/cancelled 혼합)
     - 조회수/업데이트일 차등 데이터
2. 썸네일 매핑 데이터 추가
   - `tbl_file`, `tbl_experience_program_file` 시드 추가
   - `/api/files/display` 경로 기준 실제 파일 배치
     - `C:/file/seed/program/program-5001.png` ~ `program-5012.png`
3. 상세 페이지 500 수정
   - 원인: `templates/experience/training-program.html`의 `<title th:text>` 파싱 식 오류
   - 수정 파일:
     - `src/main/resources/templates/experience/training-program.html`
     - `src/main/resources/templates/experience/detail.html`
   - 라우트 렌더 템플릿 통일:
     - `src/main/java/com/app/trycatch/controller/experience/ExperienceProgramController.java`
     - `GET /experience/program/{id}` -> `experience/detail` 렌더

### 3) 검증 결과
1. 데이터 검증
- `tbl_experience_program` 총 12건, `recruiting` 9건
- 인기공고 상위 조회수/최신공고 업데이트순 쿼리 결과 정상
- `view_experience_program_file` 12건 매핑 확인
2. 페이지 응답 검증
- `GET /experience/list` -> `200`
- `GET /experience/program/5001` -> `200`
- 상세 HTML 본문에 프로그램 제목/이미지 태그 확인

### 4) 비고
- 기존 `10000` 포트 서버가 구버전 프로세스로 남아 있으면 수정 반영이 지연될 수 있어 재기동 후 확인 필요

---

## 추가 요청 반영 (2026-03-01) - TRY-CATCH 원본 experience 재적용 + 서버 동작 검증

### 1) 요청 사항
- `C:\Users\pigch\Desktop\gb_0090_kyc\TRY-CATCH`의 `experience` 파일을 현재 프로젝트에 그대로 반영
- 현재 경로의 기존 `experience` 파일 삭제 후 원본 붙여넣기
- 3-tier 분류 기준(`xml, mapper, VO, DTO, DAO, Service, Controller, html, css, js`)에 맞춰 누락 계층 폴더 보강
- 서버 실행 후 실제 URL 동작 검증

### 2) 반영 내용
1. 원본 `experience` 리소스 재적용
   - `src/main/resources/templates/experience/list.html`
   - `src/main/resources/templates/experience/training-program.html`
   - `src/main/resources/static/css/experience/list.css`
   - `src/main/resources/static/css/experience/training-program.css`
   - `src/main/resources/static/js/experience/list.js`
   - `src/main/resources/static/js/experience/training-program.js`
2. 원본 기준으로 제거된 파일 반영
   - 삭제: `templates/experience/detail.html`
   - 삭제: `static/css/experience/program-detail.css`
   - 삭제: `static/css/experience/program-list.css`
   - 삭제: `static/js/experience/program-detail.js`
3. 3-tier 누락 계층 폴더 생성
   - `src/main/java/com/app/trycatch/dao/experience/`
   - `src/main/java/com/app/trycatch/vo/experience/`

### 3) 서버 실행/동작 검증
- 실행 명령:
```powershell
./gradlew.bat bootRun --no-daemon
```
- 애플리케이션 기동 확인:
  - `Tomcat started on port 10000`
- URL 검증 결과:
  - `GET /experience/list` -> `200`
  - `GET /experience/training-program/1` -> `302` (상세로 리다이렉트)
  - `GET /experience/detail?id=1` -> `302` (상세로 리다이렉트)
  - `GET /experience/list.html` -> `404`
  - `GET /experience/training-program.html` -> `404`
- 결론:
  - 현재 라우팅은 정적 `.html` 직접 접근이 아니라 컨트롤러 매핑 경로(`/experience/...`) 기준으로 정상 동작

---

## 추가 요청 반영 (2026-03-01) - main 탭 하드코딩 정리 + 목록 조회 `select` 검증

### 1) main 탭 하드코딩 정리
- 파일: `src/main/resources/templates/main/main.html`
- 반영:
  - `기술블로그` 탭(`dev-cont-Cntnt_OnePick`) 정적 카드 제거
  - `QnA` 탭(`dev-cont-Cntnt_Theme_V2`) 정적 카드 제거
  - 서버 모델 데이터 기반으로 변경
    - `th:each="log : ${latestSkillLogs}"`
    - `th:each="qna : ${latestQnas}"`
  - 빈 목록 시 안내 문구(`th:if`) 추가
  - 기존 슬라이더 클래스(`instance-swipwe-3`, `instance-swipwe-5`) 유지

### 2) 목록 조회가 모두 `select`인지 확인 결과
1. 메인 체험공고 목록
   - `MainHomeService.getPopularPrograms()` -> `ExperienceProgramRankDAO.findTopByViewCount()` -> `ExperienceProgramRankMapper.selectTopByViewCount()`
   - `MainHomeService.getLatestPrograms()` -> `ExperienceProgramRankDAO.findTopByUpdatedDatetime()` -> `ExperienceProgramRankMapper.selectTopByUpdatedDatetime()`
   - SQL: `mapper/mypage/experienceProgramRankMapper.xml`의 `<select ...>` 사용
2. 메인 QnA 목록
   - `MainHomeService.getLatestQnas()` -> `QnaDAO.findLatest()` -> `QnaMapper.selectLatest()`
   - SQL: `mapper/qna/qnaMapper.xml`의 `<select id="selectLatest">`
3. 메인 기술블로그 목록
   - `MainHomeService.getLatestSkillLogs()` -> `SkillLogDAO.findLatest()` -> `SkillLogMapper.selectLatest()`
   - SQL: `mapper/skilllog/skillLogMapper.xml`의 `<select id="selectLatest">`
4. 체험공고 목록 페이지
   - `ExperienceProgramService.getList()` -> `ExperienceProgramDAO.countPublic()/findPublic()`
   - `ExperienceProgramMapper.countPublic()/selectPublic()`
   - SQL: `mapper/experience/ExperienceProgramMapper.xml`의 `<select id="countPublic">`, `<select id="selectPublic">`

### 3) 검증
- `./gradlew.bat compileJava --no-daemon` 성공
- `GET /main/main` -> `200` 확인
- 응답에 `기술블로그/QnA` 탭 및 동적 렌더링 블록 존재 확인

---

## 추가 요청 반영 (2026-03-01) - 메인 공고 섹션 DB 연동 + 인사이트 개편 + 하단 섹션 제거

### 1) 요청 사항
- `summary.md`, `code_summary.md` 기준으로 메인(`/main/main`) 공고 섹션 하드코딩 제거
- 각 섹션을 DB 데이터/정렬 조건으로 노출
- `게임분야 체험정보`를 `체험 준비 인사이트`로 개편
- 맨 아래 `온라인 채용대행` 섹션 제거

### 2) 적용한 변경
1. 메인 공고 섹션용 DTO/Mapper/DAO/Service 확장
   - `ExperienceProgramRankDTO` 필드 추가:
     - `experienceProgramRecruitmentCount`, `applicantCount`
     - `corpLogoFilePath`, `corpLogoFileName`
   - `ExperienceProgramRankMapper/DAO` 메서드 추가:
     - 조회수순, 마감임박순, 모집인원순, 지원율 낮은순, 최신등록순
     - 최근 본 공고(회원 기준)
   - `MainHomeService` 메서드 추가:
     - `getDeadlineSoonPrograms`, `getHighRecruitmentPrograms`
     - `getLowApplyRatePrograms`, `getNewestPrograms`
     - `getRecentViewedPrograms`
2. SQL(XML) 추가 및 보정
   - 파일: `mapper/mypage/experienceProgramRankMapper.xml`
   - 공통 컬럼/조인 SQL 분리(`baseColumns`, `baseFrom`)
   - 신규 `<select>` 쿼리 추가:
     - `selectTopByDeadlineSoon`
     - `selectTopByRecruitmentCount`
     - `selectTopByApplyRateAsc`
     - `selectTopByCreatedDatetime`
     - `selectRecentViewedByMemberId`
   - XML 파싱 오류 수정:
     - `<=` 를 `&lt;=` 로 변경 (line 111 근처)
3. 컨트롤러 모델 바인딩 확장
   - 파일: `MemberController.goMainPage()`
   - 모델 추가:
     - `companyLatestPrograms`, `deadlineSoonPrograms`
     - `personalizedPrograms`(최근 본 공고 4개)
     - `highRecruitmentPrograms`, `lowApplyRatePrograms`
     - `newestPrograms`
4. 메인 템플릿 섹션 동적화
   - 파일: `templates/main/main.html`
   - 아래 섹션을 `th:each` 기반 동적 렌더링으로 교체:
     - `지금 가장 주목받는 공고예요!`(조회수순)
     - `기업 체험 공채`(최신순 + 회사명/회사로고/D-day)
     - `꼭 봐야할 공고예요!`(마감임박순)
     - `나에게 꼭 맞는 체험 공고를 만나보세요`(최근 본 공고)
     - `적극적으로 모집중인 공고예요!`(모집인원순)
     - `이 공고 놓치지 마세요!`(지원율 낮은순)
     - `새로운 기회를 찾아보세요!`(최신순)
   - 상세 이동 링크 통일:
     - `/experience/program/{id}`
   - D-day 표시:
     - `ChronoUnit.DAYS.between(...)` 계산 적용
5. `게임분야 체험정보` 개편
   - 섹션명을 `체험 준비 인사이트`로 변경
   - 구성:
     - `최신 기술블로그` 4건 (`/skill-log/detail?id=...`)
     - `최신 QnA` 4건 (`/qna/detail?id=...`)
   - 각 목록 `더보기` 연결:
     - 기술블로그 `/skill-log/list`, QnA `/qna/list`
6. 맨 아래 섹션 제거
   - `온라인 채용대행`(`Cntnt_Oras`) 블록 전체 삭제

### 3) 검증
1. 컴파일
- `./gradlew.bat compileJava --no-daemon` -> `BUILD SUCCESSFUL`
2. 런타임
- `/main/main` 응답 `200` 확인
- 공고 섹션 문구 노출 확인:
  - `지금 가장 주목받는 공고예요!`
  - `기업 체험 공채`
  - `꼭 봐야할 공고예요!`
  - `나에게 꼭 맞는 체험 공고를 만나보세요`
  - `적극적으로 모집중인 공고예요!`
  - `이 공고 놓치지 마세요!`
  - `새로운 기회를 찾아보세요!`
- 인사이트 섹션 문구 노출 확인:
  - `체험 준비 인사이트`
  - `최신 기술블로그`
  - `최신 QnA`
