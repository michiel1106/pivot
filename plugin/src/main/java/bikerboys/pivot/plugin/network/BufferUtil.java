package bikerboys.pivot.plugin.network;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BufferUtil {

    public static int readVarInt(ByteBuffer buf) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buf.get();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((read & 0b10000000) != 0);
        return result;
    }

    public static void writeVarInt(ByteBuffer buf, int value) {
        while ((value & -128) != 0) {
            buf.put((byte) ((value & 127) | 128));
            value >>>= 7;
        }
        buf.put((byte) value);
    }

    public static String readString(ByteBuffer buf) {
        int length = readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuffer buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.put(bytes);
    }
}
