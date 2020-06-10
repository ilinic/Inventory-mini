package com.ilinic.inventory;

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

public class InventoryActivity extends AppCompatActivity {

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
                                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.html_error), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    loadingPanel.setVisibility(View.INVISIBLE);
                                    updateMenu();
                                    return;

                                case MSG_START:
                                    loadingPanel.setVisibility(View.VISIBLE);
                                    return;

                                case MSG_SUCCESS:
                                    loadingPanel.setVisibility(View.INVISIBLE);
                                    updateMenu();
                                    listView.getAdapter().notifyDataSetChanged();
                            }
                        }
                    });
        }
    };
    InventoryAdapter inventoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        findViewById(R.id.scanBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        findViewById(R.id.historyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        popup = new PopupMenu(this, findViewById(R.id.menuBtn));

        popup.getMenuInflater().inflate(R.menu.menu_inventory, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.update_menu:
                        inventoryAdapter.loadInventory();
                        return true;
                    case R.id.sort_name:
                        inventoryAdapter.setSort(InventoryAdapter.curSort == InventoryAdapter.SORT_NAME ? -InventoryAdapter.SORT_NAME : InventoryAdapter.SORT_NAME);
                        return true;
                    case R.id.sort_id:
                        inventoryAdapter.setSort(InventoryAdapter.curSort == InventoryAdapter.SORT_ID ? -InventoryAdapter.SORT_ID : InventoryAdapter.SORT_ID);
                        return true;
                    case R.id.sort_count:
                        inventoryAdapter.setSort(InventoryAdapter.curSort == InventoryAdapter.SORT_COUNT ? -InventoryAdapter.SORT_COUNT : InventoryAdapter.SORT_COUNT);
                        return true;
                }

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

        inventoryAdapter = new InventoryAdapter(handler, ContextCompat.getColor(getApplicationContext(), R.color.color_item_dark));

        listView.setAdapter(inventoryAdapter);

        listView.setHasFixedSize(true);

        inventoryAdapter.loadInventory();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }

    synchronized private void updateMenu() {

        if (popup == null)
            return;

        popup.getMenu().findItem(R.id.sort_name).setTitle("    " + getResources().getString(R.string.sort_name));
        popup.getMenu().findItem(R.id.sort_id).setTitle("    " + getResources().getString(R.string.sort_id));
        popup.getMenu().findItem(R.id.sort_count).setTitle("    " + getResources().getString(R.string.sort_count));

        switch (InventoryAdapter.curSort) {
            case InventoryAdapter.SORT_NAME:
                popup.getMenu().findItem(R.id.sort_name).setTitle("◢ " + getResources().getString(R.string.sort_name));
                break;

            case -InventoryAdapter.SORT_NAME:
                popup.getMenu().findItem(R.id.sort_name).setTitle("◣ " + getResources().getString(R.string.sort_name));
                break;

            case InventoryAdapter.SORT_ID:
                popup.getMenu().findItem(R.id.sort_id).setTitle("◢ " + getResources().getString(R.string.sort_id));
                break;

            case -InventoryAdapter.SORT_ID:
                popup.getMenu().findItem(R.id.sort_id).setTitle("◣ " + getResources().getString(R.string.sort_id));
                break;

            case InventoryAdapter.SORT_COUNT:
                popup.getMenu().findItem(R.id.sort_count).setTitle("◢ " + getResources().getString(R.string.sort_count));
                break;

            case -InventoryAdapter.SORT_COUNT:
                popup.getMenu().findItem(R.id.sort_count).setTitle("◣ " + getResources().getString(R.string.sort_count));
                break;
        }
    }
}
