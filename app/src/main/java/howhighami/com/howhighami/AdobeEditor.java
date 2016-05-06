package howhighami.com.howhighami;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;

import java.io.File;

/**
 * Created by gciluffo on 4/12/16.
 */
public class AdobeEditor extends AppCompatActivity {

    private ImageView mEditedImageView;
    private Uri mUri;
    private Uri mEditedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle b = getIntent().getExtras();
        String uri = b.getString("uri");
        mUri = Uri.parse(uri);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEditedImageView = (ImageView) findViewById(R.id.editedImageView);
        mEditedUri = null;

        /* 2) Create a new Intent */
        Intent imageEditorIntent = new AdobeImageIntent.Builder(this)
                .setData(mUri)
                .build();

        /* 3) Start the Image Editor with request code 1 */
        startActivityForResult(imageEditorIntent, 1);

    }

    /* Handle the results */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                /* Make a case for the request code we passed to startActivityForResult() */
                case 1:

                    /* Show the image! */
                    Uri editedImageUri = data.getData();
                    mEditedImageView.setImageURI(editedImageUri);
                    // update the URI in case the user decides to edit again
                    mEditedUri = editedImageUri;

                    break;
            }
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

        if (id == R.id.delete_edited_photo) {
            if(mEditedUri != null) {
                new File(mEditedUri.getPath()).delete();
            } else {
                new File(mUri.getPath()).delete();
            }
            finish();
        }

        if (id == R.id.edit_again) {
            if(mEditedUri != null) {
                Intent imageEditorIntent = new AdobeImageIntent.Builder(this)
                        .setData(mEditedUri)
                        .build();

                startActivityForResult(imageEditorIntent, 1);
            }
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        finishWithResult();
//    }

//    private void finishWithResult() {
//        if(mEditedUri != null) {
//            // https://stackoverflow.com/questions/2497205/how-to-return-a-result-startactivityforresult-from-a-tabhost-activity
//            Bundle conData = new Bundle();
//            conData.putString("IMG_URI", mEditedUri.toString());
//            Intent intent = new Intent();
//            intent.putExtras(conData);
//            setResult(RESULT_OK, intent);
//        }
//        finish();
//    }
}
