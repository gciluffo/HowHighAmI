package howhighami.com.howhighami;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class GalleryActivity extends SingleFragmentActivity implements GalleryFragment.Callbacks{

    private static final String TAG = "GalleryActivity";

    private GalleryFragment mGalleryFragment;

    @Override
    protected Fragment createFragment() {
        mGalleryFragment = new GalleryFragment();
        return mGalleryFragment;
    }

    @Override
    public void sendPicture(String path, int alt, boolean addText) {

        // Start the photo editing fragment
        // Create fragment and give it an argument for the selected article
        PhotoEditorFragment newFragment = new PhotoEditorFragment();
        Bundle args = new Bundle();
        args.putString(PhotoEditorFragment.PICTURE_ID, path);
        args.putInt(PhotoEditorFragment.PICTURE_ALT, alt);
        args.putBoolean(PhotoEditorFragment.PICTURE_ADD, addText);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    public void notifyDelete(Uri uri) {
        Log.d(TAG, "notifyDelete() called");
        mGalleryFragment.deleteImage(uri);
    }
}
