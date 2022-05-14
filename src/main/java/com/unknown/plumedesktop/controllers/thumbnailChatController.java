package com.unknown.plumedesktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class thumbnailChatController {

    @FXML
    private Label chatName;

    public void setChatName(String name) {
        chatName.setText(name);
    }
}
