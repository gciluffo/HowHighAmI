package howhighami.com.howhighami;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gciluffo on 3/30/16.
 */

public class GalleryFragment extends Fragment {

    private static final String TAG = "GalleryFragment";
    private static final String IMG_PATHS = "image-paths";
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_PERMISSION_LOCATION_FINE = 111;
    // Request codes for implicit intents
    private static final int REQUEST_PHOTO= 1;
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

    /**
     * The current altitude. This is set in the async task
     */
    private int mCurrentAltitude;

    /**
     * Used to communitate with editor fragment
     */
    private Callbacks mCallbacks;


    private GalleryItem mMostRecentPhoto;
    private FloatingActionButton mFAB;

    public void deleteImage(Uri uri) {
        Log.d(TAG, "delete image called " + uri.toString());
        int targetIndex = getIndexFromURI(uri);

        // remove the image from the list and the recycler view
        mGalleryItems.remove(targetIndex);
        mPhotoAdapter.notifyItemRemoved(targetIndex);
        mPhotoAdapter.notifyItemRangeChanged(targetIndex, mGalleryItems.size());
        mPhotoAdapter.notifyDataSetChanged();

        // delete the image from storage
        getActivity().getContentResolver().delete(uri, null, null);
    }


    public interface Callbacks {
        void sendPicture(String path, int alt, boolean addText);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryItems = new ArrayList<>();

