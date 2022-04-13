package com.unknown.plumedesktop;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class enterPhoneController {

    @FXML
    private MFXButton next_button;

    @FXML
    private MFXTextField phone_number;

    @FXML
    void next_released(MouseEvent event) {
        PlumeApplication.sendPhoneNumber(phone_number.getText());
    }

}
