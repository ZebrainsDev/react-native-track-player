package com.guichaguri.trackplayer.service.metadata;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.facebook.react.bridge.ReadableMap;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.MusicService;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;
import com.guichaguri.trackplayer.service.player.PlaybackException;

import java.util.List;

/**
 * @author Guichaguri
 */
public class ButtonEvents extends MediaSessionCompat.Callback {

    private final MusicService service;
    private final MusicManager manager;

    public ButtonEvents(MusicService service, MusicManager manager) {
        this.service = service;
        this.manager = manager;
    }

    @Override
    public void onPlay() {
        manager.getPlayback().play();
        service.emit(MusicEvents.BUTTON_PLAY, null);
    }

    @Override
    public void onPause() {
        manager.getPlayback().pause();
        service.emit(MusicEvents.BUTTON_PAUSE, null);
    }

    @Override
    public void onStop() {
        manager.getPlayback().stop();
        service.emit(MusicEvents.BUTTON_STOP, null);
    }


    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        Bundle bundle = new Bundle();
        bundle.putString("id", mediaId);
        service.emit(MusicEvents.BUTTON_PLAY_FROM_ID, bundle);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);

        if(extras.containsKey(MediaStore.EXTRA_MEDIA_FOCUS)) {
            String focus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);

            if(MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "artist";
            } else if(MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "album";
            } else if(MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "playlist";
            } else if(MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "genre";
            } else if(MediaStore.Audio.Media.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "title";
            }

            bundle.putString("focus", focus);
        }

        if(extras.containsKey(MediaStore.EXTRA_MEDIA_TITLE))
            bundle.putString("title", extras.getString(MediaStore.EXTRA_MEDIA_TITLE));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_ARTIST))
            bundle.putString("artist", extras.getString(MediaStore.EXTRA_MEDIA_ARTIST));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_ALBUM))
            bundle.putString("album", extras.getString(MediaStore.EXTRA_MEDIA_ALBUM));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_GENRE))
            bundle.putString("genre", extras.getString(MediaStore.EXTRA_MEDIA_GENRE));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_PLAYLIST))
            bundle.putString("playlist", extras.getString(MediaStore.EXTRA_MEDIA_PLAYLIST));

        service.emit(MusicEvents.BUTTON_PLAY_FROM_SEARCH, bundle);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        List<Track> tracks = manager.getPlayback().getQueue();


        for(Track track : tracks) {
            if(track.queueId != id) continue;

            try {
                ExoPlayback playback =  manager.getPlayback();
                playback.skip(track.id);
                Bundle bundle = playback.getSkipOptionsInBundle();
                service.emit(MusicEvents.BUTTON_SKIP, bundle);
            } catch (PlaybackException ignored){
            }

            break;
        }
    }

    @Override
    public void onSkipToPrevious() {
        try {
            ExoPlayback playback =  manager.getPlayback();
            playback.skipToPrevious();
            Bundle bundle = playback.getSkipOptionsInBundle();
            service.emit(MusicEvents.BUTTON_SKIP_PREVIOUS, bundle);
        } catch (PlaybackException ignored){
        }

    }

    @Override
    public void onSkipToNext() {
        try {
            ExoPlayback playback =  manager.getPlayback();
            playback.skipToNext();
            Bundle bundle = playback.getSkipOptionsInBundle();
            service.emit(MusicEvents.BUTTON_SKIP_NEXT, bundle);
        } catch (PlaybackException ignored){
        }
    }

    @Override
    public void onRewind() {
        Bundle bundle = new Bundle();
        bundle.putInt("interval", manager.getMetadata().getJumpInterval());
        service.emit(MusicEvents.BUTTON_JUMP_BACKWARD, bundle);
    }

    @Override
    public void onFastForward() {
        Bundle bundle = new Bundle();
        bundle.putInt("interval", manager.getMetadata().getJumpInterval());
        service.emit(MusicEvents.BUTTON_JUMP_FORWARD, bundle);
    }

    @Override
    public void onSeekTo(long pos) {
        ExoPlayback playback =  manager.getPlayback();
        playback.seekTo(pos);

        Bundle bundle = new Bundle();
        bundle.putDouble("position", Utils.toSeconds(pos));
        service.emit(MusicEvents.BUTTON_SEEK_TO, bundle);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        Bundle bundle = new Bundle();
        Utils.setRating(bundle, "rating", rating);
        service.emit(MusicEvents.BUTTON_SET_RATING, bundle);
    }
}
