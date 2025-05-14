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
        int value = inputStream.readUnsignedShort();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return ((value & 0xFF) << 8) | ((value & 0xFF00) >> 8);
        }
        return value;
    }

    public short readInt16() throws IOException {
        short value = inputStream.readShort();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return (short) (((value & 0xFF) << 8) | ((value & 0xFF00) >> 8));
        }
        return value;
    }

    public int readInt32() throws IOException {
        int value = inputStream.readInt();
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return Integer.reverseBytes(value);
        }
        return value;
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

    public String readString() throws IOException {
        int length = readUInt16();
        byte[] bytes = new byte[length];
        inputStream.readFully(bytes);

        // 如果长度是奇数，读取一个额外的字节作为填充
        if (length % 2 != 0) {
            inputStream.readByte();
        }

        return new String(bytes, StandardCharsets.UTF_8);
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