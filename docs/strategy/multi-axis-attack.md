# 공격의 다차원화

## 문서 메타

- 전략명: 공격의 다차원화
- 분류: `derived`
- 상태: `seed`
- 우선순위: `medium`
- 근거 수준: `mixed`

## 한 줄 요약

정면 힘싸움 한 줄로 싸우지 말고, 서로 다른 화력축이 동시에 닿도록 전장을 설계해 적의 방어 효율을 무너뜨린다.

## 원문 추출 포인트

나무위키 `란체스터 법칙` 문서의 `응용과 극복 > 공격의 다차원화`에서 가져온 전략명이다. 원문은 고저차, 곡사, 범위타격처럼 란체스터의 단순 전제를 깨는 방식을 예시로 든다.

## CTF 해석

Arena CTF에는 전통적인 고저차는 없지만, 아래처럼 `독립 화력축`을 만들 수 있다.

- 타워 화력 + ranged focus + melee zone control
- 선두와 후미를 동시에 위협하는 crossfire 형태의 positioning
- 적의 퇴로를 막는 이동축과, 본체를 때리는 화력축의 분리
- 적이 우리 flag 쪽으로 들어올 때 `타워 사거리`, `힐러 접근`, `아군 전열`을 한 점에 겹치는 kill box 형성

## 운영 규칙

1. 정면 충돌 전에 유리한 전투 지점을 먼저 정한다.
2. melee는 적을 묶고, ranged는 안전 사거리에서 누적 피해를 넣고, heal은 노출이 적은 축을 유지한다.
3. 타워가 있으면 타워가 primary target에 같은 리듬으로 들어가도록 오더를 맞춘다.
4. 어느 한 축이 끊기면 무리해서 유지하지 말고 재배치한다.

## 지속가능성 체크

- 장점: 같은 병력으로도 교환비를 끌어올리기 쉽다.
- 장점: CTF에서 타워와 깃발 위치가 고정이라 반복 가능한 kill box를 만들기 좋다.
- 위험: 정밀한 위치 제어가 필요해 구현 난도가 높다.
- 위험: 한 축이 늦게 도착하면 오히려 분산 교전이 된다.

## 관측 지표

- 교전 시점에 primary target을 때리는 화력축 수
- 우리 타워와 primary target의 거리 일치도
- melee/ranged/heal 간 평균 거리
- 적 후퇴 경로 차단 성공률

## 실패 시그널과 카운터

- 실패 시그널: melee가 먼저 녹고 ranged와 heal이 뒤에 남음
- 실패 시그널: 타워 사거리 밖에서 불필요한 chase 발생
- 상대 카운터: 빠른 철수, 우리 ranged에 대한 focus dive
- 대응 보정: `kill box outside`, `kill box edge`, `safe fallback` 세 구역을 미리 나눈다

## 봇 구현 메모

- 각 role별 cost matrix를 따로 두고, 같은 target을 향해도 선호 위치는 다르게 계산한다.
- `makeOrders()`에서 target assignment와 position assignment를 동시에 만든다.
- 타워가 primary target에 들어가는 순간을 trigger로 삼아 ranged 집중 사격을 맞추면 효과가 크다.

## 관련 문서

- [란체스터 법칙](./lanchester-law.md)
- [타워 안전지대 통제](./tower-zone-control.md)
- [호드 결집과 집중 사격](./horde-cohesion-focus-fire.md)

## 출처

- [란체스터 법칙 - 나무모에 미러](https://www.namu.moe/w/%EB%9E%80%EC%B2%B4%EC%8A%A4%ED%84%B0%20%EB%B2%95%EC%B9%99)
- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
