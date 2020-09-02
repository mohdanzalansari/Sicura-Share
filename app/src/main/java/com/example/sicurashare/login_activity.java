package com.example.sicurashare;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class login_activity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView text;
    private Button login;
    private EditText id,pass;
    private ProgressDialog mProgress;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        text=(TextView)findViewById(R.id.register_text);
        id=(EditText)findViewById(R.id.user_id);
        pass=(EditText)findViewById(R.id.user_pass);
        login=(Button)findViewById(R.id.login_button);
        auth=FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(login_activity.this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent= new Intent(login_activity.this, HomeActivity.class);
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
                mProgress.show();
                user_login();
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
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    mProgress.dismiss();
                    Toast.makeText(login_activity.this,"Login Successful!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(login_activity.this,HomeActivity.class));
                    finish();

                }
            });

        }



    }
}
