package com.unknown.plumedesktop.controllers;

import com.unknown.plumedesktop.PlumeApplication;
import com.unknown.plumedesktop.models.ThumbnailChat;
import com.unknown.plumedesktop.models.UserMessageRenderer;
import io.github.palexdev.materialfx.controls.MFXNotificationCenter;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.drinkless.tdlib.TdApi;

import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    @FXML
    private MFXScrollPane chatScroll;

    @FXML
    private VBox chatlist;

    @FXML
    private MFXTextField inputText;

    @FXML
    private Label currentChatTitle;

    @FXML
    private ListView<com.unknown.plumedesktop.models.UserMessage> messages;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.messages.setCellFactory(new UserMessageRenderer());
    }

    @FXML
    void sendKeyReleased(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            String message = inputText.getText();
            if(!message.isEmpty()) {
                this.inputText.clear();
                PlumeApplication.tc.sendMessage(message);
            }
        }
    }

    @FXML
    void exitButtonReleased(MouseEvent event) {
        PlumeApplication.tc.logout();
    }

    @FXML
    void sendMessage(MouseEvent event) {
        String message = inputText.getText();
        if(!message.isEmpty()) {
            this.inputText.clear();
            PlumeApplication.tc.sendMessage(message);
        }
    }

    public void selectChat(Long chatId) {
        PlumeApplication.tc.setCurrentChatId(chatId);
        TdApi.Chat chat = PlumeApplication.tc.getChat(chatId);
        currentChatTitle.setText(chat.title);
        PlumeApplication.tc.getChatMessageList(chatId);
    }

    private Node loadUserMessage(String text) {
        try {
            FXMLLoader loader = new FXMLLoader(PlumeApplication.class.getResource("userMessage.fxml"));
            Node result = loader.load();
            ((UserMessage) loader.getController()).setText(text);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addChatHistory(ArrayList<TdApi.Message> ms, int totalCount) {
        System.out.println("addChatHistory called");
        ArrayList<com.unknown.plumedesktop.models.UserMessage> messages__ = new ArrayList<>();
        for(int i = 0; i < ms.size(); ++i) {
            String ms_text = "";
            TdApi.Message msi = ms.get(i);
            if(msi.content instanceof TdApi.MessageText)
                ms_text = ((TdApi.MessageText)msi.content).text.text;

            boolean ismy = false;
            if(msi.senderId instanceof TdApi.MessageSenderUser)
                ismy = (((TdApi.MessageSenderUser)msi.senderId).userId == PlumeApplication.tc.getMyId());
            messages__.add(new com.unknown.plumedesktop.models.UserMessage(ms_text, ismy));
        }
        Collections.reverse(messages__);
        this.messages.getItems().setAll(messages__);
        this.messages.scrollTo(this.messages.getItems().size());
    }

    public void setThumbnailChats(ArrayList<Node> nodes) {
        chatlist.setVisible(false);
        double cur_pos = chatScroll.getVvalue();
        chatlist.getChildren().setAll(nodes);
        chatScroll.setVvalue(Math.min(chatScroll.getVmax(), cur_pos));
        chatlist.setVisible(true);
    }

}
