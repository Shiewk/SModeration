package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.util.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.translatable;

public class Mute extends TimedPunishment {

    public Mute(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration, UUID cancelledBy) {
        super(manager, id, "mute", timestamp, issuer, target, reason, duration, cancelledBy);
    }

    public Mute(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration) {
        this(manager, id, timestamp, issuer, target, reason, duration, null);
    }

    public static class Type implements PunishmentType<Mute> {

        @Override
        public String getTypeId() {
            return "mute";
        }

        @Override
        public Component getDisplayName() {
            return translatable("smod.punishment.name.mute");
        }

        @Override
        public PunishmentDeserializer<Mute> getDeserializer() {
            return new Mute.Deserializer();
        }

        @Override
        public String getPermission() {
            return "smod.mute";
        }

        @Override
        public String getProtectionPermission() {
            return "smod.preventmute";
        }

        @Override
        public String getCancelPermission() {
            return "smod.unmute";
        }

    }

    public static class Deserializer implements PunishmentDeserializer<Mute> {

        @Override
        public @NonNull Mute deserialize(PunishmentManager manager, SerializationHelper helper) {
            return new Mute(
                    manager,
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
