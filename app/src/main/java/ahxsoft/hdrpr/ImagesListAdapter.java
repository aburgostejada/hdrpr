package ahxsoft.hdrpr;

import java.lang.Object;import java.lang.Override;import java.util.ArrayList;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagesListAdapter extends BaseAdapter {
    public static int NewImage = 1;
    public static int Images = 2;
    private final FragmentActivity activity;
    private final int layout;
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    public ImagesListAdapter(FragmentActivity activity, ArrayList<ListItem> listData, int Layout) {
        this.listData = listData;
        this.layout = Layout;
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
            if(layout == NewImage){
                convertView = layoutInflater.inflate(R.layout.list_row_layout_new_images, null);
            }else if(layout == Images){
                convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            }else {
                return null;
            }
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
            if(layout == NewImage){
                holder.exposure = (EditText) convertView.findViewById(R.id.exposure);
            }
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListItem newsItem = listData.get(position);
        holder.nameView.setText(newsItem.getName());

        if (holder.imageView != null) {
            new ImageAsyncTask(activity, holder.imageView).execute(newsItem.getImage());
        }

        if(layout == NewImage && holder.exposure != null && newsItem.getExposureTime() != -1){
            double exposureTime = newsItem.getExposureTime();
            String out;
            if(exposureTime < 1){
                exposureTime = 1 / exposureTime;
                out = "1/" + String.valueOf(Math.round(exposureTime));
            }else{
                out = String.valueOf(Math.round(exposureTime));
            }
            holder.exposure.setText(out);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView nameView;
        EditText exposure;
        ImageView imageView;

    }
}
