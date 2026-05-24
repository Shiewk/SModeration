package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.util.SerializationHelper;
import org.jetbrains.annotations.NotNull;

public interface PunishmentDeserializer<T extends Punishment> {

    @NotNull T deserialize(PunishmentManager manager, SerializationHelper helper);

}
