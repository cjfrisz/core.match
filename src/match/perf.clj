(ns match.perf)

(defprotocol I
  (x [this])
  (y [this]))

(defprotocol IClass
  (id [this]))

(deftype A [x y] I (x [_] x) (y [_] y))
(deftype B [x y] I (x [_] x) (y [_] y))
(deftype C [x y] I (x [_] x) (y [_] y))
(deftype D [x y] I (x [_] x) (y [_] y))

(extend-type A IClass (id [_] 0))
(extend-type B IClass (id [_] 1))
(extend-type C IClass (id [_] 2))
(extend-type D IClass (id [_] 3))

(defn dag1 [f1 f2]
  (case (id f1)
        0 (case (id (x f1))
                0 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood)
                1 :m-not-understood
                2 :m-not-understood
                3 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood))
        1 (case (id (x f1)) 
                0 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood)
                1 :m2
                2 :m-not-understood
                3 (case (= (y f1) (y f2))
                        true :m1
                        false :m-not-understood))
        2 (case (id f2)
                0 (case (id (x f1))
                        0 :m4
                        1 :m2
                        2 :m4
                        3 :m4)
                1 (case (id (x f1))
                        0 :m4
                        1 :m2
                        2 :m4
                        3 :m4)
                2 :m3
                3 :m-ambiguous)))

(defn dag2 [f1 f2]
  (letfn [(sd1 []
            (case (= (y f1) (y f2))
                         true :m1
                         false :m-not-understood))
          (sd2 []
            (case (id (x f1))
                         0 :m4
                         1 :m2
                         2 :m4
                         3 :m4))]
   (case (id f1)
         0 (case (id (x f1))
                 0 (sd1)
                 1 :m-not-understood
                 2 :m-not-understood
                 3 (sd1))
         1 (case (id (x f1)) 
                 0 (sd1)
                 1 :m2
                 2 :m-not-understood
                 3 (sd1))
         2 (case (id f2)
                 0 (sd2)
                 1 (sd2)
                 2 :m3
                 3 :m-ambiguous))))

(defn dag3 [fa fb]
  (case (.getName ^Class (class fa))
            "match.perf.A" (case (.getName ^Class (class (x fa)))
                                     "match.perf.A" (case (= (y fa) (y fb))
                                                          true :ma
                                                          false :m-nu)
                                     "match.perf.B" :m-nu
                                     "match.perf.C" :m-nu
                                     "match.perf.D" (case (= (y fa) (y fb))
                                                          true :ma
                                                          false :m-nu))
            "match.perf.B" (case (.getName ^Class (class (x fa))) 
                                     "match.perf.A" (case (= (y fa) (y fb))
                                                          true :ma
                                                          false :m-nu)
                                     "match.perf.B" :mb
                                     "match.perf.C" :m-nu
                                     "match.perf.D" (case (= (y fa) (y fb))
                                                          true :ma
                                                          false :m-nu))
            "match.perf.C" (case (.getName ^Class (class fb))
                                     "match.perf.A" (case (.getName ^Class (class (x fa)))
                                                              "match.perf.A" :md
                                                              "match.perf.B" :mb
                                                              "match.perf.C" :md
                                                              "match.perf.D" :md)
                                     "match.perf.B" (case (.getName ^Class (class (x fa)))
                                                              "match.perf.A" :md
                                                              "match.perf.B" :mb
                                                              "match.perf.C" :md
                                                              "match.perf.D" :md)
                                     "match.perf.C" :mc
                                     "match.perf.D" :m-amb)))

(comment
  (let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dag1 o1 o2))
  
  ;; 480ms
  (let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dotimes [_ 10]
      (time
       (dotimes [_ 1e7]
         (dag1 o1 o2)))))

  (let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dag2 o1 o2))
  
  ;; 750ms
  (let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dotimes [_ 10]
      (time
       (dotimes [_ 1e7]
         (dag2 o1 o2)))))

  (let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dag3 o1 o2))

  ;; 350ms
  (let [s1 (B. nil nil)
        o1 (A. (A. nil nil) s1)
        o2 (A. (A. nil nil) s1)]
    (dotimes [_ 10]
      (time
       (dotimes [_ 1e7]
         (dag3 o1 o2)))))
  )