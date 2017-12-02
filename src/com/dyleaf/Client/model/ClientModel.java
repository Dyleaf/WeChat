package com.dyleaf.Client.model;

import com.dyleaf.Client.chatroom.MainView;
import com.dyleaf.Utils.GsonUtils;
import com.dyleaf.bean.ClientUser;
import com.dyleaf.bean.Message;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;

import static com.dyleaf.Utils.Constants.*;

public class ClientModel {

    private BufferedReader reader;
    private PrintWriter writer;
    private Socket client;
    private final int port = 8888;
    private String IP;
    private boolean isConnect = false;                               //连接标志
    private boolean chatChange = false;
    private String chatUser = "[group]";
    private String thisUser;
    private Gson gson;

    private LinkedHashMap<String, ArrayList<Message>> userSession;   //用户消息队列存储用
    private Thread keepalive = new Thread(new KeepAliveWatchDog());
    private Thread keepreceive = new Thread(new ReceiveWatchDog());

    private ObservableList<ClientUser> userList;
    private ObservableList<Message> chatRecoder;

    private ClientModel() {
        super();
        gson = new Gson();
        ClientUser user = new ClientUser();
        user.setUserName("[group]");
        user.setStatus("");
        userSession = new LinkedHashMap<>();
        userSession.put("[group]", new ArrayList<>());
        userList = FXCollections.observableArrayList();
        chatRecoder = FXCollections.observableArrayList();
        userList.add(user);
    }

    private static ClientModel instance;

    public static ClientModel getInstance() {
        if (instance == null) {
            synchronized (ClientModel.class) {
                if (instance == null) {
                    instance = new ClientModel();
                }
            }
        }
        return instance;
    }


    public void setChatUser(String chatUser) {
        if (!this.chatUser.equals(chatUser))
            chatChange = true;
        this.chatUser = chatUser;
        //消除未读信息状态
        for (int i = 0; i < userList.size(); i++) {
            ClientUser user = userList.get(i);
            if (user.getUserName().equals(chatUser)) {
                if (user.isNotify()) {
                    System.out.println("更改消息目录");
//                    user.setStatus(user.getStatus().substring(0, user.getStatus().length() - 3));
                    userList.remove(i);
                    userList.add(i, user);
                    user.setNotify(false);
                }
                chatRecoder.clear();
                chatRecoder.addAll(userSession.get(user.getUserName()));
                break;
            }
        }
    }

    public ObservableList<Message> getChatRecoder() {
        return chatRecoder;
    }

    public ObservableList<ClientUser> getUserList() {
        return userList;
    }

    public String getThisUser() {
        return thisUser;
    }

    class KeepAliveWatchDog implements Runnable {
        @Override
        public void run() {
            HashMap<Integer, Integer> map = new HashMap<>();
            map.put(COMMAND, COM_KEEP);
            try {
                System.out.println("keep alive start" + Thread.currentThread());
                //heartbeat detection
                while (isConnect) {
                    Thread.sleep(500);
//                    System.out.println("500ms keep");
                    writer.println(gson.toJson(map));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiveWatchDog implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println(" Receieve start" + Thread.currentThread());
                String message;
                while (isConnect) {
                    message = reader.readLine();
                    // System.out.println("读取服务器信息" + message);
                    handleMessage(message);
                }
            } catch (IOException e) {

            }
        }
    }

