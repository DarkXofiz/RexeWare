package com.rexeware.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;

/** Shared helpers for AttackAura, ElytraTarget and ElytraHelper (Minecraft 1.20.1, Yarn). */
public final class CombatUtil {
    /** Replace with your own friend check, e.g. e -> FriendManager.isFriend(e). */
    public static Predicate<Entity> friendCheck = e -> false;

    private CombatUtil() {
    }

    /**
     * En uygun hedefi bulur. Tum dunyayi taramak yerine sadece menzil kutusundaki varliklari sorgular,
     * bu yuzden kalabalik sunucularda da FPS dusurmez.
     */
    public static LivingEntity findTarget(double range, boolean playersOnly, boolean visibleOnly, boolean lowestHealth) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity self = mc.player;
        if (self == null || mc.world == null) {
            return null;
        }
        Box box = self.getBoundingBox().expand(range);
        List<LivingEntity> nearby = mc.world.getEntitiesByClass(LivingEntity.class, box, e ->
                e != self
                        && e.isAlive()
                        && !e.isSpectator()
                        && !(e instanceof ArmorStandEntity)
                        && (!playersOnly || e instanceof PlayerEntity)
                        && !friendCheck.test(e));

        double maxDistSq = range * range;
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : nearby) {
            double distSq = self.squaredDistanceTo(e);
            if (distSq > maxDistSq) {
                continue;
            }
            if (visibleOnly && !self.canSee(e)) {
                continue;
            }
            double score = lowestHealth ? (e.getHealth() + e.getAbsorptionAmount()) * 1000.0 + distSq : distSq;
            if (score < bestScore) {
                bestScore = score;
                best = e;
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
