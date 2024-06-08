package de.shiewk.smoderation.punishments;

public enum PunishmentType {
    MUTE("Mute"),
    KICK("Kick"),
    BAN("Ban");

    public final String name;

    PunishmentType(String name) {
        this.name = name;
    }
}
