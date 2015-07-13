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
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Callable;

public class NewImage extends Fragment {
    private static final String ARG_SECTION_NUMBER = "newImages";
    public int RESULT_LOAD_IMAGES = 0x10;
    View rootView;
    Button button;

    public static NewImage newInstance(int sectionNumber) {
        NewImage fragment = new NewImage();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public NewImage() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String currentImageName = FileHelper.getCurrentImageName(getActivity());
        try {
            if (requestCode == RESULT_LOAD_IMAGES && resultCode == Activity.RESULT_OK && null != data && !currentImageName.equals("")) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++)
                {
                    if(FileHelper.isExternalStorageWritable() && FileHelper.isExternalStorageReadable()){
                        Uri uri = clipData.getItemAt(i).getUri();
                        File image = new File(FileHelper.getRealPathFromUri(getActivity(), uri));
                        String path = FileHelper.getFolderLocationForCurrentImage(getActivity());
                        FileHelper.copyFile(image, new File(path + image.getName()));
                    }else{
                        AlertHelper.showShort(getActivity(), R.string.problemsWithExternalStorage);
                    }
                }
            } else {
                AlertHelper.showLong(getActivity(), R.string.pickingImagesError);
            }
        } catch (Exception e) {
            AlertHelper.showLong(getActivity(), R.string.generalError);
        }finally {
            HDRPR parent = (HDRPR) getActivity();
            parent.goToDashboard();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.fragment_new_project, container, false);
        button = (Button) rootView.findViewById(R.id.createNewProject);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EditTextDialog eTDialog = AlertHelper.getNewEditTextDialog(getActivity(), R.string.nameForNewImage, R.string.ok, R.string.cancel, R.string.newProjectMessage);

                eTDialog.show(new CallableReturn<Void,String>() {
                    @Override
                    public Void call(String param) throws Exception {
                        FileHelper.setCurrentImageName(getActivity(), param);
                        Intent chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        startActivityForResult(chooseIntent, RESULT_LOAD_IMAGES);
                        return null;
                    }
                }, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        return null;
                    }
                });
            }
        });
        return rootView;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HDRPR) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
