package com.onesaf.farm.io;

import com.onesaf.farm.model.GdcSlice;
import lombok.extern.log4j.Log4j2;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * 增强版二进制读取工具，特别注重字节序处理
 */
@Log4j2
public class EnhancedBinaryReader implements Closeable {
    private FileChannel channel;
    private ByteBuffer buffer;
    private ByteOrder byteOrder;
    private long position;
    private final int bufferSize = 8192; // 8K缓冲区

    /**
     * 创建二进制读取器
     * @param path 文件路径
     * @throws IOException 如果文件无法打开
     */
    public EnhancedBinaryReader(Path path) throws IOException {
        channel = FileChannel.open(path, StandardOpenOption.READ);
        buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.flip(); // 初始化为空
        position = 0;
        byteOrder = ByteOrder.BIG_ENDIAN; // 默认使用大端字节序
    }

    /**
     * 设置字节序
     * @param order 字节序
     */
    public void setByteOrder(ByteOrder order) {
        this.byteOrder = order;
        buffer.order(order);
        log.debug("字节序已设置为: {}", order);
    }

    /**
     * 确保缓冲区中有足够的字节可供读取
     * @param bytes 需要的字节数
     * @throws IOException 如果文件结束或读取出错
     */
    private void ensureAvailable(int bytes) throws IOException {
        if (buffer.remaining() < bytes) {
            buffer.compact();
            int read = channel.read(buffer);
            buffer.flip();

            if (read == -1 || buffer.remaining() < bytes) {
                throw new IOException("文件意外结束，需要 " + bytes + " 字节，但只有 " + buffer.remaining() + " 字节可用");
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
        byte value = buffer.get();
        position++;
        return value;
    }

    /**
     * 读取一个无符号字节
     * @return 无符号字节值 (0-255)
     * @throws IOException 如果读取出错
     */
    public short readUByte() throws IOException {
        return (short)(readByte() & 0xFF);
    }

    /**
     * 读取一个无符号16位整数 - 使用当前设置的字节序
     * @return 无符号16位整数值
     * @throws IOException 如果读取出错
     */
    public int readUInt16() throws IOException {
        ensureAvailable(2);
        short value = buffer.getShort();
        position += 2;
        return value & 0xFFFF; // 确保返回无符号值
    }

    /**
     * 读取一个16位整数 - 使用当前设置的字节序
     * @return 16位整数值
     * @throws IOException 如果读取出错
     */
    public short readInt16() throws IOException {
        ensureAvailable(2);
        short value = buffer.getShort();
        position += 2;
        return value;
    }

    /**
     * 读取一个无符号16位整数 - 使用小端字节序，不管当前字节序设置
     * @return 无符号16位整数值
     * @throws IOException 如果读取出错
     */
    public int readLittleEndianUInt16() throws IOException {
        byte b1 = readByte();
        byte b2 = readByte();
        return ((b2 & 0xFF) << 8) | (b1 & 0xFF);
    }

    /**
     * 读取一个无符号16位整数 - 使用大端字节序，不管当前字节序设置
     * @return 无符号16位整数值
     * @throws IOException 如果读取出错
     */
    public int readBigEndianUInt16() throws IOException {
        byte b1 = readByte();
        byte b2 = readByte();
        return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
    }

    /**
     * 检测并读取字节序标记
     * 约定：1表示小端，0表示大端
     * @return 检测到的字节序
     * @throws IOException 如果读取出错
     */
    public ByteOrder detectAndSetByteOrder() throws IOException {
        // 读取头两个字节
        byte first = readByte();
        byte second = readByte();

        // 重置位置
        position -= 2;
        channel.position(position);
        buffer.clear();
        buffer.flip();

        // 默认的字节序标记（参考FARM文档）
        // 0表示大端，1表示小端
        if (first == 1 && second == 0) {
            setByteOrder(ByteOrder.LITTLE_ENDIAN);
            log.info("检测到小端字节序 (01 00)");
            return ByteOrder.LITTLE_ENDIAN;
        } else if (first == 0 && second == 0) {
            setByteOrder(ByteOrder.BIG_ENDIAN);
            log.info("检测到大端字节序 (00 00)");
            return ByteOrder.BIG_ENDIAN;
        } else {
            // 如果不是标准格式，使用默认的大端字节序
            log.warn("未知的字节序标记: {} {}, 使用默认的大端字节序", first, second);
            setByteOrder(ByteOrder.BIG_ENDIAN);
            return ByteOrder.BIG_ENDIAN;
        }
    }

    /**
     * 读取一个32位整数 - 使用当前设置的字节序
     * @return 32位整数值
     * @throws IOException 如果读取出错
     */
    public int readInt32() throws IOException {
        ensureAvailable(4);
        int value = buffer.getInt();
        position += 4;
        return value;
    }

    /**
     * 读取一个无符号32位整数 - 使用当前设置的字节序
     * 注意：Java没有无符号int类型，所以返回long
     * @return 无符号32位整数值
     * @throws IOException 如果读取出错
     */
    public long readUInt32() throws IOException {
        return readInt32() & 0xFFFFFFFFL;
    }

    /**
     * 读取一个32位浮点数 - 使用当前设置的字节序
     * @return 32位浮点数值
     * @throws IOException 如果读取出错
     */
    public float readFloat32() throws IOException {
        ensureAvailable(4);
        float value = buffer.getFloat();
        position += 4;
        return value;
    }

    /**
     * 读取一个64位浮点数 - 使用当前设置的字节序
     * @return 64位浮点数值
     * @throws IOException 如果读取出错
     */
    public double readFloat64() throws IOException {
        ensureAvailable(8);
        double value = buffer.getDouble();
        position += 8;
        return value;
    }

    /**
     * 读取一个64位整数 - 使用当前设置的字节序
     * @return 64位整数值
     * @throws IOException 如果读取出错
     */
    public long readInt64() throws IOException {
        ensureAvailable(8);
        long value = buffer.getLong();
        position += 8;
        return value;
    }

    /**
     * 读取一个无符号64位整数 - 使用当前设置的字节序
     * 注意：Java没有无符号long类型，所以返回long，但在达到Long.MAX_VALUE时可能会溢出
     * @return 无符号64位整数值
     * @throws IOException 如果读取出错
     */
    public long readUInt64() throws IOException {
        // 由于Java没有无符号long，所以我们只能返回signed long
        // 如果值超过了最大的signed long，会发生溢出
        return readInt64();
    }

    /**
     * 读取一个布尔值
     * @return 布尔值
     * @throws IOException 如果读取出错
     */
    public boolean readBoolean() throws IOException {
        byte value = readByte();
        return value != 0;
    }

    /**
     * 读取一个枚举值 (2字节无符号整数)
     * @return 枚举值
     * @throws IOException 如果读取出错
     */
    public int readEnum() throws IOException {
        return readUInt16();
    }

    /**
     * 读取6字节的无符号整数
     * @return 包含6字节的byte数组
     * @throws IOException 如果读取出错
     */
    public byte[] readByte6() throws IOException {
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    /**
     * 读取UUID（16字节）
     * 按照UUID标准建议的二进制传输格式读取
     * @return UUID对象
     * @throws IOException 如果读取出错
     */
    public UUID readUUID() throws IOException {
        long mostSigBits = 0;
        long leastSigBits = 0;

        // 读取time_low (4字节)
        int timeLow = (int)readUInt32();

        // 读取time_mid (2字节)
        int timeMid = readUInt16();

        // 读取time_hi_and_version (2字节)
        int timeHiAndVersion = readUInt16();

        // 计算most significant bits
        mostSigBits = ((long)timeLow << 32) |
                ((long)timeMid << 16) |
                (long)timeHiAndVersion;

        // 读取clock_seq_hi_and_reserved (1字节)
        byte clockSeqHiAndReserved = readByte();

        // 读取clock_seq_low (1字节)
        byte clockSeqLow = readByte();

        // 读取node (6字节)
        byte[] node = new byte[6];
        for (int i = 0; i < 6; i++) {
            node[i] = readByte();
        }

        // 计算least significant bits
        leastSigBits = ((long)(clockSeqHiAndReserved & 0xFF) << 56) |
                ((long)(clockSeqLow & 0xFF) << 48);

        for (int i = 0; i < 6; i++) {
            leastSigBits = (leastSigBits << 8) | (node[i] & 0xFF);
        }

        return new UUID(mostSigBits, leastSigBits);
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
        if (buffer.remaining() >= bytes) {
            buffer.position(buffer.position() + bytes);
            position += bytes;
        } else {
            int toSkip = bytes - buffer.remaining();
            position += bytes;
            buffer.position(buffer.limit());
            channel.position(position);
            buffer.clear();
            buffer.flip();
        }
    }

    /**
     * 获取当前文件位置
     * @return 当前文件位置
     */
    public long position() {
        return position;
    }

    /**
     * 设置文件位置
     * @param newPosition 新的文件位置
     * @throws IOException 如果设置位置出错
     */
    public void seek(long newPosition) throws IOException {
        position = newPosition;
        channel.position(position);
        buffer.clear();
        buffer.flip();
    }

    /**
     * 获取文件大小
     * @return 文件大小（字节数）
     * @throws IOException 如果获取大小出错
     */
    public long size() throws IOException {
        return channel.size();
    }

    /**
     * 读取GdcSlice结构
     * @return GdcSlice对象
     * @throws IOException 如果读取出错
     */
    public GdcSlice readGdcSlice() throws IOException {
        GdcSlice slice = new GdcSlice();
        slice.setLatitudeStart(readFloat64());
        slice.setLatitudeStop(readFloat64());
        slice.setLongitudeStart(readFloat64());
        slice.setLongitudeStop(readFloat64());
        return slice;
    }

    /**
     * 关闭文件
     * @throws IOException 如果关闭出错
     */
    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
            channel = null;
        }
    }

    /**
     * 释放资源
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}