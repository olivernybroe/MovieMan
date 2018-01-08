package dk.lost_world.movieman.MovieMan;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.ArrayList;

public interface MoviePromise {

    MoviePromise started(DoneCallback<Movie> callback);

    MoviePromise guessCorrect(GuessCorrectCallback callback);

    interface GuessCorrectCallback {
        void onGuessCorrect(final Movie movie, final Character guessedChar);
    }

    MoviePromise guessWrong(GuessWrongCallback callback);

    interface GuessWrongCallback {
        void onGuessWrong(final Movie movie, final Character guessedChar);
    }

    MoviePromise finished(FinishedCallback callback);

    interface FinishedCallback {
        void onFinished(final Movie movie, boolean won, long timeInSec, ArrayList<Character> guesses);
    }

    MoviePromise failed(FailCallback<Exception> callback);
}
