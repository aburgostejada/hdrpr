package ahxsoft.hdrpr;

import java.lang.Object;import java.lang.Override;import java.util.ArrayList;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagesListAdapter extends BaseAdapter {
    private final FragmentActivity activity;
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    public ImagesListAdapter(FragmentActivity activity, ArrayList<ListItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListItem newsItem = listData.get(position);
        holder.nameView.setText(newsItem.getName());

        if (holder.imageView != null) {
            new ImageAsyncTask(activity, holder.imageView).execute(newsItem.getImage());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView nameView;
        ImageView imageView;
    }
}
