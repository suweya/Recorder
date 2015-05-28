package net.suweya.recorder;

/**
 * Created by suweya on 2015/5/28.
 */
public class Record {

    public float duration;

    public String filePath;

    public Record(float duration, String filePath) {
        this.filePath = filePath;
        this.duration = duration;
    }
}
