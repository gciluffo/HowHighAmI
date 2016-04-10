package howhighami.com.howhighami;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
