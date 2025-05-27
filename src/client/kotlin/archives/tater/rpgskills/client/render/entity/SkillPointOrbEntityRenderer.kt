package archives.tater.rpgskills.client.render.entity

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.math.sin

class SkillPointOrbEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<SkillPointOrbEntity>(ctx) {
    init {
        shadowRadius = 0.15f
        shadowOpacity = 0.75f
    }

    override fun getBlockLight(entity: SkillPointOrbEntity, blockPos: BlockPos): Int = 15
        // MathHelper.clamp(super.getBlockLight(entity, blockPos) + 7, 0, 15)

    override fun render(
        entity: SkillPointOrbEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val age = (entity.age.toFloat() + tickDelta) / 4f

        matrices.push()

        val alpha = (255 * (0.5f + 0.25f * sin(age))).toInt()

//        val red = 0 // ((sin(age + 0f) + 1f) * 0.5f * 255f).toInt()
//        val green = ((sin(age) + 3f) * 0.25f * 255f).toInt()
//        val blue = 255

        matrices.translate(0f, 0.1f, 0f)
        matrices.multiply(dispatcher.rotation)
        matrices.scale(0.3f, 0.3f, 0.3f)

        val vertexConsumer = vertexConsumers.getBuffer(LAYER)
        val entry = matrices.peek()
        vertex(vertexConsumer, entry, -0.5f, -0.25f, alpha, 0f, 1f, light)
        vertex(vertexConsumer, entry, 0.5f, -0.25f, alpha, 1f, 1f, light)
        vertex(vertexConsumer, entry, 0.5f, 0.75f, alpha, 1f, 0f, light)
        vertex(vertexConsumer, entry, -0.5f, 0.75f, alpha, 0f, 0f, light)

        matrices.pop()

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    private fun vertex(
        vertexConsumer: VertexConsumer,
        matrix: MatrixStack.Entry,
        x: Float,
        y: Float,
        alpha: Int,
        u: Float,
        v: Float,
        light: Int
    ) {
        vertexConsumer.vertex(matrix, x, y, 0f)
            .color(255, 255, 255, alpha)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(matrix, 0f, 1f, 0f)
    }

    override fun getTexture(entity: SkillPointOrbEntity): Identifier = TEXTURE

    companion object {
        private val TEXTURE: Identifier = RPGSkills.id("textures/entity/skill_orb.png")
        private val LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE)
    }
}
