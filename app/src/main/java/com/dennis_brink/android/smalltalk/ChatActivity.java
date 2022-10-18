package com.dennis_brink.android.smalltalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FloatingActionButton fabSend;
    private EditText txtMessage;
    private RecyclerView rvChat;
    private ImageView imgBack;
    private TextView txtChat;

    FirebaseDatabase fb;
    DatabaseReference dbref;
    FirebaseAuth auth;
    FirebaseUser user;

    String sender,receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fb = FirebaseDatabase.getInstance();
        dbref = fb.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        Log.d("DENNIS_B", "(ChatActivity) - onCreate(): Setup references to Firebase");

        Intent i = getIntent();
        sender = user.getDisplayName();
        receiver = i.getStringExtra("target");

        Log.d("DENNIS_B", "(ChatActivity) - onCreate(): Sender " + sender + ", receiver " + receiver);

        fabSend = findViewById(R.id.fabSend);
        txtMessage = findViewById(R.id.editTextMessage);
        rvChat = findViewById(R.id.rvChat);
        imgBack = findViewById(R.id.imgBack);
        txtChat = findViewById(R.id.textViewChat);

        txtChat.setText(receiver);

        fabSend.setOnClickListener(view -> {
            String message = txtMessage.getText().toString();
            if(!message.isEmpty()){
                sendMessage(message);
                txtMessage.setText("");
            }
        });

        imgBack.setOnClickListener(view -> {
            onBackPressed();
        });

    }

    private void sendMessage(String message) {
        Log.d("DENNIS_B", "(ChatActivity) - sendMessage()");

        // get a key from firebase
        String key = dbref.child("messages").child(sender).child(receiver).push().getKey();

        Map<String, Object> map = new HashMap<>();

        map.put("message", message);
        map.put("from", sender);

        /*
            map.put("/messages/" + sender + "/" + receiver + "/" + key + "/from/", sender);
            map.put("/messages/" + sender + "/" + receiver + "/" + key + "/message/", message);
            map.put("/messages/" + receiver + "/" + sender + "/" + key + "/from/", sender);
            map.put("/messages/" + receiver + "/" + sender + "/" + key + "/message/", message);

            dbref.setValue(map);

            The code above will not work. You constantly get an error
            (ChatActivity) - sendMessage(): https://smalltalk-f0aad-default-rtdb.europe-west1.firebasedatabase.app/messages/Dennis%20Brink/Flintermans/-NEh1dbx_htLpHmdb9qN
            (ChatActivity) - sendMessage(): Error --> Invalid key: /messages/Dennis Brink/Flintermans/-NEh1dbx_htLpHmdb9qN. Keys must not contain '/', '.', '#', '$', '[', or ']'
            This is strange because the path is full of "/" characters

            The construction below will work for unclear reasons. Looks like you have to make the key the end node after which you can store any map

        */

        try {
            dbref.child("messages").child(sender).child(receiver).child(key).setValue(map).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.d("DENNIS_B", "(ChatActivity) - sendMessage(): message (sender <--> receiver) stored successfully");
                    dbref.child("messages").child(receiver).child(sender).child(key).setValue(map).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Log.d("DENNIS_B", "(ChatActivity) - sendMessage(): message (receiver <--> sender) stored successfully");
                        } else {
                            Log.d("DENNIS_B", "(ChatActivity) - sendMessage(0): Error --> " + task.getException().getLocalizedMessage());
                        }
                    });
                } else {
                    Log.d("DENNIS_B", "(ChatActivity) - sendMessage(1): Error --> " + task.getException().getLocalizedMessage());
                }
            });
        }catch(Exception e){
            Log.d("DENNIS_B", "(ChatActivity) - sendMessage(2): Error --> " + e.getLocalizedMessage());
        }

    }

    @Override
    protected void onDestroy() {
        Log.d("DENNIS_B", "(ChatActivity) - onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.d("DENNIS_B", "(ChatActivity) - onBackPressed(): Activity will be finished");
        finish();
    }
}