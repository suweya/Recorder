package net.suweya.recorder;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by suweya on 2015/5/27.
 */
public class DialogManager {

    private Dialog mDialog;

    private ImageView mIvIcon, mIvVoiceLevel;
    private TextView mTvLabel;

    private Context mContext;

    public DialogManager(Context mContext) {
        this.mContext = mContext;
    }

    public void showRecordingDialog() {
        //Init
        mDialog = new Dialog(mContext, R.style.Theme_AudioDialog);
        mDialog.setContentView(R.layout.dialog_recorder);
        mIvIcon = (ImageView) mDialog.findViewById(R.id.iv_dialog_icon);
        mIvVoiceLevel = (ImageView) mDialog.findViewById(R.id.iv_dialog_voice_level);
        mTvLabel = (TextView) mDialog.findViewById(R.id.tv_dialog_label);

        mDialog.show();
    }

    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            mIvIcon.setVisibility(View.VISIBLE);
            mIvVoiceLevel.setVisibility(View.VISIBLE);
            mTvLabel.setVisibility(View.VISIBLE);

            mIvIcon.setImageResource(R.drawable.recorder);
            mTvLabel.setText(R.string.str_recorder_slide_up_to_cancel);
        }
    }

    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            mIvIcon.setVisibility(View.VISIBLE);
            mIvVoiceLevel.setVisibility(View.GONE);
            mTvLabel.setVisibility(View.VISIBLE);

            mIvIcon.setImageResource(R.drawable.cancel);
            mTvLabel.setText(R.string.str_recorder_want_cancel);
        }
    }

    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            mIvIcon.setVisibility(View.VISIBLE);
            mIvVoiceLevel.setVisibility(View.GONE);
            mTvLabel.setVisibility(View.VISIBLE);

            mIvIcon.setImageResource(R.drawable.voice_to_short);
            mTvLabel.setText(R.string.str_recorder_too_short);
        }
    }

    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
            /*mIvIcon.setVisibility(View.VISIBLE);
            mIvVoiceLevel.setVisibility(View.VISIBLE);
            mTvLabel.setVisibility(View.VISIBLE);*/

            int levelResId = mContext.getResources()
                    .getIdentifier("v" + level, "drawable", mContext.getPackageName());
            mIvVoiceLevel.setImageResource(levelResId);
        }
    }
}
