@file:JsModule("game/prototypes")
@file:JsNonModule

package screeps.bindings.arena.game

import screeps.bindings.arena.ConstructionSite
import screeps.bindings.arena.Creep
import screeps.bindings.arena.GameObject
import screeps.bindings.arena.Resource
import screeps.bindings.arena.Source
import screeps.bindings.arena.Structure
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureExtension
import screeps.bindings.arena.StructureRampart
import screeps.bindings.arena.StructureRoad
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.StructureWall

@JsName("Creep")
external object PrototypeCreep : Prototype<Creep>

@JsName("GameObject")
external object PrototypeGameObject : Prototype<GameObject>

@JsName("Structure")
external object PrototypeStructure : Prototype<Structure>

@JsName("StructureSpawn")
external object PrototypeStructureSpawn : Prototype<StructureSpawn>

@JsName("StructureTower")
external object PrototypeStructureTower : Prototype<StructureTower>

@JsName("StructureWall")
external object PrototypeStructureWall : Prototype<StructureWall>

@JsName("StructureContainer")
external object PrototypeStructureContainer : Prototype<StructureContainer>

@JsName("Source")
external object PrototypeSource : Prototype<Source>

@JsName("Resource")
external object PrototypeResource : Prototype<Resource>

@JsName("StructureRampart")
external object PrototypeStructureRampart : Prototype<StructureRampart>

@JsName("ConstructionSite")
external object PrototypeConstructionSite : Prototype<ConstructionSite>

@JsName("StructureExtension")
external object PrototypeStructureExtension : Prototype<StructureExtension>

@JsName("StructureRoad")
external object PrototypeStructureRoad : Prototype<StructureRoad>
