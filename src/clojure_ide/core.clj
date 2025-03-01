(ns clojure-ide.core
  (:require [compojure.core :refer [GET POST routes defroutes]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [cheshire.core :as json]
            [nrepl.server :refer [start-server stop-server default-handler]]
            [cider.piggieback :refer [wrap-cljs-repl]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defonce nrepl-server (atom nil))
(defonce jetty-server (atom nil))

(defn start-nrepl []
  (reset! nrepl-server (start-server :port 7000
                                    :handler default-handler)))

(defn stop-nrepl []
  (when @nrepl-server
    (stop-server @nrepl-server)
    (reset! nrepl-server nil)))

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

(defn list-files []
  ;; Placeholder for listing files from the server's filesystem
  ;; Implement filesystem browsing logic here
  [{:name "core.clj" :path "/src/clojure_ide/core.clj"}
   {:name "project.clj" :path "/project.clj"}])

(defn load-file [path]
  ;; Placeholder for loading file content
  ;; Implement file reading logic here
  {:path path
   :content "(ns clojure-ide.core)\n\n(defn hello []\n  (println \"Hello, World!\"))"})

(defn save-file [path content]
  ;; Placeholder for saving file content
  ;; Implement file writing logic here
  {:status "success"})

(defn eval-code [code]
  ;; Placeholder for evaluating Clojure code via nREPL
  ;; Implement code evaluation logic here
  {:result "(println \"Hello from nREPL\")"})

(defroutes app-routes
  (GET "/" []
    (-> (response/resource-response "index.html" {:root "public"})
        (response/content-type "text/html; charset=utf-8")))
  (GET "/api/files" []
    (-> (response/response (json/generate-string (list-files)))
        (response/content-type "application/json; charset=utf-8")))
  (GET "/api/files/*" [*]
    (let [path (str "/" *)]
      (-> (response/response (json/generate-string (load-file path)))
          (response/content-type "application/json; charset=utf-8"))))
  (POST "/api/files/*" [* :as req]
    (let [path (str "/" *)
          content (slurp (:body req))]
      (-> (response/response (json/generate-string (save-file path content)))
          (response/content-type "application/json; charset=utf-8"))))
  (POST "/api/eval" {body :body}
    (let [code (slurp body)]
      (-> (response/response (json/generate-string (eval-code code)))
          (response/content-type "application/json; charset=utf-8"))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults #'app-routes site-defaults))

(defn -main []
  (start-nrepl)
  (start-jetty)
  (println "Clojure IDE is running."))

(comment
  (-main))
