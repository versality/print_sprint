(ns print-sprint.sagehr
  (:require [clj-http.client :as client])
  (:import [java.time LocalDate]
           [java.time.temporal ChronoUnit]
           [java.time DayOfWeek]))

(defn working-days-between [start-date end-date]
  (let [start (LocalDate/parse start-date)
        end (LocalDate/parse end-date)]
    (->> (range 0 (inc (.between ChronoUnit/DAYS start end)))
         (map #(.plusDays start %))
         (filter #(not (#{DayOfWeek/SATURDAY DayOfWeek/SUNDAY} (.getDayOfWeek %))))
         count)))

(defn fetch-leave-requests [subdomain api-key from to]
  (let [url (str "https://" subdomain ".sage.hr/api/leave-management/requests")
        response (client/get url {:headers {:x-auth-token api-key}
                                  :throw-entire-message? true
                                  :as :json
                                  :query-params {:from from
                                                 :to to}})]
    (get-in response [:body :data])))

(defn fetch-employees [subdomain api-key]
  (let [url (str "https://" subdomain ".sage.hr/api/employees")
        response (client/get url {:headers {:x-auth-token api-key}
                                  :throw-entire-message? true
                                  :as :json})]
    (get-in response [:body :data])))

(defn sum-days [coll]
  (reduce (fn [acc curr]
            (let [{:keys [employee_id days]} curr
                  accumulated-days (:days acc 0)]
              {:employee_id (or employee_id (:employee_id acc))
               :days (+ accumulated-days days)}))
          {:days 0}
          coll))

(defn fetch-absent-days [api-key subdomain employees to from]
  (let [items (fetch-leave-requests subdomain api-key from to)
        employee-ids (set employees)]
    (->> items
         (filter #(contains? employee-ids (:employee_id %)))
         (map (fn [{:keys [employee_id start_date end_date]}]
                {:employee_id employee_id
                 :days (working-days-between start_date end_date)}))
         (group-by :employee_id)
         (vals)
         (map sum-days))))


(defn report [req-employees api-key subdomain to from]
  (let [employees (fetch-employees subdomain api-key)
        absent-days (fetch-absent-days api-key subdomain req-employees to from)
        absent-days-with-names (for [employee employees
                                     absent-day absent-days
                                     :when (= (employee :id) (absent-day :employee_id))]
                                 (merge absent-day {:name (str (:first_name employee)
                                                                " "
                                                                (:last_name employee))}))]
    absent-days-with-names))