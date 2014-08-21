package ch.picard.ppimapbuilder.data;

import java.util.ArrayList;
import java.util.List;

public class JSONUtils {

	public static List<String> jsonListToStringList(JSONable... jsonAbles) {
		ArrayList<String> list = new ArrayList<String>();
		for (JSONable jsonAble : jsonAbles) {
			list.add(jsonAble.toJSON());
		}
		return list;
	}

}
