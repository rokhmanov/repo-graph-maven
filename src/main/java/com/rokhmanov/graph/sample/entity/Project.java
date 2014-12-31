package com.rokhmanov.graph.sample.entity;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class Project {

  @XmlElementWrapper(name = "dependencies")
  @XmlElement(name = "dependency")
  private ArrayList<Dependency> depList;
  private String artifactId;
  private String groupId;
  private String version;

  public void setDepList(ArrayList<Dependency> depList) {
    this.depList = depList;
  }

  public ArrayList<Dependency> getDepsList() {
    return depList;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
	this.version = version;
  }
} 
