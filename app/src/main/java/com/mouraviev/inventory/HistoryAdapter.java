package com.mouraviev.inventory;

import android.graphics.Color;
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
    private final int darkenColor;
    private ArrayList<JsonWrapper> data;
    private OkHttpClient httpClient;

    public HistoryAdapter(Handler handler, int darken) {

        darkenColor = darken;

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
                    .url(MainActivity.site + "/get_history?uid=" + MainActivity.userId)
                    .build();

            activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_START);

            httpClient.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_ERROR);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            if (!response.isSuccessful()) {
                                activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_ERROR);
                                return;
                            }

                            ResponseBody responseBody = response.body();

                            if (responseBody == null) {
                                activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_ERROR);
                                return;
                            }

                            try {

                                Gson gson = new Gson();

                                data = new ArrayList<>();
                                Collections.addAll(data, gson.fromJson(responseBody.string(), JsonWrapper[].class));

                                activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_SUCCESS);

                            } catch (Exception e) {

                                Log.e("HistoryAdapter", "httpClient.onResponse", e);
                                activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_ERROR);
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e("HistoryAdapter", "httpClient.newCall", e);
            activityUIhandler.sendEmptyMessage(HistoryActivity.MSG_ERROR);
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

        holder.textTopView.setText(el.topStr);
        holder.textBotView.setText(el.botStr);

        if (position % 2 == 0)
            holder.topView.setBackgroundColor(darkenColor);
        else
            holder.topView.setBackgroundColor(0x0);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class HistViewHolder extends RecyclerView.ViewHolder {
        TextView textTopView, textBotView;
        View topView;

        public HistViewHolder(View v) {
            super(v);
            textTopView = v.findViewById(R.id.hist_top_text);
            textBotView = v.findViewById(R.id.hist_bot_text);
            topView = v;
        }
    }

    static class JsonWrapper {
        String topStr, botStr;
    }
}