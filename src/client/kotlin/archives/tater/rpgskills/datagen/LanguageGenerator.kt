package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.data.DEFAULT_LOCK_CATEGORY
import archives.tater.rpgskills.data.addLockCategory
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider

class LanguageGenerator(dataOutput: FabricDataOutput) : FabricLanguageProvider(dataOutput) {
    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        translationBuilder.addLockCategory(DEFAULT_LOCK_CATEGORY, "Unidentified Item", "You aren't sure how to use this item")
    }
}
