package dk.lost_world.movieman;

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.ArrayList;

import dk.lost_world.movieman.MovieMan.Movie;
import dk.lost_world.movieman.MovieMan.MovieMan;
import dk.lost_world.movieman.MovieMan.MoviePromise;
import dk.lost_world.movieman.MovieMan.MoviePromise.GuessCorrectCallback;
import dk.lost_world.movieman.MovieMan.MoviePromise.GuessWrongCallback;

import static dk.lost_world.movieman.MainActivity.TAG;


public class GameFragment extends Fragment implements GuessWrongCallback, GuessCorrectCallback,  AdapterView.OnItemClickListener, MoviePromise.FinishedCallback {

    private ImageView posterView;
    private GridView gridView;
    private TextView guessView;
    private MovieMan movieMan;
    private Chronometer chronometer;
    private ImageView life1;
    private ImageView life2;
    private ImageView life3;
    private ProgressBar progressBar;

    private Listener listener;

    public GameFragment setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    interface Listener {
        void onGameWon(Movie movie, long timeInSec, ArrayList<Character> guesses);
        void onGameLost(Movie movie, long timeInSec, ArrayList<Character> guesses);
        void onGameLoadFailed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        posterView = view.findViewById(R.id.posterView);
        gridView = view.findViewById(R.id.gridView);
        guessView = view.findViewById(R.id.textGuessView);
        chronometer = view.findViewById(R.id.chronometer2);
        life1 = view.findViewById(R.id.life_1);
        life2 = view.findViewById(R.id.life_2);
        life3 = view.findViewById(R.id.life_3);
        progressBar = view.findViewById(R.id.progress);
        movieMan = MainActivity.movieMan;


        gridView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        movieMan.start()
            .started(new DoneCallback<Movie>() {
                @Override
                public void onDone(Movie result) {
                    GameFragment.this.showMovie(result);
                }
            })
            .failed(new FailCallback<Exception>() {
                @Override
                public void onFail(Exception result) {
                    Log.e(TAG, "fetch movie failed", result);
                    GameFragment.this.listener.onGameLoadFailed();
                    //TODO: add support for exist if movie fetching fails.
                }
            })
            .guessCorrect(this)
            .guessWrong(this)
            .finished(this);
    }

    public void showMovie(final Movie movie) {
        Picasso.with(this.getActivity())
            .load(movie.getBackgroundUrl())
            .fit()
            .centerCrop()
            .into(posterView, new Callback() {
                @Override
                public void onSuccess() {
                    if(getActivity() != null) {
                        progressBar.setVisibility(View.INVISIBLE);
                        gridView.setAdapter(new LetterAdapter(GameFragment.this.getActivity()));
                        guessView.setText(movie.getHiddenTitle());

                        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                            @Override
                            public void onChronometerTick(Chronometer chronometer) {
                                long t = SystemClock.elapsedRealtime() - chronometer.getBase();
                                chronometer.setText(DateFormat.format("mm:ss", t));
                            }
                        });
                        movieMan.resetTimer();
                        chronometer.start();
                    }
                }

                @Override
                public void onError() {
                    GameFragment.this.listener.onGameLoadFailed();
                }
            });
    }

    @Override
    public void onGuessWrong(Movie movie, Character guessedChar) {
        Log.d(TAG, "guess wrong");
        switch (movie.getWrongTries()) {
            case 1:
                life3.setImageDrawable(getResources().getDrawable(R.drawable.lost_life_icon));
                break;
            case 2:
                life2.setImageDrawable(getResources().getDrawable(R.drawable.lost_life_icon));
                break;
            case 3:
                life1.setImageDrawable(getResources().getDrawable(R.drawable.lost_life_icon));
                break;
        }
    }

    @Override
    public void onGuessCorrect(Movie movie, Character guessedChar) {
        Log.d(TAG, "guess correct");
        this.guessView.setText(movie.getHiddenTitle());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        view.setEnabled(false);
        view.setAlpha(0.5f);


        this.movieMan.guess(((TextView) view).getText().charAt(0));
    }

    @Override
    public void onFinished(Movie movie, boolean won, long timeInSec, ArrayList<Character> guesses) {
        chronometer.stop();
        if(won) {
            this.listener.onGameWon(movie, timeInSec, guesses);
            movieMan.reset();
        }
        else {
            this.listener.onGameLost(movie, timeInSec, guesses);
            movieMan.reset();
        }
    }
}
