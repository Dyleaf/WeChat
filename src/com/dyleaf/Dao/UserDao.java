package com.dyleaf.Dao;

import com.dyleaf.bean.ServerUser;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    //添加方法
    public void add(ServerUser p)throws SQLException;

    //更新方法
    public void update(ServerUser p)throws SQLException;

    //删除方法
    public void delete(int id)throws SQLException;

    //查找方法
    public ServerUser findById(int id)throws SQLException;

    public ServerUser findByName(String username)throws SQLException;

    //查找所有
    public List<ServerUser> findAll()throws SQLException;

}