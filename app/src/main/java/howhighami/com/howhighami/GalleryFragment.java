package howhighami.com.howhighami;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gciluffo on 3/30/16.
 */


public class GalleryFragment extends Fragment {

    private static final String TAG = "GalleryFragment";

    /**
     * Our RecyclerView object to display the photos
     */
    private RecyclerView mPhotoRecyclerView;
    /**
    * The Adapter for our RecyclerView
     */
    private PhotoAdapter mPhotoAdapter;
    /**
     * The current list of photos to be displaying
     */
    private List<GalleryItem> mGalleryItems;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // TODO: implement a galleryItem object
        mGalleryItems = new ArrayList<>();
    }

    /**
     * Called when the view is created.  Gets a reference to our recycler view
     *
     * @param inflater           our layout inflater
     * @param container          the parent container
     * @param savedInstanceState a state from a saved previous run
     * @return the created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mPhotoAdapter = null;

        return view;
    }


    // Initialize the menu from the res/menu directory
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.gallery_menu, menu);
    }

    // Handle options being selected in title bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.take_picture:
                // TODO: Set up camera to take picture, save to disk or database?
                return true;
            case R.id.share:
                // TODO: Set up sharing photo and elevation data to facebook, twitter etc.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Sets up our RecyclerView Adapter if the Fragment has been attached to an
     * activity
     */
    private void setupAdapter() {
        Log.d(TAG, "setupAdapter() called");

        // check if the fragment is added to an activity
        if (isAdded()) {
            // set the adapter
            Log.d(TAG, "Created a new adapter");
            mPhotoAdapter = new PhotoAdapter(mGalleryItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        } else if (!isAdded()) {
            Log.d(TAG, "fragment not added to activity yet");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated( Bundle ) called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    /**
     * The ViewHolder for our RecyclerView to display Photos
     */
    private class PhotoHolder extends RecyclerView.ViewHolder {
        /**
         * A textview to display the photo caption
         */
        private ImageView mItemImageView;

        /**
         * Creates a new ViewHolder
         *
         * @param itemView currently must be a text view
         */
        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
        }

        /**
         * Binds a gallery item to this ViewHolder
         *
         * @see GalleryItem
         */
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    /**
     * The Adapter for our RecyclerView
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        /**
         * The list of photos to display
         */
        private List<GalleryItem> mGalleryItems;

        /**
         * Create a new adapter with a given set of photos
         *
         * @param galleryItems the photos to display
         */
        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            //Drawable placeholder = getResources().getDrawable(R.drawable.shaq);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}
