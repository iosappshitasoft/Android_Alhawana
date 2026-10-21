package com.app.Zego.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.addon.livestream.external.helper.LocaleManager;
import com.app.addon.livestream.external.utils.Constants;
import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.LayoutItemSeatrequestBinding;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomRequestAdapter extends RecyclerView.Adapter<RoomRequestAdapter.RoomRequestVH> {

    Context context;
    List<Map<String,String>> list=new ArrayList<>();
    RoomRequestClick roomRequestClick;
    String hostName="";

    public RoomRequestAdapter(Context context, List<Map<String, String>> list, RoomRequestClick roomRequestClick, String hostName) {
        this.context = context;
        this.list = list;
        this.hostName=hostName;
        this.roomRequestClick = roomRequestClick;
    }

    @NonNull
    @Override
    public RoomRequestVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemSeatrequestBinding bind=LayoutItemSeatrequestBinding.inflate(LayoutInflater.from(context),parent,false);
        return new RoomRequestVH(bind);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomRequestVH holder, int position) {
        Map<String,String> map=list.get(position);

        holder.bind.liveMemberName.setText((map.get("user_type").equalsIgnoreCase("host")? hostName : map.get(Constants.TAG_USER_NAME) )+"");
        if (LocaleManager.isRTL()) {
            holder.bind.liveMemberItemName.setText(" أرسل طلبًا لتكون في " +(map.get("user_type").equalsIgnoreCase("host")? hostName : map.get(Constants.TAG_USER_NAME) )+ " فيديو مباشر ");
            holder.bind.liveMemberItemAgree.setText("طلب الانضمام");
            holder.bind.liveMemberItemDisagree.setText("ينكر");
        } else {
            holder.bind.liveMemberItemName.setText(/*" " + getString(R.string.text_send_request) +*/ (map.get("user_type").equalsIgnoreCase("host")? hostName : map.get(Constants.TAG_USER_NAME) ) + " " + context.getString(R.string.text_live_video));
        }

        Glide.with(context)
                .load(map.get(Constants.TAG_USER_IMAGE))
                .placeholder(R.drawable.alhawana)
                .error(R.drawable.alhawana)
                .into(holder.bind.liveMemberItemIcon);
        holder.bind.liveMemberItemAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roomRequestClick.onRequestClick(map,"accept",position);
                notifyItemRemoved(position);
            }
        });

        holder.bind.liveMemberItemDisagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roomRequestClick.onRequestClick(map,"reject",position);
                notifyItemRemoved(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RoomRequestVH extends RecyclerView.ViewHolder{

        LayoutItemSeatrequestBinding bind;
        public RoomRequestVH(@NonNull LayoutItemSeatrequestBinding itemView) {
            super(itemView.getRoot());
            bind=itemView;
        }
    }

    public interface RoomRequestClick{
        public void onRequestClick(Map<String,String> map,String status,int pos);
    }

}
