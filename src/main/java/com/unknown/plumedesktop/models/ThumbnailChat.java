package com.unknown.plumedesktop.models;

import com.unknown.plumedesktop.PlumeApplication;
import com.unknown.plumedesktop.controllers.MainController;
import com.unknown.plumedesktop.controllers.thumbnailChatController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class ThumbnailChat {
    public String title;
    public String lastMessage;
    public Long chatId;

    public ThumbnailChat(String title, String lastMessage, Long chatId) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
    }

    public Node getNode(MainController mainController) {
        try {
            FXMLLoader loader = new FXMLLoader(PlumeApplication.class.getResource("thumbnailChat.fxml"));
            Node result = loader.load();
            ((thumbnailChatController) loader.getController()).setTitle(title);
            ((thumbnailChatController) loader.getController()).setLastMessage(lastMessage);
            ((thumbnailChatController) loader.getController()).setChatId(chatId);
            ((thumbnailChatController) loader.getController()).setMainController(mainController);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
