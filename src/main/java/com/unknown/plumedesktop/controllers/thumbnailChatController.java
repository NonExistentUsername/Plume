package com.unknown.plumedesktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class thumbnailChatController {

    @FXML
    private Label chatTitle;

    @FXML
    private Label lastMessage;

    private Long chatId;
    private MainController mainController;

    public void setMainController(MainController mainController) { this.mainController = mainController; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setTitle(String title) {
        chatTitle.setText(title);
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage.setText(lastMessage);
    }

    @FXML
    void chatSelected(MouseEvent event) {
        mainController.selectChat(chatId);
    }
}
