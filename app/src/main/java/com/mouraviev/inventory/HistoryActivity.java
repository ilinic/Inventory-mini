package com.mouraviev.inventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HistoryActivity extends AppCompatActivity {

    public static final int MSG_START = 0;
    public static final int MSG_ERROR = 1;
    public static final int MSG_SUCCESS = 2;

    PopupMenu popup = null;
    RecyclerView listView;

    @SuppressLint("HandlerLeak")
    public final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            final Message msg1 = msg;
            final View loadingPanel = findViewById(R.id.loadingPanel);

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            switch (msg1.what) {
                                case MSG_ERROR:
                                    Toast toast = Toast.makeText(getApplicationContext(), (String) msg1.obj, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    loadingPanel.setVisibility(View.INVISIBLE);
                                    return;

                                case MSG_START:
                                    loadingPanel.setVisibility(View.VISIBLE);
                                    return;

                                case MSG_SUCCESS:
                                    loadingPanel.setVisibility(View.INVISIBLE);
                                    listView.getAdapter().notifyDataSetChanged();
                            }
                        }
                    });
        }
    };

    HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.scanBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.inventoryBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InventoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });

        popup = new PopupMenu(this, findViewById(R.id.menuBtn));

        popup.getMenuInflater()
                .inflate(R.menu.menu_history, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                historyAdapter.loadHistory();
                return true;
            }
        });

        findViewById(R.id.menuBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        listView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        historyAdapter = new HistoryAdapter(handler, ContextCompat.getColor(getApplicationContext(), R.color.color_item_dark));

        listView.setAdapter(historyAdapter);

        listView.setHasFixedSize(true);
        historyAdapter.loadHistory();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}