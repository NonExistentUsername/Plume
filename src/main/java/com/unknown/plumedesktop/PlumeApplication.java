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
    private static AuthSceneLoader loginScenesLoader = null;
    private static TelegramClient tc = null;

    @Override
    public void start(Stage _stage) throws IOException {
        loginScenesLoader = new AuthSceneLoader(true);
        stage = _stage;
        stage.setTitle("Plume");
        stage.setScene(loginScenesLoader.getStartScene());
        stage.show();

        tc = new TelegramClient();
        tc.create(new UpdateHandler(new AuthUpdateHandler(tc)), new TDParams(false));
    }

    static public void changeScene(Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}