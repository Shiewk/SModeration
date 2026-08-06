package de.shiewk.smoderation.paper.webhook;

public record WebhookPayload(Embed[] embeds) {

    public record Embed(String title, String description, int color, Field[] fields) {

        public record Field(String name, String value, boolean inline){

        }

    }

}
