package com.unknown.plumedesktop;

import com.unknown.plumedesktop.tdcontroller.AuthUpdateHandler;
import com.unknown.plumedesktop.tdcontroller.TDParams;
import com.unknown.plumedesktop.tdcontroller.TelegramClient;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PlumeApplication extends Application {
    private static Stage stage = null;
    private static SceneLoader loginScenesLoader = null;
    public static TelegramClient tc = null;

    private static AutoSceneChanger autoSceneChanger = null;
    public static AuthUpdateHandler authUpdateHandler = null;

    @Override
    public void start(Stage _stage) throws IOException {
        loginScenesLoader = new SceneLoader(true);
        stage = _stage;
        stage.setTitle("Plume");
        stage.setScene(loginScenesLoader.getStartScene());
        stage.show();

        tc = new TelegramClient();
        authUpdateHandler = new AuthUpdateHandler(tc);
        authUpdateHandler.addObserver(tc);
        tc.create(authUpdateHandler, new TDParams(true));

        autoSceneChanger = new AutoSceneChanger(authUpdateHandler);
        authUpdateHandler.addObserver(autoSceneChanger);
    }

    static public void changeScene(Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}