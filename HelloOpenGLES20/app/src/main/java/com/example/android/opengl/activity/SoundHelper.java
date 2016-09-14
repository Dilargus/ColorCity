package com.example.android.opengl.activity;

import android.media.AudioManager;
import android.media.SoundPool;

import com.example.android.opengl.R;
import com.example.android.opengl.SessionData;

/**
 * Created by Woess on 14.09.2016.
 */
public class SoundHelper {
    private OpenGLES20Activity act;

    public SoundHelper(OpenGLES20Activity activity) {
        this.act = activity;
    }
    protected void initSound(){
        act.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        SessionData.instance().sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool sounds, int sampleId, int status) {
                SessionData.instance().sound_loaded = true;
            }
        });
        SessionData.instance().crate_destroy1 = SessionData.instance().sounds.load(act, R.raw.crate_destroy1, 1);
        SessionData.instance().crate_destroy2 = SessionData.instance().sounds.load(act, R.raw.crate_destroy2, 1);
        SessionData.instance().crate_hit1 = SessionData.instance().sounds.load(act, R.raw.crate_hit1, 1);
        SessionData.instance().crate_hit2 = SessionData.instance().sounds.load(act, R.raw.crate_hit2, 1);
        SessionData.instance().star_hit = SessionData.instance().sounds.load(act, R.raw.star_hit, 1);
    }
}
