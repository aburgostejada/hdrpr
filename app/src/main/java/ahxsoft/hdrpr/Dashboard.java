package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Dashboard extends Fragment {
    public static final String CURRENT_IMAGE_FOLDER_KEY = "currentImageFolder";
    public static final String CURRENT_IMAGE_NAME_KEY = "currentImageName";
    private static final String ARG_SECTION_NUMBER = "dashboard";

    boolean isBound = false;
    boolean readyToProcess =false;
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
        button = (Button) rootView.findViewById(R.id.processImage);
        updateStatus(getString(R.string.readyToProcess));
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HDRPR) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void updateStatus(String status){
        TextView tv = (TextView) rootView.findViewById(R.id.dashboardLabel);
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

    private OnClickListener getProcessClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(readyToProcess){
                    sendMessage();
                }else{
                    displayShowProcessImage(view.getRootView());
                }
            }
        };
    }

    private void updateProcessImageButton(View rootView, boolean readyToProcess) {
        Button newImage = (Button) rootView.findViewById(R.id.processImage);
        if(readyToProcess){
            newImage.setText(R.string.ok);
        }else {
            newImage.setText(R.string.processButton);
        }
    }

    private void sendMessage(){
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

    private void displayShowProcessImage(final View rootView){
//        EditTextDialog eTDialog = getProcesTextDialog(getActivity(), R.string.nameForNewImage, R.string.ok, R.string.cancel, R.string.hdrOptions);
//        eTDialog.show(new CallableReturn<Void, String>() {
//            @Override
//            public Void call(String param) throws Exception {
//                sendMessage();
//                return null;
//            }
//        }, new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
//                return null;
//            }
//        });

        this.readyToProcess = true;
        updateProcessImageButton(rootView, true);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment fragment = ProcessImageFragment.newInstance();
        ft.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out);
        ft.add(R.id.process_image_fragment_container, fragment);
        ft.addToBackStack(null);
        ft.commit();

        getFragmentManager().addOnBackStackChangedListener(
        new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                readyToProcess = false;
                updateProcessImageButton(rootView, false);
            }
        });
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

    private void handleImageProcessingComplete(){
        if(FileHelper.isExternalStorageWritable() && FileHelper.isExternalStorageReadable()){
            if(!FileHelper.copyAllImages(getActivity())){
                updateStatus(R.string.generalError);
            }

            if(!FileHelper.deleteImageFolder(getActivity())){
                updateStatus(R.string.generalError);
            }
            HDRPR parent = (HDRPR) getActivity();
            parent.goToImages();
        }else{
            updateStatus(R.string.cantAccessExternalStorage);
        }
    }



    private static abstract class ResponseHandler extends Handler {}

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            isBound = true;
            mMessenger = new Messenger(service);
            button.setOnClickListener(getProcessClickListener());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            isBound = false;
        }
    };



}
