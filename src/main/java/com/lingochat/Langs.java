package com.lingochat;

import java.util.ArrayList;
import java.util.List;

public final class Langs {
	public record Lang(String code, String name) {
	}

	public static final List<Lang> ALL = List.of(
		new Lang("en", "English"),
		new Lang("es", "Spanish"),
		new Lang("fr", "French"),
		new Lang("de", "German"),
		new Lang("it", "Italian"),
		new Lang("pt", "Portuguese"),
		new Lang("ru", "Russian"),
		new Lang("ja", "Japanese"),
		new Lang("ko", "Korean"),
		new Lang("zh-CN", "Chinese (Simplified)"),
		new Lang("zh-TW", "Chinese (Traditional)"),
		new Lang("ar", "Arabic"),
		new Lang("hi", "Hindi"),
		new Lang("tr", "Turkish"),
		new Lang("nl", "Dutch"),
		new Lang("pl", "Polish"),
		new Lang("sv", "Swedish"),
		new Lang("no", "Norwegian"),
		new Lang("da", "Danish"),
		new Lang("fi", "Finnish"),
		new Lang("el", "Greek"),
		new Lang("he", "Hebrew"),
		new Lang("th", "Thai"),
		new Lang("vi", "Vietnamese"),
		new Lang("id", "Indonesian"),
		new Lang("uk", "Ukrainian"),
		new Lang("cs", "Czech"),
		new Lang("ro", "Romanian"),
		new Lang("hu", "Hungarian"),
		new Lang("bg", "Bulgarian")
	);

	private Langs() {
	}

	public static Lang byCode(String code) {
		for (Lang lang : ALL) {
			if (lang.code().equals(code)) {
				return lang;
			}
		}
		return null;
	}

	public static String nameOf(String code) {
		Lang lang = byCode(code);
		return lang != null ? lang.name() : code;
	}

	public static String next(String code, boolean backwards) {
		List<String> codes = new ArrayList<>();
		for (Lang lang : ALL) {
			codes.add(lang.code());
		}

		int index = codes.indexOf(code);
		if (index < 0) {
			return codes.get(0);
		}

		int next = backwards ? index - 1 : index + 1;
		if (next < 0) {
			next = codes.size() - 1;
		} else if (next >= codes.size()) {
			next = 0;
		}
		return codes.get(next);
	}
}
