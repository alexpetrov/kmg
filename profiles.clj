{:dev  {:env {:database-url "datomic:free://localhost:4334/kmg-sample"
              :log-file-path "logs/kmg.log"
              :sample-data-path "test/kmg/sample_data.edn"
              :real-data-path "resources/knowledge_base4it.edn"}}
 :test {:env {:database-url "datomic:mem://test"}}}
