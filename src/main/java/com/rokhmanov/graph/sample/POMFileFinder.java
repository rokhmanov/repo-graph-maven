package com.rokhmanov.graph.sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class POMFileFinder {

	private File repoDir;	

	public POMFileFinder(String folderName) {
		this.repoDir = new File(folderName);
	}
	
	public List<Path> getAllPOMs() {
	    Finder finder = new Finder("*.pom");
	    try {
//			Files.walkFileTree(repoDir.toPath(), finder);
			Files.walkFileTree(repoDir.toPath(), Collections.<FileVisitOption> emptySet(), 20, finder); 
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    List<Path> files = finder.done();
		return files;		
	}
	
	public List<Path> getReleasePOMs(){
		List<Path> allFiles = getAllPOMs();
		return allFiles.stream().filter(r -> r.toString().contains("SNAPSHOT") == false).collect(Collectors.toList());
	}
	
}
