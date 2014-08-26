(ns kmg.schema
  (:require
    [datomic.api :as d]))

(def kmg-schema [

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Domain type
  {:db/ident :domain/id
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Id of domain."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}
  {:db/ident :domain/title
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Title of domain."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}
  {:db/ident :domain/description
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Description of domain."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}
  {:db/ident :domain/default-locale
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "Default locale for domain. For example, default locale for IT is English."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

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
   :db/doc "Type of media. Reference to media.type enum."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/title
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Title of media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/url
   :db/valueType :db.type/string
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
   ;; :db/fulltext true ;; TODO add when really necessary for some feature
   :db/doc "Annotation for media."
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
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "Locale of media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/stats
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one
   :db/noHistory true
   :db/doc "Stats for media. How many times this media was completed by all users. Therefore this is stats there is no need to save history for it."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media/isbn
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "ISBN for book."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; End of Media type declaration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Media Relationship type
  {:db/ident :media.relationship/from
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "'From' end of relationship between media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media.relationship/to
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "'To' end of relationship between media."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media.relationship/type
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "Type of relationship. For example: :localization, :background."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :media.relationship/description
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Description of relationship. For example, why this media is background for that one."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; End of Media Relationship type declaration
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
;; Enum for knowledge-media type

  [:db/add (d/tempid :db.part/user) :db/ident :media.type/book]
  [:db/add (d/tempid :db.part/user) :db/ident :media.type/article]
  [:db/add (d/tempid :db.part/user) :db/ident :media.type/video]
  [:db/add (d/tempid :db.part/user) :db/ident :media.type/podcast]
  [:db/add (d/tempid :db.part/user) :db/ident :media.type/course]
  [:db/add (d/tempid :db.part/user) :db/ident :media.type/blog]


;; End of knowledge-media type enum
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specialization type
  {:db/ident :specialization/id
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/unique :db.unique/identity
   :db/doc "Specialization unique identifier"
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :specialization/title
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Specialization title"
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :specialization/annotation
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   ;; :db/fulltext true ;; add when it will be necessary
   :db/doc "Specialization annotation in Markdown."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; End of Specialization type
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specialization Relationship type
  {:db/ident :specialization.relationship/from
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "'From' end of Relationship between specialization."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :specialization.relationship/to
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "'To' end of Relationship between specialization."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :specialization.relationship/description
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Description of Relationship between specializations."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Recommendation type
  {:db/ident :recommendation/specialization
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Specializations of this Recommendation."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :recommendation/media
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Media of this Recommendation."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :recommendation/id
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/unique :db.unique/identity
   :db/doc "Recommendation id, supposed to be concatenation of specialization id and media id. Needs to reference from user feedback."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :recommendation/priority
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one
   :db/index true
   :db/doc "Priority of this Recommendation."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :recommendation/necessary
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one
   :db/index true
   :db/doc "Is this Recommendation really Necessary."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :recommendation/description
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   ;;   :db/fulltext true ;; TODO: Add full text when it will be necessary for some feature
   :db/doc "Recommendation description."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; End of Recommendation type
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; User type
  {:db/ident :user/name
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/unique :db.unique/identity
   :db/doc "User name. Login, in other words."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :user/start-career-year
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one
   :db/doc "Year of start career in this profession. Needs to calculate current experience."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :user/goal
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Goal is current user specialization in progress. From history this field will be formed list off all specializations of user."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :user/locale
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/doc "Preferred locale. If there is translation of some recommended media in :user/locale, this media will be shown to user. If some not required recommended media is not in :user/locale, it will not be shown to user. Default domain language will be shown always. For IT domain default domain language is English, for example."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

;; TODO Add fields for authentication

;; End of User type
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Feedback type
  {:db/ident :feedback/user
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Feedback's User."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :feedback/recommendation
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/doc "Recommendation for which this Feedback is for."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :feedback/irrelevant
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one
   :db/doc "Is this recommendation irrelevant."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :feedback/incomprehensible
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one
   :db/doc "Is this recommendation incomprehensible."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :feedback.comment/text
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/doc "Feedback comment in markdown."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :feedback.comment/show
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one
   :db/doc "Is comment allowed to show after premoderation."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

  {:db/ident :feedback/complete
   :db/valueType :db.type/boolean
   :db/noHistory true
   :db/cardinality :db.cardinality/one
   :db/doc "Is recommendation completed."
   :db/id (d/tempid :db.part/db)
   :db.install/_attribute :db.part/db}

])
