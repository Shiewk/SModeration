package de.shiewk.smoderation.storage;

import de.shiewk.smoderation.punishments.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

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
}
