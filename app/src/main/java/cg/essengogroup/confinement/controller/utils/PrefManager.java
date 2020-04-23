package cg.essengogroup.confinement.controller.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private Context context;

    public PrefManager(Context context) {
        this.context = context;
    }

    public void savePseudo(String pseudo) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("confinement", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pseudo", pseudo);
        editor.commit();
    }

    public String getPseudo() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("confinement", Context.MODE_PRIVATE);
        return sharedPreferences.getString("pseudo", "");
    }

    public boolean isUserLogedOut() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("confinement", Context.MODE_PRIVATE);
        return sharedPreferences.getString("pseudo", "").isEmpty();
    }


    public void setDarkMode(boolean statement) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("confinement", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("darkMode", statement);
        editor.commit();
    }

    public boolean getDarkMode() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("confinement", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("darkMode", false);
    }
}
