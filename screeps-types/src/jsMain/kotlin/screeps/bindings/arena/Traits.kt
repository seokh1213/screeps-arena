package screeps.bindings.arena

import screeps.bindings.arena.game.Goal

external interface HasPosition : Goal {
    var x: Double
    var y: Double
}

typealias Position = HasPosition
