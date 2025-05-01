package de.shiewk.smoderation.paper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkinTextureProvider {

    CompletableFuture<String> textureProperty(UUID player);

}
