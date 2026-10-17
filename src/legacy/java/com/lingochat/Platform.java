package com.lingochat;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class Platform {
	private Platform() {
	}

	public static ClickEvent translateEvent(long id) {
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/__lingochat_t_" + id);
	}

	public static ClickEvent toggleEvent(long id) {
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/__lingochat_o_" + id);
	}

	public static HoverEvent hover(String text) {
		return new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(text));
	}

	public static boolean isShiftDown() {
		return net.minecraft.client.gui.screen.Screen.hasShiftDown();
	}

	public static KeyBinding createSettingsKey() {
		return new KeyBinding("key.lingochat.settings", GLFW.GLFW_KEY_U, "key.categories.lingochat");
	}
}
