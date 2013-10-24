package ru.myshows.tasks;

import android.content.Context;
import android.widget.Toast;
import ru.myshows.activity.MyShows;
import ru.myshows.domain.IShow;
import ru.myshows.util.ShowsComparator;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: GGobozov
 * Date: 29.06.12
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class GetShowsTask extends BaseTask<List<IShow>>  {

    public static final int SHOWS_SEARCH = 1;
    public static final int SHOWS_TOP = 2;
    public static final int SHOWS_USER = 3;
    public static final int SHOWS_ALL = 4;

    private int action;

    public GetShowsTask(Context context, int action) {
        super(context);
        this.context = context;
        this.action = action;
    }

    public GetShowsTask(Context context, boolean forceUpdate, int action) {
        super(context, forceUpdate);
        this.action = action;
    }


    @Override
    public List<IShow> doInBackground(Object... objects)  {
        List shows = null;
        switch (action) {
            case SHOWS_SEARCH:
                String query = (String) objects[0];
                shows = client.search(query);
                break;
            case SHOWS_TOP:
                if (isForceUpdate)
                    MyShows.topShows = null;
                shows = MyShows.topShows != null ? MyShows.topShows : client.getTopShows(null);
                MyShows.topShows = shows;
                Collections.sort(shows, new ShowsComparator());
                break;
            case SHOWS_USER:
            default:
                if (isForceUpdate)
                    MyShows.userShows  = null;
                shows = MyShows.userShows != null ? MyShows.userShows:  client.getShows();
                MyShows.userShows = shows;

                // get unwatched episodes to make unwatched episodes seen in shows tab
                MyShows.newEpisodes =  client.getUnwatchedEpisodes();
                break;

        }
        return shows;
    }



    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }
}
