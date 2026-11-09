package com.rexeware;

import com.rexeware.modules.AttackAura;
import com.rexeware.modules.ElytraHelper;
import com.rexeware.modules.ElytraTarget;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

/** Ekran ustu bilgi paneli: watermark, aktif modul listesi ve koordinatlar. */
public final class Hud {
    public static boolean enabled = true;
    public static boolean watermark = true;
    public static boolean moduleList = true;
    public static boolean coords = true;

    private static final int ACCENT_A = 0xFF8B5CF6;
    private static final int ACCENT_B = 0xFF22D3EE;

    private record Entry(String name, BooleanSupplier on) {
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry("AttackAura", () -> AttackAura.enabled),
            new Entry("ElytraTarget", () -> ElytraTarget.enabled),
            new Entry("ElytraHelper", () -> ElytraHelper.enabled));

    private Hud() {
    }

    public static void init() {
        HudRenderCallback.EVENT.register(Hud::render);
    }

    private static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!enabled || mc.player == null || mc.options.hudHidden) {
            return;
        }
        TextRenderer tr = mc.textRenderer;

        if (watermark) {
            String name = "RexeWare";
            int w = tr.getWidth(name) + 12;
            ctx.fill(4, 4, 4 + w, 17, 0xA0101014);
            ctx.fill(4, 4, 6, 17, ACCENT_A);
            ctx.fill(4, 16, 4 + w, 17, ACCENT_B);
            ctx.drawText(tr, name, 10, 7, ACCENT_B, true);
        }

        if (moduleList) {
            List<String> names = new ArrayList<>();
            for (Entry e : ENTRIES) {
                if (e.on().getAsBoolean()) {
                    names.add(e.name());
                }
            }
            names.sort((a, b) -> tr.getWidth(b) - tr.getWidth(a));
            int screenW = ctx.getScaledWindowWidth();
            int y = 4;
            for (int i = 0; i < names.size(); i++) {
                String n = names.get(i);
                int w = tr.getWidth(n) + 8;
                int x = screenW - w - 4;
                int color = lerpColor(ACCENT_A, ACCENT_B, names.size() <= 1 ? 0f : i / (float) (names.size() - 1));
                ctx.fill(x, y, x + w, y + 12, 0xA0101014);
                ctx.fill(x + w - 2, y, x + w, y + 12, color);
                ctx.drawText(tr, n, x + 4, y + 2, color, true);
                y += 12;
            }
        }

        if (coords) {
            String text = String.format(Locale.ROOT, "XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            int y = ctx.getScaledWindowHeight() - 16;
            ctx.fill(4, y, 4 + tr.getWidth(text) + 8, y + 12, 0xA0101014);
            ctx.fill(4, y, 6, y + 12, ACCENT_A);
            ctx.drawText(tr, text, 10, y + 2, 0xFFE6E6F0, true);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >>> 16) & 255, ag = (a >>> 8) & 255, ab = a & 255;
        int br = (b >>> 16) & 255, bg = (b >>> 8) & 255, bb = b & 255;
        return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }
}
