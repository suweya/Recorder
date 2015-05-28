package net.suweya.recorder;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.suweya.recorder.widget.AudioRecorderButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private RecordAdapter mAdapter;

    private View mAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);

        AudioRecorderButton button = (AudioRecorderButton) findViewById(R.id.btn_record);
        button.setAudioFinishRecordListener(new AudioRecorderButton.AudioFinishRecordListener() {
            @Override
            public void onAudioFinish(float duration, String filePath) {
                setAdapter(new Record(duration, filePath));
            }

            @Override
            public void onShowDialog() {
                MediaManager.stop();
                if (mAnimView != null) {
                    mAnimView.setBackgroundResource(R.drawable.adj);
                    mAnimView = null;
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mAnimView != null) {
                    mAnimView.setBackgroundResource(R.drawable.adj);
                    mAnimView = null;
                }

                mAnimView = view.findViewById(R.id.record_anim);
                mAnimView.setBackgroundResource(R.drawable.play_anim);
                ((AnimationDrawable)mAnimView.getBackground()).start();

                ((RecordAdapter)parent.getAdapter()).setCurrentPlayItem(position);
                MediaManager.playSound(((Record)parent.getAdapter().getItem(position)).filePath, new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mAnimView.setBackgroundResource(R.drawable.adj);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onDestroy() {
        MediaManager.release();
        super.onDestroy();
    }

    protected void setAdapter(Record record) {
        if (mAdapter == null) {
            List<Record> datas = new ArrayList<>();
            datas.add(record);
            mAdapter = new RecordAdapter(this, datas);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.add(record);
            mListView.setSelection(mAdapter.getCount());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
