# 탄력 방어와 역공

## 문서 메타

- 전략명: 탄력 방어와 역공
- 분류: `tactical`
- 상태: `seed`
- 우선순위: `high`
- 근거 수준: `mixed`

## 한 줄 요약

적이 유리한 구역에 있을 때는 버티고, 우리 구역이나 강 중앙으로 나오면 강하게 물고, 수비 성공 뒤 또는 시간 압박 구간에만 역공한다.

## 해결하려는 문제

- enemy safe zone 깊숙이 들어갔다가 역으로 손해 보는 패턴
- 수비는 잘하지만 승리로 전환하지 못해 draw만 쌓이는 패턴
- 시간 종료 직전 판단이 늦어져 `비겨도 되는 판`과 `반드시 찔러야 하는 판`을 구분 못 하는 문제

## 핵심 가설

- 모든 구역에서 같은 강도로 싸우는 것은 비효율적이다.
- 적이 우리 safe zone 또는 river zone에 들어온 순간이 가장 좋은 방어 교전 타이밍이다.
- 깊은 역공은 평소 기본값이 아니라 `유리한 교환 직후` 혹은 `남은 시간상 필요할 때`만 열어야 한다.

## 운영 규칙

1. 기본 stance는 `우리 flag에 적보다 더 가깝게 위치`하는 것이다.
2. 적이 우리 safe zone 또는 river zone에 오면 뭉쳐서 압박한다.
3. 적이 자기 safe zone에 있으면 불필요한 dive 대신 BodyPart, 타워, 재집결을 우선한다.
4. 수비에서 이득을 봤을 때만 짧은 counterpush를 연다.
5. 남은 시간이 적고 flag 상태가 불리하면 `timed push window`를 강제로 연다.

## 지속가능성 체크

- 장점: 무리한 chase를 줄여 장기전 안정성이 높다.
- 장점: draw를 허용할지, 승부를 볼지 시간 기반으로 명확히 나눌 수 있다.
- 위험: 지나치게 보수적으로 굴면 경기 주도권을 잃는다.
- 위험: push window 계산이 늦으면 아무 것도 못 하고 끝난다.

## 관측 지표

- enemy safe zone dive 빈도와 손실
- 수비 이득 후 50틱 내 counterpush 전환율
- 마지막 200틱의 공격 성공률
- flag count 기준 의사결정 정확도

## 실패 시그널과 카운터

- 실패 시그널: 이득을 보고도 계속 뒤로만 빠짐
- 실패 시그널: behind 상태인데도 push window를 열지 않음
- 상대 카운터: 우리 수비 성향을 읽고 중앙 자원 장악, 타워 말려죽이기
- 대응 보정: `현재 flag 상태`, `남은 시간`, `타워 에너지`, `본대 체력`을 함께 본다

## 봇 구현 메모

- 현재 `Phase`는 `START`, `INITIAL`, `CONQUERING`, `IN_BATTLE`만 있으므로 실전적으로는 아래 하위 상태가 더 필요하다.
  - hold
  - punish
  - harvest
  - counterpush
  - final push
- 꼭 enum을 늘리지 않더라도 `evaluatePhase()` 내부의 세부 판정 값으로 구현 가능하다.
- `makeOrders()`는 수비와 역공을 같은 로직으로 처리하지 말고 `commit depth`를 다르게 둔다.

## 관련 문서

- [타워 안전지대 통제](./tower-zone-control.md)
- [비대칭 능력 활용](./asymmetric-capability.md)
- [각개격파](./defeat-in-detail.md)

## 출처

- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
- [Screeps #25: Arena - Pressing the Attack](https://jonwinsley.com/notes/screeps-arena-pressing-attack)
