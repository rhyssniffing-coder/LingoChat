package com.lingochat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("lingochat.json");

	private static Config instance = new Config();

	public String incomingLanguage = "en";
	public boolean autoTranslateIncoming = false;
	public boolean autoTranslateOutgoing = false;
	public String outgoingLanguage = "en";

	public static Config get() {
		return instance;
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				Config loaded = GSON.fromJson(Files.readString(FILE), Config.class);
				if (loaded != null) {
					instance = loaded;
				}
			}
		} catch (Exception e) {
			LingoChat.LOGGER.warn("couldn't read lingochat.json, using defaults", e);
		}

		// in case someone hand-edits a bogus code into the file
		if (Langs.byCode(instance.incomingLanguage) == null) {
			instance.incomingLanguage = "en";
		}
		if (Langs.byCode(instance.outgoingLanguage) == null) {
			instance.outgoingLanguage = "en";
		}
	}

	public static void save() {
		try {
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (Exception e) {
			LingoChat.LOGGER.warn("couldn't save lingochat.json", e);
		}
	}
}
