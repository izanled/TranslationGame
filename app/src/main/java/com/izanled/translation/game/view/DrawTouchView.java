package com.izanled.translation.game.view;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.eventbus.LastTextData;
import com.izanled.translation.game.utils.GoogleTranslatorTask;
import com.izanled.translation.game.utils.ToastManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

public class DrawTouchView extends View {
    private static final String TAG = DrawTouchView.class.getSimpleName();

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Context mContext;

    private float initL, initT, initB, initR;
    private boolean drawing = false;

    public Bitmap dump = null;

    boolean GaRoReverse = false;
    boolean SeRoReverse = false;

    public DrawTouchView(Context context) {
        super(context);
        init(context);
    }

    public DrawTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DrawTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context){
        mContext = context;
        //paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.RED);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dump != null){
            Rect rect = new Rect(0,0, dump.getWidth(), dump.getHeight());
            canvas.drawBitmap(dump, null, rect, null);
            canvas.save();
        }

        if (drawing) {
            if(GaRoReverse){
                // 위쪽 가로 선
                canvas.drawLine(initR, initT, initL,initT, paint);
                // 아래쪽 가로 선
                canvas.drawLine(initR, initB, initL, initB, paint);
            }else{
                // 위쪽 가로 선
                canvas.drawLine(initL, initT, initR,initT, paint);
                // 아래쪽 가로 선
                canvas.drawLine(initL, initB, initR, initB, paint);
            }

            if(SeRoReverse){
                // 앞쪽 세로 선
                canvas.drawLine(initL, initB, initL, initT, paint);
                // 뒤쪽 세로 선
                canvas.drawLine(initR, initB, initR, initT, paint);
            }else{
                // 앞쪽 세로 선
                canvas.drawLine(initL, initT, initL, initB, paint);
                // 뒤쪽 세로 선
                canvas.drawLine(initR, initT, initR, initB, paint);
            }

            canvas.save();
        }
    }

    public void getCropArea(){
        if(drawing){
            try {
                Bitmap output = Bitmap.createBitmap(dump, Math.round(initL), Math.round(initT), Math.round(initR-initL), Math.round(initB-initT));
                processImage(output);
            }catch (Exception e){
                e.printStackTrace();
                ToastManager.getInstance().showShortTosat(mContext.getString(R.string.out_area));
            }
        }else{
            ToastManager.getInstance().showShortTosat(mContext.getString(R.string.select_area));
        }
    }

    private void processImage(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(Arrays.asList("ca", "hi")).build();
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer(options);

        textRecognizer.processImage(image).addOnSuccessListener(result -> {
                    final String original = result.getText();

                    if(original.length() > 0){

                        if(original.trim().length() <= CommonData.getInstance().getCurUser().getPoint()){

                            new GoogleTranslatorTask(mContext, result1 -> {
                                if(CommonData.getInstance().getIsShowOriginal()){
                                    String lastStr = mContext.getString(R.string.original) + " : " + original + "\n" + mContext.getString(R.string.translation) + " : " + result1;

                                    LastTextData lastTextData = new LastTextData(lastStr);

                                    EventBus.getDefault().post(lastTextData);
                                }else{
                                    LastTextData lastTextData = new LastTextData(result1);

                                    EventBus.getDefault().post(lastTextData);
                                }

                                FirebaseFirestore.getInstance().collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId()).update("point", CommonData.getInstance().getCurUser().getPoint()-original.trim().length());

                            }).execute(new String[]{original, CommonData.getInstance().getmTransferTargetImg()});

                        }else{
                            ToastManager.getInstance().showLongTosat(mContext.getString(R.string.msg_missing_points));
                        }

                        new GoogleTranslatorTask(mContext, result1 -> {
                            if(CommonData.getInstance().getIsShowOriginal()){
                                String lastStr = mContext.getString(R.string.original) + " : " + original + "\n" + mContext.getString(R.string.translation) + " : " + result1;

                                LastTextData lastTextData = new LastTextData(lastStr);

                                EventBus.getDefault().post(lastTextData);
                            }else{
                                LastTextData lastTextData = new LastTextData(result1);

                                EventBus.getDefault().post(lastTextData);
                            }

                        }).execute(new String[]{original, CommonData.getInstance().getmTransferTargetImg()});
                    }else{
                        ToastManager.getInstance().showLongTosat(mContext.getString(R.string.dont_find_text));
                    }
                })
                .addOnFailureListener( e -> {ToastManager.getInstance().showLongTosat(mContext.getString(R.string.unknown_error));});
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            initR = event.getX();
            initB = event.getY();

            if(initL > initR)
                GaRoReverse = true;
            else
                GaRoReverse = false;

            if(initT > initB)
                SeRoReverse = true;
            else
                SeRoReverse = false;
            //Log.d("AAAAAAAAAA", "initB: " + initB + "initR: " + initR);
        } else if (action == MotionEvent.ACTION_DOWN) {
            initL = event.getX();
            initT = event.getY();
            initR = event.getX();
            initB = event.getY();
            //Log.d("AAAAAAAAAA", "initL: " + initL + "initT: " + initT);
            drawing = true;
        } else if (action == MotionEvent.ACTION_UP) {
            //drawing = false;
            if(initL > initR){
                float tump = initR;
                initR = initL;
                initL = tump;
            }
            if(initT > initB){
                float tump = initB;
                initB = initT;
                initT = tump;
            }
            performClick();
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
