# 11may10abu
# (c) Software Lab. Alexander Burger

# Byte order
(on *LittleEndian)

# Register assignments
(de *Registers
   (A "HiA" . "%eax") (C "HiC" . "LoC") (E "HiE" . "%ebx")
   (B . "%al") (D "HiC" "LoC" "HiA" . "%eax")
   (X "HiX" . "LoX") (Y "HiY" . "LoY") (Z "HiZ" . "LoZ")
   (L . "%ebp") (S . "%esp")
   (F . T) )
# NULL: %edx
# Temporary + Block operations: %ecx %esi %edi

# Addressing modes
(de byteReg (Reg)
   (cdr
      (assoc Reg
         (quote
            (("HiA" . "%eax") . "%al")
            ("%al" . "%al")
            (("HiC" . "LoC") . "LoC")
            (("HiE" . "%ebx") . "%bl")
            (("%ecx" . "%ecx") . "%cl")
            (("%edx" . "%edx") . "%dl")
            (("HiX" . "LoX") . "LoX")
            (("HiY" . "LoY") . "LoY")
            (("HiZ" . "LoZ") . "LoZ")
            ("%ebp" . "%bp")
            ("%esp" . "%sp") ) ) ) )  # No %spl

(de byteVal (Adr)
   (or
      (byteReg Adr)  # Register
      (fin Adr) ) )  # Byte address

(de lowByte (Adr)
   (or
      (byteReg Adr)  # Register
      (fin Adr) ) )  # Word address

