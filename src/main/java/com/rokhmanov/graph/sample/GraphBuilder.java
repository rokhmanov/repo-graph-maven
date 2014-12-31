package com.rokhmanov.graph.sample;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rokhmanov.graph.sample.entity.Dependency;
import com.rokhmanov.graph.sample.entity.Project;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GraphBuilder {
	
	public enum NodeType {
		ROOT("Root"), PROJECT("Project"), ARTIFACT("Artifact");
		private String value;
		private NodeType(String value){
			this.value = value;
		}
		
		public String toString(){
			return value;
		}
	};
	
	public enum RelationType {
		KEEPS("keeps"), HAS("has");
		private String value;
		private RelationType(String value){
			this.value = value;
		}
		
		public String toString(){
			return this.value;
		}
	}
	
	private String url;
	
	public GraphBuilder(String serverUrl) {
		if (serverUrl.endsWith("/")){
			url = serverUrl;			
		} else {
			url = serverUrl + "/";
		}
	}

	public void prepareDB(){
		if (isServerAvailable()){
			cleanDB();
			Map<String, String> props = new HashMap<String, String>();
			props.put("name", "Repository Storage");
			createNode(NodeType.ROOT, props);
		}
	}

	public void buildRepoGraph(Project repo) throws Exception {
		if (isServerAvailable() && findProject(repo).isEmpty()){
			Map<String, String> rProps = new HashMap<String, String>();			
			rProps.put("version", repo.getVersion());
			rProps.put("artifactId", repo.getArtifactId());
			rProps.put("groupId", repo.getGroupId());
			String repoNode = createNode(NodeType.PROJECT, rProps);
			addRelationship(findRootNode(), repoNode, RelationType.KEEPS);
			if (null != repo.getDepsList()){
				for (Dependency dep : repo.getDepsList()) {
					String depNode = findDependency(dep);
					if (depNode.isEmpty()){				
						Map<String, String> dProps = new HashMap<String, String>();			
						dProps.put("version", dep.getVersion());
						dProps.put("artifactId", dep.getArtifactId());
						dProps.put("groupId", dep.getGroupId());
						depNode = createNode(NodeType.ARTIFACT, dProps);
					}
					addRelationship(repoNode, depNode, RelationType.HAS);
				}				
			}
		}
	}

	
	private void cleanDB(){
		final String txUri = url + "transaction/commit";
		WebResource resource = Client.create().resource(txUri);
		String clearCypherCommand = "MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r";
		String payload = "{\"statements\" : [ {\"statement\" : \"" + clearCypherCommand + "\"} ]}";
		ClientResponse response = resource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(payload)
				.post(ClientResponse.class);
		if(response.getStatus() != ClientResponse.Status.OK.getStatusCode()){
			System.err.println("DB Cleanup failed:" + response.getStatus());
		}
		response.close();
	}
		
	protected String findProject(Project repo) throws Exception {
		StringBuilder searchCommand = new StringBuilder("MATCH (r:");
		searchCommand.append(NodeType.PROJECT).append(" {");
		searchCommand.append("version:'").append(repo.getVersion()).append("',");
		searchCommand.append("artifactId:'").append(repo.getArtifactId()).append("',");
		searchCommand.append("groupId:'").append(repo.getGroupId()).append("'");
		searchCommand.append("}) RETURN ID(r) LIMIT 1");		
		return getResult(searchCommand);
	}
	
	protected String findDependency(Dependency a) throws Exception {
		StringBuilder searchCypherCommand = new StringBuilder("MATCH (atf:");
		searchCypherCommand.append(NodeType.ARTIFACT).append(" {");
		searchCypherCommand.append("artifactId:'").append(a.getArtifactId()).append("',");
		searchCypherCommand.append("groupId:'").append(a.getGroupId()).append("',");
		searchCypherCommand.append("version:'").append(a.getVersion()).append("'");
		searchCypherCommand.append("}) RETURN ID(atf)");
		return getResult(searchCypherCommand);		
	}
	
	
	private String findRootNode() throws Exception{
		StringBuilder searchCypherCommand = new StringBuilder("MATCH (n:");
		searchCypherCommand.append(NodeType.ROOT);
		searchCypherCommand.append(") RETURN ID(n)");
		return getResult(searchCypherCommand);		
	}
	
	
	private String getResult(StringBuilder command) throws Exception {
		final String txUri = url + "transaction/commit";
		WebResource resource = Client.create().resource(txUri);
		String payload = "{\"statements\" : [ {\"statement\" : \"" + command + "\"} ]}";
		ClientResponse response = resource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(payload)
				.post(ClientResponse.class);
		String searchResult = response.getEntity(String.class);
		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
			throw new Exception("Bad response from REST:" + response);
		}
		response.close();
		return extractNodeId(searchResult);
	}
	
	private String extractNodeId(String payload){
		String i = "";
		JsonElement el = new JsonParser().parse(payload);
		JsonObject jo = el.getAsJsonObject();
		JsonArray ja =  jo.getAsJsonArray("results");
		JsonArray data = ja.get(0).getAsJsonObject().get("data").getAsJsonArray();
		if (data.size() != 0){
			i = data.get(0).getAsJsonObject().get("row").getAsJsonArray().getAsString();			
		}		
		return i;
	}
	
	private boolean isServerAvailable(){
		WebResource resource = Client.create().resource(url);
		ClientResponse response = resource.get( ClientResponse.class );
		int status = response.getStatus();
		response.close();
		if(status == ClientResponse.Status.OK.getStatusCode()){
			return true;
		}
		return false;
	}

	
    private String createNode(NodeType type, Map<String, String> params)
    {
		final String txUri = url + "transaction/commit";
		WebResource resource = Client.create().resource(txUri);
		StringBuilder cypher = new StringBuilder("create (n:").append(type).append(" {");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			cypher.append(entry.getKey()).append(":'").append(entry.getValue()).append("',");			
		}
		String cypherString = cypher.substring(0, cypher.length() - 1);
		cypherString += ( "}) return id(n)");
		
		String payload = "{\"statements\" : [ {\"statement\" : \"" + cypherString + "\"} ]}";
		ClientResponse response = resource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(payload)
				.post(ClientResponse.class);
		
		String entity = response.getEntity( String.class );
		response.close();		
        return extractNodeId(entity);
    }

      
    private void addRelationship( String startNode, String endNode, RelationType relation) {
		final String txUri = url + "transaction/commit";
		WebResource resource = Client.create().resource(txUri);
		StringBuilder cypher = new StringBuilder("MATCH (a),(b)");
		cypher.append(" WHERE ID(a)=").append(startNode).append(" AND ID(b)=").append(endNode);
		cypher.append(" CREATE (a)-[r:").append(relation).append("]->(b)");
		
		String payload = "{\"statements\" : [ {\"statement\" : \"" + cypher + "\"} ]}";
		ClientResponse response = resource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(payload)
				.post(ClientResponse.class);
		response.close();
    }

}
