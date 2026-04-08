package de.shiewk.smoderation.paper.punishments;

import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.event.PunishmentIssueEvent;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.shiewk.smoderation.paper.SModerationPaper.LOGGER;

public final class PunishmentManager {

    private static final Logger log = LoggerFactory.getLogger(PunishmentManager.class);
    private final Object2ObjectArrayMap<String, PunishmentFactory<?>> typeRegistry = new Object2ObjectArrayMap<>(1);
    private final Object ioLock = new Object();
    private final Path dataDir;

    public PunishmentManager(Path dataDir) {
        this.dataDir = dataDir;
    }

    private Path getTargetFile(UUID targetUUID){
        return dataDir.resolve(targetUUID.toString().replace("-", ""));
    }

    public boolean tryIssue(Punishment punishment) {
        try {
            PunishmentIssueEvent event = new PunishmentIssueEvent(punishment, this);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()){
                this.appendToSave(punishment);
                punishment.processIssue();
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Punishment> byTargetUUID(UUID target) {
        synchronized (ioLock) {
            Path file = getTargetFile(target);
            if (!Files.exists(file)) {
                return List.of();
            }
            try (
                    BufferedReader reader = Files.newBufferedReader(file);
                    JsonReader json = new JsonReader(reader)
            ) {
                json.setStrictness(Strictness.LENIENT);
                Object2ObjectArrayMap<UUID, Punishment> punishments = new Object2ObjectArrayMap<>(0);
                while (json.hasNext()){
                    JsonObject obj = SModerationPaper.gson.fromJson(json, JsonObject.class);
                    try {
                        SerializationHelper helper = new SerializationHelper(obj);
                        String type = helper.getString("type");
                        PunishmentFactory<?> factory = typeRegistry.get(type);
                        if (factory != null){
                            Punishment punishment = factory.deserialize(helper);
                            if (!punishment.getTargetID().equals(target)){
                                LOGGER.warn("Punishment saved in file for {} has incorrect target UUID {}", target, punishment.getTargetID());
                            } else {
                                punishments.put(punishment.getID(), punishment);
                            }
                        } else {
                            LOGGER.warn("Unknown punishment type '{}'! Can not load.", type);
                            LOGGER.warn("Please check your configuration, or see file {} to remove corrupted data.", file);
                            LOGGER.warn(obj.toString());
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Could not deserialize punishment!", e);
                        LOGGER.warn("Please check file {} for corrupted data, or remove the corresponding line.", file);
                        LOGGER.warn(obj.toString());
                    }
                }
                return List.copyOf(punishments.values());
            } catch (IOException e){
                throw new RuntimeException("Error while reading punishment file " + file, e);
            }
        }
    }

    public List<Punishment> byTargetUUID(UUID target, Predicate<Punishment> filter) {
        return byTargetUUID(target).stream().filter(filter).toList();
    }

    public <T extends Punishment> void registerType(String type, PunishmentFactory<T> factory){
        if (typeRegistry.containsKey(type)) {
            throw new IllegalStateException("Punishment type already registered: " + type);
        }
        typeRegistry.put(type, factory);
    }

    public List<String> getRegisteredTypes(){
        return List.copyOf(typeRegistry.keySet());
    }

    private void appendToSave(Punishment punishment) throws IOException {
        synchronized (ioLock) {
            Path file = getTargetFile(punishment.getTargetID());
            if (!Files.exists(file)) {
                Files.createDirectories(dataDir);
                Files.createFile(file);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND)) {
                JsonObject json = new JsonObject();
                punishment.addSerializableProperties(new SerializationHelper(json));
                SModerationPaper.gson.toJson(json, writer);
                writer.append('\n');
            }
        }
    }

    public @NotNull List<Punishment> getAll() throws IOException {
        ObjectArrayList<Punishment> punishments = new ObjectArrayList<>();
        synchronized (ioLock) {
            try (Stream<Path> stream = Files.list(dataDir)) {
                stream.forEach(file -> {
                    try {
                        String name = file.getFileName().toString();
                        UUID targetUUID = PlayerUtil.uuidFromString(name);
                        punishments.addAll(byTargetUUID(targetUUID));
                    } catch (Exception e) {
                        log.warn("Could not read punishment file {}", file, e);
                    }
                });
            }
        }
        return List.copyOf(punishments);
    }

    public List<Punishment> getAll(Predicate<Punishment> filter) throws IOException {
        return getAll().stream().filter(filter).toList();
    }

    public void cancel(TimedPunishment punishment, UUID canceller) {
        if (!punishment.isActive()){
            throw new IllegalStateException("This punishment is not active");
        }
        punishment.cancel(canceller);
        try {
            appendToSave(punishment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
