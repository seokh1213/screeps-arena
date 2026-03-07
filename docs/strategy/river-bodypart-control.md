# 강 중앙 BodyPart 통제

## 문서 메타

- 전략명: 강 중앙 BodyPart 통제
- 분류: `tactical`
- 상태: `seed`
- 우선순위: `high`
- 근거 수준: `mixed`

## 한 줄 요약

중앙 BodyPart를 먹어 전투력 차이를 만들되, 수집 때문에 본대가 찢어지지 않게 운영한다.

## 해결하려는 문제

- defensive mirror에서 무승부가 반복되는 상황
- 타워 밖 한타가 불리한데 전투력 격차를 만들 방법이 없는 상황
- extra part를 무작정 먹다 formation이 붕괴하는 문제

## 핵심 가설

- BodyPart는 누적되면 단순 1파츠 이상의 스노우볼이 된다.
- 하지만 파츠를 먹기 위해 본대가 무너지면 오히려 손해다.
- `MOVE 우선, 역할 파츠는 기동 여유가 있을 때`가 가장 안정적인 초깃값이다.

## 적용 조건

- enemy가 자기 safe zone에 머물거나, 강 중앙을 완전히 포기할 때
- 우리 집결점이 강 중앙과 너무 멀지 않을 때
- BodyPart 회수 담당이 돌아와 heal support를 받을 수 있을 때

## 운영 규칙

1. 강 중앙 근처에 본대 rally point를 둔다.
2. 파츠 회수는 본대에서 너무 멀어지지 않는 개체만 보낸다.
3. 우선순위는 기본적으로 `MOVE > 자기 역할 파츠 > 혼합 역할 파츠` 순으로 본다.
4. 새 파츠가 0 hits로 붙었을 수 있으므로 복귀 후 heal을 우선 배정한다.
5. enemy safe zone 깊숙한 파츠는 전투 승산이 없으면 포기한다.

## 지속가능성 체크

- 장점: 수비 일변도에서 벗어나 안정적인 우세를 만들 수 있다.
- 장점: 질적 우세를 자연스럽게 쌓아 후반 교전력이 강해진다.
- 위험: 파츠 욕심 때문에 본대가 2개로 갈라지기 쉽다.
- 위험: role part 과다 획득은 fatigue를 높여 formation을 망칠 수 있다.

## 관측 지표

- 파츠 타입별 확보 수
- 우리와 적의 총 active parts 차이
- 파츠 회수에 투입된 분산 시간
- 파츠 회수 직후 사망률

## 실패 시그널과 카운터

- 실패 시그널: 파츠를 먹으러 간 개체가 고립되어 끊김
- 실패 시그널: extra role part 때문에 본대 이동 속도가 느려짐
- 상대 카운터: 강 중앙 매복, 수집 담당 일점사
- 대응 보정: 회수 후보는 `expected return path safety`가 충분할 때만 허용

## 봇 구현 메모

- `Context.bodyPartItems`를 거리, 타입, 복귀 안전도 기준으로 점수화한다.
- 파츠 회수 오더는 기본적으로 `rally point -> pickup -> return`의 폐루프로 만든다.
- `MOVE` 파츠 획득 이후 centroid 속도가 어떻게 바뀌는지 로그를 남긴다.

## 관련 문서

- [질적 우세](./qualitative-overmatch.md)
- [타워 안전지대 통제](./tower-zone-control.md)
- [비대칭 능력 활용](./asymmetric-capability.md)

## 출처

- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
- [Screeps #25: Arena - Pressing the Attack](https://jonwinsley.com/notes/screeps-arena-pressing-attack)
