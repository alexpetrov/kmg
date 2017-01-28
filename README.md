# Knowledge Media Guide

Systematization and recommendation service about professional *Knowledge Media*, supposed to be driven by community of experts.

[![Vexor status](https://ci.vexor.io/projects/e69891ed-74f3-47b8-acd1-06c2da0612cd/status.svg)](https://ci.vexor.io/ui/projects/e69891ed-74f3-47b8-acd1-06c2da0612cd/builds)

## Problem

To be a good specialists we have to know a lot about our profession and it's history.
Otherwise we will reproduce all mistakes and reinvent all the wheels over and over again.

We have to read, listen, watch, but as far as I can see, there are not so much people doing it.
And I understand why. It is not so trivial to come up with decision: what to invest first in any particular moment of your career. Especially in the beginning of your path.

From my practice of being a software developers trainer I know that you can read some essential books to accelerate your professional growth significantly.

And around the middle of 2013 I came up with the idea of *Knowledge Media Guide* application.
It is obviously possible to systematize essential *Knowledge Medium* by *Specialization* and recommend it to professionals just in time they need it most.

So I wanted to make a platform easy to use by *Experts* and quite *simple* to implement.

## Solution

When I came up with this idea in 2013 my main technologies were JavaEE and Ruby On Rails. But I felt like there is too much accidental complexity there even with Rails.
I couldn't start implementing application until I mastered [Clojure](http://clojure.org) and especially [Datomic](http://www.datomic.com/) database in 2014.
It impressed me so much and architectural puzzle was solved.

As we know from Pragmatic Programmer book, plain text is the most powerful format. Therefore, the basic idea is to represent knowledge base as an easily editable plain text.
So *Experts* could contribute to *Knowledge Base* by sending *Pull Requests* editing plain text files.
It is possible thanks to elegant text representation of domain entities as *Clojure EDN* maps with meaningful references between them.
These maps are loadable *Datomic entities*.

Example of how knowledge base will look like is [here](https://github.com/alexpetrov/kmg/blob/master/resources/knowledge_base4it.edn).

Here is the [data model](https://github.com/alexpetrov/kmg/blob/master/kmg-schema.png).

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

### Installation roadmap

Making Docker containers for application and transactor.

## License

Copyright © 2017 Alexander Petrov (a.k.a. Lysenko by passport)

Distributed under the Eclipse Public License version 1.0.
