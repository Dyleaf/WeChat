package com.dyleaf.Dao;

import com.dyleaf.bean.ServerUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PersonDao的具体实现类
 * @author dyleaf
 */
public class UserDaoImpl implements UserDao {

    private UserDaoImpl() {
        super();
    }
    private  static UserDaoImpl userDao;
    public static UserDaoImpl getInstance(){
        if(userDao==null){
            synchronized (UserDaoImpl.class){
                if(userDao==null){
                    userDao = new UserDaoImpl();
                }
            }
        }
        return userDao;
    }

    /**
     * 实现添加方法
     */
    @Override
    public void add(ServerUser p) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "insert into ChatUser(id,name,password)values(?,?,?)";
//        String sql = "insert into person(name,age,description)values(?,?,?)";
        try{
            conn = DbUtils.getConnection();
            System.out.println("get connect");
            ps = conn.prepareStatement(sql);
            ps.setInt(1, p.getId());
            ps.setString(2, p.getUserName());
            ps.setString(3, p.getPassword());
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
            System.out.println("add error");
            throw new SQLException("添加数据失败");
        }finally{
            DbUtils.close(null, ps, conn);
        }
    }

    /**
     * 更新方法
     */
    @Override
    public void update(ServerUser p) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "update ChatUser set name=?,age=?,password=? where id=?";

        try{
            conn = DbUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, p.getId());
            ps.setString(2, p.getUserName());
            ps.setString(3, p.getPassword());
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
            throw new SQLException("更新数据失败");
        }finally{
            DbUtils.close(null, ps, conn);
        }
    }

    /**
     * 删除方法
     */
    @Override
    public void delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "delete from ChatUser where id=?";
        try{
            conn = DbUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1,id);
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
            throw new SQLException(" 删除数据失败");
        }finally{
            DbUtils.close(null, ps, conn);
        }
    }

    /**
     * 根据ID查询一个对象
     */
    @Override
    public ServerUser findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ServerUser p = null;
        String sql = "select name,password from ChatUser where id=?";
        try{
            conn = DbUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if(rs.next()){
                p = new ServerUser();
                p.setId(id);
                p.setUserName(rs.getString(1));
                p.setPassword(rs.getString(2));
            }
        }catch(SQLException e){
            e.printStackTrace();
            throw new SQLException("根据ID查询数据失败");
        }finally{
            DbUtils.close(rs, ps, conn);
        }
        return p;
    }

    @Override
    public ServerUser findByName(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ServerUser p = null;
        String sql = "select id,password from ChatUser where name=?";
        try{
            conn = DbUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1,username );
            rs = ps.executeQuery();
            if(rs.next()){
                p = new ServerUser(rs.getInt(1),username,rs.getString(2));
            }
        }catch(SQLException e){
            e.printStackTrace();
            throw new SQLException("根据ID查询数据失败");
        }finally{
            DbUtils.close(rs, ps, conn);
        }
        return p;
    }

    /**
     * 查询所有数据
     */
    @Override
    public List<ServerUser> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ServerUser p = null;
        List<ServerUser> persons = new ArrayList<ServerUser>();
        String sql = "select id,name,password from ChatUser";
        try{
            conn = DbUtils.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                p = new ServerUser(rs.getInt(1),rs.getString(2),rs.getString(3));
                persons.add(p);
            }
        }catch(SQLException e){
            e.printStackTrace();
            throw new SQLException("查询所有数据失败");
        }finally{
            DbUtils.close(rs, ps, conn);
        }
        return persons;
    }

}
