(ns sketches.ball
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
  [id x y]
  {:id         id
   :vx         0
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

(def num-parts 120)
(def helix-step 30)
(def parts-per-layer 6)

(defn gen-helix [x y]
  (map particle
       (range)
       (repeat x)
       (map (fn [i] (+ y (* (quot i parts-per-layer) helix-step)))
            (range num-parts))))

(def helix-x (/ w 2))
(def helix-y 100)

(defn sketch-setup 
  "Returns the initial state to use for the update-render loop."
  []
  {:helix-1 (gen-helix helix-x helix-y)
   :step 0})


(def freq 0.05)

(defn calc-oscillating-position [ipos amp step freq-mod offset]
  (+ ipos (* amp (Math/sin (+ offset (* step freq freq-mod))))))

(defn sketch-update
  "Receives the current state. Returns the next state to render."
  [state]
  {:helix-1 (map (fn [p]
                   ; Ranges from 0 to 0.5, where 0 is at the equator and 0.5 is
                   ; at the pole.
                   (def dist-from-half
                     (Math/abs (- (/ (- (:y p) helix-y)
                                     (* helix-step
                                        (/ num-parts parts-per-layer)))
                                  0.5)))
                   ; Update positions
                   (assoc p
                          :x (calc-oscillating-position
                               (:ix p)
                               (* 200 (Math/sqrt (- 1 (/ (Math/pow (* 2 dist-from-half) 2) 0.4))))
                               (:step state)
                               1
                               (rem (:id p) parts-per-layer))
                          ))
                 (:helix-1 state))
   :step (inc (:step state))})


(defn sketch-draw
  "Draws the current state to the canvas. Called on each iteration after
  sketch-update."
  [state]
  ; (apply q/background (:background (:purple-haze c/palettes)))
  (apply q/background [10 10 10])
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
(defonce sketch (create "ball"))

