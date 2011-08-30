# 27aug11abu
# Amit Kulkarni <amitkulz@gmail.com>
# (c) Software Lab. Alexander Burger

# System macros
(code 'errno_A 0)
   call ___errno  # Get address of 'errno'
   ld4 (A)  # Load value
   ret

(code 'errnoC 0)
   call ___errno  # Get address of 'errno'
   xchg A C
   st4 (C)  # Store new value
   ret

(code 's_isdirS_F 0)  # S_ISDIR
   ld4 (S `(+ I ST_MODE))  # Get 'st_mode' from 'stat'
   and A `S_IFMT
   cmp A `S_IFDIR
   ret

(code 'wifstoppedS_F 0)  # WIFSTOPPED
   ld A 0
   cc WIFSTOPPED((S I))  # Get status
   nul4
   ret

(code 'wifsignaledS_F 0)  # WIFSIGNALED
   ld A 0
   cc WIFSIGNALED((S I))  # Get status
   nul4
   ret

(code 'wtermsigS_A 0)  # WTERMSIG
   ld A 0
   cc WTERMSIG((S I))  # Get status
   nul4
   ret

# vi:et:ts=3:sw=3
