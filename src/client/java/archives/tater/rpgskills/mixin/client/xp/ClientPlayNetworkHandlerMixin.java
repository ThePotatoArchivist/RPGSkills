package archives.tater.rpgskills.mixin.client.xp;

import archives.tater.rpgskills.client.render.SkillBarRenderer;
import archives.tater.rpgskills.entity.SkillPointOrbEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @WrapOperation(
            method = "onItemPickupAnimation",
            constant = @Constant(classValue = ExperienceOrbEntity.class)
    )
    private boolean playXpSound(Object object, Operation<Boolean> original) {
        return original.call(object) || object instanceof SkillPointOrbEntity;
    }
}
