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

//    static class ResultHandler implements Client.ResultHandler {
//        @Override
//        public void onResult(TdApi.Object object) {
//            switch (object.getConstructor()) {
//                case TdApi.Error.CONSTRUCTOR -> {
//                    System.err.println("Receive an error:\n" + object);
//                    onResult(null); // repeat last action
//                }
//                case TdApi.Ok.CONSTRUCTOR -> System.err.println("Receive an OK\n");
//                default -> System.err.println("Receive wrong response from TDLib:\n" + object);
//            }
//        }
//    }

    private final ObservableComponent observableComponent = new ObservableComponent();
    private TdApi.AuthorizationState authState = null;
    private final TelegramClient tc;

    public AuthUpdateHandler(TelegramClient tc) {
        this.tc = tc;
    }

    public String getHint() {
        if(this.authState.getConstructor() == TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR)
            return ((TdApi.AuthorizationStateWaitPassword)this.authState).passwordHint;
        return "";
    }

    private void mySend(TdApi.Function query) {
        tc.send(
                query,
                tc.new RetryResultHandler(query));
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
                mySend(new TdApi.SetTdlibParameters(tc.getTdlibParameters()));
                System.err.println("TDLib params sent");
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                mySend(new TdApi.CheckDatabaseEncryptionKey());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                this.observableComponent.notify("auth wait for number");
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                this.observableComponent.notify("auth wait for code");
                break;
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR:
                this.observableComponent.notify("auth wait for password");
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                this.observableComponent.notify("authorized");
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                this.observableComponent.notify("logging out");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                this.observableComponent.notify("closed");
                break;
            default:
                System.out.println("AuthUpdateHandler Ignored: ");
                System.out.println(object);
                break;
        }
    }
}
