package howhighami.com.howhighami;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Created by gciluffo on 3/30/16.
 */
public class GalleryItem {

    private int elevation;
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private String filePath;
    private Context mAppContext;
    private Uri uri;


    public GalleryItem() {
        // Generate unique identifier
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public int  getElevation() {
        return elevation;
    }

    public UUID getmId() {
        return mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public Date getmDate() {
        return mDate;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    // For writing photos to storage
    public String getPhotoFilename() {
        return "IMG_" + getmId().toString() + ".jpg";
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }
}
