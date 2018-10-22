package com.izanled.translation.game.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.utils.ToastManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatView extends LinearLayout {

    Context mContext;
    EventListener eventListener;

    public ChatView(Context context, EventListener listener) {
        super(context);
        init(context);
        eventListener = listener;
    }

    public void init(Context context){
        mContext = context;
        LayoutInflater m_inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = m_inflater.inflate(R.layout.caht_layout, this);
        ButterKnife.bind(this, view);

       btn_close_chat.setOnClickListener(v -> eventListener.onClose());

        et_chat.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                if(et_chat.getText().toString().trim().length() <= CommonData.getInstance().getCurUser().getPoint()){
                    eventListener.requestText(et_chat.getText().toString().trim());
                }else{
                    ToastManager.getInstance().showLongTosat(mContext.getString(R.string.msg_missing_points));
                }
            }
            return false;
        });
    }

    public EditText getEt_chat() {
        return et_chat;
    }

    public interface EventListener{
        void requestText(String text);
        void onClose();
    }

    @BindView(R.id.et_chat)    EditText et_chat;
    @BindView(R.id.btn_close_chat)
    ImageButton btn_close_chat;
}