(de highWord (S)
   (cond
      ((= `(char "(") (char S))
         (pack "8" S) )
      ((>= `(char "9") (char S) `(char "0"))
         (pack "8+" S) )
      (T (pack S "+8")) ) )

(de immed32 (Src)
   (and
      (pair Src)
      (member (car Src) '("%edx" "$0" "$~0"))
      (setq Src (chop (cdr Src)))
      (= "$" (pop 'Src))
      (format
         (if (= "~" (car Src)) (cdr Src) Src) ) ) )

(de target (Adr F)
   (if
      (or
         (not *FPic)
         (= `(char ".") (char Adr))  # Local label ".1"
         (use (@L @N)
            (and
               (match '(@L "_" @N) (chop Adr))  # Local jump "foo_22"
               (= @L (chop *Label))
               (format @N) ) ) )
      Adr
      (ifn F
         (pack Adr "@plt")
         (prinst "mov" (pack Adr "@GOTPCREL(%eip)") "%esi")
         "(%esi)") ) )

(de src (Src S)
   (cond
      ((=0 S)                                         # Immediate
         (setq Src (cdr (chop Src)))
         (let (F (and (= "~" (car Src)) (pop 'Src))  N (format (chop Src)))
            (and (lt0 N) (inc 'N `(** 2 64)))
            (let (Hi (/ N `(** 2 32))  Lo (% N `(** 2 32)))
               (cons
                  (if (and (=0 Hi) (not F)) "%edx" (pack "$" F Hi))
                  (if (and (=0 Lo) (not F)) "%edx" (pack "$" F Lo)) ) ) ) )
      ((not S) Src)                                   # Register
      ((=T S)                                         # Direct
         (if (and *FPic (not (pre? "(" Src)))
            (pack Src "@GOTPCREL(%eip)")
            (cons "%edx" (pack "$" Src)) ) )
      ((not (car S))
         (let R (fin (car Src))
            (ifn (and *FPic (=T (cdr S)))
               (prog
                  (unless (pre? "%" R)
                     (prinst "mov" R (setq R "%esi")) )
                  (cons
                     (pack (cdr Src) (and (cdr Src) "+") "4(" R ")")
                     (pack (cdr Src) "(" R ")") ) )
               (prinst "add" (pack (cdr Src) "@GOTPCREL(%eip)") R)
               (cons "???" (pack "(" R ")")) ) ) )
      ((=T (car S))
         (ifn *FPic
            (let Ofs (and (cdr S) (pack "+" (cdr Src)))
               (cons (pack (car Src) Ofs "+4") (pack (car Src) Ofs)) )
            (prinst "mov" (pack (car Src) "@GOTPCREL(%eip)") "%esi")
            (cons
               (pack (cdr Src) (and (cdr Src) "+") "4(%esi)")
               (pack (cdr Src) "(%esi)") ) ) )
      (T
         (prinst "mov" (fin (src (car Src) (car S))) "%esi")
         (ifn (and *FPic (=T (cdr S)))
            (cons
               (pack (cdr Src) (and (cdr Src) "+") "4(%esi)")
               (pack (cdr Src) "(%esi)") )
            (prinst "add" (pack (cdr Src) "@GOTPCREL(%eip)") "%esi")
            (cons "4(%esi)" "(%esi)") ) ) ) )

(de lea (Src S Reg)
   (cond
      ((not S) (prinst "mov" (fin Src) Reg))  # Register
      ((=T S) (prinst "mov" (fin (src Src T)) Reg))  # Direct
      ((not (car S))
         (cond
            ((and *FPic (=T (cdr S)))
               (prinst "add" (pack (cdr Src) "@GOTPCREL(%eip)") (car Src))
               (prinst "mov" (pack "(" (fin (car Src)) ")") Reg) )
            ((cdr Src)
               (let R (fin (car Src))
                  (if (pre? "%" R)
                     (prinst "lea" (pack (cdr Src) "(" R ")") Reg)
                     (prinst "mov" R Reg)
                     (prinst "lea" (pack (cdr Src) "(" Reg ")") Reg) ) ) )
            (T (prinst "mov" (fin (car Src)) Reg)) ) )
      ((=T (car S))
         (ifn *FPic
            (prinst "lea"
               (if (cdr S)
                  (pack (car Src) "+" (cdr Src))
                  (car Src) )
               Reg )
            (prinst "mov" (pack (car Src) "@GOTPCREL(%eip)") Reg)
            (prinst "lea" (pack (cdr Src) "(%esi)") Reg) ) )
      (T
         (if (cdr S)
            (prinst "lea" (fin (src Src S)) Reg)
            (prinst "mov" (fin (src (car Src) (car S))) Reg) ) ) ) )

(de dst (Dst D)
   (cond
      ((not D) Dst)                             # Register
      ((not (car D))
         (let R (fin (car Dst))
            (ifn (and *FPic (=T (cdr D)))
               (prog
                  (unless (pre? "%" R)
                     (prinst "mov" R (setq R "%edi")) )
                  (cons
                     (pack (cdr Dst) (and (cdr Dst) "+") "4(" R ")")
                     (pack (cdr Dst) "(" R ")") ) )
               (prinst "add" (pack (cdr Dst) "@GOTPCREL(%eip)") R)
               (cons "???" (pack "(" R ")")) ) ) )
      ((=T (car D))
         (ifn *FPic
            (let Ofs (and (cdr D) (pack "+" (cdr Dst)))
               (cons (pack (car Dst) Ofs "+4") (pack (car Dst) Ofs)) )
            (prinst "mov" (pack (car Dst) "@GOTPCREL(%eip)") "%edi")
            (cons
               (pack (cdr Dst) (and (cdr Dst) "+") "4(%edi)")
               (pack (cdr Dst) "(%edi)") ) ) )
      (T
         (prinst "mov" (fin (dst (car Dst) (car D))) "%edi")
         (ifn (and *FPic (=T (cdr D)))
            (cons
               (pack (cdr Dst) (and (cdr Dst) "+") "4(%edi)")
               (pack (cdr Dst) "(%edi)") )
            (prinst "add" (pack (cdr Dst) "@GOTPCREL(%eip)") "%edi")
            (cons "4(%edi)" "(%edi)") ) ) ) )

(de dstSrcByte (Cmd Dst Src)
   (cond
      ((>= 255 (immed32 Src) 0)
         (prinst
            (pack Cmd (unless  (= "%esp" Dst) "b"))
            (fin Src)
            (lowByte Dst) ) )
      ((= "%al" Dst)
         (prinst Cmd (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst Cmd "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst Cmd (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst Cmd Src (fin Dst)) )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst (pack Cmd "l") (cdr Src) (cdr Dst))
            (prinst Cmd (cdr Src) "%ecx")
            (prinst Cmd "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst (pack Cmd "l") (car Src) (car Dst))
            (prinst Cmd (car Src) "%ecx")
            (prinst Cmd "%ecx" (car Dst)) ) ) ) )

(de dstShift (Cmd Cmd2 Dst Src)
   (if (= "%al" Dst)
      (if (pre? "$" (fin Src))
         (prinst Cmd (fin Src) "%al")
         (prinst "mov" (byteVal Src) "%cl")
         (prinst Cmd "%cl" "%al") )
      (when (= "r" (last (chop Cmd)))
         (setq Dst (cons (cdr Dst) (car Dst))) )
      (unless (pre? "%" (cdr Dst))
         (setq Cmd (pack Cmd "l")) )
      (unless (pre? "%" (car Dst))
         (setq Cmd2 (pack Cmd2 "l")) )
      (if (>= 8 (immed32 Src) 1)
         (do (immed32 Src)
            (prinst Cmd "$1" (cdr Dst))
            (prinst Cmd2 "$1" (car Dst)) )
         (ifn (= "%al" (fin Src))
            (prinst "mov" (fin Src) "%ecx")
            (prinst "mov" "%dx" "%cx")
            (prinst "mov" "%al" "%cl") )
         (prinl "1:")
         (prinst Cmd "$1" (cdr Dst))
         (prinst Cmd2 "$1" (car Dst))
         (prinst "loop" "1b") ) ) )


### Instruction set ###
(asm nop ()
   (prinst "nop") )

# Move data
(asm ld (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)  # B
         (prinst "mov" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "mov" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "mov" (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst "movl" Src (fin Dst))
         (prinst "mov" "%edx" (car Dst)) )
      ((pair (cdr Dst))  # D
         (prinst "mov" (cdr Src) "%eax")
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)))
            (prinst "movl" (car Src) "HiA")
            (prinst "mov" (car Src) "%ecx")
            (prinst "mov" "%ecx" "HiA") )
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)))
            (prinst "movl" (highWord (cdr Src)) "LoC")
            (prinst "mov" (highWord (cdr Src)) "%ecx")
            (prinst "mov" "%ecx" "LoC") )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)))
            (prinst "movl" (highWord (car Src)) "HiC")
            (prinst "mov" (highWord (car Src)) "%ecx")
            (prinst "mov" "%ecx" "HiC") ) )
      ((pair (cdr Src))  # D
         (prinst "mov" "%eax" (cdr Dst))
         (if (or (pre? "$" (car Dst)) (pre? "%" (car Dst)))
            (prinst "movl" "HiA" (car Dst))
            (prinst "mov" "HiA" "%ecx")
            (prinst "mov" "%ecx" (car Dst)) )
         (if (or (pre? "$" (cdr Dst)) (pre? "%" (cdr Dst)))
            (prinst "movl" "LoC" (highWord (cdr Dst)))
            (prinst "mov" "LoC" "%ecx")
            (prinst "mov" "%ecx" (highWord (cdr Dst))) )
         (if (or (pre? "$" (car Dst)) (pre? "%" (car Dst)))
            (prinst "movl" "HiC" (highWord (car Dst)))
            (prinst "mov" "HiC" "%ecx")
            (prinst "mov" "%ecx" (highWord (car Dst))) ) )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst "movl" (cdr Src) (cdr Dst))
            (prinst "mov" (cdr Src) "%ecx")
            (prinst "mov" "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst "movl" (car Src) (car Dst))
            (prinst "mov" (car Src) "%ecx")
            (prinst "mov" "%ecx" (car Dst)) ) ) ) )

