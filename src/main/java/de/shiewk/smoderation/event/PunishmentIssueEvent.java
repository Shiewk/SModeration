package de.shiewk.smoderation.event;

import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.storage.PunishmentContainer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PunishmentIssueEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final Punishment punishment;
    private final PunishmentContainer container;
    private boolean cancelled;

    public PunishmentIssueEvent(Punishment punishment, PunishmentContainer container) {
        this.punishment = punishment;
        this.container = container;
    }

    public Punishment getPunishment() {
        return punishment;
    }

    public PunishmentContainer getContainer() {
        return container;
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
