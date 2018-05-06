package com.dyleaf.Client;

import com.dyleaf.Client.stage.StageController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
    public static String mainViewID = "MainView";
    public static String mainViewRes = "/MainView.fxml";

    public static String loginViewID = "LoginView";
    public static String loginViewRes = "/LoginView.fxml";

    public static String EmojiSelectorID = "EmojiSelectorUI";
    public static String EmojiSelectorRes = "/EmojiSelectorUI.fxml";

    private StageController stageController;



    @Override
    public void start(Stage primaryStage) {
        //新建一个StageController控制器
        stageController = new StageController();

        //将主舞台交给控制器处理
        stageController.setPrimaryStage("primaryStage", primaryStage);

        //加载两个舞台，每个界面一个舞台
        stageController.loadStage(loginViewID, loginViewRes, StageStyle.UNDECORATED);

        //显示MainView舞台
        stageController.setStage(loginViewID);
    }


    public static void main(String[] args) {
        launch(args);
    }
}