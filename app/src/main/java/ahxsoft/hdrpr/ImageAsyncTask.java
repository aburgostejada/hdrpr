package ahxsoft.hdrpr;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.lang.Override;
import java.lang.Void;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

class ImageHandler {


    public void load(String imagePath, ImageView imageView) {
        Bitmap bitmap = getBitmapFromCache(imagePath);

        if (bitmap == null) {
            loadImage(imagePath, imageView);
        } else {
            cancelPotentialDownload(imagePath, imageView);
            imageView.setImageBitmap(bitmap);
        }
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
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String imagePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    private void loadImage(String imagePath, ImageView imageView) {
        if (cancelPotentialDownload(imagePath, imageView)) {
            ImageAsyncTask task = new ImageAsyncTask(imageView);
            DeferredDrawable deferredDrawable = new DeferredDrawable(task);
            imageView.setImageDrawable(deferredDrawable);
            task.execute(imagePath);
        }
    }

    class ImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        protected String imagePath;
        private final WeakReference<ImageView> imageViewReference;


        public ImageAsyncTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imagePath = params[0];
            return loadBitmap(imagePath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(imagePath, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        ImageAsyncTask imageAsyncTask = getImageAsyncTask(imageView);
                        if (this == imageAsyncTask) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }

            }
        }

        private Bitmap loadBitmap(String imagePath) {
            int width = 1024;
            int height = 720;

            return decodeSampledBitmapFromPath(imagePath, width, height);
        }
    }



    private static boolean cancelPotentialDownload(String imagePath, ImageView imageView) {
        ImageAsyncTask imageAsyncTask = getImageAsyncTask(imageView);

        if (imageAsyncTask != null) {
            String bitmapPath = imageAsyncTask.imagePath;
            if ((bitmapPath == null) || (!bitmapPath.equals(imagePath))) {
                imageAsyncTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static ImageAsyncTask getImageAsyncTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DeferredDrawable) {
                DeferredDrawable deferredDrawable = (DeferredDrawable) drawable;
                return deferredDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }


    static class DeferredDrawable extends ColorDrawable {
        private final WeakReference<ImageAsyncTask> imageAsyncTaskRef;

        public DeferredDrawable(ImageAsyncTask imageAsyncTask) {
            super(Color.TRANSPARENT);
            imageAsyncTaskRef = new WeakReference<>(imageAsyncTask);
        }

        public ImageAsyncTask getBitmapDownloaderTask() {
            return imageAsyncTaskRef.get();
        }
    }

     /*
     * Cache-related fields and methods.
     *
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */

        private static final int HARD_CACHE_CAPACITY = 10;
        private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

        // Hard cache, with a fixed maximum capacity and a life duration
        private final HashMap<String, Bitmap> sHardBitmapCache =
                new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
                        if (size() > HARD_CACHE_CAPACITY) {
                            // Entries push-out of hard reference cache are transferred to soft reference cache
                            sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                            return true;
                        } else
                            return false;
                    }
                };

        // Soft cache for bitmaps kicked out of hard cache
        private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
                new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

        private final Handler purgeHandler = new Handler();

        private final Runnable purger = new Runnable() {
            public void run() {
                clearCache();
            }
        };

        /**
         * Adds this bitmap to the cache.
         *
         * @param bitmap The newly downloaded bitmap.
         */
        private void addBitmapToCache(String url, Bitmap bitmap) {
            if (bitmap != null) {
                synchronized (sHardBitmapCache) {
                    sHardBitmapCache.put(url, bitmap);
                }
            }
        }

        /**
         * @param url The URL of the image that will be retrieved from the cache.
         * @return The cached bitmap or null if it was not found.
         */
        private Bitmap getBitmapFromCache(String url) {
            // First try the hard reference cache
            synchronized (sHardBitmapCache) {
                final Bitmap bitmap = sHardBitmapCache.get(url);
                if (bitmap != null) {
                    // Bitmap found in hard cache
                    // Move element to first position, so that it is removed last
                    sHardBitmapCache.remove(url);
                    sHardBitmapCache.put(url, bitmap);
                    return bitmap;
                }
            }

            // Then try the soft reference cache
            SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
            if (bitmapReference != null) {
                final Bitmap bitmap = bitmapReference.get();
                if (bitmap != null) {
                    // Bitmap found in soft cache
                    return bitmap;
                } else {
                    // Soft reference has been Garbage Collected
                    sSoftBitmapCache.remove(url);
                }
            }

            return null;
        }

        /**
         * Clears the image cache used internally to improve performance. Note that for memory
         * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
         */
        public void clearCache() {
            sHardBitmapCache.clear();
            sSoftBitmapCache.clear();
        }

        /**
         * Allow a new delay before the automatic cache clear is done.
         */
        private void resetPurgeTimer() {
            purgeHandler.removeCallbacks(purger);
            purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
        }


}


