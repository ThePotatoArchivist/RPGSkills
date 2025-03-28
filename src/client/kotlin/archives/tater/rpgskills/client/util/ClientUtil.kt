@file:Environment(EnvType.CLIENT)

package archives.tater.rpgskills.client.util

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.client.option.KeyBinding
import net.minecraft.util.Identifier
import net.minecraft.util.Util

internal fun FabricLanguageProvider.TranslationBuilder.addSkill(id: Identifier, translation: String) {
    add(Util.createTranslationKey("skill", id), translation)
}

val KeyBinding.wasPressed get() = wasPressed()