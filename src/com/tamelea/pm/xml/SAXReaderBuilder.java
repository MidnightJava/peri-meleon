package com.tamelea.pm.xml;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

public final class SAXReaderBuilder {
  public static final String nameSpace = "http://www.nova.mitre.org";

  public static SAXReader createReader() {
    SAXReader reader = new SAXReader(true);
    try {
      // enable namespace processing
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      //Enable validation by either schema or DTD, according as DOCTYPE or
      // <xs:schema> appear in instance
//      reader.setFeature("http://xml.org/sax/features/validation", true);
//      reader.setFeature("http://apache.org/xml/features/validation/schema", true);
      //entity resolver redirects search for schema
      reader.setEntityResolver(new EntityResolver());
      //reader.setErrorHandler(new ErrorHandler());
    } catch (SAXException e) {
      throw new IllegalArgumentException("ParserConfigurationException: " + e.getMessage());
    }
    return reader;
  }
  
  public static void dump(Document document) {
    try {
      OutputFormat format = OutputFormat.createPrettyPrint();
      XMLWriter writer = new XMLWriter(System.out, format);
      writer.write(document);
      writer.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //to be called after construction, to set your own error handler
//  public void setErrorHandler(DefaultHandler handler) {
//    builder.setErrorHandler(handler);
//  }
}