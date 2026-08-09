package stardewvalley.modid.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.RabbitEntity;
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
public class RabbitFootMixin {

    @Unique
    private int rabbitFootTime = -1;

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void onTickMovement(CallbackInfo ci) {
        if (!((Object)this instanceof RabbitEntity rabbit)) return;
        if (rabbit.getEntityWorld().isClient()) return;
        if (!rabbit.isAlive() || rabbit.isBaby()) return;

        ServerWorld world = (ServerWorld) rabbit.getEntityWorld();

        if (rabbitFootTime < 0) {
            rabbitFootTime = 40000 + rabbit.getRandom().nextInt(20000);
        }

        rabbitFootTime--;
        if (rabbitFootTime <= 0) {
            // 金色动物饼干：触发次数翻倍
            int loops = GoldenAnimalCrackerManager.get(world).isBoosted(rabbit.getUuid()) ? 2 : 1;
            for (int i = 0; i < loops; i++) {
                AnimalQualityHelper.Quality quality = AnimalQualityHelper.getQualityForAnimal(rabbit, world);
                Identifier footId = AnimalQualityHelper.getQualityItemId("rabbits_foot", quality);
                ItemStack rabbitsFoot = new ItemStack(
                    Registries.ITEM.get(footId),
                    1
                );
                rabbit.dropStack(world, rabbitsFoot);
            }
            rabbit.playSound(net.minecraft.sound.SoundEvents.ENTITY_RABBIT_AMBIENT, 1.0f, 1.0f);
            rabbitFootTime = 40000 + rabbit.getRandom().nextInt(20000);
        }
    }
}
