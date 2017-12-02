package com.dyleaf.Client.chatroom;


import com.dyleaf.Client.MainApp;
import com.dyleaf.Client.emojis.EmojiDisplayer;
import com.dyleaf.Client.model.ClientModel;
import com.dyleaf.Client.stage.ControlledStage;
import com.dyleaf.Client.stage.StageController;
import com.dyleaf.bean.ClientUser;
import com.dyleaf.bean.Message;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static com.dyleaf.Utils.Constants.*;
import static com.dyleaf.Utils.Constants.CONTENT;

public class MainView implements ControlledStage, Initializable {

    @FXML
    public Button btnEmoji;
    @FXML
    public TextArea textSend;
    @FXML
    public Button btnSend;
    @FXML
    public ListView chatWindow;
    @FXML
    public ListView userGroup;
    @FXML
    public Label labUserName;
    @FXML
    public Label labChatTip;
    @FXML
    public Label labUserCoumter;

    private Gson gson = new Gson();
    private StageController stageController;
    private ClientModel model;
    private static MainView instance;
    private boolean pattern = GROUP; //chat model
    private String seletUser = "[group]";
    private static String thisUser;
    private ObservableList<ClientUser> uselist;
    private ObservableList<Message> chatReccder;

    public MainView() {
        super();
        instance = this;
    }

    public static MainView getInstance() {
        return instance;
    }

    @Override
    public void setStageController(StageController stageController) {
        this.stageController = stageController;
        ;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        model = ClientModel.getInstance();
        uselist = model.getUserList();
        chatReccder = model.getChatRecoder();
        userGroup.setItems(uselist);
        chatWindow.setItems(chatReccder);
        thisUser = model.getThisUser();
        labUserName.setText("Welcome " + model.getThisUser() + "!");
        btnSend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (pattern == GROUP) {
                    HashMap map = new HashMap();
                    map.put(COMMAND, COM_CHATALL);
                    map.put(CONTENT, textSend.getText().trim());
                    model.sentMessage(gson.toJson(map));
                } else if (pattern == SINGLE) {
                    HashMap map = new HashMap();
                    map.put(COMMAND, COM_CHATWITH);
                    map.put(RECEIVER, seletUser);
                    map.put(SPEAKER, model.getThisUser());
                    map.put(CONTENT, textSend.getText().trim());
                    model.sentMessage(gson.toJson(map));
                }
                textSend.setText("");
            }
        });

        userGroup.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ClientUser user = (ClientUser) newValue;
            System.out.println("You are selecting " + user.getUserName());
            if (user.getUserName().equals("[group]")) {
                pattern = GROUP;
                if (!seletUser.equals("[group]")) {
                    model.setChatUser("[group]");
                    seletUser = "[group]";
                    labChatTip.setText("Group Chat");
                }
            } else {
                pattern = SINGLE;
                if (!seletUser.equals(user.getUserName())) {
                    model.setChatUser(user.getUserName());
                    seletUser = user.getUserName();
                    labChatTip.setText("Chatting with " + seletUser);
                    // TODO: 2017/11/29
                }
            }
        });

        chatWindow.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ChatCell();
            }
        });

        userGroup.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new UserCell();
            }
        });
    }


    @FXML
    public void onEmojiBtnClcked() {
        stageController.loadStage(MainApp.EmojiSelectorID, MainApp.EmojiSelectorRes);
        stageController.setStage(MainApp.EmojiSelectorID);
    }

    public TextArea getMessageBoxTextArea() {
        return textSend;
    }

    public Label getLabUserCoumter() {
        return labUserCoumter;
    }


    public static class UserCell extends ListCell<ClientUser> {
        @Override
        protected void updateItem(ClientUser item, boolean empty) {
            super.updateItem(item, empty);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (item != null) {
                        HBox hbox = new HBox();
                        ImageView imageHead = new ImageView(new Image("com/image/head.png"));
                        imageHead.setFitHeight(20);
                        imageHead.setFitWidth(20);
                        ClientUser user = (ClientUser) item;
                        ImageView imageStatus;
                        if(user.getUserName().equals("[group]")){
                            imageStatus = new ImageView(new Image("com/image/online.png"));
                        } else if(user.isNotify()==true){
                            imageStatus = new ImageView(new Image("com/image/message.png"));
                        }else {
                            if(user.getStatus().equals("online")){
                                imageStatus = new ImageView(new Image("com/image/online.png"));
                            }else{
                                imageStatus = new ImageView(new Image("com/image/offline.png"));
                            }
                        }
                        imageStatus.setFitWidth(20);
                        imageStatus.setFitHeight(20);
                        Label label = new Label(user.getUserName());
                        hbox.getChildren().addAll(imageHead, label,imageStatus);
                        setGraphic(hbox);
                    } else {
                        setGraphic(null);
                    }
                }
            });
        }
    }

    public static class ChatCell extends ListCell<Message> {
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //inorder to avoid the
                    if (item != null) {
                        VBox box = new VBox();
                        HBox hbox = new HBox();
                        TextFlow txtContent = new TextFlow(EmojiDisplayer.createEmojiAndTextNode(item.getContent()));
                        Label labUser = new Label(item.getSpeaker() + "[" + item.getTimer() + "]");
                        labUser.setStyle("-fx-background-color: #7bc5cd; -fx-text-fill: white;");
                        ImageView image = new ImageView(new Image("com/image/head.png"));
                        image.setFitHeight(20);
                        image.setFitWidth(20);
                        hbox.getChildren().addAll(image, labUser);
                        if (item.getSpeaker().equals(thisUser)) {
                            txtContent.setTextAlignment(TextAlignment.RIGHT);
                            hbox.setAlignment(Pos.CENTER_RIGHT);
                            box.setAlignment(Pos.CENTER_RIGHT);
                        }
                        box.getChildren().addAll(hbox, txtContent);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            });
        }
    }

}
