package stardewvalley.modid.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.equipment.SpawnCapModifier;

@Mixin(ServerWorld.class)
public class WorldTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        SpawnCapModifier.tick(world);
    }
}
