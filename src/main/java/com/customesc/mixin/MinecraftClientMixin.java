package com.customesc.mixin;

import com.customesc.FancyPauseScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen customesc$replacePauseScreen(Screen screen) {
        if (screen != null && screen.getClass() == GameMenuScreen.class) {
            return new FancyPauseScreen();
        }
        return screen;
    }
}
