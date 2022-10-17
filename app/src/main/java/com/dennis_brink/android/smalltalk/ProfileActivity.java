package com.dennis_brink.android.smalltalk;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    Uri uriCurrentImage;
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
    Uri imageSelectedUri;
    String userName;

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

        loadUserData();

    }

    private void loadUserData() {

        Log.d("DENNIS_B", "(ProfileActivity) - loadUserData()");

        progressBar.setVisibility(View.VISIBLE);

        String userName = user.getDisplayName();
        uriCurrentImage = user.getPhotoUrl();

        Log.d("DENNIS_B", "(ProfileActivity) - loadUserData(): userName: " + userName);
        Log.d("DENNIS_B", "(ProfileActivity) - loadUserData(): uriCurrentImage: " + uriCurrentImage);

        editTextDisplayName.setText(userName);

        if(!(uriCurrentImage == null)){
            Picasso.get().load(uriCurrentImage).into(imgAvatar, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onError(Exception e) {
                    Log.d("DENNIS_B", "(ProfileActivity) - loadUserData(0): Error --> " + e.getLocalizedMessage());
                    imgAvatar.setImageResource(R.drawable.ic_baseline_account_circle_24);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            Log.d("DENNIS_B", "(ProfileActivity) - loadUserData(): No image, load default image");
            imgAvatar.setImageResource(R.drawable.ic_baseline_account_circle_24);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void registerActivityResultLauncherForProfilePicture() {

        activityResultLauncherForProfilePicture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                int resultCode = result.getResultCode();
                Intent data = result.getData();
                imageIsSelected = false;
                if (resultCode == RESULT_OK && data != null) {
                    imageSelectedUri = data.getData();
                    Picasso.get().load(imageSelectedUri).into(imgAvatar);
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

    private void saveUserData(){

        userName = editTextDisplayName.getText().toString();

        if (imageIsSelected) {
            deleteOldAvatar(); // this can be executed async to the rest of the save
        }
        saveAll();

    }

    private void deleteOldAvatar(){

        Log.d("DENNIS_B", "(ProfileActivity) - deleteOldAvatar(): Avatar is to be deleted: " + imageIsSelected);
        Log.d("DENNIS_B", "(ProfileActivity) - deleteOldAvatar(): Old avatar url: " + uriCurrentImage);

        try {
            if (!(uriCurrentImage == null)) {
                StorageReference imgRefOrg = fs.getReferenceFromUrl(uriCurrentImage.toString());
                imgRefOrg.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DENNIS_B", "(ProfileActivity) - deleteOldAvatar(): Old avatar deleted");
                    } else {
                        Log.d("DENNIS_B", "(ProfileActivity) - deleteOldAvatar(): Error --> " + task.getException().getLocalizedMessage());
                        // it doesn't really matter, we are going to save the new avatar anyway
                    }
                });
            }
        } catch (Exception e) {
            Log.d("DENNIS_B", "(ProfileActivity) - deleteOldAvatar(): Exception --> " + e.getLocalizedMessage());
        }
    }

    private void saveAll(){

        /*
            3 situations: 1. image selected
                          2. image selected, save failed
                          3. no image selected
        */

        Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): userName: " + userName);
        Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): imageIsSelected: " + imageIsSelected);
        Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): imageSelectedUri: " + imageSelectedUri);

        map.put("/users/" + user.getUid() + "/username/", userName); // save the name first

        if(imageIsSelected){  // check if an image was selected (imageIsSelected == true)

            UUID imageId = UUID.randomUUID();
            String filename = "images/" + imageId.toString();

            fsref.child(filename).putFile(imageSelectedUri).addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getTask().isSuccessful()){ // successfully saved image to storage

                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated STORAGE: " + filename);

                    StorageReference imgRef = fs.getReference(filename); // create a STORAGE ref to the picture
                                                                         // get the url of the newly saved file (uri)
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        UserProfileChangeRequest profileUpdates;

                        profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .setPhotoUri(uri)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated firebase user: " + userName + "|" + uri);
                                // RTDB
                                map.put("/users/" + user.getUid() + "/avatar/", uri.toString());
                                dbref.updateChildren(map).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated RTDB: " + userName + "|" + uri);
                                        finish(); // save complete
                                    } else { // RTDB update fail
                                        Log.d("DENNIS_B", "(ProfileActivity) - saveAll(1): Error --> " + task1.getException().getLocalizedMessage());
                                        return; // dialog ?
                                    }
                                });
                            } else { // profile update fail
                                Log.d("DENNIS_B", "(ProfileActivity) - saveAll(0): Error --> " + task.getException().getLocalizedMessage());
                                return; // dialog ?
                            }
                        });
                    });

                } else { // an image was selected but is was not saved successfully

                    // set default picture (url will be null)
                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): Selected image not saved");
                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(2): Error --> " + taskSnapshot.getTask().getException().getLocalizedMessage());
                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): set firebase defaults: " + userName + "|null");

                    UserProfileChangeRequest profileUpdates;

                    profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .setPhotoUri(null)
                            .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated firebase user: " + userName + "|null");
                            // RTDB
                            map.put("/users/" + user.getUid() + "/avatar/", "null");
                            dbref.updateChildren(map).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated RTDB: " + userName + "|null");
                                    finish(); // save complete
                                } else { // RTDB update fail
                                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(3): Error --> " + task1.getException().getLocalizedMessage());
                                    return; // dialog ?
                                }
                            });
                        } else { // profile update fail
                            Log.d("DENNIS_B", "(ProfileActivity) - saveAll(4): Error --> " + task.getException().getLocalizedMessage());
                            return; // dialog ?
                        }
                    });
                }
            });

        } else { // no image selected - don't do anything in that section

            UserProfileChangeRequest profileUpdates;

            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated firebase user: " + userName);
                    // RTDB
                    dbref.updateChildren(map).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Log.d("DENNIS_B", "(ProfileActivity) - saveAll(): updated RTDB: " + userName);
                            finish(); // save complete
                        } else { // RTDB update fail
                            Log.d("DENNIS_B", "(ProfileActivity) - saveAll(5): Error --> " + task1.getException().getLocalizedMessage());
                            return; // dialog ?
                        }
                    });
                } else { // profile update fail
                    Log.d("DENNIS_B", "(ProfileActivity) - saveAll(6): Error --> " + task.getException().getLocalizedMessage());
                    return; // dialog ?
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();  <-- Remove this for sure. Causes very unpredictable behaviour
        Log.d("DENNIS_B", "(ProfileActivity) - onBackPressed(): Activity will be finished");
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d("DENNIS_B", "(ProfileActivity) - onDestroy()");
        super.onDestroy();
    }

}