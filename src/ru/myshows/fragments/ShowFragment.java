package ru.myshows.fragments;

import android.content.Context;
import android.os.Bundle;
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
import ru.myshows.api.MyShowsClient;
import ru.myshows.domain.Show;
import ru.myshows.domain.UserShow;
import ru.myshows.tasks.BaseTask;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gb
 * Date: 07.07.12
 * Time: 1:04
 * To change this template use File | Settings | File Templates.
 */
public class ShowFragment extends Fragment {


    private ImageView showLogo;
    private LinearLayout dateLayout;
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
    private View view;


    public ShowFragment() {
    }

    public ShowFragment(Show show, Double yoursRating) {
        this.show = show;
        this.watchStatus = show.getWatchStatus();
        this.yoursRating = yoursRating;

    }

    public void refresh(Show show) {
        populateUI(show);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.show, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if (savedInstanceState != null && show == null) {
//            show = (Show) savedInstanceState.getSerializable("show");
//            UserShow userShow = MyShows.getUserShow(show.getShowId());
//            show.setWatchStatus(userShow == null ? MyShowsApi.STATUS.remove : userShow.getWatchStatus());
//            watchStatus = show.getWatchStatus();
//            yoursRating = savedInstanceState.getDouble("yoursRating");
//            populateUI(show);
//        } else {

            Serializable s =  getArguments().getSerializable("show");
            if (s instanceof Show){
                show = (Show) s;
                watchStatus = show.getWatchStatus();
                yoursRating = getArguments().getDouble("rating");
                populateUI(show);
            //}
        }

    }


    private void populateUI(Show show) {
        showLogo = (ImageView) view.findViewById(R.id.show_logo);
        dateLayout = (LinearLayout) view.findViewById(R.id.show_date_layout);
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
            if (show.getWatchStatus().equals(MyShowsApi.STATUS.remove))
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

            statusButtonsLayoyt.setVisibility(View.VISIBLE);
            updateStatusButtons();



            watchingButton.setOnClickListener(statusButtonsListener);
            willWatchButton.setOnClickListener(statusButtonsListener);
            cancelledButton.setOnClickListener(statusButtonsListener);
            removeButton.setOnClickListener(statusButtonsListener);
        }





    }

    View.OnClickListener statusButtonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
            if (!show.getWatchStatus().equals(watchStatus)) {
                ChangeShowStatusTask task = new ChangeShowStatusTask(getActivity());
                task.execute(show.getShowId(), watchStatus);
            }
        }
    };



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (show != null){
            outState.putSerializable("show", show);
        }
        if (yoursRating != null)
            outState.putDouble("yoursRating", yoursRating);

    }

    public class ChangeShowRatioTask extends BaseTask<Boolean> {
        Float rating;

        public ChangeShowRatioTask(Context context) {
            super(context);
        }

        @Override
        public Boolean doInBackground(Object... objects)  {
            rating = (Float) objects[0];
            boolean result = MyShowsClient.getInstance().changeShowRatio(show.getShowId(), (int) rating.floatValue());
            return result;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (isAdded())
                Toast.makeText(getActivity(), result ? R.string.changes_saved : R.string.changes_not_saved, Toast.LENGTH_SHORT).show();
            if (result) {
                UserShow us = MyShows.getUserShow(show.getShowId());
                if (us != null)
                    us.setRating((double) rating);
            }

        }

//        @Override
//        public void onError(Exception e) {
//            e.printStackTrace();
//        }
    }

    private void updateStatusButtons() {
        if (isAdded()) {
            boolean isWatching = watchStatus.equals(MyShowsApi.STATUS.watching) || watchStatus.equals(MyShowsApi.STATUS.finished);
            watchingButton.setBackgroundDrawable(isWatching ? getResources().getDrawable(R.drawable.red_label) : null);

            boolean isLater = watchStatus.equals(MyShowsApi.STATUS.later);
            willWatchButton.setBackgroundDrawable(isLater ? getResources().getDrawable(R.drawable.red_label) : null);

            boolean isCancelled = watchStatus.equals(MyShowsApi.STATUS.cancelled);
            cancelledButton.setBackgroundDrawable(isCancelled ? getResources().getDrawable(R.drawable.red_label) : null);

            boolean isRemove = watchStatus.equals(MyShowsApi.STATUS.remove);
            removeButton.setBackgroundDrawable(isRemove ? getResources().getDrawable(R.drawable.red_label) : null);
        }
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
        if (!show.getWatchStatus().equals(watchStatus)) {
            ChangeShowStatusTask task = new ChangeShowStatusTask(getActivity());
            task.execute(show.getShowId(), watchStatus);
        }


    }


    public class ChangeShowStatusTask extends BaseTask<Boolean> {


        public ChangeShowStatusTask(Context context) {
            super(context);
        }

        @Override
        public Boolean doInBackground(Object... objects)  {

            int showId = (Integer) objects[0];
            MyShowsApi.STATUS status = (MyShowsApi.STATUS) objects[1];
            return MyShowsClient.getInstance().changeShowStatus(showId, status);
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (isAdded())
                Toast.makeText(context, result ? R.string.changes_saved : R.string.changes_not_saved, Toast.LENGTH_SHORT).show();
            if (result) {
                show.setWatchStatus(watchStatus);
                UserShow us = MyShows.getUserShow(show.getShowId());
                if (us != null) {
                    if (watchStatus.equals(MyShowsApi.STATUS.remove))
                        MyShows.userShows.remove(us);
                    else
                        us.setWatchStatus(watchStatus);
                } else {
                    if (!watchStatus.equals(MyShowsApi.STATUS.remove)) {
                        if (MyShows.userShows == null)
                            MyShows.userShows = new ArrayList<UserShow>();
                        MyShows.userShows.add(new UserShow(show, watchStatus));
                    }
                }
                yoursRatingBar.setIsIndicator(watchStatus.equals(MyShowsApi.STATUS.remove));
                updateStatusButtons();

            }
        }

//        @Override
//        public void onError(Exception e) {
//            e.printStackTrace();
//        }

    }


}
