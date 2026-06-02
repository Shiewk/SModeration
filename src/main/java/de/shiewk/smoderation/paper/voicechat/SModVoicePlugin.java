package de.shiewk.smoderation.paper.voicechat;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.punishments.Mute;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;

import java.util.UUID;

public final class SModVoicePlugin implements VoicechatPlugin {

    private final SModerationPaper plugin;
    private final PunishmentManager manager;

    public SModVoicePlugin(SModerationPaper plugin, PunishmentManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public String getPluginId() {
        return plugin.getName();
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection != null) {
            UUID uuid = connection.getPlayer().getUuid();
            if (!manager.byTargetUUID(uuid, p -> p instanceof Mute mute && mute.isActive()).isEmpty()) {
                event.cancel();
            }
        }
    }


}
