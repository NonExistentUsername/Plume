package com.unknown.plumedesktop;

public class AuthSceneController {
    private static AuthSceneController sceneController = null;

    private AuthSceneLoader authSceneLoader = null;

    private AuthSceneController() {
        this.authSceneLoader = new AuthSceneLoader(true);
    }

    static public AuthSceneController get() {
        if(sceneController == null) sceneController = new AuthSceneController();
        return sceneController;
    }

    public void setLoginScene() {
        PlumeApplication.changeScene(authSceneLoader.getLoginScene());
    }

    public void setEnterCodeScene() {
        PlumeApplication.changeScene(authSceneLoader.getEntreCodeScene());
    }
}
