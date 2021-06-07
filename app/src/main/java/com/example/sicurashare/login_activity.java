package com.example.sicurashare;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class login_activity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView text,forgotPass;
    private Button login;
    private EditText id,pass;
    private ProgressDialog mProgress;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        text= findViewById(R.id.register_text);
        id= findViewById(R.id.user_id);
        pass= findViewById(R.id.user_pass);
        login= findViewById(R.id.login_button);
        forgotPass=findViewById(R.id.forgotpassBox);
        auth=FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(login_activity.this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent= new Intent(login_activity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login_activity.this,RegisterActivity.class));
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                user_login();
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetmail=new EditText(view.getContext());

                AlertDialog.Builder passwordResetDialogBox= new AlertDialog.Builder(view.getContext());
                passwordResetDialogBox.setTitle("Reset Password?");
                passwordResetDialogBox.setMessage("Enter your email to receive the password reset link");
                passwordResetDialogBox.setView(resetmail);

                passwordResetDialogBox.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (!resetmail.getText().toString().isEmpty())
                        {
                            String mail=resetmail.getText().toString();
                            auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(login_activity.this, "Password Reset Link has been sent to your email.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(login_activity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            resetmail.setError("Enter mail id");
                        }

                    }
                });

                passwordResetDialogBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                passwordResetDialogBox.create().show();

            }
        });

    }

    private void user_login() {

        String email=id.getText().toString();
        String password=pass.getText().toString();

        if (TextUtils.isEmpty(email)&& TextUtils.isEmpty(password))
        {
            id.setError("Enter Email");
            pass.setError("Enter Password");
        }
        else if (TextUtils.isEmpty(email))
        {
            id.setError("Enter Email");
        }
        else if (TextUtils.isEmpty(password))
        {
            pass.setError("Enter Password");
        }
        else
        {
            mProgress.show();
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    mProgress.dismiss();
                    Toast.makeText(login_activity.this,"Login Successful!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(login_activity.this,WelcomeActivity.class));
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgress.dismiss();
                    Toast.makeText(login_activity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            });

        }



    }
}
