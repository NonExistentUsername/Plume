package com.unknown.plumedesktop;

import com.unknown.plumedesktop.tdcontroller.AuthUpdateHandler;
import com.unknown.plumedesktop.tdcontroller.TDParams;
import com.unknown.plumedesktop.tdcontroller.TelegramClient;
import com.unknown.plumedesktop.tdcontroller.UpdateHandler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PlumeApplication extends Application {
    private static Stage stage = null;
    private static SceneLoader loginScenesLoader = null;
    public static TelegramClient tc = null;

    private static AutoSceneChanger autoSceneChanger = null;
    public static AuthUpdateHandler autoUpdateHandler = null;

    @Override
    public void start(Stage _stage) throws IOException {
        loginScenesLoader = new SceneLoader(true);
        stage = _stage;
        stage.setTitle("Plume");
        stage.setScene(loginScenesLoader.getStartScene());
        stage.show();

        tc = new TelegramClient();
        autoUpdateHandler = new AuthUpdateHandler(tc);
        tc.create(new UpdateHandler(autoUpdateHandler), new TDParams(false));

        autoSceneChanger = new AutoSceneChanger(autoUpdateHandler);
        autoUpdateHandler.addObserver(autoSceneChanger);
    }

    static public void changeScene(Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}