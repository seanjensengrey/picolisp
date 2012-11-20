# 19nov12tj
# Authors Alexander Burger, Thorsten Jolitz
# (c) Software Lab. Alexander Burger

# Line editor
# emacs-mode

(mapc undef
   '(*Led fkey revise) )

(setq
   "Line" NIL # Holds current input line
   "LPos" 1 # Position in line (1 .. length)
   "HPos" 1 # Position in history
   "UndoLine" NIL # Undo
   "UndoPos" 0
   "Line1" NIL # Initial line
   "Insert" T # Insert mode flag
   "FKey" NIL # Function key bindings
   "Clip" NIL # Cut/Copy/Paste buffer
   "Item" NIL # Item to find
   "Found" NIL # Find stack
   "Mark" NIL # Position of mark
   "Register" NIL # (Named) storage for text-snippets
   "Complete" NIL # Input completion

   "HistMax" 1000 # History limit

   "History" # History of input lines
   (in (pack "+" (pil "history"))
      (ctl NIL
         (make (until (eof) (link (line T)))) ) )
   "Hist0" "History" )


# Switch Crtl-C off

# Ctrl-C is actually not defined as a special key, but as a signal
# handler. Depending on the 'stty' settings, a SIGINT signal is sent to
# the process when Ctrl-C is pressed.
#
# If this is not desired, then some other key (or none) must be set in the
# terminal settings. This can be done with
#
#    $ stty intr ^A
#
# or, from inside PicoLisp
#
#    (call 'stty "intr" "^A")

(raw T)
(call 'stty "intr" "")

# Basic editing routine
(de chgLine (L N)
   (let (D (length "Line") Tsm)
      (for (P (dec "LPos") (>= P 1) (dec P)) # To start of old line
         (unless
            (and
               *Tsm
               (= "\"" (get "Line" P))
               (skipQ "LPos" P "Line") )
            (prin "^H") ) )
      (for (P . C) (setq "Line" L) # Output new line
         (cond
            ((> " " C)
               (dec 'D)
               (prin "_") )
            ((or (not *Tsm) (<> "\"" C) (escQ P L))
               (dec 'D)
               (prin C) )
            (T
               (prin
                  (and Tsm (cdr *Tsm))
                  (unless (skipQ N P L)
                     (dec 'D)
                     C )
                  (and (onOff Tsm) (car *Tsm)) ) ) ) )
      (and Tsm (prin (cdr *Tsm)))
      (space D) # Clear rest of old line
      (do D (prin "^H"))
      (setq "LPos" (inc (length L)))
      (until (= N "LPos") # To new position
         (unless
            (and
               *Tsm
               (= "\"" (get "Line" "LPos"))
               (skipQ N "LPos" "Line") )
            (prin "^H") )
         (dec '"LPos") ) )
   (flush) )

# Skipped double quote
(de skipQ (N P L)
   (nor
      (>= (inc N) P (dec N))
      (= "\"" (get L (dec P)))
      (= "\"" (get L (inc P)))
      (escQ P L) ) )

# Escaped double quote
(de escQ ()
   (let Esc NIL
      (for I (dec P)
         ((if (= "\\" (get L I)) onOff off) Esc) ) ) )

# Check for delimiter
(de delim? (C)
   (member C '`(chop '" ^I^J^M\"'()[]`~")) )

# Move left
(de lMove ()
   (chgLine "Line" (max 1 (dec "LPos"))) )

# Move to beginning
(de bMove ()
   (chgLine "Line" 1) )

# Move right
(de rMove ()
   (chgLine "Line"
      (if (>= "LPos" (length "Line"))
         "LPos"
         (inc "LPos") ) ) )

# Move to end of line
(de eMove ()
   (chgLine "Line" (length "Line")) )

# Move beyond end of line
(de xMove ()
   (chgLine "Line" (inc (length "Line"))) )

# Move word left
(de lWord ()
   (use (N L)
      (chgLine "Line"
         (if (>= 1 (setq N "LPos"))
            1
            (loop
               (T (= 1 (dec 'N)) 1)
               (setq L (nth "Line" (dec N)))
               (T (and (delim? (car L)) (not (delim? (cadr L))))
                  N ) ) ) ) ) )

# Move word right
(de rWord ()
   (use (M N L)
      (setq M (length "Line"))
      (chgLine "Line"
         (if (<= M (setq N "LPos"))
            M
            (loop
               (T (= M (inc 'N)) M)
               (setq L (nth "Line" (dec N)))
               (T (and (delim? (car L)) (not (delim? (cadr L))))
                  N ) ) ) ) ) )

(de vi-rWord ()
   (use (M N L)
      (setq M (length "Line"))
      (chgLine "Line"
         (if (<= M (setq N "LPos"))
            M
            (loop
               (T (= M (inc 'N)) M)
               (setq L (nth "Line" (dec N)))
               (T (and (delim? (car L)) (not (delim? (cadr L))))
                  N ) ) ) ) ) )

# Match left parenthesis
(de lPar ()
   (let (N 1 I (dec "LPos"))
      (loop
         (T (=0 I))
         (case (get "Line" I)
            (")" (inc 'N))
            ("(" (dec 'N)) )
         (T (=0 N) (chgLine "Line" I))
         (dec 'I) ) ) )

# Match right parenthesis
(de rPar ()
   (let (N 1 I (inc "LPos"))
      (loop
         (T (> I (length "Line")))
         (case (get "Line" I)
            ("(" (inc 'N))
            (")" (dec 'N)) )
         (T (=0 N) (chgLine "Line" I))
         (inc 'I) ) ) )

# Clear to end of line
(de clrEol ()
   (let N (dec "LPos")
      (if (=0 N)
         (chgLine NIL 1)
         (chgLine (head N "Line") N) ) ) )

# Insert a char
(de insChar (C)
   (chgLine (insert "LPos" "Line" C) (inc "LPos")) )

(de del1 (L)
   (ifn (nth L "LPos")
      L
      (setq "Clip" (append "Clip" (list (get L "LPos"))))
      (remove "LPos" L) ) )

# Delete a char
(de delChar ()
   (use L
      (off "Clip")
      (chgLine
         (setq L (del1 "Line"))
         (max 1 (min "LPos" (length L))) ) ) )

# Delete a word (F: with trailing blank)
(de delWord (F)
   (let L "Line"
      (off "Clip")
      (ifn (= "(" (get L "LPos"))
         (while (and (nth L "LPos") (not (delim? (get L "LPos"))))
            (setq L (del1 L)) )
         (for (N 1 (and (setq L (del1 L)) (< 0 N)))
            (case (get L "LPos")
               ("(" (inc 'N))
               (")" (dec 'N)) ) ) )
      (and
         F
         (sp? (get L "LPos"))
         (setq L (del1 L)) )
      (chgLine L (max 1 (min "LPos" (length L)))) ) )

# Replace char
(de rplChar (C)
   (chgLine
      (insert "LPos" (remove "LPos" "Line") C)
      "LPos" ) )

# Undo mechanism
(de doUndo ()
   (setq "UndoLine" "Line" "UndoPos" "LPos") )

# Paste clip
(de doPaste ()
   (if (= 1 "LPos")
      (chgLine (append "Clip" "Line") 1)
      (chgLine
         (append
            (head (dec "LPos") "Line")
            "Clip"
            (nth "Line" "LPos") )
         (+ "LPos" (length "Clip") -1) ) ) )

# Set history line
(de setHist (N)
   (chgLine
      (if (=0 (setq "HPos" N))
         "Line1"
         (chop (get "History" "HPos")) )
      1 ) )

# Searching
(de ledSearch (L)
   (let (H (nth "History" (inc "HPos")) S (find '((X) (match "Item" (chop X))) H))
      (chgLine
         (ifn S
            (prog (beep) L)
            (push '"Found" "HPos")
            (inc '"HPos" (index S H))
            (chop S) )
         1 ) ) )

# TAB expansion
(de expandTab ()
   (let ("L" (head (dec "LPos") "Line") "S" "L")
      (while (find "skipFun" "S")
         (pop '"S") )
      (ifn "S"
         (prog
            (off "Complete")
            (do 3 (insChar " ")) )
         (ifn
            (default "Complete"
               (let "N" (inc (length "S"))
                  (mapcar
                     '((X)
                        (setq X
                           (nth
                              (mapcan
                                 '((C)
                                    (if (or (= "\\" C) (delim? C))
                                       (list "\\" C)
                                       (cons C) ) )
                                 (chop X) )
                              "N" ) )
                        (cons
                           (+ "LPos" (length X))
                           (append "L" X (nth "Line" "LPos")) ) )
                     ("tabFun" (pack "S")) ) ) )
            (beep)
            (chgLine (cdar "Complete") (caar "Complete"))
            (rot "Complete") ) ) ) )

# Insert mode
(de insMode ("C")
   (if (= "C" "^I")
      (expandTab)
      (off "Complete")
      (case "C"
         (("^H" "^?")
            (when (> "LPos" 1)
               (chgLine (remove (dec "LPos") "Line") (dec "LPos")) ) )
         ## ("^V" (insChar (key)))

         # 'M-<char>' (Meta or Alt) keymap, implemented with ESC prefix
         ("^[" (and (key 500)
                  (case @
                     # forward-word
                     # TODO: emacs  (goto end of word!)
                     ("f" (rWord))
                     # backward-word
                     ("b" (lWord))
                     # kill-word
                     ("d" (doUndo) (delWord T))
                     # toggle case of char
                     # TODO: capitalize/downcase/upcase word
                     ((or "c" "l")
                        (doUndo)
                        (rplChar
                             ((if
                                 (low? (setq "C" (get "Line" "LPos")))
                                 uppc lowc ) "C" ) )
                        (rMove) )
                     # forward-sexp
                     ("^f"
                      (case (get "Line" "LPos")
                         ("(" (rPar))
                         (T (beep)) ) )
                     # backward-sexp
                     ("^b"
                      (case (get "Line" "LPos")
                         (")" (lPar))
                         (T (beep)) ) )
                     # show present working directory (pwd)
                     ("^d" (prinl (pwd)) (quit))
                     # goto/find char
                     ("g"
                        (ifn (setq "C" (index (key) (nth "Line" (inc "LPos"))))
                           (beep)
                           (chgLine "Line" (+ "C" "LPos")) ) )
                     # accept input pattern for history search
                     ("^s"
                        (let "L" "Line"
                           (_getLine '("/") '((C) (= C "/")))
                           (unless (=T "Line")
                              (setq "Item" (append '(@) (cdr "Line") '(@)))
                              (ledSearch "L")
                              ## (off "Insert")
                              ) ) )
                     # search for next occurrence of pattern
                     # in history-search
                     ("s" (ledSearch "Line"))
                     # search for previous occurrence of pattern
                     # in history-search
                     ("r" (if "Found" (setHist (pop '"Found")) (beep))) ) ) )

         # 'C-c' (Ctrl-c) keymap
         ("^c"  (and (key 1000)
                  (case @
                     # change directory
                     ("^d"
                        (prinl "[(pwd) " (pwd) "]")
                        (prin "(cd) ")
                        (cd (read)) (quit) )
                     # make directory (with parents)
                     ("+"
                        (prinl "[(pwd) " (pwd) "]")
                        (prin "(mkdir -p) ")
                        (call 'mkdir (read) "-p") (quit) )
                     # call shell-command with arguments
                     (("^c" "!")
                      (prin "[cmd -args] ")
                      (eval
                         (append '(call)
                            (mapcar pack
                               (split (chop (line T)) " " ) ) ) )
                      (quit) ) ) ) )

         # 'C-u (Ctrl-u) keymap (functions with arguments)
         ("^u" (and (key 1000)
                 (case @
                    # list directory files
                    # (including those starting with .)
                     ("^x" (and (key 500)
                              (case @
                                 ("^d"
                                  (printsp (dir (pwd) T))
                                  (prinl) (quit) ) ) ) ) ) ) )

         # 'C-x' (Ctrl-x) keymap
         ("^x" (and (key 500)
                  (case @
                     # undo
                     ("u"
                        (let ("L" "Line" "P" "LPos")
                           (chgLine "UndoLine" "UndoPos")
                           (setq "UndoLine" "L" "UndoPos" "P") ) )
                     # list directory files
                     ("^d" (printsp (dir (pwd))) (prinl) (quit))
                     # find-file (with ZILE)
                     ("f"
                      (use X
                        (prinl "[(pwd) " (pwd) "]")
                        (prin "(zile) ")
                        (call 'zile (read)) (quit) ) )
                     # edit file (with EMACSCLIENT)
                     ## ("^f" (edit ) )
 ) ) )
                        ## (case @
                        ##    ((call 'test "-f" X)
                        ##     (call 'zile X) (quit) )
                        ##    ((call 'test "-d" X)
                        ##     (prinl "Can't open directory") (quit) )
                        ##    (T (case @
                        ##          ((call 'test "-d" (dirname X))
                        ##           (chdir (dirname X)
                        ##              (out (basename X)
                        ##              (call -zile X) )
                        ##              (quit) ) )
                        ##          (T (call 'mkdir (dirname X) "-p")
                        ##             (chdir (dirname X)
                        ##                (out (basename X)) ) ) ) ) ) ) ) ) ) )

         # 'C-h' (Ctrl-h) keymap (info/help functionality)
         ("^h" (and (key 500)
                  (case @
                     # display current contents of
                     # kill-ring (cut buffer)
                    ("r" (prinl) (println "Clip")) ) ) )

         # 'C-v' (Ctrl-v) keymap
         ## ("^v" (and (key 500)
         ##          (case @
         ##             # display current contents of
         ##             # kill-ring (cut buffer)
         ##            ("r" (prinl) (println "Clip")) ) ) )

         # undo
         ("^_" (let ("L" "Line" "P" "LPos")
                  (chgLine "UndoLine" "UndoPos")
                  (setq "UndoLine" "L" "UndoPos" "P") ) )
         # move-end-of-line
         ("^e" (eMove))
         # move-beginning-of-line
         ("^a" (bMove))
         # kill-line
         ("^k" (doUndo) (clrEol))
         # backward-char
         ("^b" (lMove))
         # forward-char
         ("^f" (and (= "LPos" (length "Line"))(xMove))(rMove))
         # next-line
         ("^n" (unless (=0 "HPos") (setHist (dec "HPos"))))
         # previous-line
         ("^p" (when (< "HPos" (length "History")) (setHist (inc "HPos"))))
         # yank
         ("^y" (doUndo) (doPaste))
         # delete-char
         ("^D" (doUndo) (delChar))
         # self-insertion
         (T
            (when (= "C" ")")
               (chgLine "Line" (prog1 "LPos" (lPar) (wait 200))) )
            (insChar  "C") ) ) ) )

# Command mode
## (de cmdMode ("C")
## (case "C"
## ("g" (prinl) (println "Clip"))
## ("$" (eMove))
## ("%"
## (case (get "Line" "LPos")
## (")" (lPar))
## ("(" (rPar))
## (T (beep)) ) )
## ("/"
## (let "L" "Line"
## (_getLine '("/") '((C) (= C "/")))
## (unless (=T "Line")
## (setq "Item" (append '(@) (cdr "Line") '(@)))
## (ledSearch "L")
## (off "Insert") ) ) )
## ("0" (bMove))
## ("A" (doUndo) (xMove) (on "Insert"))
## ("a" (doUndo) ((if (= "LPos" (length "Line")) xMove rMove)) (on "Insert"))
## ("b" (lWord))
## ("c" (doUndo) (delWord NIL) (on "Insert"))
## ("C" (doUndo) (clrEol) (xMove) (on "Insert"))
## ("d" (doUndo) (delWord T))
## ("D" (doUndo) (clrEol))
## ("f"
## (ifn (setq "C" (index (key) (nth "Line" (inc "LPos"))))
## (beep)
## (chgLine "Line" (+ "C" "LPos")) ) )
## ("h" (lMove))
## ("i" (doUndo) (on "Insert"))
## ("I" (doUndo) (bMove) (on "Insert"))
## ("j" (unless (=0 "HPos") (setHist (dec "HPos"))))
## ("k" (when (< "HPos" (length "History")) (setHist (inc "HPos"))))
## ("l" (rMove))
## ("n" (ledSearch "Line"))
## ("N" (if "Found" (setHist (pop '"Found")) (beep)))
## ("p" (doUndo) ((if (= "LPos" (length "Line")) xMove rMove)) (doPaste))
## ("P" (doUndo) (doPaste))
## ("r" (ifn "Line" (beep) (doUndo) (rplChar (key))))
## ("s" (doUndo) (delChar) (on "Insert"))
## ("S" (doUndo) (chgLine NIL 1) (on "Insert"))
## ("U" (setHist "HPos"))
## ("u"
## (let ("L" "Line" "P" "LPos")
## (chgLine "UndoLine" "UndoPos")
## (setq "UndoLine" "L" "UndoPos" "P") ) )
## ("w" (rWord))
## ("x" (doUndo) (delChar))
## ("X" (lMove) (doUndo) (delChar))
## ("~"
## (doUndo)
## (rplChar
## ((if (low? (setq "C" (get "Line" "LPos"))) uppc lowc) "C") )
## (rMove) )
## (T (beep)) ) )

# Get a line from console
(de _getLine ("L" "skipFun")
   (use "C"
      (chgLine "L" (inc (length "L")))
      (on "Insert")
      (until
         (member
            (setq "C" (let *Dbg "*Dbg" (key)))
            '("^J" "^M") )
         (case "C"
            (NIL (bye))
            ## ("^D" (prinl) (bye))
            ("^Q" (prinl) (bye))
            ## ("^X" (prin (cdr *Tsm)) (prinl) (quit)) )
            ("^G" (prin (cdr *Tsm)) (prinl) (quit)) )
         ((if "Insert" insMode insMode) "C") ) ) ) # only insert mode for emacs
         ## ((if "Insert" insMode cmdMode) "C") ) ) )

# Function keys
(de fkey (Key . Prg)
   (setq "FKey"
      (cond
         ((not Key) "FKey")
         ((not Prg) (delete (assoc Key "FKey") "FKey"))
         ((assoc Key "FKey")
            (cons (cons Key Prg) (delete @ "FKey")) )
         (T (cons (cons Key Prg) "FKey")) ) ) )

# Main editing functions
(de _led ("Line1" "tabFun" "skipFun")
   (default "tabFun"
      '((S)
         (conc
            (filter '((X) (pre? S (sym X))) (all))
            (let P (rot (split (chop S) "/"))
               (setq
                  S (pack (car P))
                  P (and (cdr P) (pack (glue "/" @) "/")) )
               (extract
                  '((X)
                     (and (pre? S X) (pack P X)) )
                  (dir P T) ) ) ) ) )
   (setq "LPos" 1 "HPos" 0)
   (_getLine "Line1" (or "skipFun" delim?))
   (prinl (cdr *Tsm)) )

(de revise ("X" "tabFun" "skipFun")
   (let ("*Dbg" *Dbg *Dbg NIL)
      (_led (chop "X") "tabFun" "skipFun")
      (pack "Line") ) )

(de saveHistory ()
   (in (pack "+" (pil "history"))
      (ctl T
         (let (Old (make (until (eof) (link (line T)))) New "History" N "HistMax")
            (out (pil "history")
               (while (and New (n== New "Hist0"))
                  (prinl (pop 'New))
                  (dec 'N) )
               (setq "Hist0" "History")
               (do N
                  (NIL Old)
                  (prinl (pop 'Old)) ) ) ) ) ) )

# Enable line editing
(de *Led
   (let ("*Dbg" *Dbg *Dbg NIL)
      (push1 '*Bye '(saveHistory))
      (push1 '*Fork '(del '(saveHistory) '*Bye))
      (_led)
      (let L (pack "Line")
         (or
            (>= 3 (length "Line"))
            (sp? (car "Line"))
            (= L (car "History"))
            (push '"History" L) )
         (and (nth "History" "HistMax") (con @))
         L ) ) )

### new Key codes (additional to lib/term.l) ###
## (setq
## *XtMf (in '("tput" "\033") (line T))
## *XtMb (in '("tput" "\b") (line T))
## *XtMd (in '("tput" "\d") (line T)) )

## (fkey *XtMf (rWord))
## (fkey *XtMb (lWord))
## (fkey *XtMd (doUndo) (delWord T))

(mapc zap
   (quote
      chgLine skipQ escQ delim? lMove bMove rMove eMove xMove lWord rWord lPar rPar
      clrEol insChar del1 delChar delWord rplChar doUndo doPaste
      setHist ledSearch expandTab insMode _getLine _led saveHistory ) )

# vi:et:ts=3:sw=3
