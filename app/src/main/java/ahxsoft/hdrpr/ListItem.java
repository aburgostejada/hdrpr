package ahxsoft.hdrpr;

import java.io.File;
import java.lang.Override;import java.lang.String;

public class ListItem implements Comparable {

	private String name;
	private File image;
    private double exposureTime;

    public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public void setExposureTime(double exposureTime) {
        this.exposureTime = exposureTime;
    }

    public double getExposureTime(){
        return exposureTime;
    }


	@Override
	public String toString() {
		return "[ name=" + name + "]";
	}

	public static ListItem newFromImage(File image) {
		ListItem imagesList = new ListItem();
		imagesList.setImage(image);
		imagesList.setName(image.getName());
		imagesList.setExposureTime(FileHelper.getExposureTimeFromImagePath(image.getAbsolutePath()));
		return imagesList;
	}

    @Override
    public int compareTo(Object another) {
        return Double.compare(((ListItem)another).getExposureTime(), getExposureTime());
    }
}
