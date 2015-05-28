package net.suweya.recorder;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by suweya on 2015/5/27.
 */
public class AudioManager {

    public interface AudioStateListener {
        void wellPrepare();
    }

    private MediaRecorder mRecorder;
    private String mDir;
    private String mCurrentFilePath;

    private static AudioManager mInstance;
    private AudioStateListener mAudioStateListener;

    /**
     * 是否开始录音
     */
    private boolean isPrepare;

    public AudioManager(String dir) {
        this.mDir = dir;
    }

    public static AudioManager getInstance(String dir) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);
                }
            }
        }
        return mInstance;
    }

    public void prepareAudio() {
        try {
            isPrepare = false;
            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = generateFileName();
            File file = new File(dir, fileName);

            mCurrentFilePath = file.getAbsolutePath();

            mRecorder = new MediaRecorder();
            //设置音频源为麦克风
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //API < 10 RAW_AMR  > 10 ARM_NB
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //设置录音文件输出路径
            mRecorder.setOutputFile(mCurrentFilePath);

            mRecorder.prepare();
            mRecorder.start();
            isPrepare = true;
            //通知Button
            if (mAudioStateListener != null) {
                mAudioStateListener.wellPrepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateFileName() {
        return UUID.randomUUID().toString() + ".amr";
    }

    public int getVoiceLevel(int maxLevel) {
        if (isPrepare) {
            try {
                int value = maxLevel * mRecorder.getMaxAmplitude()/32768 + 1;
                return value;  //getMaxAmplitude 1~32767
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    public void release() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        //mCurrentFilePath = null;
    }

    public void cancel() {
        release();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }

    public void setAudioStateListener(AudioStateListener listener) {
        this.mAudioStateListener = listener;
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

}

