package stardewvalley.modid.block;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.crop.CropConfig;
import stardewvalley.modid.item.TreeSeedItem;
import stardewvalley.modid.item.WildSeedItem;
import stardewvalley.modid.season.Season;

public class ModBlocks {

    // ========= ModFarmland (custom farmland with fertilizer support) =========
    public static final Block MOD_FARMLAND = registerBlock("mod_farmland",
        new ModFarmlandBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "mod_farmland")))
            .mapColor(net.minecraft.block.MapColor.DIRT_BROWN)
            .strength(1.5F)
            .ticksRandomly()
            .blockVision((state, world, pos) -> false)
            .suffocates((state, world, pos) -> false)
            .sounds(BlockSoundGroup.GRAVEL)));

    public static BlockState ensureModFarmland(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof FarmlandBlock && !(state.getBlock() instanceof ModFarmlandBlock)) {
            int moisture = state.get(FarmlandBlock.MOISTURE);
            BlockState newState = MOD_FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, moisture);
            world.setBlockState(pos, newState, 3);
            return newState;
        }
        return state;
    }

    // ========= Amaranth (one-time, farmland) =========
    private static final VoxelShape[] AMARANTH_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16)
    };

    private static final CropConfig AMARANTH_CONFIG = CropConfig.builder("amaranth", 7, AMARANTH_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block AMARANTH_CROP = registerCropBlock("amaranth_crop",
        new BaseCropBlock(cropSettings("amaranth_crop"), AMARANTH_CONFIG));

    public static final Item AMARANTH_SEEDS = registerSeedItem("amaranth_seeds",
        new BaseSeedItem(seedSettings("amaranth_seeds"), AMARANTH_CROP, AMARANTH_CONFIG));

    // ========= Ancient Fruit (continuous, farmland) =========
    private static final VoxelShape[] ANCIENT_FRUIT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 22, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16)
    };

    private static final CropConfig ANCIENT_FRUIT_CONFIG = CropConfig.builder("ancientfruit", 35, ANCIENT_FRUIT_SHAPES)
        .regrowCycle(7)
        
        .seasons(Season.SPRING, Season.SUMMER, Season.FALL).build();

    public static final Block ANCIENT_FRUIT_CROP = registerCropBlock("ancientfruit_crop",
        new BaseCropBlock(cropSettings("ancientfruit_crop"), ANCIENT_FRUIT_CONFIG));

    public static final Item ANCIENT_FRUIT_SEEDS = registerSeedItem("ancientfruit_seeds",
        new BaseSeedItem(seedSettings("ancientfruit_seeds"), ANCIENT_FRUIT_CROP, ANCIENT_FRUIT_CONFIG));

    // ========= Artichoke (one-time, farmland) =========
    private static final VoxelShape[] ARTICHOKE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16)
    };

    private static final CropConfig ARTICHOKE_CONFIG = CropConfig.builder("artichoke", 8, ARTICHOKE_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block ARTICHOKE_CROP = registerCropBlock("artichoke_crop",
        new BaseCropBlock(cropSettings("artichoke_crop"), ARTICHOKE_CONFIG));

    public static final Item ARTICHOKE_SEEDS = registerSeedItem("artichoke_seeds",
        new BaseSeedItem(seedSettings("artichoke_seeds"), ARTICHOKE_CROP, ARTICHOKE_CONFIG));

    // ========= Beet (one-time, farmland) =========
    private static final VoxelShape[] BEET_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16)
    };

    private static final CropConfig BEET_CONFIG = CropConfig.builder("beet", 6, BEET_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block BEET_CROP = registerCropBlock("beet_crop",
        new BaseCropBlock(cropSettings("beet_crop"), BEET_CONFIG));

    public static final Item BEET_SEEDS = registerSeedItem("beet_seeds",
        new BaseSeedItem(seedSettings("beet_seeds"), BEET_CROP, BEET_CONFIG));

    // ========= Blueberry (continuous, farmland, summer, harvest 3 + 2% extra) =========
    private static final VoxelShape[] BLUEBERRY_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16)
    };

    private static final CropConfig BLUEBERRY_CONFIG = CropConfig.builder("blueberry", 17, BLUEBERRY_SHAPES)
        .regrowCycle(4)
        .harvestDropCount(3)
        .extraDropChance(0.02f)
        
        .seasons(Season.SUMMER).build();

    public static final Block BLUEBERRY_CROP = registerCropBlock("blueberry_crop",
        new BaseCropBlock(cropSettings("blueberry_crop"), BLUEBERRY_CONFIG));

    public static final Item BLUEBERRY_SEEDS = registerSeedItem("blueberry_seeds",
        new BaseSeedItem(seedSettings("blueberry_seeds"), BLUEBERRY_CROP, BLUEBERRY_CONFIG));

    // ========= Blue Jazz (one-time, farmland, spring) =========
    private static final VoxelShape[] BLUEJAZZ_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 4, 16),
        Block.createCuboidShape(0, 0, 0, 16, 4, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16)
    };

    private static final CropConfig BLUEJAZZ_CONFIG = CropConfig.builder("bluejazz", 7, BLUEJAZZ_SHAPES)
        
        .seasons(Season.SPRING).build();

    public static final Block BLUEJAZZ_CROP = registerCropBlock("bluejazz_crop",
        new BaseCropBlock(cropSettings("bluejazz_crop"), BLUEJAZZ_CONFIG));

    public static final Item BLUEJAZZ_SEEDS = registerSeedItem("bluejazz_seeds",
        new BaseSeedItem(seedSettings("bluejazz_seeds"), BLUEJAZZ_CROP, BLUEJAZZ_CONFIG));

    // ========= Bok Choy (one-time, farmland, fall) =========
    private static final VoxelShape[] BOKCHOY_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16)
    };

    private static final CropConfig BOKCHOY_CONFIG = CropConfig.builder("bokchoy", 4, BOKCHOY_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block BOKCHOY_CROP = registerCropBlock("bokchoy_crop",
        new BaseCropBlock(cropSettings("bokchoy_crop"), BOKCHOY_CONFIG));

    public static final Item BOKCHOY_SEEDS = registerSeedItem("bokchoy_seeds",
        new BaseSeedItem(seedSettings("bokchoy_seeds"), BOKCHOY_CROP, BOKCHOY_CONFIG));

    // ========= Broccoli (continuous, farmland, fall) =========
    private static final VoxelShape[] BROCCOLI_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16)
    };

    private static final CropConfig BROCCOLI_CONFIG = CropConfig.builder("broccoli", 12, BROCCOLI_SHAPES)
        .regrowCycle(4)
        
        .seasons(Season.FALL).build();

    public static final Block BROCCOLI_CROP = registerCropBlock("broccoli_crop",
        new BaseCropBlock(cropSettings("broccoli_crop"), BROCCOLI_CONFIG));

    public static final Item BROCCOLI_SEEDS = registerSeedItem("broccoli_seeds",
        new BaseSeedItem(seedSettings("broccoli_seeds"), BROCCOLI_CROP, BROCCOLI_CONFIG));

    // ========= Cactus Fruit (continuous, sand, summer, no water) =========
    private static final VoxelShape[] CACTUSFRUIT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16)
    };

    private static final CropConfig CACTUSFRUIT_CONFIG = CropConfig.builder("cactusfruit", 15, CACTUSFRUIT_SHAPES)
        .regrowCycle(3)
        .groundTest(state -> state.getBlock() instanceof net.minecraft.block.SandBlock)
        .requiresWater(false)
        
        .seasons(Season.SUMMER).build();

    public static final Block CACTUSFRUIT_CROP = registerCropBlock("cactusfruit_crop",
        new BaseCropBlock(cropSettings("cactusfruit_crop"), CACTUSFRUIT_CONFIG));

    public static final Item CACTUSFRUIT_SEEDS = registerSeedItem("cactusfruit_seeds",
        new BaseSeedItem(seedSettings("cactusfruit_seeds"), CACTUSFRUIT_CROP, CACTUSFRUIT_CONFIG));

    // ========= Parsnip (one-time, farmland, spring) =========
    private static final VoxelShape[] PARSNIP_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16)
    };

    private static final CropConfig PARSNIP_CONFIG = CropConfig.builder("parsnip", 4, PARSNIP_SHAPES)
        
        .seasons(Season.SPRING).build();

    public static final Block PARSNIP_CROP = registerCropBlock("parsnip_crop",
        new BaseCropBlock(cropSettings("parsnip_crop"), PARSNIP_CONFIG));

    public static final Item PARSNIP_SEEDS = registerSeedItem("parsnip_seeds",
        new BaseSeedItem(seedSettings("parsnip_seeds"), PARSNIP_CROP, PARSNIP_CONFIG));

    // ========= Pineapple (continuous, farmland, summer) =========
    private static final VoxelShape[] PINEAPPLE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 22, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16)
    };

    private static final CropConfig PINEAPPLE_CONFIG = CropConfig.builder("pineapple", 21, PINEAPPLE_SHAPES)
        .regrowCycle(7)
        
        .seasons(Season.SUMMER).build();

    public static final Block PINEAPPLE_CROP = registerCropBlock("pineapple_crop",
        new BaseCropBlock(cropSettings("pineapple_crop"), PINEAPPLE_CONFIG));

    public static final Item PINEAPPLE_SEEDS = registerSeedItem("pineapple_seeds",
        new BaseSeedItem(seedSettings("pineapple_seeds"), PINEAPPLE_CROP, PINEAPPLE_CONFIG));

    // ========= Poppy (one-time, farmland, summer) =========
    private static final VoxelShape[] POPPY_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig POPPY_CONFIG = CropConfig.builder("poppy", 6, POPPY_SHAPES)
        
        .seasons(Season.SUMMER).build();

    public static final Block POPPY_CROP = registerCropBlock("poppy_crop",
        new BaseCropBlock(cropSettings("poppy_crop"), POPPY_CONFIG));

    public static final Item POPPY_SEEDS = registerSeedItem("poppy_seeds",
        new BaseSeedItem(seedSettings("poppy_seeds"), POPPY_CROP, POPPY_CONFIG));

    // ========= Potato (one-time, farmland, spring, extra 25%) =========
    private static final VoxelShape[] POTATO_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16)
    };

    private static final CropConfig POTATO_CONFIG = CropConfig.builder("potato", 6, POTATO_SHAPES)
        .extraDropChance(0.25f)
        
        .seasons(Season.SPRING).build();

    public static final Block POTATO_CROP = registerCropBlock("potato_crop",
        new BaseCropBlock(cropSettings("potato_crop"), POTATO_CONFIG));

    public static final Item POTATO_SEEDS = registerSeedItem("potato_seeds",
        new BaseSeedItem(seedSettings("potato_seeds"), POTATO_CROP, POTATO_CONFIG));

    // ========= Powdermelon (one-time, farmland, winter) =========
    private static final VoxelShape[] POWDERMELON_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16)
    };

    private static final CropConfig POWDERMELON_CONFIG = CropConfig.builder("powdermelon", 7, POWDERMELON_SHAPES)
        
        .seasons(Season.WINTER).build();

    public static final Block POWDERMELON_CROP = registerCropBlock("powdermelon_crop",
        new BaseCropBlock(cropSettings("powdermelon_crop"), POWDERMELON_CONFIG));

    public static final Item POWDERMELON_SEEDS = registerSeedItem("powdermelon_seeds",
        new BaseSeedItem(seedSettings("powdermelon_seeds"), POWDERMELON_CROP, POWDERMELON_CONFIG));

    // ========= Pumpkin (one-time, farmland, fall) =========
    private static final VoxelShape[] PUMPKIN_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16)
    };

    private static final CropConfig PUMPKIN_CONFIG = CropConfig.builder("pumpkin", 13, PUMPKIN_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block PUMPKIN_CROP = registerCropBlock("pumpkin_crop",
        new BaseCropBlock(cropSettings("pumpkin_crop"), PUMPKIN_CONFIG));

    public static final Item PUMPKIN_SEEDS = registerSeedItem("pumpkin_seeds",
        new BaseSeedItem(seedSettings("pumpkin_seeds"), PUMPKIN_CROP, PUMPKIN_CONFIG));

    // ========= Radish (one-time, farmland, summer) =========
    private static final VoxelShape[] RADISH_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16)
    };

    private static final CropConfig RADISH_CONFIG = CropConfig.builder("radish", 6, RADISH_SHAPES)
        
        .seasons(Season.SUMMER).build();

    public static final Block RADISH_CROP = registerCropBlock("radish_crop",
        new BaseCropBlock(cropSettings("radish_crop"), RADISH_CONFIG));

    public static final Item RADISH_SEEDS = registerSeedItem("radish_seeds",
        new BaseSeedItem(seedSettings("radish_seeds"), RADISH_CROP, RADISH_CONFIG));

    // ========= Red Cabbage (one-time, farmland, summer) =========
    private static final VoxelShape[] REDCABBAGE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16)
    };

    private static final CropConfig REDCABBAGE_CONFIG = CropConfig.builder("redcabbage", 9, REDCABBAGE_SHAPES)
        
        .seasons(Season.SUMMER).build();

    public static final Block REDCABBAGE_CROP = registerCropBlock("redcabbage_crop",
        new BaseCropBlock(cropSettings("redcabbage_crop"), REDCABBAGE_CONFIG));

    public static final Item REDCABBAGE_SEEDS = registerSeedItem("redcabbage_seeds",
        new BaseSeedItem(seedSettings("redcabbage_seeds"), REDCABBAGE_CROP, REDCABBAGE_CONFIG));

    // ========= Rhubarb (one-time, farmland, spring) =========
    private static final VoxelShape[] RHUBARB_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig RHUBARB_CONFIG = CropConfig.builder("rhubarb", 13, RHUBARB_SHAPES)
        
        .seasons(Season.SPRING).build();

    public static final Block RHUBARB_CROP = registerCropBlock("rhubarb_crop",
        new BaseCropBlock(cropSettings("rhubarb_crop"), RHUBARB_CONFIG));

    public static final Item RHUBARB_SEEDS = registerSeedItem("rhubarb_seeds",
        new BaseSeedItem(seedSettings("rhubarb_seeds"), RHUBARB_CROP, RHUBARB_CONFIG));

    // ========= Starfruit (one-time, farmland, summer) =========
    private static final VoxelShape[] STARFRUIT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16)
    };

    private static final CropConfig STARFRUIT_CONFIG = CropConfig.builder("starfruit", 13, STARFRUIT_SHAPES)
        
        .seasons(Season.SUMMER).build();

    public static final Block STARFRUIT_CROP = registerCropBlock("starfruit_crop",
        new BaseCropBlock(cropSettings("starfruit_crop"), STARFRUIT_CONFIG));

    public static final Item STARFRUIT_SEEDS = registerSeedItem("starfruit_seeds",
        new BaseSeedItem(seedSettings("starfruit_seeds"), STARFRUIT_CROP, STARFRUIT_CONFIG));

    // ========= Strawberry (continuous, farmland, spring, extra 2%) =========
    private static final VoxelShape[] STRAWBERRY_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig STRAWBERRY_CONFIG = CropConfig.builder("strawberry", 12, STRAWBERRY_SHAPES)
        .regrowCycle(4)
        .extraDropChance(0.02f)
        
        .seasons(Season.SPRING).build();

    public static final Block STRAWBERRY_CROP = registerCropBlock("strawberry_crop",
        new BaseCropBlock(cropSettings("strawberry_crop"), STRAWBERRY_CONFIG));

    public static final Item STRAWBERRY_SEEDS = registerSeedItem("strawberry_seeds",
        new BaseSeedItem(seedSettings("strawberry_seeds"), STRAWBERRY_CROP, STRAWBERRY_CONFIG));

    // ========= Summer Spangle (one-time, farmland, summer) =========
    private static final VoxelShape[] SUMMERSPANGLE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig SUMMERSPANGLE_CONFIG = CropConfig.builder("summerspangle", 8, SUMMERSPANGLE_SHAPES)
        
        .seasons(Season.SUMMER).build();

    public static final Block SUMMERSPANGLE_CROP = registerCropBlock("summerspangle_crop",
        new BaseCropBlock(cropSettings("summerspangle_crop"), SUMMERSPANGLE_CONFIG));

    public static final Item SUMMERSPANGLE_SEEDS = registerSeedItem("summerspangle_seeds",
        new BaseSeedItem(seedSettings("summerspangle_seeds"), SUMMERSPANGLE_CROP, SUMMERSPANGLE_CONFIG));

    // ========= Summer Squash (continuous, farmland, summer) =========
    private static final VoxelShape[] SUMMERSQUASH_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig SUMMERSQUASH_CONFIG = CropConfig.builder("summersquash", 9, SUMMERSQUASH_SHAPES)
        .regrowCycle(3)
        
        .seasons(Season.SUMMER).build();

    public static final Block SUMMERSQUASH_CROP = registerCropBlock("summersquash_crop",
        new BaseCropBlock(cropSettings("summersquash_crop"), SUMMERSQUASH_CONFIG));

    public static final Item SUMMERSQUASH_SEEDS = registerSeedItem("summersquash_seeds",
        new BaseSeedItem(seedSettings("summersquash_seeds"), SUMMERSQUASH_CROP, SUMMERSQUASH_CONFIG));

    // ========= Sunflower (one-time, farmland, summer/fall) =========
    private static final VoxelShape[] SUNFLOWER_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16)
    };

    private static final CropConfig SUNFLOWER_CONFIG = CropConfig.builder("sunflower", 8, SUNFLOWER_SHAPES)
        .harvestSeedDrop(1)
        
        .seasons(Season.SUMMER, Season.FALL).build();

    public static final Block SUNFLOWER_CROP = registerCropBlock("sunflower_crop",
        new BaseCropBlock(cropSettings("sunflower_crop"), SUNFLOWER_CONFIG));

    public static final Item SUNFLOWER_SEEDS = registerSeedItem("sunflower_seeds",
        new BaseSeedItem(seedSettings("sunflower_seeds"), SUNFLOWER_CROP, SUNFLOWER_CONFIG));

    // ========= Sweet Gem Berry (one-time, farmland, fall) =========
    private static final VoxelShape[] SWEETGEMBERRY_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16)
    };

    private static final CropConfig SWEETGEMBERRY_CONFIG = CropConfig.builder("sweetgemberry", 24, SWEETGEMBERRY_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block SWEETGEMBERRY_CROP = registerCropBlock("sweetgemberry_crop",
        new BaseCropBlock(cropSettings("sweetgemberry_crop"), SWEETGEMBERRY_CONFIG));

    public static final Item SWEETGEMBERRY_SEEDS = registerSeedItem("sweetgemberry_seeds",
        new BaseSeedItem(seedSettings("sweetgemberry_seeds"), SWEETGEMBERRY_CROP, SWEETGEMBERRY_CONFIG));

    // ========= Taro Root (one-time, farmland, summer) =========
    private static final VoxelShape[] TAROROOT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16)
    };

    private static final CropConfig TAROROOT_CONFIG = CropConfig.builder("taroroot", 7, TAROROOT_SHAPES)
        
        .seasons(Season.SUMMER).build();

    public static final Block TAROROOT_CROP = registerCropBlock("taroroot_crop",
        new BaseCropBlock(cropSettings("taroroot_crop"), TAROROOT_CONFIG));

    public static final Item TAROROOT_SEEDS = registerSeedItem("taroroot_seeds",
        new BaseSeedItem(seedSettings("taroroot_seeds"), TAROROOT_CROP, TAROROOT_CONFIG));

    // ========= Tea Leaves (continuous, farmland, spring/summer/fall) =========
    private static final VoxelShape[] TEALEAVES_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16)
    };

    private static final CropConfig TEALEAVES_CONFIG = CropConfig.builder("tealeaves", 22, TEALEAVES_SHAPES)
        .regrowCycle(1)
        
        .seasons(Season.SPRING, Season.SUMMER, Season.FALL).build();

    public static final Block TEALEAVES_CROP = registerCropBlock("tealeaves_crop",
        new BaseCropBlock(cropSettings("tealeaves_crop"), TEALEAVES_CONFIG));

    public static final Item TEALEAVES_SEEDS = registerSeedItem("tealeaves_seeds",
        new BaseSeedItem(seedSettings("tealeaves_seeds"), TEALEAVES_CROP, TEALEAVES_CONFIG));

    // ========= Tomato (continuous, farmland, summer, extra 5%) =========
    private static final VoxelShape[] TOMATO_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 22, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16)
    };

    private static final CropConfig TOMATO_CONFIG = CropConfig.builder("tomato", 15, TOMATO_SHAPES)
        .regrowCycle(4)
        .extraDropChance(0.05f)
        
        .seasons(Season.SUMMER).build();

    public static final Block TOMATO_CROP = registerCropBlock("tomato_crop",
        new BaseCropBlock(cropSettings("tomato_crop"), TOMATO_CONFIG));

    public static final Item TOMATO_SEEDS = registerSeedItem("tomato_seeds",
        new BaseSeedItem(seedSettings("tomato_seeds"), TOMATO_CROP, TOMATO_CONFIG));

    // ========= Tulip (one-time, farmland, spring) =========
    private static final VoxelShape[] TULIP_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 7, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16)
    };

    private static final CropConfig TULIP_CONFIG = CropConfig.builder("tulip", 6, TULIP_SHAPES)
        
        .seasons(Season.SPRING).build();

    public static final Block TULIP_CROP = registerCropBlock("tulip_crop",
        new BaseCropBlock(cropSettings("tulip_crop"), TULIP_CONFIG));

    public static final Item TULIP_SEEDS = registerSeedItem("tulip_seeds",
        new BaseSeedItem(seedSettings("tulip_seeds"), TULIP_CROP, TULIP_CONFIG));

    // ========= Unmilled Rice (one-time, farmland, spring) =========
    private static final VoxelShape[] UNMILLEDRICE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16)
    };

    private static final CropConfig UNMILLEDRICE_CONFIG = CropConfig.builder("unmilledrice", 6, UNMILLEDRICE_SHAPES)
        
        .seasons(Season.SPRING).build();

    public static final Block UNMILLEDRICE_CROP = registerCropBlock("unmilledrice_crop",
        new BaseCropBlock(cropSettings("unmilledrice_crop"), UNMILLEDRICE_CONFIG));

    public static final Item UNMILLEDRICE_SEEDS = registerSeedItem("unmilledrice_seeds",
        new BaseSeedItem(seedSettings("unmilledrice_seeds"), UNMILLEDRICE_CROP, UNMILLEDRICE_CONFIG));

    // ========= Wheat (one-time, farmland, summer/fall) =========
    private static final VoxelShape[] WHEAT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16)
    };

    private static final CropConfig WHEAT_CONFIG = CropConfig.builder("wheat", 4, WHEAT_SHAPES)
        
        .seasons(Season.SUMMER, Season.FALL).build();

    public static final Block WHEAT_CROP = registerCropBlock("wheat_crop",
        new BaseCropBlock(cropSettings("wheat_crop"), WHEAT_CONFIG));

    public static final Item WHEAT_SEEDS = registerSeedItem("wheat_seeds",
        new BaseSeedItem(seedSettings("wheat_seeds"), WHEAT_CROP, WHEAT_CONFIG));

    // ========= Yam (one-time, farmland, fall) =========
    private static final VoxelShape[] YAM_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16)
    };

    private static final CropConfig YAM_CONFIG = CropConfig.builder("yam", 9, YAM_SHAPES)
        
        .seasons(Season.FALL).build();

    public static final Block YAM_CROP = registerCropBlock("yam_crop",
        new BaseCropBlock(cropSettings("yam_crop"), YAM_CONFIG));

    public static final Item YAM_SEEDS = registerSeedItem("yam_seeds",
        new BaseSeedItem(seedSettings("yam_seeds"), YAM_CROP, YAM_CONFIG));


    // ========= Carrot (one-time, farmland, spring) =========
    private static final VoxelShape[] CARROT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16)
    };

    private static final CropConfig CARROT_CONFIG = CropConfig.builder("carrot", 3, CARROT_SHAPES)
        .seasons(Season.SPRING)
        .build();

    public static final Block CARROT_CROP = registerCropBlock("carrot_crop",
        new BaseCropBlock(cropSettings("carrot_crop"), CARROT_CONFIG));

    public static final Item CARROT_SEEDS = registerSeedItem("carrot_seeds",
        new BaseSeedItem(seedSettings("carrot_seeds"), CARROT_CROP, CARROT_CONFIG));

    // ========= Cauliflower (one-time, farmland, spring) =========
    private static final VoxelShape[] CAULIFLOWER_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16)
    };

    private static final CropConfig CAULIFLOWER_CONFIG = CropConfig.builder("cauliflower", 12, CAULIFLOWER_SHAPES)
        .seasons(Season.SPRING)
        .build();

    public static final Block CAULIFLOWER_CROP = registerCropBlock("cauliflower_crop",
        new BaseCropBlock(cropSettings("cauliflower_crop"), CAULIFLOWER_CONFIG));

    public static final Item CAULIFLOWER_SEEDS = registerSeedItem("cauliflower_seeds",
        new BaseSeedItem(seedSettings("cauliflower_seeds"), CAULIFLOWER_CROP, CAULIFLOWER_CONFIG));

    // ========= Coffee Bean (continuous, farmland, spring/summer) =========
    private static final VoxelShape[] COFFEEBEAN_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 13, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 18, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16),
        Block.createCuboidShape(0, 0, 0, 16, 25, 16)
    };

    private static final CropConfig COFFEEBEAN_CONFIG = CropConfig.builder("coffeebean", 12, COFFEEBEAN_SHAPES)
        .regrowCycle(2)
        .harvestDropCount(4)
        .extraDropChance(0.05f)
        .seasons(Season.SPRING, Season.SUMMER)
        .build();

    public static final Block COFFEEBEAN_CROP = registerCropBlock("coffeebean_crop",
        new BaseCropBlock(cropSettings("coffeebean_crop"), COFFEEBEAN_CONFIG));

    public static final Item COFFEEBEAN_SEEDS = registerSeedItem("coffeebean",
        new BaseSeedItem(seedSettings("coffeebean"), COFFEEBEAN_CROP, COFFEEBEAN_CONFIG));
    public static final Item COFFEEBEAN_SEEDS_SILVER = registerSeedItem("coffeebean_silver",
        new BaseSeedItem(seedSettings("coffeebean_silver"), COFFEEBEAN_CROP, COFFEEBEAN_CONFIG));
    public static final Item COFFEEBEAN_SEEDS_GOLD = registerSeedItem("coffeebean_gold",
        new BaseSeedItem(seedSettings("coffeebean_gold"), COFFEEBEAN_CROP, COFFEEBEAN_CONFIG));
    public static final Item COFFEEBEAN_SEEDS_IRIDIUM = registerSeedItem("coffeebean_iridium",
        new BaseSeedItem(seedSettings("coffeebean_iridium"), COFFEEBEAN_CROP, COFFEEBEAN_CONFIG));

    // ========= Corn (continuous, farmland, summer/fall, extra 5%) =========
    private static final VoxelShape[] CORN_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 11, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 22, 16),
        Block.createCuboidShape(0, 0, 0, 16, 22, 16),
        Block.createCuboidShape(0, 0, 0, 16, 22, 16),
        Block.createCuboidShape(0, 0, 0, 16, 28, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 1, 16)
    };

    private static final CropConfig CORN_CONFIG = CropConfig.builder("corn", 18, CORN_SHAPES)
        .regrowCycle(4)
        .seasons(Season.SUMMER, Season.FALL)
        .build();

    public static final Block CORN_CROP = registerCropBlock("corn_crop",
        new BaseCropBlock(cropSettings("corn_crop"), CORN_CONFIG));

    public static final Item CORN_SEEDS = registerSeedItem("corn_seeds",
        new BaseSeedItem(seedSettings("corn_seeds"), CORN_CROP, CORN_CONFIG));

    // ========= Cranberries (continuous, farmland, fall) =========
    private static final VoxelShape[] CRANBERRIES_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 15, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16),
        Block.createCuboidShape(0, 0, 0, 16, 21, 16)
    };

    private static final CropConfig CRANBERRIES_CONFIG = CropConfig.builder("cranberries", 10, CRANBERRIES_SHAPES)
        .regrowCycle(4)
        .seasons(Season.FALL)
        .harvestDropCount(2)
        .extraDropChance(0.1f)
        .build();

    public static final Block CRANBERRIES_CROP = registerCropBlock("cranberries_crop",
        new BaseCropBlock(cropSettings("cranberries_crop"), CRANBERRIES_CONFIG));

    public static final Item CRANBERRIES_SEEDS = registerSeedItem("cranberries_seeds",
        new BaseSeedItem(seedSettings("cranberries_seeds"), CRANBERRIES_CROP, CRANBERRIES_CONFIG));

    // ========= Eggplant (continuous, farmland, fall, extra 0.2%) =========
    private static final VoxelShape[] EGGPLANT_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig EGGPLANT_CONFIG = CropConfig.builder("eggplant", 9, EGGPLANT_SHAPES)
        .regrowCycle(4)
        .seasons(Season.FALL)
        .extraDropChance(0.02f)
        .build();

    public static final Block EGGPLANT_CROP = registerCropBlock("eggplant_crop",
        new BaseCropBlock(cropSettings("eggplant_crop"), EGGPLANT_CONFIG));

    public static final Item EGGPLANT_SEEDS = registerSeedItem("eggplant_seeds",
        new BaseSeedItem(seedSettings("eggplant_seeds"), EGGPLANT_CROP, EGGPLANT_CONFIG));

    // ========= Fairy Rose (one-time, farmland, fall) =========
    private static final VoxelShape[] FAIRYROSE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 17, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16)
    };

    private static final CropConfig FAIRYROSE_CONFIG = CropConfig.builder("fairyrose", 12, FAIRYROSE_SHAPES)
        .seasons(Season.FALL)
        .build();

    public static final Block FAIRYROSE_CROP = registerCropBlock("fairyrose_crop",
        new BaseCropBlock(cropSettings("fairyrose_crop"), FAIRYROSE_CONFIG));

    public static final Item FAIRYROSE_SEEDS = registerSeedItem("fairyrose_seeds",
        new BaseSeedItem(seedSettings("fairyrose_seeds"), FAIRYROSE_CROP, FAIRYROSE_CONFIG));

    // ========= Fiber (one-time, farmland, all seasons) =========
    private static final VoxelShape[] FIBER_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16)
    };

    private static final CropConfig FIBER_CONFIG = CropConfig.builder("fiber", 6, FIBER_SHAPES)
        .seasons(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER)
        .build();

    public static final Block FIBER_CROP = registerCropBlock("fiber_crop",
        new BaseCropBlock(cropSettings("fiber_crop"), FIBER_CONFIG));

    public static final Item FIBER_SEEDS = registerSeedItem("fiber_seeds",
        new BaseSeedItem(seedSettings("fiber_seeds"), FIBER_CROP, FIBER_CONFIG));

    // ========= Garlic (one-time, farmland, spring) =========
    private static final VoxelShape[] GARLIC_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 14, 16),
        Block.createCuboidShape(0, 0, 0, 16, 19, 16)
    };

    private static final CropConfig GARLIC_CONFIG = CropConfig.builder("garlic", 4, GARLIC_SHAPES)
        .seasons(Season.SPRING)
        .build();

    public static final Block GARLIC_CROP = registerCropBlock("garlic_crop",
        new BaseCropBlock(cropSettings("garlic_crop"), GARLIC_CONFIG));

    public static final Item GARLIC_SEEDS = registerSeedItem("garlic_seeds",
        new BaseSeedItem(seedSettings("garlic_seeds"), GARLIC_CROP, GARLIC_CONFIG));

    // ========= Grape (continuous, farmland, fall) =========
    private static final VoxelShape[] GRAPE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16),
        Block.createCuboidShape(0, 0, 0, 16, 30, 16)
    };

    private static final CropConfig GRAPE_CONFIG = CropConfig.builder("grape", 15, GRAPE_SHAPES)
        .regrowCycle(3)
        .seasons(Season.FALL)
        .build();

    public static final Block GRAPE_CROP = registerCropBlock("grape_crop",
        new BaseCropBlock(cropSettings("grape_crop"), GRAPE_CONFIG));

    public static final Item GRAPE_SEEDS = registerSeedItem("grapestarter",
        new BaseSeedItem(seedSettings("grapestarter"), GRAPE_CROP, GRAPE_CONFIG));

    // ========= Green Bean (continuous, farmland, spring) =========
    private static final VoxelShape[] GREENBEAN_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 23, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16),
        Block.createCuboidShape(0, 0, 0, 16, 27, 16)
    };

    private static final CropConfig GREENBEAN_CONFIG = CropConfig.builder("greenbean", 16, GREENBEAN_SHAPES)
        .regrowCycle(3)
        .seasons(Season.SPRING)
        .build();

    public static final Block GREENBEAN_CROP = registerCropBlock("greenbean_crop",
        new BaseCropBlock(cropSettings("greenbean_crop"), GREENBEAN_CONFIG));

    public static final Item GREENBEAN_SEEDS = registerSeedItem("beanstarter",
        new BaseSeedItem(seedSettings("beanstarter"), GREENBEAN_CROP, GREENBEAN_CONFIG));

    // ========= Hops (continuous, farmland, summer) =========
    private static final VoxelShape[] HOPS_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16),
        Block.createCuboidShape(0, 0, 0, 16, 29, 16)
    };

    private static final CropConfig HOPS_CONFIG = CropConfig.builder("hops", 16, HOPS_SHAPES)
        .regrowCycle(1)
        .seasons(Season.SUMMER)
        .build();

    public static final Block HOPS_CROP = registerCropBlock("hops_crop",
        new BaseCropBlock(cropSettings("hops_crop"), HOPS_CONFIG));

    public static final Item HOPS_SEEDS = registerSeedItem("hopsstarter",
        new BaseSeedItem(seedSettings("hopsstarter"), HOPS_CROP, HOPS_CONFIG));

    // ========= Hot Pepper (continuous, farmland, summer, extra 5%) =========
    private static final VoxelShape[] HOTPEPPER_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig HOTPEPPER_CONFIG = CropConfig.builder("hotpepper", 7, HOTPEPPER_SHAPES)
        .regrowCycle(3)
        .seasons(Season.SUMMER)
        .extraDropChance(0.03f)
        .build();

    public static final Block HOTPEPPER_CROP = registerCropBlock("hotpepper_crop",
        new BaseCropBlock(cropSettings("hotpepper_crop"), HOTPEPPER_CONFIG));

    public static final Item HOTPEPPER_SEEDS = registerSeedItem("hotpepper_seeds",
        new BaseSeedItem(seedSettings("hotpepper_seeds"), HOTPEPPER_CROP, HOTPEPPER_CONFIG));

    // ========= Kale (one-time, farmland, spring) =========
    private static final VoxelShape[] KALE_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig KALE_CONFIG = CropConfig.builder("kale", 6, KALE_SHAPES)
        .seasons(Season.SPRING)
        .build();

    public static final Block KALE_CROP = registerCropBlock("kale_crop",
        new BaseCropBlock(cropSettings("kale_crop"), KALE_CONFIG));

    public static final Item KALE_SEEDS = registerSeedItem("kale_seeds",
        new BaseSeedItem(seedSettings("kale_seeds"), KALE_CROP, KALE_CONFIG));

    // ========= Melon (one-time, farmland, summer) =========
    private static final VoxelShape[] MELON_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 6, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 9, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 20, 16)
    };

    private static final CropConfig MELON_CONFIG = CropConfig.builder("melon", 11, MELON_SHAPES)
        .seasons(Season.SUMMER)
        .build();

    public static final Block MELON_CROP = registerCropBlock("melon_crop",
        new BaseCropBlock(cropSettings("melon_crop"), MELON_CONFIG));

    public static final Item MELON_SEEDS = registerSeedItem("melon_seeds",
        new BaseSeedItem(seedSettings("melon_seeds"), MELON_CROP, MELON_CONFIG));

    // ========= Qigua (one-time, farmland, spring/summer/fall) =========
    private static final VoxelShape[] QIGUA_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 1, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16),
        Block.createCuboidShape(0, 0, 0, 16, 24, 16)
    };

    private static final CropConfig QIGUA_CONFIG = CropConfig.builder("qigua", 4, QIGUA_SHAPES)
        .seasons(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER)
        .build();

    public static final Block QIGUA_CROP = registerCropBlock("qigua_crop",
        new BaseCropBlock(cropSettings("qigua_crop"), QIGUA_CONFIG));

    public static final Item QIGUA_SEEDS = registerSeedItem("qigua_seeds",
        new BaseSeedItem(seedSettings("qigua_seeds"), QIGUA_CROP, QIGUA_CONFIG));

    // ========= 野生作物方块（15种，一次性，mod_farmland） =========
    private static final VoxelShape[] WILD_CROP_SHAPES = {
        Block.createCuboidShape(0, 0, 0, 16, 3, 16),
        Block.createCuboidShape(0, 0, 0, 16, 4, 16),
        Block.createCuboidShape(0, 0, 0, 16, 5, 16),
        Block.createCuboidShape(0, 0, 0, 16, 8, 16),
        Block.createCuboidShape(0, 0, 0, 16, 10, 16),
        Block.createCuboidShape(0, 0, 0, 16, 12, 16),
        Block.createCuboidShape(0, 0, 0, 16, 16, 16)
    };

    private static Block registerWildCrop(String cropId, Season season) {
        CropConfig config = CropConfig.builder(cropId, 6, WILD_CROP_SHAPES)
            .seasons(season)
            .build();
        return registerCropBlock(cropId + "_crop", new BaseCropBlock(cropSettings(cropId + "_crop"), config));
    }

    // 春季（4种）
    public static final Block CAIJI_WILDHORSERADISH_CROP = registerWildCrop("caiji_wildhorseradish", Season.SPRING);
    public static final Block CAIJI_DAFFODIL_CROP = registerWildCrop("caiji_daffodil", Season.SPRING);
    public static final Block CAIJI_LEEK_CROP = registerWildCrop("caiji_leek", Season.SPRING);
    public static final Block CAIJI_DANDELION_CROP = registerWildCrop("caiji_dandelion", Season.SPRING);

    // 夏季（3种，grape是作物已有grape_crop，所以用grape_ws作为方块ID）
    private static final VoxelShape[] GRAPE_WS_SHAPES = WILD_CROP_SHAPES;
    private static final CropConfig GRAPE_WS_CONFIG = CropConfig.builder("grape", 6, GRAPE_WS_SHAPES)
        .seasons(Season.SUMMER).build();
    public static final Block GRAPE_CROP_WS = registerCropBlock("grape_ws_crop",
        new BaseCropBlock(cropSettings("grape_ws_crop"), GRAPE_WS_CONFIG));
    public static final Block CAIJI_SPICEBERRY_CROP = registerWildCrop("caiji_spiceberry", Season.SUMMER);
    public static final Block CAIJI_SWEETPEA_CROP = registerWildCrop("caiji_sweetpea", Season.SUMMER);

    // 秋季（4种）
    public static final Block CAIJI_COMMONMUSHROOM_CROP = registerWildCrop("caiji_commonmushroom", Season.FALL);
    public static final Block CAIJI_WILDPLUM_CROP = registerWildCrop("caiji_wildplum", Season.FALL);
    public static final Block CAIJI_HAZELNUT_CROP = registerWildCrop("caiji_hazelnut", Season.FALL);
    public static final Block CAIJI_BLACKBERRY_CROP = registerWildCrop("caiji_blackberry", Season.FALL);

    // 冬季（4种）
    public static final Block CAIJI_CROCUS_CROP = registerWildCrop("caiji_crocus", Season.WINTER);
    public static final Block CAIJI_WINTERROOT_CROP = registerWildCrop("caiji_winterroot", Season.WINTER);
    public static final Block CAIJI_SNOWYAM_CROP = registerWildCrop("caiji_snowyam", Season.WINTER);
    public static final Block CAIJI_CRYSTALFRUIT_CROP = registerWildCrop("caiji_crystalfruit", Season.WINTER);

    // ========= 野生种子物品（WildSeedItem，在ModItems中不重复注册） =========
    private static Item registerWildSeed(String name, Block[] crops, Season[] seasons) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = new WildSeedItem(new Item.Settings().registryKey(key).maxCount(999), crops, seasons);
        return Registry.register(Registries.ITEM, key, item);
    }

    public static final Item SPRING_SEEDS = registerWildSeed("spring_seeds",
        new Block[]{CAIJI_WILDHORSERADISH_CROP, CAIJI_DAFFODIL_CROP, CAIJI_LEEK_CROP, CAIJI_DANDELION_CROP},
        new Season[]{Season.SPRING});

    public static final Item SUMMER_SEEDS = registerWildSeed("summer_seeds",
        new Block[]{GRAPE_CROP_WS, CAIJI_SPICEBERRY_CROP, CAIJI_SWEETPEA_CROP},
        new Season[]{Season.SUMMER});

    public static final Item FALL_SEEDS = registerWildSeed("fall_seeds",
        new Block[]{CAIJI_COMMONMUSHROOM_CROP, CAIJI_WILDPLUM_CROP, CAIJI_HAZELNUT_CROP, CAIJI_BLACKBERRY_CROP},
        new Season[]{Season.FALL});

    public static final Item WINTER_SEEDS = registerWildSeed("winter_seeds",
        new Block[]{CAIJI_CROCUS_CROP, CAIJI_WINTERROOT_CROP, CAIJI_SNOWYAM_CROP, CAIJI_CRYSTALFRUIT_CROP},
        new Season[]{Season.WINTER});

    // ========= 工匠设备 (Artisan Equipment Blocks) =========
    public static final Block BEE_HOUSE = registerArtisanBlock("bee_house",
        Block.createCuboidShape(0, 0, 0, 16, 24, 16), MachineType.BEE_HOUSE);
    public static final Block CASK = registerArtisanBlock("cask",
        Block.createCuboidShape(0, 0, 0, 16, 20, 16), MachineType.CASK);
    public static final Block CHEESE_PRESS = registerArtisanBlock("cheese_press",
        Block.createCuboidShape(0, 0, 0, 16, 24, 16), MachineType.CHEESE_PRESS);
    public static final Block DEHYDRATOR = registerArtisanBlock("dehydrator",
        Block.createCuboidShape(0, 0, 0, 16, 24, 16), MachineType.DEHYDRATOR);
    public static final Block FISH_SMOKER = registerArtisanBlock("fish_smoker",
        Block.createCuboidShape(0, 0, 0, 16, 28, 16), MachineType.FISH_SMOKER);
    public static final Block KEG = registerArtisanBlock("keg",
        Block.createCuboidShape(0, 0, 0, 16, 19, 16), MachineType.KEG);
    public static final Block LOOM = registerArtisanBlock("loom",
        Block.createCuboidShape(0, 0, 0, 16, 23, 16), MachineType.LOOM);
    public static final Block MAYONNAISE_MACHINE = registerArtisanBlock("mayonnaise_machine",
        Block.createCuboidShape(0, 0, 0, 16, 22, 16), MachineType.MAYONNAISE_MACHINE);
    public static final Block OIL_MAKER = registerArtisanBlock("oil_maker",
        Block.createCuboidShape(0, 0, 0, 16, 23, 16), MachineType.OIL_MAKER);
    public static final Block PRESERVES_JAR = registerArtisanBlock("preserves_jar",
        Block.createCuboidShape(0, 0, 0, 16, 18, 16), MachineType.PRESERVES_JAR);
    public static final Block FURNACE = registerBlock("furnace",
        new ArtisanEquipmentBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "furnace")))
            .mapColor(net.minecraft.block.MapColor.STONE_GRAY)
            .strength(1.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(),
            Block.createCuboidShape(0, 0, 0, 16, 21, 16), MachineType.FURNACE));

    public static final Item BEE_HOUSE_ITEM = registerBlockItem("bee_house", BEE_HOUSE);
    public static final Item CASK_ITEM = registerBlockItem("cask", CASK);
    public static final Item CHEESE_PRESS_ITEM = registerBlockItem("cheese_press", CHEESE_PRESS);
    public static final Item DEHYDRATOR_ITEM = registerBlockItem("dehydrator", DEHYDRATOR);
    public static final Item FISH_SMOKER_ITEM = registerBlockItem("fish_smoker", FISH_SMOKER);
    public static final Item KEG_ITEM = registerBlockItem("keg", KEG);
    public static final Item LOOM_ITEM = registerBlockItem("loom", LOOM);
    public static final Item MAYONNAISE_MACHINE_ITEM = registerBlockItem("mayonnaise_machine", MAYONNAISE_MACHINE);
    public static final Item OIL_MAKER_ITEM = registerBlockItem("oil_maker", OIL_MAKER);
    public static final Item PRESERVES_JAR_ITEM = registerBlockItem("preserves_jar", PRESERVES_JAR);
    public static final Item FURNACE_ITEM = registerBlockItem("furnace", FURNACE);

    // ========= Tapper (树液采集器) =========
    public static final Block TAPPER = registerBlock("tapper",
        new TapperBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "tapper")))
            .mapColor(net.minecraft.block.MapColor.OAK_TAN)
            .strength(2.0F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque()));
    public static final Item TAPPER_ITEM = registerBlockItem("tapper", TAPPER);

    // ========= Heavy Tapper (重型树液采集器) =========
    public static final Block HEAVY_TAPPER = registerBlock("heavy_tapper",
        new TapperBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "heavy_tapper")))
            .mapColor(net.minecraft.block.MapColor.OAK_TAN)
            .strength(2.0F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque(), true));
    public static final Item HEAVY_TAPPER_ITEM = registerBlockItem("heavy_tapper", HEAVY_TAPPER);

    // ========= Lightning Rod (避雷针) =========
    public static final Block LIGHTNING_ROD = registerBlock("lightning_rod",
        new LightningRodBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "lightning_rod")))
            .mapColor(net.minecraft.block.MapColor.IRON_GRAY)
            .strength(1.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque()));
    public static final Item LIGHTNING_ROD_ITEM = registerBlockItem("lightning_rod", LIGHTNING_ROD);

    // ========= 星露谷特殊草 =========
    public static final Block STARDEW_GRASS = registerBlock("stardew_grass",
        new StardewGrassBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "stardew_grass")))
            .breakInstantly()
            .noCollision()
            .nonOpaque()
            .sounds(BlockSoundGroup.GRASS)
            .mapColor(net.minecraft.block.MapColor.GREEN)));
    public static final Item STARDEW_GRASS_ITEM = registerBlockItem("stardew_grass", STARDEW_GRASS);

    // ========= Sprinkler (洒水器) =========
    public static final Block SPRINKLER = registerBlock("sprinkler",
        new SprinklerBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "sprinkler")))
            .mapColor(net.minecraft.block.MapColor.GRAY)
            .strength(1.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(), 1));
    public static final Item SPRINKLER_ITEM = registerBlockItem("sprinkler", SPRINKLER);

    // ========= Quality Sprinkler (优质洒水器) =========
    public static final Block QUALITY_SPRINKLER = registerBlock("quality_sprinkler",
        new SprinklerBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "quality_sprinkler")))
            .mapColor(net.minecraft.block.MapColor.IRON_GRAY)
            .strength(1.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(), 2));
    public static final Item QUALITY_SPRINKLER_ITEM = registerBlockItem("quality_sprinkler", QUALITY_SPRINKLER);

    // ========= Iridium Sprinkler (铱洒水器) =========
    public static final Block IRIDIUM_SPRINKLER = registerBlock("iridium_sprinkler",
        new SprinklerBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "iridium_sprinkler")))
            .mapColor(net.minecraft.block.MapColor.DIAMOND_BLUE)
            .strength(1.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(), 3));
    public static final Item IRIDIUM_SPRINKLER_ITEM = registerBlockItem("iridium_sprinkler", IRIDIUM_SPRINKLER);

    // ========= Bomb Blocks =========
    private static final float CHERRY_BOMB_POWER = 3.0f;  // 75% of TNT (4.0)
    private static final float BOMB_POWER = 5.0f;         // 125% of TNT (4.0)
    private static final float MEGA_BOMB_POWER = 8.0f;    // 200% of TNT (4.0)

    public static final Block CHERRY_BOMB_BLOCK = registerBlock("cherry_bomb_block",
        new BombBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "cherry_bomb_block")))
            .mapColor(net.minecraft.block.MapColor.RED)
            .strength(0.75F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(), CHERRY_BOMB_POWER));
    public static final Item CHERRY_BOMB_ITEM = registerBombItem("cherry_bomb", CHERRY_BOMB_BLOCK);

    public static final Block BOMB_BLOCK = registerBlock("bomb_block",
        new BombBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "bomb_block")))
            .mapColor(net.minecraft.block.MapColor.GRAY)
            .strength(0.75F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(), BOMB_POWER));
    public static final Item BOMB_BLOCK_ITEM = registerBombItem("bomb", BOMB_BLOCK);

    public static final Block MEGA_BOMB_BLOCK = registerBlock("mega_bomb_block",
        new BombBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "mega_bomb_block")))
            .mapColor(net.minecraft.block.MapColor.PURPLE)
            .strength(0.75F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(), MEGA_BOMB_POWER));
    public static final Item MEGA_BOMB_ITEM = registerBombItem("mega_bomb", MEGA_BOMB_BLOCK);

    // ========= 精炼设备 (Refined Equipment Blocks) =========
    public static final Block CHARCOAL_KILN = registerBlock("charcoal_kiln",
        new ArtisanEquipmentBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "charcoal_kiln")))
            .mapColor(net.minecraft.block.MapColor.STONE_GRAY)
            .strength(0.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(),
            Block.createCuboidShape(0, 0, 0, 16, 25, 16), MachineType.CHARCOAL_KILN));
    public static final Item CHARCOAL_KILN_ITEM = registerBlockItem("charcoal_kiln", CHARCOAL_KILN);

    public static final Block BAIT_MAKER = registerBlock("bait_maker",
        new ArtisanEquipmentBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "bait_maker")))
            .mapColor(net.minecraft.block.MapColor.OAK_TAN)
            .strength(0.5F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque(),
            Block.createCuboidShape(0, 0, 0, 16, 26, 16), MachineType.BAIT_MAKER));
    public static final Item BAIT_MAKER_ITEM = registerBlockItem("bait_maker", BAIT_MAKER);

    public static final Block CRAB_POT = registerBlock("crab_pot",
        new CrabPotBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "crab_pot")))
            .mapColor(net.minecraft.block.MapColor.OAK_TAN)
            .strength(0.5F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque()));
    public static final Item CRAB_POT_ITEM = registerBlockItem("crab_pot", CRAB_POT);

    public static final Block CRYSTALARIUM = registerBlock("crystalarium",
        new ArtisanEquipmentBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "crystalarium")))
            .mapColor(net.minecraft.block.MapColor.DIAMOND_BLUE)
            .strength(0.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(),
            Block.createCuboidShape(0, 0, 0, 16, 19, 16), MachineType.CRYSTALARIUM));
    public static final Item CRYSTALARIUM_ITEM = registerBlockItem("crystalarium", CRYSTALARIUM);

    public static final Block DELUXE_WORM_BIN = registerBlock("deluxe_worm_bin",
        new DeluxeWormBinBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "deluxe_worm_bin")))
            .mapColor(net.minecraft.block.MapColor.OAK_TAN)
            .strength(0.5F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque()));
    public static final Item DELUXE_WORM_BIN_ITEM = registerBlockItem("deluxe_worm_bin", DELUXE_WORM_BIN);

    public static final Block GARDEN_POT = registerBlock("garden_pot",
        new GardenPotBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "garden_pot")))
            .mapColor(net.minecraft.block.MapColor.TERRACOTTA_ORANGE)
            .strength(0.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque()));
    public static final Item GARDEN_POT_ITEM = registerBlockItem("garden_pot", GARDEN_POT);

    public static final Block MUSHROOM_LOG = registerBlock("mushroom_log",
        new MushroomStumpBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "mushroom_log")))
            .mapColor(net.minecraft.block.MapColor.BROWN)
            .strength(0.5F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque()));
    public static final Item MUSHROOM_LOG_ITEM = registerBlockItem("mushroom_log", MUSHROOM_LOG);

    // ========= 精炼设备 (Refinement Equipment Blocks) =========
    public static final Block SOLAR_PANEL = registerArtisanBlock("solar_panel",
        Block.createCuboidShape(0, 0, 0, 16, 24, 16), MachineType.SOLAR_PANEL);
    public static final Block SEED_MAKER = registerArtisanBlock("seed_maker",
        Block.createCuboidShape(0, 0, 0, 16, 25, 16), MachineType.SEED_MAKER);
    public static final Block GEODE_CRUSHER = registerArtisanBlock("geode_crusher",
        Block.createCuboidShape(0, 0, 0, 16, 21, 16), MachineType.GEODE_CRUSHER);
    public static final Block HEAVY_FURNACE = registerBlock("heavy_furnace",
        new ArtisanEquipmentBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "heavy_furnace")))
            .mapColor(net.minecraft.block.MapColor.STONE_GRAY)
            .strength(1.5F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque(),
            Block.createCuboidShape(0, 0, 0, 16, 23, 16), MachineType.HEAVY_FURNACE));
    public static final Block RECYCLING_MACHINE = registerArtisanBlock("recycling_machine",
        Block.createCuboidShape(0, 0, 0, 16, 18, 16), MachineType.RECYCLING_MACHINE);

    public static final Item SOLAR_PANEL_ITEM = registerBlockItem("solar_panel", SOLAR_PANEL);
    public static final Item SEED_MAKER_ITEM = registerBlockItem("seed_maker", SEED_MAKER);
    public static final Item GEODE_CRUSHER_ITEM = registerBlockItem("geode_crusher", GEODE_CRUSHER);
    public static final Item HEAVY_FURNACE_ITEM = registerBlockItem("heavy_furnace", HEAVY_FURNACE);
    public static final Item RECYCLING_MACHINE_ITEM = registerBlockItem("recycling_machine", RECYCLING_MACHINE);

    // ========= 种植在地上的树种子方块 =========
    public static final Block PLANTED_OAK = registerBlock("planted_oak",
        new TreeSeedBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "planted_oak")))
            .mapColor(net.minecraft.block.MapColor.GREEN)
            .strength(0.6F)
            .sounds(BlockSoundGroup.GRASS)
            .noCollision()
            .nonOpaque(), "acorn"));

    public static final Block PLANTED_MAPLE = registerBlock("planted_maple",
        new TreeSeedBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "planted_maple")))
            .mapColor(net.minecraft.block.MapColor.GREEN)
            .strength(0.6F)
            .sounds(BlockSoundGroup.GRASS)
            .noCollision()
            .nonOpaque(), "maple_seed"));

    public static final Block PLANTED_PINE = registerBlock("planted_pine",
        new TreeSeedBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "planted_pine")))
            .mapColor(net.minecraft.block.MapColor.GREEN)
            .strength(0.6F)
            .sounds(BlockSoundGroup.GRASS)
            .noCollision()
            .nonOpaque(), "pine_cone"));

    public static final Block PLANTED_MAHOGANY = registerBlock("planted_mahogany",
        new TreeSeedBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, "planted_mahogany")))
            .mapColor(net.minecraft.block.MapColor.GREEN)
            .strength(0.6F)
            .sounds(BlockSoundGroup.GRASS)
            .noCollision()
            .nonOpaque(), "mahogany_seed"));

    public static final Item ACORN = registerTreeSeedItem("acorn", PLANTED_OAK);
    public static final Item MAPLE_SEED = registerTreeSeedItem("maple_seed", PLANTED_MAPLE);
    public static final Item PINE_CONE = registerTreeSeedItem("pine_cone", PLANTED_PINE);
    public static final Item MAHOGANY_SEED = registerTreeSeedItem("mahogany_seed", PLANTED_MAHOGANY);

    // ========= Registration helpers =========
    private static AbstractBlock.Settings cropSettings(String name) {
        return AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(StardewValley.MOD_ID, name)))
            .noCollision()
            .nonOpaque()
            .breakInstantly()
            .sounds(BlockSoundGroup.CROP)
            .ticksRandomly();
    }

    private static Item.Settings seedSettings(String name) {
        return new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(StardewValley.MOD_ID, name)))
            .maxCount(999);
    }

    private static Block registerBlock(String name, Block block) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        return Registry.register(Registries.BLOCK, key, block);
    }

    private static Block registerCropBlock(String name, Block block) {
        return registerBlock(name, block);
    }

    private static Item registerSeedItem(String name, Item item) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key, item);
    }

    // ========= Artisan Equipment registration helpers =========
    private static Block registerArtisanBlock(String name, VoxelShape shape, MachineType machineType) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        ArtisanEquipmentBlock block = new ArtisanEquipmentBlock(AbstractBlock.Settings.create()
                .registryKey(key)
                .mapColor(net.minecraft.block.MapColor.OAK_TAN)
                .strength(2.0F)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque(),
            shape, machineType);
        if (machineType == MachineType.BEE_HOUSE) {
            // Bee house tracking is handled in ArtisanEquipmentBlock
        }
        return Registry.register(Registries.BLOCK, key, block);
    }

    private static Item registerBlockItem(String name, Block block) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = new BlockItem(block, new Item.Settings().registryKey(key).useBlockPrefixedTranslationKey().maxCount(999));
        return Registry.register(Registries.ITEM, key, item);
    }

    private static Item registerBombItem(String name, Block block) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = new stardewvalley.modid.item.BombItem(
            new Item.Settings().registryKey(key).maxCount(999), block);
        return Registry.register(Registries.ITEM, key, item);
    }

    private static Item registerTreeSeedItem(String name, Block block) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = new TreeSeedItem(new Item.Settings().registryKey(key).maxCount(999), block);
        return Registry.register(Registries.ITEM, key, item);
    }

    // ========= Totem Pole (invisible display block) =========
    public static final Block TOTEM_POLE = registerTotemPole();
    private static Block registerTotemPole() {
        Identifier id = Identifier.of(StardewValley.MOD_ID, "totem_pole");
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        TotemPoleBlock block = new TotemPoleBlock(AbstractBlock.Settings.create()
            .registryKey(key)
            .noBlockBreakParticles()
            .dropsNothing()
            .nonOpaque()
            .noCollision()
            .replaceable());
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static void registerAll() {
    }
}
