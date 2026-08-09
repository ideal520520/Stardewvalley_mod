package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {

    private static KeyBinding openMenuKey;
    private static KeyBinding weaponSkillKey;
    private static KeyBinding openBackpackKey;

    public static void register() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.stardewvalley.open_menu",
            GLFW.GLFW_KEY_TAB,
            KeyBinding.Category.MISC
        ));

        weaponSkillKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.stardewvalley.weapon_skill",
            GLFW.GLFW_KEY_R,
            KeyBinding.Category.MISC
        ));

        openBackpackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.stardewvalley.open_backpack",
            GLFW.GLFW_KEY_B,
            KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.player != null) {
                    if (client.currentScreen instanceof MainGuiScreen) {
                        client.setScreen(null);
                    } else {
                        client.setScreen(new MainGuiScreen());
                    }
                }
            }
            while (weaponSkillKey.wasPressed()) {
                if (client.player != null) {
                    // 强制触发挥手动画（绕过swingHand的守卫条件）
                    client.player.handSwinging = true;
                    client.player.handSwingTicks = 0;
                    client.player.handSwingProgress = 0.0F;
                    ClientPlayNetworking.send(new ModPayloads.WeaponSkillC2SPayload());
                }
            }
            while (openBackpackKey.wasPressed()) {
                if (client.player != null) {
                    if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen
                        || client.currentScreen instanceof InventoryScreen) {
                        client.setScreen(null);
                    } else {
                        // 发送打开背包的请求到服务端
                        ClientPlayNetworking.send(new ModPayloads.BackpackOpenC2SPayload());
                    }
                }
            }
        });
    }
}
