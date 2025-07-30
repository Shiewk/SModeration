package de.shiewk.smoderation.paper.translation;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.shiewk.smoderation.paper.SModerationPaper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
}
