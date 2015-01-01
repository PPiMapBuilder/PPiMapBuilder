package ch.picard.ppimapbuilder.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JSONUtils {

	public static List<String> jsonListToStringList(JSONable... jsonAbles) {
		ArrayList<String> list = new ArrayList<String>();
		for (JSONable jsonAble : jsonAbles) {
			list.add(jsonAble.toJSON());
		}
		return list;
	}

	public static List<String> jsonListToStringList(Collection<? extends JSONable> jsonAbles) {
		return jsonListToStringList(jsonAbles.toArray(new JSONable[jsonAbles.size()]));
	}

}
