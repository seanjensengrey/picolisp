# 26jun10abu
# (c) Software Lab. Alexander Burger

(and (=0 *Scl) (scl 6))  # Default scale 6

# Global constants
(let P (make (in "@lib/pi1k.txt" (do (+ *Scl 4) (link (char)))))
   (setq
      pi   (format P *Scl)
      pi/2 (*/ (format P (inc *Scl)) 20) ) )

(load (if (== 64 64) "@lib/math64.l" "@lib/math32.l"))

# vi:et:ts=3:sw=3
