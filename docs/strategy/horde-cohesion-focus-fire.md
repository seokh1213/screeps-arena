# 호드 결집과 집중 사격

## 문서 메타

- 전략명: 호드 결집과 집중 사격
- 분류: `tactical`
- 상태: `seed`
- 우선순위: `high`
- 근거 수준: `mixed`

## 한 줄 요약

이동할 때는 뭉치고, 싸울 때는 같은 적을 때리고, 치유는 필요한 만큼만 나눠 줘서 한타의 유효 전력 손실을 줄인다.

## 해결하려는 문제

- 이동 중 충돌로 인해 본대가 줄 서서 들어가는 문제
- heal과 damage가 서로 다른 대상에 흩어져 전투 효율이 떨어지는 문제
- 앞선 유닛만 먼저 닿아 녹아버리는 문제

## 핵심 가설

- blob 자체보다 `질서 있는 blob`이 중요하다.
- leader, intended move squares, shove logic만 있어도 결집 품질이 크게 오른다.
- target priority와 heal allocation을 그룹 기준으로 잡아야 란체스터식 집중 효과가 실제로 나온다.

## 운영 규칙

1. 이동 시 leader 하나를 두고, 나머지는 leader 또는 group centroid를 기준으로 따라간다.
2. 이미 누군가 이동 예정인 칸은 path cost를 높여 불필요한 충돌을 줄인다.
3. 앞선 유닛이 너무 앞서면 잠깐 멈춰 후미가 붙도록 한다.
4. 전투 시 primary target은 보통 `아군 centroid에 가장 가까운 적`으로 고정한다.
5. heal은 `한 대상을 완전 복구할 만큼` 먼저 배분하고 남는 자원으로 다음 대상을 본다.

## 지속가능성 체크

- 장점: 다른 전략 대부분의 기반이 되는 전술이다.
- 장점: 한 번 구현하면 공격/수비/중앙 통제 모두에 재사용 가능하다.
- 위험: shove logic과 role별 positioning이 엉키면 디버깅이 어렵다.
- 위험: 지나치게 뭉치면 좁은 지점에서 traffic jam이 생길 수 있다.

## 관측 지표

- centroid 기준 평균 분산 거리
- primary target 동시 타격 수
- 교전 첫 10틱 동안 단독 노출 유닛 수
- heal 분배 후 남는 wounded creep 수

## 실패 시그널과 카운터

- 실패 시그널: 본대가 길게 한 줄로 늘어짐
- 실패 시그널: healers가 전열에 끌려 들어감
- 상대 카운터: kite, choke 유도, 일부 유닛 미끼
- 대응 보정: 이동용 formation과 전투용 formation을 분리한다

## 봇 구현 메모

- `leader`, `group centroid`, `planned next squares`, `shove queue`를 메모리로 유지한다.
- role별 기본 위치:
  - melee: 적과 우리 flag 사이
  - ranged: 적 기준 3칸 유지
  - heal: 힐 대상 근처이되 incoming damage가 적은 칸 선호
- 현재 저장소 기준으로는 `Overmind.makeOrders()`가 비어 있으므로, 이 문서가 사실상 첫 전투 오더 설계 기준점이 된다.

## 관련 문서

- [전력 집중](./force-concentration.md)
- [질적 우세](./qualitative-overmatch.md)
- [공격의 다차원화](./multi-axis-attack.md)

## 출처

- [Screeps #25: Arena - Grouping Up](https://jonwinsley.com/notes/screeps-arena-grouping-up)
- [란체스터 법칙 - 나무모에 미러](https://www.namu.moe/w/%EB%9E%80%EC%B2%B4%EC%8A%A4%ED%84%B0%20%EB%B2%95%EC%B9%99)
