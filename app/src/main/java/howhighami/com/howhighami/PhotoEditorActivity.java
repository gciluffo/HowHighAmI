package howhighami.com.howhighami;


import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by gciluffo on 4/9/16.
 */
public class PhotoEditorActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoEditorActivity";

    @Override
    protected Fragment createFragment() {
        Log.d(TAG, "creatingFragment() called");
        return new PhotoEditorFragment();
    }

}
