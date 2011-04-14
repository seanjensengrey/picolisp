# 14apr11abu
# (c) Software Lab. Alexander Burger

# Byte order
(off *LittleEndian)

# Register assignments
(de *Registers
   (A . 3) (C . 14) (E . 15)
   (B . -3) (D 3 . 14)
   (X . 16) (Y . 17) (Z . 18)
   (L . 19) (S . 1)
   (F . T) )

(de *TempRegs
   26 27 28 29 30 )

# TOC: 2
# C arguments: 3 - 10
# NULL: 20
# ONE: 21
# Data: 22
# Operands: 23, 24, 25
# Carry flag: 31

# Temporary register
(de tmpReg @
   (let R (pop '(`(apply circ *TempRegs)))
      (if (find lt0 (rest))
         (- R)
         R ) ) )

# Machine specific
(de boxfun (Adr)
   (pack Adr "+2") )

# Addressing modes
(de uimm16 (N)
   (or
      (ge0 (setq N (format N)))
      (+ 65536 N) ) )

(de checkOp (Fun)
   (unless (Fun Op)
      (quit "Illegal operation" *Statement) ) )

(de opReg (Op Reg Ofs R)
   (let Adr (pack Ofs "(" R ")")
      (cond
         ((lt0 Reg)
            (checkOp bool)
            (cond
               ((=0 Op)
                  (if (= -3 Reg)
                     (let Byte (tmpReg)
                        (prinst "lbz" Byte Adr)
                        (prinst "insrdi" 3 Byte 8 56) )
                     (prinst "lbz" (abs Reg) Adr) ) )
               ((=T Op) (prinst "stb" (abs Reg) Adr))
               (T (prinst Op (abs Reg) Adr)) ) )
         ((not Op)
            (unless (and (=0 Ofs) (= Reg R))
               (prinst "la" Reg Adr) ) )
         ((=0 Op) (prinst "ld" Reg Adr))
         ((=T Op) (prinst "std" Reg Adr))
         (T (prinst Op Reg Adr)) )
      (cons Adr) ) )

(de opxReg (Op Reg R R2)
   (let Adr (pack R ", " R2)
      (cond
         ((lt0 Reg)
            (checkOp bool)
            (cond
               ((=0 Op)
                  (if (= -3 Reg)
                     (let Byte (tmpReg)
                        (prinst "lbzx" Byte Adr)
                        (prinst "insrdi" 3 Byte 8 56) )
                     (prinst "lbzx" (abs Reg) Adr) ) )
               ((=T Op) (prinst "stbx" (abs Reg) Adr))
               (T (prinst (pack Op "x") (abs Reg) Adr)) ) )
         ((not Op) (prinst "add" Reg Adr))
         ((=0 Op) (prinst "ldx" Reg R R2))
         ((=T Op) (prinst "stdx" Reg Adr))
         (T (prinst (pack Op "x") Reg Adr)) )
      (cons Adr "x") ) )

(de mvReg (Dst Src)
   (if (or (lt0 Dst) (lt0 Src))
      (prinst "insrdi" (abs Dst) (abs Src) 8 56)
      (prinst "mr" Dst Src) ) )

# Operation 'Op':
#  NIL   Lea
#  0     Fetch
#  T     Store
(de memory (Mem M Reg Op Tmp)  #> ([adr [. "x"]])
   (cond
      ((=0 M)  # Immediate
         (checkOp =0)
         (if (= "0" Mem)
            (if (lt0 Reg)
               (prinst "insrdi" (abs Reg) 20 8 56)
               (prinst "li" Reg 0) )
            (setq Mem
               (if (pre? "~" Mem)
                  (x| `(hex "FFFFFFFFFFFFFFFF") (format (cdr (chop Mem))))
                  (format Mem) ) )
            (cond
               ((lt0 Reg)
                  (prinst "insrdi" (abs Reg) 20 8 56)
                  (prinst "ori" (abs Reg) (abs Reg) (& 255 Mem)) )
               ((>= 32767 Mem -32768)
                  (prinst "li" Reg Mem) )
               ((>= 2147483647 Mem -2147483648)
                  (prinst "lis" Reg (>> 16 Mem))
                  (unless (=0 (setq Mem (& 65535 Mem)))
                     (prinst "ori" Reg Reg Mem) ) )
               (T
                  (prinst "lis" Reg (>> 48 Mem))
                  (unless (=0 (& 65535 (>> 32 Mem)))
                     (prinst "ori" Reg Reg (& 65535 (>> 32 Mem))) )
                  (prinst "sldi" Reg Reg 32)
                  (prinst "lis" Reg (& 65535 (>> 16 Mem)))
                  (unless (=0 (setq Mem (& 65535 Mem)))
                     (prinst "ori" Reg Reg Mem) ) ) ) )
         NIL )
      ((not M)  # Register
         (cond
            ((not Reg) (setq Reg Mem))
            ((= Mem Reg))
            ((not Op) (prinst "mr" Reg Mem))
            ((=0 Op) (mvReg Reg Mem))
            ((=T Op) (mvReg Mem Reg))
            (T (prinst Op Reg Mem)) )
         NIL )
      ((=T M)  # Direct
         (cond
            ((sub? "-" Mem)  # Label difference
               (checkOp =0)
               (prinst "li" Reg Mem)
               NIL )
            ((or *FPic (low? Mem))  # -fpic or code label
               (prinst "ld" Reg (pack Mem "@got(2)")) )
            (T (opReg NIL Reg (pack Mem "-Data") 22)) ) )
      ((not (car M))  # Indexed
         (cond
            ((not (cdr M)) (opReg Op Reg 0 (car Mem)))
            ((=0 (cdr M))
               (if (>= 32767 (cdr Mem) -32768)
                  (opReg Op Reg (cdr Mem) (car Mem))
                  (prinst "lis" (abs Reg) (>> 16 (cdr Mem)))
                  (unless (=0 (& 65535 (cdr Mem)))
                     (prinst "ori" (abs Reg) (abs Reg) (& 65535 (cdr Mem))) )
                  (opxReg Op Reg Reg (car Mem)) ) )
            ((=T (cdr M))
               (cond
                  ((sub? "-" (cdr Mem))  # Label difference
                     (opReg Op Reg (cdr Mem) (car Mem)) )
                  ((or *FPic (low? (cdr Mem)))  # -fpic or code label
                     (let R (if (lt0 Reg) (tmpReg) Reg)
                        (prinst "ld" R (pack (cdr Mem) "@got(2)"))
                        (opxReg Op Reg R (car Mem)) ) )
                  (T
                     (let R (if (lt0 Reg) (tmpReg) Reg)
                        (prinst "la" R (pack (cdr Mem) "-Data(22)"))
                        (opxReg Op Reg R (car Mem)) ) ) ) ) ) )
      ((=T (car M))  # Indirect
         (if (or *FPic (low? (car Mem)))  # -fpic or code label
            (let R (if (lt0 Reg) (tmpReg) Reg)
               (prinst "ld" R (pack (car Mem) "@got(2)"))
               (opReg 0 Reg 0 R) )
            (opReg Op Reg
               (pack
                  (and (cdr M) (pack (cdr Mem) "+"))
                  (car Mem)
                  "-Data" )
               22 ) ) )
      (T  # Combined
         (let R (or Tmp (tmpReg))
            (memory (car Mem) (car M) R 0 R)
            (opReg Op Reg (or (cdr Mem) 0) R) ) ) ) )

(de memory2 (Cmd Reg Ref Ofs)
   (prinst
      (pack (if (lt0 Reg) "stb" Cmd) (cdr Ref))
      (abs Reg)
      (if Ofs
         (pack @ "+" (car Ref))
         (car Ref) ) ) )

(de srcReg (Src S Tmp)  #> reg
   (cond
      ((not S)
         (ifn Tmp
            Src
            (prinst "mr" Tmp Src)
            Tmp ) )
      ((= "0" Src)
         (ifn Tmp
            20
            (prinst "li" Tmp 0)
            Tmp ) )
      ((= "1" Src)
         (ifn Tmp
            21
            (prinst "li" Tmp 1)
            Tmp ) )
      (T
         (prog1 (or Tmp (tmpReg))
            (memory Src S @ 0) ) ) ) )

(de srcByteReg (Src S)  #> reg
   (cond
      ((not S)
         (prog1 (tmpReg)
            (prinst "insrdi" @ (abs Src) 8 56) ) )
      ((n0 S)
         (prog1 (tmpReg)
            (memory Src S @ "lbz") ) )
      ((= "0" Src) 20)
      ((= "1" Src) 21)
      (T
         (prog1 (tmpReg)
            (prinst "li" @
               (if (pre? "~" Src)
                  (x| `(hex "FF") (format (cdr (chop Src))))
                  (format Src) ) ) ) ) ) )

(de dstReg (Dst D)  #> (NIL dst adr [. "x"])
   (cond
      (D
         (let R (tmpReg)
            (cons NIL R (memory Dst D R 0)) ) )
      ((= -3 Dst)
         (let R (tmpReg)
            (prinst "extrdi" R 3 8 56)
            (cons NIL R -3) ) )
      (T (list NIL Dst)) ) )

(de dstByteReg (Dst D)  #> (T dst adr [. "x"])
   (cond
      (D
         (let R (tmpReg)
            (cons T R (memory Dst D R "lbz")) ) )
      ((= -3 Dst)
         (let R (tmpReg)
            (prinst "extrdi" R 3 8 56)
            (cons T R -3) ) )
      (T (list T Dst)) ) )

(de dstSrcReg (Dst D Src S)  #> (src flg dst adr [. "x"])
   (if (or (= -3 Dst) (= -3 Src))
      (cons
         (srcByteReg Src S)
         (dstByteReg Dst D) )
      (cons
         (srcReg Src S)
         (dstReg Dst D) ) ) )

(de regDst (RegRef)
   (cond
      ((= -3 (cddr RegRef))
         (prinst "insrdi" 3 (cadr RegRef) 8 56) )
      ((car RegRef)  # byte-flg
         (when (cddr RegRef)
            (memory2 "stb" (cadr RegRef) (cddr RegRef)) ) )
      ((cddr RegRef)
         (memory2 "std" (cadr RegRef) (cddr RegRef)) ) ) )

### Instruction set ###
(asm nop ()
   (prinst "nop") )

(asm align (N)
   (prinst ".balign" N) )

(asm skip (N)
   (when (== 'data *Section)
      (or (=0 N) (prinst ".space" N)) ) )

(asm ld (Dst D Src S)
   (nond
      (D
         (ifn (= (3 . 14) Dst)
            (memory Src S Dst 0)
            (let A (memory Src S 14 0)  # D
               (prinst "ld" 3 (pack "8+" (car A))) ) ) )
      (S
         (ifn (= (3 . 14) Src)
            (memory Dst D Src T)
            (let A (memory Dst D 14 T)  # D
               (prinst "std" 3 (pack "8+" (car A))) ) ) )
      (NIL
         (let R (tmpReg)
            (memory Src S R 0)
            (memory Dst D R T) ) ) ) )

(asm ld2 (Src S)
   (memory Src S 3 "lhz") )

(asm ld4 (Src S)
   (memory Src S 3 "lwz") )

(asm ldc (Dst D Src S)
   (prinst "cmpdi" "cr1" 31 -2)
   (prinst "beq-" "cr1" "1f")
   (memory Src S Dst 0)
   (prinl "1:") )

(asm ldnc (Dst D Src S)
   (prinst "cmpdi" "cr1" 31 -2)
   (prinst "bne-" "cr1" "1f")
   (memory Src S Dst 0)
   (prinl "1:") )

(asm ldz (Dst D Src S)
   (prinst "bne-" "1f")
   (memory Src S Dst 0)
   (prinl "1:") )

(asm ldnz (Dst D Src S)
   (prinst "beq-" "1f")
   (memory Src S Dst 0)
   (prinl "1:") )

(asm lea (Dst D Src S)
   (ifn D
      (memory Src S Dst)
      (let R (tmpReg)
         (memory Src S R)
         (memory Dst D R T) ) ) )

(asm st2 (Dst D)
   (memory Dst D 3 "sth") )

(asm st4 (Dst D)
   (memory Dst D 3 "stw") )

(asm xchg (Dst D Dst2 D2)
   (let (Tmp (tmpReg Dst Dst2)  A (memory Dst D Tmp 0))  # Tmp = Dst
      (nond
         (D
            (if (memory Dst2 D2 Dst 0)                   # Dst = Dst2
               (memory2 "std" Tmp @)                     # Dst2 = Tmp
               (mvReg Dst2 Tmp) ) )
         (D2
            (memory2 "std" Dst2 A)
            (mvReg Dst2 Tmp) )
         (NIL
            (let (R (tmpReg)  B (memory Dst2 D2 R 0))
               (memory2 "std" R A)
               (memory2 "std" Tmp B) ) ) ) ) )

(asm movn (Dst D Src S Cnt C)
   (memory Dst D 23)
   (memory Src S 24)
   (memory Cnt C 25 0)
   (prinst "bl" "call")
   (prinst ".int" "movn-.") )

(asm mset (Dst D Cnt C)
   (memory Dst D 23)
   (memory Cnt C 24 0)
   (prinst "bl" "call")
   (prinst ".int" "mset-.") )

(asm movm (Dst D Src S End E)
   (memory Dst D 25)
   (memory Src S 23)
   (memory End E 24)
   (prinst "bl" "call")
   (prinst ".int" "save-.") )

(asm save (Src S End E Dst D)
   (memory Src S 23)
   (memory End E 24)
   (memory Dst D 25)
   (prinst "bl" "call")
   (prinst ".int" "save-.") )

(asm load (Dst D End E Src S)
   (memory Dst D 23)
   (memory End E 24)
   (memory Src S 25)
   (prinst "bl" "call")
   (prinst ".int" "load-.") )

# Arithmetics
(asm add (Dst D Src S)
   (ifn (= (3 . 14) Dst)
      (if (and (=0 S) (>= 32767 (format Src) -32768))
         (let A (dstReg Dst D)
            (prinst "addic." (cadr A) (cadr A) Src)
            (regDst A) )
         (let A (dstSrcReg Dst D Src S)
            (prinst "addc." (caddr A) (caddr A) (car A))
            (regDst (cdr A)) ) )
      (if (and (=0 S) (>= 32767 (format Src) -32768))
         (prinst "addic" 3 3 Src)
         (prinst "addc" 3 3 (srcReg Src S)) )
      (prinst "addze" 14 14) )
   (prinst "subfze" 31 21) )  # Set carry

(asm addc (Dst D Src S)
   (prinst "sradi" 0 31 1)  # Get carry
   (ifn (= (3 . 14) Dst)
      (let A (dstSrcReg Dst D Src S)
         (prinst "adde." (caddr A) (caddr A) (car A))
         (regDst (cdr A)) )
      (prinst "adde" 3 3 (srcReg Src S))
      (prinst "adde" 14 14 20) )
   (prinst "subfze" 31 21) )  # Set carry

(asm sub (Dst D Src S)
   (ifn (= (3 . 14) Dst)
      (if (and (=0 S) (>= 32767 (format Src) -32768))
         (let A (dstReg Dst D)
            (prinst "subic." (cadr A) (cadr A) Src)
            (regDst A) )
         (let A (dstSrcReg Dst D Src S)
            (prinst "subc." (caddr A) (caddr A) (car A))
            (regDst (cdr A)) ) )
      (if (and (=0 S) (>= 32767 (format Src) -32768))
         (prinst "subic" 3 3 Src)
         (prinst "subc" 3 3 (srcReg Src S)) )
      (prinst "subfze" 14 14) )
   (prinst "subfme" 31 21) )  # Set inverted carry

(asm subc (Dst D Src S)
   (prinst "xori" 0 31 1)  # Get inverted carry
   (prinst "sradi" 0 0 1)
   (ifn (= (3 . 14) Dst)
      (let A (dstSrcReg Dst D Src S)
         (prinst "subfe." (caddr A) (car A) (caddr A))
         (regDst (cdr A)) )
      (prinst "sube" 3 3 (srcReg Src S))
      (prinst "sube" 14 14 20) )
   (prinst "subfme" 31 21) )  # Set inverted carry

(asm inc (Dst D)
   (let A (dstReg Dst D)
      (prinst "addic." (cadr A) (cadr A) 1)
      (regDst A) ) )

(asm dec (Dst D)
   (let A (dstReg Dst D)
      (prinst "subic." (cadr A) (cadr A) 1)
      (regDst A) ) )

(asm not (Dst D)
   (let A (dstReg Dst D)
      (prinst "not." (cadr A) (cadr A))
      (regDst A) ) )

(asm neg (Dst D)
   (let A (dstReg Dst D)
      (prinst "neg." (cadr A) (cadr A))
      (regDst A) ) )

(asm and (Dst D Src S)
   (if (and (=0 S) (>= 32767 (format Src) -32768))
      (let A (dstReg Dst D)
         (prinst "andi." (cadr A) (cadr A) (uimm16 Src))
         (regDst A) )
      (let A (dstSrcReg Dst D Src S)
         (prinst "and." (caddr A) (caddr A) (car A))
         (regDst (cdr A)) ) ) )

(asm or (Dst D Src S)
   (let A (dstSrcReg Dst D Src S)
      (prinst "or." (caddr A) (caddr A) (car A))  # 'ori.' doesn't exist
      (regDst (cdr A)) ) )

(asm xor (Dst D Src S)
   (let A (dstSrcReg Dst D Src S)
      (prinst "xor." (caddr A) (caddr A) (car A))  # 'xori.' doesn't exist
      (regDst (cdr A)) ) )

(asm off (Dst D Src S)
   (let A (dstReg Dst D)
      (prinst "andi." (cadr A) (cadr A)
         (x| `(hex "FFFF") (format (cdr (chop Src)))) )
      (regDst A) ) )

(asm test (Dst D Src S)
   (if (and (=0 S) (>= 32767 (format Src) -32768))
      (let A (dstReg Dst D)
         (prinst "andi." 0 (cadr A) (uimm16 Src)) )
      (let A (dstSrcReg Dst D Src S)
         (prinst "and." 0 (caddr A) (car A)) ) ) )

(asm shl (Dst D Src S)
   (ifn (=0 S)
      (let A (dstSrcReg Dst D Src S)
         (prinst "sld." (caddr A) (caddr A) (car A))  # Ignore carry
         (regDst (cdr A)) )
      (let A (dstReg Dst D)
         (when (gt0 (dec (format Src)))
            (prinst "sldi" (cadr A) (cadr A) @) )
         (prinst "addc." (cadr A) (cadr A) (cadr A))
         (regDst A)
         (prinst "subfze" 31 21) ) ) )  # Set carry from MSB

(asm shr (Dst D Src S)
   (ifn (=0 S)
      (let A (dstSrcReg Dst D Src S)
         (prinst "srd." (caddr A) (caddr A) (car A))  # Ignore carry
         (regDst (cdr A)) )
      (let A (dstReg Dst D)
         (when (gt0 (dec (format Src)))
            (prinst "srdi" (cadr A) (cadr A) @) )
         (prinst "li" 31 -2)  # Set carry from LSB
         (prinst "insrdi" 31 (cadr A) 1 0)
         (prinst "srdi." (cadr A) (cadr A) 1)
         (regDst A) ) ) )

(asm rol (Dst D Src S)
   (ifn (=0 S)
      (let A (dstSrcReg Dst D Src S)
         (prinst "rotld" (caddr A) (caddr A) (car A))
         (regDst (cdr A)) )
      (let A (dstReg Dst D)
         (prinst "rotldi" (cadr A) (cadr A) (format Src))
         (regDst A) ) ) )

(asm ror (Dst D Src S)
   (ifn (=0 S)
      (quit "Non-immediate 'ror' not available")
      (let A (dstReg Dst D)
         (prinst "rotrdi" (cadr A) (cadr A) (format Src))
         (regDst A) ) ) )

(asm rcl (Dst D Src S)
   (ifn (=0 S)
      (quit "Non-immediate 'rcl' not available")
      (let A (dstReg Dst D)
         (prinst "sradi" 0 31 1)  # Get carry
         (do (setq Src (format Src))
            (prinst "adde." (cadr A) (cadr A) (cadr A)) )
         (regDst A)
         (prinst "subfze" 31 21) ) ) )  # Set carry

(asm rcr (Dst D Src S)
   (ifn (=0 S)
      (quit "Non-immediate 'rcr' not available")
      (let A (dstReg Dst D)
         (let (R1 (tmpReg)  R2 (tmpReg))
            (do (setq Src (format Src))
               (prinst "mr" 0 (cadr A))
               (prinst "rotrdi" (cadr A) (cadr A) 1)
               (prinst "insrdi" (cadr A) 31 1 0)
               (prinst "insrdi" 31 0 1 63) ) ) )
      (regDst A) ) )

(asm mul (Src S)
   (let R (srcReg Src S)
      (prinst "mulhdu" 14 3 R)
      (prinst "mulld" 3 3 R) ) )

(asm div (Src S)
   (srcReg Src S 23)
   (prinst "bl" "call")
   (prinst ".int" "div-.") )

(asm zxt ()  # 8 bit -> 64 bit
   (prinst "andi." 3 3 255) )

(asm setz ()
   (prinst "addic." 0 20 0) )  # Add zero to null

(asm clrz ()
   (prinst "cmpdi" 1 0) )  # Compare stack pointer to zero

(asm setc ()
   (prinst "li" 31 -1) )

(asm clrc ()
   (prinst "li" 31 -2) )

# Comparisons
(asm cmp (Dst D Src S)
   (if (and (=0 S) (>= 32767 (format Src) -32768))
      (let A (dstReg Dst D)
         (prinst "subic." 0 (cadr A) Src) )
      (let A (dstSrcReg Dst D Src S)
         (prinst "subc." 0 (caddr A) (car A)) ) )
   (prinst "subfme" 31 21) )  # Set inverted carry

(asm cmp4 (Src S)
   (let R (tmpReg)
      (memory Src S R "lwz")
      (prinst "subc." 0 3 R) )
   (prinst "subfme" 31 21) )  # Set inverted carry

(asm cmpn (Dst D Src S Cnt C)
   (memory Dst D 23)
   (memory Src S 24)
   (memory Cnt C 25 0)
   (prinst "bl" "call")
   (prinst ".int" "cmpn-.") )

(asm slen (Dst D Src S)
   (memory Src S 24)
   (prinst "bl" "call")
   (prinst ".int" "slen-.")
   (memory Dst D 23 T) )

(asm memb (Src S Cnt C)
   (memory Src S 23)
   (memory Cnt C 24 0)
   (prinst "bl" "call")
   (prinst ".int" "memb-.")
   (unless S (prinst "mr" Src 23))
   (unless C (prinst "mr" Cnt 24)) )

(asm null (Src S)
   (prinst "cmpdi" (srcReg Src S) 0) )

(asm nul4 ()
   (prinst "extldi." 3 3 32 32) )

# Byte addressing
(asm set (Dst D Src S)
   (memory Dst D (srcByteReg Src S) "stb") )

(asm nul (Src S)
   (prinst "cmpdi" (srcByteReg Src S) 0) )

# Types
(asm cnt (Src S)
   (prinst "andi." 0 (srcReg Src S) "0x02") )

(asm big (Src S)
   (prinst "andi." 0 (srcReg Src S) "0x04") )

(asm num (Src S)
   (prinst "andi." 0 (srcReg Src S) "0x06") )

(asm sym (Src S)
   (prinst "andi." 0 (srcReg Src S) "0x08") )

(asm atom (Src S)
   (prinst "andi." 0 (srcReg Src S) "0x0E") )

# Flow Control
(asm call (Adr A)
   (nond
      (A  # Absolute
         (prinst "bl" "call")
         (prinst ".int" (pack Adr "-.")) )
      ((=T A)  # Indexed: Ignore SUBR
         (prinst "mtctr" Adr)
         (prinst "bctrl") )
      (NIL  # Indirect
         (let R (tmpReg)
            (prinst "ld" R (pack Adr "-Data(22)"))
            (prinst "mtctr" R)
            (prinst "bctrl") ) ) ) )

(de _jmp Args
   (nond
      (A
         (let @Lbl Adr
            (ifn (cadr Args)
               (for E (fill (car Args))  # b
                  (apply prinst (cons (pack (pop 'E) (pop 'E)) E)) )
               (let Back
                  (for (P *Program (n== *Statement (car P)) (cdr P))
                     (T (and (== ': (caar P)) (= Adr (cdar P))) T) )
                  (for E
                     (fill
                        ((if
                              (or
                                 (= `(char ".") (char Adr))  # Local label
                                 (and
                                    (cdr (split (chop Adr) "_"))
                                    (format @) ) )
                              car
                              cadr )
                           Args ) )
                     (apply prinst
                        (cons
                           (pack
                              (pop 'E)
                              (case (pop 'E)
                                 ("+" (if Back "-" "+"))
                                 ("-" (if Back "+" "-")) ) )
                           E ) ) ) ) ) ) )
      ((=T A)  # Ignore SUBR
         (prinst "mtctr" Adr)
         (for E (fill (caddr Args))
            (apply prinst (cons (pack (pop 'E) (pop 'E)) E)) ) )
      (NIL
         (let R (tmpReg)
            (prinst "ld" R (pack Adr "-Data(22)"))
            (prinst "mtctr" R)
            (for E (fill (caddr Args))
               (apply prinst (cons (pack (pop 'E) (pop 'E)) E)) ) ) ) ) )

