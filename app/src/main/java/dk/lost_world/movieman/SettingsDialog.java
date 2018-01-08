package dk.lost_world.movieman;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class SettingsDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    Button closeButton;
    Button logoutButton;
    Button loginButton;
    SharedPreferences preferences;
    Listener listener;
    Switch hardModeSwitch;

    interface Listener {
        void onLoginRequested();
        void onLogoutRequested();

    }

    public SettingsDialog setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public static SettingsDialog newInstance(Listener listener) {
        SettingsDialog frag = new SettingsDialog();
        frag.setListener(listener);
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_settings, container, false);
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        closeButton = view.findViewById(R.id.closeDialog);
        closeButton.setOnClickListener(this);

        logoutButton = view.findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);

        loginButton = view.findViewById(R.id.login);
        loginButton.setOnClickListener(this);

        hardModeSwitch = view.findViewById(R.id.difficulty_switch);
        hardModeSwitch.setOnCheckedChangeListener(this);
        hardModeSwitch.setChecked(preferences.getBoolean(getString(R.string.useHardMode), false));

        if(GoogleSignIn.getLastSignedInAccount(this.getActivity()) == null) {
            loginButton.setVisibility(View.VISIBLE);
        }
        else {
            logoutButton.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view == closeButton) {
            this.dismiss();
        }
        else if(view == logoutButton) {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this.getActivity(),
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            signInClient.signOut()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SettingsDialog.this.listener.onLogoutRequested();
                        SettingsDialog.this.logoutButton.setVisibility(View.INVISIBLE);
                        SettingsDialog.this.loginButton.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          new AlertDialog.Builder(SettingsDialog.this.getActivity()).setMessage("Failed logging out.")
                              .setNeutralButton(android.R.string.ok, null).show();
                      }
                  });
        }
        else if(view == loginButton) {
            this.listener.onLoginRequested();
            this.dismiss();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        preferences.edit().putBoolean(getString(R.string.useHardMode), isChecked).commit();
    }
}