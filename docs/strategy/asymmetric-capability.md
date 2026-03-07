# 비대칭 능력 활용

## 문서 메타

- 전략명: 비대칭 능력 활용
- 분류: `derived`
- 상태: `seed`
- 우선순위: `medium`
- 근거 수준: `mixed`

## 한 줄 요약

정면 소모전이 불리하면, 연속적인 DPS 계산으로 설명되지 않는 `불연속 승부 포인트`를 만들어 게임을 비튼다.

## 원문 추출 포인트

나무위키 `란체스터 법칙` 문서의 `응용과 극복 > 비대칭 전력`을 CTF로 옮긴 문서다. 핵심은 `모든 피해와 전력 변화가 병력 수에 비례하지는 않는다`는 점이다.

## CTF 해석

CTF에서 비대칭은 아래처럼 나타난다.

- 깃발 터치 한 번으로 플래그 수가 줄어드는 `이산적 승리 조건`
- 타워 점령 여부에 따라 갑자기 바뀌는 화력/치유 환경
- 강 중앙 BodyPart로 생기는 순간적인 스펙 역전
- 시간 종료 직전 `마지막 한 번의 진입` 가치가 급상승하는 상황

즉, 이 모드의 승패는 항상 `누가 더 많이 죽였는가`로만 결정되지 않는다.

## 운영 규칙

1. 정면 교환비가 불리할수록 비대칭 승부 포인트를 먼저 찾는다.
2. 다만 `운빨 한 방`이 아니라 재현 가능한 비대칭만 채택한다.
3. 비대칭 기회가 열리면 본대 싸움보다 우선순위를 높일 수 있다.
4. 실패했을 때 즉시 재수습 가능한 범위 안에서만 시도한다.

## 지속가능성 체크

- 장점: 불리한 게임을 뒤집을 여지가 생긴다.
- 장점: 시간 제한과 깃발 규칙이 있는 CTF에 특히 잘 맞는다.
- 위험: 지나치게 의존하면 정상 전투력이 자라지 않는다.
- 위험: 한번 읽히면 재현성이 낮은 gimmick으로 전락할 수 있다.

## 관측 지표

- 비대칭 기회 탐지 수와 실제 전환 수
- last push 시도 성공률
- BodyPart 확보 후 전투력 역전 빈도
- 타워 ownership 변화 직후 승률

## 실패 시그널과 카운터

- 실패 시그널: 본대가 지고 있는데 소수 돌격만 반복함
- 실패 시그널: 기회가 없는데도 무리한 backdoor 시도
- 상대 카운터: flag guard 고정, time awareness, tower energy 관리
- 대응 보정: 비대칭 시도는 `승률 회복 장치`이지 기본 전투 플랜이 아님을 명확히 둔다

## 봇 구현 메모

- `evaluatePhase()`에 아래 opportunity detector를 넣는다.
  - enemy flag exposed
  - enemy tower dry
  - nearby BodyPart swing
  - final push window
- `makeOrders()`는 본대 플랜과 별개로 `opportunity override`를 가질 수 있어야 한다.
- 단, override는 `expected win delta`가 충분할 때만 발동한다.

## 관련 문서

- [란체스터 법칙](./lanchester-law.md)
- [강 중앙 BodyPart 통제](./river-bodypart-control.md)
- [탄력 방어와 역공](./elastic-defense-counterpush.md)

## 출처

- [란체스터 법칙 - 나무모에 미러](https://www.namu.moe/w/%EB%9E%80%EC%B2%B4%EC%8A%A4%ED%84%B0%20%EB%B2%95%EC%B9%99)
- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
