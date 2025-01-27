package archives.tater.rpgskills.datagen.defaultpack

import archives.tater.rpgskills.data.addLockCategory
import archives.tater.rpgskills.datagen.defaultpack.DefaultSkillsTagGenerator.Companion.LOCKED_BUTTONS
import archives.tater.rpgskills.util.addSkill
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.util.Identifier

class DefaultSkillsLanguageGenerator(dataOutput: FabricDataOutput) : FabricLanguageProvider(dataOutput) {
    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        translationBuilder.addSkill(Identifier("rpg_test", "potato"), "Potato Skill")
        translationBuilder.addLockCategory(LOCKED_BUTTONS, "Unknown Button", "You aren't sure what kind of button this is")
    }
}
