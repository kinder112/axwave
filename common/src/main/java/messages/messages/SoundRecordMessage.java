package messages.messages;

import recording.SoundRecord;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Formats SoundRecord according to this specification: <br>
 * Magic number (2 bytes) 0x12 0x34 <br>
 * Packet size (2 bytes) (sizeof(timestamp) + sizeof(sound format) + samples.length) <br>
 * Timestamp of first sample in payload (8 bytes) <br>
 * Sound format (2 bytes) (AudioFormatEnum ) <br>
 * Sound samples <br>
 */
public class SoundRecordMessage {

    private static final int TIMESTAMP_SIZE = 8;
    private static final int SOUND_FORMAT_SIZE = 2;

    private SoundRecord record;
    private short magic;
    private short packetSize;
    private long timestamp;

    public SoundRecordMessage(SoundRecord record, short magic) {
        this.record = record;
        this.magic = magic;
    }

    public SoundRecordMessage(SoundRecord record, short magic, short packetSize) {
        this(record, magic);
        this.packetSize = packetSize;
    }

    public ByteArrayOutputStream toByteStream() {
        final ByteArrayOutputStream formattedSample = new ByteArrayOutputStream();

        final byte[] magic = shortToBytes(this.magic);
        final byte[] packetSize = shortToBytes(calculatePacketSize());
        final byte[] timestamp = longToBytes(record.getTimestamp());
        final byte[] soundFormat = shortToBytes(record.getAudioFormat().getFormatEncoding());
        final byte[] samples = record.getSamples().toByteArray();

        formattedSample.write(magic, 0, magic.length);
        formattedSample.write(packetSize, 0, packetSize.length);
        formattedSample.write(timestamp, 0, timestamp.length);
        formattedSample.write(soundFormat, 0, soundFormat.length);
        formattedSample.write(samples, 0, samples.length);

        return formattedSample;
    }

    private short calculatePacketSize() {
        return (short) (TIMESTAMP_SIZE + SOUND_FORMAT_SIZE + record.getSamples().size());
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private byte[] shortToBytes(short x) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(x);
        return buffer.array();
    }

    public short getMagic() {
        return magic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Magic: 0x%04X Packet size: %d Timestamp: %d ", magic, packetSize, timestamp);
    }
}