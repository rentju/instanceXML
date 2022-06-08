import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

public class parseStrToJson {

	public static JSONObject switchMessage(String message) {
		JSONObject json = JSONObject.parseObject(message);
		ArrayList<String> keyList = new ArrayList<>();
		for (String key : json.keySet()) {
			if (json.get(key) instanceof JSONArray) {
				keyList.add(key);
			}
		}
		for (String key : keyList) {
			JSONArray jsonArray = json.getJSONArray(key);
			for (int i = 0; i < jsonArray.size(); i++) {
				json.putAll(json.getJSONArray(key).getJSONObject(i));
			}
			json.remove(key);
		}
		return json;
	}
}
