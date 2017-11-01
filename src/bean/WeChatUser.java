package bean;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WeChatUser {
    /**
     * define username as userID
     */
    private String userName;

    /**
     * user's message queue
     */
    private Queue<String> session;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public WeChatUser() {
        super();
        //保证线程并发安全
        session = new ConcurrentLinkedQueue();
    }

    public void addMsg(String message){
        session.offer(message);
    }
    public String getMsg(){
        if(session.isEmpty())
            return null;
        return session.poll();
    }
}
