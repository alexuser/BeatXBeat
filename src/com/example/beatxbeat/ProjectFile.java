package com.example.beatxbeat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;


public class ProjectFile {

	private DocumentBuilderFactory mDBFactory;
	private DocumentBuilder mBuilder;
	private Document mDoc;
	private String mName;
	private Context mContext;
	
	private final String PROJECT_TYPE = "beat_project";
	

	/**
	 * Instantiates a new project file.
	 * 
	 * @param pContext Context in which this project file exists.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ProjectFile(Context pContext) throws ParserConfigurationException, SAXException, IOException {
		this(pContext, null, "project" + System.currentTimeMillis());
	}

	/**
	 * Instantiates a project file. Either opens it from the given file, or creates a new project.
	 * If creating a new project, it is necessary to give it a name as an argument.
	 * 
	 * @param pContext Context in which this project file exists.
	 * @param pFile File of pre-existing project. If it is null, then method will create a new project.
	 * @param pName Name of project. Only used if pFile is null.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ProjectFile(Context pContext, File pFile, String pName) throws ParserConfigurationException, SAXException, IOException {
		mContext = pContext;
		
		mDBFactory = DocumentBuilderFactory.newInstance();
		mBuilder = mDBFactory.newDocumentBuilder();
		if (pFile == null) {
			mDoc = mBuilder.newDocument();
			mName = pName;
			
			Element root = mDoc.createElement(PROJECT_TYPE);
			mDoc.appendChild(root);
			root.setTextContent(mName);
			
		}
		else {
			mDoc = mBuilder.parse(pFile);

			Element root = mDoc.getDocumentElement();
			if (!root.getNodeName().equals(PROJECT_TYPE)) {
				throw new IOException("Invalid file - file is not a BeatbyBeat project.");
			}
			mName = root.getTextContent();
			
			NodeList listClips = root.getChildNodes();
			for ( int i = 0; i < listClips.getLength(); i++ ) {
				Node clip = listClips.item(i);
				String clipName = clip.getTextContent();
			}
		}
	}

	/**
	 * Attempts to open the given project. 
	 * 
	 * @param pFile The project file.
	 * @throws FileNotFoundException The given project could not be found, or 
	 * 	one of the clips contained in the project is missing.
	 */
	public void open(File pFile) throws FileNotFoundException{
		//implement me
		//I think this is already done in the constructor...
		//TODO Think through implementation details and usage.
	}

	/**
	 * Saves the project file onto the phone's internal storage. 
	 * 
	 * @return False if the project failed to save (ie some error was thrown), 
	 * 	otherwise returns True.
	 */
	public boolean save() {
		try {
			FileOutputStream file = mContext.openFileOutput("beatxproject_" + mName + ".xml", Context.MODE_PRIVATE);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    StringWriter writer = new StringWriter();
		    StreamResult result = new StreamResult(writer);
		    transformer.transform(new DOMSource(mDoc), result);
		    file.write(writer.toString().getBytes());
		    file.close();
		} catch (Exception e) {
			System.out.println("Project " + mName + " has failed to save!");
			e.printStackTrace();
			return false;
		}
		Log.d("", "FILE SAVED at: " + mContext.getFilesDir().getPath() + "/beatxproject_" + mName + ".xml");
		return true;
	}

	/**
	 * Attempts to add the recorded clip to the project.
	 * 
	 * @param pFile The file of the recorded clip.
	 */
	public void addClip(File pFile){
		Element root = mDoc.getDocumentElement();
		Element clip = mDoc.createElement("clip");
		clip.setTextContent(pFile.getPath());
		root.appendChild(clip);
		save();
	}

	/**
	 * Attempts to remove the recorded clip from the project.
	 * 
	 * @param pFile The file of the recorded clip.
	 */
	public void removeClip(File pFile) {
		Element root = mDoc.getDocumentElement();
		NodeList listClips = root.getChildNodes();
		Node nodeToRemove = null;
		for ( int i = 0; i < listClips.getLength(); i++ ) {
			Node clip = listClips.item(i);
			if (clip.getTextContent().equals(pFile.getPath())) {
				nodeToRemove = clip;
			}
		}
		if (nodeToRemove != null) {
			root.removeChild(nodeToRemove);
			save();
		}
	}
	
	/**
	 * Returns a list of clips. 
	 * @return ArrayList of filename strings
	 */
	public ArrayList<String> getClips() {
		ArrayList<String> listOfFilenames = new ArrayList<String>();
		Element root = mDoc.getDocumentElement();
		NodeList listClips = root.getChildNodes();
		for ( int i = 0; i < listClips.getLength(); i++ ) {
			Node clip = listClips.item(i);
			if (clip.getNodeName().equals("clip")) {
				String clipPath = clip.getTextContent();
				listOfFilenames.add(clipPath.substring(clipPath.indexOf("pcm")+3));
			}
		}
		return listOfFilenames;
	}
	
	/**
	 * Changes the project name. Should delete old project file and create a new one.
	 * 
	 * @para
	 * m pName new project name
	 */
	public void setProjectName(String pName) {
		mName = pName;
		Element root = mDoc.getDocumentElement();
		root.setTextContent(mName);
		//TODO delete old file, create new file with new name
		save();
	}
	
	/**
	 * Returns the absolute path to the directory on the filesystem where this project is stored.
	 * 
	 * @return Path of this project
	 */
	public String getProjectPath() {
		String path = mContext.getFilesDir().getPath() + "/beatxproject_" + mName + ".xml";
		return path;
	}

}
