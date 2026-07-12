package org.jeecg.modules.uav.util;


import org.jeecg.modules.uav.socket.SocketManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class test {


    public static void main(String[] args) throws IOException {
        Socket socket=new Socket("127.0.0.1", 9999);
        socket.setSoTimeout(2*1000); // 超时设置
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        boolean connected = socket.isConnected();
        System.out.println("连接状态"+connected);
        byte[] buffer = new byte[1024];
        int len;
        while ( (len = inputStream.read(buffer)) != -1) {
            byte[] data = new byte[len];
            System.arraycopy(buffer, 0, data, 0, len);
            // 交给数据解析服务处理
            System.out.println("收到数据");
        }
    }

    // 工具方法
    public static byte[] hexStringToByteArray(String hexStr) {
        hexStr = hexStr.replaceAll("\\s+", "");
        if (hexStr.length() % 2 != 0) hexStr = "0" + hexStr;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length(); i += 2) {
            result[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4) + Character.digit(hexStr.charAt(i + 1), 16));
        }
        return result;
    }
}
