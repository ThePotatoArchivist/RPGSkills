package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.RPGSkillsAttributes
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.RPGSkillsCommands
import archives.tater.rpgskills.cca.BossTrackerComponent
import archives.tater.rpgskills.client.gui.JobCompletedToast
import archives.tater.rpgskills.client.gui.screen.ClassScreen
import archives.tater.rpgskills.client.gui.screen.JobsScreen
import archives.tater.rpgskills.client.gui.screen.SkillScreen
import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.client.gui.widget.*
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.item.RPGSkillsItems
import archives.tater.rpgskills.util.add
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class LangGenerator(dataOutput: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) :
    FabricLanguageProvider(dataOutput, registryLookup) {

    override fun generateTranslations(registryLookup: RegistryWrapper.WrapperLookup, translationBuilder: TranslationBuilder) {
        val s = $$"$s"
        with(translationBuilder) {
            add(RPGSkillsCommands.LIST_SKILLS_NONE, "There are no skills")
            add(RPGSkillsCommands.LIST_SKILLS, "There are %s skills: %s")
            add(RPGSkillsCommands.GET_LEVEL, "%s has %3$s levels in skill %2$s")
            add(RPGSkillsCommands.SET_LEVEL, "Set %3$s levels in skill %2$s on %1$s")
            add(RPGSkillsCommands.ADD_LEVEL, "Gave %3$s levels in skill %2$s to %1$s")
            add(RPGSkillsCommands.ADD_POINTS, "Gave %2$s level points to %s")
            add(RPGSkillsCommands.SET_POINTS, "Set %2$s level points on %s")
            add(RPGSkillsCommands.RESET_CLASS, "Reset class for %s")
            add(RPGSkillsCommands.SET_CLASS, "Set class for %s to %s")
            add(RPGSkillsCommands.LIST_BOSSES, "%s/%s bosses have been defeated: %s")
            add(RPGSkillsCommands.LIST_BOSSES_ALL, "There are %s bosses: %s")
            add(RPGSkillsCommands.RESET_BOSSES, "Reset defeated bosses")
            add(BossTrackerComponent.BOSS_DEFEAT_TITLE, "GREAT ENEMY FELLED")
            add(BossTrackerComponent.BOSS_DEFEAT_MESSAGE, "%s was vanquished!")
            add(BossTrackerComponent.ENEMIES_STRENGTHEN_MESSAGE, "Enemies became stronger")
            add(BossTrackerComponent.CAP_RAISE_MESSAGE, "Max level is now %s")
            add(BossTrackerComponent.CAP_REMOVED_MESSAGE, "There is no longer a level limit")
            add(SkillsScreen.TITLE, "Skills")
            add(SkillsScreen.LEVEL, "Level %s")
            add(SkillsScreen.PROGRESS, "%s/%s Skill Points")
            add(SkillsScreen.LEVEL_CAP_HINT, "Defeat great enemies to increase the level cap!")
            add(SkillTabWidget.TOOLTIP, "Level %s")
            add(ClassScreen.SELECT, "Select Class")
            add(JobsScreen.ACTIVE, "Active Jobs %s/%s")
            add(JobsScreen.AVAILABLE, "Available Jobs %s/%s")
            add(ActiveJobWidget.INCOMPLETE_TASK, "\u2610")
            add(ActiveJobWidget.COMPLETE_TASK, "\u2611")
            add(ActiveJobWidget.TASK_PROGRESS, "%s/%s")
            add(ActiveJobWidget.CANCEL, "Cancel Job")
            add(SkillWidget.SKILL_LEVEL, "%s/%s")
            add(SkillScreen.LEVEL, "Level %s")
            add(LockGroupWidget.Texts.ITEMS, "Items")
            add(LockGroupWidget.Texts.BLOCKS, "Blocks")
            add(LockGroupWidget.Texts.ENTITIES, "Entities")
            add(LockGroupWidget.Texts.ENCHANTMENTS, "Enchantments")
            add(LockGroupWidget.Texts.RECIPES, "Recipes")
            add(AttributesWidget.TITLE, "Attributes")
            add(JobUnlockWidget.TITLE, "Unlocks Job: %s")
            add(AvailableJobWidget.TOOLTIP_HINT, "Click to assign")
            add(AbstractJobWidget.TASK_COUNT, "%sx")
            add(AbstractJobWidget.TASK, "%s %s")
            add(JobCompletedToast.TITLE, "Job Complete!")
            add(LockGroup.DEFAULT_ITEM_NAME, "Unknown Item")
            add(LockGroup.DEFAULT_ITEM_MESSAGE, "You don't know how to use this item")
            add(LockGroup.DEFAULT_BLOCK_MESSAGE, "You don't know how to use this block")
            add(LockGroup.DEFAULT_ENTITY_MESSAGE, "You don't know how to interact with this mob")
            add(LockGroup.DEFAULT_ENCHANTMENT_MESSAGE, "You don't know how to apply this enchantment")
            add(LockGroup.DEFAULT_RECIPE_MESSAGE, "You don't know how to craft this recipe")
            add(ItemLockTooltip.REQUIRES, "Requires:")
            add(ItemLockTooltip.REQUIRES_ANY, "Requires either:")
            add(ItemLockTooltip.REQUIREMENT, "- %s")
            add(SkillUpgradeButton.MAX, "MAX")
            add(RPGSkillsClient.RPG_SKILLS_CATEGORY, "RPG Skills")
            add(RPGSkillsClient.SKILLS_KEY_TRANSLATION, "Open Skills")
            add(RPGSkillsAttributes.BOW_DRAW_TIME, "Bow Draw Time")
            add(RPGSkillsAttributes.PROJECTILE_DIVERGENCE, "Projectile Divergence")
            add(RPGSkillsItems.RESPEC_ITEM, "Respec Item")
        }
    }
}