(asm jmp (Adr A)
   (_jmp
      (("b" NIL @Lbl))
      NIL
      (("bctr" NIL)) ) )

(asm jz (Adr A)
   (_jmp
      (("beq" - @Lbl))
      (("bne" + ".+8") ("b" NIL @Lbl))
      (("beqctr" -)) ) )

(asm jeq (Adr A)
   (_jmp
      (("beq" - @Lbl))
      (("bne" + ".+8") ("b" NIL @Lbl))
      (("beqctr" -)) ) )

(asm jnz (Adr A)
   (_jmp
      (("bne" - @Lbl))
      (("beq" + ".+8") ("b" NIL @Lbl))
      (("bnectr" -)) ) )

(asm jne (Adr A)
   (_jmp
      (("bne" - @Lbl))
      (("beq" + ".+8") ("b" NIL @Lbl))
      (("bnectr" -)) ) )

(asm js (Adr A)
   (_jmp
      (("blt" - @Lbl))
      (("bge" + ".+8") ("b" NIL @Lbl))
      (("bltctr" -)) ) )

(asm jns (Adr A)
   (_jmp
      (("bge" - @Lbl))
      (("blt" + ".+8") ("b" NIL @Lbl))
      (("bgectr" -)) ) )

(asm jsz (Adr A)
   (_jmp
      (("ble" - @Lbl))
      (("bgt" + ".+8") ("b" NIL @Lbl))
      (("blectr" -)) ) )

