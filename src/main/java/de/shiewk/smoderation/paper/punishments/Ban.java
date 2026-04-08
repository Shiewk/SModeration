package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.inventory.CustomInventory;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class Ban extends TimedPunishment {

    public Ban(UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration, UUID cancelledBy) {
        super(id, "ban", timestamp, issuer, target, reason, duration, cancelledBy);
    }

    public Ban(UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration) {
        this(id, timestamp, issuer, target, reason, duration, null);
    }

    public static class Factory implements PunishmentFactory<Ban> {

        @Override
        public @NonNull Ban deserialize(SerializationHelper helper) {
            return new Ban(
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
