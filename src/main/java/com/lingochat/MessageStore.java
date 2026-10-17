package com.lingochat;

import com.lingochat.mixin.ChatHudAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		final String targetLanguageName;
		Text untranslatedContent;
		String translatedText;
		Text translatedContent;
		Text content;
		boolean showingTranslated;
		boolean busy;

		Entry(long id, Text original) {
			this.id = id;
			this.original = original;
			this.targetLanguageName = Langs.nameOf(Config.get().incomingLanguage);
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

	// called by the version-specific click mixins once they've recognized our marker
	public static void handleTranslate(long id) {
		Entry entry = ENTRIES.get(id);
		if (entry != null) {
			startTranslate(entry, true);
		}
	}

	public static void handleToggle(long id) {
		Entry entry = ENTRIES.get(id);
		if (entry != null) {
			toggle(entry);
		}
	}

	private static Text buildUntranslated(Entry entry) {
		Style link = Style.EMPTY
			.withColor(Formatting.GRAY)
			.withItalic(true)
			.withClickEvent(Platform.translateEvent(entry.id))
			.withHoverEvent(Platform.hover("Click to translate into " + entry.targetLanguageName));
		return entry.original.copy().append(Text.literal("\ntranslate").setStyle(link));
	}

	private static Text buildTranslated(Entry entry) {
		Style link = Style.EMPTY
			.withColor(Formatting.GRAY)
			.withItalic(true)
			.withClickEvent(Platform.toggleEvent(entry.id))
			.withHoverEvent(Platform.hover("Show the original message"));
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