(asm jnsz (Adr A)
   (_jmp
      (("bgt" - @Lbl))
      (("ble" + ".+8") ("b" NIL @Lbl))
      (("bgtctr" -)) ) )

(asm jc (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("bne" - "cr1" @Lbl))
      (("beq" + "cr1" ".+8") ("b" NIL @Lbl))
      (("bnectr" - "cr1")) ) )

(asm jlt (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("bne" - "cr1" @Lbl))
      (("beq" + "cr1" ".+8") ("b" NIL @Lbl))
      (("bnectr" - "cr1")) ) )

(asm jnc (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("beq" - "cr1" @Lbl))
      (("bne" + "cr1" ".+8") ("b" NIL @Lbl))
      (("beqctr" - "cr1")) ) )

(asm jge (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("beq" - "cr1" @Lbl))
      (("bne" + "cr1" ".+8") ("b" NIL @Lbl))
      (("beqctr" - "cr1")) ) )

(asm jcz (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("bne" - "cr1" @Lbl) ("beq" - @Lbl))
      (("beq" - "cr1" ".+12") ("bne" + ".+8") ("b" NIL @Lbl))
      (("bnectr" - "cr1") ("beqctr" -) ) ) )

(asm jle (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("bne" - "cr1" @Lbl) ("beq" - @Lbl))
      (("beq" - "cr1" ".+12") ("bne" + ".+8") ("b" NIL @Lbl))
      (("bnectr" - "cr1") ("beqctr" -) ) ) )

