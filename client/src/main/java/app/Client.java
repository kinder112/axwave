package app;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import messages.SoundRecordMessage;
import netty.decoders.SoundRecordMessageResponseDecoder;
import netty.encoders.SoundRecordMessageEncoder;
import netty.handlers.ClientHandler;
import recording.SoundRecord;
import recording.impl.MicrophoneSoundSource;
import soundformats.AudioFormatEnum;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
import static soundformats.AudioFormatEnum.PCM_44100_16_STEREO_LE;

public class Client {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 19000;
    private static final short MAGIC = 0x1234;

    private static final int NUMBER_OF_EXECUTOR_THREADS = 10;
    private static final int NUMBER_OF_ARGUMENTS = 2;
    private static final int DEFAULT_RECORDING_FREQUENCY = 2;
    private static final int DEFAULT_RECORDING_LENGTH = 4;

    public static void main(String[] args) throws InterruptedException {
        int recordingFrequentness, recordingLength;

        if (args.length < NUMBER_OF_ARGUMENTS) {
            recordingFrequentness = DEFAULT_RECORDING_FREQUENCY;
            recordingLength = DEFAULT_RECORDING_LENGTH;
        } else {
            recordingFrequentness = Integer.parseInt(args[0]);
            recordingLength = Integer.parseInt(args[1]);

            if (recordingFrequentness < 1 || recordingLength < 1) {
                System.out.printf("Wrong argument values recordingFrequentness and recordingLength need to be > 0");
                return;
            }
        }

        System.out.printf("recordingFrequentness %d seconds, recordingLength %d seconds\n", recordingFrequentness, recordingLength);

        Bootstrap bootstrap = getBootstrap();
        final Channel channel = bootstrap.connect(SERVER_IP, SERVER_PORT).sync().channel();
        System.out.printf("Connected to ip %s on port %s\n", SERVER_IP, SERVER_PORT);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_EXECUTOR_THREADS);
        executorService.scheduleAtFixedRate(recordSoundAndSendThroughChannel(recordingLength, channel, PCM_44100_16_STEREO_LE), 0, recordingFrequentness, SECONDS);

    }

    private static Bootstrap getBootstrap() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new SoundRecordMessageEncoder());
                pipeline.addLast(new SoundRecordMessageResponseDecoder());
                pipeline.addLast(new ClientHandler());
            }
        });
        return bootstrap;
    }

    private static Runnable recordSoundAndSendThroughChannel(int recordingLength, Channel channel, AudioFormatEnum audioFormat) {
        return () -> {
            final SoundRecord soundRecord = new MicrophoneSoundSource().recordSound(audioFormat, recordingLength);
            SoundRecordMessage msg = null;
            try {
                msg = new SoundRecordMessage(soundRecord, MAGIC);
                System.out.println("Sending: " + msg);
                channel.writeAndFlush(msg);
            } catch (ArithmeticException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        };
    }
}
