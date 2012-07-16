package ru.myshows.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import ru.myshows.activity.MyShows;
import ru.myshows.activity.R;
import ru.myshows.api.MyShowsApi;
import ru.myshows.domain.Episode;
import ru.myshows.domain.Genre;
import ru.myshows.domain.Show;
import ru.myshows.domain.UserShow;
import ru.myshows.tasks.BaseTask;
import ru.myshows.tasks.ChangeShowStatusTask;
import ru.myshows.util.EpisodeComparator;
import ru.myshows.util.Settings;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gb
 * Date: 07.07.12
 * Time: 1:04
 * To change this template use File | Settings | File Templates.
 */
public class ShowFragment extends Fragment implements ChangeShowStatusTask.ChangeShowStatusListener {


    private ImageView showLogo;
    private LinearLayout dateLayout;
    private LinearLayout genresLayoyt;
    private LinearLayout watchingLayoyt;
    private LinearLayout myShowsRatingLayoyt;
    private LinearLayout yoursRatingLayoyt;
    private RatingBar myShowsRatingBar;
    private RatingBar yoursRatingBar;
    private Button watchingButton;
    private Button willWatchButton;
    private Button cancelledButton;
    private Button removeButton;
    private LinearLayout statusButtonsLayoyt;

    private Show show;
    private MyShowsApi.STATUS watchStatus;
    private Double yoursRating;

    public ShowFragment(Show show, MyShowsApi.STATUS watchStatus, Double yoursRating) {
        this.show = show;
        this.watchStatus = watchStatus;
        this.yoursRating = yoursRating;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show, container, false);
        populateUI(view);
        return view;
    }


    private void populateUI(View view) {
        showLogo = (ImageView) view.findViewById(R.id.show_logo);
        dateLayout = (LinearLayout) view.findViewById(R.id.show_date_layout);
        genresLayoyt = (LinearLayout) view.findViewById(R.id.show_genres_layout);
        watchingLayoyt = (LinearLayout) view.findViewById(R.id.show_watching_layout);
        myShowsRatingLayoyt = (LinearLayout) view.findViewById(R.id.show_rating_myshows_layout);
        yoursRatingLayoyt = (LinearLayout) view.findViewById(R.id.show_rating_yours_layout);
        statusButtonsLayoyt = (LinearLayout) view.findViewById(R.id.show_status_buttons_layout);
        watchingButton = (Button) statusButtonsLayoyt.findViewById(R.id.button_watching);
        willWatchButton = (Button) statusButtonsLayoyt.findViewById(R.id.button_will_watch);
        cancelledButton = (Button) statusButtonsLayoyt.findViewById(R.id.button_cancelled);
        removeButton = (Button) statusButtonsLayoyt.findViewById(R.id.button_remove);


        ImageLoader.getInstance().displayImage(show.getImageUrl(), showLogo);

        // date
        if (show.getStartedAt() != null) {
            dateLayout.setVisibility(View.VISIBLE);
            String date = show.getStartedAt();
            if (show.getEndedAt() != null)
                date += " - " + show.getEndedAt();
            ((TextView) dateLayout.findViewById(R.id.show_date_value)).setText(date);
        }

        // genres
        if (show.getGenres() != null) {
            genresLayoyt.setVisibility(View.VISIBLE);
            ((TextView) genresLayoyt.findViewById(R.id.show_genres_valye)).setText(show.getGenres());
        }
        //watching
        if (show.getWatching() != null) {
            watchingLayoyt.setVisibility(View.VISIBLE);
            ((TextView) watchingLayoyt.findViewById(R.id.show_watching_value)).setText(String.valueOf(show.getWatching()));
        }


        // myshows  rating
        if (show.getRating() != null) {
            myShowsRatingLayoyt.setVisibility(View.VISIBLE);
            myShowsRatingBar = ((RatingBar) myShowsRatingLayoyt.findViewById(R.id.show_rating_myshows_value));
            myShowsRatingBar.setRating((float) show.getRating().doubleValue());
        }



        if (MyShows.isLoggedIn) {

            // yours rating
            yoursRatingLayoyt.setVisibility(View.VISIBLE);
            yoursRatingBar = ((RatingBar) yoursRatingLayoyt.findViewById(R.id.show_rating_yours_value));

            // disable rating changing if remove status
            if (watchStatus.equals(MyShowsApi.STATUS.remove))
                yoursRatingBar.setIsIndicator(true);

            if (yoursRating != null) {
                yoursRatingBar.setRating((float) yoursRating.doubleValue());
            }

            yoursRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    new ChangeShowRatioTask(getActivity()).execute(rating);
                }
            });

            // show status buttons
            statusButtonsLayoyt.setVisibility(View.VISIBLE);
            updateStatusButtons();
        }

    }


    public class ChangeShowRatioTask extends BaseTask<Boolean> {

        public ChangeShowRatioTask(Context context) {
            super(context);
        }

        @Override
        public Boolean doWork(Object... objects) throws Exception {
            Float rating = (Float) objects[0];
            boolean result = MyShows.client.changeShowRatio(show.getShowId(), (int) rating.floatValue());
            return result;
        }

        @Override
        public void onResult(Boolean result) {
            Toast.makeText(getActivity(), result ? R.string.changes_saved : R.string.changes_not_saved, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatusButtons() {
        boolean isWatching = watchStatus.equals(MyShowsApi.STATUS.watching) || watchStatus.equals(MyShowsApi.STATUS.finished);
        watchingButton.setBackgroundDrawable(isWatching ? getResources().getDrawable(R.drawable.red_label) : null);

        boolean isLater = watchStatus.equals(MyShowsApi.STATUS.later);
        willWatchButton.setBackgroundDrawable(isLater ? getResources().getDrawable(R.drawable.red_label) : null);

        boolean isCancelled = watchStatus.equals(MyShowsApi.STATUS.cancelled);
        cancelledButton.setBackgroundDrawable(isCancelled ? getResources().getDrawable(R.drawable.red_label) : null);

        boolean isRemove = watchStatus.equals(MyShowsApi.STATUS.remove);
        removeButton.setBackgroundDrawable(isRemove ? getResources().getDrawable(R.drawable.red_label) : null);


    }

    public void changeShowStatus(View v) {
        switch (v.getId()) {
            case R.id.button_watching:
                watchStatus = MyShowsApi.STATUS.watching;
                break;
            case R.id.button_will_watch:
                watchStatus = MyShowsApi.STATUS.later;
                break;
            case R.id.button_cancelled:
                watchStatus = MyShowsApi.STATUS.cancelled;
                break;
            case R.id.button_remove:
                watchStatus = MyShowsApi.STATUS.remove;
                break;
        }

        //  activeWatchButton = (Button) v;
        ChangeShowStatusTask task = new ChangeShowStatusTask(getActivity());
        task.setChangeShowStatusListener(this);
        task.execute(show.getShowId(), watchStatus);


    }

    @Override
    public boolean onShowStatusChanged(boolean result) {
        Toast.makeText(getActivity(), result ? R.string.changes_saved : R.string.changes_not_saved, Toast.LENGTH_SHORT).show();
        if (result) {
            UserShow us = MyShows.getUserShow(show.getShowId());
            if (us != null)
                us.setWatchStatus(watchStatus);
            updateStatusButtons();
            yoursRatingBar.setIsIndicator(watchStatus.equals(MyShowsApi.STATUS.remove));
        }
        return true;
    }


}
