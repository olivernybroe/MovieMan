package dk.lost_world.movieman;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnSuccessListener;

import dk.lost_world.movieman.MovieMan.Movie;

public class GameWonFragment extends Fragment implements View.OnClickListener {

    TextView currentScore;
    TextView highScore;
    ImageButton homeButton;
    Movie movie;
    long score;
    Listener listener;
    CommonConfetti commonConfetti;


    interface Listener {
        void onMenuRequested();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_won, container, false);

        currentScore = view.findViewById(R.id.current_score_view);
        currentScore.setText(Long.toString(score));

        highScore = view.findViewById(R.id.best_score_view);
        Games.getLeaderboardsClient(this.getActivity(), GoogleSignIn.getLastSignedInAccount(this.getActivity()))
            .loadCurrentPlayerLeaderboardScore(
                getString(R.string.highscore_easy_id),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC
        ).addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
            @Override
            public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                highScore.setText(Long.toString(leaderboardScoreAnnotatedData.get().getRawScore()));
            }
        });

        homeButton = view.findViewById(R.id.menuButton);
        homeButton.setOnClickListener(this);


        commonConfetti = CommonConfetti.rainingConfetti(
            container,
            new int[] { Color.YELLOW, Color.RED, Color.GREEN }
        );

        commonConfetti.getConfettiManager()
            .enableFadeOut(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    return 0.8f;
                }
            })
            .animate();

        commonConfetti.infinite();

        return view;
    }

    public GameWonFragment setMovie(Movie movie) {
        this.movie = movie;
        return this;
    }

    public GameWonFragment setScore(long score) {
        this.score = score;
        return this;
    }

    public GameWonFragment setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        MainActivity.movieMan.reset();
        commonConfetti.getConfettiManager().terminate();
        this.listener.onMenuRequested();
    }
}
