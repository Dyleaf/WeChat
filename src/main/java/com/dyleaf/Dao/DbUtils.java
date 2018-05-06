package com.dyleaf.Dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
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
    private static Properties properties;

    private DbUtils(){}

    //使用静态块加载驱动程序
    static{
        try {
            InputStream input = Class.forName(DbUtils.class.getName()).getResourceAsStream("/db-config.properties");
            properties = new Properties();
            properties.load(input);
            URL = properties.getProperty("jdbc.url");
            USERNAME = properties.getProperty("jdbc.username");
            PASSWORD = properties.getProperty("jdbc.password");
            DRIVER = properties.getProperty("jdbc.driver");
            Class.forName(DRIVER);
            System.out.println(URL);
            System.out.println("DBUTILs success");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("DBUTILs error");
        } catch (IOException e) {
            e.printStackTrace();
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
