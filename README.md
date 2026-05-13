# Neo-4-Flix

## pour lancer le projet la première fois :

sur windos ouvrir docker desktop

```bash
sudo docker compose up --build
```

## pour lancer le projet :

sur windos ouvrir docker desktop

```bash
docker compose up
```

## pour arrêtez le projet :

```bash
docker compose down
```

## liste des port Utiliser

|Service | Port |Description|
|:---:|:---:|:---:|
| Frontend | 4200 | Angular |
| Neo4j | 7474 | Cypher DB|
|Jenkins|8080| jenkins|
|User-service|8081| java springboot|
|Movie-service|8082| java springboot|
|Rating-service|8083| java springboot|

