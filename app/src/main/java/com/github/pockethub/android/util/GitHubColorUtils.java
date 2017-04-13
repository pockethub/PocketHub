package com.github.pockethub.android.util;

import android.content.Context;

import com.github.pockethub.android.PocketHub;
import com.github.pockethub.android.R;
import com.github.pockethub.android.model.GitHubLanguage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Helper Class to Get The Color Code From the githubcolor.json file
 */

public class GitHubColorUtils {

    private static GitHubColorUtils gitHubColorUtils = null;
    private Map<String,GitHubLanguage> languageMap;
    private GitHubColorUtils(Context context){
        Gson gson = new Gson();
        languageMap = gson.fromJson(readTextFile(context.getResources().openRawResource(R.raw.githubcolor)),new TypeToken<Map<String,GitHubLanguage>>(){}.getType());

    }

    public static GitHubColorUtils getInstance(){
        if(gitHubColorUtils==null){
            gitHubColorUtils = new GitHubColorUtils(PocketHub.getAppContext());
            return gitHubColorUtils;
        }
        return gitHubColorUtils;
    }


    public  String githubColorCode(String languageName){
        return languageMap.get(languageName).getColor();
    }

    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }
}
