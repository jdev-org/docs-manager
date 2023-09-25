# Développer

Cette section partage les informations pour développer le module MapStore2.

## Prérequis

- Vous devez installer Node v12.x (ou v14.x) et NPM associé à cette version. Nous conseillons d'utiliser NVM.
- Vous devez disposer du sous module MapStore 2023.01.x dans le template MapStoreExtension

## Comment procéder ?

Pour développer, dirigez-vous vers le dépôt GitHub du template MapStore2 afin d'obtenir les informations nécessaires : 

[Lien vers le dépôt](https://github.com/geosolutions-it/MapStoreExtension)

## Commandes

Pour installer l'extension : 

```
nvm use 12
git clone --recursive https://github.com/jdev-org/docs-manager-front.git
cd docs-manager-front
git submodule update --init
npm install
```

Pour démarrer le serveur local :

```
npm run fe:start
```