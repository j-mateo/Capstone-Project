package com.benjaminearley.mysubs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.benjaminearley.mysubs.dummy.DummyContent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.appinvite.AppInviteInvitation;

import java.util.Arrays;

public class StoryListActivity extends AppCompatActivity {

    private static final int REQUEST_INVITE = 0;
    private static final String TAG = StoryListActivity.class.getSimpleName();

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getTitle());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BottomSheetDialogFragment bottomSheetDialogFragment = new SubredditBottomSheetDialogFragment();
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            });
        }

        View recyclerView = findViewById(R.id.story_list);

        if (findViewById(R.id.story_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        if (recyclerView != null) {
            setupRecyclerView((RecyclerView) recyclerView);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS, mTwoPane, this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.invite:
                onInviteClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                AnalyticsTrackers
                        .getInstance()
                        .get(AnalyticsTrackers.Target.APP)
                        .send(new HitBuilders.EventBuilder()
                                .setAction("GAPPS_INVITE")
                                .setCategory("SHARING")
                                .set("Invintation Count", Arrays.toString(ids))
                                .build()
                        );
            } else {
                Snackbar.make(findViewById(R.id.coord_layout), getString(R.string.invite_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setEmailHtmlContent("<html><body>" +
                        "<h1>" + getString(R.string.invite_h1) + "</h1>" +
                        "<a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">" + getString(R.string.invite_a) + "</a>" +
                        "<body></html>")
                .setEmailSubject(getString(R.string.invitation_subject))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }
}
