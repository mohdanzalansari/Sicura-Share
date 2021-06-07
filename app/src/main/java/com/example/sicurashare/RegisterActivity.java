package com.example.sicurashare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {


private FirebaseAuth auth;
    private Button register_button;
    private EditText reg_email, reg_pass;
    private ProgressDialog mProgress;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgress = new ProgressDialog(RegisterActivity.this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        auth=FirebaseAuth.getInstance();

        register_button= findViewById(R.id.signup_button);
        reg_email= findViewById(R.id.register_email);
        reg_pass= findViewById(R.id.register_pass);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                register_user();
            }
        });

    }

    private void register_user() {

        String email=reg_email.getText().toString();
        String password=reg_pass.getText().toString();

        if (TextUtils.isEmpty(email)&& TextUtils.isEmpty(password))
        {
            reg_email.setError("Enter Email");
            reg_pass.setError("Enter Password");
        }
        else if (TextUtils.isEmpty(email))
        {
            reg_email.setError("Enter Email");
        }
        else if (TextUtils.isEmpty(password))
        {
            reg_pass.setError("Enter Password");
        }
        else
        {
            mProgress.show();
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this,new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful())
                    {
                        mProgress.dismiss();
                        Toast.makeText(RegisterActivity.this,"Sign Up Successfully!",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this,login_activity.class));
                        finish();
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(RegisterActivity.this,"Registration Failed",Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgress.dismiss();
                    Toast.makeText(RegisterActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
