# 27jul10abu
# (c) Software Lab. Alexander Burger

(setq
   "Arg1" (0 . 1.0)
   "Arg2" (0 . 1.0) )

(de pow (X Y)
   (set "Arg1" X  "Arg2" Y)
   (native "@" "pow" 1.0 "Arg1" "Arg2") )

(de exp (X)
   (set "Arg1" X)
   (native "@" "exp" 1.0 "Arg1") )

(de log (X)
   (when (gt0 (set "Arg1" X))
      (native "@" "log" 1.0 "Arg1") ) )

(de sin (A)
   (set "Arg1" A)
   (native "@" "sin" 1.0 "Arg1") )

(de cos (A)
   (set "Arg1" A)
   (native "@" "cos" 1.0 "Arg1") )

(de tan (A)
   (set "Arg1" A)
   (native "@" "tan" 1.0 "Arg1") )

(de asin (A)
   (set "Arg1" A)
   (native "@" "asin" 1.0 "Arg1") )

(de acos (A)
   (set "Arg1" A)
   (native "@" "acos" 1.0 "Arg1") )

(de atan (A)
   (set "Arg1" A)
   (native "@" "atan" 1.0 "Arg1") )

(de atan2 (X Y)
   (set "Arg1" X  "Arg2" Y)
   (native "@" "atan2" 1.0 "Arg1" "Arg2") )

# vi:et:ts=3:sw=3
