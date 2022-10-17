package com.dennis_brink.android.smalltalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.cache.DiskLruCache;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase fb;
    DatabaseReference fbrefc;
    FirebaseUser fbuser;
    String username;

    List<SmallTalkUser> smallTalkUserList;

    ChildEventListener recyclerViewChildEventListener;

    RecyclerView recyclerViewUsers;
    UserAdapter adapter;

    SmallTalkUser smallTalkUser;

    @Override
    protected void onDestroy() {

        Log.d("DENNIS_B", "(MainActivity) - onDestroy(): Removing child event listener <recyclerViewChildEventListener>");
        fbrefc.child("users").removeEventListener(recyclerViewChildEventListener);
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DENNIS_B", "(MainActivity) - onCreate()");

        try {

            auth = FirebaseAuth.getInstance();
            fb = FirebaseDatabase.getInstance();
            fbrefc = fb.getReference();
            fbuser = auth.getCurrentUser();

            Log.d("DENNIS_B", "(MainActivity) - onCreate(): Setup references to Firebase");

            recyclerViewUsers = findViewById(R.id.rvUsers);
            recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
            smallTalkUserList = new ArrayList<>();

            setupChildEventListener();
            adapter = new UserAdapter(smallTalkUserList, fbuser.getDisplayName(), MainActivity.this);
            recyclerViewUsers.setAdapter(adapter);

        } catch (Exception e) {
            Log.d("DENNIS_B", "(MainActivity) - onCreate(): Exception " + e.getLocalizedMessage());
        }
    }

    private void setupChildEventListener(){

        Log.d("DENNIS_B", "(MainActivity) - setupChildEventListener()");

        /*
            What is the difference between ChildEventListener and ValueEventListener
            Firebase interfaces?

            They do almost same thing, though ChildEventListener can be sometimes more flexible:
            with ChildEventListener you can specify different behavior for 4 actions (onChildAdded,
            onChildChanged, onChildMoved and onChildRemoved), while ValueEventListener provides
            only onDataChanged.
            Also ChildEventListener provides DataSnapshots (immutable copies of the data) at
            child's location while ValueEventListener provides a DataSnapshot of a whole node.
         */

        recyclerViewChildEventListener = new ChildEventListener() {

            // on start up this listener will find every node under the users node because the listener is
            // initializing. It creates a list of nodes. We can use this to fill the list array and with that
            // we can fill the adapter

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String key = snapshot.getKey(); // get the UID from the database
                String name = "";
                String url = "";

                // The child event listener is added on "users" so any child of this listener would be after the key value
                // In this case it would be after /user/uid. You cannot get the value like a regular listener

                try {
                    name = snapshot.child("username").getValue(String.class);
                    url = snapshot.child("avatar").getValue(String.class);
                }catch(Exception e){
                    Log.d("DENNIS_B", "(MainActivity) - onChildAdded(): Exception reading value " + e.getLocalizedMessage());
                }

                Log.d("DENNIS_B", "(MainActivity) - onChildAdded(): " + key + "|" + name + "|" + url);

                if (!key.equals(fbuser.getUid())) { // filter your self out, you should not be in the contacts list
                    smallTalkUser = new SmallTalkUser(name, url, key);
                    smallTalkUserList.add(smallTalkUser);
                    if(adapter != null) {
                        int index = (smallTalkUserList.size() - 1);
                        adapter.notifyItemInserted(index);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase/onChildChanged: detected a change on user " + snapshot.getKey());
                updateUserList(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase/onChildRemoved: detected a removal on user " + snapshot.getKey());

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase/onChildMoved: detected a move on user " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase/onCancelled:" + error.toException().getLocalizedMessage());
            }
        };
        fbrefc.child("users").addChildEventListener(recyclerViewChildEventListener);

    }

    private void updateUserList(DataSnapshot snapshot){
        String key = snapshot.getKey();
        // find the correct record
        for (int i=0; i<smallTalkUserList.size();i++) {
            if (smallTalkUserList.get(i).getKey().equalsIgnoreCase(key)) {
                Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase/onChildAdded/updateUserList: index of object = " + i);
                smallTalkUserList.get(i).setName(snapshot.child("username").getValue(String.class));
                smallTalkUserList.get(i).setUrl(snapshot.child("avatar").getValue(String.class));
                adapter.notifyItemChanged(i, smallTalkUserList.get(i));
                Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase/onChildAdded/updateUserList: Updated object " + i + " with new data");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.mainactivity_options_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();  <-- Remove this for sure. Causes very unpredictable behaviour
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("DENNIS_B", "(MainActivity) - onOptionsItemSelected(): Selected option " + item.getItemId());
        switch(item.getItemId()){
            case R.id.action_profile:
                Log.d("DENNIS_B", "(MainActivity) - onOptionsItemSelected(): Option menu profile (29) " + R.id.action_profile);
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.action_logout: // logout en show login screen
                Log.d("DENNIS_B", "(MainActivity) - onOptionsItemSelected(): Option menu log out (28) " + R.id.action_logout);
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

