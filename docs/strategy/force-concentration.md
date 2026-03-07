# 전력 집중

## 문서 메타

- 전략명: 전력 집중
- 분류: `foundation`
- 상태: `seed`
- 우선순위: `high`
- 근거 수준: `mixed`

## 한 줄 요약

같은 병력을 여러 곳에 나눠 쓰지 말고, 한 번의 접촉에서 더 많은 화력과 치유를 같은 목표에 겹쳐 local superiority를 만든다.

## 해결하려는 문제

- 병력이 도착 순서대로 녹아버리는 축차투입
- 힐러와 딜러가 서로 다른 싸움을 하는 산개 교전
- 중앙 BodyPart와 타워 안전지대를 동시에 욕심내다 둘 다 잃는 상황

## 핵심 가설

- 같은 총량의 병력이라도 `동시 사격 가능한 숫자`가 많으면 교환비가 크게 좋아진다.
- 집중은 단순히 공격력만이 아니라 `집중 치유`, `집중 경로`, `집중 진입 타이밍`까지 포함해야 지속된다.
- CTF에서는 깃발 수비와 중앙 통제를 동시에 하지 말고, 그 틱의 승부처 하나만 잡는 편이 장기적으로 강하다.

## 적용 조건

- 교전 전 집결 시간이 허용될 것
- 타워 에너지나 중앙 BodyPart처럼 승부 가치가 높은 포인트가 명확할 것
- 상대가 분산되어 있거나, 우리보다 먼저 접촉해 줄 것

## 운영 규칙

1. 집결이 끝나기 전에는 깊게 교전하지 않는다.
2. 전투가 시작되면 primary target 하나를 정하고 화력과 힐을 몰아준다.
3. 목표를 잡아낸 뒤에는 추격보다 재결집을 우선한다.

## 지속가능성 체크

- 미시컨트롤 민감도: 중간. 집결과 타깃 일치가 필요하지만 개념 자체는 단순하다.
- 코드 복잡도: 중간. centroid, readiness gate, target assignment 정도면 시작할 수 있다.
- 재현성: 높음. 대칭 맵과 고정 초기 병력에서 특히 안정적이다.
- 카운터 노출도: 과도한 blob은 적의 kite, choke, tower trap에 취약하다.
- 장기전 내구성: 높음. 전투 방식이 단순해서 튜닝 포인트가 분명하다.

## 관측 지표

- 교전 개시 직전 centroid 반경
- primary target에 동시에 타격 가능한 아군 수
- primary heal target에 동시에 치유 가능한 아군 수
- 선두 한 기가 단독 노출된 틱 수

## 실패 시그널과 카운터

- 실패 시그널: 아군이 2개 이상 전선으로 갈라짐
- 실패 시그널: primary target이 매 틱 바뀜
- 상대 카운터: 길목 유도, kite, 타워 범위 미끼
- 대응 보정: commit 전에 `유효 사거리 내 아군 수`가 기준 미달이면 재집결

## 봇 구현 메모

- `evaluatePhase()`에서 `commitReady = 결집도 + 타워 에너지 + primary target 접근 가능성`을 계산한다.
- `makeOrders()`에서 역할별 개별 판단보다 `group objective`를 먼저 만든다.
- 우선 구현할 공통 상태:
  - group centroid
  - leader
  - primary target
  - retreat anchor

## 관련 문서

- [란체스터 법칙](./lanchester-law.md)
- [호드 결집과 집중 사격](./horde-cohesion-focus-fire.md)
- [타워 안전지대 통제](./tower-zone-control.md)

## 출처

- [Lanchester Theory - Naval Postgraduate School PDF](https://calhoun.nps.edu/bitstream/10945/52840/1/17Dec_Weber_Robert.pdf)
- [란체스터 법칙 - 나무모에 미러](https://www.namu.moe/w/%EB%9E%80%EC%B2%B4%EC%8A%A4%ED%84%B0%20%EB%B2%95%EC%B9%99)
