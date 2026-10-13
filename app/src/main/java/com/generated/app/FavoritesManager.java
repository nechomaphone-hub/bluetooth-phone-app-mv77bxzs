package com.generated.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesManager {
    private static final String PREF_NAME = "bluetooth_phone_favorites";
    private static final String KEY_FAVS = "favorite_numbers";
    private final SharedPreferences prefs;

    public FavoritesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<String> getFavorites() {
        Set<String> set = prefs.getStringSet(KEY_FAVS, new HashSet<String>());
        return new ArrayList<>(set);
    }

    public void addFavorite(String number) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_FAVS, new HashSet<String>()));
        set.add(number);
        prefs.edit().putStringSet(KEY_FAVS, set).apply();
    }

    public void removeFavorite(String number) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_FAVS, new HashSet<String>()));
        set.remove(number);
        prefs.edit().putStringSet(KEY_FAVS, set).apply();
    }

    public boolean isFavorite(String number) {
        Set<String> set = prefs.getStringSet(KEY_FAVS, new HashSet<String>());
        return set.contains(number);
    }
}
