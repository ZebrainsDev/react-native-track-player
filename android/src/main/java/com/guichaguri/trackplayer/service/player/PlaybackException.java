package com.guichaguri.trackplayer.service.player;

import com.facebook.react.bridge.Promise;

public class PlaybackException extends Throwable {

    private String code;
    private String message;


    public PlaybackException(String code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public void reject(Promise promise){
        promise.reject(code, message);
    }
}
