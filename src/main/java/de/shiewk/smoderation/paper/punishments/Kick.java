package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.inventory.CustomInventory;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class Kick extends Punishment {

    public Kick(UUID id, long timestamp, UUID issuer, UUID target, String reason) {
        super(id, "kick", timestamp, issuer, target, reason);
    }

    public static class Factory implements PunishmentFactory<Kick> {

        @Override
        public @NonNull Kick deserialize(SerializationHelper helper) {
            return new Kick(
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
