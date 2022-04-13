package com.unknown.plumedesktop.tdcontroller;

import com.unknown.plumedesktop.tools.IObservable;
import com.unknown.plumedesktop.tools.ObservableComponent;
import com.unknown.plumedesktop.tools.IObserver;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CEHA implements Client.ExceptionHandler {

    @Override
    public void onException(Throwable e) {
        System.out.println(e.getMessage());
        System.out.println("CEHA.onException updateExceptionHandler");
    }

}

class CEHB implements Client.ExceptionHandler {

    @Override
    public void onException(Throwable e) {
        System.out.println(e.getMessage());
        System.out.println("CEHB.onException defaultExceptionHandler");
    }

}

public class TelegramClient implements IObservable, IObserver {
    @Override
    public void notify(String message) {

    }

    private class RetryResultHandler implements Client.ResultHandler {
        private final TdApi.Function query;

        public RetryResultHandler(TdApi.Function query) {
            this.query = query;
        }

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    TdApi.Error error = (TdApi.Error) object;
                    System.err.println("Receive an error:\n" + object);
                    client.send(query, this);
                }
                case TdApi.Ok.CONSTRUCTOR -> System.err.println("Receive an OK\n");
                default -> System.err.println("Receive wrong response from TDLib:\n" + object);
            }
        }
    }

    private final  ObservableComponent observable_component = new ObservableComponent();

    private Client client;
    private final Lock lock = new ReentrantLock();

    private ITDParams params;
    private Client.ResultHandler updateHandler;

    static {
        try {
            System.loadLibrary("libcrypto-1_1-x64");
            System.loadLibrary("libssl-1_1-x64");
            System.loadLibrary("zlib1");
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }
    public TelegramClient() {}

    @Override
    public void addObserver(IObserver observer) {
        observable_component.addObserver(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observable_component.removeObserver(observer);
    }

    public void create(Client.ResultHandler updateHandler, ITDParams params) {
        this.params = params;
        this.updateHandler = updateHandler;

        Client.execute(new TdApi.SetLogVerbosityLevel(5));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }
        this.client = Client.create(updateHandler, new CEHA(), new CEHB());
    }

    public TdApi.TdlibParameters getTdlibParameters() {
        return this.params.getParameters();
    }

    public void sendNumber(String number) {
        TdApi.Function query = new TdApi.SetAuthenticationPhoneNumber(number, null);
        client.send(
                query,
                new TelegramClient.RetryResultHandler(query));
    }

    public void waitForNumber() {
        observable_component.notify("wait for number");
    }

    public void send(TdApi.Function query, Client.ResultHandler resultHandler) {
        lock.lock();
        client.send(
                query,
                resultHandler);
        lock.unlock();
    }
}