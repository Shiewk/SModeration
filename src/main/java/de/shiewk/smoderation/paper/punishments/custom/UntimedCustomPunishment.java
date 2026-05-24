package de.shiewk.smoderation.paper.punishments.custom;

import de.shiewk.smoderation.paper.command.CommandProvider;
import de.shiewk.smoderation.paper.command.UntimedPunishmentCommand;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentDeserializer;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.UUID;

import static de.shiewk.smoderation.paper.SModerationPaper.LOGGER;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class UntimedCustomPunishment extends Punishment {

    public record Metadata(
            String type,
            Component typeDisplayName,
            String[] commandNames,
            String commandPermission,
            String protectionPermission,
            String[] effects,
            boolean requireOnline
    ){

        public UntimedCustomPunishment deserialize(PunishmentManager manager, SerializationHelper helper){
            return new UntimedCustomPunishment(
                    manager,
                    helper.getUUID("id"),
                    helper.getLong("timestamp"),
                    helper.getUUID("issuer"),
                    helper.getUUID("target"),
                    helper.getString("reason"),
                    this
            );
        }

        public CommandProvider createCommand(PunishmentManager manager) {
            return new UntimedPunishmentCommand(
                    manager,
                    this.commandNames,
                    this.createPunishmentType(),
                    "Applies the '" + this.type + "' punishment to a player",
                    this.requireOnline,
                    (pm, issuer, target, reason) -> new UntimedCustomPunishment(
                            pm,
                            Punishment.generateUUID(),
                            System.currentTimeMillis(),
                            issuer,
                            target,
                            reason,
                            this
                    )
            );
        }

        public PunishmentType<?> createPunishmentType() {
            return new Type();
        }

        private class Type implements PunishmentType<UntimedCustomPunishment> {

            @Override
            public String getTypeId() {
                return Metadata.this.type();
            }

            @Override
            public Component getDisplayName() {
                return Metadata.this.typeDisplayName();
            }

            @Override
            public PunishmentDeserializer<UntimedCustomPunishment> getDeserializer() {
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
                throw new UnsupportedOperationException("Punishment is untimed, cannot be cancelled");
            }

        }
    }

    protected final Metadata metadata;

    private UntimedCustomPunishment(PunishmentManager manager, UUID id, long timestamp, UUID issuer, UUID target, String reason, Metadata metadata) {
        super(manager, id, metadata.type(), timestamp, issuer, target, reason);
        this.metadata = metadata;
    }

    public static Metadata tryCreateMetadata(String type, ConfigurationSection typeConfig){
        String displayName = typeConfig.getString("name", type);
        List<String> effects = new ObjectArrayList<>(typeConfig.getStringList("effects"));
        List<String> commandNames = new ObjectArrayList<>(typeConfig.getStringList("commands"));
        String commandPermission = typeConfig.getString("permission", "smod." + type);
        String protectionPermission = typeConfig.getString("protection-permission", "smod.prevent" + type);
        boolean requireOnline = typeConfig.getBoolean("require-online", true);

        commandNames.replaceAll(CommandUtil::removeLeadingSlash);
        effects.replaceAll(CommandUtil::removeLeadingSlash);

        return new Metadata(
                type,
                text(displayName),
                commandNames.toArray(String[]::new),
                commandPermission,
                protectionPermission,
                effects.toArray(String[]::new),
                requireOnline
        );
    }

    @Override
    public void processIssue() {
        super.processIssue();
        LOGGER.info("Dispatching effect commands on issue, type: '{}'", getTypeId());

        Object2ObjectArrayMap<String, String> varMap = new Object2ObjectArrayMap<>();
        varMap.put("uuid", this.target.toString());
        varMap.put("name", PlayerUtil.offlinePlayerName(this.target));
        varMap.put("moduuid", this.issuer.toString());
        varMap.put("modname", PlayerUtil.offlinePlayerName(this.issuer));
        varMap.put("reason", this.reason);
        varMap.put("type", this.type);

        for (String command : metadata.effects) {
            TimedCustomPunishment.dispatchCommandWithVars(varMap, command);
        }
    }

    @Override
    public Component adminMessage() {
        return translatable(
                "smod.punishment.broadcast.custom.untimed",
                getType().getDisplayName(),
                text(PlayerUtil.offlinePlayerName(target)),
                text(PlayerUtil.offlinePlayerName(issuer)),
                text(reason)
        );
    }

    @Override
    public Component infoMessage() {
        return translatable(
                "smod.punishment.playerMessage.custom.untimed",
                getType().getDisplayName(),
                text(PlayerUtil.offlinePlayerName(this.issuer)),
                text(reason)
        );
    }

}
