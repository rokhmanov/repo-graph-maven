repo-graph-maven
================

A command-line utility which can store Local Maven Repository structure in Neo4J Graph Database.

usage: repo-graph-maven.xxxx.jar [OPTION]...
Options:
    <directory> - (mandatory parameter) path to the local Maven repository.
    <serverURL> - (mandatory parameter) Neo4j REST server root URL.
    'clear' - (optional parameter) The existing database will be recreated if specified.
    
Example: java -jar repo-graph-maven.jar ~/.m2/repository http://192.168.0.1:7474/db/data/ clear

More details about usage and implementation:
http://rokhmanov.blogspot.com/2014/12/represent-your-local-maven-repository.html
