# 타워 안전지대 통제

## 문서 메타

- 전략명: 타워 안전지대 통제
- 분류: `tactical`
- 상태: `seed`
- 우선순위: `high`
- 근거 수준: `mixed`

## 한 줄 요약

교전의 기본 위치를 우리 타워와 우리 플래그 주변으로 고정해, 같은 병력이라도 더 유리한 구역에서 싸운다.

## 해결하려는 문제

- 열린 중원에서 의미 없이 맞교환하는 한타
- enemy safe zone 안으로 깊게 쫓아가다 타워와 역포커싱에 녹는 상황
- worker가 있어도 tower tempo를 제대로 못 살리는 문제

## 핵심 가설

- 타워가 붙은 교전은 기본적으로 우리 쪽 화력축이 하나 더 생기는 것과 같다.
- 적을 우리 safe zone으로 끌어들이면 적의 결집이 깨지고, 우리 힐과 타워는 동시에 겹치기 쉽다.
- 반대로 적 safe zone에서의 무리한 chase는 지속가능하지 않다.

## 적용 조건

- 우리 타워에 에너지가 있거나 채울 수 있어야 한다.
- 우리 본대가 타워 사거리와 완전히 분리되지 않아야 한다.
- 적이 강 중앙 또는 우리 절반 구역으로 들어오는 순간이 있어야 한다.

## 운영 규칙

1. 교전 기본 anchor를 `우리 플래그 - 우리 타워 - 강 중앙 입구` 선상에 둔다.
2. 적이 우리 safe zone 또는 river zone에 있으면 뭉쳐서 압박한다.
3. 적이 자기 safe zone 깊숙이 있으면 무리하게 물지 말고 재배치하거나 BodyPart를 본다.
4. worker는 한타 전에 tower energy를 우선 확인한다.

## 지속가능성 체크

- 장점: 대칭 맵에서 가장 재현성이 높은 기본 전술이다.
- 장점: 미세 컨트롤 실패를 타워가 일부 보정해 준다.
- 위험: tower energy가 없으면 전략 가치가 급락한다.
- 위험: safe zone에만 고정되면 draw bot으로 굳어질 수 있다.

## 관측 지표

- 교전 틱 중 아군이 우리 타워 범위 안에 있었던 비율
- tower energy 10 이상 유지 시간
- 우리 safe zone에서 시작한 전투의 교환비
- enemy safe zone 깊숙한 chase로 잃은 병력 수

## 실패 시그널과 카운터

- 실패 시그널: tower energy가 바닥인데도 같은 전술을 반복
- 실패 시그널: 적이 river BodyPart를 먹고 와도 계속 수비만 함
- 상대 카운터: river control로 스펙 격차 형성, 타워 범위 바깥 유인
- 대응 보정: safe zone 수비와 [강 중앙 BodyPart 통제](./river-bodypart-control.md)를 짝으로 운용

## 봇 구현 메모

- `evaluatePhase()`에서 각 enemy creep의 위치를 `our safe zone`, `river zone`, `enemy safe zone`으로 분류한다.
- `makeOrders()`는 zone에 따라 기본 행동을 분기한다.
  - enemy in our/river zone: 압박
  - enemy in enemy safe zone: 과도 추격 금지
- worker 오더는 tower energy가 0 또는 10 미만일 때 우선순위를 올린다.

## 관련 문서

- [전력 집중](./force-concentration.md)
- [강 중앙 BodyPart 통제](./river-bodypart-control.md)
- [탄력 방어와 역공](./elastic-defense-counterpush.md)

## 출처

- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
- [Screeps #25: Arena - Pressing the Attack](https://jonwinsley.com/notes/screeps-arena-pressing-attack)
