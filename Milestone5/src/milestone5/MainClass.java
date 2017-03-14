package milestone5;

import java.io.IOException;

import example.DesignParser;

public class MainClass {

	public static void main(String[] args) {
		DesignParser designParser = new DesignParser();
		try {
			designParser.main(args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
