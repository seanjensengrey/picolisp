# 03may11abu
# (c) Software Lab. Alexander Burger

##############################
# Iterative calculation of PI:
#  S = 0
#  P = 2
#  Loop
#     S = sqrt(S+2)
#     P = 2*P/S
##############################

(de pi (N Eps)
   (default Eps 100)
   (let (Scl (** 10 N)  S 0  N2 (* 2 Scl)  P N2  P2 0)
      (while (> (- P P2) Eps)
         (setq
            P2 P
            S (sqrt (* Scl (+ S N2)))
            P (*/ N2 P S) ) ) ) )

(test 3141592653589793238462643383279502884197169399375105820975043
   (pi 60) )


###################################
# Spigot algorithm (Jeremy Gibbons)
# Print next digit of PI (unbounded)

(de piDigit ()
   (job '((Q . 1) (R . 0) (S . 1) (K . 1) (N . 3) (L . 3))
      (while (>= (- (+ R (* 4 Q)) S) (* N S))
         (mapc set '(Q R S K N L)
            (list
               (* Q K)
               (* L (+ R (* 2 Q)))
               (* S L)
               (inc K)
               (/ (+ (* Q (+ 2 (* 7 K))) (* R L)) (* S L))
               (+ 2 L) ) ) )
      (prog1 N
         (let M (- (/ (* 10 (+ R (* 3 Q))) S) (* 10 N))
            (setq Q (* 10 Q)  R (* 10 (- R (* N S)))  N M) ) ) ) )

(prin (piDigit) ".")
(loop
   (prin (piDigit))
   (flush) )
