package com.mouraviev.inventory;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InvViewHolder> {
    public static final int SORT_NAME = 1, SORT_ID = 2, SORT_COUNT = 3;
    static int curSort;
    private ArrayList<JsonWrapper> data;
    private OkHttpClient httpClient;
    private final Handler activityUIhandler;

    public InventoryAdapter(Handler handler) {

        activityUIhandler = handler;

        httpClient = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .build();

        data = new ArrayList();

        curSort = SORT_NAME;
    }

    synchronized public void setSort(int sort) {
        curSort = sort;
        Collections.sort(data);
    }

    synchronized public void loadInventory() {
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
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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

                                setSort(curSort);
                                //notifyDataSetChanged();
                                activityUIhandler.sendEmptyMessage(InventoryActivity.MSG_SUCCESS);

                            } catch (Exception e) {
                                Message msg = new Message();
                                msg.obj = e.getMessage() + " Ошибка. Проверьте соединение и данные";
                                msg.what = InventoryActivity.MSG_ERROR;
                                activityUIhandler.sendMessage(msg);
                            }
                        }
                    });

        } catch (Exception e) {
            Message msg = new Message();
            msg.obj = "Ошибка. Проверьте соединение и данные";
            msg.what = InventoryActivity.MSG_ERROR;
            activityUIhandler.sendMessage(msg);
        }
    }

    @Override
    public InvViewHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inv_list_item, parent, false);

        InvViewHolder vh = new InvViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(InvViewHolder holder, int position) {
        JsonWrapper el = data.get(position);

        String id = String.format("%1$-10s", "№ " + el.id);
        String cnt = String.format("%1$-6s", " \u2211 " + el.cnt);

        holder.textView.setText(id + cnt + "  " + el.name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class InvViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public InvViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    class JsonWrapper implements Comparable<JsonWrapper> {
        String id, name;
        int cnt;

        @Override
        public int compareTo(@NonNull JsonWrapper o) {
            switch (InventoryAdapter.curSort) {
                case SORT_NAME:
                    return name.compareTo(o.name);
                case -SORT_NAME:
                    return -name.compareTo(o.name);
                case SORT_ID:
                    return id.compareTo(o.id);
                case -SORT_ID:
                    return -id.compareTo(o.id);
                case SORT_COUNT:
                    return cnt - o.cnt;
                case -SORT_COUNT:
                    return -cnt + o.cnt;
                default:
                    return 0;
            }
        }
    }
}