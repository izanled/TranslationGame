package com.izanled.translation.game.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.izanled.translation.game.R;
import com.izanled.translation.game.utils.ToastManager;
import com.izanled.translation.game.view.adapter.PurchasePointAdapter;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PurchaseDialog extends Dialog {

    Context context;
    EventListener listener;

    PurchasePointAdapter mAdapter;

    private BillingProcessor bp;
    ArrayList<SkuDetails> products = new ArrayList<>();

    public PurchaseDialog(@NonNull Context context, BillingProcessor bp, EventListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.bp = bp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_point);
        ButterKnife.bind(this);

        mAdapter = new PurchasePointAdapter(context, productId -> {
            listener.purchaseProduct(productId);
            dismiss();
        });

        lv_skus.setAdapter(mAdapter);
    }


    @Override
    public void show() {
        super.show();

        try {
            ArrayList<String> productIds = new ArrayList<>();

            for(String s : context.getResources().getStringArray(R.array.arrProductId))
                productIds.add(s);
            products = (ArrayList<SkuDetails>)bp.getPurchaseListingDetails(productIds);

            Collections.sort(products, (o1, o2) -> {
                if(o1.productId.equals("a2000") && !o2.productId.equals("a2000"))
                    return -1;
                else if(!o1.productId.equals("a2000") && o2.productId.equals("a2000"))
                    return 1;

                if(o1.priceLong > o2.priceLong)
                    return 1;
                else if(o1.priceLong < o2.priceLong)
                    return -1;
                else
                    return 0;
            });

            mAdapter.update(products);
        }catch (Exception e){
            e.printStackTrace();
            ToastManager.getInstance().showLongTosat(context.getString(R.string.error_msg_6));
            dismiss();
        }

    }

    public interface EventListener{
        void purchaseProduct(final String productId);
    }

    @BindView(R.id.lv_skus)    ListView lv_skus;
}
