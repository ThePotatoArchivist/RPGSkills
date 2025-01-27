package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : PlayerComponent<SkillsComponent>, AutoSyncedComponent {
    private var _levels = mutableMapOf<RegistryEntry<Skill>, Int>()
    val levels: Map<RegistryEntry<Skill>, Int> get() = _levels

    operator fun get(skill: RegistryEntry<Skill>) = _levels.getOrDefault(skill, 0)
    operator fun set(skill: RegistryEntry<Skill>, level: Int) {
        _levels[skill] = level.coerceIn(0, skill.value.levels.size)
        key.sync(player)
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean =
        sameCharacter

    override fun copyFrom(other: SkillsComponent) {
        _levels = other._levels
    }

    override fun readFromNbt(tag: NbtCompound) {
        _levels = tag.getCompound("Levels").run {
            keys.associate { player.world.registryManager[Skill].getEntry(RegistryKey.of(Skill.key, Identifier.tryParse(it))).get() to getInt(it) }
        }.toMutableMap()
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("Levels", NbtCompound().apply {
            for ((skill, level) in _levels) {
                putInt(skill.key.get().value.toString(), level)
            }
        })
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity> {
        override val key: ComponentKey<SkillsComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)
    }
}