(asm ld2 (Src S)
   (prinst "movzwl" (fin (src Src S)) "%eax")
   (prinst "mov" "%edx" "HiA") )

(asm ld4 (Src S)
   (prinst "mov" (fin (src Src S)) "%eax")
   (prinst "mov" "%edx" "HiA") )

(de _cmov (Cmd Jmp)
   (setq Dst (dst Dst D)  Src (src Src S))
   (if (atom Dst)
      (prinst Cmd (fin Src) Dst)
      (prinst Jmp "1f")
      (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
         (prinst "movl" (cdr Src) (cdr Dst))
         (prinst "mov" (cdr Src) "%ecx")
         (prinst "mov" "%ecx" (cdr Dst)) )
      (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
         (prinst "movl" (car Src) (car Dst))
         (prinst "mov" (car Src) "%ecx")
         (prinst "mov" "%ecx" (car Dst)) )
      (prinl "1:") ) )

(asm ldc (Dst D Src S)
   (_cmov "cmovcl" "jnc") )

(asm ldnc (Dst D Src S)
   (_cmov "cmovncl" "jc") )

(asm ldz (Dst D Src S)
   (_cmov "cmovzl" "jnz") )

(asm ldnz (Dst D Src S)
   (_cmov "cmovnzl" "jz") )

(asm lea (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((atom Dst)
         (prinst "lea" (fin Src) Dst) )
      ((pre? "%" (cdr Dst))
         (prinst "lea" (cdr Src) (cdr Dst)) )
      (T
         (prinst "lea" (cdr Src) "%esi")
         (prinst "mov" "%esi" (cdr Dst)) ) ) )

(asm st2 (Dst D)
   (prinst "mov" "%ax" (fin (dst Dst D))) )

(asm st4 (Dst D)
   (prinst "mov" "%eax" (fin (dst Dst D))) )

(asm xchg (Dst D Dst2 D2)
   (setq Dst (dst Dst D)  Dst2 (src Dst2 D2))
   (cond
      ((= "%al" Dst)
         (prinst "xchg" (byteVal Dst2) "%al") )
      ((= "%al" Dst2)
         (prinst "xchg" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "xchg" (fin Dst2) Dst) )
      ((atom Dst2)  # S or L
         (prinst "xchg" Dst2 (fin Dst)) )
      (T
         (if (or (pre? "%" (cdr Dst)) (pre? "%" (cdr Dst2)))
            (prinst "xchg" (cdr Dst) (cdr Dst2))
            (prinst "mov" (cdr Dst) "%ecx")
            (prinst "xchg" "%ecx" (cdr Dst2))
            (prinst "mov" "%ecx" (cdr Dst)) )
         (if (or (pre? "%" (car Dst)) (pre? "%" (car Dst2)))
            (prinst "xchg" (car Dst) (car Dst2))
            (prinst "mov" (car Dst) "%ecx")
            (prinst "xchg" "%ecx" (car Dst2))
            (prinst "mov" "%ecx" (car Dst)) ) ) ) )

(asm movm (Dst D Src S End E)
   (setq Dst (dst Dst D))
   (unless (= "(%edi)" (fin Dst))
      (prinst (if (pre? "%" (fin Dst)) "mov" "lea") (fin Dst) "%edi") )
   (lea End E "%ecx")
   (lea Src S "%esi")
   (prinst "sub" "%esi" "%ecx")
   (prinst "cld")
   (prinst "rep movsb") )

