package com.unknown.plumedesktop;

import com.unknown.plumedesktop.controllers.MainController;
import com.unknown.plumedesktop.models.ThumbnailChat;
import javafx.application.Platform;
import javafx.scene.Node;

import java.util.ArrayList;
import javafx.concurrent.Task;

public class AutoMainUIUpdater extends Thread {
    private int timeLimitMilliseconds = 1000;
    private MainController mainController = null;

    public AutoMainUIUpdater(MainController mainController) {
        this.mainController = mainController;
    }

    public AutoMainUIUpdater(int timeLimitMilliseconds, MainController mainController) {
        this.timeLimitMilliseconds = timeLimitMilliseconds;
        this.mainController = mainController;
    }

    private void updchats() {
        Task<ArrayList<Node>> updateChatList = new Task<>() {

            @Override
            protected ArrayList<Node> call() throws Exception {
                ArrayList<Node> nodes = new ArrayList<>();
                ArrayList<ThumbnailChat> chats = PlumeApplication.tc.getChatList();
                chats.forEach(chat -> {
                    Node newNode = chat.getNode(mainController);
                    nodes.add(newNode);
                });
                return nodes;
            }
        };

        updateChatList.setOnSucceeded(event -> mainController.setThumbnailChats(updateChatList.getValue()));

        Thread t = new Thread(updateChatList);
        t.setDaemon(true);
        t.start();
    }

    public void run() {
        updchats();

        while(PlumeApplication.tc.isLogggedIn()) {
            try {
                Thread.sleep(this.timeLimitMilliseconds);
            } catch (InterruptedException e) {}

            if(PlumeApplication.tc.resetIsModifiedChatList()) {
                updchats();
            }
        }
        System.out.println("Auto ui updater killed");
    }
}
