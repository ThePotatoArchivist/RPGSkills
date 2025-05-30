package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.mapToNbt
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockBox
import net.minecraft.world.World
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureKeys
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry

class StructuresSkillSourceComponent(val world: World) : Component {
    val structures = mutableListOf<Entry>()

    fun getOrCreate(box: BlockBox, structure: RegistryKey<Structure>): SkillSourceComponent =
        (structures.firstOrNull { it.box == box && it.structure.value == structure.value }
            ?: Entry(box, structure, SkillSourceComponent(getInitialPoints(structure))).also(structures::add)
        ).component

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
//        (world as ServerWorld).structureAccessor.getStructureContaining(BlockPos.ORIGIN, RPGSkillsTags.HAS_SKILL_POOL_STRUCTURE).structure
        tag.put("structures", structures.mapToNbt { NbtCompound().apply {
            put("box", BlockBox.CODEC.encodeStart(NbtOps.INSTANCE, it.box).orThrow)
            putString("structure", it.structure.value.toString())
            put("component", NbtCompound().apply { it.component.writeToNbt(this, registryLookup) })
        } })
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        structures.clear()
        for (compound in tag.getList("structures", NbtElement.COMPOUND_TYPE.toInt())) {
            compound as NbtCompound
            val structure = RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.of(compound.getString("structure")))
            structures.add(Entry(
                BlockBox.CODEC.decode(NbtOps.INSTANCE, compound.get("box")).orThrow.first,
                structure,
                SkillSourceComponent(getInitialPoints(structure)).apply {
                    readFromNbt(tag.getCompound("component"), registryLookup)
                }
            ))
        }
    }

    companion object : ComponentKeyHolder<StructuresSkillSourceComponent, World> {
        override val key: ComponentKey<StructuresSkillSourceComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("structures_skill_source"), StructuresSkillSourceComponent::class.java)

        @JvmField
        val KEY = key

        fun getInitialPoints(structure: RegistryKey<Structure>) = when(structure) {
            StructureKeys.MANSION -> 100
            else -> 40
        }
    }

    class Entry(
        val box: BlockBox,
        val structure: RegistryKey<Structure>,
        val component: SkillSourceComponent,
    )
}

