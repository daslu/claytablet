(ns clojure-ide.core
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [cheshire.core :as json]
            [nrepl.server :refer [start-server stop-server default-handler]]
            [nrepl.core :as nrepl]
            [cider.piggieback :refer [wrap-cljs-repl]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defonce nrepl-server (atom nil))
(defonce jetty-server (atom nil))

(defn start-nrepl []
  (reset! nrepl-server (start-server :port 7000
                                    :handler default-handler))
  (println "nREPL server started on port 7000"))

(defn stop-nrepl []
  (when @nrepl-server
    (stop-server @nrepl-server)
    (reset! nrepl-server nil)
    (println "nREPL server stopped")))

(defn start-jetty []
  (reset! jetty-server (run-jetty app {:port 3000 :join? false}))
  (println "Jetty server started on port 3000"))

(defn stop-jetty []
  (when @jetty-server
    (.stop @jetty-server)
    (reset! jetty-server nil)
    (println "Jetty server stopped")))

(defn restart-jetty []
  (stop-jetty)
  (start-jetty))

(defn list-clojure-files [dir]
  (let [file (io/file dir)]
    (filter #(.endsWith (.getName %) ".clj")
            (file-seq file))))

(defn list-files []
  (let [clojure-files (concat (list-clojure-files "./notebooks")
                              (list-clojure-files "./src"))]
    (mapv (fn [f]
            {:name (.getName f)
             :path (.getPath f)})
          clojure-files)))

(defn load-file [path]
  ;; Load and return the content of the specified file
  {:path path
   :content (slurp path)})

(defn save-file [path content]
  ;; Save the provided content to the specified file
  (spit path content)
  {:status "success"})

(defn eval-code [code]
  ;; Evaluate the provided Clojure code via nREPL
  (try
    (with-open [conn (nrepl/connect :port 7000)]
      (let [msgs (client/message conn {:op "eval" :code code})
            results (->> msgs
                         (filter #(contains? % :value))
                         (map :value)
                         (str/join "\n"))]
        {:result results}))
    (catch Exception e
      {:result (str "Error: " (.getMessage e))})))

(start-server :port 7001
              :handler default-handler)

;; (with-open [conn (nrepl/connect :port 7001)]
;;   (-> (nrepl/client conn 1000)    ; message receive timeout required
;;       (nrepl/message {:op "eval" :code "(+ 2 3)"})
;;       nrepl/response-values))

(defroutes app-routes
  (GET "/" []
       (-> (response/resource-response "index.html" {:root "public"})
           (response/content-type "text/html; charset=utf-8")))
  (GET "/api/files" []
       (-> (response/response (json/generate-string (list-files)))
           (response/content-type "application/json; charset=utf-8")))
  (GET "/api/files/*" [*]
       (let [path *
             file (io/file path)]
         (if (.exists file)
           (-> (response/response (json/generate-string (load-file path)))
               (response/content-type "application/json; charset=utf-8"))
           (-> (response/response (json/generate-string {:error "File not found"}))
               (response/status 404)
               (response/content-type "application/json; charset=utf-8")))))
  (POST "/api/files/*" [* :as req]
        (let [path *
              content (slurp (:body req))]
          (if (.exists (io/file path))
            (-> (response/response (json/generate-string (save-file path content)))
                (response/content-type "application/json; charset=utf-8"))
            (-> (response/response (json/generate-string {:error "File not found"}))
                (response/status 404)
                (response/content-type "application/json; charset=utf-8")))))
  (POST "/api/eval" {body :body}
        (let [code (slurp body)]
          (-> (response/response (json/generate-string (eval-code code)))
              (response/content-type "application/json; charset=utf-8"))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults #'app-routes (assoc-in site-defaults [:security :anti-forgery] false)))

(defn -main []
  (start-nrepl)
  (restart-jetty)
  (println "Clojure IDE is running."))

(comment
  (-main))
