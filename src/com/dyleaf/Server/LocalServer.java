package com.dyleaf.Server;

public class LocalServer {

    public static void main(String[] args){
        MasterServer masterServer = new MasterServer();
        masterServer.start();
    }
}
