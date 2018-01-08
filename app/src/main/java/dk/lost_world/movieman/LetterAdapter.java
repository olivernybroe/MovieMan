package dk.lost_world.movieman;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class LetterAdapter extends ArrayAdapter<String> {
    private static final List<String> letters = Arrays.asList(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "v", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9"
    );

    public LetterAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);
    }

    public LetterAdapter(@NonNull Context context) {
        super(
            context,
            0,
            letters
        );
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        String character = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.letter_item, parent, false);
        }

        TextView button = convertView.findViewById(R.id.press_character);
        button.setText(character);

        return convertView;
    }
}
