# 플래그 우선 타워 템포

## 문서 메타

- 전략명: 플래그 우선 타워 템포
- 분류: `operational`
- 상태: `draft`
- 우선순위: `high`
- 근거 수준: `rules-backed`

## 한 줄 요약

중립 플래그를 먼저 안정적으로 확보하고, 연결된 타워 템포를 살린 유리한 교전만 반복해 BodyPart와 짧은 역공으로 승부를 굳힌다.

## 해결하려는 문제

- 대칭 미러에서 초반 병력 분할이 어설퍼 한 축이 먼저 무너지는 문제
- 중립 플래그를 먹고도 타워를 비워 둬 가장 큰 보상을 못 쓰는 문제
- 플래그 리드를 잡고도 BodyPart와 worker를 계속 내줘 마지막 푸시에 뚫리는 문제
- 적 safe zone 깊숙이 chase하다 교환비와 위치를 동시에 잃는 문제

## 핵심 가설

- 이 모드는 `킬 수`보다 `플래그 수 변화`가 더 중요한 승리 기준이다.
- 중립 플래그는 점수만이 아니라 타워 ownership과 전장 기준점을 함께 앞으로 당긴다.
- 타워는 지속 화력보다 `burst swing` 가치가 높으므로, worker 생존과 tower energy 유지가 핵심이다.
- BodyPart는 한타 직후 살아남은 쪽의 질적 우세를 누적시키는 스노우볼 장치다.
- 플래그 리드를 잡은 뒤에는 deep dive보다 elastic defense와 짧은 counterpush가 더 재현성이 높다.

## 운영 규칙

1. 오프닝의 첫 목표는 enemy home flag rush가 아니라 `중립 1개 안정 확보 + 나머지 축 공짜 방치 금지`다.
2. worker 한 기는 초반 home tower를 먼저 켜고, 다른 worker는 주력 축을 따라가 새로 먹은 타워를 최대한 빨리 활성화한다.
3. 전투 병력은 기본적으로 `주력 70~80%, 보조 20~30%`로 나눈다.
4. 중립 플래그를 먹은 직후에는 blind chase 대신 `tower charge -> formation reset -> 유리 구역 유인` 순서를 따른다.
5. melee는 bodyblock과 최종 flag touch, ranger는 focus fire와 exposed target 압박, healer는 main core 유지에 우선 배정한다.
6. 전장은 `our safe zone`, `river zone`, `enemy safe zone`으로 나누고, 기본 교전 anchor는 우리 타워-우리 flag-강 입구 선상에 둔다.
7. BodyPart 회수는 `먹을 수 있는가`보다 `먹고 heal 범위로 복귀할 수 있는가`로 판단한다.
8. 기본 우선순위는 `MOVE > 역할 일치 파츠 > 그 외 파츠`로 둔다.
9. 플래그 리드 이후에는 tower energy, worker 생존, home guard를 먼저 지키고 깊은 적지 추격은 줄인다.
10. behind 상태나 막판에는 교환비보다 `한 번의 flag touch 성공 확률`을 더 높게 본다.

## 적용 조건

- 초기 병력이 고정이고, 중립 플래그와 연동된 타워가 전장 중앙 또는 전진 거점 역할을 할 때
- 타워 burst와 worker logistics가 전투 국면을 실제로 바꿀 수 있을 때
- BodyPart 스노우볼이 draw 성향의 mirror를 깨는 유효 수단일 때

## 지속가능성 체크

- 장점: 대칭 맵에서 가장 재현성이 높은 기본 운영 플랜이다.
- 장점: 타워, worker, BodyPart, final push를 하나의 의사결정 체계로 묶을 수 있다.
- 위험: 너무 보수적으로 굴면 중립만 나눠 먹고 draw bot으로 굳을 수 있다.
- 위험: bodypart greed로 formation이 찢어지면 전략 핵심이 무너진다.
- 위험: 타워 연결 정보를 잘못 읽으면 잘못된 worker tempo를 만들 수 있다.

## 관측 지표

- 첫 중립 플래그 확보 시점
- 중립 확보 후 첫 tower charge 완료 시점
- 플래그 리드 상태에서 enemy safe zone deep chase 빈도
- worker 생존 시간과 tower energy 유지 시간
- BodyPart 확보량과 확보 직후 사망률
- last push window 개시 시점과 실제 flag touch 성공률

## 실패 시그널과 카운터

- 실패 시그널: 중립 플래그를 먹고도 타워가 장시간 비어 있음
- 실패 시그널: 플래그 리드인데도 worker를 교환당하며 타워 템포를 잃음
- 실패 시그널: bodypart 회수 때문에 본대가 두 덩어리로 찢어짐
- 실패 시그널: behind 상태인데도 계속 safe defense만 반복함
- 상대 카운터: 중앙 BodyPart 선점, worker 저격, deep split push, tower edge bait
- 대응 보정: `현재 flag count`, `tower energy`, `bodypart swing`, `남은 시간`을 함께 본다

## 봇 구현 메모

- phase는 최소한 `INITIAL`, `IN_BATTLE`, `CONQUERING`을 상황에 따라 실제로 나눠야 한다.
- objective flag는 `neutral 우선`, 그다음 `enemy flag` 또는 `가장 위협받는 my flag`로 전환한다.
- worker는 `harvest -> tower charge`를 기본 루프로 두고, 특히 charge 대상 tower의 우선순위를 명시해야 한다.
- melee/ranger/healer는 모두 같은 objective를 보되, healer는 wounded support target을 우선하고 ranger는 opportunistic focus fire를 가져야 한다.
- BodyPart 회수는 `nearby + safe enough + phase pressure 낮음`일 때만 오버라이드로 허용한다.

## 관련 문서

- [타워 안전지대 통제](./tower-zone-control.md)
- [강 중앙 BodyPart 통제](./river-bodypart-control.md)
- [탄력 방어와 역공](./elastic-defense-counterpush.md)
- [호드 결집과 집중 사격](./horde-cohesion-focus-fire.md)

## 출처

- [Capture The Flag Rules (Project Baseline)](../capture-the-flag-rules.md)
- [Screeps Arena 공식 문서](https://arena.screeps.com/docs#)
