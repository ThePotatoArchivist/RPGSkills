package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.util.cataclysmId
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.entity.EntityType
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class EntityTagGenerator(
    output: FabricDataOutput,
    completableFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider.EntityTypeTagProvider(output, completableFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(RPGSkillsTags.MINIBOSS).apply {
            addOptional(cataclysmId("amethyst_crab"))
            addOptional(cataclysmId("deepling_priest"))
            addOptional(cataclysmId("deepling_warlock"))
            addOptional(cataclysmId("coral_golem"))

            addOptional(cataclysmId("the_prowler"))

            addOptional(cataclysmId("kobolediator"))
            addOptional(cataclysmId("wadjet"))

            addOptional(cataclysmId("ignited_revenant"))
//            addOptional(cataclysmId("ignited_berserker"))

            addOptional(cataclysmId("ender_golem"))

            addOptional(cataclysmId("aptrgangr"))

            // TODO Scylla miniboss(es)
        }
        getOrCreateTagBuilder(RPGSkillsTags.BASIC_BOSS).add(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER
        )
        getOrCreateTagBuilder(RPGSkillsTags.EARLY_BOSS).apply {
            addOptional(cataclysmId("netherite_monstrosity"))
            addOptional(cataclysmId("ender_guardian"))
        }
        getOrCreateTagBuilder(RPGSkillsTags.MID_BOSS).apply {
            addOptional(cataclysmId("the_leviathan"))
            addOptional(cataclysmId("the_harbinger"))
            addOptional(cataclysmId("ancient_remnant"))
            addOptional(cataclysmId("ancient_ancient_remnant"))
            addOptional(cataclysmId("scylla"))
            addOptional(cataclysmId("maledictus"))
        }
        getOrCreateTagBuilder(RPGSkillsTags.FINAL_BOSS).apply {
            addOptional(cataclysmId("ignis"))
        }
        getOrCreateTagBuilder(RPGSkillsTags.DLC_BOSS).apply {
            addOptional(Identifier.of("fdbossess", "chesed"))
        }
        getOrCreateTagBuilder(RPGSkillsTags.IGNORES_SKILL_SOURCE).apply {
            forceAddTag(RPGSkillsTags.MINIBOSS)
            forceAddTag(RPGSkillsTags.BASIC_BOSS)
            forceAddTag(RPGSkillsTags.EARLY_BOSS)
            forceAddTag(RPGSkillsTags.MID_BOSS)
            forceAddTag(RPGSkillsTags.FINAL_BOSS)
            forceAddTag(RPGSkillsTags.DLC_BOSS)
        }
    }
}