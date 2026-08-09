package stardewvalley.modid.item;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import stardewvalley.modid.AnimalFriendshipManager;
import stardewvalley.modid.AnimalQualityHelper;
import stardewvalley.modid.GoldenAnimalCrackerManager;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.season.LuckManager;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.skill.SkillEffectHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimalInteractionHandler {

    private static final Map<UUID, Long> MILKED_ENTITIES = new HashMap<>();

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getMainHandStack();
            if (stack.isEmpty()) return ActionResult.PASS;

            // 牛奶桶挤奶
            if (stack.getItem() == ModItems.ITEMS.get("milk_pail")) {
                if ((entity instanceof CowEntity || entity instanceof GoatEntity) && entity instanceof AnimalEntity animal && !animal.isBaby()) {
                    if (world.isClient()) return ActionResult.SUCCESS;
                    return handleMilkPail(player, (ServerWorld) world, entity);
                }
                return ActionResult.PASS;
            }

            // 星露谷剪刀剪羊毛
            if (stack.getItem() == ModItems.ITEMS.get("shears")) {
                if (entity instanceof SheepEntity sheep && sheep.isShearable() && !sheep.isBaby()) {
                    if (world.isClient()) return ActionResult.SUCCESS;
                    return handleShears((ServerWorld) world, player, entity, stack);
                }
                return ActionResult.PASS;
            }

            // 干草喂食（1天1次，增加好感度）
            if (stack.getItem() == ModItems.ITEMS.get("hay")) {
                if (entity instanceof AnimalEntity animal && !animal.isBaby()) {
                    if (world.isClient()) return ActionResult.SUCCESS;
                    return handleHayFeed((ServerWorld) world, player, entity);
                }
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        });
    }

    private static ActionResult handleMilkPail(PlayerEntity player, ServerWorld world, net.minecraft.entity.Entity entity) {
        long currentDay = world.getTimeOfDay() / 24000L;
        UUID entityUuid = entity.getUuid();

        if (MILKED_ENTITIES.containsKey(entityUuid) && MILKED_ENTITIES.get(entityUuid) == currentDay) {
            player.sendMessage(Text.translatable("message.stardewvalley.already_milked"), true);
            return ActionResult.FAIL;
        }

        // 金色动物饼干：挤奶触发次数翻倍
        float luckMult = player instanceof ServerPlayerEntity sp ? StardewValley.getFinalLuckMultiplier(world, sp) : StardewValley.getLuckMultiplier(world);
        int drops = GoldenAnimalCrackerManager.get(world).isBoosted(entity.getUuid()) ? 2 : 1;
        AnimalQualityHelper.Quality milkQuality = AnimalQualityHelper.getQualityForAnimal((AnimalEntity) entity, world);
        for (int j = 0; j < drops; j++) {
            ItemStack milkStack;
            if (entity instanceof CowEntity) {
                String baseMilk = world.random.nextFloat() < 0.1f * luckMult ? "large_milk" : "milk";
                milkStack = new ItemStack(
                    Registries.ITEM.get(AnimalQualityHelper.getQualityItemId(baseMilk, milkQuality)),
                    1
                );
            } else {
                String baseMilk = world.random.nextFloat() < 0.1f * luckMult ? "large_goat_milk" : "goat_milk";
                milkStack = new ItemStack(
                    Registries.ITEM.get(AnimalQualityHelper.getQualityItemId(baseMilk, milkQuality)),
                    1
                );
            }
            if (!player.getInventory().insertStack(milkStack)) {
                player.dropItem(milkStack, false);
            }
        }
        MILKED_ENTITIES.put(entityUuid, currentDay);
        // 挤奶增加5好感度
        AnimalFriendshipManager.get(world).addFriendship(entityUuid, 5);
        return ActionResult.SUCCESS;
    }

    private static ActionResult handleShears(ServerWorld world, PlayerEntity player, net.minecraft.entity.Entity entity, ItemStack shearsStack) {
        SheepEntity sheep = (SheepEntity) entity;

        // 手动处理剪羊毛，绕过战利品表（战利品表只会掉落原版羊毛）
        sheep.setSheared(true);

        // 牧羊人：剪毛触发次数翻倍
        int drops = 1;
        if (SkillEffectHelper.hasShepherd(world, player.getUuid())) {
            drops *= 2;
        }
        // 金色动物饼干：剪毛触发次数再翻倍（与牧羊人叠加为4次）
        if (GoldenAnimalCrackerManager.get(world).isBoosted(entity.getUuid())) {
            drops *= 2;
        }
        AnimalQualityHelper.Quality woolQuality = AnimalQualityHelper.getQualityForAnimal((AnimalEntity) entity, world);
        for (int j = 0; j < drops; j++) {
            ItemStack woolStack = new ItemStack(
                Registries.ITEM.get(AnimalQualityHelper.getQualityItemId("wool", woolQuality)),
                1
            );
            sheep.dropStack(world, woolStack, sheep.getHeight() / 2.0f + 0.5f);
        }

        sheep.playSound(net.minecraft.sound.SoundEvents.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        sheep.emitGameEvent(net.minecraft.world.event.GameEvent.SHEAR, sheep);

        shearsStack.damage(1, sheep, net.minecraft.entity.EquipmentSlot.MAINHAND);
        return ActionResult.SUCCESS;
    }

    private static ActionResult handleHayFeed(ServerWorld world, PlayerEntity player, net.minecraft.entity.Entity entity) {
        long currentDay = world.getTimeOfDay() / 24000L;
        UUID entityUuid = entity.getUuid();

        AnimalFriendshipManager manager = AnimalFriendshipManager.get(world);
        if (manager.isFedToday(entityUuid, currentDay)) {
            player.sendMessage(Text.translatable("message.stardewvalley.already_fed"), true);
            return ActionResult.FAIL;
        }

        // 计算好感度增加量
        int friendshipAmount = 40;
        if (entity instanceof ChickenEntity && SkillEffectHelper.hasCoopmaster(world, player.getUuid())) {
            friendshipAmount = 55;
        } else if ((entity instanceof SheepEntity || entity instanceof GoatEntity) && SkillEffectHelper.hasShepherd(world, player.getUuid())) {
            friendshipAmount = 55;
        }

        manager.feed(entityUuid, friendshipAmount, currentDay);
        player.getMainHandStack().decrement(1);
        player.sendMessage(Text.translatable("message.stardewvalley.animal_fed", manager.getFriendship(entityUuid)), true);
        entity.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        // 动物目录（书本掉落）：喂食干草时0.4%起+0.05%每次，掉落后稳定0.8%
        float luckMult = StardewValley.getFinalLuckMultiplier(world, (ServerPlayerEntity) player);
        BookDataManager.rollProgressiveDrop(world, (ServerPlayerEntity) player, "animal_catalogue",
            0.004f, 0.0005f, 0.008f, luckMult);

        return ActionResult.SUCCESS;
    }

    public static void resetDaily(long currentDay) {
        MILKED_ENTITIES.values().removeIf(day -> day < currentDay);
    }
}
