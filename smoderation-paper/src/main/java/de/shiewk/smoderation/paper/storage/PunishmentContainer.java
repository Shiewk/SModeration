package de.shiewk.smoderation.paper.storage;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.punishments.Punishment;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PunishmentContainer {

    private final CopyOnWriteArrayList<Punishment> punishments = new CopyOnWriteArrayList<>();

    public PunishmentContainer(){}

    public void add(Punishment punishment){
        punishments.add(punishment);
    }

    public @Nullable Punishment remove(int index){
        return punishments.remove(index);
    }

    public void remove(Punishment punishment){
        punishments.remove(punishment);
    }

    public @Nullable Punishment find(Predicate<Punishment> predicate){
        for (Punishment punishment : new CopyOnWriteArrayList<>(punishments)) {
            if (predicate.test(punishment)){
                return punishment;
            }
        }
        return null;
    }

    public @NotNull List<Punishment> findAll(Predicate<Punishment> predicate){
        List<Punishment> found = new ArrayList<>();
        for (Punishment punishment : new CopyOnWriteArrayList<>(punishments)) {
            if (predicate.test(punishment)){
                found.add(punishment);
            }
        }
        return found;
    }

    public List<CommandSender> collectBroadcastTargets(){
        ArrayList<CommandSender> senders = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("smod.notifications")){
                senders.add(onlinePlayer);
            }
        }
        senders.add(Bukkit.getConsoleSender());
        return Collections.unmodifiableList(senders);
    }

    public @Nullable Punishment findByTimestamp(long timestamp){
        return find(punishment -> punishment.time == timestamp);
    }

    public ArrayList<Punishment> copy() {
        return new ArrayList<>(punishments);
    }

    public void load(File file){
        final ComponentLogger logger = SModerationPaper.LOGGER;
        try {
            logger.info("Loading from {}", file.getPath());
            if (!file.isFile()){
                logger.warn("The file does not exist.");
            } else {
                try (FileInputStream fin = new FileInputStream(file)){
                    GZIPInputStream gzin = new GZIPInputStream(fin);
                    while (gzin.available() > 0){
                        add(Punishment.load(gzin));
                    }
                }
                logger.info("Successfully loaded {} items.", punishments.size());
            }
        } catch (EOFException e) {
            logger.error("The file was not correctly saved, {} items could be recovered!", this.punishments.size());
        } catch (IOException e){
            logger.error("An error occurred while loading: {}", e.toString());
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                logger.error(stackTraceElement.toString());
            }
        }
    }

    public void save(File file) {
        final ComponentLogger logger = SModerationPaper.LOGGER;
        try {
            logger.info("Saving to {}", file.getPath());
            if (!file.isFile()){
                file.mkdirs();
                file.delete();
                file.createNewFile();
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                GZIPOutputStream gzout = new GZIPOutputStream(outputStream);
                for (Punishment punishment : copy()) {
                    punishment.writeBytes(gzout);
                }
                gzout.close();
            }
            logger.info("Successfully saved.");
        } catch (IOException e){
            logger.error("An error occurred while saving: {}", e.toString());
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                logger.error(stackTraceElement.toString());
            }
        }
    }
}
