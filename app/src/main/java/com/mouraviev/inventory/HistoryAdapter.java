package com.mouraviev.inventory;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistViewHolder> {

    private final Handler activityUIhandler;
    private ArrayList<JsonWrapper> data;
    private OkHttpClient httpClient;

    public HistoryAdapter(Handler handler) {

        activityUIhandler = handler;

        httpClient = new OkHttpClient.Builder()
                .callTimeout(20, TimeUnit.SECONDS)
                .build();

        data = new ArrayList();
    }


    synchronized public void loadHistory() {
        Request request;

        try {
            request = new Request.Builder()
                    .url(MainActivity.site + "/get_inventory?uid=" + MainActivity.userId)
                    .build();

            activityUIhandler.sendEmptyMessage(InventoryActivity.MSG_START);

            httpClient.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Message msg = new Message();
                            msg.obj = "Ошибка. Проверьте соединение и данные";
                            msg.what = InventoryActivity.MSG_ERROR;
                            activityUIhandler.sendMessage(msg);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            if (!response.isSuccessful()) {
                                Message msg = new Message();
                                msg.obj = "Ошибка. Проверьте соединение и данные";
                                msg.what = InventoryActivity.MSG_ERROR;
                                activityUIhandler.sendMessage(msg);
                                return;
                            }

                            ResponseBody responseBody = response.body();

                            if (responseBody == null) {
                                Message msg = new Message();
                                msg.obj = "Ошибка. Проверьте соединение и данные";
                                msg.what = InventoryActivity.MSG_ERROR;
                                activityUIhandler.sendMessage(msg);
                                return;
                            }

                            try {

                                Gson gson = new Gson();

                                data = new ArrayList<>();
                                Collections.addAll(data, gson.fromJson(responseBody.string(), JsonWrapper[].class));

                            } catch (Exception e) {

                                Log.e("InventoryAdapter", "httpClient.onResponse", e);
                                Message msg = new Message();
                                msg.obj = "Ошибка. Проверьте соединение и данные";
                                msg.what = InventoryActivity.MSG_ERROR;
                                activityUIhandler.sendMessage(msg);
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e("InventoryAdapter", "httpClient.newCall", e);
            Message msg = new Message();
            msg.obj = "Ошибка. Проверьте соединение и данные";
            msg.what = InventoryActivity.MSG_ERROR;
            activityUIhandler.sendMessage(msg);
        }
    }

    @Override
    public HistViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hist_list_item, parent, false);

        return new HistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistViewHolder holder, int position) {
        JsonWrapper el = data.get(position);

        // date delta cnt_before cnt_after prodid
        // user_name prod_name

        String top = String.format("%s   𝚫 %d   ∑ %d → %d   № %s", el.dt, el.d, el.bf, el.af, el.pid);
        String bot = String.format("%s   %s", el.unme, el.pnme);

        holder.textTopView.setText(top);
        holder.textBotView.setText(bot);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class HistViewHolder extends RecyclerView.ViewHolder {
        TextView textTopView, textBotView;

        public HistViewHolder(View v) {
            super(v);
            textTopView = v.findViewById(R.id.hist_top_text);
            textBotView = v.findViewById(R.id.hist_bot_text);
        }
    }

    static class JsonWrapper {
        String dt, unme, pnme, pid;
        int d, bf, af;
    }
}