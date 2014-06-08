(ns kmg.datomic
  (:require
    [datomic.api :as d]
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

#_(def db-url "datomic:free://localhost:4334/kmg")

#_(def conn (d/connect db-url))
#_(defn db [] (d/db conn))

(def schema [

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Media type

  {:db/ident :media/id
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/unique :db.unique/identity
   :db/doc "Media unique identifier."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/type
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Type of media. Reference to Type Entity."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/title
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Title of media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/url
   :db/valueType :db.type/uri
   :db/cardinality :db.cardinality/one
   :db/doc "URL of media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/author
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/many
   :db/doc "Media authors. Reference to Author entity."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/annotation
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Annotation for media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/prerequisite
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/many
   :db/doc "Prerequisite media. Media that is required, to be able to understand this one."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/experience
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one
   :db/doc "Experience. Minimum years of experience to understand this media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/essential
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one
   :db/doc "Is this media essential. This means, that you will have no recommendations until you complete this media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/locale
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Locale of media. English :en by default."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/localization
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Localization of media. This means, that this media is localization of another media. So it should be recommended to users with :media/locale of this one as preferred locale in profile."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/stats
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one
   :db/noHistory true
   :db/doc "Stats for media. How many times this media was completed by all users. Therefore this is stats there is no need to save history for it."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; End of Media type declaration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Author type

  {:db/ident :author/id
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/unique :db.unique/identity
   :db/doc "Author unique identifier"
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :author/name
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Author name"
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :author/user
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Author user in knowledge-media-guide, if exists"
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; End of Author type declaration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Type of knowledge-media type




])

#_(defn reset []
  (d/release conn)
  (d/delete-database db-url)
  (d/create-database db-url)
  (alter-var-root #'conn (constantly (d/connect db-url)))
  @(d/transact conn schema))
