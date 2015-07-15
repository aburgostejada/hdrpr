package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.ContentResolver;
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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        String currentImageName = FileHelper.getCurrentImageFolderName(getActivity());
//
//        HDRPR parent = (HDRPR) getActivity();
//        parent.goToDashboard();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.fragment_image_viewer, container, false);


        ArrayList<ListItem> listData = getListData();
        final ListView listView = (ListView) rootView.findViewById(R.id.images_list);
        listView.setAdapter(new ImagesListAdapter(getActivity(), listData));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ListItem newsData = (ListItem) listView.getItemAtPosition(position);
                Toast.makeText(getActivity(), "Selected :" + " " + newsData, Toast.LENGTH_LONG).show();
            }
        });


        return rootView;
    }

    private ArrayList<ListItem> getListData() {
        ArrayList<ListItem> listData = new ArrayList<>();
        File[] fileList = FileHelper.getMediaDirectory().listFiles();
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
