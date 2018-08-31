package com.example.bacor.okim;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<MsgBean> list;
    private Context context;


    public MsgAdapter(List<MsgBean> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MsgBean msgBean = list.get(position);
        if(msgBean.getType() == MsgBean.TYPE_RECEIVED){
            //receive the message
            holder.mLlLeftMsg.setVisibility(View.VISIBLE);
            holder.mLlRightMsg.setVisibility(View.GONE);
            holder.mLeftMsg.setText(msgBean.getContent());
        }else if(msgBean.getType() == MsgBean.TYPE_SEND){
            //send the message
            holder.mLlLeftMsg.setVisibility(View.GONE);
            holder.mLlRightMsg.setVisibility(View.VISIBLE);
            holder.mRightMsg.setText(msgBean.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLlLeftMsg;
        private LinearLayout mLlRightMsg;
        private TextView mLeftMsg;
        private TextView mRightMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mLlLeftMsg = itemView.findViewById(R.id.ll_left_msg);
            mLlRightMsg = itemView.findViewById(R.id.ll_right_msg);
            mLeftMsg = itemView.findViewById(R.id.left_msg);
            mRightMsg = itemView.findViewById(R.id.right_msg);
        }
    }
}
