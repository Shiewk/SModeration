package de.shiewk.smoderation.paper.punishments.custom;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.CancelPunishmentCommand;
import de.shiewk.smoderation.paper.command.CommandProvider;
import de.shiewk.smoderation.paper.command.TimedPunishmentCommand;
import de.shiewk.smoderation.paper.punishments.*;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import de.shiewk.smoderation.paper.util.TimeUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.shiewk.smoderation.paper.SModerationPaper.LOGGER;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class TimedCustomPunishment extends TimedPunishment {

    public record Effects(
            String[] apply,
            String[] join,
            String[] respawn,
            String[] death,
            String[] expire
    ){}

    public record Metadata(
            String type,
            Component typeDisplayName,
            String[] applyCommandNames,
            String[] cancelCommandNames,
            String commandPermission,
            String protectionPermission,
            String cancelPermission,
            Effects effects,
            boolean requireOnline
    ){

        public TimedCustomPunishment deserialize(PunishmentManager manager, SerializationHelper helper){
            return new TimedCustomPunishment(
                    manager,
                    helper.getUUID("id"),
                    helper.getLong("timestamp"),
                    helper.getUUID("issuer"),
                    helper.getUUID("target"),
                    helper.getString("reason"),
                    helper.getLong("duration"),
                    helper.getUUID("cancelledBy", null),
                    this,
                    helper.getBoolean("expiryAck", false)
            );
        }

        public PunishmentType<?> createPunishmentType() {
            return new Type();
        }

        public CommandProvider createApplyCommand(PunishmentManager manager) {
            return new TimedPunishmentCommand(
                    manager,
                    this.applyCommandNames,
                    this.createPunishmentType(),
                    "Applies the '" + this.type + "' punishment to a player",
                    this.requireOnline,
                    (pm, issuer, target, duration, reason) -> new TimedCustomPunishment(
                            pm,
                            Punishment.generateUUID(),
                            System.currentTimeMillis(),
                            issuer,
                            target,
                            reason,
                            duration,
                            null,
                            this,
                            false
                    )
            );
        }

        public CommandProvider createCancelCommand(PunishmentManager manager) {
            return new CancelPunishmentCommand(
                    manager,
                    this.cancelCommandNames,
                    this.type,
                    this.cancelPermission,
                    "Cancels the '" + this.type + "' punishment of a player"
            );
        }

        private class Type implements PunishmentType<TimedCustomPunishment> {

            @Override
            public String getTypeId() {
                return Metadata.this.type();
            }

            @Override
            public Component getDisplayName() {
                return Metadata.this.typeDisplayName();
            }

            @Override
            public PunishmentDeserializer<TimedCustomPunishment> getDeserializer() {
                return Metadata.this::deserialize;
            }

            @Override
            public String getPermission() {
                return Metadata.this.commandPermission();
            }

            @Override
            public String getProtectionPermission() {
                return Metadata.this.protectionPermission();
            }

            @Override
            public String getCancelPermission() {
                return Metadata.this.cancelPermission();
            }

        }
    }

    protected final Metadata metadata;
    protected boolean expiryAcknowledged;

    protected TimedCustomPunishment(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason, long duration, UUID cancelledBy, Metadata metadata, boolean expiryAcknowledged) {
        super(manager, id, metadata.type(), timestamp, issuer, target, reason, duration, cancelledBy);
        this.metadata = metadata;
        this.expiryAcknowledged = expiryAcknowledged;
    }

    @Override
    public void addSerializableProperties(SerializationHelper helper) {
        super.addSerializableProperties(helper);
        helper.putBoolean("expiryAck", this.expiryAcknowledged);
    }

    public static Metadata tryCreateMetadata(String type, ConfigurationSection typeConfig){
        String displayName = typeConfig.getString("name", type);
        boolean requireOnline = typeConfig.getBoolean("require-online", true);

        List<String> applyCommandNames = new ObjectArrayList<>(typeConfig.getStringList("commands.apply"));
        List<String> cancelCommandNames = new ObjectArrayList<>(typeConfig.getStringList("commands.cancel"));

        List<String> applyEffects = new ObjectArrayList<>(typeConfig.getStringList("effects.apply"));
        List<String> joinEffects = new ObjectArrayList<>(typeConfig.getStringList("effects.join"));
        List<String> respawnEffects = new ObjectArrayList<>(typeConfig.getStringList("effects.respawn"));
        List<String> deathEffects = new ObjectArrayList<>(typeConfig.getStringList("effects.death"));
        List<String> expireEffects = new ObjectArrayList<>(typeConfig.getStringList("effects.expire"));

        String commandPermission = typeConfig.getString("permission", "smod." + type);
        String protectionPermission = typeConfig.getString("protection-permission", "smod.prevent" + type);
        String cancelPermission = typeConfig.getString("cancel-permission", "smod.un" + type);

        applyCommandNames.replaceAll(CommandUtil::removeLeadingSlash);
        cancelCommandNames.replaceAll(CommandUtil::removeLeadingSlash);

        applyEffects.replaceAll(CommandUtil::removeLeadingSlash);
        joinEffects.replaceAll(CommandUtil::removeLeadingSlash);
        respawnEffects.replaceAll(CommandUtil::removeLeadingSlash);
        deathEffects.replaceAll(CommandUtil::removeLeadingSlash);
        expireEffects.replaceAll(CommandUtil::removeLeadingSlash);

        return new Metadata(
                type,
                text(displayName),
                applyCommandNames.toArray(String[]::new),
                cancelCommandNames.toArray(String[]::new),
                commandPermission,
                protectionPermission,
                cancelPermission,
                new Effects(
                        applyEffects.toArray(String[]::new),
                        joinEffects.toArray(String[]::new),
                        respawnEffects.toArray(String[]::new),
                        deathEffects.toArray(String[]::new),
                        expireEffects.toArray(String[]::new)
                ),
                requireOnline
        );
    }

    @Override
    public Component adminMessage() {
        if (isPermanent()){
            return translatable(
                    "smod.punishment.broadcast.custom.timed.permanent",
                    getType().getDisplayName(),
                    text(PlayerUtil.offlinePlayerName(target)),
                    text(PlayerUtil.offlinePlayerName(issuer)),
                    text(reason)
            );
        } else {
            return translatable(
                    "smod.punishment.broadcast.custom.timed",
                    getType().getDisplayName(),
                    text(PlayerUtil.offlinePlayerName(target)),
                    text(PlayerUtil.offlinePlayerName(issuer)),
                    text(reason),
                    TimeUtil.formatTimeLong(this.duration)
            );
        }
    }

    @Override
    public Component infoMessage() {
        if (isPermanent()){
            return translatable(
                    "smod.punishment.playerMessage.custom.timed.permanent",
                    getType().getDisplayName(),
                    text(PlayerUtil.offlinePlayerName(this.issuer)),
                    text(reason)
            );
        } else {
            return translatable(
                    "smod.punishment.playerMessage.custom.timed",
                    getType().getDisplayName(),
                    text(PlayerUtil.offlinePlayerName(this.issuer)),
                    text(reason),
                    TimeUtil.formatTimeLong(this.timestamp + this.duration - System.currentTimeMillis())
            );
        }
    }

    @Override
    public Component cancelMessage() {
        return translatable(
                "smod.punishment.cancel.custom",
                getType().getDisplayName(),
                text(PlayerUtil.offlinePlayerName(target)),
                text(PlayerUtil.offlinePlayerName(cancelledBy))
        );
    }

    @Override
    public void processIssue() {
        super.processIssue();
        this.applyEffects("apply", getCustomMetadata().effects().apply());
    }

    public void applyEffects(String event, String[] effects) {
        LOGGER.info("Applying {} custom effects (triggered by '{}')", effects.length, event);

        Object2ObjectArrayMap<String, String> varMap = collectVariables();

        for (String command : effects) {
            dispatchCommandWithVars(varMap, command);
        }
    }

    public @NonNull Object2ObjectArrayMap<String, String> collectVariables() {
        Object2ObjectArrayMap<String, String> varMap = new Object2ObjectArrayMap<>();
        varMap.put("uuid", this.target.toString());
        varMap.put("name", PlayerUtil.offlinePlayerName(this.target));
        varMap.put("duration", String.valueOf(this.getDuration()));
        varMap.put("moduuid", this.issuer.toString());
        varMap.put("modname", PlayerUtil.offlinePlayerName(this.issuer));
        varMap.put("reason", this.reason);
        varMap.put("type", this.type);
        return varMap;
    }

    public static void dispatchCommandWithVars(Object2ObjectArrayMap<String, String> varMap, String command) {
        for (Map.Entry<String, String> entry : varMap.entrySet()) {
            command = command.replaceAll("\\$" + entry.getKey(), entry.getValue());
        }

        String finalCommand = command;
        CommandUtil.dispatchConsoleCommand(finalCommand)
                .thenAccept(found -> {
                    if (!found) {
                        LOGGER.info("Failed to dispatch: '{}'", finalCommand);
                    }
                })
                .exceptionally(t -> {
                    LOGGER.error("Failed to dispatch: '{}'", finalCommand, t);
                    return null;
                });
    }

    public Metadata getCustomMetadata() {
        return metadata;
    }

    public boolean isExpiryAcknowledged() {
        return expiryAcknowledged;
    }

    public static void checkAllForExpiry(PunishmentManager manager) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayerForExpiry(player, manager);
        }
    }

    public static void checkPlayerForExpiry(Player player, PunishmentManager manager) {
        for (Punishment punishment : manager.byTargetUUID(player.getUniqueId(), p -> p instanceof TimedCustomPunishment c && !c.isActive() && !c.isExpiryAcknowledged())) {
            ((TimedCustomPunishment) punishment).acknowledgeExpiry();
        }
    }

    public void acknowledgeExpiry() {
        if (!isActive() && !isExpiryAcknowledged()){
            this.expiryAcknowledged = true;
            try {
                updateSaveData();
                applyEffects("expire", getCustomMetadata().effects().expire());
            } catch (IOException e) {
                SModerationPaper.LOGGER.error("Failed to update save data", e);
                this.expiryAcknowledged = false;
            }
        }
    }

}
