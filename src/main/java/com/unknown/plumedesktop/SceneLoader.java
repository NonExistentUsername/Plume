package com.unknown.plumedesktop;

import com.unknown.plumedesktop.controllers.enterCodeController;
import com.unknown.plumedesktop.controllers.enterPasswordController;
import com.unknown.plumedesktop.controllers.enterPhoneController;
import com.unknown.plumedesktop.controllers.MainController;
import com.unknown.plumedesktop.models.ThumbnailChat;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class SceneLoader {
    private final int width = 600;
    private final int height = 370;
    private SceneAndController main = null;
    private SceneAndController start = null;
    private SceneAndController enterPhoneNumber = null;
    private SceneAndController enterCode = null;
    private SceneAndController enterPassword = null;

    static private class SceneAndController {
        public Scene scene;
        public Object controller;
        public SceneAndController(Scene scene, Object controller) {
            this.scene = scene;
            this.controller = controller;
        }
    };

    private SceneAndController load(String fxml_filename) {
        FXMLLoader fxmlLoader = new FXMLLoader(PlumeApplication.class.getResource(fxml_filename));
        try {
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            Object controller = fxmlLoader.getController();
            return new SceneAndController(scene, controller);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
            return null;
        }
    }

    private void load_enterCode() {
        enterCode = this.load("enterCode.fxml");
        assert enterCode != null;
        ((enterCodeController)enterCode.controller).setTelegramClient(PlumeApplication.tc);
    }

    private void load_enterPhoneNumber() {
        enterPhoneNumber = this.load("enterPhoneNumber.fxml");
        assert enterPhoneNumber != null;
        ((enterPhoneController)enterPhoneNumber.controller).setTelegramClient(PlumeApplication.tc);
    }

    private void load_enterPassword() {
        enterPassword = load("enterPassword.fxml");
        assert enterPassword != null;
        ((enterPasswordController)enterPassword.controller).setTelegramClient(PlumeApplication.tc);
    }

    private void load_main() {
        main = load("main.fxml");
        assert main != null;
    }

    private void load_all() {
        start = this.load("startPage.fxml");
        this.load_enterPhoneNumber();
        this.load_enterCode();
        this.load_enterPassword();
        this.load_main();
    }

    public SceneLoader(boolean loadAll) {
        if(loadAll) {
            this.load_all();
        }
    }

    public Scene getStartScene() {
        if(start == null) {
            start = load("startPage.fxml");
        }
        assert start != null;
        return start.scene;
    }

    public Scene getEnterPhoneNumberScene() {
        if(enterPhoneNumber == null) {
            load_enterPhoneNumber();
        }
        assert enterPhoneNumber != null;
        return enterPhoneNumber.scene;
    }

    public Scene getEntreCodeScene() {
        if(enterCode == null) {
            load_enterCode();
        }
        assert enterCode != null;
        return enterCode.scene;
    }

    public Scene getEnterPasswordScene() {
        if(enterPassword == null) {
            load_enterPassword();
        }
        assert enterPassword != null;
        return enterPassword.scene;
    }

    public Object getEnterPasswordController() {
        if(enterPassword == null) {
            load_enterPassword();
        }
        assert enterPassword != null;
        return enterPassword.controller;
    }

    public Scene getMainScene() {
        if(main == null) {
            load_main();
        }
        assert main != null;
        return main.scene;
    }

    public MainController getMainController() {
        if(main == null) {
            load_main();
        }
        assert main != null;
        return (MainController) main.controller;
    }
}