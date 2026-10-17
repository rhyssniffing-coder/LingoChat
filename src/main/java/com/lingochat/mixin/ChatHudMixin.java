package com.lingochat.mixin;

import com.lingochat.MessageStore;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@ModifyVariable(
		method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
		at = @At("HEAD"),
		argsOnly = true,
		index = 1
	)
	private Text lingochat$addTranslateLink(Text message) {
		return MessageStore.wrap(message);
	}

	@Inject(method = "clear(Z)V", at = @At("TAIL"))
	private void lingochat$onClear(boolean clearHistory, CallbackInfo ci) {
		MessageStore.clear();
	}
}
