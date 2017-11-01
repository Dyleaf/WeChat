package Server;

import bean.WeChatUser;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class MasterServer {

    /**
     * 用户列表
     */
    private ArrayList<WeChatUser> users;

    public ServerSocket masterServer;
    public WorkServer workServer;

    private int port = 8888;

    public void start() {
        users = new ArrayList<>();
        try {
            masterServer = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                workServer = new WorkServer(masterServer.accept(), users);
                workServer.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
