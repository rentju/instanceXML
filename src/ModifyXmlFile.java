import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.Iterator;

public class ModifyXmlFile {

	public static Document readFileToDocument(String xmlFilePath) throws DocumentException {
		//1.创建Reader对象
		SAXReader reader = new SAXReader();
		//2.加载xml
		return reader.read(new File(xmlFilePath));
	}

	public static XMLWriter readFileToXMLWriter(String xmlFilePath) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		return new XMLWriter(new FileOutputStream(xmlFilePath), format);
	}

	public static void modify(JSONObject json, String jobID, String xmlFilePath) throws Exception {
		Document document = readFileToDocument(xmlFilePath);
		//获取根节点
		Element rootElement = document.getRootElement();
		Iterator iterator = rootElement.elementIterator();
		while (iterator.hasNext()) {
			Element stu = (Element) iterator.next();
			Attribute attribute = stu.attribute("value");
			String paraName = stu.attributeValue("name");
			String paraValue = json.getString(paraName);
			if (StringUtil.isNotEmpty(paraValue)) {
				attribute.setValue(paraValue);
			}
			if ("DttJobId".equals(paraName)) {
				attribute.setValue(jobID);
			}
		}
		XMLWriter xmlWriter = readFileToXMLWriter(xmlFilePath);
		xmlWriter.write(document);
		xmlWriter.close();
	}
}
