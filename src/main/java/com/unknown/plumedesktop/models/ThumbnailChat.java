package com.unknown.plumedesktop.models;

import com.unknown.plumedesktop.PlumeApplication;
import com.unknown.plumedesktop.controllers.thumbnailChatController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class ThumbnailChat {
    private String name;

    public ThumbnailChat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getNode() {
        try {
            FXMLLoader loader = new FXMLLoader(PlumeApplication.class.getResource("thumbnailChat.fxml"));
            Node result = loader.load();
            ((thumbnailChatController) loader.getController()).setChatName(name);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
