package dk.lost_world.movieman.MovieMan;

import android.util.Log;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dk.lost_world.movieman.MainActivity.TAG;

public class DeferredMovie extends AndroidDeferredObject<Movie, Exception, MovieMan> implements MoviePromise, Promise<Movie, Exception, MovieMan>{
    protected final List<GuessCorrectCallback> guessCorrectCallbacks = new CopyOnWriteArrayList<>();
    protected final List<GuessWrongCallback> guessWrongCallbacks = new CopyOnWriteArrayList<>();
    protected final List<FinishedCallback> finishedCallbacks = new CopyOnWriteArrayList<>();

    protected volatile boolean isStarted = false;


    public DeferredMovie start(final Movie movie) {
        synchronized (this) {
            if (!isPending())
                throw new IllegalStateException("Game already finished, cannot resolve again");

            this.isStarted = true;
            this.state = State.PENDING;
            this.resolveResult = movie;

            try {
                triggerDone(movie);
            } finally {
                triggerAlways(state, movie, null);
            }
        }
        return this;
    }

    public DeferredMovie guessCorrect(final Movie movie, final Character guessedChar) {
        synchronized (this) {
            if (!isPending())
                throw new IllegalStateException("Game already finished, cannot resolve again");

            this.state = State.PENDING;
            this.resolveResult = movie;

            try {
                triggerGuessCorrect(movie, guessedChar);
            } finally {
                triggerAlways(state, movie, null);
            }
        }
        return this;
    }

    public DeferredMovie guessWrong(Movie movie, char guessedChar) {
        synchronized (this) {
            if (!isPending())
                throw new IllegalStateException("Game already finished, cannot resolve again");

            this.state = State.PENDING;
            this.resolveResult = movie;

            try {
                triggerGuessWrong(movie, guessedChar);
            } finally {
                triggerAlways(state, movie, null);
            }
        }
        return this;
    }

    public DeferredMovie finished(Movie movie, boolean won, long timeInSec, ArrayList<Character> guesses) {
        synchronized (this) {
            if (!isPending())
                throw new IllegalStateException("Game already finished, cannot resolve again");

            this.state = State.RESOLVED;
            this.resolveResult = movie;

            try {
                triggerFinished(movie, won, timeInSec, guesses);
            } finally {
                triggerAlways(state, movie, null);
            }
        }
        return this;
    }

    @Override
    public MoviePromise started(DoneCallback<Movie> callback) {
        synchronized (this) {
            if (this.isStarted){
                triggerDone(callback, resolveResult);
            }else{
                doneCallbacks.add(callback);
            }
        }
        return this;
    }

    @Override
    public MoviePromise guessCorrect(GuessCorrectCallback callback) {
        guessCorrectCallbacks.add(callback);
        return this;
    }

    @Override
    public MoviePromise guessWrong(GuessWrongCallback callback) {
        guessWrongCallbacks.add(callback);
        return this;
    }

    @Override
    public MoviePromise finished(FinishedCallback callback) {
        finishedCallbacks.add(callback);
        return this;
    }
    @Override
    public MoviePromise failed(FailCallback<Exception> callback) {
        this.fail(callback);
        return this;
    }

    public MoviePromise moviePromise() {
        return this;
    }

    protected void triggerFinished(Movie movie, boolean won, long timeInSec, ArrayList<Character> guesses) {
        for (FinishedCallback callback : finishedCallbacks) {
            try {
                triggerFinished(callback, movie, won, timeInSec, guesses);
            } catch (Exception e) {
                log.error("an uncaught exception occurred in a FinishedCallback", e);
            }
        }
        finishedCallbacks.clear();
    }

    protected void triggerFinished(FinishedCallback callback, Movie movie, boolean won, long timeInSec, ArrayList<Character> guesses) {
        callback.onFinished(movie, won, timeInSec, guesses);
    }

    protected void triggerGuessWrong(Movie movie, Character guessedChar) {
        for (GuessWrongCallback callback : guessWrongCallbacks) {
            try {
                triggerGuessWrong(callback, movie, guessedChar);
            } catch (Exception e) {
                log.error("an uncaught exception occurred in a GuessWrongCallback", e);
            }
        }
    }

    protected void triggerGuessWrong(GuessWrongCallback callback, Movie movie, Character guessedChar) {
        callback.onGuessWrong(movie, guessedChar);
    }

    protected void triggerGuessCorrect(Movie movie, Character guessedChar) {
        for (GuessCorrectCallback callback : guessCorrectCallbacks) {
            try {
                triggerGuessCorrect(callback, movie, guessedChar);
            } catch (Exception e) {
                log.error("an uncaught exception occurred in a GuessCorrectCallback", e);
            }
        }
    }

    protected void triggerGuessCorrect(GuessCorrectCallback callback, Movie movie, Character guessedChar) {
        callback.onGuessCorrect(movie, guessedChar);
    }
}
