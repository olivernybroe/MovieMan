package dk.lost_world.movieman;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import dk.lost_world.movieman.MovieMan.Movie;
import dk.lost_world.movieman.MovieMan.MovieMan;

import static com.google.android.gms.common.api.CommonStatusCodes.SIGN_IN_REQUIRED;

public class MainActivity extends Activity implements MenuFragment.Listener, MultiplayerFragment.Listener, GameFragment.Listener, SettingsDialog.Listener, GameWonFragment.Listener {

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_ACHIEVEMENT_UI = 9003;

    public static final String TAG = "MovieMan";
    GoogleSignInClient signInClient;
    public static final MovieMan movieMan = new MovieMan();

    private MenuFragment menuFragment;
    private GameFragment gameFragment;
    private MultiplayerFragment multiplayerFragment;
    private GameWonFragment gameWonFragment;
    private GameLostFragment gameLostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        menuFragment = new MenuFragment().setListener(this);
        gameFragment = new GameFragment().setListener(this);
        multiplayerFragment = new MultiplayerFragment().setListener(this);
        gameWonFragment = new GameWonFragment().setListener(this);
        gameLostFragment = new GameLostFragment().setListener(this);


        signInClient = GoogleSignIn.getClient(this,
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .build()
        );

        getFragmentManager().beginTransaction().add(R.id.fragment_container, menuFragment).commit();
    }

    private void signInSilently() {
        signInClient.silentSignIn()
            .addOnSuccessListener(this, new OnSuccessListener<GoogleSignInAccount>() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    Log.d(TAG, "signInSilently(): success");
                    menuFragment.updateUi(account);
                    Games.getGamesClient(MainActivity.this, account)
                        .setViewForPopups(getWindow().getDecorView().findViewById(android.R.id.content));
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "signInSilently(): failure", e);
                    menuFragment.updateUi(null);
                    signIn();
                }
            });
    }

    private void switchToFragment(Fragment newFragment) {
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment, "CURRENT_FRAGMENT")
            .addToBackStack(null)
            .commit();
    }

    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //signInSilently();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() >= 1) {
            if(getFragmentManager().findFragmentByTag("CURRENT_FRAGMENT") instanceof GameFragment) {
                if(movieMan.getMovie() != null) {
                    incrementGameAbandoned(movieMan.getMovie());
                }
                movieMan.reset();
            }

            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            menuFragment.setListener(this);
            signInSilently();
        }
        else {
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, result.getStatus().toString());
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                menuFragment.updateUi(signedInAccount);
            } else {
                Log.e(TAG, "error code is:" + String.valueOf(result.getStatus().getStatusCode()));
                String message = result.getStatus().getStatusMessage();

                if (message == null || message.isEmpty()) {
                    message = "Failed to sign in to Play Games. Maybe try updating it Play Games through Store?";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        signInSilently();
    }

    @Override
    public void onStartGameRequested() {
        boolean hardMode = this.getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.useHardMode), false);

        if(hardMode) {
            movieMan.setHardMode(true);
        }

        switchToFragment(gameFragment.setListener(this));
    }

    @Override
    public void onHighscoreRequested() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .getLeaderboardIntent(getString(R.string.highscore_easy_id))
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_LEADERBOARD_UI);
                }
            });
    }

    @Override
    public void onAchievementsRequested() {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .getAchievementsIntent()
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                }
            });
    }

    @Override
    public void onSettingsRequested() {
        SettingsDialog dialog = SettingsDialog.newInstance(this);
        dialog.show(getFragmentManager(), "settingDialog");
    }

    @Override
    public void onMultiplayerRequested() {
        switchToFragment(multiplayerFragment);
    }

    @Override
    public void onStartMultiplayerGameRequested(Movie movie) {
        movieMan.setMovie(movie);
        switchToFragment(gameFragment.setListener(this));
    }

    @Override
    public void onGameWon(Movie movie, long timeInSec, ArrayList<Character> guesses) {
        Log.e(TAG, "GAME WON");
        long score = timeInSec*(movie.getWrongTries()+1);

        switchToFragment(gameWonFragment.setMovie(movie).setScore(score));
        incrementGameWon(movie);
        if(!movie.isWithMultiplayer()) {
            submitScore(score);
        }
    }

    @Override
    public void onGameLost(Movie movie, long timeInSec, ArrayList<Character> guesses) {
        Log.e(TAG, "GAME LOST");
        switchToFragment(gameLostFragment.setMovie(movie));
        incrementGameLost(movie);
    }

    public void incrementGamePlayed(EventsClient eventsClient, Movie movie) {
        if(movie.isWithMultiplayer()) {
            eventsClient.increment(getString(R.string.event_multiplayer_game_played_id), 1);
        }
        else {
            eventsClient.increment(getString(R.string.event_game_played_id), 1);
        }
    }

    public void incrementGameLost(Movie movie) {
        EventsClient eventsClient = Games.getEventsClient(
            this,
            GoogleSignIn.getLastSignedInAccount(this)
        );
        incrementGamePlayed(eventsClient, movie);
    }

    public void incrementGameAbandoned(Movie movie) {
        EventsClient eventsClient = Games.getEventsClient(this, GoogleSignIn.getLastSignedInAccount(this));

        if(movie.isWithMultiplayer()) {
            eventsClient.increment(getString(R.string.event_multiplayer_game_abandoned_id), 1);
        }
        else {
            eventsClient.increment(getString(R.string.event_game_abandoned_id), 1);
        }
    }

    public void incrementGameWon(Movie movie) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        EventsClient eventsClient = Games.getEventsClient(this, account);

        incrementGamePlayed(eventsClient, movie);

        if(movie.isWithMultiplayer()) {
            Log.d(TAG, "WON MULTIPLAYER GAME");
            eventsClient.increment(getString(R.string.event_multiplayer_game_won_id), 1);
        }
        else {
            Log.d(TAG, "WON GAME");
            eventsClient.increment(getString(R.string.event_game_won_id), 1);
            AchievementsClient achievementsClient = Games.getAchievementsClient(this, account);
            achievementsClient.increment(getString(R.string.achievement_5_games), 1);
            achievementsClient.increment(getString(R.string.achievement_20_games), 1);
            achievementsClient.increment(getString(R.string.achievement_50_games), 1);
            achievementsClient.increment(getString(R.string.achievement_100_games), 1);
            achievementsClient.increment(getString(R.string.achievement_1000_games), 1);
        }
    }

    public void submitScore(long score) {
        Log.d(TAG, "score submitted: "+ score);
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .submitScore(getString(R.string.highscore_easy_id), score);
    }

    @Override
    public void onLoginRequested() {
        this.signIn();
    }

    @Override
    public void onLogoutRequested() {
        this.menuFragment.updateUi(null);
    }

    @Override
    public void onMenuRequested() {
        this.onBackPressed();
    }
}

