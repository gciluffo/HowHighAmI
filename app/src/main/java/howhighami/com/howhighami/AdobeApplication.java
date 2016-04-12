package howhighami.com.howhighami;

/**
 * Created by gciluffo on 4/12/16.
 */

import android.app.Application;

import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;

/**
 * Created by ash on 3/11/16.
 */
public class AdobeApplication extends Application implements IAviaryClientCredentials {

    /* Be sure to fill in the two strings below. */
    private static final String CREATIVE_SDK_CLIENT_ID = "e264b6e98fa4459fbdf91e556b45a9e5";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "ae2eb369-6677-4dab-bba3-36d365bf252d";

    @Override
    public void onCreate() {
        super.onCreate();
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());
    }

    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }

    @Override
    public String getBillingKey() {
        return ""; // Leave this blank
    }
}
