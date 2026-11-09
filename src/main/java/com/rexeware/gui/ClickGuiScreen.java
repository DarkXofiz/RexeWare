package com.rexeware.gui;

import com.rexeware.Hud;
import com.rexeware.RexeWareClient;
import com.rexeware.modules.AttackAura;
import com.rexeware.modules.ElytraHelper;
import com.rexeware.modules.ElytraTarget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/** RexeWare ClickGUI. R ile acilir ve kapanir. Sol tik: ac/kapat, sag tik: ayarlar. */
public final class ClickGuiScreen extends Screen {
    private static final int ACCENT_A = 0xFF8B5CF6;
    private static final int ACCENT_B = 0xFF22D3EE;
    private static final int HEADER = 24;
    private static final int ROW = 20;
    private static final int GAP = 12;

    private static List<Panel> panels;

    private long lastNanos = System.nanoTime();
    private Panel draggingPanel;
    private Setting.Slider draggingSlider;

    public ClickGuiScreen() {
        super(Text.literal("RexeWare"));
        if (panels == null) {
            panels = buildPanels();
        }
    }

    private static List<Panel> buildPanels() {
        List<Panel> list = new ArrayList<>();

        Panel combat = new Panel("COMBAT");
        combat.modules.add(new ModuleEntry("AttackAura", () -> AttackAura.enabled, v -> AttackAura.enabled = v)
                .add(new Setting.Slider("Range", 1.0, 6.0, 0.1, 1, () -> AttackAura.range, v -> AttackAura.range = v))
                .add(new Setting.Slider("Rotation Speed", 5.0, 180.0, 1.0, 0, () -> AttackAura.rotationSpeed, v -> AttackAura.rotationSpeed = (float) v))
                .add(new Setting.Bool("Rotate", () -> AttackAura.rotate, v -> AttackAura.rotate = v))
                .add(new Setting.Bool("Players Only", () -> AttackAura.playersOnly, v -> AttackAura.playersOnly = v)));
        list.add(combat);

        Panel elytra = new Panel("ELYTRA");
        elytra.modules.add(new ModuleEntry("ElytraTarget", () -> ElytraTarget.enabled, v -> ElytraTarget.enabled = v)
                .add(new Setting.Slider("Range", 8.0, 128.0, 1.0, 0, () -> ElytraTarget.range, v -> ElytraTarget.range = v))
                .add(new Setting.Slider("Rotation Speed", 1.0, 60.0, 1.0, 0, () -> ElytraTarget.rotationSpeed, v -> ElytraTarget.rotationSpeed = (float) v))
                .add(new Setting.Slider("Predict Ticks", 0.0, 10.0, 0.5, 1, () -> ElytraTarget.predictTicks, v -> ElytraTarget.predictTicks = v))
                .add(new Setting.Bool("Players Only", () -> ElytraTarget.playersOnly, v -> ElytraTarget.playersOnly = v)));
        elytra.modules.add(new ModuleEntry("ElytraHelper", () -> ElytraHelper.enabled, v -> ElytraHelper.enabled = v)
                .add(new Setting.Bool("Auto Takeoff", () -> ElytraHelper.autoTakeoff, v -> ElytraHelper.autoTakeoff = v))
                .add(new Setting.Bool("Auto Firework", () -> ElytraHelper.autoFirework, v -> ElytraHelper.autoFirework = v))
                .add(new Setting.Slider("Min Speed", 0.2, 2.0, 0.05, 2, () -> ElytraHelper.minSpeed, v -> ElytraHelper.minSpeed = v))
                .add(new Setting.Slider("Firework Delay", 5.0, 100.0, 1.0, 0, () -> ElytraHelper.fireworkDelay, v -> ElytraHelper.fireworkDelay = (int) Math.round(v))));
        list.add(elytra);

        Panel visual = new Panel("VISUAL");
        visual.modules.add(new ModuleEntry("HUD", () -> Hud.enabled, v -> Hud.enabled = v)
                .add(new Setting.Bool("Watermark", () -> Hud.watermark, v -> Hud.watermark = v))
                .add(new Setting.Bool("Module List", () -> Hud.moduleList, v -> Hud.moduleList = v))
                .add(new Setting.Bool("Coordinates", () -> Hud.coords, v -> Hud.coords = v)));
        list.add(visual);

        return list;
    }