(asm movn (Dst D Src S Cnt C)
   (lea Dst D "%edi")
   (lea Src S "%esi")
   (prinst "mov" (fin (src Cnt C)) "%ecx")
   (prinst "cld")
   (prinst "rep movsb") )

(asm mset (Dst D Cnt C)
   (setq Dst (dst Dst D))
   (unless (= "(%edi)" (fin Dst))
      (prinst (if (pre? "%" (fin Dst)) "mov" "lea") (fin Dst) "%edi") )
   (prinst "mov" (fin (src Cnt C)) "%ecx")
   (prinst "cld")
   (prinst "rep stosb") )


# Arithmetics
(asm add (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)
         (prinst "add" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "add" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "add" (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst "addl" Src (fin Dst)) )
      ((pair (cdr Dst))  # D
         (prinst "add" (cdr Src) "%eax")
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)))
            (prinst "adcl" (car Src) "HiA")
            (prinst "mov" (car Src) "%ecx")
            (prinst "adc" "%ecx" "HiA") )
         (prinst "adc" "%edx" "LoC")
         (prinst "adc" "%edx" "HiC") )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst "addl" (cdr Src) (cdr Dst))
            (prinst "mov" (cdr Src) "%ecx")
            (prinst "add" "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst "adcl" (car Src) (car Dst))
            (prinst "mov" (car Src) "%ecx")
            (prinst "adc" "%ecx" (car Dst)) ) ) ) )

(asm addc (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)
         (prinst "adc" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "adc" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "adc" (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst "addl" Src (fin Dst)) )
      ((pair (cdr Dst))  # D
         (prinst "adc" (cdr Src) "%eax")
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)))
            (prinst "adcl" (car Src) "HiA")
            (prinst "mov" (car Src) "%ecx")
            (prinst "adc" "%ecx" "HiA") )
         (prinst "adc" "%edx" "LoC")
         (prinst "adc" "%edx" "HiC") )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst "adcl" (cdr Src) (cdr Dst))
            (prinst "mov" (cdr Src) "%ecx")
            (prinst "adc" "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst "adcl" (car Src) (car Dst))
            (prinst "mov" (car Src) "%ecx")
            (prinst "adc" "%ecx" (car Dst)) ) ) ) )

(asm sub (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)
         (prinst "sub" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "sub" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "sub" (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst "subl" Src (fin Dst)) )
      ((pair (cdr Dst))  # D
         (prinst "sub" (cdr Src) "%eax")
         (prinst "sbbl" (car Src) "HiA")
         (prinst "sbb" "%edx" "LoC")
         (prinst "sbb" "%edx" "HiC") )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst "subl" (cdr Src) (cdr Dst))
            (prinst "mov" (cdr Src) "%ecx")
            (prinst "sub" "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst "sbbl" (car Src) (car Dst))
            (prinst "mov" (car Src) "%ecx")
            (prinst "sbb" "%ecx" (car Dst)) ) ) ) )

(asm subc (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)
         (prinst "sbb" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "sbb" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "sbb" (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst "sbbl" Src (fin Dst)) )
      ((pair (cdr Dst))  # D
         (prinst "sbb" (cdr Src) "%eax")
         (prinst "sbbl" (car Src) "HiA")
         (prinst "sbb" "%edx" "LoC")
         (prinst "sbb" "%edx" "HiC") )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst "sbbl" (cdr Src) (cdr Dst))
            (prinst "mov" (cdr Src) "%ecx")
            (prinst "sbb" "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst "sbbl" (car Src) (car Dst))
            (prinst "mov" (car Src) "%ecx")
            (prinst "sbb" "%ecx" (car Dst)) ) ) ) )

(asm not (Dst D)
   (setq Dst (dst Dst D))
   (if (atom Dst)  # B
      (prinst "not" Dst)
      (prinst "notl" (cdr Dst))
      (prinst "notl" (car Dst)) ) )

(asm neg (Dst D)
   (setq Dst (dst Dst D))
   (if (atom Dst)  # B
      (prinst "neg" Dst)
      (prinst "negl" (cdr Dst))
      (prinst "adcl" "%edx" (car Dst))
      (prinst "negl" (car Dst)) ) )

(asm and (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)
         (prinst "and" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "and" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "and" (fin Src) Dst) )
      (T
         (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)) (pre? "%" (cdr Dst)))
            (prinst "andl" (cdr Src) (cdr Dst))
            (prinst "and" (cdr Src) "%ecx")
            (prinst "and" "%ecx" (cdr Dst)) )
         (if (or (pre? "$" (car Src)) (pre? "%" (car Src)) (pre? "%" (car Dst)))
            (prinst "andl" (car Src) (car Dst))
            (prinst "and" (car Src) "%ecx")
            (prinst "and" "%ecx" (car Dst)) ) ) ) )

(asm or (Dst D Src S)
   (dstSrcByte "or" (dst Dst D) (src Src S)) )

(asm xor (Dst D Src S)
   (dstSrcByte "xor" (dst Dst D) (src Src S)) )

(asm off (Dst D Src S)
   (dstSrcByte "and" (dst Dst D) (src Src S)) )

(asm test (Dst D Src S)
   (dstSrcByte "test" (dst Dst D) (src Src S)) )

