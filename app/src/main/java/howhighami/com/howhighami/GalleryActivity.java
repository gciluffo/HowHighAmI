package howhighami.com.howhighami;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;



public class GalleryActivity extends SingleFragmentActivity implements GalleryFragment.Callbacks {

    private static final String TAG = "GalleryActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        Log.d(TAG, "creatingFragment() called");
        return new GalleryFragment();
    }


    @Override
    public void sendPicture(String path) {

        // Start the photo editing fragment
        // Create fragment and give it an argument for the selected article
        PhotoEditorFragment newFragment = new PhotoEditorFragment();
        Bundle args = new Bundle();
        args.putString(PhotoEditorFragment.PICTURE_ID, path);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
