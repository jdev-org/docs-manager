# Développer



## Prérequis

- OS linux (e.g Debian 11)
- Java OpenJDK 17
- mvn
- Un serveur PostgreSQL doit être accessible
- Les droits root / sudo sur la machine

## Installation locale

Pour démarrer le service localement, vous devez suivre la procédure d'installation sur votre environnement Linux (ne pas suivre la section "Georchestra Configuration" si non souhaitée): 

[https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#install](https://github.com/jdev-org/docs-manager/tree/main/docs-manager-back#install)

## Paramétrage en développement

Si vous exécuter le service via la commande `java -jar`, vous pouvez modifier le fichier `resources/application.properties`.

Si vous souhaitez utiliser un fichier `.properties` selon un chemin spécifique, vous pouvez l'ajouter via l'option `--spring.config.location` tel que la commande :

`--spring.config.location=/srv/apps/application.properties`

Vous aurez donc la commande (`file.jar` est à remplacer par le nom du jar) : 

`java -jar target/file.jar --spring.config.location=/srv/apps/application.properties`

## Build

Vous pouvez exécuter le build :

cd docs-manager-back
mvn clean install

## Démarrage du service en mode dev

### Via JAVA

Exécuter cette commande dans un terminal :

`java -jar target/docsmanager-1.0.0-SNAPSHOT.jar`


### Via VSCode

Consultez en premier les documentations associées de l'éditeur :

- [Tutoriel](https://code.visualstudio.com/docs/java/java-tutorial)

- [https://code.visualstudio.com/docs/java/java-debugging](https://code.visualstudio.com/docs/java/java-debugging)


Réalisez ensuite ces étapes :

1. Ouvrir dans VSCode le répertoire contenant le backend
2. Installer les modules `Extension Pack for Java`
3. Ajouter dans docs-manager-back/.vscode le fichier de configuration suivant :

```
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Current File",
            "request": "launch",
            "mainClass": "${file}"
        },
        {
            "type": "java",
            "name": "Application",
            "request": "launch",
            "mainClass": "com.frontbackend.springboot.Application",
            "projectName": "upload-file-to-postgresql"
        }
    ]
}
```

Vous pouvez à présent démarrer le service en mode développement (à condition d'avoir builder le `.jar` auparavant).


