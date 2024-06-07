package de.shiewk.smoderation.storage;

import de.shiewk.smoderation.punishments.Punishment;
import org.jetbrains.annotations.Nullable;

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

    public boolean remove(Punishment punishment){
        return punishments.remove(punishment);
    }

    public @Nullable Punishment find(Predicate<Punishment> predicate){
        for (Punishment punishment : new CopyOnWriteArrayList<>(punishments)) {
            if (predicate.test(punishment)){
                return punishment;
            }
        }
        return null;
    }

    public @Nullable Punishment findByTimestamp(long timestamp){
        return find(punishment -> punishment.time == timestamp);
    }
}
