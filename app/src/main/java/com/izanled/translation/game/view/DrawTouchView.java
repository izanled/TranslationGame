package com.izanled.translation.game.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
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
import com.izanled.translation.game.utils.NaverNMTTranslatorTask;
import com.izanled.translation.game.utils.ToastManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

public class DrawTouchView extends View {
    private static final String TAG = DrawTouchView.class.getSimpleName();

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Context mContext;
    EventListener eventListener;

    private float initL, initT, initB, initR;
    private boolean drawing = false;

    public Bitmap screenShotBitmap = null;

    boolean GaRoReverse = false;
    boolean SeRoReverse = false;

    long myBaseTime;

    public DrawTouchView(Context context,  EventListener eventListener) {
        super(context);
        this.eventListener = eventListener;
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
        if (screenShotBitmap != null){
            try {
                Rect rect = new Rect(0,0, screenShotBitmap.getWidth(), screenShotBitmap.getHeight());
                canvas.drawBitmap(screenShotBitmap, null, rect, null);
                canvas.save();
            }catch (Exception e){
                ToastManager.getInstance().showLongTosat(mContext.getString(R.string.error_capture));
            }
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
                Bitmap output = Bitmap.createBitmap(screenShotBitmap, Math.round(initL), Math.round(initT), Math.round(initR-initL), Math.round(initB-initT));
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

        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(Arrays.asList("zh", "en","ja")).build();
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer(options);

        textRecognizer.processImage(image).addOnSuccessListener(result -> {
                    final String original = result.getText().replaceAll("(\r\n|\r|\n|\n\r)", " ").trim();

                    if(original.length() > 0){

                        if(CommonData.getInstance().getTransApi() == 0){
                            if(original.length()+CommonData.CONSUMPTION_POINTS_OCR <= CommonData.getInstance().getCurUser().getPoint()){

                                new GoogleTranslatorTask(mContext, result1 -> {
                                    if(CommonData.getInstance().getIsShowOriginal()){
                                        String lastStr = mContext.getString(R.string.original) + " : " + original + "\n" + mContext.getString(R.string.translation) + " : " + result1;

                                        LastTextData lastTextData = new LastTextData(lastStr);

                                        EventBus.getDefault().post(lastTextData);
                                    }else{
                                        LastTextData lastTextData = new LastTextData(result1);

                                        EventBus.getDefault().post(lastTextData);
                                    }

                                    FirebaseFirestore.getInstance().collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId())
                                            .update(CommonData.FIELD_POINT, CommonData.getInstance().getCurUser().getPoint()-(original.trim().length()+CommonData.CONSUMPTION_POINTS_OCR));

                                }).execute(new String[]{original, CommonData.getInstance().getmTransferTargetImg()});
                            }else{
                                ToastManager.getInstance().showLongTosat(mContext.getString(R.string.msg_missing_points).replace("[point]", String.valueOf(CommonData.CONSUMPTION_POINTS_OCR)));
                            }
                        }else{
                            if(CommonData.CONSUMPTION_POINTS_OCR <= CommonData.getInstance().getCurUser().getPoint()){
                                new NaverNMTTranslatorTask(mContext, result12 -> {
                                    if(CommonData.getInstance().getIsShowOriginal()){
                                        String lastStr = mContext.getString(R.string.original) + " : " + original + "\n" + mContext.getString(R.string.translation) + " : " + result12;

                                        LastTextData lastTextData = new LastTextData(lastStr);

                                        EventBus.getDefault().post(lastTextData);
                                    }else{
                                        LastTextData lastTextData = new LastTextData(result12);

                                        EventBus.getDefault().post(lastTextData);
                                    }
                                    FirebaseFirestore.getInstance().collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId())
                                            .update(CommonData.FIELD_POINT, CommonData.getInstance().getCurUser().getPoint()-CommonData.CONSUMPTION_POINTS_OCR);

                                }).execute(new String[]{original, CommonData.getInstance().getmTransferTargetImg()});

                             /*new NaverSMTTranslatorTask(mContext, result12 -> {
                                if(CommonData.getInstance().getIsShowOriginal()){
                                    String lastStr = mContext.getString(R.string.original) + " : " + original + "\n" + mContext.getString(R.string.translation) + " : " + result12;

                                    LastTextData lastTextData = new LastTextData(lastStr);

                                    EventBus.getDefault().post(lastTextData);
                                }else{
                                    LastTextData lastTextData = new LastTextData(result12);

                                    EventBus.getDefault().post(lastTextData);
                                }

                                FirebaseFirestore.getInstance().collection(CommonData.COLLECTION_USERS).document(CommonData.getInstance().getUserDocId()).update("point", CommonData.getInstance().getCurUser().getPoint()-original.trim().length());
                            }).execute(new String[]{original, CommonData.getInstance().getmTransferTargetImg()});*/
                            }else{
                                ToastManager.getInstance().showLongTosat(mContext.getString(R.string.msg_missing_points_papago).replace("[point]", String.valueOf(CommonData.CONSUMPTION_POINTS_OCR)));
                            }
                        }


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

            myBaseTime = SystemClock.elapsedRealtime();
        } else if (action == MotionEvent.ACTION_UP) {
            //drawing = false;
            int x_up = (int)(event.getX() - initL);    //이동한 거리
            int y_up = (int)(event.getY() - initT);    //이동한 거리
            Log.d(TAG, "X 축 이동 거리 = " + x_up);
            Log.d(TAG, "Y 축 이동 거리 = " + y_up);
            if(Math.abs(x_up) < 10 && Math.abs(y_up)  < 10){
                long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간(??)^^;
                long outTime = now - myBaseTime;

                Log.d(TAG, "outTime = " + outTime);

                if(outTime >= 1000L){
                    eventListener.onClose();
                    return false;
                }
            }

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

    public interface EventListener{
        void onClose();
    }
}
