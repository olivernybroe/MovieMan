package dk.lost_world.movieman.MovieMan;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.lost_world.movieman.MainActivity.TAG;

public class Movie {
    private boolean withMultiplayer = false;
    private String title;
    private String backgroundUrl;
    private List<Integer> genres;
    private String posterUrl;
    private int year;
    private int tries = 0;
    private int wrongTries = 0;
    private ArrayList<Character> guesses = new ArrayList<>();
    private static final SparseArray<String> genreMap = new SparseArray<>();
    static {
        genreMap.put(28, "Action");
        genreMap.put(12, "Adventure");
        genreMap.put(16, "Animation");
        genreMap.put(35, "Comedy");
        genreMap.put(80, "Crime");
        genreMap.put(99, "Documentary");
        genreMap.put(18, "Drama");
        genreMap.put(10751, "Family");
        genreMap.put(14, "Fantasy");
        genreMap.put(36, "History");
        genreMap.put(26, "Horror");
        genreMap.put(10402, "Music");
        genreMap.put(9648, "Mystery");
        genreMap.put(10749, "Romance");
        genreMap.put(878, "SciFi");
        //genreMap.put(10770, "TV Movie");
        genreMap.put(53, "Thriller");
        genreMap.put(10752, "War");
        genreMap.put(37, "Western");
    }

    public Movie(String title, List<Integer> genres, int year, String posterUrl, String backgroundUrl) {
        this.title = title;
        this.genres = genres;
        this.year = year;
        this.posterUrl = posterUrl;
        this.backgroundUrl = backgroundUrl;
    }

    public Movie(JSONObject jsonMovie) throws JSONException {
        this(
            jsonMovie.getString("title"),
            toGenres(jsonMovie.getJSONArray("genre_ids")),
            Integer.valueOf(jsonMovie.getString("release_date").substring(0, 4)),
            jsonMovie.getString("poster_path"),
            jsonMovie.getString("backdrop_path")
        );
    }

    private static List<Integer> toGenres(JSONArray jsonArray) {
        List<Integer> genres = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                genres.add(jsonArray.getInt(i));
            } catch (JSONException ignored) {}
        }

        return genres;
    }

    public String getHiddenTitle() {
        String guessedChars = TextUtils.join("", guesses)+TextUtils.join("", lowercase(guesses));
        return this.title
            .replaceAll("[^"+guessedChars+" \\W\\d]", "_");
    }


    public String getTitle() {
        return title;
    }

    public boolean guess(char guessedChar) throws IllegalArgumentException {
        guessedChar = Character.toUpperCase(guessedChar);
        if(this.guesses.contains(guessedChar)) {
            throw new IllegalArgumentException("["+guessedChar+"] has already been guessed on.");
        }
        this.guesses.add(guessedChar);
        this.tries++;

        if(this.title.toUpperCase().indexOf(guessedChar) >= 0) {
            return true;
        }
        this.wrongTries++;
        return false;
    }

    private List<Character> lowercase(List<Character> chars) {
        List<Character> newChars = new ArrayList<>();

        for (Character character: chars) {
            newChars.add(Character.toLowerCase(character));
        }
        return newChars;
    }

    public String getGenres() {
        StringBuilder genreString = new StringBuilder();
        String prefix = "";
        for (Integer genreId : this.genres) {
            if(Movie.genreMap.get(genreId) != null) {
                genreString.append(prefix);
                prefix = ", ";
                genreString.append(Movie.genreMap.get(genreId));
            }
        }

        return genreString.toString();
    }

    public int getYear() {
        return year;
    }

    public String getPosterUrl() {
        return "https://image.tmdb.org/t/p/original/"+posterUrl;
    }

    public String getPosterUrlThumbnail() {
        return "https://image.tmdb.org/t/p/w300/"+posterUrl;
    }

    public String getBackgroundUrl() {
        return "https://image.tmdb.org/t/p/original/"+backgroundUrl;
    }

    public int getTries() {
        return tries;
    }

    public ArrayList<Character> getGuesses() {
        return guesses;
    }

    public boolean hasGuessedAllLetters() {
        ArrayList<Character> chars = new ArrayList<>();
        for (char c : this.title.replaceAll("[^\\w]", "").toCharArray()) {
            chars.add(Character.toUpperCase(c));
        }
        return this.guesses.containsAll(chars);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public int getWrongTries() {
        return wrongTries;
    }

    public boolean isWithMultiplayer() {
        return this.withMultiplayer;
    }

    public Movie withMultiplayer(boolean withMultiplayer) {
        this.withMultiplayer = withMultiplayer;
        return this;
    }
}
