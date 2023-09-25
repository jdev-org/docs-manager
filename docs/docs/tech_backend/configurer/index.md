# Configuration

## Configuration des logs

La configuration des logs est décrite dans la section **logging** du dépôt :

- [Gestion des logs](https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#logging)

Vous pouvez suivre ces instructions pour cette partie du backend.

## Configuration avec geOrchestra

La configuration du backend pour georchestra est accessible dans le dépôt :

- [Intégration dans geOrchestra](https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#georchestra-configuration)

Vous pouvez suivre ces instructions pour cette partie du backend.

## Fichier de configuration

La localisation du fichier **application.properties** est décrit dans cette section du dépôt :

- [https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#configuration](https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#configuration)

Lors de l'installation, il est préférable d'utiliser un fichier externe à renseigner dans le fichier de service Linux (/etc/systemd/system/docsmanager.service) :

- [https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#deploy-jar](https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#deploy-jar)


## Emplacement du fichier de configuration

Vous pouvez modifier le fichier `docsmanager.service` pour préciser le fichier `.properties` à utiliser via l'option `--spring.config.location` en remplaçant par exemple :

`--spring.config.location=/etc/georchestra/datadir/docs-manager/application.properties`

...par :

`--spring.config.location=/srv/docsmanager/application.properties`

**Attention** : vous devez **vérifier les droits** pour que le fichier `application.properties` reste accessible.

### Description des paramètres à administrer

Cette section ne concerne que les paramètres à administrer. Les autres paramètres du fichier sont à laisser en l'état.

- Le fichier de configuration est par défaut visible ici :

[https://github.com/jdev-org/docs-manager/blob/main/docs-manager-back/src/main/resources/application.properties](https://github.com/jdev-org/docs-manager/blob/main/docs-manager-back/src/main/resources/application.properties)

- Accès à la base PostgreSQL :

```
spring.datasource.url=jdbc:postgresql://localhost:5432/documents
spring.datasource.username=dbuser
spring.datasource.password=secret
```

- Roles administrateurs :

```
docs.roles.admin = MAPSTORE_ADMIN,SUPERUSER,ROLE_MAPSTORE_ADMIN,ROLE_SUPERUSER,DOC_ADMIN
```

- Roles additionnels

Ces rôles sont dits additionnels car ils n'ont pas un shéma par défaut propre à la console geOrchestra (ex: SV_PWRS_MON_ROLE_EDIT).

Pour ajouter un rôle additionnel, vous devez indiquer l'ID du plugin concerné (ou ID ou Code du contexte), et indiquer les rôles additionnels concernés selon s'il permet la lecture (read) ou l'écriture (edit).

Par exemple, pour un contexte avec un ID de valeur **CARTEAUX** et les rôles additionnels de lecture et écriture **SV_PWRS_CARTEAUX_READ**, **SV_PWRS_CARTEAUX_EDIT** on aura : 

```
{'CARTEAUX':{'edit':['SV_PWRS_CARTEAUX_EDIT'], 'read':['SV_PWRS_CARTEAUX_READ']}}
```

Vous pouvez donc rajouter d'autres rôle à cette configuration tel que :

```
{'ID_PLUGIN':{'edit':['ROLE_A','ROLE_B'], 'read':['ROLE_X']}, 'CARTEAUX':{'edit':['SV_PWRS_CARTEAUX_EDIT'], 'read':['SV_PWRS_CARTEAUX_READ']}}
```


- Configuration des niveaux logs

```
logging.level.root=info
logging.level.org.springframework.web=info
#logging.level.org.hibernate.SQL=debug
logging.level.logger.org.hibernate.type=trace
logging.level.org.hibernate.internal=error
```