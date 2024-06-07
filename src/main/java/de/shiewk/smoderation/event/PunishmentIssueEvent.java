package de.shiewk.smoderation.event;

import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.storage.PunishmentContainer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PunishmentIssueEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final Punishment punishment;
    private final PunishmentContainer container;

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
}
