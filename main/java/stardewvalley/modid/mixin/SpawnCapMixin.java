package stardewvalley.modid.mixin;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stardewvalley.modid.equipment.SpawnCapModifier;

@Mixin(SpawnHelper.Info.class)
public class SpawnCapMixin {

    @Redirect(method = "isBelowCap", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/SpawnGroup;getCapacity()I"))
    private int redirectCapacity(SpawnGroup group) {
        int cap = group.getCapacity();
        if (group != SpawnGroup.MONSTER) return cap;
        float mult = SpawnCapModifier.getMultiplier();
        return mult != 1.0f ? Math.max(1, (int) (cap * mult)) : cap;
    }
}
