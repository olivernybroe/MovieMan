package dk.lost_world.movieman;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import dk.lost_world.movieman.MovieMan.Movie;

public class GameLostFragment extends Fragment implements View.OnClickListener {

    Movie movie;
    GameWonFragment.Listener listener;
    ImageButton homeButton;
    ImageView movieView;
    TextView titleView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_lost, container, false);

        homeButton = view.findViewById(R.id.menuButton);
        homeButton.setOnClickListener(this);

        movieView = view.findViewById(R.id.imageView);
        Picasso.with(this.getActivity())
            .load(movie.getPosterUrl())
            .into(movieView);

        titleView = view.findViewById(R.id.titleView);
        titleView.setText(movie.getTitle());

        MediaPlayer mediaPlayer = MediaPlayer.create(this.getActivity(), R.raw.game_lost_sound);
        mediaPlayer.start();

        return view;
    }

    public GameLostFragment setMovie(Movie movie) {
        this.movie = movie;
        return this;
    }

    public GameLostFragment setListener(GameWonFragment.Listener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        MainActivity.movieMan.reset();
        this.listener.onMenuRequested();
    }
}
