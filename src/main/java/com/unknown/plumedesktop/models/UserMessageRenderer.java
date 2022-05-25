package com.unknown.plumedesktop.models;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Function;


public class UserMessageRenderer implements Callback<ListView<UserMessage>, ListCell<UserMessage>> {
    @Override
    public ListCell<UserMessage> call(ListView<UserMessage> p) {

        ListCell<UserMessage> cell = new ListCell<UserMessage>(){
            {
                setMaxWidth(Control.USE_PREF_SIZE - 50);
            }
            @Override
            protected void updateItem(UserMessage message, boolean bln) {
                super.updateItem(message, bln);
                setGraphic(null);
                setText(null);
                if (message != null) {
                    HBox hBox = new HBox();

                    Text name = new Text(message.text);

                    hBox.getChildren().add(name);
                    if(!message.isMy)
                        hBox.setAlignment(Pos.CENTER_LEFT);
                    else
                        hBox.setAlignment(Pos.CENTER_RIGHT);

                    setGraphic(hBox);
                }
            }
        };
        return cell;
    }
}

