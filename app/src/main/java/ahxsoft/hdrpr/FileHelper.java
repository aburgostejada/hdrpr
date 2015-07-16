package ahxsoft.hdrpr;

import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileHelper {


    public static String durant = "_du.png";
    public static String hdr = ".hdr";
    public static String drago = "_dra.png";
    public static String fusion = "_fu.png";

    public static String getCurrentImageFolderName(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferencesKey), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.currentImageName), "");
    }

    public static void setCurrentImageFolderName(Context context, String currentName){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferencesKey), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.currentImageName), currentName);
        editor.commit();
    }

    public static String getValidFileName(String name){
        return name.replaceAll("\\W+", "");
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    public static String getFolderLocation(Context context, String currentImageName ) {
        return context.getExternalFilesDir(null).getAbsolutePath() + File.separator + getValidFileName(currentImageName) + File.separator;
    }

    public static String getCurrentFolderLocation(Context activity) {
        return getFolderLocation(activity, getCurrentImageFolderName(activity));
    }

    public static String getCurrentFolderLocation(Context activity, String imageType) {
        return getCurrentFolderLocation(activity) + getCurrentImageNameFor(activity, imageType);
    }

    public static String getCurrentImageNameFor(Context activity, String imageType) {
        return getCurrentImageFolderName(activity) + imageType;
    }

    public static File getMediaDirectory(String name) {
        return new File(getMediaDirectoryPath() +  name);
    }

    public static File getMediaDirectory() {
        return new File(getMediaDirectoryPath());
    }

    public static String getMediaDirectoryPath() {
        File imagesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return imagesPath + File.separator + HDRPR.IMAGE_DIR + File.separator;
    }

    public static void updateMediaServer(Context context, File file, String mimeType ) {
        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{ mimeType }, null);
    }

    public static boolean isImage(File file){
       String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
        for (String extension : okFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }

    public static Boolean copyImage(Activity activity, String imageType) {
            File image = new File(FileHelper.getCurrentFolderLocation(activity, imageType));
            File path = getMediaDirectory(image.getName());
            try {
                FileHelper.copyFile(image, path);
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                Uri uri = Uri.fromFile(path);
                ContentResolver cR = activity.getContentResolver();
                updateMediaServer(activity, path, mime.getMimeTypeFromExtension(cR.getType(uri)));
            } catch (IOException e) {
                return false;
            }
        return true;
    }

    public static Boolean copyAllImages(Activity activity) {
        return copyImage(activity, durant) && copyImage(activity, hdr) && copyImage(activity, drago) && copyImage(activity, fusion);

    }

    public static Boolean deleteImageFolder(Activity activity){
        try {
            deleteRecursive(new File(getCurrentFolderLocation(activity)));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static double getExposureTimeFromImagePath(String image){
        double exposure = -1;
        try {
            ExifInterface exif = new ExifInterface(image);
            String str = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            if(str != null){
                exposure = Double.parseDouble(str);
            }
        } catch (IOException e) {
            return exposure;
        }
        return exposure;
    }


}
