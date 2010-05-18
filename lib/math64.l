# 18may10abu
# (c) Software Lab. Alexander Burger

(load "lib/native.l")

(de log (X)
   (and (gt0 X) ("log" X 1.0)) )

(gcc "math" NIL
   (pow (X Y) "Pow" 'N X Y 1.0)
   (exp (X) "Exp" 'N X 1.0)
   ("log" (X) "Log" 'N X 1.0)
   (sin (A) "Sin" 'N A 1.0)
   (cos (A) "Cos" 'N A 1.0)
   (tan (A) "Tan" 'N A 1.0)
   (asin (A) "Asin" 'N A 1.0)
   (acos (A) "Acos" 'N A 1.0)
   (atan (A) "Atan" 'N A 1.0)
   (atan2 (X Y) "Atan2" 'N X Y 1.0) )

#include <math.h>

long Pow(long x, long y, int scl) {
   return round((double)scl * pow((double)x / (double)scl, (double)y / (double)scl));
}

long Exp(long x, int scl) {
   return round((double)scl * exp((double)x / (double)scl));
}

long Log(long x, int scl) {
   return round((double)scl * log((double)x / (double)scl));
}

long Sin(long a, int scl) {
   return round((double)scl * sin((double)a / (double)scl));
}

long Cos(long a, int scl) {
   return round((double)scl * cos((double)a / (double)scl));
}

long Tan(long a, int scl) {
   return round((double)scl * tan((double)a / (double)scl));
}

long Asin(long a, int scl) {
   return round((double)scl * asin((double)a / (double)scl));
}

long Acos(long a, int scl) {
   return round((double)scl * acos((double)a / (double)scl));
}

long Atan(long a, int scl) {
   return round((double)scl * atan((double)a / (double)scl));
}

long Atan2(long x, long y, int scl) {
   return round((double)scl * atan2((double)x / (double)scl, (double)y / (double)scl));
}
/**/

# vi:et:ts=3:sw=3
