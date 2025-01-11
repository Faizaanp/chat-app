package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText userName, userEmail, userPassword;
    TextView signinBtn, signupBtn;
    String name, email, password;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        userName = findViewById(R.id.usernametext);
        userEmail = findViewById(R.id.emailText);
        userPassword = findViewById(R.id.passwordtext);
        signinBtn = findViewById(R.id.login);
        signupBtn = findViewById(R.id.signup);

        signupBtn.setOnClickListener(v ->{
            email = userEmail.getText().toString().trim();
            password = userPassword.getText().toString().trim();
            name = userName.getText().toString().trim();
            if(TextUtils.isEmpty(email)){
                userEmail.setError("Please enter your email");
                userEmail.requestFocus();
                return;
            }
            if(TextUtils.isEmpty(name)){
                userName.setError("Please enter your username");
                userName.requestFocus();
                return;
            }
            if(TextUtils.isEmpty(password)){
                userPassword.setError("Please enter your password");
                userPassword.requestFocus();
                return;
            }
            Signup();
        });

        signinBtn.setOnClickListener(v ->{
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
        });

    }

    protected void onStart(){
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(SignupActivity.this,MainActivity.class));
            finish();
        }
    }

    private void Signup(){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    //whenever a new user is created with this method a random userid(display name) is assigned to it
                    //the display name can be changed with UserProfileChangeRequest
                    public void onSuccess(AuthResult authResult) {
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        firebaseUser.updateProfile(userProfileChangeRequest);
                        UserModel userModel = new UserModel(FirebaseAuth.getInstance().getUid(), name, email, password);
                        databaseReference.child(FirebaseAuth.getInstance().getUid()).setValue(userModel);
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this, "Signup Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}