package eu.makeitapp.meetup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import eu.makeitapp.meetup.adapter.MTPMessageAdapter;
import eu.makeitapp.meetup.model.MTPMessage;
import eu.makeitapp.mkbaas.MKCollection;
import eu.makeitapp.mkbaas.MKCollectionFile;
import eu.makeitapp.mkbaas.MKError;
import eu.makeitapp.mkbaas.MKQuery;
import eu.makeitapp.mkbaas.listener.MKCallback;


public class MTPMainActivity extends AppCompatActivity implements View.OnClickListener, MKCallback {
    private static final String ACTION_PUSH = "PUSH";

    private static final String PREFS_KEY__USERNAME = "KEY__USERNAME";
    private static final String MESSAGE_COLLECTION_NAME = "MeetupMessage";
    private static final String KEY__MESSAGE_TEXT = "messageText";
    private static final String KEY__MESSAGE_CREATOR = "messageCreatorName";
    private static final String KEY__MESSAGE_ATTACHMENT = "messageAttachment";

    private static final int CAMERA_REQUEST_CODE = 2403;


    private RecyclerView messageRecyclerView;
    private BroadcastReceiver pushReceiver;
    private MTPMessageAdapter adapter;
    private ArrayList<MTPMessage> messages;

    private EditText messageEditText;
    private Button sendButton;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity__main);

        otherBoringStuff();


        messageRecyclerView = (RecyclerView) findViewById(R.id.rv__message_list);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        messages = new ArrayList<>();
        adapter = new MTPMessageAdapter(messages, username);

        messageRecyclerView.setAdapter(adapter);

        updateData();

        //Create receiver for push notification broadcasted by our custom receiver service
        IntentFilter filter = new IntentFilter(ACTION_PUSH);
        pushReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String messageObj = intent.getStringExtra("obj");

                MTPMessage message = new Gson().fromJson(messageObj, MTPMessage.class);
                messages.add(message);
                adapter.notifyDataSetChanged();

                messageRecyclerView.scrollToPosition(messages.size() - 1);

            }
        };

        //Register receiver
        registerReceiver(pushReceiver, filter);
    }

    private void updateData() {
        new AsyncTask<Void, Void, ArrayList<MTPMessage>>() {

            @Override
            protected ArrayList<MTPMessage> doInBackground(Void... params) {

                ArrayList<MTPMessage> messageArrayList = new ArrayList<>();

                //Create a MKQuery object to query the remote BaaS
                MKQuery messageQuery = new MKQuery(MESSAGE_COLLECTION_NAME);

                //Add a simple filter to the query to order results
                messageQuery.orderAscendingByCreationDate();

                //Query the BaaS synchronously, we are in a AsynTask so don't care about network on main thread
                //
                //findAll() will always return an ArrayList of MKCollection directly if sync, by callback if async
                ArrayList<MKCollection> collections = messageQuery.findAll().doSynchronously();

                for (MKCollection collection : collections) {
                    //Extract out real data model class from a MKCollection object
                    MTPMessage message = collection.extractMKObject(MTPMessage.class);

                    messageArrayList.add(message);
                }

                return messageArrayList;
            }

            @Override
            protected void onPostExecute(ArrayList<MTPMessage> messageArrayList) {
                super.onPostExecute(messageArrayList);
                messages.addAll(messageArrayList);
                adapter.notifyDataSetChanged();
                messageRecyclerView.scrollToPosition(messages.size() - 1);
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pushReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn__send:
                if (messageEditText.getText().length() > 4) {
                    sendMessage(messageEditText.getText().toString());
                } else {
                    Toast.makeText(MTPMainActivity.this, "Message too short!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void sendMessage(String message) {
        messageEditText.setEnabled(false);
        sendButton.setEnabled(false);

        //Create a new MKCollection object
        MKCollection messageCollection = new MKCollection(MESSAGE_COLLECTION_NAME);

        //Put field by key,  value
        messageCollection.put(KEY__MESSAGE_TEXT, message);
        messageCollection.put(KEY__MESSAGE_CREATOR, username);

        //Save object to remote BaaS, async. No need to pass a not null callback
        messageCollection.save().doAsynchronously(this);
    }

    private void otherBoringStuff() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        username = PreferenceManager.getDefaultSharedPreferences(this).getString(PREFS_KEY__USERNAME, null);
        getSupportActionBar().setSubtitle("Logged in as: " + username);

        messageEditText = (EditText) findViewById(R.id.et__message);
        sendButton = (Button) findViewById(R.id.btn__send);
        sendButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dcnmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_camera:
                dispatchTakePictureIntent();
                break;
        }
        return true;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadPhoto(imageBitmap);
        }
    }

    private void uploadPhoto(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);


        //Upload a file
        new MKCollectionFile.Upload("photo", bs).doAsynchronously(new MKCallback() {
            @Override
            public void onCompleted(Object o, MKError mkError, Object o2) {
                //Error is null, upload succeed
                if (mkError == null) {
                    //The uploaded MKCollectionFIle is returned by the callback populated with upload data
                    MKCollectionFile collectionFile = (MKCollectionFile) o;


                    //Create a new chat message
                    MKCollection fileMessage = new MKCollection(MESSAGE_COLLECTION_NAME);


                    //Add the "standard"
                    fileMessage.put(KEY__MESSAGE_CREATOR, username);
                    fileMessage.put(KEY__MESSAGE_TEXT, "image");

                    //Add the reference of uploaded file url
                    fileMessage.put(KEY__MESSAGE_ATTACHMENT, collectionFile.getLocation());

                    //Save the message on remote BaaS
                    fileMessage.save().doAsynchronously(null);
                }
            }
        });

    }

    @Override
    public void onCompleted(Object o, final MKError mkError, Object o2) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = mkError != null ? "Error" : "Message sent!!";
                Toast.makeText(MTPMainActivity.this, message, Toast.LENGTH_SHORT).show();
                messageEditText.setEnabled(true);
                sendButton.setEnabled(true);
                messageEditText.setText("");
            }
        });
    }
}
