package com.onesaf.farm.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 二进制数据读取工具类
 */
public class BinaryReader implements Closeable {
    private final DataInputStream inputStream;
    private final ByteOrder byteOrder;

    public BinaryReader(InputStream inputStream, ByteOrder byteOrder) {
        this.inputStream = new DataInputStream(inputStream);
        this.byteOrder = byteOrder;
    }

    public byte readByte() throws IOException {
        return inputStream.readByte();
    }

    public int readUInt8() throws IOException {
        return inputStream.readUnsignedByte();
    }

    public int readUInt16() throws IOException {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            // 首先读取两个字节
            int b1 = inputStream.readUnsignedByte();
            int b2 = inputStream.readUnsignedByte();

            // 使用小端字节序将它们组合
            return (b2 << 8) | b1;
        } else {
            // 大端字节序，直接读取无符号短整型
            return inputStream.readUnsignedShort();
        }
    }

    public short readInt16() throws IOException {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            // 首先读取两个字节
            int b1 = inputStream.readUnsignedByte();
            int b2 = inputStream.readByte();

            // 使用小端字节序将它们组合
            return (short)((b2 << 8) | b1);
        } else {
            // 大端字节序，直接读取有符号短整型
            return inputStream.readShort();
        }
    }

    public int readInt32() throws IOException {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            // 首先读取四个字节
            int b1 = inputStream.readUnsignedByte();
            int b2 = inputStream.readUnsignedByte();
            int b3 = inputStream.readUnsignedByte();
            int b4 = inputStream.readByte(); // 最高位可能为负

            // 使用小端字节序将它们组合
            return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        } else {
            // 大端字节序，直接读取有符号整型
            return inputStream.readInt();
        }
    }

    public long readUInt32() throws IOException {
        long value = inputStream.readInt() & 0xFFFFFFFFL;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return Integer.reverseBytes((int) value) & 0xFFFFFFFFL;
        }
        return value;
    }

    public long readInt64() throws IOException {
        long value = inputStream.readLong();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return Long.reverseBytes(value);
        }
        return value;
    }

    public float readFloat32() throws IOException {
        int intBits = inputStream.readInt();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            intBits = Integer.reverseBytes(intBits);
        }
        return Float.intBitsToFloat(intBits);
    }

    public double readFloat64() throws IOException {
        long longBits = inputStream.readLong();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            longBits = Long.reverseBytes(longBits);
        }
        return Double.longBitsToDouble(longBits);
    }

    public boolean readBoolean() throws IOException {
        return inputStream.readBoolean();
    }

    /**
     * 读取一个字符串，格式为：
     * - UInt16长度
     * - 字符数据
     * - 如果长度是奇数，则有一个填充字节
     */
    public String readString() throws IOException {
        try {
            // 读取字符串长度
            int length = readUInt16();

            if (length == 0) {
                // 空字符串，但仍需要考虑可能的填充
                return "";
            }

            if (length > 10000) {
                // 防止异常长度导致内存问题
                throw new IOException("字符串长度异常: " + length);
            }

            // 读取字符数据
            byte[] bytes = new byte[length];
            inputStream.readFully(bytes);

            // 如果长度是奇数，读取一个额外的字节作为填充
            if (length % 2 != 0) {
                inputStream.readByte();
            }

            return new String(bytes, StandardCharsets.UTF_8);
        } catch (EOFException e) {
            throw new IOException("读取字符串时遇到文件结束", e);
        } catch (Exception e) {
            throw new IOException("读取字符串时出错: " + e.getMessage(), e);
        }
    }

    public UUID readUUID() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 读取16个字节
        byte[] bytes = new byte[16];
        inputStream.readFully(bytes);

        buffer.put(bytes);
        buffer.flip();

        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    /**
     * 跳过指定字节数
     */
    public void skip(long bytesToSkip) throws IOException {
        long skipped = 0;
        while (skipped < bytesToSkip) {
            long s = inputStream.skip(bytesToSkip - skipped);
            if (s <= 0) {
                break;
            }
            skipped += s;
        }

        if (skipped < bytesToSkip) {
            throw new EOFException("Unexpected end of file");
        }
    }
}