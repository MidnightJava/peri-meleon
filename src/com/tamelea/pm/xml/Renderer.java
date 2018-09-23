package com.tamelea.pm.xml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

/**
 * Render DOM tree through XSLT and FOP.
 */
public final class Renderer {
	//singleton
	private static Renderer renderer = null;
	
	private FopFactory fopFactory;
	
	public static Renderer getRenderer()
	throws RendererException
	{
		if (renderer == null) {
			try {
				renderer = new Renderer();
			} catch (Exception e)  {
				throw new RendererException(e);
			}
		}
		return renderer;
	}
	
	private Renderer() 
	throws SAXException, IOException 
	{
		fopFactory = FopFactory.newInstance();
		fopFactory.setUserConfig("file:/Library/Application Support/com.tamelea.perimeleon/resource/fop.xconf");
	}
	
	public void render(Document original, String stylesheet, FileOutputStream output) 
	throws RendererException 
	{
		try {
			TransformerFactory factory  = TransformerFactory.newInstance();
//		InputStream styleSheetStream = PeriMeleon.class.getClassLoader().getResourceAsStream("resource/directory-pdf.xsl");
			InputStream styleSheetStream = new FileInputStream(stylesheet);
			Document styleSheetDocument = new SAXReader(false).read(styleSheetStream);
			DocumentSource styleSheetSource = new DocumentSource(styleSheetDocument);
			Transformer transformer = factory.newTransformer(styleSheetSource);
			DocumentSource source = new DocumentSource(original);
			DocumentResult transformed = new DocumentResult();
			transformer.transform(source, transformed);
			Document transformedDoc = transformed.getDocument();
			Util.dumpDocument(transformedDoc, System.out);
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, output);
			Transformer identityTransformer = factory.newTransformer();
			//TODO Eventually do away with intermediate Document
			DocumentSource fopInputSource = new DocumentSource(transformedDoc);
//		Source fopInputSource = new StreamSource(new File("fop/fonts1.xml"));
			Result fopResult = new SAXResult(fop.getDefaultHandler());
			identityTransformer.transform(fopInputSource, fopResult);
		} catch (Exception e) {
			throw new RendererException(e);
		}
	}

}
