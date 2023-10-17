(ns print-sprint.jira
  (:require [clj-http.client :as client])
  (:import [java.util Base64]))

(defn encode [to-encode]
  (.encodeToString  (Base64/getEncoder) (.getBytes to-encode)))

(defn fetch-velocity [subdomain email api-key board-id]
  (let [url (str "https://" subdomain
                 ".atlassian.net/rest/greenhopper/1.0/rapid/charts/velocity")
        auth (str "Basic " (encode (str email ":" api-key)))
        headers {"Authorization" auth
                 "Content-Type" "application/json"}
        sprints-response (client/get url {:headers headers
                                          :as :json
                                          :throw-entire-message? true
                                          :query-params {:rapidViewId board-id}})]
    (:body sprints-response)))

(defn report [subdomain email api-key board-id size]
  (->> (fetch-velocity subdomain email api-key board-id)
       (:velocityStatEntries)
       (take-last size)
       (map (fn [[sprint-id sprint]]
              {:sprint-id sprint-id
               :estimated (get-in sprint [:estimated :value])
               :completed (get-in sprint [:completed :value])}))))