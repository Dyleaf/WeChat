package com.dyleaf.Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

/**
 * 数据库操作工具类
 * @author dyleaf
 *
 */
public class DbUtils {

    //数据库连接地址
    public static String URL;
    //用户名
    public static String USERNAME;
    //密码
    public static String PASSWORD;
    //mysql的驱动类
    public static String DRIVER;
    //获取配置信息的内容
//    private static ResourceBundle rb;

    private DbUtils(){}

    //使用静态块加载驱动程序
    static{
       /* rb= ResourceBundle.getBundle("F:\\WeChat\\src\\com\\db-config.properties");
        URL = rb.getString("jdbc.url");
        USERNAME = rb.getString("jdbc.username");
        PASSWORD = rb.getString("jdbc.password");
        DRIVER = rb.getString("jdbc.driver");
        */
        URL = "jdbc:sqlserver://localhost:1433;database=dyleaf";
        USERNAME = "dyleafidea";
        PASSWORD ="2389231";
        DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        try {
            Class.forName(DRIVER);
            System.out.println("DBUTILs success");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("DBUTILs error");
        }
    }

    //定义一个获取数据库连接的方法
    public static Connection getConnection(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("获取连接失败");
        }
        return conn;
    }

    /**
     * 关闭数据库连接
     * @param rs
     * @param stat
     * @param conn
     */
    public static void close(ResultSet rs,Statement stat,Connection conn){
        try {
            if(rs!=null)rs.close();
            if(stat!=null)stat.close();
            if(conn!=null)conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
