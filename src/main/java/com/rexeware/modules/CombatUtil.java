package com.rexeware.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

/** Shared helpers for AttackAura, ElytraTarget and ElytraHelper (Minecraft 1.20.1, Yarn). */
public final class CombatUtil {
    /** Replace with your own friend check, e.g. e -> FriendManager.isFriend(e). */
    public static Predicate<Entity> friendCheck = e -> false;

    private CombatUtil() {
    }

    public static LivingEntity findTarget(double range, boolean playersOnly) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            return null;
        }
        LivingEntity best = null;
        double bestDist = range * range;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player) {
                continue;
            }
            if (!living.isAlive() || living.isSpectator()) {
                continue;
            }
            if (playersOnly && !(living instanceof PlayerEntity)) {
                continue;
            }
            if (friendCheck.test(living)) {
                continue;
            }
            double dist = mc.player.squaredDistanceTo(living);
            if (dist <= bestDist) {
                bestDist = dist;
                best = living;
            }
        }
        return best;
    }

    public static void rotateToward(ClientPlayerEntity player, Vec3d target, float maxStep) {
        double dx = target.x - player.getX();
        double dy = target.y - player.getEyeY();
        double dz = target.z - player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float wantedYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float wantedPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        float deltaYaw = MathHelper.clamp(MathHelper.wrapDegrees(wantedYaw - player.getYaw()), -maxStep, maxStep);
        float deltaPitch = MathHelper.clamp(wantedPitch - player.getPitch(), -maxStep, maxStep);

        player.setYaw(player.getYaw() + deltaYaw);
        player.setPitch(MathHelper.clamp(player.getPitch() + deltaPitch, -90.0f, 90.0f));
    }
}
