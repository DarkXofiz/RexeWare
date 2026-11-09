package com.rexeware.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/** AttackAura for Minecraft 1.20.1. Call AttackAura.tick() every client tick (START_CLIENT_TICK). */
public final class AttackAura {
    public static boolean enabled = false;
    public static double range = 3.5;
    public static float rotationSpeed = 30.0f;
    public static float cooldown = 0.95f;
    public static boolean playersOnly = true;
    public static boolean rotate = true;
    public static boolean visibleOnly = false;
    public static boolean lowestHealth = false;

    private static LivingEntity target;

    private AttackAura() {
    }

    public static LivingEntity getTarget() {
        return target;
    }

    public static void tick() {
        target = null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!enabled || mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) {
            return;
        }
        target = CombatUtil.findTarget(range, playersOnly, visibleOnly, lowestHealth);
        if (target == null) {
            return;
        }
        if (rotate) {
            CombatUtil.rotateToward(mc.player, new Vec3d(target.getX(), target.getBodyY(0.6), target.getZ()), rotationSpeed);
        }
        if (mc.player.getAttackCooldownProgress(0.5f) >= cooldown) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
