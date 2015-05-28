package net.suweya.recorder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by suweya on 2015/5/28.
 */
public class RecordAdapter extends ArrayAdapter<Record> {

    private int mMinWidth, mMaxWidth;
    private int mCurrentPlayItem = -1;

    public RecordAdapter(Context context, List<Record> objects) {
        super(context, -1, objects);

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mMinWidth = (int) (screenWidth * 0.15F);
        mMaxWidth = (int) (screenWidth * 0.7F);
    }

    @DebugLog
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
            holder = new ViewHolder();
            holder.duration = (TextView) convertView.findViewById(R.id.tv_record_time);
            holder.length = convertView.findViewById(R.id.fl_record_length);
            holder.animView = convertView.findViewById(R.id.record_anim);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.duration.setText(Math.round(getItem(position).duration) + "\"");
        ViewGroup.LayoutParams params = holder.length.getLayoutParams();
        params.width = (int) (mMinWidth + (mMaxWidth / 60F * getItem(position).duration));
        if (mCurrentPlayItem == position) {
            holder.animView.setBackgroundResource(R.drawable.play_anim);
            ((AnimationDrawable)holder.animView.getBackground()).start();
        } else {
            holder.animView.setBackgroundResource(R.drawable.adj);
        }

        return convertView;
    }

    public void setCurrentPlayItem(int currentPlayItem) {
        this.mCurrentPlayItem = currentPlayItem;
    }

    static class ViewHolder {
        TextView duration;
        View length;
        View animView;
    }
}
