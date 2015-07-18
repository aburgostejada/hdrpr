package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ImageViewer extends Fragment {
    private static final String ARG_SECTION_NUMBER = "imageViewer";
    View rootView;
    private int listPosition;

    public static ImageViewer newInstance(int sectionNumber) {
        ImageViewer fragment = new ImageViewer();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ImageViewer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        listPosition = info.position;
        menu.setHeaderTitle(R.string.choseAction);
        menu.add(0, v.getId(), 0, R.string.open);
        menu.add(0, v.getId(), 1, R.string.share);
        menu.add(0, v.getId(), 2, R.string.delete);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        ArrayList<ListItem> listData = getListData();
        final ListView listView = (ListView) rootView.findViewById(R.id.images_list);
        registerForContextMenu(listView);
        listView.setAdapter(new ImagesListAdapter(getActivity(), listData, ImagesListAdapter.Images));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ListItem data = (ListItem) listView.getItemAtPosition(position);
                openImage(data.getImage());
            }
        });


        return rootView;
    }

    private void openImage(File image){
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(image), "image/*");
        startActivity(intent);
    }

    private String getDeleteMessage(String name){
        return getString(R.string.areYouSure) + " " + name + " ?";
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        ListView listView = (ListView) rootView.findViewById(R.id.images_list);
        final ImagesListAdapter adapter = (ImagesListAdapter) listView.getAdapter();
        final ListItem data = (ListItem) adapter.getItem(listPosition);

        if(item.getTitle().equals(getString(R.string.open)))
        {
            openImage(data.getImage());
        }
        else if(item.getTitle().equals(getString(R.string.share)))
        {
            shareImage(data.getImage());
        }
        else if(item.getTitle().equals(getString(R.string.delete)))
        {
            AlertHelper.setYesNoDialog(getActivity(), R.string.deleteImage, getDeleteMessage(data.getName()),
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    deleteImage(data, adapter);
                    return false;
                }
            }, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return false;
                }
            });

        }

        return true;
    }

    private void shareImage(File image) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
    }

    private void deleteImage(ListItem image, ImagesListAdapter adapter) {
        image.delete();
        adapter.remove(image);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<ListItem> getListData() {
        ArrayList<ListItem> listData = new ArrayList<>();
        File[] fileList = FileHelper.getMediaDirectory().listFiles();
        Arrays.sort(fileList, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        for (File f : fileList)
        {
            if(FileHelper.isImage(f)){
                listData.add(ListItem.newFromImage(f));
            }
        }

        return listData;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HDRPR) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
