# 질적 우세

## 문서 메타

- 전략명: 질적 우세
- 분류: `derived`
- 상태: `seed`
- 우선순위: `high`
- 근거 수준: `mixed`

## 한 줄 요약

숫자가 같거나 조금 부족해도 `사거리`, `속도`, `치유 배분`, `BodyPart 상태`, `전술 품질`에서 우위를 만들면 교환비를 뒤집을 수 있다.

## 원문 추출 포인트

나무위키 `란체스터 법칙` 문서의 `응용과 극복 > 군정예화 또는 현대화`를 CTF 문맥에 맞게 재해석했다. 핵심은 `숫자를 제외한 조건이 동등해야만 순수 물량 법칙이 성립한다`는 점이다.

## CTF 해석

- 같은 14기라도 더 잘 뭉치고, 더 정확히 focus fire하고, 더 적게 막히는 쪽이 질적으로 우세하다.
- `MOVE` 추가 파츠는 실제로는 기동 품질 향상이라서 단순 1파츠 이상의 가치가 있다.
- extra role part는 강해 보이지만 fatigue를 늘려 formation을 깨뜨릴 수 있으므로 무조건 좋은 것은 아니다.
- 힐러 배분, 사거리 유지, pathing traffic 관리도 모두 `전술 품질`에 포함된다.

## 운영 규칙

1. 수 싸움 전에 먼저 `유효 전력 품질`을 높인다.
2. BodyPart는 무조건 줍는 대신 `MOVE 우선, 역할 파츠는 여유 기동이 있을 때` 원칙을 둔다.
3. 교전 중에는 과잉 치유와 과잉 추격을 줄여 실제 유효 액션 수를 늘린다.
4. 같은 전술을 계속 써도 흔들리지 않는 이동/타깃/치유 품질을 먼저 고정한다.

## 지속가능성 체크

- 장점: 일회성 깜짝수가 아니라 코드 품질이 누적되어 계속 남는다.
- 장점: 래더 환경에서 가장 재현성이 높다.
- 위험: 구현 범위가 넓어서 산만하게 만들면 아무 것도 완성되지 않는다.
- 위험: 숫자 열세가 너무 크면 질적 우세만으로는 못 막는다.

## 관측 지표

- 교전 중 `idle tick` 비율
- primary target 변경 빈도
- heal 낭비량과 overheal 빈도
- BodyPart 확보 후 실제 유지 시간
- 평균 이동 지연과 collision 빈도

## 실패 시그널과 카운터

- 실패 시그널: 역할별 포지션은 좋아졌지만 킬이 나지 않음
- 실패 시그널: role part 욕심으로 formation이 깨짐
- 상대 카운터: 순수 물량 blob, tower safe zone 강제
- 대응 보정: 질적 우세는 반드시 [전력 집중](./force-concentration.md)과 묶어서 쓴다

## 봇 구현 메모

- `evaluatePhase()`는 병력 수뿐 아니라 `active role parts`, `expected movement speed`, `available heal`도 비교해야 한다.
- BodyPart 가치 함수 예시:
  - `MOVE`: 항상 높음
  - 동일 역할 파츠: 기동 여유가 있을 때만 높음
  - 혼합 역할 파츠: 실험 전까지는 보수적으로
- `makeOrders()`에서 `healer allocation`과 `path congestion`을 공통 로직으로 뽑아두면 다른 전략에도 재사용 가능하다.

## 관련 문서

- [란체스터 법칙](./lanchester-law.md)
- [강 중앙 BodyPart 통제](./river-bodypart-control.md)
- [호드 결집과 집중 사격](./horde-cohesion-focus-fire.md)

## 출처

- [란체스터 법칙 - 나무모에 미러](https://www.namu.moe/w/%EB%9E%80%EC%B2%B4%EC%8A%A4%ED%84%B0%20%EB%B2%95%EC%B9%99)
- [Screeps #25: Arena - Pressing the Attack](https://jonwinsley.com/notes/screeps-arena-pressing-attack)
- [Screeps #25: Arena - Grouping Up](https://jonwinsley.com/notes/screeps-arena-grouping-up)
