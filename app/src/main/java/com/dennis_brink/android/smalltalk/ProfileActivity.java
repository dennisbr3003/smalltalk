package com.dennis_brink.android.smalltalk;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    Uri imageUri;
    private ActivityResultLauncher<Intent> activityResultLauncherForProfilePicture;
    private TextInputEditText editTextDisplayName;
    private CircleImageView imgAvatar;
    private ProgressBar progressBar;
    private Button bntSave;
    boolean imageIsSelected = false;

    FirebaseDatabase fb;
    DatabaseReference dbref;
    FirebaseAuth auth;
    FirebaseStorage fs;
    StorageReference fsref;
    FirebaseUser user;
    String image_url;

    ValueEventListener profileValueEventListener;

    Map<String, Object> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgAvatar = findViewById(R.id.imgCircleAccountProfile);
        editTextDisplayName = findViewById(R.id.editTextSignUpNameProfile);
        bntSave = findViewById(R.id.btnSignUpSignUpProfile);
        progressBar = findViewById(R.id.progressBarProfile);

        registerActivityResultLauncherForProfilePicture();

        auth = FirebaseAuth.getInstance();
        fb = FirebaseDatabase.getInstance();
        dbref = fb.getReference();
        fs = FirebaseStorage.getInstance();
        fsref = fs.getReference();
        user = auth.getCurrentUser();

        bntSave.setOnClickListener(view -> saveUserData());
        imgAvatar.setOnClickListener(view -> deviceImageSelector());

        Log.d("DENNIS_B", "(ProfileActivity) - onCreate(). Start loading userdata from FireBase");

        loadUserData();

    }

    private void saveProfileUpdate() {
        saveUserData();
    }

    private void registerActivityResultLauncherForProfilePicture() {

        activityResultLauncherForProfilePicture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                int resultCode = result.getResultCode();
                Intent data = result.getData();
                imageIsSelected = false;
                if (resultCode == RESULT_OK && data != null) {
                    imageUri = data.getData();
                    Picasso.get().load(imageUri).into(imgAvatar);
                    imageIsSelected = true;
                }

            }
        });

    }

    private void deviceImageSelector(){

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncherForProfilePicture.launch(i);

    }

    private void loadUserData() {

        Log.d("DENNIS_B", "(ProfileActivity) - loadUserData()");

        progressBar.setVisibility(View.VISIBLE);

        profileValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("username").getValue().toString();
                image_url = snapshot.child("avatar").getValue().toString();

                editTextDisplayName.setText(name);
                if(!image_url.equals("null")){
                    Picasso.get().load(image_url).into(imgAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError(Exception e) {
                            Log.d("DENNIS_B", "(ProfileActivity) - profileValueEventListener/onDataChange: User data not loaded " + e.getLocalizedMessage());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_baseline_account_circle_24);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "User data could not be loaded", Toast.LENGTH_SHORT).show();
                Log.d("DENNIS_B", "(ProfileActivity) - profileValueEventListener/onCancelled: Error " + error.toException().getLocalizedMessage());
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        dbref.child("users").child(user.getUid()).addValueEventListener(profileValueEventListener);

    }

    private void saveUserData(){

        String userName = editTextDisplayName.getText().toString();
        map.put("/users/" + user.getUid() + "/username/", userName);

        Log.d("DENNIS_B", "(ProfileActivity) - saveUserData(): Image (new) is selected: " + imageIsSelected);

        if(imageIsSelected){
            try { // first delete the old avatar (the file, the reference will be overwritten)

                if (!image_url.equals("null")) {
                    StorageReference imgRefOrg = fs.getReferenceFromUrl(image_url);
                    imgRefOrg.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DENNIS_B", "(ProfileActivity) - saveUserData(): Old avatar deleted");
                        } else {
                            Log.d("DENNIS_B", "(ProfileActivity) - saveUserData(): Error deleting old avatar (still saving new profile picture)");
                            Log.d("DENNIS_B", "(ProfileActivity) - saveUserData(): Storage error: " + task.getException().getLocalizedMessage());
                        }
                    });
                }
                saveImageToStorage(); // other data is saved as well in the callback

            } catch (Exception e) {
                Toast.makeText(this, "User data could not be saved.", Toast.LENGTH_SHORT).show();
                Log.d("DENNIS_B", "(ProfileActivity) - saveUserData(): " + e.getLocalizedMessage());
            }
        } else {
            Log.d("DENNIS_B", "(ProfileActivity) - saveUserData(): No (new) image selected. To be saved: " + map.toString());
            saveDataToDatabase();
        }

    }

    private void saveDataToDatabase(){
        dbref.updateChildren(map).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d("DENNIS_B", "(ProfileActivity) - saveDataToDatabase(): Data sent to db successfully");
                finish();
            } else{
                Log.d("DENNIS_B", "(ProfileActivity) - saveDataToDatabase(): Error saving data " + task.getException().getLocalizedMessage());
                Toast.makeText(ProfileActivity.this, "User data could not be saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImageToStorage(){

        UUID imageId = UUID.randomUUID();
        String filename = "images/" + imageId.toString();

        Log.d("DENNIS_B", "(ProfileActivity) - saveImageToStorage(): New avatar filename: " + filename);

        fsref.child(filename).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            if(taskSnapshot.getTask().isSuccessful()){
                StorageReference imgRef = fs.getReference(filename); // create a dbref to the picture
                imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileUrl = uri.toString();
                    map.put("/users/" + user.getUid() + "/avatar/", fileUrl);
                    Log.d("DENNIS_B", "(ProfileActivity) - saveImageToStorage(): To be saved: " + map.toString());
                    saveDataToDatabase();
                });
            } else {
                Log.d("DENNIS_B", "(ProfileActivity) - saveImageToStorage(): Error saving to STORAGE: " + taskSnapshot.getTask().getException().getLocalizedMessage());
                map.put("/users/" + user.getUid() + "/avatar/", "null");
            }
        });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();  <-- Remove this for sure. Causes very unpredictable behaviour
        Log.d("DENNIS_B", "(ProfileActivity) - onBackPressed(): Activity will be finished");
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d("DENNIS_B", "(ProfileActivity) - onStop():  Removing event listener <profileValueEventListener>");
        dbref.child("users").child(user.getUid()).removeEventListener(profileValueEventListener);
        super.onDestroy();
    }

}