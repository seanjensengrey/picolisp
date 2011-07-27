# 27jul11abu
# (c) Software Lab. Alexander Burger

# Convert to PDF document
(de dviPdf (Doc)
   (prog1
      (tmp Doc ".pdf")
      (call "/usr/bin/dvips" "-q" (pack Doc ".dvi"))
      (call "ps2pdf" (pack Doc ".ps") @)
      (call 'rm "-f"
         (pack Doc ".tex")
         (pack Doc ".dvi")
         (pack Doc ".ps") ) ) )

# Tex Formatter
(de texFmt (S)
   (_tex S)
   (prinl) )

(de tex (S  . @)
   (prin "\\" S)
   (when (args)
      (prin "{")
      (_tex (next))
      (while (args)
         (when (next)
            (prin "\\\\")
            (_tex (arg)) ) )
      (prin "}") )
   (prinl) )

(de texl (S Lst)
   (prin "\\" S "{")
   (_tex (pop 'Lst))
   (while Lst
      (when (pop 'Lst)
         (prin "\\\\")
         (_tex @) ) )
   (prinl "}") )

(de _tex (X)
   (for C (chop X)
      (cond
         ((sub? C "#$%&_{}")
            (prin "\\" C) )
         ((sub? C "<²>")
            (prin "$" C "$") )
         (T
            (prin
               (case C
                  (`(char 8364) "\\EUR")
                  ("\"" "\\char34")
                  ("\\" "$\\backslash$")
                  ("\^" "\\char94")
                  ("~" "\\char126")
                  (T C) ) ) ) ) ) ) )


### TeX Document ###
(de document (Doc Cls Typ Use . Prg)
   (out (list "@bin/lat1" (pack Doc ".tex"))
      (prinl "\\documentclass[" Cls "]{" Typ "}")
      (while Use
         (if (atom (car Use))
            (prinl "\\usepackage{" (pop 'Use) "}")
            (prinl "\\usepackage[" (caar Use) "]{" (cdr (pop 'Use)) "}") ) )
      (prinl "\\begin{document}")
      (prEval Prg 2)
      (prinl "\\end{document}") )
   (call 'sh "-c"
      (pack "latex -interaction=batchmode " Doc ".tex >/dev/null") )
   (call 'rm (pack Doc ".aux") (pack Doc ".log")) )

(de \\block (S . Prg)
   (prinl "\\begin{" S "}")
   (prEval Prg 2)
   (prinl "\\end{" S "}") )


### Tabular environment ###
(de \\table (Fmt . Prg)
   (prinl "\\begin{tabular}[c]{" Fmt "}")
   (prEval Prg 2)
   (prinl "\\end{tabular}") )

(de \\carry ()
   (prinl "\\end{tabular}")
   (prinl)
   (prinl "\\begin{tabular}[c]{" "Fmt" "}") )

(de \\head @
   (prin "\\textbf{" (next) "}")
   (while (args)
      (prin " & \\textbf{")
      (_tex (next))
      (prin "}") )
   (prinl "\\\\") )

(de \\row @
   (when (=0 (next))
      (next)
      (prin "\\raggedleft ") )
   (ifn (=T (arg))
      (_tex (arg))
      (prin "\\textbf{")
      (_tex (next))
      (prin "}") )
   (while (args)
      (prin " & ")
      (when (=0 (next))
         (next)
         (prin "\\raggedleft ") )
      (ifn (=T (arg))
         (_tex (arg))
         (prin "\\textbf{")
         (_tex (next))
         (prin "}") ) )
   (prinl "\\\\") )

(de \\hline ()
   (prinl "\\hline") )

(de \\cline (C1 C2)
   (prinl "\\cline{" C1 "-" C2 "}") )


### Letter Document Class ###
(de \\letter (Lst . Prg)
   (prin "\\begin{letter}{" (pop 'Lst))
   (while Lst
      (when (pop 'Lst)
         (prin "\\\\" @) ) )
   (prinl "}")
   (prEval Prg 2)
   (prinl "\\end{letter}") )

(de \\signature (S)
   (tex "signature" S) )

(de \\opening (S)
   (tex "opening" S) )

(de \\closing (S)
   (tex "closing" S) )
