(ns print-sprint.core
  (:require [print-sprint.sagehr :as sagehr]
            [print-sprint.jira :as jira]
            [clojure.string :as string]
            [clojure.pprint :refer [print-table]]))

;; clojure -M -m print-sprint.core --from 2023-09-26 --to 2023-10-10 --sprints 5 --ids 1198812,2061438,2164546,805320,2887309,2407647 --board-id 85

;; Sage HR configuration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def sagehr-subdomain (delay (System/getenv "SAGEHR_SUBDOMAIN")))
(def sagehr-api-key (delay (System/getenv "SAGEHR_API_KEY")))


;; Jira configuration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def jira-api-key (delay (System/getenv "JIRA_API_KEY")))
(def email (delay (System/getenv "JIRA_USER_EMAIL")))
(def jira-subdomain (delay (System/getenv "JIRA_SUBDOMAIN")))

(defn calculate-story-points [sagehr-report jira-report]
  (let [business-days-per-sprint 10
        total-estimated (reduce + (map :estimated jira-report))
        total-completed (reduce + (map :completed jira-report))
        total-sprints (count jira-report)
        avg-estimated (/ total-estimated total-sprints)
        avg-completed (/ total-completed total-sprints)

        total-vacation-days (reduce + (map :days sagehr-report))
        employees-count (count sagehr-report)

        total-working-days-next-sprint (* business-days-per-sprint employees-count)
        vacation-deduction-percent (/ total-vacation-days total-working-days-next-sprint)]

    {:suggested-estimation (int (Math/ceil (- avg-estimated (* avg-estimated vacation-deduction-percent))))
     :avg-completed (int (Math/ceil (- avg-completed (* avg-completed vacation-deduction-percent))))}))

(defn -main [& args]
  (let [parsed-args (apply hash-map args)
        from (parsed-args "--from")
        to (parsed-args "--to")
        sprints (Integer/parseInt (parsed-args "--sprints"))
        employee-ids-args (parsed-args "--ids")
        board-id (Integer/parseInt (parsed-args "--board-id"))
        employee-ids (set (map #(Integer/parseInt %) (string/split employee-ids-args #",")))
        sagehr-report (sagehr/report employee-ids @sagehr-api-key @sagehr-subdomain to from)
        jira-report (jira/report @jira-subdomain @email @jira-api-key board-id sprints)
        ]
    (println (str "Period: " from " - " to))
    (print "SageHR Report")
    (print-table sagehr-report)
    (println)
    (print "Jira Report")
    (print-table jira-report)
    (println (str "Suggested story points for next sprint: " 
                  (:suggested-estimation (calculate-story-points sagehr-report jira-report))))))
