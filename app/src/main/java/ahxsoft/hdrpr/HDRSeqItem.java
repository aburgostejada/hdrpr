package ahxsoft.hdrpr;

import android.graphics.Bitmap;

class HDRSeqItem{
    private Bitmap image;
    private Double time;

    public HDRSeqItem(Bitmap image, Double time){
        this.image = image;
        this.time = time;
    }

    public Bitmap getImage(){
        return image;
    }

    public Double getTime(){
        return time;
    }
}
