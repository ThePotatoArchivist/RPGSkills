package archives.tater.rpgskills

import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import net.bettercombat.api.WeaponAttributesHelper.override
import org.objectweb.asm.tree.ClassNode

class RPGSkillsMixinConfigPlugin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String?) {
    }

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String): Boolean = when {
        mixinClassName.contains("bettercombat") -> FabricLoader.getInstance().isModLoaded("bettercombat")
        mixinClassName.contains("spellengine") -> FabricLoader.getInstance().isModLoaded("spell_engine")
        mixinClassName.contains("cataclysm") -> FabricLoader.getInstance().isModLoaded("cataclysm")
        mixinClassName.contains("penchant") -> FabricLoader.getInstance().isModLoaded("penchant")
        mixinClassName.contains("brewinandchewin") -> FabricLoader.getInstance().isModLoaded("brewinandchewin")
        mixinClassName.contains("chipped") -> FabricLoader.getInstance().isModLoaded("chipped")
        else -> true
    }

    override fun acceptTargets(myTargets: MutableSet<String>?, otherTargets: MutableSet<String>?) {
    }

    override fun getMixins(): MutableList<String>? = null

    override fun preApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
    }

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
    }
}