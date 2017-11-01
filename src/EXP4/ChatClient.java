package EXP4;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * 在线聊天客户端 步骤： 1、生成图形窗口界面轮廓 2、为轮廓添加关闭事件 3、在轮廓中加入输入区域和内容展示区域 4、为输入区域添加回车事件
 * 5、建立服务端连接并发送数据
 *
 * @author tuzongxun123
 *
 */
public class ChatClient extends Frame {

    // 用户输入区域
    private TextField textField = new TextField();
    // 内容展示区域
    private TextArea textArea = new TextArea();
    private Socket socket = null;
    // 数据输出流
    private DataOutputStream dataOutputStream = null;
    // 数据输入流
    private DataInputStream dataInputStream = null;
    private boolean isConnect = false;
    Thread tReceive = new Thread(new ReceiveThread());
    String name = "";

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.createName();
        chatClient.launcFrame();

    }

    /**
     * 建立一个简单的图形化窗口
     *
     * @author：tuzongxun
     * @Title: launcFrame
     * @param
     * @return void
     * @date May 18, 2016 9:57:00 AM
     * @throws
     */
    public void launcFrame() {
        setLocation(300, 200);
        this.setSize(200, 400);
        add(textField, BorderLayout.SOUTH);
        add(textArea, BorderLayout.NORTH);
        // 根据窗口里面的布局及组件的preferedSize来确定frame的最佳大小
        pack();
        // 监听图形界面窗口的关闭事件
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
                disConnect();
            }
        });
        textField.addActionListener(new TFLister());
        // 设置窗口可见
        setVisible(true);
        connect();
        // 启动接受消息的线程
        tReceive.start();
    }


    public void connect() {
        try {
            // 新建服务端连接
            socket = new Socket("127.0.0.1", 5000);
            // 获取客户端输出流
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            System.out.println("连上服务端");
            isConnect = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 生成随机的客户端名字
    public void createName() {
        String[] str1 = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8",
                "9", "0", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z" };
        Random ran = new Random();

        for (int i = 0; i < 6; i++) {
            int n = ran.nextInt(str1.length);
            if (n < str1.length) {
                String str = str1[n];
                name = name + str;
                System.out.println(name);
            } else {
                i--;
                continue;
            }
        }
        this.setTitle(name);
    }

    public void disConnect() {
        try {
            isConnect = false;
            // 停止线程
            tReceive.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendMessage(String text) {
        try {
            dataOutputStream.writeUTF(name + ":" + text);
            dataOutputStream.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private class TFLister implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = textField.getText().trim();
            // 清空输入区域信息
            textField.setText("");
            // 回车后发送数据到服务器
            sendMessage(text);
        }
    }

    private class ReceiveThread implements Runnable {
        @Override
        public void run() {
            try {
                while (isConnect) {
                    String message = dataInputStream.readUTF();
                    String txt = textArea.getText();
                    if (txt != null && !"".equals(txt.trim())) {
                        message = textArea.getText() + "\n" + message;
                    }
                    textArea.setText(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
