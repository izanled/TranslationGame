package com.izanled.translation.game.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anjlab.android.iab.v3.SkuDetails;
import com.izanled.translation.game.R;

import java.util.ArrayList;

public class PurchasePointAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SkuDetails> products;
    EventListener listener;

    public PurchasePointAdapter(Context context, EventListener listener){
        this.context = context;
        this.products = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SkuDetails sku = products.get(position);
        final ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.point_item, parent, false);
            holder = new ViewHolder();
            holder.tv_label = convertView.findViewById(R.id.tv_label);
            holder.tv_price = convertView.findViewById(R.id.tv_price);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.tv_label.setText(sku.title.replaceAll("\\(.*\\)",""));
        holder.tv_price.setText(sku.priceText);
        convertView.setOnClickListener(v -> listener.purchaseProduct(sku.productId));

        return convertView;
    }

    public void update(ArrayList<SkuDetails> products){
        this.products = products;
        notifyDataSetChanged();
    }

    class ViewHolder{
        public TextView tv_label;
        public TextView tv_price;
    }

    public interface EventListener{
        void purchaseProduct(final String productId);
    }
}
