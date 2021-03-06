package netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import messages.SoundRecordMessage;
import messages.SoundRecordResponseMessage;
import recording.SoundRecord;
import writers.DataWriter;
import writers.SoundRecordFileWriter;


/**
 * Logic that describes Server reaction to {@link SoundRecordMessage} <br>
 * In nutshell Ping Client back with {@link SoundRecordResponseMessage} <br>
 * And asynchronously save received {@link recording.SoundRecord} to filesystem
 */
public class ServerHandler extends SimpleChannelInboundHandler<SoundRecordMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SoundRecordMessage msg) throws Exception {
        System.out.println("Got: " + msg);
        final SoundRecordResponseMessage response = new SoundRecordResponseMessage(msg);
        spawnThreadToSaveSoundRecord(msg);
        System.out.println("Responding: " + response);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    private void spawnThreadToSaveSoundRecord(SoundRecordMessage msg) {
        new Thread(() -> {
            DataWriter<SoundRecord> writer = new SoundRecordFileWriter();
            writer.write(msg.getRecord());
        }).start();
    }
}
