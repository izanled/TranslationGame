package com.izanled.translation.game.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.ui.auth.data.model.User;
import com.izanled.translation.game.data.UserData;

public class CommonData {
    private Context mContext;
    private static CommonData	_instance;

    public CommonData(){}

    public CommonData(Context context)
    {
        this.mContext	=	context;
    }

    public static final String COLLECTION_USERS = "users";
    public static final String FIELD_EMAIL = "email";


    private String imgPath;
    private boolean isStarted = false;
    private String mSourceLang = "zh";
    private String mTargetLang = "ko";

    private UserData curUser;

    private String mTransferTargetImg = "img";
    private String mTransferTargetTxt = "txt";

    /**
     * CommonData 인스턴스 리턴
     * @return CommonData
     */
    public static CommonData getInstance()
    {
        if (_instance == null)
        {
            synchronized (CommonData.class)
            {
                if(_instance == null)
                {
                    _instance = new CommonData();
                }
            }
        }
        return _instance;
    }

    /**
     * CommonData 인스턴스 리턴
     * @param context context
     * @return CommonData
     */
    public static CommonData getInstance(Context context)
    {
        if (_instance == null)
        {
            synchronized (CommonData.class)
            {
                if(_instance == null)
                {
                    _instance	=	new CommonData(context);
                }
            }
        }
        return _instance;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public String getmSourceLang() {
        return mSourceLang;
    }

    public void setmSourceLang(String mSourceLang) {
        this.mSourceLang = mSourceLang;
    }

    public String getmTargetLang() {
        return mTargetLang;
    }

    public void setmTargetLang(String mTargetLang) {
        this.mTargetLang = mTargetLang;
    }

    public String getmTransferTargetImg() {
        return mTransferTargetImg;
    }

    public void setmTransferTargetImg(String mTransferTargetImg) {
        this.mTransferTargetImg = mTransferTargetImg;
    }

    public String getmTransferTargetTxt() {
        return mTransferTargetTxt;
    }

    public void setmTransferTargetTxt(String mTransferTargetTxt) {
        this.mTransferTargetTxt = mTransferTargetTxt;
    }

    public UserData getCurUser() {
        return curUser;
    }

    public void setCurUser(UserData curUser) {
        this.curUser = curUser;
    }

    /**
     * preferences 삭제
     */
    public void deletePreferences()
    {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.clear();
        editor.commit();
    }

    /**
     * preference 권한 얻기
     * @return
     */
    public SharedPreferences getSharedPreference()								// 데이터 가져오기
    {
        return mContext.getSharedPreferences("TRANS_DATA", Activity.MODE_PRIVATE);
    }

    /**
     * String 타입의 preferencedata 를 저장한다.
     * @param key 키값
     * @param value 저장할 데이터
     */
    public void setSharedPreferenceData(String key, String value)
    {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * int 타입의 preferencedata 를 저장한다.
     *
     * @param key 키값
     * @param value 저장할 데이터
     */
    public void setSharedPreferenceData(String key, int value)
    {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * boolean 타입의 preferencedata 를 저장한다.
     *
     * @param key 키값
     * @param value 저장할 데이터
     */
    public void setSharedPreferenceData(String key, boolean value)
    {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    // 번역 할 언어
    public void setSourceLang(int sourceLang){setSharedPreferenceData("mSourceLang", sourceLang);}
    public int getSourceLang(){return getSharedPreference().getInt("mSourceLang",0);}

    // 번역 결과 언어
    public void setTargetLang(int targetLang){setSharedPreferenceData("mTargetLang", targetLang);}
    public int getTargetLang(){return getSharedPreference().getInt("mTargetLang",4);}

    // 화면 방향
    public void setOrientation(boolean orientation){setSharedPreferenceData("mOrientation", orientation);}
    public boolean getOrientation(){return getSharedPreference().getBoolean("mOrientation",false);}

    // 원문 표시 여부
    public void setIsShowOriginal(boolean isShowOriginal){setSharedPreferenceData("isShowOriginal", isShowOriginal);}
    public boolean getIsShowOriginal(){return getSharedPreference().getBoolean("isShowOriginal",false);}
}
