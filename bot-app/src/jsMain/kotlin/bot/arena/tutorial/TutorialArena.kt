package bot.arena.tutorial

class TutorialArena {

    fun loop() {
        /**
         * 상황:
         * - spawn 1개 (최초 에너지 500, 틱당 1 증가)
         * - 에너지 소스 1000 1개
         * - 적 개체 3개 좌상단. (healer(move, heal), melee(move, attack), ranger(move, rangedAttack))
         *
         * body part 당 3틱
         *   • TOUGH: 10 에너지
         *   • MOVE: 50 에너지
         *   • CARRY: 50 에너지
         *   • ATTACK: 80 에너지
         *   • WORK: 100 에너지
         *   • RANGED_ATTACK: 150 에너지
         *   • HEAL: 250 에너지
         *
         * 전략:
         * - 빠른 드론 만들어서 캐서 이동하게 한다. 최대 3명이 캘 수 있는듯? (<--- 추후 비싼 일꾼으로 바꿔야함)
         * -
         */

    }

}
