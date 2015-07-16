package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ImageViewer extends Fragment {
    private static final String ARG_SECTION_NUMBER = "imageViewer";
    public int RESULT_LOAD_IMAGES = 0x10;
    View rootView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.fragment_image_viewer, container, false);


        ArrayList<ListItem> listData = getListData();
        final ListView listView = (ListView) rootView.findViewById(R.id.images_list);
        listView.setAdapter(new ImagesListAdapter(getActivity(), listData, ImagesListAdapter.Images));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ListItem data = (ListItem) listView.getItemAtPosition(position);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(data.getImage()), "image/*");
                startActivity(intent);
            }
        });

        return rootView;
    }

    private ArrayList<ListItem> getListData() {
        ArrayList<ListItem> listData = new ArrayList<>();
        File[] fileList = FileHelper.getMediaDirectory().listFiles();
        Arrays.sort(fileList, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
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
