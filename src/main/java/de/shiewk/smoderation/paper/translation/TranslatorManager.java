package de.shiewk.smoderation.paper.translation;

import com.google.gson.FormattingStyle;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import de.shiewk.smoderation.paper.SModerationPaper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;

public class TranslatorManager {

    public final String resourcePath;
    public final Locale[] availableLocales;
    private final MiniMessageTranslationStore translationStore;

    public TranslatorManager(Key key, MiniMessage miniMessage, String resourcePath, Locale[] availableLocales) {
        this.resourcePath = resourcePath;
        this.availableLocales = availableLocales;
        this.translationStore = MiniMessageTranslationStore.create(key, miniMessage);
    }

    public void load(){
        for (Locale locale : availableLocales) {
            String s = locale.getLanguage() + "_" + locale.getCountry().toLowerCase();
            try (InputStream stream = SModerationPaper.class.getClassLoader().getResourceAsStream(resourcePath + s + ".json")) {
                if (stream == null) {
                    SModerationPaper.LOGGER.warn("Translations for {} not found or not accessible", locale);
                    continue;
                }

                Map<String, String> translationMap = SModerationPaper.gson.fromJson(new InputStreamReader(stream), new TypeToken<>(){});
                translationStore.registerAll(locale, translationMap);

            } catch (IOException | JsonSyntaxException | JsonIOException e) {
                SModerationPaper.LOGGER.warn("Failed to load translations for {}", locale, e);
            }
        }
        GlobalTranslator.translator().addSource(translationStore);
    }

    public void loadCustomMessages(Path customPath) {
        try {
            if (Files.notExists(customPath)) {
                Files.createDirectories(customPath.getParent());
                Files.write(customPath, "{}".getBytes(), StandardOpenOption.CREATE);
            }

            Map<String, String> predefinedMap;
            try (InputStream stream = SModerationPaper.class.getClassLoader().getResourceAsStream(resourcePath + "en_us.json")) {
                if (stream == null) {
                    SModerationPaper.LOGGER.warn("English (US) predefined translations not found or not accessible");
                    predefinedMap = Map.of();
                } else {
                    predefinedMap = SModerationPaper.gson.fromJson(new InputStreamReader(stream), new TypeToken<>(){});
                }
            }

            Map<String, String> translationMap;
            try (InputStream stream = Files.newInputStream(customPath)) {
                translationMap = SModerationPaper.gson.fromJson(new InputStreamReader(stream), new TypeToken<>(){});
            }

            boolean updated = false;
            for (Map.Entry<String, String> entry : predefinedMap.entrySet()) {
                if (!translationMap.containsKey(entry.getKey())) {
                    translationMap.put(entry.getKey(), entry.getValue());
                    updated = true;
                }
            }

            if (updated) {
                SModerationPaper.LOGGER.warn("Updating {} custom translations", translationMap.size());
                try (OutputStream stream = Files.newOutputStream(customPath, StandardOpenOption.TRUNCATE_EXISTING);
                     OutputStreamWriter writer = new OutputStreamWriter(stream);
                     JsonWriter jsonWriter = new JsonWriter(writer)) {
                    jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
                    SModerationPaper.gson.toJson(translationMap, translationMap.getClass(), jsonWriter);
                }
                SModerationPaper.LOGGER.info("Update successful");
            }

            translationStore.registerAll(Locale.forLanguageTag("en-US"), translationMap);
            SModerationPaper.LOGGER.info("Registered {} custom translations", translationMap.size());

        } catch (IOException e) {
            SModerationPaper.LOGGER.warn("Failed to load custom translations", e);
        }
        GlobalTranslator.translator().addSource(translationStore);
    }
}
