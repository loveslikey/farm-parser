package com.onesaf.farm.io;

import lombok.extern.log4j.Log4j2;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 二进制读取工具，用于高效读取二进制文件
 */
@Log4j2
public class BinaryReader implements Closeable {
    private FileChannel channel;
    private ByteBuffer buffer;
    private ByteOrder byteOrder;

    /**
     * 创建二进制读取器
     * @param path 文件路径
     * @throws IOException 如果文件无法打开
     */
    public BinaryReader(Path path) throws IOException {
        channel = FileChannel.open(path, StandardOpenOption.READ);
        buffer = ByteBuffer.allocateDirect(8192); // 8K缓冲区
        buffer.flip(); // 初始化为空
        byteOrder = ByteOrder.BIG_ENDIAN; // 默认使用大端字节序
    }

    /**
     * 设置字节序
     * @param order 字节序
     */
    public void setByteOrder(ByteOrder order) {
        this.byteOrder = order;
        buffer.order(order);
    }

    /**
     * 确保缓冲区中有足够的字节可供读取
     * @param bytes 需要的字节数
     * @throws IOException 如果文件结束或读取出错
     */
    private void ensureAvailable(int bytes) throws IOException {
        if (buffer.remaining() < bytes) {
            buffer.compact();
            channel.read(buffer);
            buffer.flip();

            if (buffer.remaining() < bytes) {
                throw new IOException("文件意外结束");
            }
        }
    }

    /**
     * 读取一个字节
     * @return 字节值
     * @throws IOException 如果读取出错
     */
    public byte readByte() throws IOException {
        ensureAvailable(1);
        return buffer.get();
    }

    /**
     * 读取一个无符号16位整数
     * @return 无符号16位整数值
     * @throws IOException 如果读取出错
     */
    public short readUInt16() throws IOException {
        ensureAvailable(2);
        return buffer.getShort();
    }

    /**
     * 读取一个无符号32位整数
     * @return 无符号32位整数值
     * @throws IOException 如果读取出错
     */
    public int readUInt32() throws IOException {
        ensureAvailable(4);
        return buffer.getInt();
    }

    /**
     * 读取一个有符号32位整数
     * @return 有符号32位整数值
     * @throws IOException 如果读取出错
     */
    public int readInt32() throws IOException {
        ensureAvailable(4);
        return buffer.getInt();
    }

    /**
     * 读取一个有符号64位整数
     * @return 有符号64位整数值
     * @throws IOException 如果读取出错
     */
    public long readInt64() throws IOException {
        ensureAvailable(8);
        return buffer.getLong();
    }

    /**
     * 读取一个64位浮点数
     * @return 64位浮点数值
     * @throws IOException 如果读取出错
     */
    public double readFloat64() throws IOException {
        ensureAvailable(8);
        return buffer.getDouble();
    }

    /**
     * 读取指定长度的字符串
     * @param length 字符串长度（字节数）
     * @return 字符串值
     * @throws IOException 如果读取出错
     */
    public String readString(int length) throws IOException {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = readByte();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 跳过指定数量的字节
     * @param bytes 要跳过的字节数
     * @throws IOException 如果跳过操作出错
     */
    public void skip(int bytes) throws IOException {
        while (bytes > 0) {
            int toSkip = Math.min(bytes, buffer.remaining());
            buffer.position(buffer.position() + toSkip);
            bytes -= toSkip;

            if (bytes > 0) {
                buffer.clear();
                channel.read(buffer);
                buffer.flip();
            }
        }
    }

    /**
     * 获取当前文件位置
     * @return 当前文件位置
     * @throws IOException 如果获取位置出错
     */
    public long position() throws IOException {
        return channel.position() - buffer.remaining();
    }

    /**
     * 设置文件位置
     * @param position 新的文件位置
     * @throws IOException 如果设置位置出错
     */
    public void seek(long position) throws IOException {
        channel.position(position);
        buffer.clear();
        buffer.flip();
    }

    /**
     * 关闭文件
     * @throws IOException 如果关闭出错
     */
    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }
}