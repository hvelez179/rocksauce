package com.example.android.hectorvelez_androidcodechallenge.models;


import android.content.Context;

import com.example.android.hectorvelez_androidcodechallenge.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable{

    public static String URL_POSTS = "http://www.reddit.com/r/%s/.json";

    private String title;
    private String author;
    private String thumbnail;

    @SerializedName("num_comments")
    private int numberOfComments;

    private int ups;
    private int downs;

    public static void getPosts(Context context, FutureCallback<List<Post>> callback ){

        getPosts(context, "", callback);
    }

    public static void getPosts(Context context, String query, final FutureCallback<List<Post>> callback) {

        query = "".equals(query) ? context.getString( R.string.default_search_term) : query;

        Ion.with(context)
                .load("GET", String.format(URL_POSTS, query))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        List<Post> posts = new ArrayList<>();

                        if(e == null && result != null && !result.isJsonNull()) {

                            Gson gson = new Gson();

                            if (result.has("data")) {

                                JsonObject data = result.get("data").getAsJsonObject();

                                if (data.has("children")) {

                                    JsonArray children = data.get("children").getAsJsonArray();

                                    for (int i = 0; i < children.size(); i++) {

                                        JsonObject object = (JsonObject) children.get(i);

                                        if (object.has("data")) {

                                            Post post = gson.fromJson(object.get("data").getAsJsonObject(), Post.class);
                                            posts.add(post);
                                        }
                                    }
                                }
                            }
                        }

                        callback.onCompleted(e, posts);
                    }
                });
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public int getUps() {
        return ups;
    }

    public void setUps(int ups) {
        this.ups = ups;
    }

    public int getDowns() {
        return downs;
    }

    public void setDowns(int downs) {
        this.downs = downs;
    }



}
