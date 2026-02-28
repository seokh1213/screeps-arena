package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.arena.GameObject

/**
 * Kotlin 바인딩에 아직 노출되지 않은 프로퍼티들을 안전하게 접근하기 위한 확장.
 * - id는 Arena에서 number|string일 수 있으니 문자열로 정규화.
 * - controlledBy는 시즌/모드마다 존재할 수 있어 dynamic 접근을 사용.
 */

val GameObject.idString: String get() = id.toString()

/**
 * CTF에서 Flag가 타워/링크 등을 제어하는 관계를 dynamic으로 접근.
 * 없으면 null.
 */
val GameObject.controlledById: String?
    get() = runCatching {
        this.asDynamic().controlledBy?.id?.toString() as String?
    }.getOrNull()
