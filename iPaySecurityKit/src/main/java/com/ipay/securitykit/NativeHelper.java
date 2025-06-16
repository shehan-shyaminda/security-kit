package com.ipay.securitykit;


import android.content.res.AssetManager;

public class NativeHelper {

    static {
        System.loadLibrary("native-lib");
    }

    public native boolean checkFridaServer();
    public native boolean checkObjectionInjected();
    public native boolean checkMagiskMounted();
    //    public native boolean isApktoolArtifactPresent(AssetManager assetManager);
    public native boolean nativeIsEmulator();
}