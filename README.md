# staylocatedapi

This is a draft/dev repo submission, hence the nonsensical commits.  When the core functionality and core security implementations are complete, this project will be trashed and the code base will be recommitted as a fresh repo with an initial commit.  All other commits afterward will be performed via submitted branches (fix/etc, feature/etc, refactor/etc) with subsequent pull requests into master (or dev).

### Installation
Grab from GitHub > Use Eclipse > Import existing Maven Project.  You may need to configure project facets.  Will need to test with a fresh client.  Would also like to consider moving this to IntelliJ with gradle support.  

### SSL
Note that this app is, for the moment, hosted on Heroku.  As such, I've acquiesced to using their Tomcat-wrapper, webapp-runner [https://github.com/jsimone/webapp-runner/tree/tomcat7].  I would have liked to use JBoss AS 7.1 here for the experience, but considering I can host any JBoss application locally with relative ease, this wasn't much of a sacrifice.  Anyway, the bigger implication here is that Heroku offers piggyback SSL by default [https://blog.heroku.com/archives/2012/5/3/announcing_better_ssl_for_your_app] which means no manual SSL configuration was done here, save for a somewhat proprietary approach to redirecting HTTP to HTTPS due to Heroku's web/routing set up [http://stackoverflow.com/a/13649976].

Quote:
`The issue is that Heroku's web/routing tier handles the SSL, and proxies the request to your webapp as plain HTTP. So Tomcat doesn't know the request is secured elsewhere (SSL offloading) RemoteIpValve essentially over-rides the protocol, port and IP address that Tomcat sees (and, in turn, how request.isSecure() is evaluated) by looking at HTTP headers set by the proxying server that handled the SSL.`

### Research, notes, and references for later

Heroku Idiosyncrasies
 (Heroku gives examples using dbcp.BasicDataSource whereas I simply use jdbc.datasource.DriverManagerDataSource)
 https://devcenter.heroku.com/articles/heroku-postgresql#spring-xml
 https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-database_url-in-spring-with-xml-configuration

Spring Security OAuth2 sparklr/tonr Demo projects (Official)<br>
 https://github.com/spring-projects/spring-security-oauth/tree/master/samples/oauth2

Spring Security OAuth2 Demo project<br>
 https://github.com/javajack/SpringOAuth2.0Demo

spring-security-oauth2-2.0.7.RELEASE and its required (and optional) dependencies<br>
 http://mvnrepository.com/artifact/org.springframework.security.oauth/spring-security-oauth2/2.0.7.RELEASE
 
spring-security-oauth2-1.0.5.RELEASE and its required (and optional) dependencies<br>
 http://mvnrepository.com/artifact/org.springframework.security.oauth/spring-security-oauth2/1.0.5.RELEASE
 
Spring Security 3.2.8 RELEASE FAQ<br>
 http://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/faq.html
 
API Endpoint HTTP testing web-tool<br>
 https://www.hurl.it/
 
Spring Security Namespace link to review later:<br>
 http://docs.spring.io/spring-security/site/docs/3.1.x/reference/ns-config.html
 
Maven links to review later:<br>
 http://maven.apache.org/settings.html
 
REST links to review later:<br>
 http://stackoverflow.com/questions/6068113/do-sessions-really-violate-restfulness <br>
 http://stackoverflow.com/questions/544474/can-you-help-me-understand-this-common-rest-mistakes-sessions-are-irrelevant
 
Android links to review later:<br>
 http://stackoverflow.com/questions/18072945/android-http-post-to-rest-api <br>
 http://stackoverflow.com/questions/14268029/to-call-rest-post-webservice-from-android

Google Developers<br>
 https://console.developers.google.com/project/staylocated/apiui/credential (API key) <br>
 https://developers.google.com/maps/licensing (Restrictions and constraints to using their Maps API)
 
OpenStreetMap (alternative to Google Maps)<br>
 https://www.openstreetmap.org/ <br>
 http://wiki.openstreetmap.org/wiki/API
 
LeanKit Kanban board (only other project members and myself can view this)<br>
 https://devcru.leankit.com/Boards/View/221586782#workflow-view
 
### Classpaths

Eclipse, Maven, and Heroku all seem to have different default classpaths.<br>
- Eclipse: /bin (binary build path, though this appears to be overridden in this project in favor of Maven's own [declared outside of Eclipse via mvn eclipse:eclipse])<br>
- Maven: /target/classes<br>
- Heroku/webapp-runner: / (project root)

This is important to know due to various dependencies and, as discovered, inclusion of properties files in the project

### postgres on Heroku
 postgreSQL version on Herku is 9.4
 Note that a default installation of postgres does NOT come equipped with the features necessary to produce UUIDs (`uuid_generate_v4()`)
 You will need to perform the following steps:
 - Ensure the staylocatedapi app has been pushed and deployed to Heroku
 - Heroku Toolbelt is installed (giving you access to the `heroku` command in command prompt)
 - MS Command Prompt (in project directory):
  - First ensure the database is up and running with `heroku pg:info`
  - Then enter postgres SQL mode with `heroku pg:psql`
  - `CREATE EXTENSION "uuid-ossp";`
 - Create your tables as needed