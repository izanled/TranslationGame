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
    public static final String FIELD_POINT = "point";

    public static final int CONSUMPTION_POINTS_OCR = 20;
    public static final int REWOAD_POINTS = 150;

    private boolean isServiceRun = false;
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


    public boolean isServiceRun() {
        return isServiceRun;
    }

    public void setServiceRun(boolean serviceRun) {
        isServiceRun = serviceRun;
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

    // 유저 고유 아이디
    public void setUserDocId(String userDocId){setSharedPreferenceData("mUserDocId", userDocId);}
    public String getUserDocId(){return getSharedPreference().getString("mUserDocId",null);}

    // 번역 할 언어
    public void setSourceLang(int sourceLang){setSharedPreferenceData("mSourceLang", sourceLang);}
    public int getSourceLang(){return getSharedPreference().getInt("mSourceLang",0);}

    // 번역 결과 언어
    public void setTargetLang(int targetLang){setSharedPreferenceData("mTargetLang", targetLang);}
    public int getTargetLang(){return getSharedPreference().getInt("mTargetLang",4);}

    // 번역 도구 선택
    public void setTransApi(int transApi){setSharedPreferenceData("mTransApi", transApi);}
    public int getTransApi(){return getSharedPreference().getInt("mTransApi",0);}

    // 원문 표시 여부
    public void setIsShowOriginal(boolean isShowOriginal){setSharedPreferenceData("isShowOriginal", isShowOriginal);}
    public boolean getIsShowOriginal(){return getSharedPreference().getBoolean("isShowOriginal",false);}

    // 이미지 사이즈
    public void setImgSize(boolean isImgSizeBig){setSharedPreferenceData("mImgSize", isImgSizeBig);}
    public boolean getImgSize(){return getSharedPreference().getBoolean("mImgSize",true);}

    // 파파고 클라이언트 아이디
    public void setClientId(String clientId){setSharedPreferenceData("mClientId", clientId);}
    public String getClientId(){return getSharedPreference().getString("mClientId","");}

    // 파파고 시크릿 아이디
    public void setSecretId(String secretId){setSharedPreferenceData("mSecretId", secretId);}
    public String getSecretId(){return getSharedPreference().getString("mSecretId","");}

    // 공지 봤는지 유무
    public void setIsShowNotice(boolean isShowNotice){setSharedPreferenceData("mIsShowNotice", isShowNotice);}
    public boolean IsShowNotice(){return getSharedPreference().getBoolean("mIsShowNotice",false);}
}