package archives.tater.rpgskills.client.render.entity

import archives.tater.rpgskills.entity.SkillPointOrbEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.util.Identifier

class SkillPointOrbEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<SkillPointOrbEntity>(ctx) {

    override fun getTexture(entity: SkillPointOrbEntity): Identifier = TEXTURE

    companion object {
        private val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/experience_orb.png")
        private val LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE)
    }
}