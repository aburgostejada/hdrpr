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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class Dashboard extends Fragment {
    private static final String ARG_SECTION_NUMBER = "dashboard";
    boolean isBound = false;
    private boolean saveHDFFile = false;
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
        bindService();
        String currentImage = getCurrentImageName();
        if(currentImage.equals("")){
            goToNewImage();
        }
    }

    private void bindService(){
        Intent intent = new Intent(getActivity(), HDRProcessor.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mMessenger == null){
            bindService();
        }
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
                if(isProcessImageVisible()){
                    sendMessage();
                    updateProcessImageButton(true);
                }else{
                    displayShowProcessImage();
                }
            }
        };
    }

    private void updateProcessImageButton(boolean hide) {
        Button newImage = (Button) rootView.findViewById(R.id.processImage);
        if(isProcessImageVisible()){
            newImage.setText(R.string.ok);
        }else {
            newImage.setText(R.string.processButton);
        }

        if(hide){
            newImage.setVisibility(View.INVISIBLE);
        }else{
            newImage.setVisibility(View.VISIBLE);
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
        bundle.putString(Constants.CURRENT_IMAGE_FOLDER_KEY, getCurrentImageFolder());
        bundle.putString(Constants.CURRENT_IMAGE_NAME_KEY, getCurrentImageName());
        bundle.putString(Constants.SELECTED_TONE_MAP_ALG_KEY, getSelectedToneMapAlg());
        bundle.putInt(Constants.SELECTED_MAX_IMAGE_HEIGHT_KEY, getSelectedMaxImageHeight());
        saveHDFFile = getShouldSaveHDRFIle();
        bundle.putBoolean(Constants.SHOULD_SAVE_HDR_FILE_KEY, saveHDFFile);


        msg.setData(bundle);
        updateStatus("Processing");
        try {
            if(mMessenger != null){
                mMessenger.send(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int getSelectedMaxImageHeight() {
        Spinner size =(Spinner) rootView.findViewById(R.id.sizes);
        return  Integer.parseInt(size.getSelectedItem().toString());
    }

    private boolean getShouldSaveHDRFIle() {
        CheckBox saveHDRFile = (CheckBox) rootView.findViewById(R.id.saveHdrFile);
        return saveHDRFile.isChecked();
    }

    private String getSelectedToneMapAlg() {
        Spinner toneMapAlg =(Spinner) rootView.findViewById(R.id.toneMapAlg);
        return toneMapAlg.getSelectedItem().toString();
    }

    private void displayShowProcessImage(){
        updateProcessImageButton(false);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment fragment = ProcessImageFragment.newInstance();
        ft.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out);
        ft.add(R.id.process_image_fragment_container, fragment, ProcessImageFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();

        getFragmentManager().addOnBackStackChangedListener(
        new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                updateProcessImageButton(false);
            }
        });
    }

    private boolean isProcessImageVisible(){
        FragmentManager fm = getFragmentManager();
        if(fm == null){
            return false;
        }else{
            Fragment fragment = fm.findFragmentByTag(ProcessImageFragment.TAG);
            if (fragment != null && fragment.isVisible()) {
                return true;
            }
            return false;
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

    private void handleImageProcessingComplete(){
        if(FileHelper.isExternalStorageWritable() && FileHelper.isExternalStorageReadable()){
            if(!FileHelper.copyAllImages(getActivity(), saveHDFFile)){
                updateStatus(R.string.generalError);
                return;
            }

            if(!FileHelper.deleteImageFolder(getActivity())){
                updateStatus(R.string.generalError);
                return;
            }

            FileHelper.setCurrentImageFolderName(getActivity(), "");
            goToImages();
        }else{
            updateStatus(R.string.cantAccessExternalStorage);
        }
    }

    private void goToImages(){
        HDRPR parent = (HDRPR) getActivity();
        parent.goToImages();
    }


    private void goToNewImage(){
        HDRPR parent = (HDRPR) getActivity();
        parent.goToNewImage();
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
