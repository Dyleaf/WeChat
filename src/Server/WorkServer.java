package Server;

import bean.WeChatUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

/**
 * deal with the thing of its user
 */
public class WorkServer implements Runnable {

    private WeChatUser workUser; //被托管的用户

    private String userName; //user id

    private Socket socket;

    private ArrayList<WeChatUser> users;

    private BufferedReader bufferIn;
    private boolean isLogOut = false;
    private int MESSAGE = 0;
    private int LOGOUT = 1;

    public WorkServer(Socket socket, ArrayList users) {
        super();
        this.socket = socket; //bind socket
        this.users = users;
    }

    @Override
    public void run() {
        //todo server's work
        try {
            bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                //heart check
                Thread.sleep(500);
                String readLine;
                // check weather the user is logOut
                if ((readLine = bufferIn.readLine()) == null) {
                    logOut();
                }
                handleMessage(readLine);
                if (isLogOut) {
                    bufferIn.close(); // kill the I/O stream
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logOut();
        } catch (SocketException e) {
            e.printStackTrace();
            logOut();
        } catch (IOException e) {
            e.printStackTrace();
            logOut();
        }
    }

    /**
     * the message to deal with client's command
     *
     * @param readLine
     */
    private void handleMessage(String readLine) {
        //split the commmandpr
        //todo deal message with 正则表达式
        //String[] messages = readLine.split(",");

    }

    /**
     * send message to special user
     *
     * @param userName other user
     * @param message
     */
    private void sendmessageTo(String userName, String message) {
        for (WeChatUser user : users) {
            if (user.getUserName().equals(userName)) {
                user.addMsg(message);
                break;
            }
        }
    }

    /**
     * broadcast the message to all user
     *
     * @param message the message
     * @param type    that contain "message", "logOUt", "signUp"
     */
    private void broadcast(String message, int type) {
        for (WeChatUser user : users) {
            if (!user.getUserName().equals(userName)) {
                switch (type) {
                    case MESSAGE:
                    case LOGOUT:
                }
            }
            user.addMsg(message);
        }
    }

    /**
     * the  method to release socket's resource.
     */
    private void logOut() {
        users.remove(workUser);
        workUser = null;
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
            for (WeChatUser user : users) {
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
     *
     * @param userName
     * @return
     */
    private boolean createUserName(String userName) {
        for (WeChatUser user : users) {
            if (user.getUserName().equals(userName))
                return false;
        }
        //add user to userList
        WeChatUser newUser = new WeChatUser();
        newUser.setUserName(userName);
        users.add(newUser);
        this.userName = userName;
        return true;
    }

}
