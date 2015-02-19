# Knowledge Media Guide

Systematization and recommendation service on professional *knowledge media*, supposed to be driven by community of experts.

[ ![Codeship Status for alexpetrov/kmg](https://www.codeship.io/projects/580c52e0-ded2-0131-ed85-3ee96e1cc881/status)](https://www.codeship.io/projects/24875)

## Problem description

To be a good specialists we have to know a lot about our profession and it's history.
Otherwise we will reproduce all mistakes and reinvent all wheels and biсycles over and over again.

We have to read, listen, watch a lot, but as I can see, there are not so much people doing it.
And I understand why. It is not so trivial to come up with decision: what to invest first in any particular moment of your career. Especially in the beginning of your path.

From my practice of being trainer of software developers I know that you can read some essential books to accelerate your professional growth.

And I come up with the idea of Knowledge Media Guide.
It is obviously possible to systematize essential *Knowledge Medium* by *Specialization* and recommend it to professionals just in time they need it most.

So the problem is to make platform easy to use by *Experts* and quite *simple* to implement.

## Solution overview

When I come up with this idea my main technologies were JavaEE and Ruby On Rails. But I felt like there is too much accidental complexity even with Rails.
I couldn't start implementing it until I mastered [Clojure](http://clojure.org) and especially [Datomic](http://www.datomic.com/) database.
It impressed me so much and "puzzle was solved".

As we know from Pragmatic Programmer book, plain text is the most powerful and independent format. Therefore, the basic idea is to represent knowledge base as easily editable plain text.
So *Experts* could contribute to *Knowledge Base* by sending *Pull Requests*.
It is possible thanks to representation of entities as *Clojure EDN* maps with meaningful references between them.
These maps are loadable *Datomic entities* (with some tricks).

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

### Run interactive ClojureScript environent

Run [lein-figwheel](https://github.com/bhauman/lein-figwheel) Leiningen plugin in console:

```
lein figwheel
```

Open browser on this page:

```
http://localhost:3449/html/development.html
```

and enjoy hacking by editing kmg/client.cljs file and see immediate result!

### Installation roadmap

Making Docker containers for application and transactor.

## License

Copyright © 2014 Alexander Petrov (a.k.a. Lysenko by passport)

Distributed under the Eclipse Public License version 1.0.

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/alexpetrov/kmg/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
