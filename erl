#!/bin/sh
exec ${0%/*}/ersatz/picolisp -"on *Dbg" ${0%/*}/lib.l @lib/misc.l @lib/pilog.l @lib/debug.l @lib/lint.l "$@"
