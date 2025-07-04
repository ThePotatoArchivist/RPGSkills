package archives.tater.rpgskills.client

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class RPGSkillsMixinConfigPlugin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String?) {
    }

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String): Boolean {
        return FabricLoader.getInstance().isModLoaded("bettercombat") || !mixinClassName.contains("bettercombat")
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