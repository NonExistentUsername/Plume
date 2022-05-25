package com.unknown.plumedesktop.models;

public class UserMessage {
    public String text;
    public boolean isMy;

    public UserMessage(String text, boolean isMy) {
        this.text = text;
        this.isMy = isMy;
    }
}
