package stardewvalley.modid.fishing;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class FishingModeManager {
    private static int currentMode = 1;

    public static int getMode() {
        return currentMode;
    }

    public static void setMode(int mode) {
        if (mode == 1 || mode == 2) {
            currentMode = mode;
        }
    }

    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("stardewvalley")
                .then(CommandManager.literal("fishingmod")
                    .then(CommandManager.literal("1").executes(ctx -> {
                        setMode(1);
                        ctx.getSource().sendFeedback(() -> Text.literal("钓鱼模式：原始模式"), false);
                        return 1;
                    }))
                    .then(CommandManager.literal("2").executes(ctx -> {
                        setMode(2);
                        ctx.getSource().sendFeedback(() -> Text.literal("钓鱼模式：小游戏模式"), false);
                        return 1;
                    }))
                    .executes(ctx -> {
                        int m = getMode();
                        String name = m == 1 ? "原始模式" : "小游戏模式";
                        ctx.getSource().sendFeedback(() -> Text.literal("当前钓鱼模式：" + name), false);
                        return 1;
                    }))
            );
        });
    }
}
