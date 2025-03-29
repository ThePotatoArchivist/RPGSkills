package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.RPGSkillsCommands
import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.util.add
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider

class LangGenerator(dataOutput: FabricDataOutput) : FabricLanguageProvider(dataOutput) {
    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        val s = "\$s"
        translationBuilder.add(RPGSkillsCommands.LIST_NONE, "There are no skills")
        translationBuilder.add(RPGSkillsCommands.LIST, "There are %s skills: %s")
        translationBuilder.add(RPGSkillsCommands.GET_LEVEL, "%s has %3$s levels in skill %2$s")
        translationBuilder.add(RPGSkillsCommands.SET_LEVEL, "Set %3$s levels in skill %2$s on %1$s")
        translationBuilder.add(RPGSkillsCommands.ADD_LEVEL, "Gave %3$s levels in skill %2$s to %1$s")
        translationBuilder.add(RPGSkillsCommands.ADD_POINTS, "Gave %2$s level points to %s")
        translationBuilder.add(RPGSkillsCommands.SET_POINTS, "Set %2$s level points on %s")
        translationBuilder.add(SkillsScreen.TITLE, "Skills")
        translationBuilder.add(ItemLockTooltip.REQUIRES, "Requires:")
        translationBuilder.add(ItemLockTooltip.REQUIRES_ANY, "Requires either:")
        translationBuilder.add(ItemLockTooltip.REQUIREMENT, "- %s")
        translationBuilder.add(SkillUpgradeButton.MAX, "MAX")
        translationBuilder.add(RPGSkillsClient.RPG_SKILLS_CATEGORY, "RPG Skills")
        translationBuilder.add(RPGSkillsClient.SKILLS_KEY_TRANSLATION, "Open Skills")
    }
}