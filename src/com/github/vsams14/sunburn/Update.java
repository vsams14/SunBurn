package com.github.vsams14.sunburn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Update{

	URL u = null;
	String title, link, version, thisversion;
	int changes;
	boolean off = false;
	File folder;
	private Main sunburn;

	public Update(Main sunburn){
		this.sunburn = sunburn;
	}

	public void readRSS() {
		folder = sunburn.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		try {
			u = new URL("http://dev.bukkit.org/server-mods/sunburn-reborn/files.rss");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			
			if(isInternetReachable()){
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = builder.parse(u.openStream());
				Element element = (Element)doc.getElementsByTagName("item").item(0);
				title = getElementValue(element, "title");
				link = getElementValue(element, "link");
				thisversion = sunburn.getDescription().getVersion().replaceAll("\\D+", "");
				version = title.replaceAll("\\D+", "");
			}else{
				thisversion = sunburn.getDescription().getVersion().replaceAll("\\D+", "");
				version = thisversion;
			}
		} catch (Exception e) {
		}

	}

	public boolean isInternetReachable(){
		try {
			InetAddress address = InetAddress.getByName("java.sun.com");

			if(address == null)
			{
				return false;
			}

		} catch (UnknownHostException e) {
			return false;
		}
		return true;
	}

	public boolean isCurrent() {
		if (version.equalsIgnoreCase(thisversion)) {
			return true;
		}
		int a = 0;
		try {
			a = Integer.parseInt(version);
		} catch (Exception e) {
			a = Integer.parseInt(version.replace(".", ""));
		}
		int b;
		try {
			b = Integer.parseInt(thisversion);
		}
		catch (Exception e)
		{
			b = Integer.parseInt(thisversion.replace(".", ""));
		}

		changes =  a-b;
		off = !(b>=a);
		return b >= a;
	}

	public boolean update() throws IOException, BadLocationException{
		URL toCheck = new URL(link);
		URL check = lookForLink(toCheck);
		if (check == null) {
			return false;
		}
		File updated = new File(folder.getParent() + File.separator + "SunBurn" + "." + 
				check.getPath().split("\\.")[(check.getPath().split("\\.").length - 1)]);
		if (updated.exists()) {
			updated.delete();
		}
		updated.createNewFile();
		ReadableByteChannel rbc = Channels.newChannel(check.openStream());
		FileOutputStream fos = new FileOutputStream(updated);
		fos.getChannel().transferFrom(rbc, 0L, 16777216L);
		fos.flush();
		fos.close();
		return true;

	}

	private URL lookForLink(URL url) throws IOException, BadLocationException {
		URLConnection connection = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		HTMLDocument htmlDoc = (HTMLDocument)new HTMLEditorKit().createDefaultDocument();
		HTMLEditorKit.Parser parser = new ParserDelegator();
		HTMLEditorKit.ParserCallback callback = htmlDoc.getReader(0);
		parser.parse(br, callback, true);

		for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.A); iterator.isValid(); iterator.next()) {
			AttributeSet attributes = iterator.getAttributes();

			int startOffset = iterator.getStartOffset();
			int endOffset = iterator.getEndOffset();
			int length = endOffset - startOffset;
			String text = htmlDoc.getText(startOffset, length);
			if (text.equalsIgnoreCase("Download")) {
				String link = (String)attributes.getAttribute(HTML.Attribute.HREF);
				if (link.startsWith("/")) {
					link = "http://" + url.getHost() + (String)attributes.getAttribute(HTML.Attribute.HREF);
				}
				return new URL(link);
			}
		}
		return null;
	}

	private String getElementValue(Element parent, String label) {
		return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));
	}

	private String getCharacterDataFromElement(Element e) {
		try {
			Node child = e.getFirstChild();
			if ((child instanceof CharacterData)) {
				CharacterData cd = (CharacterData)child;
				return cd.getData();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	public void unloadServer(){
		Server server = sunburn.getServer();
		server.savePlayers();
		for(Player player : server.getOnlinePlayers()){
			player.kickPlayer("Server Shutdown for updates.");
		}
		for(World world : server.getWorlds()){
			server.unloadWorld(world, true);
		}
		server.shutdown();
	}

}
