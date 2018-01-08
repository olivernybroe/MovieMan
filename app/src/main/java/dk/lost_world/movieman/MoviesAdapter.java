package dk.lost_world.movieman;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jdeferred.DeferredRunnable;
import org.jdeferred.ProgressCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import dk.lost_world.movieman.MovieMan.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MoviesAdapter extends ArrayAdapter<Movie> {
    private final OkHttpClient client = new OkHttpClient();
    private final AndroidDeferredManager manager = new AndroidDeferredManager();


    public MoviesAdapter(@NonNull Context context) {
        super(context, 0);

        fetchMovies();
    }

    public void fetchMovies() {
        int pageNum = (new Random()).nextInt(100)+1;

        final Request request = new Request.Builder()
            .url("https://api.themoviedb.org/3/discover/movie?api_key=23dc4229760c4f90f153b02167484554&language=en-US&sort_by=revenue.desc&include_adult=false&include_video=false&page="+pageNum+"&with_original_language=en")
            .build();

        manager.when(new DeferredRunnable<Movie>() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray results = json.getJSONArray("results");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject jsonMovie = results.getJSONObject(i);
                        notify(new Movie(jsonMovie));
                    }
                } catch (Exception ignored) {
                }
            }
        }).progress(new ProgressCallback<Movie>() {
            @Override
            public void onProgress(Movie progress) {
                MoviesAdapter.this.add(progress);
            }
        });
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Movie movie = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_item, parent, false);
        }

        TextView genre = convertView.findViewById(R.id.genres);
        TextView title = convertView.findViewById(R.id.title);
        TextView year = convertView.findViewById(R.id.year);
        ImageView poster = convertView.findViewById(R.id.movieCoverView);

        genre.setText(movie.getGenres());
        title.setText(movie.getTitle());
        year.setText(Integer.toString(movie.getYear()));
        Picasso.with(convertView.getContext())
            .load(movie.getPosterUrlThumbnail())
            .fit()
            .into(poster);


        return convertView;
    }
}
