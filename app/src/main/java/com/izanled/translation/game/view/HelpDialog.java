package com.izanled.translation.game.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.izanled.translation.game.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpDialog extends Dialog implements View.OnClickListener{
    public HelpDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.help_layout);
        ButterKnife.bind(this);

        btn_close.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = getWindow().getAttributes();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);
    }

    @BindView(R.id.btn_close)    Button btn_close;

    @Override
    public void onClick(View v) {
        this.dismiss();
    }
}
