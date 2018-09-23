package com.tamelea.pm.xml;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ErrorHandler extends DefaultHandler {

  public void warning(SAXParseException e)
      throws SAXException
  {
    System.out.println("Parse warning: \n  ln: " + e.getLineNumber()
                       + "\n  URI: " + e.getSystemId()
                       + "\n  msg: " + e.getMessage());
    //throw new SAXException("Warning!");
  }

  public void error(SAXParseException e)
      throws SAXException
  {
    System.out.println("Parse error: \n  ln: " + e.getLineNumber()
                       + "\n  URI: " + e.getSystemId()
                       + "\n  msg: " + e.getMessage());
    throw new SAXException("Error!");
  }

  public void fatalError(SAXParseException e)
      throws SAXException
  {
    System.out.println("Fatal parse error: \n  ln: " + e.getLineNumber()
                       + "\n  URI: " + e.getSystemId()
                       + "\n  msg: " + e.getMessage());
    throw new SAXException("Fatal error!");
  }
}