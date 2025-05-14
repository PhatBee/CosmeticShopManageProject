package vn.phatbee.cosmesticshopapp.manager;

import android.content.Context;
import android.content.SharedPreferences;

import vn.phatbee.cosmesticshopapp.model.User;

public class UserSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_IMAGE = "image";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public UserSessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user){
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_GENDER, user.getGender());
        editor.putString(KEY_IMAGE, user.getImage());
        editor.apply();
    }

    public User getUserDetails(){
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setUserId(pref.getLong(KEY_USER_ID, 0));
        user.setUsername(pref.getString(KEY_USERNAME, ""));
        user.setEmail(pref.getString(KEY_EMAIL, ""));
        user.setGender(pref.getString(KEY_GENDER, ""));
        user.setImage(pref.getString(KEY_IMAGE, ""));
        return user;
    }

    public void logoutUser(){
        editor.clear();
        editor.apply();

    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
