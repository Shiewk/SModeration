package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.inventory.CustomInventory;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.translatable;

public class Ban extends TimedPunishment {

    public Ban(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration, UUID cancelledBy) {
        super(manager, id, "ban", timestamp, issuer, target, reason, duration, cancelledBy);
    }

    public Ban(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration) {
        this(manager, id, timestamp, issuer, target, reason, duration, null);
    }

    public static class Type implements PunishmentType<Ban> {

        @Override
        public String getTypeId() {
            return "ban";
        }

        @Override
        public Component getDisplayName() {
            return translatable("smod.punishment.name.ban");
        }

        @Override
        public PunishmentDeserializer<Ban> getDeserializer() {
            return new Ban.Deserializer();
        }

        @Override
        public String getPermission() {
            return "smod.ban";
        }

        @Override
        public String getProtectionPermission() {
            return "smod.preventban";
        }

        @Override
        public String getCancelPermission() {
            return "smod.unban";
        }

    }

    public static class Deserializer implements PunishmentDeserializer<Ban> {

        @Override
        public @NonNull Ban deserialize(PunishmentManager manager, SerializationHelper helper) {
            return new Ban(
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

    @Override
    public void processIssue() {
        super.processIssue();
        final Player player = Bukkit.getPlayer(getTargetID());
        if (player != null) {
            player.kick(CustomInventory.renderComponent(player, infoMessage()));
        }
    }

}
