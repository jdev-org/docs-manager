# Docs Manager API

This API Allow to SAVE, DELETE, SEARCH a document inside a dedicated table.
This API save document as Blob (Byte).

> This API works with java 17.

## Install

> This install is available only with a sudo or root user.

### Prerequisite

* Install Java 17

API works with Java 17. 

You can control if java 17 is available in your OS package manager :

```
apt-cache search openjdk | grep 17
```


If available, install Java 17 with this command :

```
sudo apt install openjdk-17-jdk
```

> If not available, you have to [install openjdk-17 manually from archive](https://computingforgeeks.com/install-oracle-java-openjdk-on-debian-linux/?expand_article=1).

You can check version with this command if you need to keep initial java version : 

```
java --version
```

If this command return wrong java version, you just have to control available java versions with :

```
update-alternatives --list java
```
...and select a specific OS Java version if needed:

```
sudo update-alternatives --config java
```

> Note : java is available in a path like `/usr/lib/jvm/java-17-openjdk-amd64/bin/java`
  
* Install maven (only for dev)

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
CREATE DATABASE applis WITH OWNER userapp ENCODING 'UTF8' TEMPLATE = template0;;
```

### Configuration


* **Configuration from datadir**

With geOrchestra, we will use a properties file from datadir.

So, create a new `/etc/georchestra/datadir/docs-manager` directory :

```
sudo mkdir /etc/georchestra/datadir/docs-manager
```

Download properties file :

```
cd /etc/georchestra/datadir/docs-manager
curl -O https://raw.githubusercontent.com/jdev-org/docs-manager/main/docs-manager-back/src/main/resources/application.properties
```

In `application.properties` file, adapt this minimal content to connect DB : 

```
spring.datasource.url=jdbc:postgresql://localhost:5432/documents
spring.datasource.username=dbuser
spring.datasource.password=secret
```

* **Set additional roles**

By default, this backend reads classic georchestra roles with  _EDIT (writer) or _READ (reader).

If you need to differents roles as ROLE_ZZZ_ABC (reader) and ROLE_YYY_ABC (writer), you have to use `docs.roles.additionnal` config.

**Example :**

My documents will be saved with id CARTEAUX (ID defined in POST request parameter).

Here my roles :

- one role SV_PWRS_CARTEAUX_CAR to write
- two roles SV_PWRS_CARTEAUX_CVI and SV_PWRS_CARTEAUX_READER to read

I will use this docs.roles.addition value (object) :

```
docs.roles.additionnal = {'CARTEAUX': {'edit': ['SV_PWRS_CARTEAUX_CAR'], 'read': ['SV_PWRS_CARTEAUX_CVI', 'SV_PWRS_CARTEAUX_READER']}}`
```

Logically, writer have readers capabilities (for a given documents app ID).

Now, only SV_PWRS_CARTEAUX_CAR users can edit documents with CARTEAUX id app value.
Now, only SV_PWRS_CARTEAUX_CAR, SV_PWRS_CARTEAUX_CVI and SV_PWRS_CARTEAUX_READER users can access / read documents with CARTEAUX id app value.

* **Port**

By default we use the port `8092`. You can change it in `application.properties` by this config : 

```
server.port=8092
```

* **CORS**

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

Change user and security :

```
chown -R tomcat:tomcat /srv/docsmanager
chmod +x /srv/docsmanager/docsmanager-1.0.0-SNAPSHOT.jar
```

* Create daemone file

Create new file `docsmanager.service` file in `/etc/systemd/system`.

```
sudo nano /etc/systemd/system/docsmanager.service
```

Past this code inside `docsmanager.service` file :

> Control that logs directory already exists before start service !

```
[Unit]
Description=docs-manager backend
After=syslog.target

[Service]
User=tomcat
ExecStart=/usr/lib/jvm/java-17-openjdk-amd64/bin/java -jar /srv/docsmanager/docsmanager-1.0.0-SNAPSHOT.jar --spring.config.location=/etc/georchestra/datadir/docs-manager/application.properties
SuccessExitStatus=143
StandardOutput=append:/etc/georchestra/logs/docsmanager.log
StandardError=append:/etc/georchestra/logs/docsmanager.log

[Install]
WantedBy=multi-user.target
```

> Note : You can change --spring.config.location value to use another properties file

* Enable service

```
systemctl enable docsmanager.service
```

* Reload systemctl (needed if you change service)

```
sudo systemctl daemon-reload
```

* Start `docsmanager` service

```
sudo service docsmanager start
```

## Logging

### Location

By defautl, docs-manager service target `/etc/georchestra/logs` directory.

In /etc/systemd/system/docsmanager.service

```
StandardOutput=append:/etc/georchestra/logs/docsmanager.log
StandardError=append:/etc/georchestra/logs/docsmanager.log
```

### Level

To change logging level, you can change application.properties config :

https://github.com/jdev-org/docs-manager/blob/ab34298a241dd7ec70ae4fecea377517a4dbc323/docs-manager-back/src/main/resources/application.properties#L26-L31

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

* Open `/etc/georchestra/datadir/security-proxy/security-mappings.xml` 

```
sudo nano /etc/georchestra/datadir/security-proxy/security-mappings.xml
```

...and insert :


```
<intercept-url pattern="/docs/.*" access="IS_AUTHENTICATED_FULLY" />
```

* Open `/etc/georchestra/datadir/security-proxy/targets-mapping.properties` 

```
sudo nano /etc/georchestra/datadir/security-proxy/targets-mapping.properties
```

...and insert :

```
docs=http://localhost:8092/files/
```

Now restart security-proxy service:

```
sudo service restart tomcat@proxycas
```

# Developper corner

TODO

* Restart

```
sudo service tomcat@proxycas restart
```
