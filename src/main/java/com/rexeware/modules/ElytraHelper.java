package com.rexeware.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

/** ElytraHelper for Minecraft 1.20.1. Call ElytraHelper.tick() every client tick (START_CLIENT_TICK). */
public final class ElytraHelper {
    public static boolean enabled = false;
    public static boolean autoTakeoff = true;
    public static boolean autoFirework = true;
    public static double minSpeed = 0.8;
    public static int fireworkDelay = 30;

    private static int takeoffCooldown;
    private static int fireworkCooldown;

    private ElytraHelper() {
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!enabled || mc.player == null || mc.world == null || mc.interactionManager == null || mc.getNetworkHandler() == null) {
            return;
        }
        if (takeoffCooldown > 0) {
            takeoffCooldown--;
        }
        if (fireworkCooldown > 0) {
            fireworkCooldown--;
        }

        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(Items.ELYTRA) || !ElytraItem.isUsable(chest)) {
            return;
        }

        if (autoTakeoff && takeoffCooldown == 0 && !mc.player.isOnGround() && !mc.player.isFallFlying()
                && !mc.player.isTouchingWater() && !mc.player.hasVehicle()
                && mc.player.getVelocity().y < 0.0 && mc.player.fallDistance > 0.5f) {
            mc.player.startFallFlying();
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            takeoffCooldown = 10;
        }

        if (autoFirework && mc.player.isFallFlying() && fireworkCooldown == 0
                && mc.player.getVelocity().length() < minSpeed && useFirework(mc)) {
            fireworkCooldown = fireworkDelay;
        }
    }

    private static boolean useFirework(MinecraftClient mc) {
        PlayerInventory inventory = mc.player.getInventory();
        if (mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            return true;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.getStack(slot).isOf(Items.FIREWORK_ROCKET)) {
                int previous = inventory.selectedSlot;
                inventory.selectedSlot = slot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                inventory.selectedSlot = previous;
                return true;
            }
        }
        return false;
    }
}
