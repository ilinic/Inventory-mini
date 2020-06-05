package com.mouraviev.inventory;

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
    public static final int SORT_NAME_UP = 1, SORT_NAME_DOWN = -1, SORT_ID_UP = 2, SORT_ID_DOWN = -2, SORT_COUNT_UP = 3, SORT_COUNT_DOWN = -3;
    private static int curSort;
    private StateListener stateListerner;
    private ArrayList<JsonWrapper> data;
    private OkHttpClient httpClient;

    // Provide a suitable constructor (depends on the kind of dataset)
    public InventoryAdapter() {
        httpClient = new OkHttpClient.Builder()
                .callTimeout(2, TimeUnit.SECONDS)
                .build();

        data = new ArrayList();

        curSort = SORT_NAME_UP;
    }

    public void addErrorListener(StateListener listener) {
        stateListerner = listener;
    }

    public void setSort(int sort) {
        curSort = sort;
        Collections.sort(data);
    }

    public void loadInventory() {
        Request request;

        try {
            request = new Request.Builder()
                    .url(MainActivity.site + "/get_inventory?uid=" + MainActivity.userId)
                    .build();

            if (stateListerner == null)
                stateListerner = new StateListener();

            stateListerner.OnBusyStart();

            httpClient.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            stateListerner.OnError("Ошибка. Проверьте соединение и данные");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                stateListerner.OnError("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            ResponseBody responseBody = response.body();

                            if (responseBody == null) {
                                stateListerner.OnError("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            try {

                                Gson gson = new Gson();

                                data = new ArrayList<>();
                                Collections.addAll(data, gson.fromJson(responseBody.string(), JsonWrapper[].class));

                                setSort(curSort);

                                stateListerner.OnSuccess();
                                notifyDataSetChanged();

                            } catch (Exception e) {
                                stateListerner.OnError("Ошибка. Проверьте соединение и данные");
                            }
                        }
                    });

        } catch (Exception e) {
            stateListerner.OnError("Ошибка. Проверьте соединение и данные");
        }
    }

    // Create new views (invoked by the inv_list_item manager)
    @Override
    public InvViewHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inv_list_item, parent, false);

        InvViewHolder vh = new InvViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the inv_list_item manager)
    @Override
    public void onBindViewHolder(InvViewHolder holder, int position) {
        JsonWrapper el = data.get(position);
        holder.textView.setText("\u2211" + el.id + " №" + el.cnt + " " + el.name);
    }

    // Return the size of your dataset (invoked by the inv_list_item manager)
    @Override
    public int getItemCount() {
        return data.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class InvViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView textView;

        public InvViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    public static class StateListener {
        public void OnBusyStart() {
        }

        public void OnError(String msg) {
        }

        public void OnSuccess() {
        }
    }


    class JsonWrapper implements Comparable<JsonWrapper> {
        String id, name;
        int cnt;

        @Override
        public int compareTo(@NonNull JsonWrapper o) {
            switch (InventoryAdapter.curSort) {
                case SORT_NAME_UP:
                    return name.compareTo(o.name);
                case SORT_NAME_DOWN:
                    return -name.compareTo(o.name);
                case SORT_ID_UP:
                    return id.compareTo(o.id);
                case SORT_ID_DOWN:
                    return -id.compareTo(o.id);
                case SORT_COUNT_UP:
                    return cnt - o.cnt;
                case SORT_COUNT_DOWN:
                    return -cnt + o.cnt;
                default:
                    return 0;
            }
        }
    }
}