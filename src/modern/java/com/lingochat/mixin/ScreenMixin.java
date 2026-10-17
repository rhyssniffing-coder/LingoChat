package com.lingochat.mixin;

import com.lingochat.MessageStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(
		method = "handleClickEvent(Lnet/minecraft/text/ClickEvent;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/Screen;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void lingochat$handleCustomClick(ClickEvent event, MinecraftClient client, Screen screenAfterRun, CallbackInfo ci) {
		if (!(event instanceof ClickEvent.Custom custom)) {
			return;
		}

		boolean isTranslate = custom.id().equals(MessageStore.TRANSLATE_EVENT);
		boolean isToggle = custom.id().equals(MessageStore.TOGGLE_EVENT);
		if (!isTranslate && !isToggle) {
			return;
		}

		// ours - cancel even if the id can't be parsed, so nothing weird reaches the server
		ci.cancel();

		long id = payloadId(custom);
		if (id < 0) {
			return;
		}

		if (isTranslate) {
			MessageStore.handleTranslate(id);
		} else {
			MessageStore.handleToggle(id);
		}
	}

	private static long payloadId(ClickEvent.Custom custom) {
		Optional<NbtElement> payload = custom.payload();
		if (payload.isPresent() && payload.get() instanceof NbtString string) {
			Optional<String> value = string.asString();
			if (value.isPresent()) {
				try {
					return Long.parseLong(value.get());
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return -1L;
	}
}
