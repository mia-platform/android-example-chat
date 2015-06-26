package eu.makeitapp.meetup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import eu.makeitapp.mkbaas.MKAppInstance;
import eu.makeitapp.mkbaas.MKError;
import eu.makeitapp.mkbaas.MKLoginActivity;
import eu.makeitapp.mkbaas.MKUser;
import eu.makeitapp.mkbaas.listener.MKSessionListener;

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
        MKAppInstance.sharedInstance().init(this, getString(R.string.mk_secret), getString(R.string.mk_api_endpoint));

        //Init pish component
        MKAppInstance.sharedInstance().initPush(this, getString(R.string.mk_gcm_sender_id));

        //Define custom push receiver
        MKAppInstance.sharedInstance().setCustomPushIntentServiceClass(MTPPushIntentService.class);

        //Try to restore a saved user session
        MKAppInstance.sharedInstance().restoreUserSession(this, new MKSessionListener() {
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
