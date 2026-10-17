package com.lingochat.mixin;

import com.lingochat.MessageStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(
		method = "handleClickEvent(Lnet/minecraft/text/ClickEvent;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/Screen;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void lingochat$handleCustomClick(ClickEvent event, MinecraftClient client, Screen screenAfterRun, CallbackInfo ci) {
		if (event instanceof ClickEvent.Custom custom && MessageStore.handleClick(custom)) {
			ci.cancel();
		}
	}
}