    @Override
    protected void init() {
        int n = panels.size();
        int perRow = Math.max(1, (this.width - 16 + GAP) / (Panel.WIDTH + GAP));
        for (int i = 0; i < n; i++) {
            Panel p = panels.get(i);
            if (p.x != Integer.MIN_VALUE) {
                continue;
            }
            int row = i / perRow;
            int col = i % perRow;
            int inRow = Math.min(perRow, n - row * perRow);
            int startX = (this.width - (inRow * Panel.WIDTH + (inRow - 1) * GAP)) / 2;
            p.x = startX + col * (Panel.WIDTH + GAP);
            p.y = 40 + row * 140;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ---------------------------------------------------------------- cizim

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
        lastNanos = now;
        float k = Math.min(1f, dt * 16f);

        ctx.fillGradient(0, 0, this.width, this.height, 0x95000000, 0x4010081F);

        Text brand = Text.literal("RexeWare").formatted(Formatting.BOLD);
        ctx.drawText(this.textRenderer, brand, (this.width - this.textRenderer.getWidth(brand)) / 2, 14, ACCENT_B, true);

        for (Panel p : panels) {
            renderPanel(ctx, p, mouseX, mouseY, k);
        }

        String hint = "LMB toggle   |   RMB settings   |   R close";
        ctx.drawText(this.textRenderer, hint, (this.width - this.textRenderer.getWidth(hint)) / 2, this.height - 16, 0xFF8A8A99, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, Panel p, int mx, int my, float k) {
        int x = p.x;
        int y = p.y;
        int w = Panel.WIDTH;

        int bodyH = 0;
        for (ModuleEntry m : p.modules) {
            m.toggleAnim += ((m.getter.getAsBoolean() ? 1f : 0f) - m.toggleAnim) * k;
            m.expandAnim += ((m.expanded ? 1f : 0f) - m.expandAnim) * k;
            bodyH += ROW + Math.round(settingsHeight(m) * m.expandAnim);
        }
        int total = HEADER + bodyH + 5;
        p.totalH = total;

        roundedRect(ctx, x - 1, y - 1, w + 2, total + 2, 5, 0x668B5CF6);
        roundedRect(ctx, x, y, w, total, 4, 0xF0101014);
        roundedRect(ctx, x, y, w, HEADER, 4, 0xFF18181F);
        ctx.fill(x, y + HEADER - 4, x + w, y + HEADER, 0xFF18181F);
        hGradient(ctx, x + 4, y + HEADER - 2, w - 8, 2, ACCENT_A, ACCENT_B);

        Text title = Text.literal(p.title).formatted(Formatting.BOLD);
        ctx.drawText(this.textRenderer, title, x + (w - this.textRenderer.getWidth(title)) / 2, y + 8, 0xFFFFFFFF, true);

        int cy = y + HEADER + 2;
        for (ModuleEntry m : p.modules) {
            m.rx = x + 3;
            m.ry = cy;
            m.rw = w - 6;
            m.rh = ROW - 2;

            boolean hover = inside(mx, my, m.rx, m.ry, m.rw, m.rh);
            int base = hover ? 0xFF22222B : 0xFF18181E;
            int bg = lerpColor(base, 0xFF3A2A6E, m.toggleAnim);
            roundedRect(ctx, m.rx, m.ry, m.rw, m.rh, 3, bg);
            if (m.toggleAnim > 0.02f) {
                int bar = lerpColor(0x008B5CF6, ACCENT_B, m.toggleAnim);
                ctx.fill(m.rx, m.ry + 4, m.rx + 2, m.ry + m.rh - 4, bar);
            }
            int textColor = lerpColor(0xFF9A9AA8, 0xFFFFFFFF, m.toggleAnim);
            ctx.drawText(this.textRenderer, m.name, m.rx + 7, m.ry + 5, textColor, false);
            ctx.drawText(this.textRenderer, m.expanded ? "v" : ">", m.rx + m.rw - 10, m.ry + 5, 0xFF6E6E7C, false);

            int full = settingsHeight(m);
            int visH = Math.round(full * m.expandAnim);
            m.sTop = cy + ROW;
            m.sVisH = visH;
            if (visH > 0) {
                ctx.enableScissor(x + 3, m.sTop, x + w - 3, m.sTop + visH);
                int sy = m.sTop + 2;
                for (Setting s : m.settings) {
                    s.rx = x + 8;
                    s.ry = sy;
                    s.rw = w - 16;
                    s.rh = s.height();
                    drawSetting(ctx, s, k);
                    sy += s.height();
                }
                ctx.disableScissor();
            }
            cy += ROW + visH;
        }
    }

    private void drawSetting(DrawContext ctx, Setting s, float k) {
        if (s instanceof Setting.Bool b) {
            boolean on = b.getter.getAsBoolean();
            if (b.anim < 0f) {
                b.anim = on ? 1f : 0f;
            }
            b.anim += ((on ? 1f : 0f) - b.anim) * k;
            ctx.drawText(this.textRenderer, b.name, s.rx, s.ry + 5, 0xFFB4B4C0, false);
            int sx = s.rx + s.rw - 22;
            roundedRect(ctx, sx, s.ry + 4, 22, 11, 5, lerpColor(0xFF2A2A34, ACCENT_A, b.anim));
            int knobX = sx + 2 + Math.round(b.anim * 11f);
            roundedRect(ctx, knobX, s.ry + 6, 7, 7, 3, 0xFFFFFFFF);
        } else if (s instanceof Setting.Slider sl) {
            ctx.drawText(this.textRenderer, sl.name, s.rx, s.ry + 2, 0xFFB4B4C0, false);
            String value = sl.display();
            ctx.drawText(this.textRenderer, value, s.rx + s.rw - this.textRenderer.getWidth(value), s.ry + 2, 0xFFFFFFFF, false);
            int trackY = s.ry + 14;
            roundedRect(ctx, s.rx, trackY, s.rw, 4, 2, 0xFF2A2A34);
            int fillW = Math.max(4, (int) Math.round(sl.fraction() * s.rw));
            roundedRect(ctx, s.rx, trackY, fillW, 4, 2, ACCENT_A);
            roundedRect(ctx, s.rx + fillW - 3, trackY - 2, 6, 8, 2, 0xFFFFFFFF);
        }
    }

    private static int settingsHeight(ModuleEntry m) {
        if (m.settings.isEmpty()) {
            return 0;
        }
        int h = 4;
        for (Setting s : m.settings) {
            h += s.height();
        }
        return h;
    }

    // ---------------------------------------------------------------- girdi

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel p = panels.get(i);
            if (!inside(mx, my, p.x, p.y, Panel.WIDTH, p.totalH)) {
                continue;
            }
            if (inside(mx, my, p.x, p.y, Panel.WIDTH, HEADER)) {
                if (button == 0) {
                    draggingPanel = p;
                    p.dragOffX = (int) mx - p.x;
                    p.dragOffY = (int) my - p.y;
                    panels.remove(i);
                    panels.add(p);
                }
                return true;
            }
            for (ModuleEntry m : p.modules) {
                if (inside(mx, my, m.rx, m.ry, m.rw, m.rh)) {
                    if (button == 0) {
                        m.setter.accept(!m.getter.getAsBoolean());
                    } else if (button == 1 && !m.settings.isEmpty()) {
                        m.expanded = !m.expanded;
                    }
                    return true;
                }
                if (m.sVisH > 0 && my >= m.sTop && my < m.sTop + m.sVisH) {
                    for (Setting s : m.settings) {
                        if (!inside(mx, my, s.rx, s.ry, s.rw, s.rh)) {
                            continue;
                        }
                        if (button == 0) {
                            if (s instanceof Setting.Bool b) {
                                b.toggle();
                            } else if (s instanceof Setting.Slider sl) {
                                draggingSlider = sl;
                                sl.setFromMouse(mx);
                            }
                        }
                        return true;
                    }
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingPanel != null) {
            draggingPanel.x = Math.max(0, Math.min(this.width - Panel.WIDTH, (int) mx - draggingPanel.dragOffX));
            draggingPanel.y = Math.max(0, Math.min(this.height - HEADER, (int) my - draggingPanel.dragOffY));
            return true;
        }
        if (draggingSlider != null) {
            draggingSlider.setFromMouse(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingPanel = null;
        draggingSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (RexeWareClient.openGuiKey != null && RexeWareClient.openGuiKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ---------------------------------------------------------------- yardimcilar

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void roundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            ctx.fill(x, y, x + w, y + h, color);
            return;
        }
        ctx.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            ctx.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
    }

    private static void hGradient(DrawContext ctx, int x, int y, int w, int h, int from, int to) {
        for (int i = 0; i < w; i += 2) {
            int c = lerpColor(from, to, w <= 1 ? 0f : i / (float) (w - 1));
            ctx.fill(x + i, y, Math.min(x + i + 2, x + w), y + h, c);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 255, ar = (a >>> 16) & 255, ag = (a >>> 8) & 255, ab = a & 255;
        int ba = (b >>> 24) & 255, br = (b >>> 16) & 255, bg = (b >>> 8) & 255, bb = b & 255;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    private static final class Panel {
        static final int WIDTH = 140;
        final String title;
        final List<ModuleEntry> modules = new ArrayList<>();
        int x = Integer.MIN_VALUE;
        int y = 40;
        int totalH = HEADER;
        int dragOffX;
        int dragOffY;

        Panel(String title) {
            this.title = title;
        }
    }
}
