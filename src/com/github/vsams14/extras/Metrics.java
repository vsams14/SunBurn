/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.github.vsams14.extras;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.github.vsams14.SunBurn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Metrics {

	private final static int REVISION = 5;
	private static final String BASE_URL = "http://mcstats.org";
	private static final String REPORT_URL = "/report/%s";
	private static final String CONFIG_FILE = "plugins/PluginMetrics/config.yml";
	private static final String CUSTOM_DATA_SEPARATOR = "~~";
	private static final int PING_INTERVAL = 10;
	private final Plugin plugin;
	private SunBurn sunburn;
	private Map<Plugin, Set<Graph>> graphs = Collections.synchronizedMap(new HashMap<Plugin, Set<Graph>>());
	private Graph burnGraph, wasteGraph;
	private final YamlConfiguration configuration;
	private final File configurationFile;
	private final String guid;
	private final Object optOutLock = new Object();
	private volatile int taskId = -1;

	public Metrics(final Plugin plugin) throws IOException {
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin cannot be null");
		}

		this.plugin = plugin;

		// load the config
		configurationFile = new File(CONFIG_FILE);
		configuration = YamlConfiguration.loadConfiguration(configurationFile);

		// add some defaults
		configuration.addDefault("opt-out", false);
		configuration.addDefault("guid", UUID.randomUUID().toString());

		// Do we need to create the file?
		if (configuration.get("guid", null) == null) {
			configuration.options().header("http://mcstats.org").copyDefaults(true);
			configuration.save(configurationFile);
		}

		// Load the guid then
		guid = configuration.getString("guid");
	}

	public Graph createGraph(Plugin plugin, Metrics.Graph.Type type, String name){
		if ((plugin == null) || (type == null) || (name == null)) {
			throw new IllegalArgumentException("All arguments must not be null");
		}

		Graph graph = new Graph(type, name);

		Set<Graph> graphs = getOrCreateGraphs(plugin);

		graphs.add(graph);

		return graph;
	}

	private Set<Graph> getOrCreateGraphs(Plugin plugin)
	{
		Set<Graph> theGraphs = (Set<Graph>) this.graphs.get(plugin);

		if (theGraphs == null) {
			theGraphs = Collections.synchronizedSet(new HashSet<Graph>());
			this.graphs.put(plugin, theGraphs);
		}

		return theGraphs;
	}

	public boolean start() {
		synchronized (optOutLock) {
			// Did we opt out?
			if (isOptOut()) {
				return false;
			}

			// Is metrics already running?
			if (taskId >= 0) {
				return true;
			}

			// Begin hitting the server with glorious data
			taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {

				private boolean firstPost = true;

				public void run() {
					try {
						// This has to be synchronized or it can collide with the disable method.
						synchronized (optOutLock) {
							// Disable Task, if it is running and the server owner decided to opt-out
							if (isOptOut() && taskId > 0) {
								plugin.getServer().getScheduler().cancelTask(taskId);
								taskId = -1;
								// Tell all plotters to stop gathering information.
								Set<Graph> valuesInSet = graphs.get(plugin);
								Iterator<Graph> gi = valuesInSet.iterator();
								while(gi.hasNext()){
									Graph graph = gi.next();
									graph.onOptOut();
								}
							}
						}

						// We use the inverse of firstPost because if it is the first time we are posting,
						// it is not a interval ping, so it evaluates to FALSE
						// Each time thereafter it will evaluate to TRUE, i.e PING!
						postPlugin(!firstPost);

						// After the first post we set firstPost to false
						// Each post thereafter will be a ping
						firstPost = false;
					} catch (IOException e) {
						Bukkit.getLogger().log(Level.INFO, "[Metrics] " + e.getMessage());
					}
				}
			}, 0, PING_INTERVAL * 1200);

			return true;
		}
	}

	public boolean isOptOut() {
		synchronized(optOutLock) {
			try {
				// Reload the metrics file
				configuration.load(CONFIG_FILE);
			} catch (IOException ex) {
				Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
				return true;
			} catch (InvalidConfigurationException ex) {
				Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
				return true;
			}
			return configuration.getBoolean("opt-out", false);
		}
	}

	public void enable() throws IOException {
		// This has to be synchronized or it can collide with the check in the task.
		synchronized (optOutLock) {
			// Check if the server owner has already set opt-out, if not, set it.
			if (isOptOut()) {
				configuration.set("opt-out", false);
				configuration.save(configurationFile);
			}

			// Enable Task, if it is not running
			if (taskId < 0) {
				start();
			}
		}
	}

	public void disable() throws IOException {
		// This has to be synchronized or it can collide with the check in the task.
		synchronized (optOutLock) {
			// Check if the server owner has already set opt-out, if not, set it.
			if (!isOptOut()) {
				configuration.set("opt-out", true);
				configuration.save(configurationFile);
			}

			// Disable Task, if it is running
			if (taskId > 0) {
				this.plugin.getServer().getScheduler().cancelTask(taskId);
				taskId = -1;
			}
		}
	}

	private void postPlugin(final boolean isPing) throws IOException {
		// The plugin's description file containg all of the plugin data such as name, version, author, etc
		final PluginDescriptionFile description = plugin.getDescription();

		addCustomData();

		// Construct the post data
		final StringBuilder data = new StringBuilder();
		data.append(encode("guid")).append('=').append(encode(guid));
		encodeDataPair(data, "version", description.getVersion());
		encodeDataPair(data, "server", Bukkit.getVersion());
		encodeDataPair(data, "players", Integer.toString(Bukkit.getServer().getOnlinePlayers().length));
		encodeDataPair(data, "revision", String.valueOf(REVISION));

		// If we're pinging, append it
		if (isPing) {
			encodeDataPair(data, "ping", "true");
		}

		// Acquire a lock on the graphs, which lets us make the assumption we also lock everything
		// inside of the graph (e.g plotters)
		synchronized (graphs) {
			Set<Graph> valuesInSet = getOrCreateGraphs(plugin);
			final Iterator<Graph> iter = valuesInSet.iterator();

			while (iter.hasNext()) {
				final Graph graph = iter.next();

				for (Plotter plotter : graph.getPlotters()) {
					// The key name to send to the metrics server
					// The format is C-GRAPHNAME-PLOTTERNAME where separator - is defined at the top
					// Legacy (R4) submitters use the format Custom%s, or CustomPLOTTERNAME
					final String key = String.format("C%s%s%s%s", CUSTOM_DATA_SEPARATOR, graph.getName(), CUSTOM_DATA_SEPARATOR, plotter.getColumnName());

					// The value to send, which for the foreseeable future is just the string
					// value of plotter.getValue()
					final String value = Integer.toString(plotter.getValue());

					// Add it to the http post data :)
					encodeDataPair(data, key, value);
				}
			}
		}

		// Create the url
		URL url = new URL(BASE_URL + String.format(REPORT_URL, encode(plugin.getDescription().getName())));

		// Connect to the website
		URLConnection connection;

		// Mineshafter creates a socks proxy, so we can safely bypass it
		// It does not reroute POST requests so we need to go around it
		if (isMineshafterPresent()) {
			connection = url.openConnection(Proxy.NO_PROXY);
		} else {
			connection = url.openConnection();
		}

		connection.setDoOutput(true);

		// Write the data
		final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(data.toString());
		writer.flush();

		// Now read the response
		final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		final String response = reader.readLine();

		// close resources
		writer.close();
		reader.close();

		if (response == null || response.startsWith("ERR")) {
			throw new IOException(response); //Throw the exception
		} else {
			// Is this the first update this hour?
			if (response.contains("OK This is your first update this hour")) {
				synchronized (graphs) {
					Set<Graph> valuesInSet = getOrCreateGraphs(plugin);
					final Iterator<Graph> iter = valuesInSet.iterator();

					while (iter.hasNext()) {
						final Graph graph = iter.next();

						for (Plotter plotter : graph.getPlotters()) {
							plotter.reset();
						}
					}
				}
			}
		}
	}

	private boolean isMineshafterPresent() {
		try {
			Class.forName("mineshafter.MineServer");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
		buffer.append('&').append(encode(key)).append('=').append(encode(value));
	}

	private static String encode(final String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}

	public static class Graph{

		private final Type type;
		private final String name;
		private final Set<Metrics.Plotter> plotters = new LinkedHashSet<Metrics.Plotter>();

		private Graph(Type type, String name){
			this.type = type;
			this.name = name;
		}

		public String getName(){
			return this.name;
		}

		public void addPlotter(Metrics.Plotter plotter){
			this.plotters.add(plotter);
		}

		public void removePlotter(Metrics.Plotter plotter){
			this.plotters.remove(plotter);
		}

		public Set<Metrics.Plotter> getPlotters(){
			return Collections.unmodifiableSet(this.plotters);
		}

		public int hashCode(){
			return this.type.hashCode() * 17 ^ this.name.hashCode();
		}

		public boolean equals(Object object){
			if (!(object instanceof Graph)) {
				return false;
			}

			Graph graph = (Graph)object;
			return (graph.type == this.type) && (graph.name.equals(this.name));
		}

		protected void onOptOut(){}

		public static enum Type{
			Line, 

			Area, 

			Column, 

			Pie;
		}

	}

	public static abstract class Plotter {

		private final String name;

		public Plotter() {
			this("Default");
		}

		public Plotter(final String name) {
			this.name = name;
		}

		public abstract int getValue();

		public String getColumnName() {
			return name;
		}

		public void reset() {
		}

		@Override
		public int hashCode() {
			return getColumnName().hashCode();
		}

		@Override
		public boolean equals(final Object object) {
			if (!(object instanceof Plotter)) {
				return false;
			}

			final Plotter plotter = (Plotter) object;
			return plotter.name.equals(name) && plotter.getValue() == getValue();
		}

	}


	public void findCustomData(SunBurn sunburn){
		this.sunburn = sunburn;
		burnGraph = createGraph(sunburn, Metrics.Graph.Type.Line, "Burning");
		wasteGraph = createGraph(sunburn, Metrics.Graph.Type.Line, "Wasteland Worlds");
	}

	public void addCustomData(){
		String burning, waste;

		boolean bp = sunburn.config.bPlayer;
		boolean ba = sunburn.config.bAnimal;

		if(bp&&ba){
			burning = "All On";
		}else if(bp&&(!ba)){
			burning = "Only Players";
		}else if(ba&&(!bp)){
			burning = "Only Mobs";
		}else{
			burning = "All Off";
		}
		
		if(sunburn.config.autoburn){
			waste = sunburn.config.wasteworlds.size()+" Worlds";
		}else{
			waste = "0 Worlds";
		}
		burnGraph.addPlotter(new Plotter(burning){
			public int getValue(){
				return 1;
			}
		});
		wasteGraph.addPlotter(new Plotter(waste){
			public int getValue(){
				return 1;
			}
		});
	}

}