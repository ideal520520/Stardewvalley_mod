package stardewvalley.modid.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.ArtisanEquipmentBlock;
import stardewvalley.modid.block.BombBlock;
import stardewvalley.modid.block.LightningRodBlock;
import stardewvalley.modid.block.SprinklerBlock;
import stardewvalley.modid.block.TapperBlock;
import stardewvalley.modid.season.Season;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@Mixin(AbstractBlock.class)
public class BombExplosionDropsMixin {

    private static final Map<String, Integer> FORAGE_VALUES = Map.ofEntries(
        Map.entry("caiji_wildhorseradish", 50), Map.entry("caiji_daffodil", 30),
        Map.entry("caiji_leek", 60), Map.entry("caiji_dandelion", 40),
        Map.entry("caiji_springonion", 8), Map.entry("caiji_salmonberry", 5),
        Map.entry("caiji_spiceberry", 80), Map.entry("caiji_sweetpea", 50),
        Map.entry("caiji_fiddleheadfern", 90), Map.entry("caiji_wildplum", 80),
        Map.entry("caiji_hazelnut", 90), Map.entry("caiji_blackberry", 20),
        Map.entry("caiji_morel", 150), Map.entry("caiji_commonmushroom", 40),
        Map.entry("caiji_redmushroom", 75), Map.entry("caiji_purplemushroom", 250),
        Map.entry("caiji_chanterelle", 160), Map.entry("caiji_winterroot", 70),
        Map.entry("caiji_crystalfruit", 150), Map.entry("caiji_snowyam", 100),
        Map.entry("caiji_crocus", 60), Map.entry("caiji_holly", 80),
        Map.entry("caiji_cavecarrot", 25), Map.entry("caiji_nautilusshell", 120),
        Map.entry("caiji_coral", 80), Map.entry("caiji_seaurchin", 160),
        Map.entry("caiji_rainbow_shell", 300), Map.entry("caiji_clam", 50),
        Map.entry("caiji_cockle", 50), Map.entry("caiji_mussel", 30),
        Map.entry("caiji_oyster", 40), Map.entry("caiji_seaweed", 20),
        Map.entry("caiji_coconut", 100), Map.entry("cactusfruit", 75),
        Map.entry("caiji_ginger", 60), Map.entry("caiji_magmacap", 400)
    );
    private static final float FORAGE_TOTAL_PRICE;

    static {
        float sum = 0;
        for (int v : FORAGE_VALUES.values()) sum += v;
        FORAGE_TOTAL_PRICE = sum;
    }

