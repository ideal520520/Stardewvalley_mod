package stardewvalley.modid.fishing;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FishingTimeManager {
    private static boolean timeFrozen = false;
    private static long frozenTime = -1;

    public static void freeze(ServerWorld world) {
        if (world == null) return;
        frozenTime = world.getTimeOfDay();
        timeFrozen = true;
    }

    public static void unfreeze() {
        timeFrozen = false;
        frozenTime = -1;
    }

    public static boolean isTimeFrozen() {
        return timeFrozen;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!timeFrozen || frozenTime < 0) return;

            for (ServerWorld world : server.getWorlds()) {
                if (world.getTimeOfDay() != frozenTime) {
                    world.setTimeOfDay(frozenTime);
                }
            }
        });
    }
}
