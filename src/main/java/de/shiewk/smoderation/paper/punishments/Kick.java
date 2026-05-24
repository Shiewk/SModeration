package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.inventory.CustomInventory;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.translatable;

public class Kick extends Punishment {

    public Kick(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason) {
        super(manager, id, "kick", timestamp, issuer, target, reason);
    }

    public static class Type implements PunishmentType<Kick> {

        @Override
        public String getTypeId() {
            return "kick";
        }

        @Override
        public Component getDisplayName() {
            return translatable("smod.punishment.name.kick");
        }

        @Override
        public PunishmentDeserializer<Kick> getDeserializer() {
            return new Deserializer();
        }

        @Override
        public String getPermission() {
            return "smod.kick";
        }

        @Override
        public String getProtectionPermission() {
            return "smod.preventkick";
        }

        @Override
        public String getCancelPermission() {
            throw new UnsupportedOperationException("can't cancel kick");
        }
    }

    public static class Deserializer implements PunishmentDeserializer<Kick> {

        @Override
        public @NonNull Kick deserialize(PunishmentManager manager, SerializationHelper helper) {
            return new Kick(
                    manager,
                    helper.getUUID("id"),
                    helper.getLong("timestamp"),
                    helper.getUUID("issuer"),
                    helper.getUUID("target"),
                    helper.getString("reason")
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
