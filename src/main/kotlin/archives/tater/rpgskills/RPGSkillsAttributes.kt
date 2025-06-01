package archives.tater.rpgskills

import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttribute.Category
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier

object RPGSkillsAttributes {
    private fun register(identifier: Identifier, attribute: EntityAttribute): RegistryEntry<EntityAttribute> =
        Registry.registerReference(Registries.ATTRIBUTE, identifier, attribute)

    private fun register(identifier: Identifier, fallback: Double, min: Double, max: Double, category: Category = Category.POSITIVE, isTracked: Boolean = false) =
        register(
            identifier,
            ClampedEntityAttribute(identifier.toTranslationKey("attribute.name"), fallback, min, max).apply {
                setCategory(category)
                this.isTracked = isTracked
            }
        )

    private fun register(path: String, fallback: Double, min: Double, max: Double, category: Category = Category.POSITIVE, isTracked: Boolean = false) =
        register(RPGSkills.id(path), fallback, min, max, category, isTracked)

    @JvmField
    val BOW_DRAW_TIME = register("bow_draw_time", 20.0, 1.0, 100.0, Category.NEGATIVE, true) // prevent division by 0

    @JvmField
    val PROJECTILE_DIVERGENCE = register("projectile_divergence", 1.0, 0.0, 8.0, Category.NEGATIVE)

    fun register() {}
}