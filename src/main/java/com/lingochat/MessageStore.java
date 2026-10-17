package com.lingochat;

import com.lingochat.mixin.ChatHudAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MessageStore {
	public static final Identifier TRANSLATE_EVENT = Identifier.of(LingoChat.MOD_ID, "translate");
	public static final Identifier TOGGLE_EVENT = Identifier.of(LingoChat.MOD_ID, "toggle");

	private static final int MAX_TRACKED = 256;
	private static final LinkedHashMap<Long, Entry> ENTRIES = new LinkedHashMap<>();
	private static final IdentityHashMap<Text, Entry> BY_CONTENT = new IdentityHashMap<>();
	private static long nextId = 1;

	private MessageStore() {
	}

	private static final class Entry {
		final long id;
		final Text original;
		Text untranslatedContent;
		String translatedText;
		Text translatedContent;
		Text content;
		boolean showingTranslated;
		boolean busy;

		Entry(long id, Text original) {
			this.id = id;
			this.original = original;
		}
	}

	/**
	 * Adds the little "translate" link under an incoming message.
	 * Called from a mixin right before the chat hud stores the message.
	 */
	public static Text wrap(Text message) {
		if (message.getString().isBlank() || BY_CONTENT.containsKey(message)) {
			return message;
		}

		Entry entry = new Entry(nextId++, message);
		entry.untranslatedContent = buildUntranslated(entry);
		entry.content = entry.untranslatedContent;
		track(entry);

		if (Config.get().autoTranslateIncoming) {
			startTranslate(entry, false);
		}

		return entry.untranslatedContent;
	}

	public static void clear() {
		ENTRIES.clear();
		BY_CONTENT.clear();
	}

	/**
	 * Handles clicks on our custom click events. Returns true if the event was ours.
	 */
	public static boolean handleClick(ClickEvent.Custom custom) {
		if (!custom.id().equals(TRANSLATE_EVENT) && !custom.id().equals(TOGGLE_EVENT)) {
			return false;
		}

		long id = payloadId(custom);
		Entry entry = id >= 0 ? ENTRIES.get(id) : null;
		if (entry != null) {
			if (custom.id().equals(TRANSLATE_EVENT)) {
				startTranslate(entry, true);
			} else {
				toggle(entry);
			}
		}

		// even if the entry is gone, it was still our event - don't send a packet about it
		return true;
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

	private static Text buildUntranslated(Entry entry) {
		Style link = Style.EMPTY
			.withColor(Formatting.GRAY)
			.withItalic(true)
			.withClickEvent(new ClickEvent.Custom(TRANSLATE_EVENT, Optional.of((NbtElement) NbtString.of(Long.toString(entry.id)))))
			.withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to translate this message")));
		return entry.original.copy().append(Text.literal("\ntranslate").setStyle(link));
	}

	private static Text buildTranslated(Entry entry) {
		Style link = Style.EMPTY
			.withColor(Formatting.GRAY)
			.withItalic(true)
			.withClickEvent(new ClickEvent.Custom(TOGGLE_EVENT, Optional.of((NbtElement) NbtString.of(Long.toString(entry.id)))))
			.withHoverEvent(new HoverEvent.ShowText(Text.literal("Show the original message")));
		return Text.literal(entry.translatedText).append(Text.literal("\noriginal").setStyle(link));
	}

	private static Text buildProgress(Entry entry) {
		Style note = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
		return entry.original.copy().append(Text.literal("\ntranslating...").setStyle(note));
	}

	private static void startTranslate(Entry entry, boolean fromClick) {
		if (entry.busy || entry.showingTranslated) {
			return;
		}

		entry.busy = true;
		if (fromClick) {
			apply(entry, buildProgress(entry));
		}

		String targetLanguage = Config.get().incomingLanguage;
		MinecraftClient client = MinecraftClient.getInstance();
		Translate.request(entry.original.getString(), targetLanguage).whenComplete((result, error) -> client.execute(() -> {
			entry.busy = false;

			if (error != null || result == null) {
				LingoChat.LOGGER.warn("couldn't translate chat message", error);
				if (fromClick) {
					apply(entry, entry.untranslatedContent);
					client.inGameHud.setOverlayMessage(Text.literal("LingoChat: translation failed"), false);
				}
				return;
			}

			entry.translatedText = result.text();
			entry.translatedContent = buildTranslated(entry);
			entry.showingTranslated = true;
			apply(entry, entry.translatedContent);
		}));
	}

	private static void toggle(Entry entry) {
		if (entry.busy) {
			return;
		}

		if (entry.showingTranslated) {
			entry.showingTranslated = false;
			apply(entry, entry.untranslatedContent);
		} else if (entry.translatedContent != null) {
			entry.showingTranslated = true;
			apply(entry, entry.translatedContent);
		}
	}

	private static void apply(Entry entry, Text newContent) {
		Text previous = entry.content;
		entry.content = newContent;
		BY_CONTENT.remove(previous);
		BY_CONTENT.put(newContent, entry);

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.inGameHud == null) {
			return;
		}

		ChatHud chatHud = client.inGameHud.getChatHud();
		ChatHudAccess access = (ChatHudAccess) chatHud;
		List<ChatHudLine> messages = access.getMessages();

		boolean replaced = false;
		for (int i = 0; i < messages.size(); i++) {
			ChatHudLine line = messages.get(i);
			if (line.content() == previous) {
				messages.set(i, new ChatHudLine(line.creationTick(), newContent, line.signature(), line.indicator()));
				replaced = true;
				break;
			}
		}

		if (replaced) {
			access.rebuildVisibleMessages();
		}
	}

	private static void track(Entry entry) {
		ENTRIES.put(entry.id, entry);
		BY_CONTENT.put(entry.untranslatedContent, entry);

		Iterator<Map.Entry<Long, Entry>> iterator = ENTRIES.entrySet().iterator();
		while (ENTRIES.size() > MAX_TRACKED && iterator.hasNext()) {
			Map.Entry<Long, Entry> eldest = iterator.next();
			Entry old = eldest.getValue();
			BY_CONTENT.remove(old.content);
			BY_CONTENT.remove(old.untranslatedContent);
			BY_CONTENT.remove(old.translatedContent);
			iterator.remove();
		}
	}
}
