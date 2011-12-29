# 29dec11abu
# (c) Software Lab. Alexander Burger

(let "Lst" (chop (opt))
   (if (= "-" (car "Lst"))
      (let "Str" (pack (cdr "Lst"))
         (for "Sym" (all)
            (and (pre? "Str" "Sym") (prinl "-" "Sym")) ) )
      (let ("Path" (rot (split "Lst" "/"))  "Str" (pack (car "Path")))
         (setq "Path" (and (cdr "Path") (pack (glue "/" @) "/")))
         (for "Sym" (dir "Path" T)
            (and (pre? "Str" "Sym") (prinl "Path" "Sym")) ) ) ) )

# vi:et:ts=3:sw=3
