package com.lingochat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public final class Platform {
	public static final Identifier TRANSLATE_EVENT = Identifier.of(LingoChat.MOD_ID, "translate");
	public static final Identifier TOGGLE_EVENT = Identifier.of(LingoChat.MOD_ID, "toggle");

	private Platform() {
	}

	public static ClickEvent translateEvent(long id) {
		return new ClickEvent.Custom(TRANSLATE_EVENT, Optional.of((NbtElement) NbtString.of(Long.toString(id))));
	}

	public static ClickEvent toggleEvent(long id) {
		return new ClickEvent.Custom(TOGGLE_EVENT, Optional.of((NbtElement) NbtString.of(Long.toString(id))));
	}

	public static HoverEvent hover(String text) {
		return new HoverEvent.ShowText(Text.literal(text));
	}

	public static boolean isShiftDown() {
		return MinecraftClient.getInstance().isShiftPressed();
	}

	public static KeyBinding createSettingsKey() {
		return new KeyBinding(
			"key.lingochat.settings",
			GLFW.GLFW_KEY_U,
			KeyBinding.Category.create(Identifier.of(LingoChat.MOD_ID, "main"))
		);
	}
}
