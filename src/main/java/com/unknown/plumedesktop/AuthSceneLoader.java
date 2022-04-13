package com.unknown.plumedesktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class AuthSceneLoader {
    private final int width = 600;
    private final int height = 370;
    private Scene startScene = null;
    private Scene loginScene = null;
    private Scene enterCodeScene = null;

    private Scene load(String fxml_filename) {
        FXMLLoader fxmlLoader = new FXMLLoader(PlumeApplication.class.getResource(fxml_filename));
        try {
            return new  Scene(fxmlLoader.load(), width, height);
        } catch (IOException e) {
            return null;
        }
    }

    private void load_all() {
        startScene = this.load("startPage.fxml");
        loginScene = this.load("enterPhoneNumber.fxml");
        enterCodeScene = this.load("enterCode.fxml");
    }

    public AuthSceneLoader(boolean loadAll) {
        if(loadAll) {
            this.load_all();
        }
    }

    public Scene getStartScene() {
        if(startScene == null) {
            startScene = load("startPage.fxml");
        }
        return startScene;
    }

    public Scene getLoginScene() {
        if(loginScene == null) {
            loginScene = load("enterPhoneNumber.fxml");
        }
        return loginScene;
    }

    public Scene getEntreCodeScene() {
        if(enterCodeScene == null) {
            enterCodeScene = load("enterCode.fxml");
        }
        return enterCodeScene;
    }
}