    /**
     * disconnect
     */
    public void disConnect() {
        isConnect = false;
        keepalive.stop();
        keepreceive.stop();
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void handleMessage(String message) {
        Map<Integer, Object> gsonMap = GsonUtils.GsonToMap(message);
        Integer command = GsonUtils.Double2Integer((Double) gsonMap.get(COMMAND));
        Message m;
        switch (command) {
            case COM_GROUP:
                HashSet<String> recoder = new HashSet<>();
                for (ClientUser u : userList) {
                    if (u.isNotify()) {
                        recoder.add(u.getUserName());
                    }
                }
                ArrayList<String> userData = (ArrayList<String>) gsonMap.get(COM_GROUP);
                userList.remove(1, userList.size());
                int onlineUserNum = 0;
                for (int i = 0; i < userData.size(); i++) {
                    ClientUser user = new ClientUser();
                    user.setUserName(userData.get(i));
                    user.setStatus(userData.get(++i));
                    if (user.getStatus().equals("online"))
                        onlineUserNum++;
                    if (recoder.contains(user.getUserName())) {
                        user.setNotify(true);
                        user.setStatus(user.getStatus() + "(*)");
                    }
                    userList.add(user);
                    userSession.put(user.getUserName(), new ArrayList<>());
                }
                int finalOnlineUserNum = onlineUserNum;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        MainView.getInstance().getLabUserCoumter().setText("服务器在线人数为" + finalOnlineUserNum);
                    }
                });
                break;
            case COM_CHATALL:
                m = new Message();
                m.setTimer((String) gsonMap.get(TIME));
                m.setSpeaker((String) gsonMap.get(SPEAKER));
                m.setContent((String) gsonMap.get(CONTENT));
                if (chatUser.equals("[group]")) {
                    chatRecoder.add(m);
                }
                userSession.get("[group]").add(m);
                break;
            case COM_CHATWITH:
                String speaker = (String) gsonMap.get(SPEAKER);
                String receiver = (String) gsonMap.get(RECEIVER);
                String time = (String) gsonMap.get(TIME);
                String content = (String) gsonMap.get(CONTENT);
                m = new Message();
                m.setSpeaker(speaker);
                m.setContent(content);
                m.setTimer(time);
                if (thisUser.equals(receiver)) {
                    if (!chatUser.equals(speaker)) {
                        for (int i = 0; i < userList.size(); i++) {
                            if (userList.get(i).getUserName().equals(speaker)) {
                                ClientUser user = userList.get(i);
                                if (!user.isNotify()) {
                                    //user.setStatus(userList.get(i).getStatus() + "(*)");
                                    user.setNotify(true);
                                }
                                userList.remove(i);
                                userList.add(i, user);
                                break;
                            }
                        }
                        System.out.println("标记未读");
                    }else{
                        chatRecoder.add(m);
                    }
                    userSession.get(speaker).add(m);
                }else{
                    if(chatUser.equals(receiver))
                        chatRecoder.add(m);
                    userSession.get(receiver).add(m);
                }
                break;
            default:
                break;
        }
        System.out.println("服务器发来消息" + message + "消息结束");
    }


    /**
     * 该方法作废
     *
     * @param chatUser
     * @return
     */
    private LinkedList<Message> loadChatRecoder(String chatUser) {
        LinkedList<Message> messagesList = new LinkedList<>();
        if (userSession.containsKey(chatUser)) {
            ArrayList<Message> recoder = userSession.get(chatUser);
            for (Message s : recoder) {
                messagesList.add(s);
            }
        }
        return messagesList;
    }

    /**
     * sent json string  message to server
     *
     * @param message that must be json string
     */
    public void sentMessage(String message) {
        writer.println(message);
    }


    /**
     * @param username
     * @param IP
     * @param buf
     * @return
     */
    public boolean CheckLogin(String username, String IP, String password, StringBuffer buf, int type) {
        this.IP = IP; //bind server IP
        Map<Integer, Object> map;
        try {
            //针对多次尝试登录
            if (client == null || client.isClosed()) {
                client = new Socket(IP, port);
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);
            }
            map = new HashMap<>();
            if (type == 0)
                map.put(COMMAND, COM_LOGIN);
            else
                map.put(COMMAND, COM_SIGNUP);
            map.put(USERNAME, username);
            map.put(PASSWORD, password);
            writer.println(gson.toJson(map));
            String strLine = reader.readLine(); //readline是线程阻塞的
            System.out.println(strLine);
            map = GsonUtils.GsonToMap(strLine);
            Integer result = GsonUtils.Double2Integer((Double) map.get(COM_RESULT));
            if (result == SUCCESS) {
                isConnect = true;
                //request group
                map.clear();
                map.put(COMMAND, COM_GROUP);
                writer.println(gson.toJson(map));
                thisUser = username;
                keepalive.start();
                keepreceive.start();
                return true;
            } else {
                String description = (String) map.get(COM_DESCRIPTION);
                buf.append(description);
                return false;
            }
        } catch (ConnectException e) {
            buf.append(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            buf.append(e.toString());
        }
        return false;
    }


}
