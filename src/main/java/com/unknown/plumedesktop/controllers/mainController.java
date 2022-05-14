package com.unknown.plumedesktop.controllers;

import com.unknown.plumedesktop.models.ThumbnailChat;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class mainController {

    @FXML
    private VBox chatlist;

    public void addThumbnailChat(ThumbnailChat c) {
        Node newChatNode = c.getNode();
        if(newChatNode != null)
            chatlist.getChildren().add(newChatNode);
    }

}
