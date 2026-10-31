package com.customesc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class FancyPauseScreen extends Screen {
    // ---- tweak these to restyle everything ----
    private static final int ACCENT = 0xFF00E5FF;   // cyan
    private static final int DANGER = 0xFFFF4D5E;   // red for quit
    private static final int PANEL_W = 290;
    private static final int BTN_W = 200;
    private static final int BTN_H = 24;
    private static final int GAP = 6;
    // -------------------------------------------

    private long openTime;
    private final List<FancyButton> buttons = new ArrayList<>();
    private final List<Integer> baseX = new ArrayList<>();
    private int menuTop;

    public FancyPauseScreen() {
        super(Text.translatable("menu.game"));
    }

    @Override
    protected void init() {
        openTime = Util.getMeasuringTimeMs();
        buttons.clear();
        baseX.clear();
        MinecraftClient mc = this.client;

        List<Object[]> entries = new ArrayList<>();
        entries.add(new Object[]{Text.translatable("menu.returnToGame"), ACCENT, (Runnable) () -> {
            mc.setScreen(null);
            mc.mouse.lockCursor();
        }});
        entries.add(new Object[]{Text.translatable("gui.advancements"), ACCENT, (Runnable) () ->
                mc.setScreen(new AdvancementsScreen(mc.player.networkHandler.getAdvancementHandler()))});
        entries.add(new Object[]{Text.translatable("gui.stats"), ACCENT, (Runnable) () ->
                mc.setScreen(new StatsScreen(this, mc.player.getStatHandler()))});
        entries.add(new Object[]{Text.translatable("menu.options"), ACCENT, (Runnable) () ->
                mc.setScreen(new OptionsScreen(this, mc.options))});
        if (mc.isIntegratedServerRunning() && mc.getServer() != null && !mc.getServer().isRemote()) {
            entries.add(new Object[]{Text.translatable("menu.shareToLan"), ACCENT, (Runnable) () ->
                    mc.setScreen(new OpenToLanScreen(this))});
        }
        Text quitLabel = mc.isInSingleplayer()
                ? Text.translatable("menu.returnToMenu")
                : Text.translatable("menu.disconnect");
        entries.add(new Object[]{quitLabel, DANGER, null});

        int total = entries.size() * (BTN_H + GAP) - GAP;
        menuTop = (this.height - total) / 2 + 24;
        int x = 34;

        for (int i = 0; i < entries.size(); i++) {
            Object[] e = entries.get(i);
            int y = menuTop + i * (BTN_H + GAP);
            final FancyButton[] self = new FancyButton[1];
            Runnable action = (Runnable) e[2];
            if (action == null) {
                action = () -> {
                    self[0].active = false;
                    disconnect();
                };
            }
            FancyButton b = new FancyButton(x, y, BTN_W, BTN_H, (Text) e[0], i + 1, (Integer) e[1], action);
            self[0] = b;
            buttons.add(b);
            baseX.add(x);
            this.addDrawableChild(b);
        }
    }

    private void disconnect() {
        MinecraftClient mc = this.client;
        boolean single = mc.isInSingleplayer();
        boolean realms = mc.isConnectedToRealms();
        mc.world.disconnect();
        if (single) {
            mc.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        } else {
            mc.disconnect();
        }
        TitleScreen title = new TitleScreen();
        if (single) {
            mc.setScreen(title);
        } else if (realms) {
            mc.setScreen(new RealmsMainScreen(title));
        } else {
            mc.setScreen(new MultiplayerScreen(title));
        }
    }

    private static float easeOut(float t) {
        float k = 1f - MathHelper.clamp(t, 0f, 1f);
        return 1f - k * k * k;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = this.client;
        long now = Util.getMeasuringTimeMs();
        float open = easeOut((now - openTime) / 350f);

        // 1) background: dark vertical gradient
        ctx.fillGradient(0, 0, width, height,
                ((int) (0xA0 * open) << 24) | 0x05070D,
                ((int) (0xE6 * open) << 24) | 0x0B1226);

        // 2) left panel: horizontal fade
        for (int i = 0; i < PANEL_W; i++) {
            float a = 1f - i / (float) PANEL_W;
            int alpha = MathHelper.clamp((int) (a * a * 0xE0 * open), 0, 255);
            ctx.fill(i, 0, i + 1, height, (alpha << 24) | 0x04060C);
        }

        // 3) moving diagonal "scanline" shimmer (pure code texture)
        int shimmerX = (int) ((now / 12L) % (width + 200)) - 100;
        for (int i = 0; i < 60; i++) {
            int a = (int) (0x14 * (1f - i / 60f) * open);
            ctx.fill(shimmerX - i, 0, shimmerX - i + 1, height, (a << 24) | (ACCENT & 0xFFFFFF));
        }

        // 4) vertical accent line + title
        int lineTop = menuTop - 62;
        int lineBot = menuTop + buttons.size() * (BTN_H + GAP) - GAP + 10;
        ctx.fill(22, lineTop, 24, lineBot, ACCENT);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(34, lineTop + 2, 0);
        ctx.getMatrices().scale(2.5f, 2.5f, 1f);
        ctx.drawTextWithShadow(textRenderer, "PAUSED", 0, 0, 0xFFFFFFFF);
        ctx.getMatrices().pop();
        ctx.drawTextWithShadow(textRenderer, "// game menu", 36, lineTop + 28, 0xFF6C7A89);

        // 5) slide-in animation for buttons (staggered)
        for (int i = 0; i < buttons.size(); i++) {
            float t = easeOut((now - openTime - i * 45L) / 380f);
            int off = (int) ((1f - t) * -(BTN_W + 60));
            buttons.get(i).setX(baseX.get(i) + off);
            buttons.get(i).setAlpha(Math.max(t, 0.02f));
        }

        // 6) player card (bottom-left)
        if (mc.player != null) {
            int cy = height - 58;
            ctx.fill(34, cy, 34 + BTN_W, cy + 40, 0x66000000);
            ctx.fill(34, cy, 36, cy + 40, ACCENT);
            PlayerSkinDrawer.draw(ctx, mc.player.getSkinTexture(), 42, cy + 4, 32);
            ctx.drawTextWithShadow(textRenderer, mc.player.getName(), 82, cy + 6, 0xFFFFFFFF);

            String where = mc.isInSingleplayer()
                    ? "Singleplayer"
                    : (mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Multiplayer");
            ctx.drawText(textRenderer, where, 82, cy + 18, 0xFF8FA3B8, false);
            BlockPos p = mc.player.getBlockPos();
            ctx.drawText(textRenderer, p.getX() + ", " + p.getY() + ", " + p.getZ(), 82, cy + 29, 0xFF566677, false);
        }

        // 7) dimension tag (top-right)
        if (mc.world != null) {
            String dim = mc.world.getRegistryKey().getValue().getPath().toUpperCase().replace('_', ' ');
            int w = textRenderer.getWidth(dim);
            ctx.drawTextWithShadow(textRenderer, dim, width - w - 14, 12, ACCENT);
            ctx.fill(width - w - 18, 10, width - w - 16, 22, ACCENT);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
