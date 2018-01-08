package dk.lost_world.movieman;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import dk.lost_world.movieman.MovieMan.Movie;

import static dk.lost_world.movieman.MainActivity.TAG;

public class MultiplayerFragment extends Fragment implements AdapterView.OnItemClickListener{

    ListView recyclerView;
    MoviesAdapter moviesAdapter;

    private Listener listener;

    public MultiplayerFragment setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    interface Listener {
        void onStartMultiplayerGameRequested(Movie movie);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiplayer, container, false);

        moviesAdapter = new MoviesAdapter(this.getActivity());
        recyclerView = view.findViewById(R.id.movieView);
        recyclerView.setAdapter(moviesAdapter);
        recyclerView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Movie selected.");
        this.listener.onStartMultiplayerGameRequested(((Movie) parent.getItemAtPosition(position)).withMultiplayer(true));
    }
}
