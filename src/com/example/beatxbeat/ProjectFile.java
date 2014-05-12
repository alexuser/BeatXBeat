package com.example.beatxbeat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

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
	private final String CLIP_ATTRIBUTE = "clip";
	private final String PROJECT_NAME_ATTRIBUTE = "name";
	private final String CLIP_RESULT_ATTRIBUTE = "clip_result";


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
			root.setAttribute(PROJECT_NAME_ATTRIBUTE, mName);
			mDoc.appendChild(root);

		}
		else {
			mDoc = mBuilder.parse(pFile);

			Element root = mDoc.getDocumentElement();
			if (!root.getNodeName().equals(PROJECT_TYPE)) {
				throw new IOException("Invalid file - file is not a BeatbyBeat project.");
			}
			mName = root.getAttribute(PROJECT_NAME_ATTRIBUTE);
		}
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
			Log.e("ProjectFile: save()", "Project " + mName + " has failed to save!");
			e.printStackTrace();
			return false;
		}
		Log.d("", "FILE SAVED at: " + mContext.getFilesDir().getPath() + "/beatxproject_" + mName + ".xml");
		return true;
	}

	/**
	 * Attempts to add the recorded clip with its beat representation to the project.
	 * If the project already contains the clip, this method does nothing.
	 * 
	 * @param pFile The file of the recorded clip.
	 * @param result The string representation of the beat time of the clip.
	 */
	public void addClip(File pFile, String resultPath) {
		Element root = mDoc.getDocumentElement();
		boolean hasClip = false;			
		NodeList listClips = root.getChildNodes();
		for ( int i = 0; i < listClips.getLength(); i++ ) {
			Element clip = (Element) listClips.item(i);
			if (clip.getAttribute(CLIP_ATTRIBUTE).equals(pFile.getPath())) {
				hasClip = true;
			}
		}
		if (!hasClip) {
			Element clip = mDoc.createElement(CLIP_ATTRIBUTE);
			clip.setAttribute(CLIP_ATTRIBUTE, pFile.getPath());
			clip.setAttribute(CLIP_RESULT_ATTRIBUTE, resultPath);
			root.appendChild(clip);
			save();
		}
		
	}


	/**
	 * Attempts to remove the recorded clip from the project.
	 * 
	 * @param pFile The file of the recorded clip.
	 * @param pPermDelete Boolean flag that selects whether or not to delete
	 * the file permanently.
	 */
	public void removeClip(File pFile, boolean pPermDelete) {
		Element root = mDoc.getDocumentElement();
		NodeList listClips = root.getChildNodes();
		Node nodeToRemove = null;
		for ( int i = 0; i < listClips.getLength(); i++ ) {
			Element clip = (Element) listClips.item(i);
			if (clip.getAttribute(CLIP_ATTRIBUTE).equals(pFile.getPath())) {
				nodeToRemove = clip;
			}
		}
		if (nodeToRemove != null) {
			root.removeChild(nodeToRemove);
			if (pPermDelete) {
				boolean deleted = pFile.delete();
				Log.d("ProjectFile", "The clip file has " + (deleted ? "":"not ") + "been deleted!");
				File resultFile = new File(pFile.getPath().replace(".pcm", ".txt"));
				deleted = resultFile.delete();
				Log.d("ProjectFile", "The result file has " + (deleted ? "":"not ") + "been deleted!");
			}
			save();
		}
	}

	/**
	 * Returns a list of paths of clips. 
	 * @return ArrayList of filename strings
	 */
	public ArrayList<String> getClips() {
		ArrayList<String> listOfFilenames = new ArrayList<String>();
		Element root = mDoc.getDocumentElement();
		NodeList listClips = root.getChildNodes();
		for ( int i = 0; i < listClips.getLength(); i++ ) {
			Element clip = (Element) listClips.item(i);
			if (clip.hasAttribute(CLIP_ATTRIBUTE)) {
				String clipPath = clip.getAttribute(CLIP_ATTRIBUTE);
				listOfFilenames.add(clipPath);
			}
		}
		return listOfFilenames;
	}

	/**
	 * Returns a hashmap mapping the clip name to the clip beat result. 
	 * @return HashMap of clipnames mapped to the clip result
	 */
	public HashMap<String, String> getClipResults() {
		HashMap<String, String> results = new HashMap<String, String>();
		Element root = mDoc.getDocumentElement();
		NodeList listClips = root.getChildNodes();
		for ( int i = 0; i < listClips.getLength(); i++ ) {
			Element clip = (Element) listClips.item(i);
			if (clip.hasAttribute(CLIP_ATTRIBUTE)) {
				String clipPath = clip.getAttribute(CLIP_ATTRIBUTE);
				String clipName = clipPath.substring(clipPath.lastIndexOf("/"));
				String clipResult = null;
				try {
					clipResult = getResultString(clip.getAttribute(CLIP_RESULT_ATTRIBUTE));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				results.put(clipName, clipResult);
			}
		}
		return results;
	}

	/**
	 * Getter for project name.
	 * @return mName
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Changes the project name. Should delete old project file and create a new one.
	 * 
	 * @param pName new project name
	 */
	public void setProjectName(String pName) {
		File oldFile = new File(getProjectPath());
		boolean deleted = oldFile.delete();
		Log.d("ProjectFile", "The project file has " + (deleted ? "":"not ") + "been deleted!");
		mName = pName;
		Element root = mDoc.getDocumentElement();
		root.setAttribute(PROJECT_NAME_ATTRIBUTE, mName);
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

	/**
	 * Reads the .txt file and extracts the beat transcription result string.
	 * @param path Path of the text file containing result string.
	 * @return the Result string in the text file
	 * @throws IOException
	 */
	private String getResultString(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String nextLine = reader.readLine();
		String resultString = "";
		while (nextLine != null) {
			resultString = resultString + nextLine;
			nextLine = reader.readLine();
		}
		reader.close();
		return resultString;
	}



}
