package com.customesc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class FancyButton extends PressableWidget {
    private final Runnable action;
    private final int accent;
    private final String index;
    private float hover = 0f;

    public FancyButton(int x, int y, int w, int h, Text label, int idx, int accent, Runnable action) {
        super(x, y, w, h, Text.literal(label.getString().toUpperCase()));
        this.action = action;
        this.accent = accent;
        this.index = String.format("%02d", idx);
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean hot = this.isHovered() || this.isFocused();
        hover = MathHelper.lerp(0.28f, hover, hot && active ? 1f : 0f);

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();

        // base plate
        ctx.fill(x, y, x + w, y + h, 0x66000000);
        // animated highlight sweep
        int sweep = (int) (w * hover);
        if (sweep > 0) {
            ctx.fill(x, y, x + sweep, y + h, (0x55 << 24) | (accent & 0xFFFFFF));
        }
        // left accent bar (grows on hover)
        int bar = 2 + Math.round(hover * 3);
        ctx.fill(x, y, x + bar, y + h, active ? accent : 0xFF555555);
        // bottom hairline
        ctx.fill(x, y + h - 1, x + w, y + h, 0x22FFFFFF);

        int textColor = !active ? 0xFF666666 : blend(0xFFB8C0CC, 0xFFFFFFFF, hover);
        int tx = x + 12 + Math.round(hover * 6);
        int ty = y + (h - 8) / 2;
        ctx.drawTextWithShadow(mc.textRenderer, getMessage(), tx, ty, textColor);

        // small index number on the right
        int idxColor = blend(0x55FFFFFF, accent, hover);
        ctx.drawText(mc.textRenderer, index, x + w - 6 - mc.textRenderer.getWidth(index), ty, idxColor, false);
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, aa = (a >>> 24);
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255, ba = (b >>> 24);
        int r = (int) MathHelper.lerp(t, ar, br);
        int g = (int) MathHelper.lerp(t, ag, bg);
        int bl = (int) MathHelper.lerp(t, ab, bb);
        int al = (int) MathHelper.lerp(t, aa, ba);
        return (al << 24) | (r << 16) | (g << 8) | bl;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public void playDownSound(net.minecraft.client.sound.SoundManager soundManager) {
        super.playDownSound(soundManager);
    }
}
