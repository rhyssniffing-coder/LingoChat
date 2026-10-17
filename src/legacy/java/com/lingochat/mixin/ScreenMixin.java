package com.lingochat.mixin;

import com.lingochat.MessageStore;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
	private static final String PREFIX = "/__lingochat_";

	@Inject(
		method = "handleTextClick(Lnet/minecraft/text/Style;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void lingochat$handleMarker(Style style, CallbackInfoReturnable<Boolean> cir) {
		if (style == null) {
			return;
		}

		ClickEvent event = style.getClickEvent();
		if (event == null || event.getAction() != ClickEvent.Action.RUN_COMMAND) {
			return;
		}

		String command = event.getValue();
		if (command == null || !command.startsWith(PREFIX)) {
			return;
		}

		// it's ours (or a malformed version of ours) - never let it reach the server
		cir.setReturnValue(true);

		if (command.length() <= PREFIX.length()) {
			return;
		}

		char action = command.charAt(PREFIX.length());
		try {
			long id = Long.parseLong(command.substring(PREFIX.length() + 1));
			if (action == 't') {
				MessageStore.handleTranslate(id);
			} else if (action == 'o') {
				MessageStore.handleToggle(id);
			}
		} catch (NumberFormatException ignored) {
		}
	}
}
