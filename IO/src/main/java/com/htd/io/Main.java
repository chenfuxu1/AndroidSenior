package com.htd.io;

import com.htd.utils.Sout;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.Okio;
import okio.Source;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-02-19 22:25
 * <p>
 * Desc:
 */
public class Main {
    private static final String TAG = "Main";

    public static void nio() {
        try {
            RandomAccessFile file = new RandomAccessFile("./IO/htd.txt", "r");
            FileChannel channel = file.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            channel.read(byteBuffer);

            // 这两行等价于 byteBuffer.flip()
            byteBuffer.limit(byteBuffer.position());
            byteBuffer.position(0);
            // byteBuffer.flip();

            Sout.INSTANCE.d(TAG, Charset.defaultCharset().decode(byteBuffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 阻塞的 nio
    public static void nio2() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(80));
            serverSocketChannel.configureBlocking(false); // 非阻塞式
            SocketChannel socketChannel = serverSocketChannel.accept();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(byteBuffer);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void okio() {
        try(Source source = Okio.source(new File("./IO/htd.txt"))) {
            Buffer buffer = new Buffer();
            source.read(buffer, 1024);
            Sout.INSTANCE.d(TAG, buffer.readUtf8Line());
            Sout.INSTANCE.d(TAG, buffer.readUtf8Line());
            Sout.INSTANCE.d(TAG, buffer.readUtf8Line());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
