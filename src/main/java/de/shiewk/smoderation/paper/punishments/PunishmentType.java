package de.shiewk.smoderation.paper.punishments;

import net.kyori.adventure.text.Component;

public interface PunishmentType<T extends Punishment> {

    String getTypeId();

    Component getDisplayName();
    PunishmentDeserializer<T> getDeserializer();

    String getPermission();
    String getProtectionPermission();
    String getCancelPermission();

}
