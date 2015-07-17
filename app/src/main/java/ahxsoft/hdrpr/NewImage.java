package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class NewImage extends Fragment {
    private static final String ARG_SECTION_NUMBER = "newImages";
    private static final String ARG_START_NEW_IMAGE = "startNewImage";
    public int RESULT_LOAD_IMAGES = 0x10;
    View rootView;
    String currentName;
    ClipData clipData;
    Boolean readyToProcess =false;
    Boolean startNewImage = false;


    public static NewImage newInstance(int sectionNumber) {
        NewImage fragment = new NewImage();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(int sectionNumber, boolean startNewImage) {
        NewImage fragment = new NewImage();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putBoolean(ARG_START_NEW_IMAGE, startNewImage);
        fragment.setArguments(args);
        return fragment;
    }

    public NewImage() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        startNewImage = args.getBoolean(ARG_START_NEW_IMAGE, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String currentImageName = FileHelper.getCurrentImageFolderName(getActivity());
        try {
            if (requestCode == RESULT_LOAD_IMAGES && resultCode == Activity.RESULT_OK && null != data && !currentImageName.equals("")) {
                preProcessSetUp(data);
            } else {
                AlertHelper.showLong(getActivity(), R.string.pickingImagesError);
            }
        } catch (Exception e) {
            AlertHelper.showLong(getActivity(), R.string.generalError);
        }
    }

    private void preProcessSetUp(Intent data){
        clipData = data.getClipData();
        loadNewImages(rootView);
        setCancelNewImageVisible(rootView);
        readyToProcess = true;
        updateNewImageButton(rootView, true);
    }

    private void updateNewImageButton(View rootView, Boolean readyToProcess) {
        Button newImage = (Button) rootView.findViewById(R.id.createNewImage);
        if(readyToProcess){
            newImage.setText(R.string.ok);
        }else {
            newImage.setText(R.string.createNewImage);
        }
    }

    private void setCancelNewImageVisible(View rootView) {
        rootView.findViewById(R.id.cancelNewImage).setVisibility(View.VISIBLE);
    }

//    private void setCancelNewImageInVisible(View rootView) {
//        rootView.findViewById(R.id.cancelNewImage).setVisibility(View.INVISIBLE);
//    }

    private void prepareForProcessing(ClipData clipData) {
        for (int i = 0; i < clipData.getItemCount(); i++)
        {
            if(FileHelper.isExternalStorageWritable() && FileHelper.isExternalStorageReadable()){
                Uri uri = clipData.getItemAt(i).getUri();
                File image = new File(FileHelper.getRealPathFromUri(getActivity(), uri));
                String path = FileHelper.getCurrentFolderLocation(getActivity());
                try {
                    FileHelper.copyFile(image, new File(path + image.getName()));
                } catch (IOException e) {
                    AlertHelper.showLong(getActivity(), R.string.generalError);
                }
            }else{
                AlertHelper.showShort(getActivity(), R.string.problemsWithExternalStorage);
            }
        }
        FileHelper.createProcessControlFile(getActivity(), getDataFromView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.fragment_new_image, container, false);
        Button newImageButton = (Button) rootView.findViewById(R.id.createNewImage);
        Button cancelImageButton= (Button) rootView.findViewById(R.id.cancelNewImage);
        newImageButton.setOnClickListener(createNewImagesOnClickListener());
        cancelImageButton.setOnClickListener(cancelNewImagesOnClickListener());

        if(startNewImage){
            displayShowNewImage();
        }

        return rootView;
    }


    private OnClickListener cancelNewImagesOnClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                readyToProcess = false;
                HDRPR parent = (HDRPR) getActivity();
                parent.goToStartNewImage();
            }
        };
    }

    private void displayShowNewImage(){
        EditTextDialog eTDialog = AlertHelper.getNewEditTextDialog(getActivity(), R.string.nameForNewImage, R.string.ok, R.string.cancel, R.string.newProjectMessage);
        eTDialog.show(new CallableReturn<Void, String>() {
            @Override
            public Void call(String param) throws Exception {
                displayImagePicker();
                currentName = param;
                return null;
            }
        }, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        });
    }


    private OnClickListener createNewImagesOnClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(readyToProcess){
                    FileHelper.setCurrentImageFolderName(getActivity(), currentName);
                    prepareForProcessing(clipData);
                    goToDashboard();
                }else{
                    displayShowNewImage();
                }
            }
        };
    }

    private HashMap<String, Double> getDataFromView(){
        final ListView listView = (ListView) rootView.findViewById(R.id.images_list);

        ListAdapter adapter = listView.getAdapter();
        int count = adapter.getCount();
        HashMap<String, Double> images = new HashMap<>();

        for(int i=0;i<count ;i++){
            View view = listView.getChildAt(i);
            if(view != null){
                TextView name = (TextView) view.findViewById(R.id.name);
                EditText editText = (EditText) view.findViewById(R.id.exposure);
                Double exposure = NumberHelper.fromStringFraction(editText.getText().toString());
                images.put(name.getText().toString(),exposure);
            }else{
                ListItem item = (ListItem) adapter.getItem(i);
                images.put(item.getName(), item.getExposureTime());
            }
        }

        return images;
    }

    private void goToDashboard() {

        HDRPR parent = (HDRPR) getActivity();
        parent.goToDashboard();
    }

    private void displayImagePicker(){
        Intent chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(chooseIntent, RESULT_LOAD_IMAGES);
    }

    private void loadNewImages(View rootView){
        ArrayList<ListItem> listData = getListData(clipData);
        final ListView listView = (ListView) rootView.findViewById(R.id.images_list);
        listView.setAdapter(new ImagesListAdapter(getActivity(), listData, ImagesListAdapter.NewImage));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ListItem data = (ListItem) listView.getItemAtPosition(position);
                Toast.makeText(getActivity(), "Selected :" + " " + data, Toast.LENGTH_LONG).show();
            }
        });
    }

    private ArrayList<ListItem> getListData(ClipData clipData) {
        ArrayList<ListItem> listData = new ArrayList<>();
        File[] fileList = FileHelper.getMediaDirectory().listFiles();
        Arrays.sort(fileList, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Double.compare(1 / FileHelper.getExposureTimeFromImagePath(f2.getAbsolutePath()), 1 / FileHelper.getExposureTimeFromImagePath(f1.getAbsolutePath()));
            }
        });

        for (int i = 0; i < clipData.getItemCount(); i++)
        {
            if(FileHelper.isExternalStorageWritable() && FileHelper.isExternalStorageReadable()){
                File image = new File(FileHelper.getRealPathFromUri(getActivity(), clipData.getItemAt(i).getUri()));
                if(FileHelper.isImage(image)){
                    listData.add(ListItem.newFromImage(image));
                }
            }else{
                AlertHelper.showShort(getActivity(), R.string.problemsWithExternalStorage);
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
