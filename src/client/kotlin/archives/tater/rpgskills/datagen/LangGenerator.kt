package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.RPGSkillsCommands
import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider

class LangGenerator(dataOutput: FabricDataOutput) : FabricLanguageProvider(dataOutput) {
    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        val s = "\$s"
        translationBuilder.add(RPGSkillsCommands.Translations.LIST_NONE, "There are no skills")
        translationBuilder.add(RPGSkillsCommands.Translations.LIST, "There are %s skills: %s")
        translationBuilder.add(RPGSkillsCommands.Translations.GET_LEVEL, "%s has %3$s levels in skill %2$s")
        translationBuilder.add(RPGSkillsCommands.Translations.SET_LEVEL, "Set %3$s levels in skill %2$s on %1$s")
        translationBuilder.add(RPGSkillsCommands.Translations.ADD_LEVEL, "Gave %3$s levels in skill %2$s to %1$s")
        translationBuilder.add(RPGSkillsCommands.Translations.ADD_POINTS, "Gave %2$s level points to %s")
        translationBuilder.add(RPGSkillsCommands.Translations.SET_POINTS, "Set %2$s level points on %s")
        translationBuilder.add(SkillsScreen.NAME_TRANSLATION, "Skills")
    }
}