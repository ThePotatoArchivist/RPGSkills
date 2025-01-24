package archives.tater.rpgskills.util

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.util.Identifier
import net.minecraft.util.Util

internal fun FabricLanguageProvider.TranslationBuilder.addSkill(id: Identifier, translation: String) {
    add(Util.createTranslationKey("skill", id), translation)
}
