package com.example.chatnow;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText editTextPassword,editTextConfirmPassword;
    private View viewProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextPassword=findViewById(R.id.etPassword);
        editTextConfirmPassword=findViewById(R.id.etConfirmPassword);
        viewProgressBar=findViewById(R.id.progressBar);
    }

    public void btnChangePassword(View view) {
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword=editTextConfirmPassword.getText().toString().trim();

        if (password.equals(""))
        {
            editTextPassword.setError("Enter Password");
        }
        else if (confirmPassword.equals(""))
        {
            editTextConfirmPassword.setError("Confirm Password");
        }
        else if (!password.equals(confirmPassword))
        {
            editTextConfirmPassword.setError("Password mismatch");
        }

        else
        {
            viewProgressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


            if (firebaseUser!=null)
            {
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        viewProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChangePasswordActivity.this,"Password changed successfully",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else
                        {
                            Toast.makeText(ChangePasswordActivity.this,"Something went wrong"+task.getException(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}