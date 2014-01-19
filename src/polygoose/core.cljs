(ns polygoose.core
  (:require-macros [dommy.macros :as dommym :refer [node by-id sel1]]
                   [garden.def :as gardenm]
                   [cljs.core.async.macros :as asyncm :refer [go alt!]])
  (:require [cljs.core.async :as async :refer [chan
                                               timeout
                                               <!
                                               >!
                                               take!
                                               put!
                                               close!]]

            [dommy.core :as dommy :refer [listen!]]
            [dommy.attrs :as dommya]
            [dommy.utils :as dommyu]
            [dommy.template :as dommyt]
            [garden.core :as garden]
            [garden.arithmetic]
            [garden.color]
            [garden.compiler]
            [garden.media]
            [garden.stylesheet]
            [garden.types]
            [garden.units]
            [garden.util]
            [cloact.core :as cloact]
            [cljs-http.client :as http-client]))

;; make prn/print go to (.log js/console)
(enable-console-print!)

;; make an api call in a go block
(go (let [r (<! (http-client/get "https://api.github.com/users" {:with-credentials? false}))]
      (prn "go block!")
      (prn (:status r))
      (prn (map :login (:body r)))))

;; make an api call using callbacks (boooo!)
(async/take! (http-client/get "https://api.github.com/users" {:with-credentials? false})
             (fn [r]
               (prn "callback!")
               (prn (:status r))
               (prn (map :login (:body r)))))

;; build a stylesheet
(def test-style [:body {:background "red"}])
(garden/css test-style)


;; camera stuff

;; The idea is to have a camera that you can click to take a picture. If you want to retake it,
;; clicking again will re-activate it. If you find a picture you like, you can "submit" it
;; (currently to an img field).

(defn build-dom []
  (dommy/append! (sel1 "body")
                 (node
                  [:div#container {:style (garden/style {:position :absolute})}
                   [:div#viddiv {:style (garden/style
                                         {:position :absolute
                                          :left 0
                                          :top 0
                                          :z-index 500})}
                   [:video#video {:width 640 :height 480 :autoplay true}]]
                   ;[:button#snap "Cheese!"]
                   [:div#canvasdiv {:style (garden/style
                                            {:position :absolute
                                             :left 0
                                             :top 0
                                             :z-index 1})}
                   [:canvas#canvas {:width 640 :height 480}]]
                   [:button#submit {:style (garden/style {:position :relative
                                                          :top 481})} "Submit!"]
                   [:img#snapshot {:style (garden/style {:position :relative
                                                         :left 641})}]])))


(defn swap-zindex!
  [foo bar]
  (let [foo-z (dommya/style foo :z-index)
        bar-z (dommya/style bar :z-index)]
    (dommya/set-style! foo :z-index bar-z)
    (dommya/set-style! bar :z-index foo-z)))


(defn get-canvas-context
  [canvas]
  (.getContext canvas "2d"))

(defn take-snapshot!
  [video canvas & {:keys [width height]}]
  (let [w (or width 640)
        h (or height 480)]
    (.drawImage (get-canvas-context canvas) video 0 0 w h)))

(defn setup-snapshot!
  [el video canvas & {:keys [width height]}]
  (let [w (or width 640)
        h (or height 480)]
    (.webkitGetUserMedia js/navigator
                         #js {:video true :audio false}
                         (fn [stream]
                           (aset video "src" (.createObjectURL (.-webkitURL js/window) stream))
                           (.play video))
                         #(.debug js/console (.stringify js/JSON %)))
    (listen! video
             :click
             (fn []
               (prn "taking snapshot then switching to canvas")
               (take-snapshot! video canvas :width w :height h)
               (swap-zindex! (dommy/closest video :div) (dommy/closest canvas :div))
               ))

    (listen! canvas
             :click
             (fn []
               (prn "switching to the video element")
               (swap-zindex! (dommy/closest video :div) (dommy/closest canvas :div))))

    ))


(defn get-data-url-from-canvas
  "Get the data url from a canvas"
  [canvas]
  (.toDataURL canvas))

(defn set-img-from-canvas!
  "Set an img based on the contents of a canvas"
  [canvas img]
  (aset img "src" (get-data-url-from-canvas canvas)))

(go (build-dom)
    (<! (timeout 100))

    (setup-snapshot! (by-id "snap")
                     (by-id "video")
                     (by-id "canvas"))

    (listen! (by-id "submit")
             :click
             (fn []
               (prn "submitting image")
               (set-img-from-canvas! (by-id "canvas")
                                     (by-id "snapshot")))))
