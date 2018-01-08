package dk.lost_world.movieman;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import static dk.lost_world.movieman.MainActivity.TAG;


public class MenuFragment extends Fragment implements View.OnClickListener {
    protected ImageButton startButton;
    protected ImageButton achievementButton;
    protected ImageButton highscoreButton;
    protected ImageButton settingButton;
    protected ImageButton multiplayerButton;

    private Listener listener;

    public MenuFragment setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    interface Listener {
        void onStartGameRequested();
        void onHighscoreRequested();
        void onAchievementsRequested();
        void onSettingsRequested();
        void onMultiplayerRequested();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        startButton = view.findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        achievementButton = view.findViewById(R.id.achievementButton);
        achievementButton.setOnClickListener(this);

        highscoreButton = view.findViewById(R.id.highscoreButton);
        highscoreButton.setOnClickListener(this);

        settingButton = view.findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(this);

        multiplayerButton = view.findViewById(R.id.multiplayerButton);
        multiplayerButton.setOnClickListener(this);

        updateUi(null);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v == startButton) {
            Log.d(TAG, "Start clicked");
            listener.onStartGameRequested();
        } else if(v == highscoreButton) {
            Log.d(TAG, "Highscore clicked");
            listener.onHighscoreRequested();
        } else if(v == achievementButton) {
            Log.d(TAG, "Achievement clicked");
            listener.onAchievementsRequested();
        } else if(v == settingButton) {
            Log.d(TAG, "Settings clicked");
            listener.onSettingsRequested();
        } else if(v == multiplayerButton) {
            Log.d(TAG, "Multiplayer clicked");
            listener.onMultiplayerRequested();
        }
    }

    public void updateUi(@Nullable GoogleSignInAccount account) {
        boolean exist = account != null;

        startButton.setAlpha(exist ? 1 : 0.5f);
        startButton.setEnabled(exist);
        achievementButton.setEnabled(exist);
        achievementButton.setAlpha(exist ? 1 : 0.5f);
        highscoreButton.setEnabled(exist);
        highscoreButton.setAlpha(exist ? 1 : 0.5f);
        settingButton.setEnabled(true);
        multiplayerButton.setEnabled(exist);
        multiplayerButton.setAlpha(exist ? 1 : 0.5f);
    }
}
