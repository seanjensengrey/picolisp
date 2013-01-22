# 22jan13abu
# (c) Software Lab. Alexander Burger

# *Salt *Login *Users *Perms

# crypt(3) algorithm, e.g. (16 . "$6$@1$")
(default *Salt (2 . "@1"))

(de salt ()
   (text (cdr *Salt)
      (make
         (in "/dev/urandom"
            (do (car *Salt)
               (link
                  (get
                     '`(mapcar char
                        (conc
                           (range 46 57)
                           (range 65 90)
                           (range 97 122) ) )
                     (& 63 (rd 1)) ) ) ) ) ) ) )

(de passwd (Str Salt)
   (if (and native *Salt)
      (native "libcrypt.so" "crypt" 'S Str (or Salt (salt)))
      Str ) )

(de auth (Nm Pw)
   (with (db 'nm '+User Nm)
      (and
         (or
            (= (: pw) Pw)
            (= (: pw) (passwd Pw (: pw))) )
         This ) ) )

### Login ###
(de login (Nm Pw)
   (ifn (setq *Login (auth Nm Pw))
      (msg *Pid " ? " Nm)
      (msg *Pid " * " (stamp) " " Nm)
      (tell 'hi *Pid Nm *Adr)
      (push1 '*Bye '(logout))
      (push1 '*Fork '(del '(logout) '*Bye))
      (timeout (setq *Timeout `(* 3600 1000))) )
   *Login )

(de logout ()
   (when *Login
      (rollback)
      (off *Login)
      (tell 'hi *Pid)
      (msg *Pid " / " (stamp))
      (timeout (setq *Timeout `(* 300 1000))) ) )

(de hi (Pid Nm Adr)
   (if (and (= Nm (get *Login 'nm)) (= Adr *Adr))
      (bye)
      (hi2 Pid Nm)
      (tell 'hi2 *Pid (get *Login 'nm)) ) )

(de hi2 (Pid Nm)
   (if2 Nm (lup *Users Pid)
      (con @ Nm)
      (idx '*Users (cons Pid Nm) T)
      (idx '*Users @ NIL) ) )


### Role ###
(class +Role +Entity)

(rel nm (+Need +Key +String))          # Role name
(rel perm (+List +Symbol))             # Permission list
(rel usr (+List +Joint) role (+User))  # Associated users


### User ###
(class +User +Entity)

(rel nm (+Need +Key +String))          # User name
(rel pw (+String))                     # Password
(rel role (+Joint) usr (+Role))        # User role


### Permission management ###
(de permission Lst
   (while Lst
      (queue '*Perms (car Lst))
      (def (pop 'Lst) (pop 'Lst)) ) )

(de may Args
   (mmeq Args (get *Login 'role 'perm)) )

(de must Args
   (unless
      (if (cdr Args)
         (mmeq @ (get *Login 'role 'perm))
         *Login )
      (msg *Pid " No permission: " (car Args))
      (forbidden) ) )

# vi:et:ts=3:sw=3
