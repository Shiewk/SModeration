package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.util.SerializationHelper;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class Mute extends TimedPunishment {

    public Mute(UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration, UUID cancelledBy) {
        super(id, "mute", timestamp, issuer, target, reason, duration, cancelledBy);
    }

    public Mute(UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration) {
        this(id, timestamp, issuer, target, reason, duration, null);
    }

    public static class Factory implements PunishmentFactory<Mute> {

        @Override
        public @NonNull Mute deserialize(SerializationHelper helper) {
            return new Mute(
                    helper.getUUID("id"),
                    helper.getLong("timestamp"),
                    helper.getUUID("issuer"),
                    helper.getUUID("target"),
                    helper.getString("reason"),
                    helper.getLong("duration"),
                    helper.getUUID("cancelledBy", null)
            );
        }

    }
}
