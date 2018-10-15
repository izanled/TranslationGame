package com.izanled.translation.game.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastManager {
    private static ToastManager	_instance;

    public Toast mToast;
    private Context mContext;

    public void showShortTosat(String msg){
        if(mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        }else{
            mToast.cancel();
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        }

        mToast.show();
    }

    public void showLongTosat(String msg){
        if(mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        }else{
            mToast.cancel();
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        }

        mToast.show();
    }

    public ToastManager(){}

    public ToastManager(Context context){ this.mContext = context; }

    /**
     * CommonData 인스턴스 리턴
     * @return CommonData
     */
    public static ToastManager getInstance()
    {
        if (_instance == null)
        {
            synchronized (ToastManager.class)
            {
                if(_instance == null)
                {
                    _instance = new ToastManager();
                }
            }
        }
        return _instance;
    }

    /**
     * ToastManager 인스턴스 리턴
     * @param context context
     * @return CommonData
     */
    public static ToastManager getInstance(Context context)
    {
        if (_instance == null)
        {
            synchronized (ToastManager.class)
            {
                if(_instance == null)
                {
                    _instance	=	new ToastManager(context);
                }
            }
        }
        return _instance;
    }
}
