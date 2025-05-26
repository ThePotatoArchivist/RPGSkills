package archives.tater.rpgskills.client.render.entity

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
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.MathHelper.sin

class SkillPointOrbEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<SkillPointOrbEntity>(ctx) {
    init {
        shadowRadius = 0.15f
        shadowOpacity = 0.75f
    }

    override fun getBlockLight(entity: SkillPointOrbEntity, blockPos: BlockPos): Int =
        MathHelper.clamp(super.getBlockLight(entity, blockPos) + 7, 0, 15)

    override fun render(
        entity: SkillPointOrbEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val age = (entity.age.toFloat() + tickDelta) / 2f

        matrices.push()

        val red = 0 // ((sin(age + 0f) + 1f) * 0.5f * 255f).toInt()
        val green = ((sin(age) + 3f) * 0.25f * 255f).toInt()
        val blue = 255

        matrices.translate(0f, 0.1f, 0f)
        matrices.multiply(dispatcher.rotation)
        matrices.scale(0.3f, 0.3f, 0.3f)

        val vertexConsumer = vertexConsumers.getBuffer(LAYER)
        val entry = matrices.peek()
        vertex(vertexConsumer, entry, -0.5f, -0.25f, red, green, blue, 0.25f, 0.25f, light)
        vertex(vertexConsumer, entry, 0.5f, -0.25f, red, green, blue, 0.5f, 0.25f, light)
        vertex(vertexConsumer, entry, 0.5f, 0.75f, red, green, blue, 0.5f, 0f, light)
        vertex(vertexConsumer, entry, -0.5f, 0.75f, red, green, blue, 0.25f, 0f, light)

        matrices.pop()

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    private fun vertex(
        vertexConsumer: VertexConsumer,
        matrix: MatrixStack.Entry,
        x: Float,
        y: Float,
        red: Int,
        green: Int,
        blue: Int,
        u: Float,
        v: Float,
        light: Int
    ) {
        vertexConsumer.vertex(matrix, x, y, 0f)
            .color(red, green, blue, 128)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(matrix, 0f, 1f, 0f)
    }

    override fun getTexture(entity: SkillPointOrbEntity): Identifier = TEXTURE

    companion object {
        private val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/experience_orb.png")
        private val LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE)
    }
}
