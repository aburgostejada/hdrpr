package ahxsoft.hdrpr;

import java.lang.Override;import java.lang.String;public class ListItem {

	private String name;
	private int Path;

	public int getPath() {
		return Path;
	}

	public void setPath(int Path) {
		this.Path = Path;
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
}
