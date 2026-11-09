package com.rexeware.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/** ElytraTarget for Minecraft 1.20.1. Call ElytraTarget.tick() every client tick (START_CLIENT_TICK). */
public final class ElytraTarget {
    public static boolean enabled = false;
    public static double range = 64.0;
    public static float rotationSpeed = 12.0f;
    public static double predictTicks = 3.0;
    public static boolean playersOnly = true;

    private static LivingEntity target;

    private ElytraTarget() {
    }

    public static LivingEntity getTarget() {
        return target;
    }

    public static void tick() {
        target = null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!enabled || mc.player == null || mc.world == null || !mc.player.isFallFlying()) {
            return;
        }
        target = CombatUtil.findTarget(range, playersOnly);
        if (target == null) {
            return;
        }
        Vec3d aim = new Vec3d(target.getX(), target.getBodyY(0.5), target.getZ())
                .add(target.getVelocity().multiply(predictTicks));
        CombatUtil.rotateToward(mc.player, aim, rotationSpeed);
    }
}
