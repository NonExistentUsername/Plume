package com.unknown.plumedesktop.controllers;

import com.unknown.plumedesktop.tdcontroller.TelegramClient;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class enterPasswordController {

    @FXML
    private Label hint;

    @FXML
    private MFXButton next_button;

    @FXML
    private MFXPasswordField password;

    private TelegramClient tc = null;

    @FXML
    void next_released(MouseEvent event) {
        tc.sendPassword(password.getText());
    }

    public void setTelegramClient(TelegramClient tc) {
        this.tc = tc;
    }

    public void setHint(String hint) {
        this.hint.setText(hint);
    }

}
