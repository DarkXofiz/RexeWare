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

    private static boolean keyWasDown;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rexeware.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.rexeware"));

        Hud.init();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            AttackAura.tick();
            ElytraTarget.tick();
            ElytraHelper.tick();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Baska bir mod ayni tusa bagliysa bile calissin diye tusu dogrudan klavyeden okuyoruz.
            while (openGuiKey.wasPressed()) {
                // kuyrugu bosalt
            }
            int code = KeyBindingHelper.getBoundKeyOf(openGuiKey).getCode();
            boolean down = code != GLFW.GLFW_KEY_UNKNOWN
                    && InputUtil.isKeyPressed(client.getWindow().getHandle(), code);
            if (down && !keyWasDown && client.currentScreen == null && client.player != null) {
                client.setScreen(new ClickGuiScreen());
            }
            keyWasDown = down;
        });
    }
}
