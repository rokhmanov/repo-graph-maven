package com.rokhmanov.graph.sample;

import java.nio.file.Path;
import java.util.List;

public class Neo4JProcessor {

	private static String repoDir = "";
	private static String serverURL = "http://localhost:7474/db/data/";
	private static boolean clearFlag = false;
	
	public static void main(String[] args) {
		switch (args.length) {
		case 0:
			System.out.println("This utility builds the hierarchy of Maven Projects and dependencies in Neo4J Graph database.");
			System.out.println("usage: repo-graph-maven.xxxx.jar [OPTION]...");
			System.out.println("Options:");
			System.out.println("    <directory> - (mandatory parameter) path to the local Maven repository.");
			System.out.println("    <serverURL> - (mandatory parameter) Neo4j REST server root URL.");
			System.out.println("    'clear' - (optional parameter) The existing database will be recreated if specified.");
			System.out.println("Example: java -jar repo-graph-maven.jar ~/.m2/repository http://192.168.0.1:7474/db/data/ clear");			
			System.exit(0);
		case 2: 
			repoDir = args[0];
			serverURL = args[1];
			break;	
		case 3: 
			repoDir = args[0];
			serverURL = args[1];
			if (args[2].equalsIgnoreCase("clear")){
				clearFlag = true;				
			}
			break;	
		default:
			System.err.println("ERROR: Wrong number of arguments.");
			System.exit(1);
		}
		POMFileFinder reader = new POMFileFinder(repoDir);
		GraphBuilder builder = new GraphBuilder(serverURL);
		XMLConverter converter = new XMLConverter();
		if (clearFlag){
			System.out.println("WARNING: The neo4j database will be recreated.");
			builder.prepareDB();			
		}
		List<Path> paths = reader.getReleasePOMs();
		System.out.println("Found repositories:" + paths.size());
		for (Path path : paths) {
			try {
				builder.buildRepoGraph(converter.convertFromXML(path));
				System.out.println("Processed repository:" + path.toString());
			} catch (Exception e){
				System.err.println("ERROR populating graph:" + path.toString() + " Error:" + e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("Done.");
	}

}
