package com.example.android.hectorvelez_androidcodechallenge.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import com.example.android.hectorvelez_androidcodechallenge.R;
import com.example.android.hectorvelez_androidcodechallenge.adapters.PostsAdapter;
import com.example.android.hectorvelez_androidcodechallenge.models.Post;
import com.koushikdutta.async.future.FutureCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
                                                                PostsAdapter.PostsAdapterListener{

    ActionBar mActionBar;
    RecyclerView mRecyclerView;
    PostsAdapter mAdapter;
    SearchView mSearchView;
    SwipeRefreshLayout mSwipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setBackgroundDrawable(new ColorDrawable( ContextCompat.getColor(this, R.color.colorWhite)));

        final View actionBarView = LayoutInflater.from(this).inflate(R.layout.layout_action_bar, null);

        //make action bar fill the entire width of the screen
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);

        mActionBar.setCustomView(actionBarView, layoutParams);

        Toolbar toolbar = (Toolbar) actionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setPadding(0, 0, 0, 0);

        View btnSearch = actionBarView.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fetchPosts(getQuery());
            }
        });

        mSearchView = (SearchView) actionBarView.findViewById(R.id.searchView);
        mSearchView.setIconifiedByDefault(false);   //make searchView query "always visible", xml property won't work
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(this);

        setRecyclerView(new ArrayList<Post>());

        fetchPosts(getQuery());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext( CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ShareActivity.REQUEST_CODE_SHARE_METHOD) {

            if(resultCode == RESULT_OK){

                Post post = data != null ? (Post) data.getSerializableExtra(ShareActivity.ARG_POST) : null;
                String shareMethod = data != null ? data.getStringExtra(ShareActivity.ARG_SHARE_METHOD) : ShareActivity.SHARE_METHOD_EMAIL;
                sharePost(post, shareMethod);
            }
        }
    }


    @Override
    public void onRefresh() {

        fetchPosts(getQuery());
    }

    @Override
    public void onItemClick(Post post) {

        selectShareMethod(post);
    }

    private void selectShareMethod(Post post) {

        if(post == null) {
            return;
        }

        Intent shareActivity = new Intent(this, ShareActivity.class);
        shareActivity.putExtra(ShareActivity.ARG_POST, post);
        startActivityForResult(shareActivity, ShareActivity.REQUEST_CODE_SHARE_METHOD);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom);
    }

    private void sharePost(Post post, String shareMethod) {

//        Toast.makeText(this, "Sharing post!!!", Toast.LENGTH_SHORT).show();
        String method = shareMethod.equals("text/html") ? "com.android.email.activity.MessageCompose" : "com.android.mms.ui.ConversationComposer";
        Intent intent = setShareIntent(post, shareMethod);

        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> activityList = pm.queryIntentActivities( intent, 0 );

        for( ResolveInfo app: activityList ){

            if( app.activityInfo.name.contains( method ) ){

                ActivityInfo activityInfo = app.activityInfo;
                ComponentName name = new ComponentName( activityInfo.applicationInfo.packageName, activityInfo.name );
                intent.setComponent( name );
                startActivity( intent );
                return;
            }
        }
    }

    private Intent setShareIntent(Post post, String shareMethod){

        Intent intent = new Intent( Intent.ACTION_SEND );
        intent.setType( shareMethod );

        if( shareMethod.equals( "text/html" ) ){

            intent.putExtra( Intent.EXTRA_EMAIL, "" );
            intent.putExtra( Intent.EXTRA_SUBJECT, post.getTitle() );
            intent.putExtra( Intent.EXTRA_TEXT, Html.fromHtml( "<img src='" + post.getThumbnail() + "' style='float:left; margin-right:15px' />" +
                    "<div style='float:left'><h3 style='margin:0'>" + post.getAuthor() + "</h3>" +
                    "<p style='margin:0'>" + post.getTitle() + "</p></div>" ) );

            return intent;
        }

        intent.putExtra( Intent.EXTRA_TEXT, post.getAuthor() + "\n" + post.getTitle() );

        return intent;
    }

    private void fetchPosts(String query) {

        Post.getPosts(this, query, new FutureCallback<List<Post>>() {
            @Override
            public void onCompleted(Exception e, List<Post> posts) {

                mAdapter.clear();
                mAdapter.addAll(posts);
                mSwipeContainer.setRefreshing(false);
            }
        });
    }

    private String getQuery() {

        String query = mSearchView.getQuery().toString();
        query = query.trim();
        query = "".equals(query) ? getString(R.string.default_search_term) : query;
        mSearchView.setQuery(query, false);

        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return query;
    }

    private void setRecyclerView(List<Post> posts) {

        if(posts == null){
            return;
        }

        if(mRecyclerView == null) {
            return;
        }

        mAdapter = new PostsAdapter(posts, this);
        mRecyclerView.setAdapter(mAdapter);
    }
}