(asm shl (Dst D Src S)
   (dstShift "shl" "rcl" (dst Dst D) (src Src S)) )

(asm shr (Dst D Src S)
   (dstShift "shr" "rcr" (dst Dst D) (src Src S)) )

(asm rol (Dst D Src S)
   (dstShift "rol" "rcl" (dst Dst D) (src Src S)) )

(asm ror (Dst D Src S)
   (dstShift "ror" "rcr" (dst Dst D) (src Src S)) )

(asm rcl (Dst D Src S)
   (dstShift "rcl" "rcl" (dst Dst D) (src Src S)) )

(asm rcr (Dst D Src S)
   (dstShift "rcr" "rcr" (dst Dst D) (src Src S)) )

(asm mul (Src S)
   (setq Src (src Src S))
   (prinst "push" "%ebx")
   (prinst "mov" "HiA" "%ebx")  # MADA
   (prinst "mov" (car Src) "%edx")
   (prinst "mov" (cdr Src) "%ecx")
   (prinst "imul" "%eax" "%ecx")
   (prinst "imul" "%edx" "%ebx")
   (prinst "mul" "%edx")
   (prinst "add" "%ebx" "%ecx")
   (prinst "lea" "(%ecx,%edx)" "%edx")
   (prinst "xor" "%edx" "%edx")
   (prinst "pop" "%ebx") )

(asm div (Src S)
   (setq Src (fin (src Src S)))  # MADA
   (prinst "mov" Src (setq Src "%ecx"))
   (prinst "divl" Src) )

(asm zxt ()  # 8 bit -> 64 bit
   (prinst "movzx" "%al" "%eax")
   (prinst "mov" "%edx" "HiA") )

(asm setc ()
   (prinst "stc") )

(asm clrc ()
   (prinst "clc") )

(asm setz ()
   (prinst "or" "%edx" "%edx") )

(asm clrz ()
   (prinst "cmp" "%esp" "%edx") )


# Comparisons
(asm cmp (Dst D Src S)
   (setq Dst (dst Dst D)  Src (src Src S))
   (cond
      ((= "%al" Dst)
         (prinst "cmp" (byteVal Src) "%al") )
      ((= "%al" Src)
         (prinst "cmp" "%al" (byteVal Dst)) )
      ((atom Dst)  # S or L
         (prinst "cmp" (fin Src) Dst) )
      ((atom Src)  # Direct, S or L
         (prinst "cmpl" Src (fin Dst)) )
      (T
         (prinst "mov" (cdr Src) "%ecx")
         (prinst "sub" (cdr Dst) "%ecx")
         (prinst "mov" (car Src) "%esi")
         (prinst "sbb" (car Dst) "%esi")
         (prinst "jnz" "1f")
         (prinst "or" "%esi" "%ecx")
         (prinl "1:") ) ) )


(asm cmp4 (Src S)
   (prinst "cmp" (fin (src Src S)) "%eax") )

(asm cmpm (Dst D Src S End E)
   (setq Dst (dst Dst D))
   (unless (= "(%edi)" (fin Dst))
      (prinst (if (pre? "%" (fin Dst)) "mov" "lea") (fin Dst) "%esi") )
   (lea End E "%ecx")
   (lea Src S "%edi")
   (prinst "sub" "%esi" "%ecx")
   (prinst "cld")
   (prinst "repnz cmpsb") )

(asm cmpn (Dst D Src S Cnt C)
   (setq Dst (dst Dst D))
   (unless (= "(%edi)" (fin Dst))
      (prinst (if (pre? "%" (fin Dst)) "mov" "lea") (fin Dst) "%esi") )
   (lea Src S "%edi")
   (prinst "mov" (fin (src Cnt C)) "%ecx")
   (prinst "cld")
   (prinst "repnz cmpsb") )

(asm slen (Dst D Src S)
   (setq Dst (dst Dst D))
   (prinst "cld")
   (prinst "xor" "%ecx" "%ecx")
   (prinst "not" "%ecx")  # Infinite
   (lea Src S "%edi")
   (prinst "xchg" "%al" "%dl")  # Save B
   (prinst "repnz scasb")
   (prinst "xchg" "%al" "%dl")  # Restore B
   (prinst "not" "%ecx")
   (prinst "dec" "%ecx")
   (prinst "mov" "%ecx" (fin Dst)) )

(asm memb (Src S Cnt C)
   (prinst "cld")
   (lea Src S "%edi")
   (setq Cnt (src Cnt C))
   (prinst "mov" (fin Cnt) "%ecx")
   (prinst "repnz scasb")
   (unless (and S C)
      (prinst "jnz" "1f")
      (unless S
         (prinst "mov" "%edi" (cdr Src)) )
      (unless C
         (prinst "mov" "%edx" (car Cnt))
         (prinst "mov" "%ecx" (cdr Cnt)) )
      (prinl "1:") ) )

(asm null (Src S)
   (setq Src (src Src S))
   (prinst "cmp" "%edx" (car Src))
   (prinst "jnz" "1f")
   (prinst "cmp" "%edx" (cdr Src))
   (prinst "jz" "1f")
   (prinst "mov" "$1" "%cl")  # nz, s
   (prinst "or" "%cl" "%cl")
   (prinl "1:") )

