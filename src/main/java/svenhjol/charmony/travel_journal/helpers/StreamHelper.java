package svenhjol.charmony.travel_journal.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class StreamHelper {
    public static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }

    public static int readInt(ByteArrayInputStream stream) throws IOException {
        byte[] intBytes = new byte[4];
        if (stream.read(intBytes) != 4) {
            throw new IOException("Could not read integer from stream");
        }
        return ((intBytes[0] & 0xFF) << 24) |
            ((intBytes[1] & 0xFF) << 16) |
            ((intBytes[2] & 0xFF) << 8)  |
            (intBytes[3] & 0xFF);
    }
}
