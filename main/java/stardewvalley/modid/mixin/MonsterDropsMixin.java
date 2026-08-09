package stardewvalley.modid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.equipment.EquipmentStateManager;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.item.ModItems;

import java.util.Set;

@Mixin(LivingEntity.class)
public class MonsterDropsMixin {

    private static Set<EntityType<?>> getTargetMobs() {
        return Set.of(
            EntityType.BLAZE,
            EntityType.BOGGED,
            EntityType.BREEZE,
            EntityType.CREEPER,
            EntityType.ELDER_GUARDIAN,
            EntityType.ENDERMITE,
            EntityType.EVOKER,
            EntityType.GHAST,
            EntityType.GUARDIAN,
            EntityType.HOGLIN,
            EntityType.HUSK,
            EntityType.MAGMA_CUBE,
            EntityType.PARCHED,
            EntityType.PHANTOM,
            EntityType.PIGLIN_BRUTE,
            EntityType.PILLAGER,
            EntityType.RAVAGER,
            EntityType.SHULKER,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.VEX,
            EntityType.VINDICATOR,
            EntityType.WARDEN,
            EntityType.WITCH,
            EntityType.WITHER_SKELETON,
            EntityType.ZOGLIN,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.CAVE_SPIDER,
            EntityType.DROWNED,
            EntityType.ENDERMAN,
            EntityType.PIGLIN,
            EntityType.SPIDER,
            EntityType.ZOMBIFIED_PIGLIN
        );
    }

    @Unique
    private static int countBurglarsRings(ServerPlayerEntity player, ServerWorld world) {
        EquipmentInventory inv = EquipmentStateManager.get(world).getPlayerData(player.getUuid());
        int count = 0;
        for (int i = 0; i < 2; i++) {
            ItemStack s = inv.getSlot(i);
            if (s.isEmpty()) continue;
            Identifier id = Registries.ITEM.getId(s.getItem());
            if (StardewValley.MOD_ID.equals(id.getNamespace()) && "burglars_ring".equals(id.getPath())) {
                count++;
            }
        }
        return count;
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    private void onDropLoot(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (!causedByPlayer) return;
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!getTargetMobs().contains(self.getType())) return;

        float luckMultiplier = StardewValley.getFinalLuckMultiplier(world, player);

        dropStardewLoot(self, world, player, luckMultiplier);

        // 骷髅（含流浪者、凋零骷髅、沼泽骷髅）33%概率*最终幸运掉落骨头碎片1~3
        if (self.getType() == EntityType.SKELETON || self.getType() == EntityType.STRAY
            || self.getType() == EntityType.WITHER_SKELETON || self.getType() == EntityType.BOGGED) {
            if (self.getRandom().nextFloat() < 0.33f * luckMultiplier) {
                int count = 1 + self.getRandom().nextInt(2);
                ItemStack boneFragment = new ItemStack(ModItems.ITEMS.get("bone_fragment"), count);
                if (!boneFragment.isEmpty()) {
                    self.dropStack(world, boneFragment);
                }
            }
        }

        // 所有僵尸及其变体（僵尸、尸壳、溺尸、僵尸村民、僵尸猪灵）33%概率*最终幸运掉落虫肉1~3
        if (self.getType() == EntityType.ZOMBIE || self.getType() == EntityType.HUSK
            || self.getType() == EntityType.DROWNED || self.getType() == EntityType.ZOMBIE_VILLAGER
            || self.getType() == EntityType.ZOMBIFIED_PIGLIN) {
            if (self.getRandom().nextFloat() < 0.33f * luckMultiplier) {
                int count = 1 + self.getRandom().nextInt(2);
                ItemStack bugMeat = new ItemStack(ModItems.ITEMS.get("bug_meat"), count);
                if (!bugMeat.isEmpty()) {
                    self.dropStack(world, bugMeat);
                }
            }
        }

        // 窃贼戒指：每枚戒指固定额外进行一次星露谷战利品判定
        int burglarsCount = countBurglarsRings(player, world);
        for (int i = 0; i < burglarsCount; i++) {
            dropStardewLoot(self, world, player, luckMultiplier);
        }
    }