    @Inject(method = "onExploded", at = @At("HEAD"))
    private void onBombExplosionDestroy(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger, CallbackInfo ci) {
        Block block = state.getBlock();

        // Protected blocks: spawn their item drop (block destruction handled by default onExploded)
        if (block instanceof TapperBlock || block instanceof ArtisanEquipmentBlock || block instanceof LightningRodBlock || block instanceof SprinklerBlock) {
            Item dropItem = block.asItem();
            if (dropItem != null && dropItem != net.minecraft.item.Items.AIR) {
                world.spawnEntity(new net.minecraft.entity.ItemEntity(world,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(dropItem)));
            }
            return;
        }

        // Below drops only apply to Stardew bomb explosions
        if (!BombBlock.hasActiveExplosion()) return;

        float luckMult;
        UUID placerUuid = BombBlock.getPlacer(pos);
        if (placerUuid != null && world.getServer() != null && world.getServer().getPlayerManager().getPlayer(placerUuid) instanceof ServerPlayerEntity sp) {
            luckMult = StardewValley.getFinalLuckMultiplier(world, sp);
        } else {
            luckMult = StardewValley.getLuckMultiplier(world);
        }

        // ===== Foraging drops =====
        Season season = Season.fromTimeOfDay(world.getTimeOfDay());
        String biomeId = world.getBiome(pos).getKey().map(k -> k.getValue().toString()).orElse("");

        // Grass / dirt / grass blocks
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.SAND ||
            block == Blocks.SHORT_GRASS || block == Blocks.TALL_GRASS || block == Blocks.FERN ||
            block == Blocks.LEAF_LITTER) {
            float seedChance = 0.0192f * luckMult;
            if (world.random.nextFloat() < seedChance) {
                Item mixedSeeds = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "mixedseeds"));
                if (mixedSeeds != null) dropItem(world, pos, mixedSeeds, 1);
            }
            if (season == Season.SUMMER && world.random.nextFloat() < seedChance) {
                Item flowerSeeds = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "mixedflowerseeds"));
                if (flowerSeeds != null) dropItem(world, pos, flowerSeeds, 1);
            }
            rollForageDrop(world, pos, getSeasonalGrassItems(season));
            return;
        }

        // Stone → mushroom / cave carrot drops (stone continues to gem drops below)
        if (block == Blocks.STONE || block == Blocks.GRAVEL) {
            rollForageDrop(world, pos, getStoneItems());
            if (block == Blocks.GRAVEL) return;
        }

        if (block == Blocks.MYCELIUM) {
            rollForageDrop(world, pos, getMyceliumItems(season));
            return;
        }

        if (block == Blocks.SAND || block == Blocks.RED_SAND) {
            if (biomeId.contains("beach") || biomeId.contains("ocean") || biomeId.contains("river")) {
                rollForageDrop(world, pos, getWaterItems());
            } else if (biomeId.contains("desert")) {
                rollForageDrop(world, pos, getDesertItems());
            }
            return;
        }

        if (state.isIn(net.minecraft.registry.tag.BlockTags.LOGS)) {
            Item sap = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "caiji_sap"));
            if (sap != null) dropItem(world, pos, sap, 1);
            if (world.random.nextFloat() < 0.0128f * luckMult) {
                Item hardwood = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "hardwood"));
                if (hardwood != null) dropItem(world, pos, hardwood, 1);
            }
            if (season == Season.SUMMER) {
                rollForageDrop(world, pos, List.of("caiji_fiddleheadfern"));
            }
            return;
        }

        if (state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES)) {
            if (world.random.nextFloat() < 0.0512f * luckMult) {
                Item sap = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "caiji_sap"));
                if (sap != null) dropItem(world, pos, sap, 1);
            }
            return;
        }

        if (block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK) {
            rollForageDrop(world, pos, getMushroomItems());
            return;
        }

        if (block == Blocks.CACTUS) {
            if (world.random.nextFloat() < 0.128f * luckMult) {
                Item cactus = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "cactusfruit"));
                if (cactus != null) dropItem(world, pos, cactus, 1);
            }
            return;
        }

        // ===== Ore drops =====
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            dropItem(world, pos, Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "coal")), 1 + world.random.nextInt(3));
        } else if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            dropItem(world, pos, Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "copper_ore")), 2 + world.random.nextInt(3));
        } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            dropItem(world, pos, Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "iron_ore")), 1 + world.random.nextInt(3));
        } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            dropItem(world, pos, Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "gold_ore")), 1 + world.random.nextInt(2));
        } else if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            dropItem(world, pos, net.minecraft.item.Items.REDSTONE, 4 + world.random.nextInt(2));
        } else if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            dropItem(world, pos, net.minecraft.item.Items.LAPIS_LAZULI, 4 + world.random.nextInt(5));
        } else if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            dropItem(world, pos, net.minecraft.item.Items.DIAMOND, 1);
        }

        // Probability-based gem/mineral drops
        if (isMiningBlock(block)) {
            StardewValley.tryExplosionGemDrop(world, pos);
        }
    }

    // ===== Foraging drop helpers =====

    private static void rollForageDrop(ServerWorld world, BlockPos pos, List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) return;
        float luckMult;
        UUID placerUuid = BombBlock.getPlacer(pos);
        if (placerUuid != null && world.getServer() != null && world.getServer().getPlayerManager().getPlayer(placerUuid) instanceof ServerPlayerEntity sp) {
            luckMult = StardewValley.getFinalLuckMultiplier(world, sp);
        } else {
            luckMult = StardewValley.getLuckMultiplier(world);
        }
        for (String id : candidates) {
            int price = FORAGE_VALUES.getOrDefault(id, 50);
            float x = price / FORAGE_TOTAL_PRICE;
            float chance = (float) (Math.log(1.0 / x + 1) - 1) / 100.0f * 0.64f * luckMult;
            if (chance > 0 && world.random.nextFloat() < chance) {
                Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, id));
                if (item != null) dropItem(world, pos, item, 1);
                return;
            }
        }
    }

    private static List<String> getSeasonalGrassItems(Season season) {
        return switch (season) {
            case SPRING -> List.of("caiji_wildhorseradish", "caiji_daffodil", "caiji_leek", "caiji_dandelion", "caiji_springonion", "caiji_salmonberry");
            case SUMMER -> List.of("caiji_spiceberry", "caiji_sweetpea", "caiji_fiddleheadfern");
            case FALL -> List.of("caiji_wildplum", "caiji_hazelnut", "caiji_blackberry");
            case WINTER -> List.of("caiji_winterroot", "caiji_crystalfruit", "caiji_snowyam", "caiji_crocus", "caiji_holly");
        };
    }

    private static List<String> getStoneItems() {
        return List.of("caiji_redmushroom", "caiji_purplemushroom", "caiji_cavecarrot");
    }

    private static List<String> getMyceliumItems(Season season) {
        return switch (season) {
            case SPRING -> List.of("caiji_morel", "caiji_commonmushroom");
            case SUMMER -> List.of("caiji_commonmushroom", "caiji_redmushroom");
            case FALL -> List.of("caiji_commonmushroom", "caiji_chanterelle", "caiji_redmushroom", "caiji_purplemushroom");
            case WINTER -> List.of();
        };
    }

    private static List<String> getWaterItems() {
        return List.of("caiji_nautilusshell", "caiji_coral", "caiji_seaurchin", "caiji_rainbow_shell",
            "caiji_clam", "caiji_cockle", "caiji_mussel", "caiji_oyster", "caiji_seaweed");
    }

    private static List<String> getDesertItems() {
        return List.of("caiji_coconut", "cactusfruit");
    }

    private static List<String> getMushroomItems() {
        return List.of("caiji_redmushroom", "caiji_purplemushroom", "caiji_chanterelle", "caiji_commonmushroom", "caiji_morel");
    }

    private static boolean isMiningBlock(Block block) {
        return block == Blocks.STONE || block == Blocks.COBBLESTONE ||
            block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE ||
            block == Blocks.BASALT || block == Blocks.BLACKSTONE ||
            block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE || block == Blocks.TUFF ||
            block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
            block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE ||
            block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
            block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
            block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE ||
            block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE ||
            block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE;
    }

    private static void dropItem(ServerWorld world, BlockPos pos, Item item, int count) {
        if (item == null) return;
        world.spawnEntity(new net.minecraft.entity.ItemEntity(world,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(item, count)));
    }
}
