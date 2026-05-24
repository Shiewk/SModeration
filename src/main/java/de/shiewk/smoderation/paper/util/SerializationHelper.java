package de.shiewk.smoderation.paper.util;

import com.google.gson.JsonObject;

import java.util.UUID;

public final class SerializationHelper {

    private final JsonObject json;

    public SerializationHelper(JsonObject json) {
        this.json = json;
    }

    public String getString(String key) {
        try {
            return json.get(key).getAsString();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Key " + key + " does not exist on this object");
        } catch (UnsupportedOperationException | IllegalStateException e) {
            throw new IllegalStateException("Tried to get string " + key + ", but is " + json.get(key).getClass().getSimpleName());
        }
    }

    public void putString(String key, String value) {
        json.addProperty(key, value);
    }

    public long getLong(String key) {
        try {
            return json.get(key).getAsLong();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Key " + key + " does not exist on this object");
        } catch (UnsupportedOperationException | IllegalStateException e) {
            throw new IllegalStateException("Tried to get long " + key + ", but is " + json.get(key).getClass().getSimpleName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Tried to get long " + key + ", but is malformed: " + json.get(key).getAsString());
        }
    }

    public void putLong(String key, long value) {
        json.addProperty(key, value);
    }

    public UUID getUUID(String key) {
        try {
            return PlayerUtil.uuidFromString(getString(key));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("UUID " + key + " malformed: " + getString(key));
        }
    }

    public UUID getUUID(String key, UUID defaultValue) {
        try {
            return getUUID(key);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    public void putUUID(String key, UUID value) {
        if (value == null) return;
        json.addProperty(key, value.toString().replace("-", ""));
    }

    public boolean getBoolean(String key) {
        try {
            return json.get(key).getAsBoolean();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Key " + key + " does not exist on this object");
        } catch (UnsupportedOperationException | IllegalStateException e) {
            throw new IllegalStateException("Tried to get " + key + ", but is " + json.get(key).getClass().getSimpleName());
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getBoolean(key);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    public void putBoolean(String key, boolean value) {
        json.addProperty(key, value);
    }

}
