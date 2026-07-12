package org.jeecg.modules.uav.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;

public class ByteUtils {
    // int转小端序字节数组
    public static byte[] intToBytesLittleEndian(int value) {
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }
    // LocalDateTime转9字节时间戳
    public static byte[] localDateTimeTo9Bytes(LocalDateTime time) {
        ByteBuffer buffer = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) time.getYear());          // 年(2字节)
        buffer.put((byte) time.getMonthValue());          // 月(1字节)
        buffer.put((byte) time.getDayOfMonth());          // 日(1字节)
        buffer.put((byte) time.getHour());                // 时(1字节)
        buffer.put((byte) time.getMinute());              // 分(1字节)
        buffer.put((byte) time.getSecond());              // 秒(1字节)
        buffer.putShort((short) ((short) time.getNano() / 1000000)); // 毫秒(2字节)
        return buffer.array();
    }
    /**
     * 小端序：字节数组转int
     */
    public static int bytesToIntLittleEndian(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("字节数组长度必须为4");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
    }

    /**
     * 小端序：字节数组转long
     */
    public static long bytesToLongLittleEndian(byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException("字节数组长度必须为8");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    /**
     * 小端序：字节数组转float
     */
    public static float bytesToFloatLittleEndian(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("字节数组长度必须为4");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getFloat();
    }

    /**
     * 解析9字节时间戳（文档定义格式）
     */
    public static LocalDateTime parse9ByteTimestamp(byte[] bytes) {
        if (bytes.length != 9) {
            throw new IllegalArgumentException("时间戳字节数组长度必须为9");
        }
        
        // 解析规则：Year(2字节) + Month(1) + Day(1) + Hour(1) + Minute(1) + Second(1) + Millisecond(2)
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int year = buffer.getShort() & 0xFFFF;
        int month = buffer.get() & 0xFF;
        int day = buffer.get() & 0xFF;
        int hour = buffer.get() & 0xFF;
        int minute = buffer.get() & 0xFF;
        int second = buffer.get() & 0xFF;
        
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    /**
     * 字节数组转十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    // 字节数组合并
    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] arr : arrays) {
            length += arr.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }
        return result;
    }

    /**
     * 小端序字节数组转short
     */
    public static short bytesToShortLittleEndian(byte[] bytes) {
        if (bytes == null || bytes.length != 2) {
            throw new IllegalArgumentException("字节数组长度必须为2");
        }
        short value = 0;
        value |= (short) ((bytes[0] & 0xFF) << 0);
        value |= (short) ((bytes[1] & 0xFF) << 8);
        return value;
    }
}