    @Unique
    private static void dropStardewLoot(LivingEntity self, ServerWorld world, ServerPlayerEntity player, float luckMultiplier) {
        // 50%概率掉落太阳精华/虚空精华/蝙蝠翅膀中的一个
        if (self.getRandom().nextFloat() < 0.33f * luckMultiplier) {
            float roll = self.getRandom().nextFloat();
            String dropName;
            if (roll < 0.33f) {
                dropName = "solar_essence";
            } else if (roll < 0.66f) {
                dropName = "void_essence";
            } else {
                dropName = "bat_wing";
            }
            int count = 1 + self.getRandom().nextInt(3);
            ItemStack stack = new ItemStack(ModItems.ITEMS.get(dropName), count);
            if (!stack.isEmpty()) {
                self.dropStack(world, stack);
            }
        }

        // 0.15%概率掉落金色动物饼干（受最终运气影响）
        if (self.getRandom().nextFloat() < 0.0015f * luckMultiplier) {
            ItemStack stack = new ItemStack(ModItems.ITEMS.get("golden_animal_cracker"), 1);
            if (!stack.isEmpty()) {
                self.dropStack(world, stack);
            }
        }

        // 0.05%概率掉落星露谷钻石
        if (self.getRandom().nextFloat() < 0.0005f * luckMultiplier) {
            ItemStack stack = new ItemStack(ModItems.ITEMS.get("diamond"), 1);
            if (!stack.isEmpty()) {
                self.dropStack(world, stack);
            }
        }

        // 0.05%概率掉落五彩碎片
        if (self.getRandom().nextFloat() < 0.0005f * luckMultiplier) {
            ItemStack stack = new ItemStack(ModItems.ITEMS.get("prismatic_shard"), 1);
            if (!stack.isEmpty()) {
                self.dropStack(world, stack);
            }
        }

        // 0.05%概率掉落幸运戒指
        if (self.getRandom().nextFloat() < 0.0005f * luckMultiplier) {
            ItemStack stack = new ItemStack(ModItems.ITEMS.get("lucky_ring"), 1);
            if (!stack.isEmpty()) {
                self.dropStack(world, stack);
            }
        }

        // 怪物图鉴：击杀怪物掉落书本（渐进概率）
        BookDataManager.rollProgressiveDrop(world, player, "monster_compendium",
            0.0001f, 0.00015f, 0.0005f, luckMultiplier);

        // 天数>50后，击杀怪物概率掉落迷之盒和金色迷之盒（独立判定，等概率）
        long currentDay = world.getTimeOfDay() / 24000L;
        if (currentDay > 50) {
            // 幸运等级（Minecraft幸运效果）
            int luckLevel = 0;
            net.minecraft.entity.effect.StatusEffectInstance luckEffect = player.getStatusEffect(net.minecraft.entity.effect.StatusEffects.LUCK);
            if (luckEffect != null) {
                luckLevel = luckEffect.getAmplifier() + 1;
            }
            float mysteryChance = 0.01f + (luckMultiplier - 1.0f) / 10.0f + luckLevel * 0.005f;
            // 迷之书：使用后概率×1.33
            if (BookDataManager.get(world).hasUsedBook(player.getUuid(), "book_of_mysteries")) {
                mysteryChance *= 1.33f;
            }
            // 迷之盒（独立判定）
            if (self.getRandom().nextFloat() < mysteryChance) {
                ItemStack stack = new ItemStack(ModItems.ITEMS.get("mystery_box"), 1);
                if (!stack.isEmpty()) {
                    self.dropStack(world, stack);
                }
            }
            // 金色迷之盒（独立判定，需采集精通）
            if (stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(world, player.getUuid(), stardewvalley.modid.skill.SkillRegistry.Category.FORAGING)
                && self.getRandom().nextFloat() < mysteryChance) {
                ItemStack stack = new ItemStack(ModItems.ITEMS.get("golden_mystery_box"), 1);
                if (!stack.isEmpty()) {
                    self.dropStack(world, stack);
                }
            }
        }
    }
}
