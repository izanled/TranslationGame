package com.izanled.translation.game.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.data.UserData;
import com.izanled.translation.game.eventbus.BitmapData;
import com.izanled.translation.game.eventbus.LastTextData;
import com.izanled.translation.game.sqlite.DBManager;
import com.izanled.translation.game.sqlite.SqlManager;
import com.izanled.translation.game.utils.GoogleTranslatorTask;
import com.izanled.translation.game.utils.ToastManager;
import com.izanled.translation.game.view.DrawTouchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TranslationService extends Service implements View.OnClickListener {
    private static final String TAG = TranslationService.class.getSimpleName();

    private FirebaseFirestore db;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    String lastText = null;
    boolean isOpen = false;

    int MAX_X = -1, MAX_Y = -1;
    float START_X, START_Y;
    int PREV_X, PREV_Y;

    DBManager mDb;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            EventBus.getDefault().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        db = FirebaseFirestore.getInstance();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);  //윈도우 매니저
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mDb = new DBManager(this, "TansferGame", null, 1);
        SqlManager.shared(this, mDb);

        initCanvasView();
        initTextView(inflater);
        initChat(inflater);
        initActionBtnView(inflater);
        initTvPointView();


        try{
            db.collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId()).addSnapshotListener((documentSnapshot, e) -> {
                CommonData.getInstance().setCurUser(documentSnapshot.toObject(UserData.class));
                tv_point.setText(String.valueOf(CommonData.getInstance().getCurUser().getPoint()) + " P");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 액션 버튼 그리기
     * @param layoutInflater
     */
    private void initActionBtnView(LayoutInflater layoutInflater){

        mActionBtnView = layoutInflater.inflate(R.layout.action_btn_layout, null);

        mActionBtnView.setVisibility(View.VISIBLE);

        btn_screen_shot = mActionBtnView.findViewById(R.id.btn_screen_shot);
        btn_capture = mActionBtnView.findViewById(R.id.btn_capture);
        btn_chat = mActionBtnView.findViewById(R.id.btn_chat);
        btn_last_text = mActionBtnView.findViewById(R.id.btn_last_text);
        btn_close = mActionBtnView.findViewById(R.id.btn_close);
        btn_arrow = mActionBtnView.findViewById(R.id.btn_arrow);

        btn_capture.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        btn_chat.setOnClickListener(this);
        btn_last_text.setOnClickListener(this);
        btn_arrow.setOnClickListener(this);

        btn_capture.setVisibility(View.GONE);
        btn_close.setVisibility(View.GONE);
        btn_chat.setVisibility(View.GONE);
        btn_last_text.setVisibility(View.GONE);

        btn_screen_shot.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:                //사용자 터치 다운이면
                    START_X = event.getRawX();                    //터치 시작 점
                    START_Y = event.getRawY();                    //터치 시작 점
                    PREV_X = mParamsBtn.x;                            //뷰의 시작 점
                    PREV_Y = mParamsBtn.y;                            //뷰의 시작 점
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int)(event.getRawX() - START_X);    //이동한 거리
                    int y = (int)(event.getRawY() - START_Y);    //이동한 거리

                    //터치해서 이동한 만큼 이동 시킨다
                    mParamsBtn.x = PREV_X + x;
                    mParamsBtn.y = PREV_Y + y;

                    //optimizePosition();        //뷰의 위치 최적화
                    mWindowManager.updateViewLayout(mActionBtnView, mParamsBtn);    //뷰 업데이트
                    break;
                case MotionEvent.ACTION_UP:

                    int x_up = (int)(event.getRawX() - START_X);    //이동한 거리
                    int y_up = (int)(event.getRawY() - START_Y);    //이동한 거리
                    if(Math.abs(x_up) < 10 && Math.abs(y_up)  < 10){
                        // 버튼 눌림 처리
                        try{
                            mActionBtnView.setVisibility(View.GONE);
                            ToastManager.getInstance().showShortTosat(getString(R.string.msg_help_service));
                            CommonData.getInstance().setStarted(true);

                            mActionBtnView.postDelayed(() -> {
                                mActionBtnView.setVisibility(View.VISIBLE);
                                btn_screen_shot.setVisibility(View.VISIBLE);
                                btn_capture.setVisibility(View.VISIBLE);
                                btn_close.setVisibility(View.VISIBLE);
                                tv_point.setVisibility(View.VISIBLE);
                            }, 500l);

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                    break;
            }
            return true;
        });

        //최상위 윈도우에 넣기 위한 설정
        mParamsBtn = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,//항상 최 상위. 터치 이벤트 받을 수 있음.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  //포커스를 가지지 않음
                PixelFormat.TRANSLUCENT);                                        //투명
        mParamsBtn.gravity = Gravity.LEFT | Gravity.TOP;                   //왼쪽 상단에 위치하게 함.
        mWindowManager.addView(mActionBtnView, mParamsBtn);      //윈도우에 뷰 넣기. permission 필요.
    }

    /**
     * 사각형 그리기 뷰 세팅
     */
    public void initCanvasView(){
        mCanvasView = new DrawTouchView(this);
        mCanvasView.setBackgroundColor(getColor(R.color.alaphGray));

        mCanvasView.setVisibility(View.GONE);

        //최상위 윈도우에 넣기 위한 설정
        mParamsCanvas = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,//항상 최 상위. 터치 이벤트 받을 수 있음.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  //포커스를 가지지 않음
                PixelFormat.TRANSLUCENT);                                        //투명
        mWindowManager.addView(mCanvasView, mParamsCanvas);      //윈도우에 뷰 넣기. permission 필요.
    }

    /**
     * 포인트 표시
     */
    public void initTvPointView(){
        tv_point = new TextView(this);
        tv_point.setTextColor(Color.WHITE);
        tv_point.setVisibility(View.GONE);

        mParamsPoint = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParamsPoint.gravity = Gravity.RIGHT | Gravity.TOP;
        mParamsPoint.horizontalMargin = 0.05f;
        mWindowManager.addView(tv_point, mParamsPoint);      //윈도우에 뷰 넣기. permission 필요.

        //db.collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getCurUser()._docId).addSnapshotListener((documentSnapshot, e) -> tv_point.setText(documentSnapshot.toObject(UserData.class).getPoint()));
    }

    /**
     * 텍스트 박스 초기화
     * @param layoutInflater
     */
    private void initTextView(LayoutInflater layoutInflater){
        mTextView = layoutInflater.inflate(R.layout.text_view, null);
        mTextView.setVisibility(View.GONE);

        tv_text_box = mTextView.findViewById(R.id.tv_text_box);

        tv_text_box.setOnClickListener(this);

        mParamsText = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,//항상 최 상위. 터치 이벤트 받을 수 있음.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  //포커스를 가지지 않음
                PixelFormat.TRANSLUCENT);
        mParamsText.gravity = Gravity.BOTTOM | Gravity.CENTER;                   //하단 중앙에 위치하게 함.
        mParamsText.verticalMargin = 0.1f;
        mWindowManager.addView(mTextView, mParamsText);      //윈도우에 뷰 넣기. permission 필요.
    }

    /**
     * 채팅 창 초기화
     */
    private void initChat(LayoutInflater layoutInflater){
        mChatView = layoutInflater.inflate(R.layout.caht_layout, null);
        et_chat = mChatView.findViewById(R.id.et_chat);
        btn_close_chat = mChatView.findViewById(R.id.btn_close_chat);
        et_chat.setOnEditorActionListener((textView, i, keyEvent) -> {
            if(i == EditorInfo.IME_ACTION_DONE){

                if(et_chat.getText().toString().trim().length() <= CommonData.getInstance().getCurUser().getPoint()){
                    new GoogleTranslatorTask(this, result -> {
                        ClipData clipData = ClipData.newPlainText("label", result);
                        clipboardManager.setPrimaryClip(clipData);
                        ToastManager.getInstance().showLongTosat(getString(R.string.copy_text));
                        db.collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId()).update("point", CommonData.getInstance().getCurUser().getPoint()-et_chat.getText().toString().trim().length());
                    }).execute(new String[]{et_chat.getText().toString().trim(), CommonData.getInstance().getmTransferTargetTxt()});
                }else{
                    ToastManager.getInstance().showLongTosat(getString(R.string.msg_missing_points));
                }

                mChatView.setVisibility(View.GONE);
                inputMethodManager.hideSoftInputFromInputMethod(et_chat.getWindowToken(), 0);
            }
            return false;
        });

        btn_close_chat.setOnClickListener(this);

        mChatView.setVisibility(View.GONE);

        mParamsChat = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,//항상 최 상위. 터치 이벤트 받을 수 있음.
                0,  //포커스를 가지지 않음
                PixelFormat.TRANSLUCENT);
        mParamsChat.gravity = Gravity.TOP | Gravity.CENTER;                   //왼쪽 상단에 위치하게 함.
        mWindowManager.addView(mChatView, mParamsChat);      //윈도우에 뷰 넣기. permission 필요.
    }

    /**
     * 클릭 이벤트
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
                mCanvasView.getCropArea();
                break;
            case R.id.btn_close:
                btn_capture.setVisibility(View.GONE);
                btn_close.setVisibility(View.GONE);
                mCanvasView.setVisibility(View.GONE);
                tv_point.setVisibility(View.GONE);
                break;
            case R.id.btn_last_text:
                tv_text_box.setText(lastText);
                mTextView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_arrow:
                if(isOpen){
                    isOpen = false;
                    btn_arrow.setImageResource(android.R.drawable.arrow_down_float);
                    btn_chat.setVisibility(View.GONE);
                    btn_last_text.setVisibility(View.GONE);
                    tv_point.setVisibility(View.GONE);
                }else{
                    isOpen = true;
                    btn_arrow.setImageResource(android.R.drawable.arrow_up_float);
                    btn_chat.setVisibility(View.VISIBLE);
                    btn_last_text.setVisibility(View.VISIBLE);
                    tv_point.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_chat:
                mChatView.setVisibility(View.VISIBLE);
                et_chat.setText("");
                inputMethodManager.showSoftInput(et_chat, InputMethodManager.SHOW_FORCED);
                break;
            case R.id.btn_close_chat:
                mChatView.setVisibility(View.GONE);
                inputMethodManager.hideSoftInputFromWindow(et_chat.getWindowToken(), 0);
                break;
            case R.id.tv_text_box:
                mTextView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onDestroy() {
        if(mWindowManager != null) {        //서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.
            if(mActionBtnView != null) mWindowManager.removeView(mActionBtnView);
            if(mCanvasView != null) mWindowManager.removeView(mCanvasView);
            if(mTextView != null) mWindowManager.removeView(mTextView);
            if(mChatView != null) mWindowManager.removeView(mChatView);
            if(tv_point != null)    mWindowManager.removeView(tv_point);
        }
        super.onDestroy();
    }

    /**
     * 뷰의 위치가 화면 안에 있게 최대값을 설정한다
     */
    private void setMaxPosition() {
        Log.d(TAG, "setMaxPosition()");
        DisplayMetrics matrix = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(matrix);		//화면 정보를 가져와서

        MAX_X = matrix.widthPixels - mActionBtnView.getWidth();			//x 최대값 설정
        MAX_Y = matrix.heightPixels - mActionBtnView.getHeight();			//y 최대값 설정
    }

    /**
     * 뷰의 위치가 화면 안에 있게 하기 위해서 검사하고 수정한다.
     */
    private void optimizePosition() {
        //최대값 넘어가지 않게 설정
        if(mParamsBtn.x > MAX_X) mParamsBtn.x = MAX_X;
        if(mParamsBtn.y > MAX_Y) mParamsBtn.y = MAX_Y;
        if(mParamsBtn.x < 0) mParamsBtn.x = 0;
        if(mParamsBtn.y < 0) mParamsBtn.y = 0;
    }

    /**
     * 가로 / 세로 모드 변경 시 최대값 다시 설정해 주어야 함.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        setMaxPosition();		//최대값 다시 설정
        optimizePosition();		//뷰 위치 최적화
    }

    /**
     * 이미지 전달 후 실행 핸들러
     */
    final Handler mImgHandler = new Handler(){
        public void handleMessage(Message msg){
            mCanvasView.setVisibility(View.VISIBLE);
            mCanvasView.invalidate();
            mCanvasView.requestLayout();
        }
    };

    /**
     * 이미지 전달 왔을 때
     */
    @Subscribe
    public void onEvent(BitmapData data){
        if(mCanvasView.dump != null) mCanvasView.dump.recycle();

        mCanvasView.dump = data.getBitmap();
        mImgHandler.sendEmptyMessage(1);
    }

    /**
     * 텍스트 전달 후 실행 핸들러
     */
    final Handler mTextHandler = new Handler(){
        public void handleMessage(Message msg){
            tv_text_box.setText(lastText);
            mTextView.setVisibility(View.VISIBLE);
        }
    };

    /**
     * 텍스트 전달 왔을 때
     */
    @Subscribe
    public void onEvent(LastTextData data){
        lastText = data.getLastText();
        mTextHandler.sendEmptyMessage(1);
    }

    private WindowManager.LayoutParams mParamsBtn;  //layout params 객체. 뷰의 위치 및 크기
    private WindowManager.LayoutParams mParamsCanvas;  //layout params 객체. 뷰의 위치 및 크기
    private WindowManager.LayoutParams mParamsText;  //layout params 객체. 뷰의 위치 및 크기
    private WindowManager.LayoutParams mParamsChat;  //layout params 객체. 뷰의 위치 및 크기
    private WindowManager.LayoutParams mParamsPoint;  //layout params 객체. 뷰의 위치 및 크기
    private WindowManager mWindowManager;          //윈도우 매니저
    private InputMethodManager inputMethodManager;
    private ClipboardManager clipboardManager;
    private LayoutInflater inflater;
    private View mActionBtnView;
    private View mTextView;
    private View mChatView;
    private DrawTouchView mCanvasView;

    TextView tv_text_box;

    ImageButton btn_screen_shot;
    ImageButton btn_capture;
    ImageButton btn_close;
    ImageButton btn_last_text;
    ImageButton btn_arrow;
    ImageButton btn_chat;

    EditText et_chat;
    ImageButton btn_close_chat;

    TextView tv_point;
}
