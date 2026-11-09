package com.rexeware;

import com.rexeware.gui.ClickGuiScreen;
import com.rexeware.modules.AttackAura;
import com.rexeware.modules.ElytraHelper;
import com.rexeware.modules.ElytraTarget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RexeWareClient implements ClientModInitializer {
    /** ClickGUI tusu, varsayilan R. Oyun ayarlarindan degistirilebilir. */
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rexeware.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.rexeware"));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            AttackAura.tick();
            ElytraTarget.tick();
            ElytraHelper.tick();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
        });
    }
}
