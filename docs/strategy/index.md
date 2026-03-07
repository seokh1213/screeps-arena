# CTF 전략/전술 인덱스

이 디렉터리는 `Screeps Arena Capture The Flag` 전투 전략을 장기적으로 누적·검증하기 위한 문서 모음이다. 목적은 아이디어를 한 번 적고 잊는 것이 아니라, 이론과 리플레이 관찰을 같은 포맷으로 쌓아 `지속가능한 전술`만 남기는 데 있다.

## 사용 원칙

1. 새 아이디어는 먼저 [템플릿](./template.md)으로 초안 문서를 만든다.
2. 기존 전략을 복제하지 말고, 어떤 가설이 다른지부터 분리해서 적는다.
3. 교전 방식은 반드시 `언제 유리한지`, `언제 버려야 하는지`, `봇에 어떻게 넣을지`까지 적는다.
4. 사후 평가는 느낌 대신 `결집도`, `교환비`, `타워 에너지`, `BodyPart 확보량`, `플래그 변화`로 남긴다.

## 문서 지도

| 문서 | 분류 | 핵심 | 초기 판단 |
| --- | --- | --- | --- |
| [란체스터 법칙](./lanchester-law.md) | foundation | 병력과 화력 집중이 전투 결과를 어떻게 증폭시키는지 설명하는 기준 문서 | 반드시 공통 참조 |
| [전력 집중](./force-concentration.md) | foundation | 한 번에 더 많은 화력과 치유를 같은 접촉점에 실어 local superiority를 만드는 원칙 | 가장 먼저 자동화할 가치가 큼 |
| [각개격파](./defeat-in-detail.md) | derived | 적이 합류하기 전에 분리된 적을 하나씩 제거 | 란체스터 문서에서 직접 추출 |
| [공격의 다차원화](./multi-axis-attack.md) | derived | 하나의 전선이 아니라 여러 화력축을 동시에 형성 | 란체스터 문서에서 직접 추출 |
| [질적 우세](./qualitative-overmatch.md) | derived | 숫자 대신 사거리, 조합, 훈련도, 제어 품질로 교환비를 개선 | 란체스터 문서에서 직접 추출 |
| [비대칭 능력 활용](./asymmetric-capability.md) | derived | 연속 소모전이 아니라 불연속적 승부 포인트를 찌름 | 란체스터 문서에서 직접 추출 |
| [타워 안전지대 통제](./tower-zone-control.md) | tactical | 유리한 교전 구역을 우리 타워와 플래그 주변으로 고정 | CTF 특화, 기본 전술 |
| [강 중앙 BodyPart 통제](./river-bodypart-control.md) | tactical | 중앙 리소스를 먹되 산개는 최소화 | CTF 특화, 스노우볼 전술 |
| [호드 결집과 집중 사격](./horde-cohesion-focus-fire.md) | tactical | 뭉친 채 이동하고 같은 적을 때리며 필요한 만큼만 치유 | 실제 전투 방식 핵심 |
| [탄력 방어와 역공](./elastic-defense-counterpush.md) | tactical | 적의 안전지대 깊숙한 곳은 피하고, 수비 성공 뒤 역공하거나 시간 압박에 맞춰 찌름 | 장기적으로 가장 안정적 |

## 추천 읽기 순서

1. [란체스터 법칙](./lanchester-law.md)
2. [전력 집중](./force-concentration.md)
3. [각개격파](./defeat-in-detail.md)
4. [타워 안전지대 통제](./tower-zone-control.md)
5. [호드 결집과 집중 사격](./horde-cohesion-focus-fire.md)
6. 나머지 파생/상황 전략

## 운영 체크리스트

- 교전 직전: 우리 병력이 한 점에 모였는가
- 교전 직전: 우리 타워, 중립 타워, 강 중앙 BodyPart 중 어디가 승부처인가
- 교전 중: 같은 적에게 화력과 치유가 겹치고 있는가
- 교전 중: 상대를 우리 안전지대로 끌고 왔는가, 아니면 우리가 상대 안전지대로 말려 들어갔는가
- 교전 후: 살아남은 병력 수보다 `남은 기능 파츠 수`가 더 중요한가
- 경기 막판: 비기면 충분한가, 반드시 깃발을 만져야 하는가

## 코드 연결 지점

- 현재 전투 판단의 중심은 [Overmind.kt](../../bot-app/src/jsMain/kotlin/bot/arena/mode/capturetheflag/Overmind.kt) 에 있다.
- 상태 분류는 [Phase.kt](../../bot-app/src/jsMain/kotlin/bot/arena/mode/capturetheflag/model/Phase.kt), 관측 데이터는 [Context.kt](../../bot-app/src/jsMain/kotlin/bot/arena/mode/capturetheflag/model/Context.kt), 지속 메모리는 [Memory.kt](../../bot-app/src/jsMain/kotlin/bot/arena/mode/capturetheflag/memory/Memory.kt) 기준으로 연결하면 된다.
- 따라서 각 전략 문서의 `봇 구현 메모`는 결국 `phase 판정`, `target selection`, `movement formation`, `commit / retreat gate` 네 축으로 번역되어야 한다.

## 출처

- [란체스터 법칙 - 나무모에 미러](https://www.namu.moe/w/%EB%9E%80%EC%B2%B4%EC%8A%A4%ED%84%B0%20%EB%B2%95%EC%B9%99)
- [Lanchester Theory - Naval Postgraduate School PDF](https://calhoun.nps.edu/bitstream/10945/52840/1/17Dec_Weber_Robert.pdf)
- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
- [Screeps #25: Arena - Pressing the Attack](https://jonwinsley.com/notes/screeps-arena-pressing-attack)
- [Screeps #25: Arena - Grouping Up](https://jonwinsley.com/notes/screeps-arena-grouping-up)
