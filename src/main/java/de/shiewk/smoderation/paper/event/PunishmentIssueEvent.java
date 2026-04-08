package de.shiewk.smoderation.paper.event;

import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PunishmentIssueEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final Punishment punishment;
    private final PunishmentManager manager;
    private boolean cancelled;

    public PunishmentIssueEvent(Punishment punishment, PunishmentManager manager) {
        this.punishment = punishment;
        this.manager = manager;
    }

    public Punishment getPunishment() {
        return punishment;
    }

    public PunishmentManager getPunishmentManager() {
        return manager;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
