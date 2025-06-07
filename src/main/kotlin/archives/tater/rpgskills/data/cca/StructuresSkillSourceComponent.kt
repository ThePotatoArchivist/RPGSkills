package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.SkillPointConstants
import archives.tater.rpgskills.util.*
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.math.BlockBox
import net.minecraft.world.World
import net.minecraft.world.gen.structure.Structure
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry

class StructuresSkillSourceComponent(val world: World) : Component {
    private val structures = mutableListOf<Entry>()

    fun getOrCreate(box: BlockBox, structure: RegistryKey<Structure>): SkillSourceComponent =
        (structures.firstOrNull { it.box == box && it.structure == structure }
            ?: Entry(box, structure, SkillSourceComponent(SkillPointConstants.getStructurePoints(structure))).also(structures::add)
        ).component

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(this, tag).logIfError()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag).logIfError()
    }

    companion object : ComponentKeyHolder<StructuresSkillSourceComponent, World> {
        val CODEC = recordMutationCodec(
            Entry.CODEC.mutateCollection().fieldFor("structures", StructuresSkillSourceComponent::structures)
        )

        override val key: ComponentKey<StructuresSkillSourceComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("structures_skill_source"), StructuresSkillSourceComponent::class.java)
    }

    class Entry(
        val box: BlockBox,
        val structure: RegistryKey<Structure>,
        val component: SkillSourceComponent,
    ) {
        companion object {
            val CODEC: Codec<Entry> = RecordCodecBuilder.create { it.group(
                BlockBox.CODEC.fieldOf("box").forGetter(Entry::box),
                RegistryKey.createCodec(RegistryKeys.STRUCTURE).fieldOf("structure").forGetter(Entry::structure),
                SkillSourceComponent.createCodec().fieldOf("component").forGetter(Entry::component),
            ).apply(it, ::Entry) }
        }
    }
}

