package com.unknown.plumedesktop.controllers;

import com.unknown.plumedesktop.tdcontroller.TelegramClient;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class enterCodeController {

    @FXML
    private MFXTextField code;

    @FXML
    private MFXButton next_button;

    private TelegramClient tc = null;

    public void setTelegramClient(TelegramClient tc) {
        this.tc = tc;
    }

    @FXML
    void next_released(MouseEvent event) {
        if(this.tc != null)
            this.tc.sendCode(code.getText());
    }

}
