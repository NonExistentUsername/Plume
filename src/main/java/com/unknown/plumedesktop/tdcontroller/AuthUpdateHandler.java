package com.unknown.plumedesktop.tdcontroller;

import com.unknown.plumedesktop.tools.IObservable;
import com.unknown.plumedesktop.tools.IObserver;
import com.unknown.plumedesktop.tools.ObservableComponent;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;


public class AuthUpdateHandler implements Client.ResultHandler, IObservable {
    @Override
    public void addObserver(IObserver observer) {
        observableComponent.addObserver(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observableComponent.removeObserver(observer);
    }

    class ResultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    System.err.println("Receive an error:\n" + object);
                    onResult(null); // repeat last action
                }
                case TdApi.Ok.CONSTRUCTOR -> System.err.println("Receive an OK\n");
                default -> System.err.println("Receive wrong response from TDLib:\n" + object);
            }
        }
    }

    private final ObservableComponent observableComponent = new ObservableComponent();
    private TdApi.AuthorizationState authState = null;
    private final TelegramClient tc;

    public AuthUpdateHandler(TelegramClient tc) {
        this.tc = tc;
    }

    @Override
    public void onResult(TdApi.Object object) {
        if(object != null) {
            if(object instanceof TdApi.UpdateAuthorizationState)
                this.authState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
            else
                return;
        }

        switch(this.authState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                tc.send(
                    new TdApi.SetTdlibParameters(tc.getTdlibParameters()),
                    new AuthUpdateHandler.ResultHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                tc.send(
                        new TdApi.CheckDatabaseEncryptionKey(),
                        new AuthUpdateHandler.ResultHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                this.observableComponent.notify("auth wait for number");
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                this.observableComponent.notify("auth wait for code");
                break;
            default:
                System.out.println("AuthUpdateHandler Ignored: ");
                System.out.println(object);
                break;
        }
    }
}
