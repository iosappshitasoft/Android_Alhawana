package com.app.alhawanajd.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.model.UserList;

import java.util.List;

public class UserAdapter extends ArrayAdapter<UserList.Result> {

    private List<UserList.Result> users;
    private Context context;

    public UserAdapter(@NonNull Context context, @NonNull List<UserList.Result> users) {
        super(context, android.R.layout.simple_dropdown_item_1line, users);
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        UserList.Result user = users.get(position);

        textView.setText(user.getUserName()); // show username in dropdown
        return convertView;
    }

    @Nullable
    @Override
    public UserList.Result getItem(int position) {
        return users.get(position);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    public void updateList(List<UserList.Result> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }
}
