# Docs Manager API

This API Allow to SAVE, DELETE, SEARCH a document inside a dedicated table.
This API save document as Blob (Byte).

## Install

> This install is available only with a sudo or root user.

### Prerequisite

* Install Java 17

API works with Java 17. Install Java 17 with this command :

```
sudo apt install openjdk-17-jdk
```

You can check version with : 

```
java --version
```

If you need to select a specific OS Java version, change the version with :

```
sudo update-alternatives --config java
```

> Note : java is available in a path like `/usr/lib/jvm/java-17-openjdk-amd64/bin/java`
  
* Install maven

```
sudo apt install maven
```

### Database

Spring-boot will create needed table automatically. You juste have to create a database if needed or reuse one.
We advice to use a dedicated user to limit user action in your SQL spaces.

Here, we will create a dedicated database with a new user.

* Create user `userapp` with a custom password (change it)

```
CREATE USER userapp WITH ENCRYPTED PASSWORD 'password@tochange'
```

* Create database with name `applis` (here we select UTF8 encoding that you can change or remove if needed)

```
CREATE DATABASE applis WITH OWNER userapp ENCODING UTF8;
```

### Configuration


* Configuration from datadir

With geOrchestra, we will use a properties file from datadir.

So, create a new `/etc/georchestra/docs-manager` directory :

```
sudo mkdir /etc/georchestra/docs-manager
```

Copy `application.properties` file inside and adapt this minimal content : 

```
spring.datasource.url=jdbc:postgresql://localhost:5432/documents
spring.datasource.username=dbuser
spring.datasource.password=secret
```

* Port

By default we use the port `8081`. You can change it in `application.properties` by this config : 

```
server.port=8081
```

* CORS

By default, CORS allow all origins and all URI parttern in [this code](https://github.com/jdev-org/docs-manager/blob/main/docs-manager-back/src/main/java/org/georchestra/docsmanager/config/WebConfig.java).

To change this configuration, uncomment this line in `application.properties` file and adapt it : 

```
# spring.cors.origins=http://localhost:8082
# spring.cors.pattern=/**
```


### Deploy JAR

We will prepare here file system en deploy app `.jar` file.

* File system

Create a new `/srv/docsmanager` directory and download `.jar` file in this new directory.

* Create daemone file

Creane new file `docsmanager.service` file in `/etc/systemd/system`.

```
sudo nano /etc/systemd/system/docsmanager.service
```

Past this code inside `docsmanager.service` file :

```
[Unit]
Description=docs-manager backend
After=syslog.target

[Service]
User=www-data
ExecStart=/usr/lib/jvm/java-17-openjdk-amd64/bin/java -jar /srv/docsmanager/docsmanager-0.0.1-SNAPSHOT.jar --spring.config.location=/etc/georchestra/docs-manager/application.properties --debug
SuccessExitStatus=143
StandardOutput=append:/srv/log/docsmanager.log
StandardError=append:/srv/log/docsmanager.log

[Install]
WantedBy=multi-user.target
```

> Note : You can change --spring.config.location value to use another properties file

* Reload systemctl

```
sudo systemctl daemon-reload
```

* Start `docsmanager` service

```
sudo service docsmanager start
```

## GeOrchestra configuration

**With this configuration, API will be available with https://fqdn.fr/docs/**

### Web configuration

Create `/var/www/georchestra/conf/docsmanager.conf` file 

```
sudo nano /var/www/georchestra/conf/docsmanager.conf
```

...and past this config :


```
<Proxy http://localhost:8180/docs*>
    Require all granted
</Proxy>
ProxyPass /docs http://localhost:8180/docs
ProxyPassReverse /docs http://localhost:8180/docs
```

Restart apache : 

```
sudo service apache2 restart
sudo service apache2 reload
```

### Security Proxy configuration

* Open `/etc/georchestra/security-proxy/security-mappings.xml` 

```
sudo nano /etc/georchestra/security-proxy/security-mappings.xml
```

...and insert :


```
<intercept-url pattern="/docs/.*" access="IS_AUTHENTICATED_FULLY" />
```

* Open `/etc/georchestra/security-proxy/targets-mapping.properties` 

```
sudo nano /etc/georchestra/security-proxy/targets-mapping.properties
```

...and insert :

```
docs=http://localhost:8081/files/
```

* Restart

```
sudo service tomcat@proxycas restart
```