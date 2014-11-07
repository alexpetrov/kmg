# Knowledge Media Guide

Systematization and recommendation service on professional knowledge media

[ ![Codeship Status for alexpetrov/kmg](https://www.codeship.io/projects/580c52e0-ded2-0131-ed85-3ee96e1cc881/status)](https://www.codeship.io/projects/24875)

This is a prototype of Knowledge Media systematization and recommendation service,
supposed to be driven by community of experts.
The basic idea is to represent knowledge base as easily editable and plain text.
It is possible thanks to representation of entities as clojure EDN maps with meaningful references.
These maps are loadable Datomic entities, but with some tricks.

Example of how knowledge base will look like is [here](https://github.com/alexpetrov/kmg/blob/domain-layer/resources/knowledge_base4it.edn).

Here is the [data model](https://github.com/alexpetrov/kmg/blob/domain-layer/kmg-schema.png).

## Installation


### Run tests

To be able to run tests do the following

```
./install-datomic.sh
lein test
```

### Run web application with sample data

Sample data shows main features of application.
To run web application do:

```
./run-transactor.sh
./import-sample-data.sh
lein cljsbuild once
lein ring server
```

### Run web application with Knowledge Base for IT Domain

To check current state of Knowledge Base for IT Domain:

```
./run-transactor.sh
./import-knowledge-base-4-it.sh
lein cljsbuild once
lein ring server
```

### Installation roadmap

Making Docker containers for application and transactor.

## License

Copyright © 2014 Alexander Petrov (a.k.a. Lysenko by passport)

Distributed under the Eclipse Public License version 1.0.
