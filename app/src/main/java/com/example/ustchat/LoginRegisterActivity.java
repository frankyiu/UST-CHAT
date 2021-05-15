package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    Toolbar toolbar;
    TextView tvContinue;
    TextView tvForgotPW;
    Button btnLogin;
    Button btnSignup;
    EditText etEmail;
    EditText etPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        toolbar = findViewById(R.id.login_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        btnLogin = findViewById(R.id.btn_login_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(new Callback(){
                    @Override
                    public void callback() {
                        startActivity(new Intent(getApplicationContext(), CourseActivity.class));
                        overridePendingTransition(0, 0);
                    }
                });

            }
        });
        btnSignup = findViewById(R.id.btn_login_sign_up);

        tvContinue = findViewById(R.id.tv_login_continue_as_a_visitor);
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                }
                startActivity(new Intent(getApplicationContext(), CourseActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        etEmail = findViewById(R.id.edit_login_student_email);
        etPassword = findViewById(R.id.edit_login_password_);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(getApplicationContext(), CourseActivity.class));
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view instanceof EditText) {
            ((EditText) view).clearFocus();
            InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.login_info) {
            //openDialog();
        }
        return true;
    }

    public void openDialog() {
        RegisterDialog dialogRegister = new RegisterDialog(this);
        dialogRegister.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogRegister.show();
    }


    private void login(Callback callbock){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if(email.equals("") || password.equals("")){
            Toast.makeText(LoginRegisterActivity.this, "Please input a valid email or password.",
                    Toast.LENGTH_SHORT).show();
        }else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Login:success");
                                if(mAuth.getCurrentUser().isEmailVerified())
                                    callbock.callback();
                                else
                                    Toast.makeText(LoginRegisterActivity.this, "Please verify your email address first.",
                                            Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "Login:failure", task.getException());
                                Toast.makeText(LoginRegisterActivity.this, "Login failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void forgetPassword(View view) {
        String emailAddress = etEmail.getText().toString().trim();

        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

}

class RegisterDialog extends Dialog {
    private ImageButton ibRegister;
    private EditText etITSC;
    private EditText etPW;
    private TextView tvWarningInvalidITSC;
    private TextView tvWarningInvalidPW;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    final private Context context;
    private static final String TAG = "LoginActivity";
    public RegisterDialog(final Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_account);

        etITSC = findViewById(R.id.et_create_account_itsc_account);
        etPW = findViewById(R.id.et_create_account_pw);
        ibRegister = findViewById(R.id.ib_create_account_submit);
        tvWarningInvalidITSC = findViewById(R.id.tv_create_account_warning_itsc_account);
        tvWarningInvalidPW = findViewById(R.id.tv_create_account_warning_pw);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        ibRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                register();
            }
        });

        clearWarningMessages();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    public void register() {
        if (mAuth.getCurrentUser() == null) {
            String itsc = etITSC.getText().toString();
            String password = etPW.getText().toString();
            if (validateRegistrationSubmission(itsc, password)) {
                String email = itsc + "@connect.ust.hk";
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmailAndPassword:success");
                                    sendVerifyEmail();
                                    writeUserToDB();
                                } else {
                                    Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                                    Toast.makeText((LoginRegisterActivity) context, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }else if(!mAuth.getCurrentUser().isEmailVerified()){
//            send email again
            sendVerifyEmail();
        }
    }

    private void sendVerifyEmail() {
        if(mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()){
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendEmailVerification: email sent");
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                    }
                }
            });
        }
    }

    private void writeUserToDB() {
        FirebaseUser user = mAuth.getCurrentUser();
        User nuser = new User(user.getUid(),user.getEmail());
        mDatabaseRef.child("users").child(user.getUid()).setValue(nuser);
    }

    public boolean validateRegistrationSubmission(String itsc, String password) {
        boolean success = true;
        boolean itscErrorFlag = false;
        boolean pwErrorFlag = false;

        if (itsc.isEmpty()) {
            tvWarningInvalidITSC.setText("ITSC account is required.");
            itscErrorFlag = true;
            success = false;
        }
        else if (!itsc.matches("[a-z]+")) {
            tvWarningInvalidITSC.setText("invalid ITSC account.");
            itscErrorFlag = true;
            success = false;
        }

        if (password.isEmpty()) {
            tvWarningInvalidPW.setText("password is required.");
            pwErrorFlag = true;
            success = false;
        }
        else if (password.length() < 7) {
            tvWarningInvalidPW.setText("length of the password should be at least 7.");
            pwErrorFlag = true;
            success = false;
        }

        if (itscErrorFlag) {
            etITSC.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            tvWarningInvalidITSC.setVisibility(View.VISIBLE);
        }
        else {
            etITSC.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidITSC.setVisibility(View.GONE);
        }

        if (pwErrorFlag) {
            etPW.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            tvWarningInvalidPW.setVisibility(View.VISIBLE);
        }
        else {
            etPW.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidPW.setVisibility(View.GONE);
        }

        return success;
    }

    public void clearWarningMessages() {
        tvWarningInvalidITSC.setVisibility(View.GONE);
        tvWarningInvalidPW.setVisibility(View.GONE);
    }

}
