package com.dennis_brink.android.smalltalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ResetPwdActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private Button btnSend;
    ProgressBar progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);

        editTextEmail = findViewById(R.id.editTextResetEmail);
        btnSend = findViewById(R.id.btnForgotPasswordSendMail);
        progressBar = findViewById(R.id.progressBarForgotPassword);

        auth = FirebaseAuth.getInstance();

        btnSend.setOnClickListener(view -> {

            Button btn = (Button) view;
            btn.setClickable(false);
            String userEmail = editTextEmail.getText().toString();
            if(userEmail == null || userEmail.isEmpty()){
                Toast.makeText(this, "Please enter an e-mail address", Toast.LENGTH_SHORT).show();
                btn.setClickable(true);
                return;
            }
            sendPasswordResetMail(userEmail, view);
            btn.setClickable(true);
        });

    }

    private void sendPasswordResetMail(String userMail, View view){

        Button btn = (Button) view;
        progressBar.setVisibility(View.VISIBLE);

        auth.sendPasswordResetEmail(userMail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ResetPwdActivity.this, "E-mail with reset link is sent", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    btn.setClickable(true);
                } else {
                    Toast.makeText(ResetPwdActivity.this, "E-mail could not be sent", Toast.LENGTH_SHORT).show();
                    Log.d("DENNIS_B", "Error sending reset mail " + task.getException().getLocalizedMessage());
                    progressBar.setVisibility(View.INVISIBLE);
                    btn.setClickable(true);
                }
            }
        });
    }

}