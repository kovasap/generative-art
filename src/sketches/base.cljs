(ns sketches.base
  (:require [cljs.pprint]
            [sketches.common :as c]
            [quil.core :as q :include-macros true]
            [quil.middleware :as middleware]))

; Get size of canvas to inform where to draw things.
; (def body (.-body js/document))
; (def w (.-clientWidth body))
; (def h (.-clientHeight body))
(def w 1080)
(def h 1080)

(defn particle
  "Creates a particle map."
  [id]
  {
         :id         id
         :vx         0
         :vy         0
         :direction  0
         :x          (q/random w)
         :y          (q/random h)
         :color      (rand-nth (:colors (:purple-haze c/palettes)))})


(defn sketch-setup 
  "Returns the initial state to use for the update-render loop."
  []
  {:particles (map (particle (range 0 100)))
   :step 0})


(defn sketch-update
  "Receives the current state. Returns the next state to render."
  [state]
  ; (print (first (:particles state)))
  {:particles (:particles state)
   :step (inc (:step state))})


(defn sketch-draw
  "Draws the current state to the canvas. Called on each iteration after
  sketch-update."
  [state]
  (q/frame-rate 60)
  (apply q/background (:background (:purple-haze c/palettes)))
  (q/no-stroke)
  (c/draw-circles (:particles state) ))


; Draw a quil sketch onto the canvas.
(defn create [canvas]
  (q/sketch
    :host canvas
    :size [w h]
    :draw #'sketch-draw
    :setup #'sketch-setup
    :update #'sketch-update
    :middleware [middleware/fun-mode]
    :settings (fn []
                ; (q/pixel-density 1)
                (q/random-seed 666)
                (q/noise-seed 666))))

; Make sure that only one instance of the sketch is ever created.
(defonce sketch (create "base"))

