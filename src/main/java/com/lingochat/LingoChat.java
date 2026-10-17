package com.lingochat;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class LingoChat implements ClientModInitializer {
	public static final String MOD_ID = "lingochat";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static KeyBinding settingsKey;
	// set while we're sending a translated version ourselves, so the event doesn't fire twice
	private static boolean resending;

	@Override
	public void onInitializeClient() {
		Config.load();

		settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.lingochat.settings",
			GLFW.GLFW_KEY_U,
			KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (settingsKey.wasPressed()) {
				client.setScreen(new SettingsScreen());
			}
		});

		ClientSendMessageEvents.ALLOW_CHAT.register(LingoChat::onSendChat);

		LOGGER.info("LingoChat loaded");
	}

	private static boolean onSendChat(String message) {
		if (resending) {
			return true;
		}

		Config config = Config.get();
		if (!config.autoTranslateOutgoing || message.isBlank() || message.startsWith("/")) {
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		Translate.request(message, config.outgoingLanguage).whenComplete((result, error) -> client.execute(() -> {
			String out = message;
			if (error == null && result != null) {
				out = truncate(result.text());
			} else {
				LOGGER.warn("couldn't translate outgoing message", error);
			}

			if (client.player == null || client.player.networkHandler == null) {
				return;
			}

			resending = true;
			try {
				client.player.networkHandler.sendChatMessage(out);
			} finally {
				resending = false;
			}

			if (error != null || result == null) {
				client.inGameHud.setOverlayMessage(Text.literal("LingoChat: translation failed, sent as you typed it"), false);
			}
		}));

		return false;
	}

	// the server disconnects you for chat packets over 256 chars, and translated
	// text is often longer than what was typed
	private static String truncate(String text) {
		if (text.length() <= 256) {
			return text;
		}

		int end = 256;
		if (Character.isLowSurrogate(text.charAt(end - 1))) {
			end--;
		}
		return text.substring(0, end);
	}
}
