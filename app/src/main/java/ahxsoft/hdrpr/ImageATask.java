package ahxsoft.hdrpr;

import java.lang.Integer;import java.lang.Override;import java.lang.Void;import java.lang.ref.WeakReference;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;


class ImageATask extends AsyncTask<Integer, Void, Bitmap> {
    private Context context;
    private final WeakReference<ImageView> imageViewReference;


    public ImageATask (Context context, ImageView imageView){
        this.context = context;
        imageViewReference = new WeakReference<>(imageView);

    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        return loadBitmap(params[0]);

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.placeholder);
                    imageView.setImageDrawable(placeholder);
                }
            }

        }
    }

    private Bitmap loadBitmap(Integer resourceId) {
        return  decodeSampledBitmapFromResource(context.getResources(), resourceId, 125, 125);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

//    private Bitmap loadBitmap(String url) {
//        HttpURLConnection urlConnection = null;
//        try {
//            URL uri = new URL(url);
//            urlConnection = (HttpURLConnection) uri.openConnection();
//
//            int statusCode = urlConnection.getResponseCode();
//            if (statusCode != HttpStatus.SC_OK) {
//                return null;
//            }
//
//            InputStream inputStream = urlConnection.getInputStream();
//            if (inputStream != null) {
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                return bitmap;
//            }
//        } catch (Exception e) {
//            urlConnection.disconnect();
//            Log.w("ImageDownloader", "Error downloading image from " + url);
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }
//        return null;
//    }
}