(asm zero (Src S)
   (setq Src (src Src S))
   (prinst "cmp" "%edx" (car Src))
   (prinst "jnz" "1f")
   (prinst "cmpl" "$2" (cdr Src))
   (prinl "1:") )

(asm nul4 ()
   (prinst "cmp" "%edx" "%eax") )


# Byte addressing
(asm set (Dst D Src S)
   (setq Dst (lowByte (dst Dst D))  Src (fin (src Src S)))
   (cond
      ((= "%edx" Src)
         (prinst "mov" "%dl" Dst) )
      ((or (pre? "$" Src) (pre? "%" Src) (pre? "%" Dst))
         (prinst "movb" Src Dst) )
      (T
         (prinst "mov" Src "%cl")
         (prinst "mov" "%cl" Dst) ) ) )

(asm nul (Src S)
   (prinst "cmp" "%dl" (fin (src Src S))) )


# Types
(asm cnt (Src S)
   (prinst
      (if (= "%esp" Src) "test" "testb")
      "$0x02"
      (lowByte (src Src S)) ) )

(asm big (Src S)
   (prinst
      (if (= "%esp" Src) "test" "testb")
      "$0x04"
      (lowByte (src Src S)) ) )

(asm num (Src S)
   (prinst
      (if (= "%esp" Src) "test" "testb")
      "$0x06"
      (lowByte (src Src S)) ) )

(asm sym (Src S)
   (prinst
      (if (= "%esp" Src) "test" "testb")
      "$0x08"
      (lowByte (src Src S)) ) )

(asm atom (Src S)
   (prinst
      (if (= "%esp" Src) "test" "testb")
      "$0x0E"
      (lowByte (src Src S)) ) )


# Flow Control
(asm call (Adr A)
   (nond
      (A (prinst "call" (target Adr)))
      ((=T A) (prinst "call" (pack "*" (cdr Adr))))
      (NIL
         (prinst "mov" (target Adr T) "%ecx")
         (prinst "call" "*%ecx") ) ) )

(asm jmp (Adr A)
   (nond
      (A (prinst "jmp" (target Adr)))
      ((=T A) (prinst "jmp" (pack "*" (cdr Adr))))
      (NIL
         (prinst "mov" (target Adr T) "%ecx")
         (prinst "jmp" "*%ecx") ) ) )

(de _jmp (Opc Opc2)
   (ifn A
      (prinst Opc (target Adr))
      (prinst Opc2 "1f")
      (ifn (=T A)
         (prinst "jmp" (pack "*" (cdr Adr)))
         (prinst "mov" (target Adr T) "%ecx")
         (prinst "jmp" "*%ecx") )
      (prinl "1:") ) )

(asm jz (Adr A)
   (_jmp "jz" "jnz") )

(asm jeq (Adr A)
   (_jmp "jz" "jnz") )

(asm jnz (Adr A)
   (_jmp "jnz" "jz") )

(asm jne (Adr A)
   (_jmp "jnz" "jz") )

(asm js (Adr A)
   (_jmp "js" "jns") )

(asm jns (Adr A)
   (_jmp "jns" "js") )

(asm jsz (Adr A)
   (_jmp "jle" "jg") )

(asm jnsz (Adr A)
   (_jmp "jg" "jle") )

(asm jc (Adr A)
   (_jmp "jc" "jnc") )

(asm jlt (Adr A)
   (_jmp "jc" "jnc") )

(asm jnc (Adr A)
   (_jmp "jnc" "jc") )

(asm jge (Adr A)
   (_jmp "jnc" "jc") )

(asm jcz (Adr A)
   (_jmp "jbe" "ja") )

(asm jle (Adr A)
   (_jmp "jbe" "ja") )

(asm jncz (Adr A)
   (_jmp "ja" "jbe") )

(asm jgt (Adr A)
   (_jmp "ja" "jbe") )

