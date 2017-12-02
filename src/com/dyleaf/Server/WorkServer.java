package com.dyleaf.Server;

import com.dyleaf.Dao.UserDaoImpl;
import com.dyleaf.Utils.GsonUtils;
import com.dyleaf.bean.ServerUser;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.dyleaf.Utils.Constants.*;

/**
 * @Author com.dyleaf [com.dyleaf.github.io]
 * @ceeate 2017/11/30
 * @description the worker server, deal with the thing of its user
 */
public class WorkServer extends Thread {

    private ServerUser workUser; //the user is connected

    private Socket socket;

    private ArrayList<ServerUser> users;

    private BufferedReader reader;

    private PrintWriter writer;

    private boolean isLogOut = false;

    private long currentTime = 0;

    private Gson gson;

    public WorkServer(Socket socket, ArrayList users) {
        super();
        gson = new Gson();
        this.socket = socket; //bind socket
        this.users = users;   //get the common user resource
    }

    @Override
    public void run() {
        //todo server's work
        try {
            currentTime = new Date().getTime();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            String readLine;
            while (true) {
                //heart check
                long newTime = new Date().getTime();
                if (newTime - currentTime > 2000) {
                    logOut();
                } else {
                    currentTime = newTime;
                }
                readLine = reader.readLine();
                if (readLine == null)
                    logOut();
                handleMessage(readLine);
                sentMessageToClient();
                if (isLogOut) {
                    // kill the I/O stream
                    reader.close();
                    writer.close();
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            logOut();
        } catch (IOException e) {
            e.printStackTrace();
            logOut();
        }
    }


    /**
     * the message to deal with com.dyleaf.Client's command
     *
     * @param readLine
     */
    private void handleMessage(String readLine) {
        System.out.println("handle message" + readLine);
        Map<Integer, Object> gsonMap = GsonUtils.GsonToMap(readLine);
        Integer command = GsonUtils.Double2Integer((Double) gsonMap.get(COMMAND));
        HashMap map = new HashMap();
        String username, password;
        switch (command) {
            case COM_GROUP:
                writer.println(getGroup());
                System.out.println(workUser.getUserName() + "请求获得在线用户详情");
                break;
            case COM_SIGNUP:
                username = (String) gsonMap.get(USERNAME);
                password = (String) gsonMap.get(PASSWORD);
                map.put(COMMAND, COM_RESULT);
                if (createUser(username, password)) {
                    //需要马上变更心跳
                    currentTime = new Date().getTime();
                    //存储信息
                    map.put(COM_RESULT, SUCCESS);
                    map.put(COM_DESCRIPTION, "success");
                    writer.println(gson.toJson(map));
                    broadcast(getGroup(),COM_SIGNUP);
                    System.out.println("用户" + username + "注册上线了");
                } else {
                    map.put(COM_RESULT, FAILED);
                    map.put(COM_DESCRIPTION, username + "已经被注册");
                    writer.println(gson.toJson(map)); //返回消息给服务器
                    System.out.println(username + "该用户已经被注册");
                }
                break;
            case COM_LOGIN:
                username = (String) gsonMap.get(USERNAME);
                password = (String) gsonMap.get(PASSWORD);
                boolean find = false;
                for (ServerUser u : users) {
                    if (u.getUserName().equals(username)) {
                        if (!u.getPassword().equals(password)) {
                            map.put(COM_DESCRIPTION, "账号密码输入有误");
                            break;
                        }
                        if (u.getStatus().equals("online")) {
                            map.put(COM_DESCRIPTION, "该用户已经登录");
                            break;
                        }
                        currentTime = new Date().getTime();
                        map.put(COM_RESULT, SUCCESS);
                        map.put(COM_DESCRIPTION, username + "success");
                        u.setStatus("online");
                        writer.println(gson.toJson(map));
                        workUser = u;
                        broadcast(getGroup(), COM_SIGNUP);
                        find = true;
                        System.out.println("用户" + username + "上线了");
                        break;
                    }
                }
                if (!find) {
                    map.put(COM_RESULT, FAILED);
                    if (!map.containsKey(COM_DESCRIPTION))
                        map.put(COM_DESCRIPTION, username + "未注册");
                    writer.println(gson.toJson(map)); //返回消息给服务器
                }
                break;
            case COM_CHATWITH:
                String receiver = (String) gsonMap.get(RECEIVER);
                map = new HashMap();
                map.put(COMMAND, COM_CHATWITH);
                map.put(SPEAKER, gsonMap.get(SPEAKER));
                map.put(RECEIVER, gsonMap.get(RECEIVER));
                map.put(CONTENT, gsonMap.get(CONTENT));
                map.put(TIME, getFormatDate());
                for (ServerUser u : users) {
                    if (u.getUserName().equals(receiver)) {
                        u.addMsg(gson.toJson(map));
                        break;
                    }
                }
                workUser.addMsg(gson.toJson(map));
                break;
            case COM_CHATALL:
                map = new HashMap();
                map.put(COMMAND, COM_CHATALL);
                map.put(SPEAKER, workUser.getUserName());
                map.put(TIME, getFormatDate());
                map.put(CONTENT, gsonMap.get(CONTENT));
                broadcast(gson.toJson(map), COM_MESSAGEALL);
                break;
            default:
//                System.out.println("");
                break;
        }
    }

    /**
     * @return current time the formatDate String
     */
    public String getFormatDate() {
        Date date = new Date();
        long times = date.getTime();//时间戳
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * broadcast the message to all user
     *
     * @param message the message
     * @param type    that contain "message", "logOUt", "signUp"
     */
    private void broadcast(String message, int type) {
        System.out.println(workUser.getUserName() + " 开始广播broadcast " + message);

        switch (type) {
            case COM_MESSAGEALL:
                for (ServerUser u : users) {
                    u.addMsg(message);
                }
                break;
            case COM_LOGOUT:
            case COM_SIGNUP:
                for (ServerUser u : users) {
                    if (!u.getUserName().equals(workUser.getUserName())) {
                        u.addMsg(message);
                    }
                }
                break;
        }

    }

    /**
     * send the message to com.dyleaf.Client
     */
    private void sentMessageToClient() {
        String message;
        if (workUser != null)
            while ((message = workUser.getMsg()) != null) {
                writer.println(message); //write it will  auto flush.
                System.out.println(workUser.getUserName() + "的数据仓发送 message: " + message + "剩余 size" + workUser.session.size());
            }
    }

    /**
     * the  method to release socket's resource.
     */
    private void logOut() {
        if (workUser == null)
            return;
        System.out.println("用户 " + workUser.getUserName() + " 已经离线");
        // still hold this user and change it's status
        workUser.setStatus("offline");
        for (ServerUser u : users) {
            if (u.getUserName().equals(workUser.getUserName()))
                u.setStatus("offline");
        }
        broadcast(getGroup(), COM_LOGOUT);
        isLogOut = true;
    }

    /**
     * get a random name
     *
     * @return
     */
    private String getRandomName() {
        String[] str1 = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8",
                "9", "0", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z"};
        StringBuilder name = new StringBuilder();
        String userName = name.toString();
        Random ran = new Random();
        boolean success = false;
        do {
            for (int i = 0; i < 6; i++) {
                int n = ran.nextInt(str1.length);
                String str = str1[n];
                name.append(str);
                System.out.println(name);
            }
            success = true;
            userName = name.toString();
            for (ServerUser user : users) {
                if (userName.equals(user.getUserName())) {
                    success = false;
                    break;
                }
            }
        } while (!success);
        return userName;
    }

    /**
     * create username and bind userName . if failed it will return failed.
     * if success it will add to users.
     *
     * @param userName
     * @return
     */
    private boolean createUser(String userName, String password) {
        for (ServerUser user : users) {
            if (user.getUserName().equals(userName)) {
                return false;
            }
        }
        //add user to userList
        ServerUser newUser = new ServerUser(users.size(), userName, password);
        newUser.setStatus("online");
        users.add(newUser);
        //  add user to db
        try {
            UserDaoImpl.getInstance().add(newUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        workUser = newUser;
        return true;
    }

    /**
     * return the json of group
     *
     * @return
     */
    private String getGroup() {
        String[] userlist = new String[users.size() * 2];
        int j = 0;
        for (int i = 0; i < users.size(); i++, j++) {
            userlist[j] = users.get(i).getUserName();
            userlist[++j] = users.get(i).getStatus();
        }
        HashMap map = new HashMap();
        map.put(COMMAND, COM_GROUP);
        map.put(COM_GROUP, userlist);
        return gson.toJson(map);
    }
}
