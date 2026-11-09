package com.rexeware;

import com.mojang.brigadier.Command;
import com.rexeware.modules.AttackAura;
import com.rexeware.modules.ElytraHelper;
import com.rexeware.modules.ElytraTarget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

public class RexeWareClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            AttackAura.tick();
            ElytraTarget.tick();
            ElytraHelper.tick();
        });

        // Komutlar: /rexe aura, /rexe elytratarget, /rexe elytrahelper (her biri ac/kapat)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("rexe")
                        .then(ClientCommandManager.literal("aura").executes(ctx -> {
                            AttackAura.enabled = !AttackAura.enabled;
                            ctx.getSource().sendFeedback(Text.literal("AttackAura: " + (AttackAura.enabled ? "ON" : "OFF")));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(ClientCommandManager.literal("elytratarget").executes(ctx -> {
                            ElytraTarget.enabled = !ElytraTarget.enabled;
                            ctx.getSource().sendFeedback(Text.literal("ElytraTarget: " + (ElytraTarget.enabled ? "ON" : "OFF")));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(ClientCommandManager.literal("elytrahelper").executes(ctx -> {
                            ElytraHelper.enabled = !ElytraHelper.enabled;
                            ctx.getSource().sendFeedback(Text.literal("ElytraHelper: " + (ElytraHelper.enabled ? "ON" : "OFF")));
                            return Command.SINGLE_SUCCESS;
                        }))));
    }
}
