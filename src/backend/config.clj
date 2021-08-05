(ns backend.config)

(def ^:private resource-path (System/getenv "RESOURCE_PATH"))

(defn get-resource [relative-path]
  (str resource-path relative-path))
