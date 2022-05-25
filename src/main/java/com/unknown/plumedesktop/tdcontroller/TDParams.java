package com.unknown.plumedesktop.tdcontroller;

import org.drinkless.tdlib.TdApi;

import java.io.File;
import java.util.Scanner;

public class TDParams implements ITDParams {
    private boolean is_test_config = false;
    private int apiId = 0;
    private String apiHash = "";

    private void loadConig() {
        try {
            File file = new File("config");

            Scanner sc = new Scanner(file);

            this.apiId = Integer.parseInt(sc.nextLine());
            this.apiHash = sc.nextLine();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        System.out.println("loaded");
        System.out.println(this.apiId);
        System.out.println(this.apiHash);
    }

    public TDParams(boolean test_config) {
        this.is_test_config = test_config;
        loadConig();
    }

    @Override
    public TdApi.TdlibParameters getParameters() {
        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
        parameters.databaseDirectory = "tdlib";
        parameters.useMessageDatabase = true;
        parameters.useSecretChats = false;
        parameters.apiId = this.apiId;
        parameters.apiHash = this.apiHash;
        parameters.systemLanguageCode = "en";
        parameters.deviceModel = "i love Angelinka <3";
        parameters.applicationVersion = "1.0";
        parameters.enableStorageOptimizer = true;
        parameters.useTestDc = is_test_config;
        return parameters;
    }
}
