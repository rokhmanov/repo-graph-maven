package com.rokhmanov.graph.sample;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Ignore;
import org.junit.Test;

import com.rokhmanov.graph.sample.entity.Dependency;
import com.rokhmanov.graph.sample.entity.Project;

@Ignore
public class AuditParsingTests {
	
	@Test
	public void getAllPoms() throws Exception {
		POMFileFinder fileReader = new POMFileFinder("src/test/resources/repository/");
		List<Path> allPoms = fileReader.getAllPOMs();
		assertTrue(allPoms.size() == 3);
	}

	@Test
	public void testRetrieveDependencies() throws Exception {
		String xml1 = "<project>"
				+ "<parent>"
				+ "	<artifactId>parentArtifactName</artifactId><groupId>parentGroupName</groupId><version>parentVersionNumber</version>"
				+ "</parent>"
				+ "<artifactId>artifactName</artifactId>"
				+ "<dependencies>"
				+ "	<dependency><groupId>dep1Group</groupId><artifactId>dep1Artifact</artifactId><version>dep1Version</version><type>jar</type><scope>compile</scope></dependency>"
				+ "	<dependency><groupId>dep2Group</groupId><artifactId>dep2Artifact</artifactId><version>dep2Version</version></dependency>"
				+ "</dependencies>"
				+ "<profile>"
				+ "<dependencies>"
				+ "	<dependency><groupId>WrongGroup</groupId><artifactId>WrongArtifact</artifactId><version>WrongVersion</version></dependency>"
				+ "</dependencies>"
				+ "</profile>"
				+ "</project>"; 
	    JAXBContext context = JAXBContext.newInstance(Project.class);
		Unmarshaller um = context.createUnmarshaller();
		Project newP1 = (Project)um.unmarshal(new StringReader(xml1));
		ArrayList<Dependency> newDeps = newP1.getDepsList();
		assertTrue(newDeps.size() == 2);
		assertTrue(newP1.getVersion() == null);
	}
		
	
	@Test
	public void testXMLConverter() throws Exception {
		XMLConverter converter = new XMLConverter();
		List<Path> paths = new POMFileFinder("C:/Users/rokan01/git/repo-graph-maven/src/test/resources/repository/").getAllPOMs();
		for (Path path : paths) {
			System.out.println(converter.convertFromXML(path).getArtifactId());
		}
		assertTrue(paths.size() == 3);
	}

	
	@Test
	public void testNeo4j() throws Exception{
		long startTime = System.currentTimeMillis();
		GraphBuilder builder = new GraphBuilder("http://localhost:7474/db/data");
		POMFileFinder reader = new POMFileFinder("C:/Users/rokan01/git/repo-graph-maven/src/test/resources/repository/");
		XMLConverter converter = new XMLConverter();
		builder.prepareDB();
		List<Path> paths = reader.getAllPOMs();
		for (Path path : paths) {
			builder.buildRepoGraph(converter.convertFromXML(path));
			System.out.println("Processed repository:" + path.toString());
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Done in: " + (endTime - startTime)/1000 + "s.");
	}
	
}
