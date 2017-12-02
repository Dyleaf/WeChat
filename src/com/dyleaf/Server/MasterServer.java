package com.dyleaf.Server;

import com.dyleaf.Dao.UserDaoImpl;
import com.dyleaf.bean.ServerUser;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.ArrayList;

public class MasterServer {

    /**
     * 用户列表
     */
    private ArrayList<ServerUser> users;

    public ServerSocket masterServer;
    public WorkServer workServer;

    private int port = 8888;

    public void start() {
        users = new ArrayList<>();
        try {
            masterServer = new ServerSocket(port);
            try {
                users = (ArrayList<ServerUser>) UserDaoImpl.getInstance().findAll();
                for (ServerUser u:users) {
                    u.setStatus("offline");
                }
                System.out.println("get user"+users.size());
            } catch (SQLException e) {
                System.out.println("userList init failed");
                e.printStackTrace();
            }
            System.out.println("server loading");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                workServer = new WorkServer(masterServer.accept(), users);
                workServer.start();
                System.out.println("workServer product");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
