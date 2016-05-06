package howhighami.com.howhighami;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.adobe.creativesdk.aviary.AdobeImageIntent;

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
            // TODO: delete edited image and return to unedited image
        }

        if (id == R.id.edit_again) {
            // TODO: open edited image in adobe image editor
            if(mEditedUri != null) {
                Intent imageEditorIntent = new AdobeImageIntent.Builder(this)
                        .setData(mEditedUri)
                        .build();

                startActivityForResult(imageEditorIntent, 1);
            }
        }

        if (id == R.id.save_edited) {
            // TODO: save edited image
        }

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
