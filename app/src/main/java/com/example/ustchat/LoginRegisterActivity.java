package com.example.ustchat;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class LoginRegisterActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView tvContinue;
    TextView tvForgotPW;
    Button btnLogin;
    Button btnSignup;
    EditText etEmail;
    EditText etPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        toolbar = findViewById(R.id.login_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        btnLogin = findViewById(R.id.btn_login_login);
        btnSignup = findViewById(R.id.btn_login_sign_up);

        tvContinue = findViewById(R.id.tv_login_continue_as_a_visitor);
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

}

class RegisterDialog extends Dialog {
    private ImageButton ibRegister;
    private EditText etITSC;
    private EditText etPW;
    private TextView tvWarningInvalidITSC;
    private TextView tvWarningInvalidPW;

    public RegisterDialog(final Context context) {
        super(context);
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
        String istc = etITSC.getText().toString();
        String password = etPW.getText().toString();
        if (validateRegistrationSubmission(istc, password)) {

        }
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
