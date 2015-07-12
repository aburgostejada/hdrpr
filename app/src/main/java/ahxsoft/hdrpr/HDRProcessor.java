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
    private String TAG = "HDRProcessorService";
    public static final String folderPath = "/sdcard/hdr/";

    static final int START = 1;
    static final int RESPONSE = 2;

    Messenger mMessenger = new Messenger(new IncomingHandler());

    static class IncomingHandler extends Handler {
        public native String startProcessJNI(String imagePath);

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
                    updateStatus(msg, startProcessJNI(folderPath));

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
