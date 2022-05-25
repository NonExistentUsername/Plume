package com.unknown.plumedesktop;

import com.unknown.plumedesktop.controllers.enterPasswordController;
import javafx.scene.Scene;

public class AuthSceneController {
    private static AuthSceneController sceneController = null;
//    private final Lock lock = new ReentrantLock();

    private SceneLoader authSceneLoader = null;

    private AuthSceneController() {
        this.authSceneLoader = new SceneLoader(false);
    }

    static public AuthSceneController get() {
        if(sceneController == null) sceneController = new AuthSceneController();
        return sceneController;
    }

    public void setEnterPhoneNumberScene() {
        PlumeApplication.changeScene(authSceneLoader.getEnterPhoneNumberScene());
    }
    public void setEnterCodeScene() {
        PlumeApplication.changeScene(authSceneLoader.getEntreCodeScene());
    }
    public void setEnterPasswordScene(String hint) {
        Scene sc = authSceneLoader.getEnterPasswordScene();
        enterPasswordController epc = (enterPasswordController) authSceneLoader.getEnterPasswordController();
        epc.setHint(hint);
        PlumeApplication.changeScene(sc);
    }
    public void setMainScene() {
        PlumeApplication.tc.setMainController(authSceneLoader.getMainController());
        AutoMainUIUpdater autoMainUIUpdater = new AutoMainUIUpdater(authSceneLoader.getMainController());
        PlumeApplication.changeScene(authSceneLoader.getMainScene());
        autoMainUIUpdater.setDaemon(true);
        autoMainUIUpdater.setPriority(3);
        autoMainUIUpdater.start();
    }
    public void setStartScene() {
        PlumeApplication.changeScene(authSceneLoader.getStartScene());
    }
}
