package eu.makeitapp.meetup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import eu.makeitapp.mkbaas.core.Log;
import eu.makeitapp.mkbaas.core.MKAppInstance;
import eu.makeitapp.mkbaas.core.MKError;
import eu.makeitapp.mkbaas.core.MKLoginActivity;
import eu.makeitapp.mkbaas.core.MKPushExtension;
import eu.makeitapp.mkbaas.core.MKSessionListener;
import eu.makeitapp.mkbaas.core.MKUser;
import eu.makeitapp.mkbaas.core.MKUserExtension;
import retrofit.RestAdapter;


/**
 * ${PROJECT}
 * <p/>
 * Created by Federico Oldrini (federico.oldrini@makeitapp.eu) on 09/04/2015.
 */
public class MTPWelcomeActivity extends Activity {
    private static final String KEY__USERNAME = "KEY__USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Sdk entrypoint
        MKAppInstance.sharedInstance().init(this, getString(R.string.mk_secret), getString(R.string.mk_api_endpoint), new MKUserExtension(), new MKPushExtension(getString(R.string.mk_gcm_sender_id)));
        MKAppInstance.sharedInstance().setHttpLogLevel(RestAdapter.LogLevel.FULL);
        MKAppInstance.sharedInstance().setLogLevel(Log.MKLogLevel.INFO);

        //Define custom push receiver
        MKPushExtension.getInstance().setCustomPushIntentServiceClass(MTPPushIntentService.class);

        //Try to restore a saved user session
        MKUserExtension.restoreUserSession(this, new MKSessionListener() {
            @Override
            public void onRestore(MKUser mkUser, MKError mkError) {
                //Error is null, all si fine, user founded!
                if (mkError == null) {
                    //Do logged stuff
                    onUserLogged(mkUser);
                }
                //Error not null, no user founded, launch Login activity flow
                else {
                    MKLoginActivity.startActivityForResult(MTPWelcomeActivity.this, false);
                }
            }
        }, true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Intercept the login activity result
        MKLoginActivity.onActivityResult(this, requestCode, resultCode, data, true);

        //Create user from login activity result
        MKUser user = MKUser.createFromIntent(data);

        //Do logged stuff
        onUserLogged(user);
    }

    private void onUserLogged(MKUser user) {
        PreferenceManager.getDefaultSharedPreferences(MTPWelcomeActivity.this).edit().putString(KEY__USERNAME, user.getUsername()).commit();

        //Start main chat activity
        startActivity(new Intent(MTPWelcomeActivity.this, MTPMainActivity.class));
        finish();
    }
}