        /**
         * Request permissions for writing to disk and getting location
         */
        boolean hasLocationPermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean hasStoragePermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasStoragePermission) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
        if (!hasLocationPermission) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION_FINE);
        } else {
            // preemptively get location
            new GoogleAltitude().execute();
        }


        /**
         * Restore image paths and GalleryItem Obj's when starting app
         */
        SharedPreferences settings = getActivity().getSharedPreferences(IMG_PATHS, 0);
        mGalleryItems.clear();
        int size = settings.getInt("gallery_size", 0);
        for(int i=0;i<size;i++) {
            GalleryItem item = new GalleryItem();
            Uri uri = Uri.parse(settings.getString("img_uri" + i, null));
            String path = getRealPathFromURI(uri);
            int alt = settings.getInt("img_alt" + i, 0);
            item.setElevation(alt);
            item.setFilePath(path);
            item.setUri(uri);

            File f = new File(item.getFilePath());
            if(f.exists()){
                mGalleryItems.add(item);
            }
        }
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
        // Add spacing to items in recycler view
        mPhotoRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(2));

        mFAB = (FloatingActionButton) view.findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /**
                 * Take the picture, save it to disk, record URI
                 */
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Image File name");
                Uri mCapturedImageURI = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                /**
                 * Create a galleryItem object and add to list of galleryItem to be displayed
                 */
                startActivityForResult(intentPicture, REQUEST_PHOTO);
                mMostRecentPhoto = new GalleryItem();
                mMostRecentPhoto.setFilePath(getRealPathFromURI(mCapturedImageURI));
                mMostRecentPhoto.setUri(mCapturedImageURI);
                Log.d(TAG, "The path from fragment is " + mMostRecentPhoto.getFilePath());

                /**
                 * Run the async task to get elevation
                 */
                new GoogleAltitude().execute();

                /**
                 * Send file path and alt to edit fragment
                 */
                mCallbacks.sendPicture(mMostRecentPhoto.getUri().toString(), mCurrentAltitude, true);
                mGalleryItems.add(mMostRecentPhoto);
            }
        });

        mPhotoAdapter = null;
        setupAdapter();

        return view;
    }

    /**
     * Gets path to image with uri
     * @return
     */
    public String getRealPathFromURI(Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(
                    contentUri,
                    proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }

    /**
     * Takes image uri, returns position of that image in mPhotoAdapter
     * @param imageURI
     * @return the index of image corresponding to the URI, -1 if not found
     */
    private int getIndexFromURI(Uri imageURI) {
        for(int i = 0; i < mGalleryItems.size(); i++) {
            if(mGalleryItems.get(i).getUri().equals(imageURI)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() Called");

        /**
         * Save info of images before closing app
         */
        SharedPreferences settings = getActivity().getSharedPreferences(IMG_PATHS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        editor.putInt("gallery_size", mGalleryItems.size());
        for(int i=0;i<mGalleryItems.size();i++) {
            editor.remove("img_uri" + i);
            editor.putString("img_uri" + i, mGalleryItems.get(i).getUri().toString());
        }

        for(int i=0;i<mGalleryItems.size();i++) {
            editor.remove("img_alt" + i);
            editor.putInt("img_alt" + i, mGalleryItems.get(i).getElevation());
        }

        // Commit the edits!
        editor.commit();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PHOTO && resultCode == getActivity().RESULT_OK) {
            Log.d(TAG, "Successfully got image from camera activity");
            mPhotoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Handles the result of a permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION_FINE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new GoogleAltitude().execute();
                } else {
                    Toast.makeText(getActivity().getParent(), "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    class GoogleAltitude extends AsyncTask<Void, Void, Integer> {

        private boolean mFoundLocation;
        /**
         * Do things to connect to google maps and get elevation from lat/long
         * @param params
         * @return
         */
        // TODO: Check that the user has internet first, else altitude is 0
        protected Integer doInBackground(Void... params) {
            double result = Double.NaN;
            mFoundLocation = false;
            try {
                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                List<String> providers = lm.getProviders(true);
                Location bestLocation = null;
                for (String provider : providers) {
                    Location l = lm.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        bestLocation = l;
                    }
                }
                if(bestLocation != null) {

                    double longitude = bestLocation.getLongitude();
                    double latitude = bestLocation.getLatitude();
                    Log.d(TAG, "LONGITUDE IS " + longitude);
                    Log.d(TAG, "LATITUDE IS " + latitude);

                    mFoundLocation = true;

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpContext localContext = new BasicHttpContext();
                    String url = "http://maps.googleapis.com/maps/api/elevation/"
                            + "xml?locations=" + String.valueOf(latitude)
                            + "," + String.valueOf(longitude)
                            + "&sensor=true";
                    HttpGet httpGet = new HttpGet(url);
                    try {
                        HttpResponse response = httpClient.execute(httpGet, localContext);
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            InputStream instream = entity.getContent();
                            int r = -1;
                            StringBuffer respStr = new StringBuffer();
                            while ((r = instream.read()) != -1)
                                respStr.append((char) r);
                            String tagOpen = "<elevation>";
                            String tagClose = "</elevation>";
                            if (respStr.indexOf(tagOpen) != -1) {
                                int start = respStr.indexOf(tagOpen) + tagOpen.length();
                                int end = respStr.indexOf(tagClose);
                                String value = respStr.substring(start, end);
                                Log.d(TAG, "Elevation result is " + value + " m ");
                                result = (Double.parseDouble(value) * 3.2808399); // convert from meters to feet
                                mCurrentAltitude = (int) result;

                                // update the most recent photo's elevation
                                if(mMostRecentPhoto != null) {
                                    mMostRecentPhoto.setElevation(mCurrentAltitude);
                                }
                            }
                            instream.close();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                } else {
                    Log.d(TAG, "ERROR: Location was null");
//                    Toast.makeText(getActivity().getParent(), "Unable to get location fix. Make sure location in enabled", Toast.LENGTH_LONG).show();
                }
            } catch (SecurityException e) {
                Log.d(TAG, "ERROR: App does not have location permissions.");
            }
            Log.d(TAG, "The Result is " + result);
            return (int)result;
        }

        /**
         * Return the value from doInBackground to fragment
         * @param alt
         */
        @Override
        protected void onPostExecute(Integer alt) {
            Log.d(TAG, "onPostExecute value is " + alt);
            if(!mFoundLocation) {
                Handler handler =  new Handler(getActivity().getMainLooper());
                handler.post( () -> Toast.makeText(getActivity(), "Unable to get location fix; your elevation will show as 0. Make sure location is enabled.", Toast.LENGTH_LONG).show());
            }
            // Set the value
            mCurrentAltitude = alt;
        }
    }

    /*****************************************************************************
     * Everything under this comment is related to displaying the photos on the grid
     *
     *****************************************************************************/
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
     * Class that adds spacing between items in recycler view
     */
    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int mVerticalSpaceHeight;

        public VerticalSpaceItemDecoration(int mVerticalSpaceHeight) {
            this.mVerticalSpaceHeight = mVerticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = mVerticalSpaceHeight;
            // Dont add white space to the last item in the row
            if((parent.getChildPosition(view) + 1) % 3 != 0)
            outRect.right = mVerticalSpaceHeight;

        }
    }

    /**
     * The ViewHolder for our RecyclerView to display Photos
     */
    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            mItemImageView.setOnClickListener(this);
        }

        /**
         * Pull up the fragment to edit it or share when clicking it
         */
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "Image: " + pos);
            GalleryItem item = mGalleryItems.get(pos);
            mCallbacks.sendPicture(item.getUri().toString(), item.getElevation(), false);
        }

        /**
         * Binds a gallery item to this ViewHolder
         *
         * @see GalleryItem
         */
        public void bindDrawable(Bitmap image) {
            // Resize the image for the grid layout
            // TODO: Handle landscape images
            Bitmap resized = PictureUtils.resizeBitmap(image, 160, 320);
            mItemImageView.setImageBitmap(resized);
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

        // Display the picture in the grid
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Bitmap bitmap = PictureUtils.getScaledBitmap(galleryItem.getFilePath(), getActivity());

            photoHolder.bindDrawable(bitmap);
            //Drawable placeholder = getResources().getDrawable(R.drawable.shaq);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

}
