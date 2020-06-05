package com.mouraviev.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class InventoryActivity extends AppCompatActivity {

    PopupMenu popup;
    RecyclerView listView;
    InventoryAdapter inventoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        findViewById(R.id.scanBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.historyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        popup = new PopupMenu(this, findViewById(R.id.menuBtn));

        popup.getMenuInflater()
                .inflate(R.menu.menu_inventory, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(
                        InventoryActivity.this,
                        "You Clicked : " + item.getTitle(),
                        Toast.LENGTH_SHORT
                ).show();

                return true;
            }
        });

        findViewById(R.id.menuBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        listView = (RecyclerView) findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the inv_list_item size of the RecyclerView
        //listView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        inventoryAdapter = new InventoryAdapter();

        inventoryAdapter.addErrorListener(new InventoryAdapter.StateListener() {
            @Override
            public void OnError(String msg) {
                showToast(msg);
            }

            @Override
            public void OnBusyStart() {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                            }
                        });
            }

            @Override
            public void OnSuccess() {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
                            }
                        });
            }
        });

        listView.setAdapter(inventoryAdapter);
        inventoryAdapter.loadInventory();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void showToast(final String msg) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
    }
}