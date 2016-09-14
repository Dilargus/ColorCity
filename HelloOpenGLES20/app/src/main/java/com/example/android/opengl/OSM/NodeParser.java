package com.example.android.opengl.OSM;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Woess
 *	XMLParser Class to fill buildings with data from Openstreetmap
 */
public class NodeParser extends DefaultHandler {

	public NodeParser(Context context) {
		super();
		this.context = context;
	}

	private static String TAG = "XMLHANDLER";
    private boolean way_tag = false;
    private Context context;
    private Toast toast;
   
    Map nodes = new HashMap<Long,Node>();


    public HashMap<Long,Node> getParsedData() {
            return (HashMap<Long,Node>) this.nodes;
    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {
            // Nothing to do
    }

    /** Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String namespaceURI, String localName,
                    String qName, Attributes atts) throws SAXException {
            if (localName.equals("node")) {
            	Node new_node =  new Node(Long.parseLong(atts.getValue(0)),Double.parseDouble(atts.getValue(1)),Double.parseDouble(atts.getValue(2)));
            	Log.i("Testing nodes" , new_node.getId() + " x " + new_node.getX() + " y " + new_node.getY());
				nodes.put(Long.parseLong(atts.getValue(0)), new_node);
            	if (toast == null) {
            		//toast = Toast.makeText(context.getApplicationContext(), "vertices: " + nodes.size(), Toast.LENGTH_LONG);
            	}
            	//toast.setText("vertices: " + nodes.size());
    			//toast.show();

            }
    }
   
    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
                    throws SAXException {
    }
   
    /** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
	public void characters(char ch[], int start, int length) {
	}
}
