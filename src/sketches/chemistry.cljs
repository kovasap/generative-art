(ns sketches.chemistry
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

(def max-starting-velocity 0.5)
(def max-force 10)

(defn distance
  "Distance between two points encoded as maps with :x and :y keys."
  [p1 p2]
  (Math/sqrt (+ (Math/pow (- (:x p1) (:x p2)) 2)
                (Math/pow (- (:y p1) (:y p2)) 2))))

(def particle-consts {
   :halogen {:force (fn [self other] 
                    (let [dist (distance self other)
                          repulsive (/ 10 (Math/pow dist 2))
                          attractive (/ 10 dist)]
                         (min (- repulsive attractive) max-force)))
             :size 5
             :mass 10 }
   :metal {:force (fn [self other] 
                    (let [dist (distance self other)
                          repulsive (/ 10 (Math/pow dist 2))
                          attractive (/ 10 dist)]
                         (min (- repulsive attractive) max-force)))
             :size 10
             :mass 1000 }
})

(defn particle
  "Creates a particle map."
  [element id]
  (assoc element
         :id         id
         :vx         (q/random (- max-starting-velocity) max-starting-velocity)
         :vy         (q/random (- max-starting-velocity) max-starting-velocity)
         :direction  0
         :x          (q/random w)
         :y          (q/random h)
         :color      (rand-nth (:colors (:purple-haze c/palettes)))))


(defn sketch-setup 
  "Returns the initial state to use for the update-render loop."
  []
  {:particles (concat
                (map (partial particle (:halogen particle-consts)) (range 0 100))
                (map (partial particle (:metal particle-consts)) (range 0 10)))
   :step 0})


(defn apply-to-xy
  "Apply the given function to both :x and :y keys in the given map."
  [xy f]
  (assoc xy
         :x (f (:x xy))
         :y (f (:y xy))))

(defn unit-vector
  "Unit vector pointing from the source point to the target point."
  [source target dist]
  {:x (/ (- (:x target) (:x source)) dist)
   :y (/ (- (:y target) (:y source)) dist)})


(defn single-source-force
  "Force on target particle from the source particle."
  [target source]
  (if (= (:id target) (:id source))
    ; Have particles exert 0 force on themselves.
    {:x 0 :y 0}
    (let [dist (distance source target)
          force-mag ((:force source) source target)
          unit-vec (unit-vector source target dist)]
      {:x (* (:x unit-vec) force-mag) 
       :y (* (:y unit-vec) force-mag)})))

(defn global-force-on-particle
  "The net force on a particle from all other particles."
  [particle all-particles]
  (reduce (fn [xy1 xy2] {:x (+ (:x xy1) (:x xy2))
                         :y (+ (:x xy1) (:x xy2))})
          (map (partial single-source-force particle) all-particles)))

(defn update-positions
  "Updates positions of particles based on particle velocity for one timestep."
  [particle]
  (assoc particle
         :x (+ (:x particle) (:vx particle))
         :y (+ (:y particle) (:vy particle))))

(defn update-velocities
  "Updates velocities of particles based on given delta-v for one timestep."
  [particle dv]
  (assoc particle
         :vx (+ (:vx particle) (:x dv))
         :vy (+ (:vy particle) (:y dv))))

(defn sketch-update
  "Receives the current state. Returns the next state to render."
  [state]
  ; (print (first (:particles state)))
  {:particles (map (fn [p]
                     ; F = ma = m * dv / dt
                     ; -> dv = F * dt / m
                     (def dv (apply-to-xy
                               (global-force-on-particle p (:particles state))
                               (fn [force-val] (/ force-val (:mass p)))))
                     ; (if (= (:id p) 1) (print dv))
                     (-> p
                       (update-velocities dv)
                       (update-positions)))
                (:particles state))
   :step (inc (:step state))})


(defn sketch-draw
  "Draws the current state to the canvas. Called on each iteration after
  sketch-update."
  [state]
  (q/frame-rate 60)
  ; (apply q/background (:background (:purple-haze c/palettes)))
  (apply q/background [10 10 10 5])
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
(defonce sketch (create "chemistry"))

