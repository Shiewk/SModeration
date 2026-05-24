package de.shiewk.smoderation.paper.util;

import de.shiewk.smoderation.paper.punishments.*;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import static de.shiewk.smoderation.paper.SModerationPaper.LOGGER;

public final class SModLegacy {
    private SModLegacy() {}

    private static byte[] longToBytes(long v){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(v);
        return buffer.array();
    }

    private static long bytesToLong(byte[] i){
        if (i.length != 8){
            throw new IllegalArgumentException("length must be 8");
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(0, i);
        return buffer.getLong(0);
    }

    private static byte[] uuidToBytes(UUID uuid){
        byte[] l = longToBytes(uuid.getLeastSignificantBits());
        byte[] m = longToBytes(uuid.getMostSignificantBits());
        return new byte[]{
                m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7],
                l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7]
        };
    }

    private static UUID bytesToUuid(byte[] i){
        if (i.length != 16){
            throw new IllegalArgumentException("length must be 16, was " + i.length);
        }
        long l = bytesToLong(new byte[]{ i[8], i[9], i[10], i[11], i[12], i[13], i[14], i[15] });
        long m = bytesToLong(new byte[]{ i[0], i[1], i[2], i[3], i[4], i[5], i[6], i[7] });
        return new UUID(m, l);
    }

    private static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4){
            throw new IllegalArgumentException("length must be 4");
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(0, bytes);
        return buffer.getInt(0);
    }

    private static byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    private static byte[] readStreamInternal(InputStream stream, int len) throws IOException {
        final byte[] bytes = stream.readNBytes(len);
        if (bytes.length != len){
            throw new EOFException("Stream has ended before enough bytes were read");
        }
        return bytes;
    }

    public static void migrateV1PunishmentsFile(PunishmentManager manager, Path path, Path copy) {
        int count = 0;
        try {
            if (Files.isRegularFile(path)) {
                LOGGER.info("Migrating V1 punishment file: {}", path);
                try (InputStream in = new FileInputStream(path.toFile());
                     GZIPInputStream gzin = new GZIPInputStream(in)){
                    while (gzin.available() > 0){
                        int type = bytesToInt(readStreamInternal(gzin, 4));
                        long time = bytesToLong(readStreamInternal(gzin, 8));
                        long until = bytesToLong(readStreamInternal(gzin, 8));
                        UUID by = bytesToUuid(readStreamInternal(gzin, 16));
                        UUID to = bytesToUuid(readStreamInternal(gzin, 16));
                        int reasonLen = bytesToInt(readStreamInternal(gzin, 4));
                        String reason = new String(readStreamInternal(gzin, reasonLen));
                        UUID canceller = null;
                        boolean cancelled = gzin.read() == 1;
                        if (cancelled){
                            canceller = bytesToUuid(readStreamInternal(gzin, 16));
                        }
                        // Type 0: mute; 1: kick; 2: ban
                        Punishment p = switch (type){
                            case 0 -> new Mute(
                                    manager,
                                    Punishment.generateUUID(),
                                    time,
                                    by,
                                    to,
                                    reason,
                                    until - time,
                                    canceller
                            );
                            case 1 -> new Kick(
                                    manager,
                                    Punishment.generateUUID(),
                                    time,
                                    by,
                                    to,
                                    reason
                            );
                            case 2 -> new Ban(
                                    manager,
                                    Punishment.generateUUID(),
                                    time,
                                    by,
                                    to,
                                    reason,
                                    until - time,
                                    canceller
                            );
                            default -> throw new IllegalArgumentException("Invalid legacy type for punishment: " + type);
                        };
                        count++;
                        manager.appendToSave(p);
                        LOGGER.info("Migrated: {}", p);
                    }
                }
                LOGGER.info("Successfully loaded {} items.", count);
                Files.move(path, copy);
            }
        } catch (EOFException e) {
            LOGGER.error("The file was not correctly saved, {} items could be recovered!", count);
        } catch (IOException e){
            LOGGER.error("An error occurred while loading", e);
            throw new RuntimeException(e);
        }
    }
}
