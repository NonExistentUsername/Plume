package com.unknown.plumedesktop;

import com.unknown.plumedesktop.tdcontroller.AuthUpdateHandler;
import com.unknown.plumedesktop.tools.IObserver;
import javafx.application.Platform;

public class AutoSceneChanger implements IObserver {

    private AuthUpdateHandler auth_update_handler = null;

    public AutoSceneChanger(AuthUpdateHandler auth_update_handler) {
        this.auth_update_handler = auth_update_handler;
    }

    @Override
    public void notify(String message) {
        System.out.println("AutoSceneChanger.notify");
        System.out.println(message);
        if(message.equals("auth wait for code"))
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AuthSceneController.get().setEnterCodeScene();
                }
            });
        else
        if(message.equals("auth wait for password")) {
            String hint = this.auth_update_handler.getHint();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AuthSceneController.get().setEnterPasswordScene(hint);
                }
            });
        } else
        if(message.equals("authorized")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AuthSceneController.get().setMainScene();
                }
            });
        } else
        if(message.equals("closed")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AuthSceneController.get().setStartScene();
                }
            });
        }
    }
}
