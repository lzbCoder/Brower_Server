package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    //开启服务器
    public void startServer(){
        try {
            System.out.println("启动服务器");
            //获取配置文件中的port
            int port = Integer.parseInt(ServerFileReader.getValue("port"));
            //创建ServerSocket对象，开启一个服务
            ServerSocket server = new ServerSocket(port);
            //接受多个浏览器连接
            while (true){
                //等待某一个客户端过来连接
                Socket socket = server.accept();
                //启动一个线程  负责处理当前浏览器发送过来的消息
                new ServerHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
