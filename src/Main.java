import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
	public static void main(String[] args) throws Exception {
		String jobID = "";
		if (args.length == 0) {
			String inputStr = "";
			do {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("please enter a jobID:");
				inputStr = br.readLine();
				if (inputStr.matches("\\d+")) {
					jobID = inputStr;
					break;
				} else {
					inputStr = "";
				}
			} while (inputStr.isEmpty());
		} else {
			String jobIdTmp = args[0];
			if (jobIdTmp.matches("\\d+")) {
				jobID = args[0];
			} else {
				throw new RuntimeException("The first parameter is used as jobID, so it should be a string contains only digit");
			}
		}
		String message = HttpUtil.doGet("http://10.1.0.16/dttJob" + jobID + "/work/testData.json");
		JSONObject json = parseStrToJson.switchMessage(message);
		ModifyXmlFile.modify(json, jobID, "../i2up/InstanceInfo.xml");
	}
}
