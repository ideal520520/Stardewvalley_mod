package stardewvalley.modid.crop;

import net.minecraft.util.math.random.Random;

public class QualityCalculator {

    public static CropQuality rollQuality(Random random, int farmingLevel, int fertilizerTier, float luckMultiplier) {
        float goldChance = (0.2f * (farmingLevel / 10.0f)
            + 0.2f * fertilizerTier * ((farmingLevel + 2) / 12.0f)
            + 0.01f) * luckMultiplier;

        if (fertilizerTier >= 3) {
            float iridiumChance = goldChance / 2.0f;
            if (random.nextFloat() < iridiumChance) {
                return CropQuality.IRIDIUM;
            }
        }

        if (random.nextFloat() < goldChance) {
            return CropQuality.GOLD;
        }

        float silverChance = goldChance * 2.0f;
        if (random.nextFloat() < silverChance) {
            return CropQuality.SILVER;
        }

        return CropQuality.NORMAL;
    }
}
