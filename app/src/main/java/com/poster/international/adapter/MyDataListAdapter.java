package com.poster.international.adapter;

/**
 * Created by lion1988 on 25.02.2015.
 */

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.poster.international.androidbootstrap.BootstrapButton;
import com.poster.international.androidbootstrap.BootstrapEditText;
import com.poster.international.api.CustomCookieStore;
import com.poster.international.app.R;
import com.poster.international.bitmaphandler.AsyncTask;
import com.poster.international.constants.Constant;
import com.poster.international.custom.designs.CustomBootstrapPhoneMask;
import com.poster.international.dialogs.GameStatusDialog;
import com.poster.international.dialogs.ProgressBarDialog;
import com.poster.international.model.FeedDataHolder;
import com.poster.international.model.MyDataModel;
import com.poster.international.model.UpdateDataModel;
import com.poster.international.picasso.CircleTransform;
import com.poster.international.picasso.PicassoBigCache;
import com.poster.international.util.Data;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;

public class MyDataListAdapter extends RecyclerView.Adapter<MyDataListAdapter.ViewHolder> {
    gghgkghgghj
    int rowLayout;
    private ArrayList<MyDataModel> myDataHoldersList;
    private FragmentActivity context;

    public MyDataListAdapter(ArrayList<MyDataModel> myDataHoldersList,
                                 FragmentActivity context, int rowLayout) {
        this.rowLayout = rowLayout;
        this.context = context;
        this.myDataHoldersList = myDataHoldersList;
        Log.d("AA", "Size MyDataActivity =>" + myDataHoldersList.size());
    }


    public void clearApplications() {
        int size = this.myDataHoldersList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                myDataHoldersList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addApplications(ArrayList<FeedDataHolder> feedDataHolderList) {
        this.myDataHoldersList.addAll(myDataHoldersList);
        this.notifyItemRangeInserted(0, feedDataHolderList.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final MyDataModel myData = myDataHoldersList.get(i);
        viewHolder.mdataName.setText(myData.getEmail());
        if (myData.getPhone() != null)
            viewHolder.mdataPhone.setText(myData.getPhone() + viewHolder.mdataPhone.getMask());
//        viewHolder.mdataBal.setText(myData.getBalance());
        viewHolder.mdataRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = null;
                String phone = null;
                name = viewHolder.mdataName.getEditableText().toString();
                phone = viewHolder.mdataPhone.getEditableText().toString();
                //  Check network connection. If fine, run feed
                //===========================================================================
                if (!Data.isNetworkEnabled) {
                    Toast.makeText(context.getApplicationContext(), Data.NETWORK_ISSUE,
                            Toast.LENGTH_SHORT).show();
                } else {
                    new UpdateDataAsync(name, phone).execute();
                }
            }
        });

        CircleTransform transform = new CircleTransform();
        PicassoBigCache.INSTANCE.getPicassoBigCache(context)
                .load(Constant.SERVER + Constant.MY_DATAS_API_IMG + myData.getImg())
                .transform(transform)
                .into(viewHolder.mdataImg);
    }

    @Override
    public int getItemCount() {
        return myDataHoldersList == null ? 0 : myDataHoldersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public BootstrapEditText mdataName;
        public CustomBootstrapPhoneMask mdataPhone;
        public ImageView mdataImg;
        public TextView mdataBal;
        public BootstrapButton mdataRefresh;

        public ViewHolder(View itemView) {
            super(itemView);
            mdataName = (BootstrapEditText) itemView.findViewById(R.id.mdata_login);
            mdataPhone = (CustomBootstrapPhoneMask) itemView.findViewById(R.id.mdata_phone);
            mdataImg = (ImageView) itemView.findViewById(R.id.mdata_img);
            mdataBal = (TextView) itemView.findViewById(R.id.mdata_balance);
            mdataRefresh = (BootstrapButton) itemView.findViewById(R.id.mdata_btn_refresh);
        }
    }


    //==========================================================================================
    //  Function for update, parse and install my data
    //==========================================================================================
    class UpdateDataAsync extends AsyncTask<String, String, String> {
        private ProgressBarDialog dialog;
        private String response;
        private String url;
        private String name;
        private String phone;
        private OkHttpClient client;

        public UpdateDataAsync(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressBarDialog(context, R.drawable.ic_spinner_alternative);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            url = Constant.SERVER + Constant.MY_DATAS_UPDATE_API;
            String json = null;
            client = new OkHttpClient();
            try {
                RequestBody formBody = new FormEncodingBuilder()
                        .add(Constant.MY_DATAS_CNAME, name)
                        .add(Constant.MY_DATAS_PHONE, phone)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();

                client.setCookieHandler(new CookieManager(
                        new CustomCookieStore(context),
                        CookiePolicy.ACCEPT_ALL));

                final Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful())
                    json = response.body().string();

            } catch (IOException e) {
                Log.d("TAG", "Exception: " + e.toString());
            }

            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result != null) {
                Gson gson = new Gson();
                UpdateDataModel model = gson.fromJson(result, UpdateDataModel.class);
                dialog.dismiss();
                GameStatusDialog gameStatus = new GameStatusDialog();
                if (model.getStatus().equals(Constant.SUCCESS_RESP)) {
                    gameStatus.GameStatus(Constant.DIALOG_TITLE_MY_DATA, Constant.SUCCESS);
                } else {
                    gameStatus.GameStatus(Constant.DIALOG_TITLE_MY_DATA, result);
                }
                gameStatus.show(context.getSupportFragmentManager(), "dialog");
            }
            else {
                dialog.dismiss();
            }
        }
    }

}

