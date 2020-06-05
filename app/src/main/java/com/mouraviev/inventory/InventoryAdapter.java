package com.mouraviev.inventory;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InvViewHolder> {
    public static final int SORT_NAME_UP = 1, SORT_NAME_DOWN = -1, SORT_ID_UP = 2, SORT_ID_DOWN = -2, SORT_COUNT_UP = 3, SORT_COUNT_DOWN = -3;
    private StateListener stateListerner;

    private ArrayList<InvViewHolder> dataset;
    private OkHttpClient httpClient;

    // Provide a suitable constructor (depends on the kind of dataset)
    public InventoryAdapter() {
        dataset = new ArrayList();
        httpClient = new OkHttpClient.Builder()
                .callTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    public void addErrorListenter(StateListener listener) {
        stateListerner = listener;
    }

    public void setSort(int sort) {

    }

    public void loadInventory() {
        Request request;

        try {
            request = new Request.Builder()
                    .url(MainActivity.site + "/get_inventory?uid=" + MainActivity.userId)
                    .build();

            if(stateListerner == null)
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
                                stateListerner.OnError(("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            ResponseBody responseBody = response.body();

                            if (responseBody == null) {
                                stateListerner.OnError(("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            Gson gson = new Gson();

                            Map<String, String> responseMap = gson.fromJson(responseBody.string(), new
                                    TypeToken<Map<String, String>>() {
                                    }.getType());

                            if (!responseMap.get("prodid").equals(curCode))
                                return;

                            if (responseMap.get("err").equals("1")) {
                                stateListerner.OnError(("Ошибка авторизации. Проверьте соединение и данные");
                                showProdInfo("");
                                curCode = "";
                                return;
                            }

                            if (responseMap.get("err").equals("2")) {
                                showProdInfo("Не найден №" + responseMap.get("prodid"));
                                stateListerner.OnError(("Не найден №" + responseMap.get("prodid") + ". Проверьте данные");
                                curCode = "";
                                return;
                            }

                            if (!responseMap.get("err").equals("0")) {
                                stateListerner.OnError(("Ошибка. Проверьте соединение и данные");
                                showProdInfo("");
                                curCode = "";
                                return;
                            }

                            stateListerner.OnError(("\u2211" + responseMap.get("count") + " №" + responseMap.get("prodid") + " " + responseMap.get("prodname"));
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText("10");//mDataset[position]);
    }

    // Return the size of your dataset (invoked by the inv_list_item manager)
    @Override
    public int getItemCount() {
        return 10;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class InvViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView textView;
        String prodid, count, name;

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

}