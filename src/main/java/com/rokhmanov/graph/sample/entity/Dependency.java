package com.rokhmanov.graph.sample.entity;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class Dependency {

  private String artifactId;
  private String groupId;
  private String version;

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