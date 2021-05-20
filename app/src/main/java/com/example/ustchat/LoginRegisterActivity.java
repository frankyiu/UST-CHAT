package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;



public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "LoginRegisterActivity";
    Toolbar toolbar;
    TextView tvContinue;
    TextView tvForgotPW;
    TextView tvWarningInvalidITSC;
    TextView tvWarningInvalidPW;
    TextView tvWarningLoginFailure;
    Button btnLogin;
    Button btnSignup;
    EditText etITSC;
    EditText etPW;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        toolbar = findViewById(R.id.toolbar_center_title);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        btnLogin = findViewById(R.id.btn_login_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnSignup = findViewById(R.id.btn_login_sign_up);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterDialog();
            }
        });

        tvContinue = findViewById(R.id.tv_login_continue_as_a_visitor);
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TO-DO: no login
                switchToGeneralCourseActicity();
            }
        });

        etITSC = findViewById(R.id.et_login_itsc);
        etPW = findViewById(R.id.et_login_password);
        tvForgotPW = findViewById(R.id.tv_login_forgot_password);
        tvForgotPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPasswordDialog();
            }
        });

        tvWarningInvalidITSC = findViewById(R.id.tv_login_warning_itsc);
        tvWarningInvalidITSC.setVisibility(View.GONE);
        tvWarningInvalidPW = findViewById(R.id.tv_login_warning_password);
        tvWarningInvalidPW.setVisibility(View.GONE);
        tvWarningLoginFailure = findViewById(R.id.tv_login_warning_login_failure);
        tvWarningLoginFailure.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()){
            switchToGeneralCourseActicity();
        }
    }

    private void testing(){
        FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();

                            // Log and toast
                            Log.d(TAG, token);
                        }
                    });
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
            openInfoDialog();
        }
        return true;
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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    public void login() {
        String istc = etITSC.getText().toString();
        String password = etPW.getText().toString();
        if (validateLoginSubmission(istc, password)) {
            mAuth.signInWithEmailAndPassword(istc, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Login:success");
                                if(mAuth.getCurrentUser().isEmailVerified())
                                    switchToGeneralCourseActicity();
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

    public boolean validateLoginSubmission(String itsc, String password) {
        boolean success = true;

        if (itsc.isEmpty()) {
            tvWarningInvalidITSC.setText(R.string.et_warning_login_ITSC_empty);
            tvWarningInvalidITSC.setVisibility(View.VISIBLE);
            etITSC.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            success = false;
        }
        else {
            etITSC.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidITSC.setVisibility(View.GONE);
        }

        if (password.isEmpty()) {
            tvWarningInvalidPW.setText(R.string.et_warning_login_password_empty);
            tvWarningInvalidPW.setVisibility(View.VISIBLE);
            etPW.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            success = false;
        }
        else {
            etPW.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidPW.setVisibility(View.GONE);
        }

        // TO-DO : check if the login records exist in the database
        if (success) {
            boolean existInDatabase = true;
            // ....

            if (existInDatabase) {
                tvWarningLoginFailure.setVisibility(View.GONE);
            }
            else {
                tvWarningInvalidPW.setText(R.string.et_warning_login_failure);
                tvWarningLoginFailure.setVisibility(View.VISIBLE);
                success = false;
            }
        }

        return success;
    }

    public void openRegisterDialog() {
        RegisterDialog dialogRegister = new RegisterDialog(this);
        dialogRegister.show();
    }

    public void openForgotPasswordDialog() {
        ForgotPWDialog dialogForgotPW = new ForgotPWDialog(this);
        dialogForgotPW.show();
    }

    // TO-DO
    public void handleForgetPW() {
//        String emailAddress = etEmail.getText().toString().trim();
//
//        mAuth.sendPasswordResetEmail(emailAddress)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "Email sent.");
//                        }
//                    }
//                });
    }
    private void openInfoDialog() {
        InfoDialog dialogInfo = new InfoDialog(this, getResources().getString(R.string.ustchat_info_title),
                getResources().getString(R.string.ustchat_info_description));
        dialogInfo.show();
    }

    public void switchToGeneralCourseActicity() {
        startActivity(new Intent(getApplicationContext(), CourseActivity.class));
        overridePendingTransition(0, 0);
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
    private static final String TAG = "RegisterDialog";

    public RegisterDialog(final Context context) {
        super(context);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_create_account);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        etITSC = findViewById(R.id.et_create_account_itsc_account);
        etPW = findViewById(R.id.et_create_account_pw);
        ibRegister = findViewById(R.id.ib_create_account_submit);
        tvWarningInvalidITSC = findViewById(R.id.tv_create_account_warning_itsc_account);
        tvWarningInvalidPW = findViewById(R.id.tv_create_account_warning_pw);

        TextView tvDescription = findViewById(R.id.tv_create_account_description);
        if (Build.VERSION.SDK_INT >= 26) {
            tvDescription.setJustificationMode(0x00000001);
        }

        ibRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                register();
            }
        });

        clearWarningMessages();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getX(), (int)event.getY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    public void register() {
        String itsc = etITSC.getText().toString();
        String password = etPW.getText().toString();
        if (validateRegistrationSubmission(itsc, password)) {
            String email = itsc + "@connect.ust.hk";
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendVerifyEmail();
                                writeUserToDB();
                                Log.d(TAG, "createUserWithEmailAndPassword:success");
                            } else {
                                Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                            }
                        }
                    });
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
        String Uid = mAuth.getCurrentUser().getUid();
        mDatabaseRef.child("users/"+Uid+"/email/").setValue(mAuth.getCurrentUser().getEmail());
    }
    public boolean validateRegistrationSubmission(String itsc, String password) {
        boolean success = true;
        boolean itscErrorFlag = false;
        boolean pwErrorFlag = false;

        if (itsc.isEmpty()) {
            tvWarningInvalidITSC.setText(R.string.et_warning_login_ITSC_empty);
            itscErrorFlag = true;
            success = false;
        }
        else if (!itsc.matches("[a-z]+")) {
            tvWarningInvalidITSC.setText(R.string.et_warning_login_ITSC_invalid);
            itscErrorFlag = true;
            success = false;
        }

        if (password.isEmpty()) {
            tvWarningInvalidPW.setText(R.string.et_warning_login_password_empty);
            pwErrorFlag = true;
            success = false;
        }
        else if (password.length() < 7) {
            tvWarningInvalidPW.setText(R.string.et_warning_login_password_invalid);
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

class ForgotPWDialog extends Dialog {
    private ImageButton ibSubmit;
    private EditText etITSC;
    private TextView tvWarningInvalidITSC;

    public ForgotPWDialog(final Context context) {
        super(context);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_forgot_password);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        etITSC = findViewById(R.id.et_forgot_password_itsc_account);
        ibSubmit = findViewById(R.id.ib_forgot_password_submit);
        tvWarningInvalidITSC = findViewById(R.id.tv_forgot_password_warning_itsc_account);

        TextView tvDescription = findViewById(R.id.tv_forgot_password_description);
        if (Build.VERSION.SDK_INT >= 26) {
            tvDescription.setJustificationMode(0x00000001);
        }

        ibSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });

        tvWarningInvalidITSC.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getX(), (int)event.getY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    public void handleForgotPassword() {
        String istc = etITSC.getText().toString();
        if (validateForgotPasswordSubmission(istc)) {
            // TO-DO : help users reset password
        }
    }

    public boolean validateForgotPasswordSubmission(String itsc) {
        boolean success = true;

        if (itsc.isEmpty()) {
            tvWarningInvalidITSC.setText(R.string.et_warning_login_ITSC_empty);
            success = false;
        }
        else if (!itsc.matches("[a-z]+")) {
            tvWarningInvalidITSC.setText(R.string.et_warning_login_ITSC_invalid);
            success = false;
        }

        if (!success) {
            etITSC.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            tvWarningInvalidITSC.setVisibility(View.VISIBLE);
        }
        else {
            etITSC.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidITSC.setVisibility(View.GONE);
        }

        return success;
    }
}
