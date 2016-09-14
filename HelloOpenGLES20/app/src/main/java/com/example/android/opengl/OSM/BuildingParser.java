package com.example.android.opengl.OSM;

import android.content.Context;
import android.widget.Toast;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * @author Woess
 *	XMLParser Class to fill buildings with data from Openstreetmap
 */
public class BuildingParser extends DefaultHandler {

	public BuildingParser(Context context) {
		super();
		this.context = context;
	}


	private boolean alt_server = false;
	private static String TAG = "XMLHANDLER";
    private boolean way_tag = false;
    private Context context;
    private Toast toast;

	public BuildingParser(Context context, HashMap<Long,Node> nodes) {
		super();
		this.context = context;
		this.nodes = nodes;
		this.alt_server = true;
	}

    Map nodes = new HashMap();

    private ArrayList<Building> buildings = new ArrayList<Building>();
    private Building current;

    public ArrayList<Building> getParsedData() {
            return this.buildings;
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
				if(!alt_server) {
					Node new_node = new Node(Long.parseLong(atts.getValue(0)), Double.parseDouble(atts.getValue(1)), Double.parseDouble(atts.getValue(2)));
					//Log.i("Testing nodes", new_node.getId() + " x " + new_node.getX() + " y " + new_node.getY());
					nodes.put(Long.parseLong(atts.getValue(0)), new_node);
					if (toast == null) {
						//toast = Toast.makeText(context.getApplicationContext(), "vertices: " + nodes.size(), Toast.LENGTH_LONG);
					}
					//toast.setText("vertices: " + nodes.size());
					//toast.show();
				}
            }else if (localName.equals("way")) {
            	way_tag = true;
            	current = new Building(Long.parseLong(atts.getValue(0)));
            }else if (localName.equals("nd")) {
            	current.nodes.add((Node)nodes.get(Long.parseLong(atts.getValue(0))));
            }else if (localName.equals("tag")) {
            	String attrValue = atts.getValue("k");
            	if(way_tag && attrValue.equals("addr:street")){
            	   attrValue = atts.getValue("v");
             	   current.setName(attrValue);
            	}

	            else if(way_tag && attrValue.equals("building")){
            	   attrValue = atts.getValue("v");
            	   if(attrValue.equals("yes")){

            		   	current.center = current.polygonCenterOfMass();
					  // for(int i=current.nodes.size()/2 +1; i<current.nodes.size();i++){
						//   current.nodes.remove(i);
					  // }

            		   	buildings.add(current);
						if (toast == null) {
							//toast= Toast.makeText(context.getApplicationContext(), "Buildings: " + buildings.size(), Toast.LENGTH_LONG);
						}
						//toast.setText("Buildings: " + buildings.size());
						//toast.show();
            	   }
                	  
                }
            }
    }
   
    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
                    throws SAXException {
            if (localName.equals("way")) {
            	way_tag = false;
            }
    }
   
    /** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
	public void characters(char ch[], int start, int length) {
	}
}
