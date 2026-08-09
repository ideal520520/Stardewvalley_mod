package stardewvalley.modid.crop;

import net.minecraft.block.BlockState;
import net.minecraft.util.shape.VoxelShape;
import stardewvalley.modid.block.ModFarmlandBlock;
import stardewvalley.modid.season.Season;

import java.util.function.Predicate;

public class CropConfig {

    private final String cropId;
    private final int maxAge;
    private final int regrowCycle;
    private final VoxelShape[] shapes;
    private final Season[] seasons;
    private final Predicate<BlockState> groundTest;
    private final float extraDropChance;
    private final int harvestDropCount;
    private final int harvestSeedDrop;
    private final boolean requiresWater;

    private CropConfig(Builder builder) {
        this.cropId = builder.cropId;
        this.maxAge = builder.maxAge;
        this.regrowCycle = builder.regrowCycle;
        this.shapes = builder.shapes;
        this.seasons = builder.seasons;
        this.groundTest = builder.groundTest;
        this.extraDropChance = builder.extraDropChance;
        this.harvestDropCount = builder.harvestDropCount;
        this.harvestSeedDrop = builder.harvestSeedDrop;
        this.requiresWater = builder.requiresWater;
    }

    public String cropId() { return cropId; }
    public int maxAge() { return maxAge; }
    public int regrowCycle() { return regrowCycle; }
    public int growthAge() { return maxAge - regrowCycle; }
    public VoxelShape[] shapes() { return shapes; }
    public Season[] seasons() { return seasons; }
    public Predicate<BlockState> groundTest() { return groundTest; }
    public float extraDropChance() { return extraDropChance; }
    public int harvestDropCount() { return harvestDropCount; }
    public int harvestSeedDrop() { return harvestSeedDrop; }
    public boolean requiresWater() { return requiresWater; }

    public static Builder builder(String cropId, int maxAge, VoxelShape[] shapes) {
        return new Builder(cropId, maxAge, shapes);
    }

    public static class Builder {
        private final String cropId;
        private final int maxAge;
        private final VoxelShape[] shapes;
        private int regrowCycle = 0;
        private Season[] seasons = Season.values();
        private Predicate<BlockState> groundTest = state -> state.getBlock() instanceof ModFarmlandBlock;
        private float extraDropChance = 0.0f;
        private int harvestDropCount = 1;
        private int harvestSeedDrop = 0;
        private boolean requiresWater = true;

        private Builder(String cropId, int maxAge, VoxelShape[] shapes) {
            this.cropId = cropId;
            this.maxAge = maxAge;
            this.shapes = shapes;
        }

        public Builder regrowCycle(int regrowCycle) {
            this.regrowCycle = regrowCycle;
            return this;
        }

        public Builder seasons(Season... seasons) {
            this.seasons = seasons;
            return this;
        }

        public Builder groundTest(Predicate<BlockState> groundTest) {
            this.groundTest = groundTest;
            return this;
        }

        public Builder extraDropChance(float extraDropChance) {
            this.extraDropChance = extraDropChance;
            return this;
        }

        public Builder harvestDropCount(int harvestDropCount) {
            this.harvestDropCount = harvestDropCount;
            return this;
        }

        public Builder harvestSeedDrop(int harvestSeedDrop) {
            this.harvestSeedDrop = harvestSeedDrop;
            return this;
        }

        public Builder requiresWater(boolean requiresWater) {
            this.requiresWater = requiresWater;
            return this;
        }

        public CropConfig build() {
            if (shapes.length != maxAge + 1) {
                throw new IllegalArgumentException("shapes length must be maxAge + 1");
            }
            return new CropConfig(this);
        }
    }
}