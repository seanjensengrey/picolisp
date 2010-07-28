# 28jul10abu
# (c) Software Lab. Alexander Burger

(setq
   "Fix1" (0 . 1.0)
   "Fix2" (0 . 1.0) )

(de pow (X Y)
   (set "Fix1" X  "Fix2" Y)
   (native "@" "pow" 1.0 "Fix1" "Fix2") )

(de exp (X)
   (set "Fix1" X)
   (native "@" "exp" 1.0 "Fix1") )

(de log (X)
   (when (gt0 (set "Fix1" X))
      (native "@" "log" 1.0 "Fix1") ) )

(de sin (A)
   (set "Fix1" A)
   (native "@" "sin" 1.0 "Fix1") )

(de cos (A)
   (set "Fix1" A)
   (native "@" "cos" 1.0 "Fix1") )

(de tan (A)
   (set "Fix1" A)
   (native "@" "tan" 1.0 "Fix1") )

(de asin (A)
   (set "Fix1" A)
   (native "@" "asin" 1.0 "Fix1") )

(de acos (A)
   (set "Fix1" A)
   (native "@" "acos" 1.0 "Fix1") )

(de atan (A)
   (set "Fix1" A)
   (native "@" "atan" 1.0 "Fix1") )

(de atan2 (X Y)
   (set "Fix1" X  "Fix2" Y)
   (native "@" "atan2" 1.0 "Fix1" "Fix2") )

# vi:et:ts=3:sw=3
