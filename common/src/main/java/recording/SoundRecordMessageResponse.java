package recording;

import formaters.SoundRecordMessage;

import java.nio.ByteBuffer;

/**
 * Created by kinder112 on 30.10.2016.
 */
public class SoundRecordMessageResponse {
    private short magic;
    private long timestamp;

    public SoundRecordMessageResponse(SoundRecordMessage message) {
        magic = message.getMagic();
        timestamp = message.getTimestamp();
    }

    public SoundRecordMessageResponse(byte[] buffer) {
        final ByteBuffer wrappedBuffer = ByteBuffer.wrap(buffer);
        magic = wrappedBuffer.getShort();
        timestamp = wrappedBuffer.getLong();
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.putShort(magic);
        buffer.putLong(timestamp);
        return buffer.array();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
