package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.fieldFor
import archives.tater.rpgskills.util.mutateCollection
import archives.tater.rpgskills.util.recordMutationCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
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
        CODEC.update(NbtOps.INSTANCE, tag, this)
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, NbtOps.INSTANCE, tag)
    }

    companion object : ComponentKeyHolder<StructuresSkillSourceComponent, World> {
        val CODEC = recordMutationCodec(
            Entry.CODEC.mutateCollection().fieldFor("structures", StructuresSkillSourceComponent::structures)
        )

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
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create { it.group(
                BlockBox.CODEC.fieldOf("box").forGetter(Entry::box),
                RegistryKey.createCodec(RegistryKeys.STRUCTURE).fieldOf("structure").forGetter(Entry::structure),
                SkillSourceComponent.createCodec().fieldOf("component").forGetter(Entry::component),
            ).apply(it, ::Entry) }
        }
    }
}

