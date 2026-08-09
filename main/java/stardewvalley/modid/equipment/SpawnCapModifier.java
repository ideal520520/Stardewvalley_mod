package stardewvalley.modid.equipment;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.effect.ModStatusEffects;

public class SpawnCapModifier {

    private static float currentMultiplier = 1.0f;
    private static long lastTick = -1;

    public static float getMultiplier() {
        return currentMultiplier;
    }

    public static void tick(ServerWorld world) {
        long time = world.getTime();
        if (time == lastTick) return;
        lastTick = time;

        float mult = 1.0f;
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.hasStatusEffect(ModStatusEffects.OIL_OF_GARLIC)) mult *= 0.5f;
            if (player.hasStatusEffect(ModStatusEffects.MONSTER_MUSK)) mult *= 2.0f;
        }
        currentMultiplier = mult;
    }
}