(asm cc (Adr A Arg M)
   (if (lst? Arg)
      (let  Lea NIL
         (prinst "mov" "%esp" "%edx")
         (mapc
            '((Src S)
               (if (== '& Src)
                  (on Lea)
                  (cond
                     (Lea (lea Src S "%ecx"))
                     ((== 'pop Src) (prinst "pop" "%ecx"))
                     (T (prinst "mov" (fin (src Src S)) "%ecx")) )
                  (prinst "xchg" "%esp" "%edx")
                  (prinst "push" "%ecx")
                  (prinst "xchg" "%esp" "%edx")
                  (off Lea) ) )
            Arg
            M ) )
      (prinl "1:")
      (prinst "cmp" "%esp" Arg)
      (prinst "jz" "2f")
      (prinst "pop" "%ecx")
      (prinst "xchg" "%esp" "%edx")
      (prinst "push" "%ecx")
      (prinst "xchg" "%esp" "%edx")
      (prinl "2:") )
   (prinst "xchg" "%esp" "%edi")
   ((get 'call 'asm) Adr A)
   (prinst "xchg" "%esp" "%edi")
   (unless (lst? Arg)
      (prinst "mov" Arg "%esp") ) )

(asm ret ()
   (prinst "ret") )

(asm begin (N)
   (prinst "push" "%ebx")
   (prinst "push" "%esi")
   (prinst "push" "%edi")
   (prinst "xor" "%edx" "%edx")  # NULL register
   (when (>= N 6)                                  # Z
      (prinst "pushl" "HiZ")
      (prinst "pushl" "LoZ")
      (prinst "movl" "24(%esp)" "%ecx")
      (prinst "movl" "%ecx" "LoZ") )
   (when (>= N 5)                                  # Y
      (prinst "pushl" "HiY")
      (prinst "pushl" "LoY")
      (prinst "movl" "20(%esp)" "%ecx")
      (prinst "movl" "%ecx" "LoY") )
   (when (>= N 4)                                  # X
      (prinst "pushl" "HiX")
      (prinst "pushl" "LoX")
      (prinst "movl" "16(%esp)" "%ecx")
      (prinst "movl" "%ecx" "LoX") )
   (when (>= N 3)                                  # E
      (prinst "movl" "12(%esp)" "%ebx") )
   (when (>= N 2)                                  # C
      (prinst "movl" "8(%esp)" "%ecx")
      (prinst "movl" "%ecx" "LoC") )
   (when (>= N 1)                                  # A
      (prinst "movl" "4(%esp)" "%eax") ) )

(asm return (N)
   (when (>= N 4)
      (prinst "popl" "LoX")
      (prinst "popl" "HiX") )
   (when (>= N 5)
      (prinst "popl" "LoY")
      (prinst "popl" "HiY") )
   (when (>= N 6)
      (prinst "popl" "LoZ")
      (prinst "popl" "HiZ") )
   (prinst "pop" "%edi")
   (prinst "pop" "%esi")
   (prinst "pop" "%ebx")
   (prinst "ret") )


# Stack Manipulations
(asm push (Src S)
   (setq Src (src Src S))
   (cond
      ((=T Src)
         (prinst "push" "%edx")
         (prinst "pushf") )
      ((atom Src)  # S or L
         (prinst "push" "%edx")
         (prinst "push" Src) )
      (T
         (prinst (if (pre? "%" (car Src)) "push" "pushl") (car Src))
         (prinst (if (pre? "%" (cdr Src)) "push" "pushl") (cdr Src)) ) ) )

(asm pop (Dst D)
   (setq Dst (dst Dst D))
   (cond
      ((=T Dst)
         (prinst "popf")
         (prinst "pop" "%edi") )
      ((atom Dst)  # S or L
         (prinst "pop" Dst)
         (prinst "pop" "%edi") )
      (T
         (prinst (if (pre? "%" (cdr Dst)) "pop" "popl") (cdr Dst))
         (prinst (if (pre? "%" (car Dst)) "pop" "popl") (car Dst)) ) ) )

(asm link ()
   (prinst "push" "%edx")
   (prinst "push" "%ebp")
   (prinst "mov" "%esp" "%ebp") )

(asm tuck (Src S)
   (setq Src (src Src S))
   (prinst "mov" "(%esp)" "%ebp")
   (if (or (pre? "$" (car Src)) (pre? "%" (car Src)))
      (prinst "movl" (car Src) "4(%esp)")
      (prinst "mov" (car Src) "%esi")
      (prinst "mov" "%esi" "4(%esp)") )
   (if (or (pre? "$" (cdr Src)) (pre? "%" (cdr Src)))
      (prinst "movl" (cdr Src) "(%esp)")
      (prinst "mov" (cdr Src) "%esi")
      (prinst "mov" "%esi" "(%esp)") ) )

(asm drop ()
   (prinst "mov" "(%ebp)" "%esp")
   (prinst "pop" "%ebp")
   (prinst "pop" "%edi") )

# Evaluation
(asm eval ()
   (prinst "test" "$0x06" "%bl")       # Number?
   (prinst "jnz" "2f")                 # Yes: Skip
   (prinst "test" "$0x08" "%bl")       # Symbol?
   (prinst "jz" "1f")
   (prinst "mov" "4(%ebx)" "%ecx")     # Yes: Get value
   (prinst "mov" "%ecx" "HiE")
   (prinst "mov" "(%ebx)" "%ebx")
   (prinst "jmp" "2f")                 # and skip
   (prinl "1:")
   (prinst "call" (target 'evListE_E)) # Else evaluate list
   (prinl "2:") )

(asm eval+ ()
   (prinst "test" "$0x06" "%bl")       # Number?
   (prinst "jnz" "2f")                 # Yes: Skip
   (prinst "test" "$0x08" "%bl")       # Symbol?
   (prinst "jz" "1f")
   (prinst "mov" "4(%ebx)" "%ecx")     # Yes: Get value
   (prinst "mov" "%ecx" "HiE")
   (prinst "mov" "(%ebx)" "%ebx")
   (prinst "jmp" "2f")                 # and skip
   (prinst "push" "%edx")              # Else 'link'
   (prinst "push" "%ebp")
   (prinst "mov" "%esp" "%ebp")
   (prinl "1:")
   (prinst "call" (target 'evListE_E)) # Evaluate list
   (prinst "pop" "%ebp")
   (prinst "pop" "%edi")
   (prinl "2:") )

(asm eval/ret ()
   (prinst "test" "$0x06" "%bl")       # Number?
   (prinst "jnz" "ret")                # Yes: Return
   (prinst "test" "$0x08" "%bl")       # Symbol?
   (prinst "jz" 'evListE_E)            # No: Evaluate list
   (prinst "mov" "4(%ebx)" "%ecx")     # Get value
   (prinst "mov" "%ecx" "HiE")
   (prinst "mov" "(%ebx)" "%ebx")
   (prinst "ret") )

(asm exec (Reg)
   (prinl "1:")                        # do
   (prinst "mov" (cdr Reg) "%esi")     # ld E (R)
   (prinst "mov"
      (pack "4(%esi)")
      "%ecx" )
   (prinst "mov" "%ecx" "HiE")
   (prinst "mov"
      (pack "(%esi)")
      "%ebx" )
   (prinst "mov"                       # ld R (R CDR)
      (pack "8(%esi)")
      "%esi" )
   (prinst "mov" "%esi" (cdr Reg))
   (prinst "test" "$0x0E" "%bl")       # atom E
   (prinst "jnz" "2f")
   (prinst "call" (target 'evListE_E)) # evList
   (prinl "2:")
   (prinst "testb"                     # atom R
      "$0x0E"
      (byteReg Reg) )
   (prinst "jz" "1b") )                # until nz

(asm prog (Reg)
   (prinl "1:")                        # do
   (prinst "mov" (cdr Reg) "%esi")     # ld E (R)
   (prinst "mov"
      (pack "4(%esi)")
      "%ecx" )
   (prinst "mov" "%ecx" "HiE")
   (prinst "mov"
      (pack "(%esi)")
      "%ebx" )
   (prinst "mov"                       # ld R (R CDR)
      (pack "8(%esi)")
      "%esi" )
   (prinst "mov" "%esi" (cdr Reg))
   (prinst "test" "$0x06" "%bl")       # eval
   (prinst "jnz" "3f")
   (prinst "test" "$0x08" "%bl")
   (prinst "jz" "2f")
   (prinst "mov" "4(%ebx)" "%ecx")
   (prinst "mov" "%ecx" "HiE")
   (prinst "mov" "(%ebx)" "%ebx")
   (prinst "jmp" "3f")
   (prinl "2:")
   (prinst "call" (target 'evListE_E))
   (prinl "3:")
   (prinst "testb"                     # atom R
      "$0x0E"
      (byteReg Reg) )
   (prinst "jz" "1b") )                # until nz


# System
(asm initData ()
   (prinl "   .globl  HiA")
   (prinl "HiA: .long 0")
   (prinl "   .globl  LoC")
   (prinl "LoC: .long 0")
   (prinl "   .globl  HiC")
   (prinl "HiC: .long 0")
   (prinl "   .globl  HiE")
   (prinl "HiE: .long 0")
   (prinl "   .globl  LoX")
   (prinl "LoX: .long 0")
   (prinl "   .globl  HiX")
   (prinl "HiX: .long 0")
   (prinl "   .globl  LoY")
   (prinl "LoY: .long 0")
   (prinl "   .globl  HiY")
   (prinl "HiY: .long 0")
   (prinl "   .globl  LoZ")
   (prinl "LoZ: .long 0")
   (prinl "   .globl  HiZ")
   (prinl "HiZ: .long 0") )

(asm initCode ()
   (prinst "xor" "%edx" "%edx")  # Init NULL register
   (prinst "mov" "8(%esp)" "%esi")  # Get second argument
   (prinst "mov" "(%esi)" "%eax")  # Get command
   (ifn *FPic
      (prinst "mov" "%eax" "AV0")
      (prinst "mov" "AV0@GOTPCREL(%eip)" "%edi")
      (prinst "mov" "%eax" "(%edi)") )
   (prinst "lea" "4(%esi)" "%esi")  # Get argument vector
   (ifn *FPic
      (prinst "mov" "%esi" "AV")
      (prinst "mov" "AV@GOTPCREL(%eip)" "%edi")
      (prinst "mov" "%esi" "(%edi)") ) )


### Optimizer ###
# Replace the the next 'cnt' elements with 'lst'
(de optimize (L))  #> (cnt . lst)

### Patch "src64/lib/asm.l" ###
(patch (get 'word 'asm) '(prinst ".quad" N)
   '(if (num? N)
      (prinst ".quad" N)
      (prinst ".long" N)
      (prinst ".long" 0) ) )

(patch (get 'initSym 'asm) '(prinst ".quad" Val)
   '(if (num? Val)
      (prinst ".quad" Val)
      (prinst ".long" Val)
      (prinst ".long" 0) ) )

(patch (get 'initSym 'asm) '(prinst ".quad" ".+20")
   '(prog
      (prinst ".long" Val)
      (prinst ".long" 0) ) )

# vi:et:ts=3:sw=3