(asm jncz (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("bne" + "cr1" ".+8") ("bne" - @Lbl))
      (("bne" + "cr1" ".+12") ("beq" + ".+8") ("b" NIL @Lbl))
      (("bne" + "cr1" ".+8") ("bnectr" -)) ) )

(asm jgt (Adr A)
   (prinst "cmpdi" "cr1" 31 -2)
   (_jmp
      (("bne" + "cr1" ".+8") ("bne" - @Lbl))
      (("bne" + "cr1" ".+12") ("beq" + ".+8") ("b" NIL @Lbl))
      (("bne" + "cr1" ".+8") ("bnectr" NIL)) ) )

(asm dval ()
   # MADA
   )

(asm fval ()
   # MADA
   )

(asm fix ()
   # MADA
   )

(asm cc (Adr A Arg M)
   (let Reg (3 4 5 6 7 8 9 10)  # Support only max. 8 parameters
      (if (lst? Arg)
         (let (Lea NIL  Tmp NIL)
            (when (fish '((X) (= 3 X)) (cdr Arg))
               (prinst "mr" (setq Tmp (tmpReg)) 3) )
            (mapc
               '((Src S)
                  (if (== '& Src)
                     (on Lea)
                     (setq Src
                        (recur (Src)
                           (cond
                              ((= 3 Src) (or Tmp 3))
                              ((atom Src) Src)
                              (T (cons (recurse (car Src)) (recurse (cdr Src)))) ) ) )
                     (cond
                        ((not Reg)  # 'Src' not stack-relative here!
                           #{MADA}# )
                        ((and (=T S) (== 'pop Src))
                           (prinst "ld" (pop 'Reg) "0(1)")
                           (prinst "addi" 1 1 8) )
                        (Lea (memory Src S (pop 'Reg)))
                        ((= 3 Src) (pop 'Reg))
                        (T (srcReg Src S (pop 'Reg))) )
                     (off Lea) ) )
               Arg
               M ) )
         #{MADA}# ) )
   (nond
      (A  # Absolute
         (prinst "mflr" 23)
         (prinst "stdu" 1 "-112(1)")
         (prinst "bl" Adr)
         (prinst "nop")
         (prinst "addi" 1 1 112)
         (prinst "mtlr" 23) )
      ((=T A)  # Indexed
         (prinst "mtctr" Adr)
         (prinst "bctrl") ) )
   (and
      (lst? Arg)
      (gt0 (- (length Arg) 8))
      (prinst "addi" 1 1 (* @ 8)) ) )

(asm ret ()
   (prinst "blr") )

(asm begin (N)
   (when (>= N 6)                                  # Z
      (prinst "stdu" 18 "-8(1)")
      (prinst "mr" 18 8) )
   (when (>= N 5)                                  # Y
      (prinst "stdu" 17 "-8(1)")
      (prinst "mr" 17 7) )
   (when (>= N 4)                                  # X
      (prinst "stdu" 16 "-8(1)")
      (prinst "mr" 16 6) )
   (when (>= N 3)                                  # E
      (prinst "stdu" 15 "-8(1)")
      (prinst "mr" 15 5) )
   (when (>= N 2)                                  # C
      (prinst "stdu" 14 "-8(1)")
      (prinst "mr" 14 4) ) )

(asm return (N)
   (and (>= N 2) (prinst "ld" 14 "0(1)"))          # C
   (and (>= N 3) (prinst "ld" 15 "8(1)"))          # E
   (and (>= N 4) (prinst "ld" 16 "16(1)"))         # X
   (and (>= N 5) (prinst "ld" 17 "32(1)"))         # Y
   (and (>= N 6) (prinst "ld" 18 "48(1)"))         # Z
   (prinst "addi" 1 1 (* 8 (min 6 (dec N)))) )

# Stack Manipulations
(asm push (Src S)
   (ifn (=T Src)
      (prinst "stdu" (srcReg Src S) "-8(1)")
      (let R (tmpReg)
         (prinst "mfocrf" R 128)  # Get CR[0]
         (prinst "insrdi" R 31 1 63)  # Carry into LSB
         (prinst "stdu" R "-8(1)") ) ) )

(asm pop (Dst D)
   (cond
      (D
         (let R (tmpReg)
            (prinst "ld" R "0(1)")
            (memory Dst D R T) ) )
      ((=T Dst)
         (let R (tmpReg)
            (prinst "ld" R "0(1)")
            (prinst "insrdi" 31 R 1 0)  # Set carry from LSB
            (prinst "mtocrf" 128 R) ) )  # Set CR[0] (LT, GT, EQ, SO)
      (T (prinst "ld" Dst "0(1)")) )
   (prinst "addi" 1 1 8) )

(asm link ()
   (prinst "stdu" 19 "-8(1)")  # Push L
   (prinst "mr" 19 1) )

(asm tuck (Src S)
   (prinst "ld" 19 "0(1)")  # Get L
   (prinst "std" (srcReg Src S) "0(1)") )

(asm drop ()
   (prinst "ld" 1 "0(19)")  # Restore S
   (prinst "ld" 19 "0(1)")  # and L
   (prinst "addi" 1 1 8) )

# Evaluation
(asm eval ()
   (prinst "rldicl." 0 15 63 62)  # Number?
   (prinst "bne-" "2f")           # Yes: Skip
   (prinst "rldicl." 0 15 61 63)  # Symbol?
   (prinst "bne-" "1f")           # Yes: Get value
   (prinst "ld" 15 "0(15)")
   (prinst "b" "2f")              # and skip
   (prinl "1:")
   (prinst "bl" "call")           # Else evaluate list
   (prinst ".int" "evListE_E-.")
   (prinl "2:") )

(asm eval+ ()
   (prinst "rldicl." 0 15 63 62)  # Number?
   (prinst "bne-" "2f")           # Yes: Skip
   (prinst "rldicl." 0 15 61 63)  # Symbol?
   (prinst "bne-" "1f")           # Yes: Get value
   (prinst "ld" 15 "0(15)")
   (prinst "b" "2f")              # and skip
   (prinl "1:")
   (prinst "stdu" 19 "-8(1)")     # Else 'link'
   (prinst "mr" 19 1)
   (prinst "bl" "call")           # Evaluate list
   (prinst ".int" "evListE_E-.")
   (prinst "ld" 19 "0(1)")        # Pop L
   (prinst "addi" 1 1 8)
   (prinl "2:") )

(asm eval/ret ()
   (prinst "rldicl." 0 15 63 62)  # Number?
   (prinst "bnelr")               # Yes: Return
   (prinst "rldicl." 0 15 61 63)  # Symbol?
   (prinst "bne-" "1f")           # No: Evaluate list
   (prinst "ld" 15 "0(15)")       # Get value
   (prinst "blr")
   (prinl "1:")
   (prinst "b" "evListE_E") )

(asm exec (Reg)
   (prinl "1:")                   # do
   (prinst "ld" 15                # ld E (R)
      (pack "0(" Reg ")") )
   (prinst "andi." 0 15 "0x0E")   # atom E
   (prinst "bne+" "2f")
   (prinst "bl" "call")           # Evaluate list
   (prinst ".int" "evListE_E-.")
   (prinl "2:")
   (prinst "ld" Reg               # ld R (R CDR)
      (pack "8(" Reg ")") )
   (prinst "andi." 0 Reg "0x0E")  # atom R
   (prinst "beq+" "1b") )         # until nz

(asm prog (Reg)
   (prinl "1:")                   # do
   (prinst "ld" 15                # ld E (R)
      (pack "0(" Reg ")") )
   (prinst "andi." 0 15 "0x06")   # eval
   (prinst "bne-" "2f")
   (prinst "andi." 0 15 "0x08")
   (prinst "bne-" "2f")
   (prinst "ld" 15 "0(15)")
   (prinst "b" "2f")
   (prinst "bl" "call")           # Evaluate list
   (prinst ".int" "evListE_E-.")
   (prinl "2:")
   (prinst "ld" Reg               # ld R (R CDR)
      (pack "8(" Reg ")") )
   (prinst "andi." 0 Reg "0x0E")  # atom R
   (prinst "beq+" "1b") )         # until nz


# System
(asm initData ())

(asm initCode ()
   (prinl "# Subroutine-call emulation")
   (prinl "call:")
   (prinst "mflr" 11)          # Get return address
   (prinst "stdu" 11 "-8(1)")  # Save it
   (prinst "lwa" 0 "0(11)")    # Target offset
   (prinst "add" 0 0 11)
   (prinst "mtlr" 0)           # Call target
   (prinst "blrl")
   (prinst "ld" 11 "0(1)")     # Pop return address
   (prinst "addi" 1 1 8)
   (prinst "addi" 0 11 4)      # Update return address
   (prinst "mtctr" 0)          # Return
   (prinst "bctr")
   (prinl)
   (prinl "# movn dst src cnt")
   (prinl "movn:")
   (prinst "subi" 23 23 1)     # Adjust 'dst'
   (prinst "subi" 24 24 1)     # and 'src'
   (prinl "1:")
   (prinst "subic." 25 25 1)   # Decrement 'cnt'
   (prinst "bltlr")            # Return if done
   (prinst "lbzu" 26 "1(24)")  # Next byte from 'src'
   (prinst "stbu" 26 "1(23)")  # Write to 'dst'
   (prinst "b" "1b")
   (prinl)
   (prinl "# mset dst src cnt")
   (prinl "mset:")
   (prinst "subi" 23 23 1)     # Adjust 'dst'
   (prinl "1:")
   (prinst "subic." 24 24 1)   # Decrement 'cnt'
   (prinst "bltlr")            # Return if done
   (prinst "stbu" 3 "1(23)")   # Write B to 'dst'
   (prinst "b" "1b")
   (prinl)
   (prinl "# save src end dst")
   (prinl "save:")
   (prinst "subi" 23 23 8)     # Adjust 'src'
   (prinst "subi" 25 25 8)     # and 'dst'
   (prinl "1:")
   (prinst "ldu" 26 "8(23)")   # Next word from 'src'
   (prinst "cmpd" 23 24)       # Done?
   (prinst "beqlr")            # Yes: Return
   (prinst "stdu" 26 "8(25)")  # Write to 'dst'
   (prinst "b" "1b")
   (prinl)
   (prinl "# load dst end src")
   (prinl "load:")
   (prinst "subi" 23 23 8)     # Adjust 'dst'
   (prinst "subi" 25 25 8)     # and 'src'
   (prinl "1:")
   (prinst "ldu" 26 "8(25)")   # Next word from 'src'
   (prinst "cmpd" 25 24)       # Done?
   (prinst "beqlr")            # Yes: Return
   (prinst "stdu" 26 "8(23)")  # Write to 'dst'
   (prinst "b" "1b")
   (prinl)
   (prinl "# cmpn dst src cnt")
   (prinl "cmpn:")
   (prinst "lbz" 26 "0(23)")   # First byte from 'dst'
   (prinst "lbz" 27 "0(24)")   # First byte from 'src'
   (prinl "1:")
   (prinst "subc." 0 26 27)    # Same bytes?
   (prinst "bnelr")            # No: Return 'ne'
   (prinst "subic." 25 25 1)   # Decrement 'cnt'
   (prinst "beqlr")            # Return 'eq' if done
   (prinst "lbzu" 26 "1(23)")  # Next bytes
   (prinst "lbzu" 27 "1(24)")
   (prinst "b" "1b")
   (prinl)
   (prinl "# slen dst src")
   (prinl "slen:")
   (prinst "li" 23 0)          # Init 'dst' counter
   (prinst "lbz" 26 "0(24)")   # First byte from 'src'
   (prinl "1:")
   (prinst "cmpdi" 26 0)       # Done?
   (prinst "beqlr")            # Yes: Return
   (prinst "addi" 23 23 1)     # Increment 'cnt'
   (prinst "lbzu" 26 "1(24)")  # Next byte
   (prinst "b" "1b")
   (prinl)
   (prinl "# memb src cnt")
   (prinl "memb:")
   (prinst "lbz" 26 "0(23)")   # First byte from 'src'
   (prinst "insrdi" 25 3 8 56) # Get B
   (prinl "1:")
   (prinst "subic." 24 24 1)   # Decrement 'cnt'
   (prinst "bltlr")            # Return 'ne' if done
   (prinst "cmpd" 26 25)       # Found?
   (prinst "beqlr")            # Yes: Return 'eq'
   (prinst "lbzu" 26 "1(23)")  # Next byte
   (prinst "b" "1b")
   (prinl)
   (prinl "# div src")
   (prinl "div:")
   (prinst "divdu" 24 3 23)    # Only 64-bit division for now
   (prinst "mr" 3 24)          # Quotient
   (prinst "mulld" 14 24 23)   # Remainder
   (prinst "subf" 14 14 3)
   (prinl "blr") )

(asm initMain ()
   (prinst ".quad" "main1" ".TOC.@tocbase" 0)
   (prinl "main1:")
   (prinst "li" 20 0)                 # Init NULL register
   (prinst "li" 21 1)                 # Init ONE register
   (prinst "ld" 22 "Data@got(2)")     # Globals base
   (prinst "ld" 16 "0(4)")            # Get command in X
   (prinst "la" 17 "8(4)")            # argument vector in Y
   (prinst "subi" 3 3 1)              # and pointer to last argument in Z
   (prinst "sldi" 3 3 3)
   (prinst "add" 18 4 3) )

### Optimizer ###
# Replace the the next 'cnt' elements with 'lst'
(de optimize (L))  #> (cnt . lst)

# vi:et:ts=3:sw=3
