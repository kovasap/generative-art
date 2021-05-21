(ns sketches.lines
  (:require [cljs.pprint :as pp]
            [sketches.common :as c]
            [quil.core :as q :include-macros true]
            [quil.middleware :as middleware]))

; Get size of canvas to inform where to draw things.
; (def body (.-body js/document))
; (def w (.-clientWidth body))
; (def h (.-clientHeight body))
(def w 800)
(def h 500)

(defn line
  "Creates a line map."
  [id]
  {
         :id         id
         :x1          (q/random w)
         :y1          (q/random h)
         :x2          (q/random w)
         :y2          (q/random h)
         :color      (rand-nth (:colors (:purple-haze c/palettes)))})


(defn sketch-setup 
  "Returns the initial state to use for the update-render loop."
  []
  {:lines (map line (range 0 10))
   :step 0})


(defn sketch-update
  "Receives the current state. Returns the next state to render."
  [state]
  ; (print (first (:lines state)))
  {:lines (:lines state)
   :step (inc (:step state))})


(defn sketch-draw
  "Draws the current state to the canvas. Called on each iteration after
  sketch-update."
  [state]
  (q/frame-rate 60)
  (apply q/background [255 255 255 255])
  ; (q/no-stroke)
  (pp/pprint (:lines state))
  (doseq [ln (:lines state)]
    (apply q/fill (:color ln))
    (q/line (:x1 ln) (:y1 ln) (:x2 ln) (:y2 ln))))


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
(defonce sketch (create "lines"))

