## Diary 기능
- 회원가입
- 로그인
- 일기 작성
- 일기 교환
- AI 추천
- 유료 기능

## Tech
- SwiftUI
- JAVA
- Spring Boot
- JPA
- MySQL

## 일기 교환 API

일기를 `POST /api/diaries`로 작성하면 서버가 차단 관계와 수신 한도를 확인한 뒤 랜덤한 사용자에게 보냅니다. 
작성 응답의 `exchange.status`가 `PENDING`이면 전달된 상태이고, `exchange`가 `null`이면 현재 받을 수 있는 사용자가 없습니다. 모든 요청에 로그인 JWT가 필요합니다.

- `GET /api/matches/inbox`: 나에게 도착해 답장을 기다리는 일기 목록
- `POST /api/matches/{exchangeId}/reply`: 수신자가 `diaryDate`, `title`, `content`, `mood`를 보내 답장 일기를 작성
- `POST /api/matches/{exchangeId}/reject`: 수신자가 교환 거절
- `GET /api/matches`: 내가 보낸 요청과 받은 요청의 상태 조회
- `POST /api/matches` (`{"diaryId": 1}`): 기한 만료·거절 후 또는 상대가 없었던 일기의 재매칭 시도
- `DELETE /api/matches/{exchangeId}`: 답장 대기 중인 요청 취소

답장 기한은 요청 생성 후 24시간입니다. 수신자가 동시에 받을 수 있는 `PENDING` 요청은 1건이며, 
만료된 요청은 1분마다 `EXPIRED`로 바뀝니다. 답장이 작성되면 `COMPLETED`가 되어 양쪽 일기를 볼 수 있습니다. 
재매칭은 자동 전달이 아니라 작성자가 위 API로 요청합니다. 
기존 `diary_exchanges` 기록은 DB에 보존되지만 새 교환 목록 API에는 포함되지 않습니다.
