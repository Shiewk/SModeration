package de.shiewk.smoderation.paper.punishments;

import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.translatable;

public enum PunishmentType {
    MUTE(translatable("smod.punishment.name.mute")),
    KICK(translatable("smod.punishment.name.kick")),
    BAN(translatable("smod.punishment.name.ban"));

    public final Component name;

    PunishmentType(Component name) {
        this.name = name;
    }
}
