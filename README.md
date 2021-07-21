# Bot: Super Dice Roll

Discord ~~and Telegram~~ bot that roll dices using using commands like `/roll 4d6+4`.

[![discord-super-dice-roll](https://img.shields.io/badge/Discord-Add%20To%20Your%20Server-blueviolet?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/api/oauth2/authorize?client_id=861964097700757534&permissions=2148005952&scope=bot%20applications.commands)

## Usage
- `/help`    Show available commands and how to use them.
- `/roll`    Roll dices. Example: `/roll 3D6+3`.
- `/history` Lists your lasts 10 rolls with the results.

# Developers

## About this code
 - **parenthesin**: Helpers and wrappers to give a foundation to create new services in clojure,
you can find components for database, http, webserver and tools for db migrations.
 - **super-dice-roll**: Bot code for interaction via webhooks and rolling dices.

## Available Endpoints

Verb | URL                | Description
-----| ------------------ | ------------------------------------------------
POST | /discord/webhook   | Receives Discord's [interaction object](https://discord.com/developers/docs/interactions/slash-commands#interaction-object)

## Configure the config.edn 
Set the [`resources/config.edn`](https://github.com/rafaeldelboni/super-dice-roll-clj/blob/main/resources/config.edn) with your keys or the corresponding enviroment variables.  

## Docker
Start containers with postgres `user: postgres, password: postgres, hostname: db, port: 5432`  
and [pg-admin](http://localhost:5433) `email: pg@pg.cc, password: pg, port: 5433`
```bash
docker-compose -f docker/docker-compose.yml up -d
```
Stop containers
```bash
docker-compose -f docker/docker-compose.yml stop
```

## Running the server
First you need to have the database running, for this you can use the docker command in the step above.

### Repl
You can start a repl open and evaluate the file `src/super_dice_roll/server.clj` and execute following code:
```clojure
(start-system! (build-system-map))
```

### Uberjar
You can generate an uberjar and execute it via java in the terminal:
```bash
# genarate a service.jar in the root of this repository.
clj -X:uberjar
# execute it via java
java -jar service.jar
```

## Repl
To open a nrepl
```bash
clj -M:nrepl
```
To open a nrepl with all test extra-deps on it
```bash
clj -M:test:nrepl
```

## Run Tests
To run unit tests inside `./test/unit`
```bash
clj -M:test :unit
```
To run integration tests inside `./test/integration`
```bash
clj -M:test :integration
```
To run all tests inside `./test`
```bash
clj -M:test
```
To generate a coverage report 
```bash
clj -M:test --plugin kaocha.plugin/cloverage
```

## Lint
Auto code format
```bash
clj -M:lint-fix
```
Runs kondo to lint src/test files
```bash
clj -M:lint
```

## Migrations
To create a new migration with a name
```bash
clj -M:migratus create migration-name
```
To execute all pending migrations
```bash
clj -M:migratus migration
```
To rollback the latest migration
```bash
clj -M:migratus rollback
```
See [Migratus Usage](https://github.com/yogthos/migratus#usage) for documentation on each command.

## Directory Structure
```
./
├── .clj-kondo -- clj-kondo configuration and classes
├── .github
│   └── workflows -- Github workflows folder.
├── docker -- docker and docker-compose files for the database
├── resources -- Application resources assets folder and configuration files
│   └── migrations -- Current database schemas, synced on service startup.
├── src -- Library source code and headers.
│   ├── parenthesin -- Source for common utilities and helpers.
│   └── super_dice_roll -- Source for the bot.
└── test -- Test source code.
    ├── integration -- Integration tests source (uses state-flow).
    │   ├── parenthesin -- Tests for common utilities and helpers.
    │   └── super_dice_roll -- Tests for bot.
    └── unit -- Unity tests source (uses clojure.test).
        ├── parenthesin -- Tests for common utilities.
        └── super_dice_roll -- Tests for bot.
```

## License
This is free and unencumbered software released into the public domain.  
For more information, please refer to <http://unlicense.org>
