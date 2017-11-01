package EXP4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class Server {

    public static void main(String[] args) {
        new Server().start();
    }

    // 是否成功启动服务端
    private boolean isStart = false;
    // 服务端socket
    private ServerSocket serverSocket = null;
    // 客户端socket
    private Socket socket = null;
    // 保存客户端集合
    List<singleSocket> socketList = new ArrayList<singleSocket>();

    public void start() {
        try {
            serverSocket = new ServerSocket(5000);
            isStart = true;
            while (isStart) {
                // 启动监听
                socket = serverSocket.accept();
                System.out.println("one client connect");
                // 启动客户端线程
                singleSocket client = new singleSocket(socket);
                new Thread(client).start();
                socketList.add(client);
            }
        } catch (BindException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务器端对应的socket
     */
    private class singleSocket implements Runnable {
        private Socket socket = null;
        private DataInputStream dataInputStream = null;
        private DataOutputStream dataOutputStream = null;
        private boolean isConnect = false;

        public singleSocket(Socket socket) {
            this.socket = socket;
            try {
                isConnect = true;
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 将客户端的信息传回
         *
         * @param message
         */
        public void sendMessageToClients(String message) {
            try {
                dataOutputStream.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            isConnect = true;
//            isConnect = socket.isConnected();
            singleSocket singleSocket = null;
            try {
                while (isConnect) {
                    // 读取客户端传递的string
                    String message = dataInputStream.readUTF();
                    System.out.println("message from client：" + message);
                    for (int i = 0; i < socketList.size(); i++) {
                        singleSocket = socketList.get(i);
                        singleSocket.sendMessageToClients(message);
                    }
                }
            } catch (EOFException e) {
                System.out.println("client is closed");
            } catch (SocketException e) {
                //移除出问题的socket进行资源回收
                if (singleSocket != null) {
                    socketList.remove(singleSocket);
                }
                System.out.println("singleSocket is Closed");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 关闭相关资源，进行资源回收
                try {
                    dataInputStream.close();
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
