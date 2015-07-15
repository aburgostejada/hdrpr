package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class Dashboard extends Fragment {
    public static final String CURRENT_IMAGE_FOLDER_KEY = "currentImageFolder";
    public static final String CURRENT_IMAGE_NAME_KEY = "currentImageName";
    private static final String ARG_SECTION_NUMBER = "dashboard";

    boolean isBound = false;
    Messenger mMessenger;
    View rootView;
    Button button;

    public static Dashboard newInstance(int sectionNumber) {
        Dashboard fragment = new Dashboard();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public Dashboard() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), HDRProcessor.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.fragment_dashboard, container, false);
        button = (Button) rootView.findViewById(R.id.createNewImage);
        updateStatus(getString(R.string.readyToProcess));


//        ThumbnailUtils.extractThumbnail()

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HDRPR) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void updateStatus(String status){
        TextView tv = (TextView) rootView.findViewById(R.id.textView);
        tv.setText(status + ": " + FileHelper.getCurrentImageFolderName(getActivity()));
    }

    private void updateStatus(int status){
        updateStatus(getString(status));
    }

    private String getCurrentImageFolder(){
        return FileHelper.getCurrentFolderLocation(getActivity());
    }

    public String getCurrentImageName() {
        return FileHelper.getCurrentImageFolderName(getActivity());
    }


    static abstract class ResponseHandler extends Handler {}

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            isBound = true;
            mMessenger = new Messenger(service);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message msg = Message.obtain(null, HDRProcessor.START, 0, 0);
                    msg.replyTo = new Messenger(new ResponseHandler(){
                        @Override
                        public void handleMessage(Message msg) {
                           handleServiceResponse(msg);
                        }
                    });
                    Bundle bundle = new Bundle();
                    bundle.putString(CURRENT_IMAGE_FOLDER_KEY, getCurrentImageFolder());
                    bundle.putString(CURRENT_IMAGE_NAME_KEY, getCurrentImageName());
                    msg.setData(bundle);
                    updateStatus("Processing");
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void handleImageProcessingComplete(){
            if(FileHelper.isExternalStorageWritable() && FileHelper.isExternalStorageReadable()){
                copyImage(FileHelper.durant);
                copyImage(FileHelper.drago);
                copyImage(FileHelper.hdr);
                copyImage(FileHelper.fusion);
            }else{
                updateStatus(R.string.cantAccessExternalStorage);
            }
        }

        private void copyImage(String imageType){
            File image = new File(FileHelper.getCurrentFolderLocation(getActivity(), imageType));
            File path = FileHelper.getMediaDirectory(image.getName());
            try {
                FileHelper.copyFile(image, path);
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                Uri uri = Uri.fromFile(path);
                ContentResolver cR = getActivity().getContentResolver();
                FileHelper.updateMediaServer(getActivity(), path, mime.getMimeTypeFromExtension(cR.getType(uri)));
            } catch (IOException e) {
                updateStatus(R.string.generalError);
                e.printStackTrace();
            }
        }

        private void handleServiceResponse(Message msg){
            int respCode = msg.what;
            switch (respCode) {
                case HDRProcessor.RESPONSE: {
                    String status = msg.getData().getString("respData");
                    if(getCurrentImageFolder().equals(status)){
                        handleImageProcessingComplete();
                        updateStatus(R.string.imageProcessCompleted);
                    }else {
                        updateStatus(R.string.generalError);
                    }

                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            isBound = false;
        }
    };



}
