package stardewvalley.modid.item;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import stardewvalley.modid.GoldenAnimalCrackerManager;

public class GoldenAnimalCrackerItem extends Item {

    public GoldenAnimalCrackerItem(Settings settings) {
        super(settings);
    }

    public static void registerEvents() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(entity instanceof ChickenEntity ||
                  entity instanceof SheepEntity ||
                  entity instanceof CowEntity ||
                  entity instanceof GoatEntity ||
                  entity instanceof PigEntity ||
                  entity instanceof RabbitEntity)) {
                return ActionResult.PASS;
            }

            var stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof GoldenAnimalCrackerItem) {
                ServerWorld serverWorld = (ServerWorld) world;
                GoldenAnimalCrackerManager manager = GoldenAnimalCrackerManager.get(serverWorld);
                if (manager.isBoosted(entity.getUuid())) {
                    player.sendMessage(Text.translatable("message.stardewvalley.already_boosted"), true);
                    return ActionResult.FAIL;
                }
                manager.boost(entity.getUuid());
                stack.decrement(1);
                player.sendMessage(Text.translatable("message.stardewvalley.animal_boosted"), true);
                entity.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}
