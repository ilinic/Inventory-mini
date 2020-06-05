package com.mouraviev.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class InventoryActivity extends AppCompatActivity {

    PopupMenu popup;

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

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}