package de.shiewk.smoderation.paper.util;

import java.nio.ByteBuffer;
import java.util.UUID;


/**
 * Utility class for byte-based saving of integers, longs and UUIDs
 */
public abstract class ByteUtil {
    private ByteUtil(){}

    public static byte[] longToBytes(long v){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(v);
        return buffer.array();
    }

    public static long bytesToLong(byte[] i){
        if (i.length != 8){
            throw new IllegalArgumentException("length must be 8");
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(0, i);
        return buffer.getLong(0);
    }

    public static byte[] uuidToBytes(UUID uuid){
        byte[] l = longToBytes(uuid.getLeastSignificantBits());
        byte[] m = longToBytes(uuid.getMostSignificantBits());
        return new byte[]{
                m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7],
                l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7]
        };
    }

    public static UUID bytesToUuid(byte[] i){
        if (i.length != 16){
            throw new IllegalArgumentException("length must be 16, was " + i.length);
        }
        long l = bytesToLong(new byte[]{ i[8], i[9], i[10], i[11], i[12], i[13], i[14], i[15] });
        long m = bytesToLong(new byte[]{ i[0], i[1], i[2], i[3], i[4], i[5], i[6], i[7] });
        return new UUID(m, l);
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4){
            throw new IllegalArgumentException("length must be 4");
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(0, bytes);
        return buffer.getInt(0);
    }

    public static byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }
}
