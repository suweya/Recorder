package net.suweya.recorder.widget;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import net.suweya.recorder.AudioManager;
import net.suweya.recorder.DialogManager;
import net.suweya.recorder.R;
import net.suweya.recorder.Utils.SizeUtils;

/**
 * Created by suweya on 2015/5/27.
 */
public class AudioRecorderButton extends Button implements AudioManager.AudioStateListener {

    public interface AudioFinishRecordListener {
        void onAudioFinish(float duration, String filePath);

        void onShowDialog();
    }

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    /**
     * TouchMove to cancel distance
     */
    private static final int CANCEL_DISTANCE = SizeUtils.dpToPx(50);
    /**
     * 音量最大数值
     */
    public static final int VOICE_MAX_LEVEL = 7;

    /**
     * 按钮当前状态
     */
    private int mCurrentState = STATE_NORMAL;
    /**
     * 是否正在录音
     */
    private boolean mIsRecording = false;
    /**
     * 是否已经触发了Long Click
     */
    private boolean mReady = false;

    private DialogManager mDialogmanager;

    private AudioManager mAudioManager;

    private AudioFinishRecordListener mAudioFinishRecordListener;

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDialogmanager = new DialogManager(getContext());

        String dir = Environment.getExternalStorageDirectory() + "/VOICE_MSG";
        mAudioManager = AudioManager.getInstance(dir);
        mAudioManager.setAudioStateListener(this);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAudioManager.prepareAudio();
                mReady = true;
                return false;
            }
        });
    }

    public void setAudioFinishRecordListener(AudioFinishRecordListener listener) {
        this.mAudioFinishRecordListener = listener;
    }

    private static final int MSG_AUDIO_PREPARED = 0x100;
    private static final int MSG_VOICE_CHANGED = 0x101;
    private static final int MSG_DIALOG_DISMISS = 0x102;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    if (mAudioFinishRecordListener != null) {
                        mAudioFinishRecordListener.onShowDialog();
                    }
                    mDialogmanager.showRecordingDialog();
                    mIsRecording = true;
                    //启动一个Thread定时的获取音量大小，并在线程中计算录音时长
                    new Thread(mVoiceLevelUpdateRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    mDialogmanager.updateVoiceLevel(mAudioManager.getVoiceLevel(VOICE_MAX_LEVEL));
                    break;
                case MSG_DIALOG_DISMISS:
                    mDialogmanager.dismissDialog();
                    break;
            }
        }
    };

    //当前录音时长
    private float mRecordDuration;

    private Runnable mVoiceLevelUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            while (mIsRecording) {
                try {
                    Thread.sleep(100);
                    mRecordDuration += 0.1F;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void wellPrepare() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeSatet(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsRecording) {
                    //根据X、Y坐标判断是否要取消
                    if (wantToCancel(x, y)) {
                        changeSatet(STATE_WANT_TO_CANCEL);
                    } else {
                        changeSatet(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //1.还没有进入LongClick的时候就UP了，只需要清除相应的标志位
                Log.d("Duration", "mReady -> " + mReady);
                if (mReady) {
                    Log.d("Duration", "mIsRecording -> " + mIsRecording + " mRecordDuration -> " + mRecordDuration);
                    //2.进入了LongClick，但是录音时间过短、或AudioRecorder还没准备好 (2
                    // 秒以上)
                    if (!mIsRecording || mRecordDuration < 1F) {

                        mDialogmanager.tooShort();
                        mAudioManager.cancel();
                        mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1500);

                    } else if (mCurrentState == STATE_RECORDING) {

                        //正常录制结束
                        mDialogmanager.dismissDialog();
                        mIsRecording = false;
                        if (mAudioFinishRecordListener != null) {
                            mAudioFinishRecordListener.onAudioFinish(mRecordDuration,
                                    mAudioManager.getCurrentFilePath());
                        }
                        mAudioManager.release();

                    } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                        //取消录制
                        mDialogmanager.dismissDialog();
                        mAudioManager.cancel();
                    }
                }
                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态标志位
     */
    private void reset() {
        mIsRecording = false;
        mReady = false;
        //重置录音时间
        mRecordDuration = 0;
        changeSatet(STATE_NORMAL);
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -CANCEL_DISTANCE || y > getHeight() + CANCEL_DISTANCE) {
            return true;
        }
        return false;
    }

    private void changeSatet(int state) {

        if (mCurrentState != state) {
            mCurrentState = state;
            switch (state) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recoder_normal);
                    setText(R.string.str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_recording);
                    if (mIsRecording) {
                        mDialogmanager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_want_cancel);
                    mDialogmanager.wantToCancel();
                    break;
            }
        }
    }

}
