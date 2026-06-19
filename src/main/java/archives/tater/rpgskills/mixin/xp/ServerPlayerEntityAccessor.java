package archives.tater.rpgskills.mixin.xp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {
    @Invoker
    void invokeUpdateScores(ScoreboardCriterion criterion, int score);
}
