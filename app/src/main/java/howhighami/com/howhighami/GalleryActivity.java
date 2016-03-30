package howhighami.com.howhighami;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;



public class GalleryActivity extends SingleFragmentActivity {

    private static final String TAG = "GalleryActivity";

    @Override
    protected Fragment createFragment() {
        Log.d(TAG, "creatingFragment() called");
        return new GalleryFragment();
    }
}
