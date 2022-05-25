package com.unknown.plumedesktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class UserMessage {

    @FXML
    private TextArea text;

    public void setText(String text) {
        this.text.setText(text);
    }
}
