package com.example.ustchat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class InfoDialog extends Dialog {
    private String title;
    private String info;

    public InfoDialog(final Context context, String title, String info) {
        super(context);
        this.title = title;
        this.info = info;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_info);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitle = findViewById(R.id.tv_info_title);
        tvTitle.setText(title);
        TextView tvInfo = findViewById(R.id.tv_info_text);
        tvInfo.setText(info);
        if (Build.VERSION.SDK_INT >= 26) {
            tvInfo.setJustificationMode(0x00000001);
        }
        MaterialButton mbOK = findViewById(R.id.btn_info_ok);

        mbOK.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
