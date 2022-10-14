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

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase fb;
    DatabaseReference fbref;
    DatabaseReference fbrefc;
    FirebaseUser fbuser;
    String username;

    List<SmallTalkUser> smallTalkUserList;

    ValueEventListener recyclerViewEventListener;
    ChildEventListener recyclerViewChildEventListener;

    RecyclerView recyclerViewUsers;
    UserAdapter adapter;

    SmallTalkUser smallTalkUser;

    @Override
    protected void onDestroy() {

        Log.d("DENNIS_B", "(MainActivity) - onDestroy(): Removing event listener <recyclerViewEventListener>");
        fbref.child("users").child(fbuser.getUid()).child("username").removeEventListener(recyclerViewEventListener);
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
            fbref = fb.getReference();
            fbrefc = fb.getReference();
            fbuser = auth.getCurrentUser();

            Log.d("DENNIS_B", "(MainActivity) - onCreate(): Setup references to Firebase");

            recyclerViewEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    username = snapshot.getValue().toString(); // this is you: the logged in person, you should not be in de list of possible recipients
                    Log.d("DENNIS_B", "(MainActivity) - onCreate/recyclerViewEventListener/onDataChange: Name: " + username);
                    getUsersFromFirebase();
                    adapter = new UserAdapter(smallTalkUserList, username, MainActivity.this);
                    recyclerViewUsers.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            fbref.child("users").child(fbuser.getUid()).child("username").addValueEventListener(recyclerViewEventListener);

            Log.d("DENNIS_B", "(MainActivity) - onCreate(): Created event listener <recyclerViewEventListener>");

            recyclerViewUsers = findViewById(R.id.rvUsers);
            recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
            smallTalkUserList = new ArrayList<>();

        } catch (Exception e) {
            Log.d("DENNIS_B", "(MainActivity) - onCreate(): Exception " + e.getLocalizedMessage());
        }
    }

    private void getUsersFromFirebase(){

        Log.d("DENNIS_B", "(MainActivity) - getUsersFromFirebase()");

        recyclerViewChildEventListener = new ChildEventListener() {

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
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        fbrefc.child("users").addChildEventListener(recyclerViewChildEventListener);

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

