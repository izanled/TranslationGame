package com.izanled.translation.game.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.izanled.translation.game.R;
import com.izanled.translation.game.common.CommonData;
import com.izanled.translation.game.data.TranslationData;
import com.izanled.translation.game.sqlite.SqlManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class NaverNMTTranslatorTask extends AsyncTask<String, Void, String> {

    private final static String URL = "https://openapi.naver.com/v1/papago/n2mt";
    private final static String TARGET = "&target=";
    private final static String SOURCE = "source=";
    private final static String TEXT = "&text=";

    Context mContext;
    EventListener eventListener;

    public NaverNMTTranslatorTask(Context context, EventListener eventListener) {
        mContext = context;
        this.eventListener = eventListener;
    }

    @Override
    protected String doInBackground(String... strings) {
        StringBuffer result = new StringBuffer();
        try{
            java.net.URL url = new URL(URL);
            String params = null;
            String encodedText = URLEncoder.encode(strings[0],"UTF-8");

            TranslationData translationData = null;

            if(strings[1].equals(CommonData.getInstance().getmTransferTargetImg())){
                params = SOURCE + CommonData.getInstance().getmSourceLang() + TARGET + CommonData.getInstance().getmTargetLang() + TEXT + encodedText;
                translationData = SqlManager.shared().getTranslationData(strings[0], CommonData.getInstance().getmSourceLang(), CommonData.getInstance().getmTargetLang());
            }else{
                params = SOURCE + CommonData.getInstance().getmTargetLang() + TARGET + CommonData.getInstance().getmSourceLang() + TEXT + encodedText;
                translationData = SqlManager.shared().getTranslationData(strings[0], CommonData.getInstance().getmTargetLang(), CommonData.getInstance().getmSourceLang());
            }

            if(translationData != null){
                translationData.setCount(translationData.getCount()+1);
                SqlManager.shared().updateTranslation(translationData);
                return translationData.getTranslation();
            }else{
                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-Naver-Client-Id", CommonData.getInstance().getClientId());
                conn.setRequestProperty("X-Naver-Client-Secret", CommonData.getInstance().getSecretId());
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();

                InputStream stream = null;
                if(conn.getResponseCode() == 200){
                    stream = conn.getInputStream();
                }else{
                    stream = conn.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;

                while ((line = reader.readLine()) != null){
                    result.append(line);
                }

                JSONObject jsonObject = new JSONObject(result.toString());
                if(jsonObject != null){
                    String output = jsonObject.getJSONObject("message").getJSONObject("result").getString("translatedText");

                    if(output != null && output.length() > 0){
                        TranslationData newData = null;
                        if(strings[1].equals(CommonData.getInstance().getmTransferTargetImg()))
                            newData = new TranslationData(0, strings[0], output, 1, CommonData.getInstance().getmSourceLang(), CommonData.getInstance().getmTargetLang());
                        else
                            newData = new TranslationData(0, strings[0], output, 1, CommonData.getInstance().getmTargetLang(), CommonData.getInstance().getmSourceLang());
                        SqlManager.shared().insertTranslation(newData);
                        return output;
                    }else{
                        return mContext.getString(R.string.fail_translation);
                    }
                }else{
                    return mContext.getString(R.string.fail_translation);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            return mContext.getString(R.string.fail_translation_papago);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        eventListener.returnResult(s);
    }

    public interface EventListener{
        void returnResult(String result);
    }
}
