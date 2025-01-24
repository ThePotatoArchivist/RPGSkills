package archives.tater.rpgskills.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.data.DataOutput

abstract class SkillProvider(
    dataOutput: FabricDataOutput,
) : FabricCodecDataProvider<Skill>(dataOutput, DataOutput.OutputType.DATA_PACK, "rpgskills/skills", Skill.CODEC) {
	override fun getName(): String = "Skills"
}
