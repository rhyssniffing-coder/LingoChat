package com.lingochat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Translate {
	public record Result(String text, String sourceLanguage) {
	}

	private static final HttpClient HTTP = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.build();

	private static final ExecutorService POOL = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "lingochat-translate");
		thread.setDaemon(true);
		return thread;
	});

	// people say the same short stuff over and over, no point hitting the API every time
	private static final int MAX_CACHED = 256;
	private static final Map<String, Result> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, Result>(64, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Result> eldest) {
			return this.size() > MAX_CACHED;
		}
	});

	private Translate() {
	}

	public static CompletableFuture<Result> request(String text, String targetLanguage) {
		String key = targetLanguage + ' ' + text;
		Result cached = CACHE.get(key);
		if (cached != null) {
			return CompletableFuture.completedFuture(cached);
		}

		CompletableFuture<Result> future = new CompletableFuture<>();
		POOL.execute(() -> {
			try {
				Result result = fetch(text, targetLanguage);
				CACHE.put(key, result);
				future.complete(result);
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}

	private static Result fetch(String text, String targetLanguage) throws Exception {
		String url = "https://translate.googleapis.com/translate_a/single"
			+ "?client=gtx&sl=auto&dt=t"
			+ "&tl=" + encode(targetLanguage)
			+ "&q=" + encode(text);

		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
			.timeout(Duration.ofSeconds(10))
			.header("User-Agent", "Mozilla/5.0")
			.GET()
			.build();

		HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new IllegalStateException("translate endpoint returned " + response.statusCode());
		}

		JsonArray root = JsonParser.parseString(response.body()).getAsJsonArray();
		StringBuilder translated = new StringBuilder();
		for (JsonElement segment : root.get(0).getAsJsonArray()) {
			translated.append(segment.getAsJsonArray().get(0).getAsString());
		}

		String source = "";
		if (root.size() > 2 && root.get(2).isJsonPrimitive()) {
			source = root.get(2).getAsString();
		}

		return new Result(translated.toString(), source);
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
