package ahxsoft.hdrpr;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class HDRProcessor extends Service {
    public static String TAG = "HDRProcessorService";
    static final int START = 1;
    static final int RESPONSE = 2;

    Messenger mMessenger = new Messenger(new IncomingHandler());

    static class IncomingHandler extends Handler {
        public native String startProcessJNI(String imagePath, String imageName, int maxHeight, String toneMapAlgType, boolean hdrFile);

        static {
            System.loadLibrary("hdrpr-jni");
        }

        private void updateStatus(Message msg , String status){
            Message resp = Message.obtain(null, RESPONSE);
            Bundle bResp = new Bundle();
            bResp.putString("respData", status);
            resp.setData(bResp);
            try {
                msg.replyTo.send(resp);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START:
                    Bundle data = msg.getData();
                    int maxImageHeight = data.getInt(Constants.SELECTED_MAX_IMAGE_HEIGHT_KEY);
                    boolean saveHDRFile = data.getBoolean(Constants.SHOULD_SAVE_HDR_FILE_KEY);
                    String folder = data.getString(Constants.CURRENT_IMAGE_FOLDER_KEY);
                    String imageName = data.getString(Constants.CURRENT_IMAGE_NAME_KEY);
                    String toneMapAlg = data.getString(Constants.SELECTED_TONE_MAP_ALG_KEY);
                    updateStatus(msg, startProcessJNI(folder, imageName, maxImageHeight, toneMapAlg, saveHDRFile));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    public HDRProcessor() {

    }

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }




}
