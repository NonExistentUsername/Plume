package com.unknown.plumedesktop.tdcontroller;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class UpdateHandler implements Client.ResultHandler {
    private final Client.ResultHandler authUpdateHandler;

    public UpdateHandler(Client.ResultHandler authUpdateHandler) {
        this.authUpdateHandler = authUpdateHandler;
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch(object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                this.authUpdateHandler.onResult(object);
                break;
            default:
                System.out.println("UpdateHandler ignored");
//                System.out.println(object);
                break;
        }
    }
}
