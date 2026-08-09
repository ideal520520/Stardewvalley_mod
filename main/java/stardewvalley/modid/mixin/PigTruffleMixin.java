package stardewvalley.modid.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.AnimalQualityHelper;
import stardewvalley.modid.AnimalFriendshipManager;
import stardewvalley.modid.GoldenAnimalCrackerManager;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.season.LuckManager;

@Mixin(AnimalEntity.class)
public class PigTruffleMixin {

    @Unique
    private int truffleTime = -1;

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void onTickMovement(CallbackInfo ci) {
        if (!((Object)this instanceof PigEntity pig)) return;
        if (pig.getEntityWorld().isClient()) return;
        if (!pig.isAlive() || pig.isBaby()) return;

        ServerWorld world = (ServerWorld) pig.getEntityWorld();

        if (truffleTime < 0) {
            truffleTime = 10000 + pig.getRandom().nextInt(6000);
        }

        truffleTime--;
        if (truffleTime <= 0) {
            // 金色动物饼干：触发次数翻倍
            int loops = GoldenAnimalCrackerManager.get(world).isBoosted(pig.getUuid()) ? 2 : 1;
            for (int i = 0; i < loops; i++) {
                AnimalQualityHelper.Quality quality = AnimalQualityHelper.getQualityForAnimal(pig, world);
                Identifier truffleId = AnimalQualityHelper.getQualityItemId("truffle", quality);
                ItemStack truffle = new ItemStack(
                    Registries.ITEM.get(truffleId),
                    1
                );
                pig.dropStack(world, truffle);
            }
            pig.playSound(net.minecraft.sound.SoundEvents.ENTITY_PIG_AMBIENT, 1.0f, 1.0f);
            truffleTime = 10000 + pig.getRandom().nextInt(6000);
        }
    }
}
