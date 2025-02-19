package mg.itu.prom.util;

import jakarta.servlet.ServletConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public abstract class XMLInterceptor {

    public static String readBasePackage(ServletConfig config) {
        return config.getInitParameter("base-package");
    }
    public static String readBasePackage(String configFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(configFile));

        Element root = document.getDocumentElement();
        NodeList componentScanList = root.getElementsByTagName("context:component-scan");

        if (componentScanList.getLength() > 0) {
            Element componentScanElement = (Element) componentScanList.item(0);
            return componentScanElement.getAttribute("base-package");
        }
        throw new Exception("Controller package not found");
    }

    public static String extractProjectName(String path) {
        String[] parts = path.split("[\\\\/]");
        return parts[1];
    }
}
