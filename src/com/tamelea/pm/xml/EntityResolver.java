package com.tamelea.pm.xml;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.File;

public class EntityResolver extends DefaultHandler {

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException
	{
		//System.out.println("Entity public ID:" + publicId + " system ID:" + systemId);
		if (systemId.endsWith(".xsd")) {
			//Redirect search for schema to a resource in the classpath
			String schemaName = new File(systemId).getName();
			//System.out.println("Got schema name " + schemaName);
			String fileSeparator = System.getProperty("file.separator");
			String resourceName = "resources" + fileSeparator + schemaName;
			//System.out.println("Resource name: " + resourceName);
			InputSource is = new InputSource(ClassLoader
					.getSystemResourceAsStream("resource/" + resourceName));
			if (is != null)
				return is;
			throw new SAXException("Resource " + resourceName + " not found");
		}
		return null;
	}
}
