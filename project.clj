(defproject crucible "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.taoensso/timbre "4.0.0"]
                 ;crucible dep
                 [com.novemberain/langohr "3.0.1"]
                 [com.cognitect/transit-clj "0.8.275"]
                 ;raft dep
                 [clj-time "0.11.0"]
                 ;raft dep
                 [http-kit "2.1.18"]
                 ;raft dep
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;raft dep
                 [compojure "1.4.0"]
                 ;TODO we need to figure out why we need this
                 [javax.servlet/servlet-api "2.5"]])