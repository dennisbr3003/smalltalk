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
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextDisplayName;
    private Button btnSignUp;
    private CircleImageView imgAvatar;
    private ProgressBar progressBar;

    boolean imageIsSelected = false;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference dbref;

    FirebaseStorage fs;
    StorageReference fsref;

    Uri imageUri;
    private ActivityResultLauncher<Intent> activityResultLauncherForProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextDisplayName = findViewById(R.id.editTextSignUpName);
        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextPassword = findViewById(R.id.editTekstSignUpPassword);
        imgAvatar = findViewById(R.id.imgCircleAccount);
        progressBar = findViewById(R.id.progressBarProfile);

        btnSignUp = findViewById(R.id.btnSignUpSignUp);

        registerActivityResultLauncherForProfilePicture();

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        dbref = db.getReference();
        fs = FirebaseStorage.getInstance();
        fsref = fs.getReference();

        btnSignUp.setOnClickListener(view -> {
            //
            Button btn = (Button) view;

            btn.setClickable(false);

            String userEmail = editTextEmail.getText().toString();
            String userPassword = editTextPassword.getText().toString();
            String userName = editTextDisplayName.getText().toString();

            if(userEmail == null || userEmail.isEmpty()){
                Toast.makeText(this, "Please enter a e-mail address", Toast.LENGTH_SHORT).show();
                btn.setClickable(true);
                return;
            }
            if(userPassword == null || userPassword.isEmpty()){
                Toast.makeText(this, "Please enter a password, password cannot be empty", Toast.LENGTH_SHORT).show();
                btn.setClickable(true);
                return;
            }

            if(userName == null || userName.isEmpty()){
                Toast.makeText(this, "Please enter a display name", Toast.LENGTH_SHORT).show();
                btn.setClickable(true);
                return;
            }

            signUp(userEmail.trim(), userPassword.trim(), userName.trim());

            btn.setClickable(true);

        });

        imgAvatar.setOnClickListener(view -> {
            // access device photo's
            deviceImageSelector();
        });

    }

    private void signUp(String userEmail, String userPassword, String userName) {

        Log.d("DENNIS_B", "(SignUpActivity) - signUp(): E-mail: " + userEmail);
        Log.d("DENNIS_B", "(SignUpActivity) - signUp(): Password: " + userPassword);
        Log.d("DENNIS_B", "(SignUpActivity) - signUp(): Display name: " + userName);

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                dbref.child("users").child(auth.getUid()).child("username").setValue(userName);

                if(imageIsSelected){
                    //dbref.child("users").child(auth.getUid()).child("avatar").setValue("null");
                    UUID imageId = UUID.randomUUID();
                    String filename = "images/" + imageId.toString();
                    fsref.child(filename).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                        if(taskSnapshot.getTask().isSuccessful()){
                            StorageReference imgRef = fs.getReference(filename); // create a dbref to the picture
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String fileUrl = uri.toString();
                                dbref.child("users").child(auth.getUid()).child("avatar").setValue(fileUrl).addOnSuccessListener(unused -> {
                                    Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Saved avatar URL to RTDB: user/" + auth.getUid() + "/avatar");

                                    //update user with displayname and url
                                    Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Update Firebase user profile" + ( (!userName.equals(null) && !userName.equals("")) && !uri.equals(null) ));
                                    if ( (!userName.equals(null) && !userName.equals("")) && !uri.equals(null) ) {
                                        Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Preparing to update Firebase user profile");
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(userName)
                                                .setPhotoUri(uri)
                                                .build();

                                        user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Toast.makeText(SignUp.this, "Displayname updated", Toast.LENGTH_SHORT).show();
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Updated Firebase user profile with " + userName + "  and url");
                                                Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                                            } else {
                                                //prgSignUp.setVisibility(View.INVISIBLE); <-- finally
                                                Toast.makeText(SignUpActivity.this, "Displayname NOT updated, account not complete", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }).addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this, "User avatar NOT saved", Toast.LENGTH_SHORT).show();
                                    Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword():  Error saving URL to RTDB: " + e.getLocalizedMessage());
                                    return;
                                });
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "User avatar NOT saved", Toast.LENGTH_SHORT).show();
                            Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Error saving avatar to STORAGE: " + taskSnapshot.getTask().getException().getLocalizedMessage());
                            return;
                        }
                    });
                } else {
                    Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): No picture selected. Save 'null' to RTDB");
                    dbref.child("users").child(auth.getUid()).child("avatar").setValue("null");
                    // still update
                    Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Update Firebase user profile" + ( (!userName.equals(null) && !userName.equals(""))));
                    if ( (!userName.equals(null) && !userName.equals(""))) {
                        Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Preparing to update Firebase user profile");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                // Toast.makeText(SignUp.this, "Displayname updated", Toast.LENGTH_SHORT).show();
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword(): Updated Firebase user profile with " + userName );
                                //Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                //prgSignUp.setVisibility(View.INVISIBLE); <-- finally
                                Toast.makeText(SignUpActivity.this, "Displayname NOT updated, account not complete", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }
                // Start MainActivity via onStart in LoginActivity since the newly created user IS logged in
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "User account NOT created", Toast.LENGTH_SHORT).show();
                Log.d("DENNIS_B", "(SignUpActivity) - createUserWithEmailAndPassword():  Error saving user profile to AUTH: " + task.getException().getLocalizedMessage());
                return;
            }
        });

        progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onDestroy() {
        Log.d("DENNIS_B", "(SignUpActivity) - onDestroy(): Destroying SignUpActivity");
        super.onDestroy();
    }

    private void deviceImageSelector(){

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncherForProfilePicture.launch(i);

    }

}