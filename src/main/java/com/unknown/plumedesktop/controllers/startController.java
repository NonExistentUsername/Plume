package com.unknown.plumedesktop.controllers;

import com.unknown.plumedesktop.AuthSceneController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class startController {

    @FXML
    private MFXButton start_messaging;

    @FXML
    void start_messaging_pressed(MouseEvent event) {
        
        AuthSceneController.get().setEnterPhoneNumberScene();
    }

}
