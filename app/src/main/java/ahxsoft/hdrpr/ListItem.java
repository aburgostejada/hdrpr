package ahxsoft.hdrpr;

import android.graphics.Bitmap;

import java.io.File;
import java.lang.Override;import java.lang.String;public class ListItem {

	private String name;
	private File image;

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


	@Override
	public String toString() {
		return "[ name=" + name + "]";
	}

	public static ListItem newFromImage(File image) {
		ListItem imagesList = new ListItem();
		imagesList.setImage(image);
		imagesList.setName(image.getName());
		return imagesList;
	}
}
