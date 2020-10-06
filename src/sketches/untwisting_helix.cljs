(ns sketches.untwisting-helix
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
  [x y]
  {:vx         0
   :vy         0
   :size       10
   :direction  0
   ; Initial positions
   :ix         x
   :iy         y
   ; Current positions
   :x          x
   :y          y
   :color      (rand-nth (:colors (:purple-haze c/palettes)))})

(def helix-length 800)
(def helix-step 30)

(defn gen-helix [x y vertical?]
  (cond vertical? 
        (map particle (repeat x) (range y (+ y helix-length) helix-step))
        :else
        (map particle (range x (+ x helix-length) helix-step) (repeat y) )))


(defn sketch-setup 
  "Returns the initial state to use for the update-render loop."
  []
  {:helix-1 (gen-helix (/ w 2) (- (/ h 2) (/ helix-length 2)) true)
   :step 0})


(def amp 100)
(def freq 1)

(defn calc-oscillating-position [ipos step freq-mod]
  (+ ipos (* amp (Math/sin (* step freq freq-mod)))))

(defn apply-acceleration [x v fx strength]
  (def abs-distance (Math/abs (- fx x)))
  (def direction (/ (- fx x) abs-distance))
  (def dv (cond (< abs-distance 5)
                (* direction 10 strength)
                :else
                (* strength direction abs-distance)))
  (+ v dv))

(defn sketch-update
  "Receives the current state. Returns the next state to render."
  [state]
  {:helix-1 (map (fn [p]
                   ; Update velocities
                   (def vp (assoc p
                                  :vy (apply-acceleration (:y p)
                                                          (:vy p)
                                                          (/ h 2)
                                                          0.0001)))
                   ; Update positions
                   (assoc vp
                          :x (calc-oscillating-position (:ix vp)
                                                        (:step state)
                                                        (/ (:y p) h 5))
                          :y (+ (:vy vp) (:y vp))
                          ))
                 (:helix-1 state))
   :step (inc (:step state))})


(defn sketch-draw
  "Draws the current state to the canvas. Called on each iteration after
  sketch-update."
  [state]
  (apply q/background (:background (:purple-haze c/palettes)))
  (q/no-stroke)
  (c/draw-circles (:helix-1 state) ))


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
(defonce sketch (create "untwisting-helix"))

