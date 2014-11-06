# Knowledge Media Guide

Systematization and recommendation service on professional knowledge media

[ ![Codeship Status for alexpetrov/kmg](https://www.codeship.io/projects/580c52e0-ded2-0131-ed85-3ee96e1cc881/status)](https://www.codeship.io/projects/24875)

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
lein ring server
```

### Run web application with Knowledge Base for IT Domain

To check current state of Knowledge Base for IT Domain:

```
./run-transactor.sh
./import-knowledge-base-4-it.sh
lein ring server
```

## License

Copyright © 2014 Alexander Petrov (a.k.a. Lysenko by passport)

Distributed under the Eclipse Public License version 1.0.
