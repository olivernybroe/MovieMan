package dk.lost_world.movieman.MovieMan;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dk.lost_world.movieman.MainActivity.TAG;

public class MovieMan {
    private DeferredMovie deferred;
    private Movie movie;
    private final OkHttpClient client = new OkHttpClient();
    private long startTime;
    private boolean hardMode = false;

    public MovieMan() {
    }

    public MovieMan reset() {
        this.movie = null;
        this.hardMode = false;
        return this;
    }

    public Request getRequest() {
        int min = this.hardMode ? 20 : 1;
        int max = this.hardMode ? min+40 : min+20;
        return this.getRequest(new Random().nextInt(max - min + 1) + min);
    }

    public Request getRequest(int page) {
        return new Request.Builder()
            .url(new HttpUrl.Builder()
                .scheme("https")
                .host("api.themoviedb.org")
                .addPathSegments("3/discover/movie")
                .addQueryParameter("api_key", "23dc4229760c4f90f153b02167484554")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("sort_by", "revenue.desc")
                .addQueryParameter("include_adult", "false")
                .addQueryParameter("include_video", "false")
                .addQueryParameter("page", Integer.toString(page))
                .addQueryParameter("with_original_language", "en")
                .build()
            ).build();

    }

    public MoviePromise start() {
        deferred = new DeferredMovie();

        if(this.movie == null) {

            final Request request = this.getRequest();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    deferred.reject(e);
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try{
                        JSONObject json = new JSONObject(response.body().string());

                        int max = json.getInt("total_results")-1;

                        int movieNum = (new Random()).nextInt(max >= json.getJSONArray("results").length() ? json.getJSONArray("results").length() : max);
                        final JSONObject movie = json.getJSONArray("results").getJSONObject(movieNum);

                        MovieMan.this.movie = new Movie(movie);
                        deferred.start(MovieMan.this.movie);
                        startTime = SystemClock.elapsedRealtime();
                    }
                    catch (JSONException e) {
                        deferred.reject(e);
                    }
                }
            });
        }
        else {
            deferred.start(this.movie);
            startTime = SystemClock.elapsedRealtime();
        }

        return deferred.moviePromise();
    }


    public MovieMan guess(char guessedChar) {
        try{
            if(this.movie.guess(guessedChar)) {
                this.deferred.guessCorrect(this.movie, guessedChar);
            }
            else {
                this.deferred.guessWrong(this.movie, guessedChar);
            }
        }
        catch (IllegalArgumentException ignored) {}

        if(this.movie.getWrongTries() >= 3) {
            this.deferred.finished(this.movie, false, SystemClock.elapsedRealtime() - startTime,  this.movie.getGuesses()); //TODO: add time
        }
        else if(this.movie.hasGuessedAllLetters()) {
            this.deferred.finished(this.movie, true, SystemClock.elapsedRealtime() - startTime, this.movie.getGuesses()); // TODO: add time
        }

        return this;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setHardMode(boolean hardMode) {
        this.hardMode = hardMode;
    }
}
