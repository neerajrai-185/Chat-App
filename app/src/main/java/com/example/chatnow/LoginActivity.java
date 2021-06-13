package com.example.chatnow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private String   email, password;
    private View viewProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail    = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        viewProgressBar  = findViewById(R.id.progressBar);
    }

    public void tvSignupClick(View view)
    {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }


    public void btnLoginClick(View view) {
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        if (email.equals(""))
        {
            editTextEmail.setError("Please Enter Email Correct");
        }
        else if (password.equals(""))
        {
            editTextPassword.setError("Please Enter Password Correct");
        }
        else
        {
            viewProgressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    viewProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful())
                    {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        Toast.makeText(LoginActivity.this,"Login Successfully :"+task.getException(),Toast.LENGTH_SHORT);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Login Failed : Please Try Agian :" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }


    public void tvResetPasswordClick(View view)
    {
        startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
    }


}