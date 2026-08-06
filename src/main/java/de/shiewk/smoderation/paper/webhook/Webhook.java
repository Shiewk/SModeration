package de.shiewk.smoderation.paper.webhook;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.inventory.CustomInventory;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.TimedPunishment;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SchedulerUtil;
import de.shiewk.smoderation.paper.util.TimeUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.translatable;


public final class Webhook {

    private final URI uri;
    private final Locale locale;

    public Webhook(URI uri, Locale locale) {
        this.uri = uri;
        this.locale = locale;
    }

    public CompletableFuture<Void> execute(WebhookPayload payload){
        return CompletableFuture.runAsync(() -> {
            try (HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build()) {
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(SModerationPaper.gson.toJson(payload)))
                        .header("Content-Type", "application/json")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() > 299) {
                    throw new RuntimeException("HTTP response from webhook: " + response.statusCode() + "; body: " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, r -> SchedulerUtil.runAsyncNow(SModerationPaper.PLUGIN, t -> r.run()));
    }

    public String renderToText(Component component){
        return PlainTextComponentSerializer.plainText().serialize(CustomInventory.renderComponent(this.locale, component));
    }

    public WebhookPayload makePunishmentIssueMessage(Punishment punishment){
        ObjectArrayList<WebhookPayload.Embed.Field> fields = new ObjectArrayList<>();
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.punishment.type")),
                "**" + renderToText(punishment.getType().getDisplayName()) + "** (" + punishment.getTypeId() + ")",
                true
        ));
        if (punishment instanceof TimedPunishment timed){
            fields.add(new WebhookPayload.Embed.Field(
                    renderToText(translatable("smod.webhook.punishment.duration")),
                    timed.isPermanent() ?
                            renderToText(translatable("smod.webhook.punishment.duration.infinite")) :
                            renderToText(TimeUtil.formatTimeLong(timed.getDuration())),
                    true
            ));
        }
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.punishment.target")),
                "**" + PlayerUtil.offlinePlayerName(punishment.getTargetID()) + "** (" + punishment.getTargetID() + ")",
                false
        ));
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.punishment.issuer")),
                "**" + PlayerUtil.offlinePlayerName(punishment.getIssuerID()) + "** (" + punishment.getIssuerID() + ")",
                false
        ));
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.punishment.reason")),
                punishment.getReason(),
                false
        ));
        return new WebhookPayload(
                new WebhookPayload.Embed[]{
                        new WebhookPayload.Embed(
                                this.renderToText(
                                        translatable(
                                                "smod.webhook.punishment.issued",
                                                punishment.getType().getDisplayName()
                                        )
                                ),
                                null,
                                0xff8800,
                                fields.toArray(WebhookPayload.Embed.Field[]::new)
                        )
                }
        );
    }

    public WebhookPayload makePunishmentCancelMessage(TimedPunishment punishment) {
        ObjectArrayList<WebhookPayload.Embed.Field> fields = new ObjectArrayList<>();
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.punishment.type")),
                "**" + renderToText(punishment.getType().getDisplayName()) + "** (" + punishment.getTypeId() + ")",
                true
        ));
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.cancel.durationRemaining")),
                punishment.isPermanent() ?
                        renderToText(translatable("smod.webhook.punishment.duration.infinite")) :
                        renderToText(TimeUtil.formatTimeLong(punishment.getExpiry() - System.currentTimeMillis())),
                true
        ));
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.cancel.canceller")),
                "**" + PlayerUtil.offlinePlayerName(punishment.getCancelledBy()) + "** (" + punishment.getCancelledBy() + ")",
                false
        ));
        fields.add(new WebhookPayload.Embed.Field(
                renderToText(translatable("smod.webhook.cancel.target")),
                "**" + PlayerUtil.offlinePlayerName(punishment.getTargetID()) + "** (" + punishment.getTargetID() + ")",
                false
        ));
        return new WebhookPayload(
                new WebhookPayload.Embed[]{
                        new WebhookPayload.Embed(
                                this.renderToText(
                                        translatable(
                                                "smod.webhook.cancel.cancelled",
                                                punishment.getType().getDisplayName()
                                        )
                                ),
                                null,
                                0xff8800,
                                fields.toArray(WebhookPayload.Embed.Field[]::new)
                        )
                }
        );
    }
}
