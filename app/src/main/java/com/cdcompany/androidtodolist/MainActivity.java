package com.cdcompany.androidtodolist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.mongo.options.FindOptions;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;


import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private App realmApp;
    private MongoCollection<Document> mongoCollection;
    private User user;
    private String TAG = "MainActivity";
    private SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

    }

    protected void onStart() {
        super.onStart();
        loadData();
    }

    private void loadData() {
        setupDbConnection();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> descriptionList = findDescriptionList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupRecyclerView(descriptionList);
                        pullToRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void setupDbConnection() {
        String appID = "androidtodolist-ltwba";
        Realm.init(this); // initialize Realm, required before interacting with SDK
        realmApp = new App(new AppConfiguration.Builder(appID)
                .build());

        // an authenticated user is required to access a MongoDB instance
        Credentials credentials = Credentials.anonymous();
        realmApp.login(credentials);
        user = realmApp.currentUser();
        if (user.isLoggedIn()) {
            MongoClient mongoClient = user.getMongoClient("mongodb-atlas");
            if (mongoClient != null) {
//                mongoCollection = mongoClient.getDatabase("shopping").getCollection("itemcollections");
                mongoCollection = mongoClient.getDatabase("todos").getCollection("nothingtodos");

                Log.v(TAG, "Successfully connected to the MongoDB instance.");
            } else {
                Log.e(TAG, "Error connecting to the MongoDB instance.");
            }
        }
    }

    private List<String> findDescriptionList() {
        String sortString = "{ \"date\" : -1 }";
        Document sortBson = org.bson.Document.parse(sortString);
        RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find().sort(sortBson).iterator();
        List<String> list = new ArrayList<>();

        MongoCursor<Document> results = findTask.get();
        while (results.hasNext()) {
            Document document = results.next();
//            list.add(document.get("name").toString());
            list.add(document.get("todo_description").toString());
        }
        return list;
    }

    private void setupRecyclerView(List<String> todoList) {
        RecyclerView todoListRecyclerView = findViewById(R.id.todo_list);
        todoListRecyclerView.setAdapter(new TodoAdapter(todoList));
        todoListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}