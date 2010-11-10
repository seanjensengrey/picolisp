// 09nov10abu
// (c) Software Lab. Alexander Burger

import java.util.*;
import java.math.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.lang.reflect.*;

/* Ersatz PicoLisp Interpreter (Poor Man's PicoLisp) */
public class PicoLisp {
   final static Console Term = System.console();
   final static HashMap<String,Symbol> Intern = new HashMap<String,Symbol>();
   final static HashMap<String,Symbol> Transient = new HashMap<String,Symbol>();
   final static byte MonLen[] = new byte[] {31,31,28,31,30,31,30,31,31,30,31,30,31};
   final static byte Version[] = new byte[] {3,0,4,4};

   final static Number Zero = new Number(0);
   final static Number One = new Number(1);
   final static Number Two = new Number(2);

   final static NilSym Nil = new NilSym();
   final static Symbol T = mkSymbol(null, "T", Intern);
   final static Symbol At = mkSymbol(Nil, "@", Intern);
   final static Symbol At2 = mkSymbol(Nil, "@@", Intern);
   final static Symbol At3 = mkSymbol(Nil, "@@@", Intern);
   final static Symbol This = mkSymbol(Nil, "This", Intern);
   final static Symbol Dbg = mkSymbol(Nil, "*Dbg", Intern);
   final static Symbol Scl = mkSymbol(Zero, "*Scl", Intern);
   final static Symbol Class = mkSymbol(Nil, "*Class", Intern);
   final static Symbol Run = mkSymbol(Nil, "*Run", Intern);
   final static Symbol Up = mkSymbol(Nil, "^", Intern);
   final static Symbol Err = mkSymbol(Nil, "*Err", Intern);
   final static Symbol Msg = mkSymbol(Nil, "*Msg", Intern);
   final static Symbol Uni = mkSymbol(Nil, "*Uni", Intern);
   final static Symbol Bye = mkSymbol(Nil, "*Bye", Intern);

   final static Symbol Quote = mkSymbol(Zero, "quote", Intern);
   final static Symbol Meth = mkSymbol(One, "meth", Intern);

   final static String Delim = " \t\n\r\"'(),[]`~{}";

   static Catch Catch;
   static Env Env = new Env();
   static StringBuilder Line;
   static Process[] Pids = new Process[12];
   static PicoLispReader[] InFiles = new PicoLispReader[12];
   static PicoLispWriter[] OutFiles = new PicoLispWriter[12];
   final static PicoLispReader StdIn = new PicoLispReader(System.in, 0, null, 0);
   final static PicoLispWriter StdOut = new PicoLispWriter(System.out, 1);
   final static PicoLispWriter StdErr = new PicoLispWriter(System.err, 2);
   static PicoLispReader InFile = StdIn;
   static PicoLispWriter OutFile = StdOut;
   static Any TheCls, TheKey, Penv, Pnl;
   static String[] Argv;
   static String Home;
   static Calendar Cal;
   static int MaxFun;
   static long USec, Seed;
   static boolean Break, Jam, B;
   static Bind Brk;

   public static void main(String[] argv) {
      Argv = argv;
      mkSymbol(new Number("2"), "quit", Intern);
      mkSymbol(new Number("3"), "public", Intern);
      mkSymbol(new Number("4"), "java", Intern);
      mkSymbol(new Number("5"), "byte:", Intern);
      mkSymbol(new Number("6"), "char:", Intern);
      mkSymbol(new Number("7"), "int:", Intern);
      mkSymbol(new Number("8"), "long:", Intern);
      mkSymbol(new Number("9"), "double:", Intern);
      mkSymbol(new Number("10"), "big:", Intern);
      mkSymbol(new Number("11"), "data", Intern);
      mkSymbol(new Number("12"), "args", Intern);
      mkSymbol(new Number("13"), "next", Intern);
      mkSymbol(new Number("14"), "arg", Intern);
      mkSymbol(new Number("15"), "rest", Intern);
      mkSymbol(new Number("16"), "date", Intern);
      mkSymbol(new Number("17"), "time", Intern);
      mkSymbol(new Number("18"), "usec", Intern);
      mkSymbol(new Number("19"), "file", Intern);
      mkSymbol(new Number("20"), "version", Intern);
      mkSymbol(new Number("21"), "apply", Intern);
      mkSymbol(new Number("22"), "pass", Intern);
      mkSymbol(new Number("23"), "maps", Intern);
      mkSymbol(new Number("24"), "map", Intern);
      mkSymbol(new Number("25"), "mapc", Intern);
      mkSymbol(new Number("26"), "maplist", Intern);
      mkSymbol(new Number("27"), "mapcar", Intern);
      mkSymbol(new Number("28"), "mapcon", Intern);
      mkSymbol(new Number("29"), "mapcan", Intern);
      mkSymbol(new Number("30"), "filter", Intern);
      mkSymbol(new Number("31"), "extract", Intern);
      mkSymbol(new Number("32"), "seek", Intern);
      mkSymbol(new Number("33"), "find", Intern);
      mkSymbol(new Number("34"), "pick", Intern);
      mkSymbol(new Number("35"), "cnt", Intern);
      mkSymbol(new Number("36"), "sum", Intern);
      mkSymbol(new Number("37"), "maxi", Intern);
      mkSymbol(new Number("38"), "mini", Intern);
      mkSymbol(new Number("39"), "fish", Intern);
      mkSymbol(new Number("40"), "by", Intern);
      mkSymbol(new Number("41"), "as", Intern);
      mkSymbol(new Number("42"), "lit", Intern);
      mkSymbol(new Number("43"), "eval", Intern);
      mkSymbol(new Number("44"), "run", Intern);
      mkSymbol(new Number("45"), "def", Intern);
      mkSymbol(new Number("46"), "de", Intern);
      mkSymbol(new Number("47"), "dm", Intern);
      mkSymbol(new Number("48"), "box", Intern);
      mkSymbol(new Number("49"), "new", Intern);
      mkSymbol(new Number("50"), "type", Intern);
      mkSymbol(new Number("51"), "isa", Intern);
      mkSymbol(new Number("52"), "method", Intern);
      mkSymbol(new Number("53"), "send", Intern);
      mkSymbol(new Number("54"), "try", Intern);
      mkSymbol(new Number("55"), "super", Intern);
      mkSymbol(new Number("56"), "extra", Intern);
      mkSymbol(new Number("57"), "with", Intern);
      mkSymbol(new Number("58"), "bind", Intern);
      mkSymbol(new Number("59"), "job", Intern);
      mkSymbol(new Number("60"), "let", Intern);
      mkSymbol(new Number("61"), "let?", Intern);
      mkSymbol(new Number("62"), "use", Intern);
      mkSymbol(new Number("63"), "and", Intern);
      mkSymbol(new Number("64"), "or", Intern);
      mkSymbol(new Number("65"), "nand", Intern);
      mkSymbol(new Number("66"), "nor", Intern);
      mkSymbol(new Number("67"), "xor", Intern);
      mkSymbol(new Number("68"), "bool", Intern);
      mkSymbol(new Number("69"), "not", Intern);
      mkSymbol(new Number("70"), "nil", Intern);
      mkSymbol(new Number("71"), "t", Intern);
      mkSymbol(new Number("72"), "prog", Intern);
      mkSymbol(new Number("73"), "prog1", Intern);
      mkSymbol(new Number("74"), "prog2", Intern);
      mkSymbol(new Number("75"), "if", Intern);
      mkSymbol(new Number("76"), "if2", Intern);
      mkSymbol(new Number("77"), "ifn", Intern);
      mkSymbol(new Number("78"), "when", Intern);
      mkSymbol(new Number("79"), "unless", Intern);
      mkSymbol(new Number("80"), "cond", Intern);
      mkSymbol(new Number("81"), "nond", Intern);
      mkSymbol(new Number("82"), "case", Intern);
      mkSymbol(new Number("83"), "state", Intern);
      mkSymbol(new Number("84"), "while", Intern);
      mkSymbol(new Number("85"), "until", Intern);
      mkSymbol(new Number("86"), "do", Intern);
      mkSymbol(new Number("87"), "loop", Intern);
      mkSymbol(new Number("88"), "at", Intern);
      mkSymbol(new Number("89"), "for", Intern);
      mkSymbol(new Number("90"), "catch", Intern);
      mkSymbol(new Number("91"), "throw", Intern);
      mkSymbol(new Number("92"), "finally", Intern);
      mkSymbol(new Number("93"), "!", Intern);
      mkSymbol(new Number("94"), "e", Intern);
      mkSymbol(new Number("95"), "$", Intern);
      mkSymbol(new Number("96"), "sys", Intern);
      mkSymbol(new Number("97"), "call", Intern);
      mkSymbol(new Number("98"), "ipid", Intern);
      mkSymbol(new Number("99"), "opid", Intern);
      mkSymbol(new Number("100"), "kill", Intern);
      mkSymbol(new Number("101"), "bye", Intern);
      mkSymbol(new Number("102"), "name", Intern);
      mkSymbol(new Number("103"), "sp?", Intern);
      mkSymbol(new Number("104"), "pat?", Intern);
      mkSymbol(new Number("105"), "fun?", Intern);
      mkSymbol(new Number("106"), "getd", Intern);
      mkSymbol(new Number("107"), "all", Intern);
      mkSymbol(new Number("108"), "intern", Intern);
      mkSymbol(new Number("109"), "====", Intern);
      mkSymbol(new Number("110"), "box?", Intern);
      mkSymbol(new Number("111"), "str?", Intern);
      mkSymbol(new Number("112"), "zap", Intern);
      mkSymbol(new Number("113"), "chop", Intern);
      mkSymbol(new Number("114"), "pack", Intern);
      mkSymbol(new Number("115"), "glue", Intern);
      mkSymbol(new Number("116"), "text", Intern);
      mkSymbol(new Number("117"), "pre?", Intern);
      mkSymbol(new Number("118"), "sub?", Intern);
      mkSymbol(new Number("119"), "val", Intern);
      mkSymbol(new Number("120"), "set", Intern);
      mkSymbol(new Number("121"), "setq", Intern);
      mkSymbol(new Number("122"), "xchg", Intern);
      mkSymbol(new Number("123"), "on", Intern);
      mkSymbol(new Number("124"), "off", Intern);
      mkSymbol(new Number("125"), "onOff", Intern);
      mkSymbol(new Number("126"), "zero", Intern);
      mkSymbol(new Number("127"), "one", Intern);
      mkSymbol(new Number("128"), "default", Intern);
      mkSymbol(new Number("129"), "push", Intern);
      mkSymbol(new Number("130"), "push1", Intern);
      mkSymbol(new Number("131"), "pop", Intern);
      mkSymbol(new Number("132"), "cut", Intern);
      mkSymbol(new Number("133"), "del", Intern);
      mkSymbol(new Number("134"), "queue", Intern);
      mkSymbol(new Number("135"), "fifo", Intern);
      mkSymbol(new Number("136"), "idx", Intern);
      mkSymbol(new Number("137"), "lup", Intern);
      mkSymbol(new Number("138"), "put", Intern);
      mkSymbol(new Number("139"), "get", Intern);
      mkSymbol(new Number("140"), "prop", Intern);
      mkSymbol(new Number("141"), ";", Intern);
      mkSymbol(new Number("142"), "=:", Intern);
      mkSymbol(new Number("143"), ":", Intern);
      mkSymbol(new Number("144"), "::", Intern);
      mkSymbol(new Number("145"), "putl", Intern);
      mkSymbol(new Number("146"), "getl", Intern);
      mkSymbol(new Number("147"), "meta", Intern);
      mkSymbol(new Number("148"), "low?", Intern);
      mkSymbol(new Number("149"), "upp?", Intern);
      mkSymbol(new Number("150"), "lowc", Intern);
      mkSymbol(new Number("151"), "uppc", Intern);
      mkSymbol(new Number("152"), "fold", Intern);
      mkSymbol(new Number("153"), "car", Intern);
      mkSymbol(new Number("154"), "cdr", Intern);
      mkSymbol(new Number("155"), "caar", Intern);
      mkSymbol(new Number("156"), "cadr", Intern);
      mkSymbol(new Number("157"), "cdar", Intern);
      mkSymbol(new Number("158"), "cddr", Intern);
      mkSymbol(new Number("159"), "caaar", Intern);
      mkSymbol(new Number("160"), "caadr", Intern);
      mkSymbol(new Number("161"), "cadar", Intern);
      mkSymbol(new Number("162"), "caddr", Intern);
      mkSymbol(new Number("163"), "cdaar", Intern);
      mkSymbol(new Number("164"), "cdadr", Intern);
      mkSymbol(new Number("165"), "cddar", Intern);
      mkSymbol(new Number("166"), "cdddr", Intern);
      mkSymbol(new Number("167"), "caaaar", Intern);
      mkSymbol(new Number("168"), "caaadr", Intern);
      mkSymbol(new Number("169"), "caadar", Intern);
      mkSymbol(new Number("170"), "caaddr", Intern);
      mkSymbol(new Number("171"), "cadaar", Intern);
      mkSymbol(new Number("172"), "cadadr", Intern);
      mkSymbol(new Number("173"), "caddar", Intern);
      mkSymbol(new Number("174"), "cadddr", Intern);
      mkSymbol(new Number("175"), "cdaaar", Intern);
      mkSymbol(new Number("176"), "cdaadr", Intern);
      mkSymbol(new Number("177"), "cdadar", Intern);
      mkSymbol(new Number("178"), "cdaddr", Intern);
      mkSymbol(new Number("179"), "cddaar", Intern);
      mkSymbol(new Number("180"), "cddadr", Intern);
      mkSymbol(new Number("181"), "cdddar", Intern);
      mkSymbol(new Number("182"), "cddddr", Intern);
      mkSymbol(new Number("183"), "nth", Intern);
      mkSymbol(new Number("184"), "con", Intern);
      mkSymbol(new Number("185"), "cons", Intern);
      mkSymbol(new Number("186"), "conc", Intern);
      mkSymbol(new Number("187"), "circ", Intern);
      mkSymbol(new Number("188"), "rot", Intern);
      mkSymbol(new Number("189"), "list", Intern);
      mkSymbol(new Number("190"), "need", Intern);
      mkSymbol(new Number("191"), "range", Intern);
      mkSymbol(new Number("192"), "full", Intern);
      mkSymbol(new Number("193"), "make", Intern);
      mkSymbol(new Number("194"), "made", Intern);
      mkSymbol(new Number("195"), "chain", Intern);
      mkSymbol(new Number("196"), "link", Intern);
      mkSymbol(new Number("197"), "yoke", Intern);
      mkSymbol(new Number("198"), "copy", Intern);
      mkSymbol(new Number("199"), "mix", Intern);
      mkSymbol(new Number("200"), "append", Intern);
      mkSymbol(new Number("201"), "delete", Intern);
      mkSymbol(new Number("202"), "delq", Intern);
      mkSymbol(new Number("203"), "replace", Intern);
      mkSymbol(new Number("204"), "strip", Intern);
      mkSymbol(new Number("205"), "split", Intern);
      mkSymbol(new Number("206"), "reverse", Intern);
      mkSymbol(new Number("207"), "flip", Intern);
      mkSymbol(new Number("208"), "trim", Intern);
      mkSymbol(new Number("209"), "clip", Intern);
      mkSymbol(new Number("210"), "head", Intern);
      mkSymbol(new Number("211"), "tail", Intern);
      mkSymbol(new Number("212"), "stem", Intern);
      mkSymbol(new Number("213"), "fin", Intern);
      mkSymbol(new Number("214"), "last", Intern);
      mkSymbol(new Number("215"), "==", Intern);
      mkSymbol(new Number("216"), "n==", Intern);
      mkSymbol(new Number("217"), "=", Intern);
      mkSymbol(new Number("218"), "<>", Intern);
      mkSymbol(new Number("219"), "=0", Intern);
      mkSymbol(new Number("220"), "=T", Intern);
      mkSymbol(new Number("221"), "n0", Intern);
      mkSymbol(new Number("222"), "nT", Intern);
      mkSymbol(new Number("223"), "<", Intern);
      mkSymbol(new Number("224"), "<=", Intern);
      mkSymbol(new Number("225"), ">", Intern);
      mkSymbol(new Number("226"), ">=", Intern);
      mkSymbol(new Number("227"), "max", Intern);
      mkSymbol(new Number("228"), "min", Intern);
      mkSymbol(new Number("229"), "atom", Intern);
      mkSymbol(new Number("230"), "pair", Intern);
      mkSymbol(new Number("231"), "lst?", Intern);
      mkSymbol(new Number("232"), "num?", Intern);
      mkSymbol(new Number("233"), "sym?", Intern);
      mkSymbol(new Number("234"), "flg?", Intern);
      mkSymbol(new Number("235"), "member", Intern);
      mkSymbol(new Number("236"), "memq", Intern);
      mkSymbol(new Number("237"), "mmeq", Intern);
      mkSymbol(new Number("238"), "sect", Intern);
      mkSymbol(new Number("239"), "diff", Intern);
      mkSymbol(new Number("240"), "index", Intern);
      mkSymbol(new Number("241"), "offset", Intern);
      mkSymbol(new Number("242"), "length", Intern);
      mkSymbol(new Number("243"), "size", Intern);
      mkSymbol(new Number("244"), "assoc", Intern);
      mkSymbol(new Number("245"), "asoq", Intern);
      mkSymbol(new Number("246"), "rank", Intern);
      mkSymbol(new Number("247"), "match", Intern);
      mkSymbol(new Number("248"), "fill", Intern);
      mkSymbol(new Number("249"), "prove", Intern);
      mkSymbol(new Number("250"), "->", Intern);
      mkSymbol(new Number("251"), "unify", Intern);
      mkSymbol(new Number("252"), "sort", Intern);
      mkSymbol(new Number("253"), "format", Intern);
      mkSymbol(new Number("254"), "+", Intern);
      mkSymbol(new Number("255"), "-", Intern);
      mkSymbol(new Number("256"), "inc", Intern);
      mkSymbol(new Number("257"), "dec", Intern);
      mkSymbol(new Number("258"), "*", Intern);
      mkSymbol(new Number("259"), "*/", Intern);
      mkSymbol(new Number("260"), "/", Intern);
      mkSymbol(new Number("261"), "%", Intern);
      mkSymbol(new Number("262"), ">>", Intern);
      mkSymbol(new Number("263"), "lt0", Intern);
      mkSymbol(new Number("264"), "ge0", Intern);
      mkSymbol(new Number("265"), "gt0", Intern);
      mkSymbol(new Number("266"), "abs", Intern);
      mkSymbol(new Number("267"), "bit?", Intern);
      mkSymbol(new Number("268"), "&", Intern);
      mkSymbol(new Number("269"), "|", Intern);
      mkSymbol(new Number("270"), "x|", Intern);
      mkSymbol(new Number("271"), "seed", Intern);
      mkSymbol(new Number("272"), "rand", Intern);
      mkSymbol(new Number("273"), "path", Intern);
      mkSymbol(new Number("274"), "read", Intern);
      mkSymbol(new Number("275"), "wait", Intern);
      mkSymbol(new Number("276"), "char", Intern);
      mkSymbol(new Number("277"), "skip", Intern);
      mkSymbol(new Number("278"), "line", Intern);
      mkSymbol(new Number("279"), "load", Intern);
      mkSymbol(new Number("280"), "in", Intern);
      mkSymbol(new Number("281"), "out", Intern);
      mkSymbol(new Number("282"), "open", Intern);
      mkSymbol(new Number("283"), "close", Intern);
      mkSymbol(new Number("284"), "prin", Intern);
      mkSymbol(new Number("285"), "prinl", Intern);
      mkSymbol(new Number("286"), "space", Intern);
      mkSymbol(new Number("287"), "print", Intern);
      mkSymbol(new Number("288"), "printsp", Intern);
      mkSymbol(new Number("289"), "println", Intern);
      mkSymbol(new Number("290"), "flush", Intern);
      mkSymbol(new Number("291"), "port", Intern);
      mkSymbol(new Number("292"), "accept", Intern);
      mkSymbol(new Number("293"), "connect", Intern);
      MaxFun = 293;
      init();
      for (boolean first = true; ; first = false) {
         try {
            if (first)
               loadAll(null);
            load(null, ':', Nil);
            bye(0);
         }
         catch (Control e) {}
         catch (Throwable e) {error(null, null, e.toString());}
      }
   }

   final static void init() {
      String s;
      Home = "";
      for (int i = 0; i < Argv.length; ++i)
         if ((s = Argv[i]).charAt(0) != '-') {
            if ((i = s.lastIndexOf('/')) >= 0 && !(i == 1 && s.charAt(0) == '.'))
               Home = s.substring(0, i+1);
            break;
         }
      try {
         if (Term != null) {
            final Pipe p = Pipe.open();
            StdIn.Chan = p.source();
            StdIn.Ops = SelectionKey.OP_READ;
            (new Thread() {
               public void run() {
                  for (;;) {
                     String s = Term.readLine();
                     if (s == null)
                        Line = null;
                     else {
                        Line = new StringBuilder(s);
                        Line.append('\n');
                     }
                     try {p.sink().write(ByteBuffer.allocate(1));}
                     catch (IOException e) {giveup(e);}
                  }
               }
            } ).start();
         }
      }
      catch (IOException e) {giveup(e);}
      USec = System.nanoTime() / 1000;
   }

   final static void giveup(Exception e) {
      System.err.println(e);
      System.exit(1);
   }

   final static Any bye(int n) {
      if (!B) {
         B = true;
         unwind(null);
         Bye.Car.prog();
      }
      System.exit(n);
      return null;  /* Brain-dead Java */
   }

   final static int waitFd(Any ex, int fd, int ms) {
      int i;
      Selector sel;
      Any task = Env.Task,  at = At.Car;
      try {
         for (;;) {
            sel = Selector.open();
            int t = ms >= 0? ms : Integer.MAX_VALUE;
            if (fd >= 0 && InFiles[fd] != null)
               if (InFiles[fd].ready(sel))
                  t = 0;
               else
                  InFiles[fd].register(sel);
            for (Any x = Env.Task = Run.Car; x instanceof Cell; x = x.Cdr) {
               if (memq(x.Car, task) == null) {
                  if ((i = ((Number)x.Car.Car).Cnt) < 0) {
                     if ((i = ((Number)x.Car.Cdr.Car).Cnt) < t)
                        t = i;
                  }
                  else if (i != fd) {
                     if (i < InFiles.length && InFiles[i] != null)
                        if (InFiles[i].ready(sel))
                           t = 0;
                        else
                           InFiles[i].register(sel);
                  }
               }
            }
            long d = System.currentTimeMillis();
            if (t == 0)
               sel.selectNow();
            else
               sel.select(t);
            t = (int)(System.currentTimeMillis() - d);
            if (ms > 0  &&  (ms -= t) < 0)
               ms = 0;
            for (Any x = Env.Task; x instanceof Cell; x = x.Cdr) {
               if (memq(x.Car, task) == null) {
                  if ((i = ((Number)x.Car.Car).Cnt) < 0) {
                     if ((i = ((Number)x.Car.Cdr.Car).Cnt - t) > 0)
                        ((Number)x.Car.Cdr.Car).Cnt = i;
                     else {
                        ((Number)x.Car.Cdr.Car).Cnt = -((Number)x.Car.Car).Cnt;
                        At.Car = x.Car.Car;
                        x.Car.Cdr.Cdr.prog();
                     }
                  }
                  else if (i != fd) {
                     if (i < InFiles.length && InFiles[i] != null && InFiles[i].ready(sel)) {
                        At.Car = x.Car.Car;
                        x.Car.Cdr.prog();
                     }
                  }
               }
            }
            if (ms == 0 || fd < 0 || InFiles[fd] != null && InFiles[fd].ready(sel))
               break;
            sel.close();
         }
      }
      catch (IOException e) {giveup(e);}
      At.Car = at;
      Env.Task = task;
      return ms;
   }

   final static long initSeed(Any x) {
      long n;
      for (n = 0; x instanceof Cell; x = x.Cdr)
         n += initSeed(x.Car);
      if (x != Nil) {
         if (x instanceof Number && ((Number)x).Big == null)
            n += ((Number)x).Cnt;
         else {
            byte b[] = x instanceof Symbol? x.name().getBytes() : ((Number)x).Big.toByteArray();
            for (int i = 0; i < b.length; ++i)
               n += b[i];
         }
      }
      return n;
   }

   final static Any date(int y, int m, int d) {
      int n;

      if (m<1 || m>12 || d<1 || d>MonLen[m] && (m!=2 || d!=29 || y%4!=0 || y%100==0 && y%400!=0))
         return Nil;
      n = (12*y + m - 3) / 12;
      return new Number((4404*y+367*m-1094)/12 - 2*n + n/4 - n/100 + n/400 + d);
   }

   final static Any date(int n) {
      int y = (100*n - 20) / 3652425;
      n += (y - y/4);
      y = (100*n - 20) / 36525;
      n -= 36525*y / 100;
      int m = (10*n - 5) / 306;
      int d = (10*n - 306*m + 5) / 10;
      if (m < 10)
         m += 3;
      else {
         ++y;
         m -= 9;
      }
      return new Cell(new Number(y), new Cell(new Number(m), new Cell(new Number(d), Nil)));
   }

   final static Any time(Calendar cal) {
      return new Number(cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND));
   }

   final static Any time(int h, int m, int s) {
      if (h < 0 || h > 23  ||  m < 0 || m > 59  ||  s < 0 || s > 60)
         return Nil;
      return new Number(h * 3600 + m * 60 + s);
   }

   final static char firstChar(Any s) {
      String nm = s.name();
      return nm.length() == 0? '\0' : nm.charAt(0);
   }

   final static String path(String s) {
      if (s.length() > 0)
         if (s.charAt(0) == '+') {
            if (s.length() > 1 && s.charAt(1) == '@')
               return "+" + Home + s.substring(1);
         }
         else if (s.charAt(0) == '@')
            return Home + s.substring(1);
      return s;
   }

   final static void unwind(Catch target) {
      int i, j, n;
      Bind p;
      Catch q;
      Any x, y;
      while ((q = Catch) != null) {
         while ((p = Env.Bind) != null) {
            if ((i = p.Eswp) != 0) {
               j = i;  n = 0;
               for (;;) {
                  ++n;
                  if (++j == 0 || (p = p.Link) == null)
                     break;
                  if (p.Eswp < i)
                     --j;
               }
               do {
                  for (p = Env.Bind, j = n; --j != 0; p = p.Link);
                  if ((p.Eswp -= i) >= 0) {
                     if (p.Eswp > 0)
                        p.Eswp = 0;
                     for (j = p.Cnt; (j -= 2) >= 0;) {
                        y = p.Data[j+1];
                        x = y.Car;  y.Car = p.Data[j];  p.Data[j] = x;
                     }
                  }
               } while (--n != 0);
            }
            if (Env.Bind == q.Env.Bind)
               break;
            if (Env.Bind.Eswp == 0)
               for (i = Env.Bind.Cnt; (i -= 2) >= 0;)
                  Env.Bind.Data[i+1].Car = Env.Bind.Data[i];
            Env.Bind = Env.Bind.Link;
         }
         while (Env.InFrames != q.Env.InFrames)
            Env.popInFiles();
         while (Env.OutFrames != q.Env.OutFrames)
            Env.popOutFiles();
         Env = q.Env;
         q.Fin.eval();
         Catch = q.Link;
         if (q == target)
            return;
      }
      while (Env.Bind != null) {
         if (Env.Bind.Eswp == 0)
            for (i = Env.Bind.Cnt; (i -= 2) >= 0;)
               Env.Bind.Data[i+1].Car = Env.Bind.Data[i];
         Env.Bind = Env.Bind.Link;
      }
      while (Env.InFrames != null)
         Env.popInFiles();
      while (Env.OutFrames != null)
         Env.popOutFiles();
   }

   final static void error(Any ex, Any x, String msg) {
      Up.Car = ex == null? Nil : ex;
      if (msg.length() != 0) {
         Msg.Car = mkStr(msg);
         for (Catch p = Catch;  p != null;  p = p.Link) {
            Any y = p.Tag;
            if (y != null)
               while (y instanceof Cell) {
                  if (msg.indexOf(y.Car.name()) >= 0)
                     throw new Control(ex, p.Tag, y.Car == Nil? Msg.Car : y.Car);
                  y = y.Cdr;
               }
         }
      }
      Env.pushOutFile(new OutFrame(OutFiles[2], 0));
      if (InFile.Name != null)
         StdErr.Wr.print('[' + InFile.Name + ':' + InFile.Src  + "] ");
      if (ex != null) {
         StdErr.Wr.print("!? ");
         StdErr.print(ex);
         StdErr.newline();
      }
      if (x != null) {
         StdErr.print(x);
         StdErr.Wr.print(" -- ");
      }
      if (msg.length() != 0) {
         StdErr.Wr.print(msg);
         StdErr.newline();
         if (Err.Car != Nil && !Jam) {
            Jam = true;
            Err.Car.prog();
            Jam = false;
         }
         load(null, '?', Nil);
      }
      unwind(null);
      Env.Args = null;
      Env.Next = 0;
      Env.Task = Env.Make = Env.Yoke = null;
   }

   final static Any err(Any ex, Any x, String msg) {
      error(ex, x, msg);
      throw new Control();
   }

   final static Any brkLoad(Any x) {
      if (!Break) {
         Break = true;
         OutFile.Wr.flush();
         Brk = new Bind();
         Brk.add(Up.Car);  Brk.add(Up);  Up.Car = x;
         Brk.add(Run.Car);  Brk.add(Run);  Run.Car = Nil;
         Brk.add(At.Car);  Brk.add(At);
         Env.Bind = Brk;
         Env.pushOutFile(new OutFrame(OutFiles[1], 0));
         OutFile.print(x);
         OutFile.newline();
         load(null, '!', Nil);
         Env.popOutFiles();
         At.Car = Brk.Data[4];
         Run.Car = Brk.Data[2];
         x = Up.Car;
         Up.Car = Brk.Data[0];
         Env.Bind = Brk.Link;
         Break = false;
      }
      return x;
   }

   final static void trace(int i, Any x, String s) {
      if (i > 64)
         i = 64;
      while (--i >= 0)
         StdErr.space();
      if (x instanceof Symbol)
         StdErr.print(x);
      else {
         StdErr.print(x.Car);
         StdErr.space();
         StdErr.print(x.Cdr);
         StdErr.space();
         StdErr.print(This.Car);
      }
      StdErr.Wr.print(s);
   }

   final static Any execError(Any x) {return err(null, x, "Can't execute");}
   final static Any protError(Any x) {return err(null, x, "Protected symbol");}
   final static Any symError(Any x) {return err(null, x, "Symbol expected");}
   final static Any  argError(Any ex, Any x) {return err(ex, x, "Bad argument");}
   final static Any cntError(Any ex, Any x) {return err(ex, x, "Small number expected");}
   final static void needVar(Any ex, Any x) {if (x instanceof Number) err(ex, x, "Variable expected");}

   final static void badFd(Any ex, Any x) {err(ex, x, "Bad FD");}
   final static void closeErr(IOException e) {err(null, null, e.toString());}

   final static Any load(Any ex, char pr, Any x) {
      if (x instanceof Symbol && firstChar(x) == '-')
         return ((Symbol)x).parse(true,null).eval();
      Env.pushInFile(x.rdOpen(ex));
      Transient.clear();
      x = Nil;
      for (;;) {
         Any y;
         if (InFile.Name != null)
            y = InFile.read('\0');
         else {
            if (pr != '\0'  &&  InFile.Chr == 0) {
               OutFile.Wr.print(pr);
               OutFile.space();
               OutFile.Wr.flush();
            }
            y = InFile.read('\n');
            if (InFile.Chr == '\n')
               InFile.Chr = 0;
         }
         if (y == Nil)
            break;
         if (InFile.Name != null || InFile.Chr != 0 || pr == '\0')
            x = y.eval();
         else {
            Any at = At.Car;
            x = At.Car = y.eval();
            At3.Car = At2.Car;
            At2.Car = at;
            OutFile.Wr.print("-> ");
            OutFile.Wr.flush();
            OutFile.print(x);
            OutFile.newline();
         }
      }
      Env.popInFiles();
      Transient.clear();
      return x;
   }

   final static Any loadAll(Any ex) {
      Any x = Nil;
      while (Argv.length > 0  &&  !Argv[0].equals("-")) {
         x = load(ex, '\0', mkStr(Argv[0]));
         String[] a = new String[Argv.length-1];
         System.arraycopy(Argv, 1, a, 0, a.length);
         Argv = a;
      }
      return x;
   }

   final static Any undefined(Any x, Any ex) {
      return err(ex, x, "Undefined");
   }

   final static Any[] append(Any[] a, int i, Any x) {
      if (i == a.length) {
         Any[] b = new Any[i*2];
         System.arraycopy(a, 0, b, 0, i);
         a = b;
      }
      a[i] = x;
      return a;
   }

   final static int allocPid() {
      int i;
      for (i = 2; Pids[i] != null; ++i) {
         if (i == Pids.length) {
            Process[] p = new Process[i*2];
            System.arraycopy(Pids, 0, p, 0, i);
            Pids = p;
            break;
         }
      }
      return i;
   }

   final static int allocFd() {
      int i;
      for (i = 3; InFiles[i] != null || OutFiles[i] != null; ++i) {
         if (i == InFiles.length) {
            PicoLispReader[] r = new PicoLispReader[i*2];
            System.arraycopy(InFiles, 0, r, 0, i);
            InFiles = r;
            PicoLispWriter[] w = new PicoLispWriter[i*2];
            System.arraycopy(OutFiles, 0, w, 0, i);
            OutFiles = w;
            break;
         }
      }
      return i;
   }

   final static Any mkSocket(SocketChannel chan) throws IOException {
      int i = allocFd();
      Socket sock = chan.socket();
      new PicoLispReader(sock.getInputStream(), i, chan, SelectionKey.OP_READ);
      new PicoLispWriter(sock.getOutputStream(), i);
      return new Number(i);
   }

   final static Any mkChar(char c) {return new Symbol(null, "" + c);}
   final static Any mkStr(String nm) {return nm == null || nm.length() == 0? Nil : new Symbol(null, nm);}
   final static Any mkStr(StringBuilder sb) {return mkStr(sb.toString());}
   final static Symbol mkSymbol(Any val) {return new Symbol(val, null);}

   final static Symbol mkSymbol(Any val, String nm, HashMap<String,Symbol> table) {
      Symbol sym;
      if ((sym = table.get(nm)) == null) {
         sym = new Symbol(val, nm);
         table.put(nm, sym);
      }
      return sym;
   }

   final static Any strToNum(String s, int scl) throws NumberFormatException {
      if (s.length() != 0 && s.charAt(0) == '+')
         s = s.substring(1);
      if (s.indexOf('.') <= 0)
         return new Number(s);
      return new Number((new BigDecimal(s)).setScale(scl, RoundingMode.HALF_UP).unscaledValue());
   }

   final static Any strToAtom(String s) {
      if (s.equals("NIL"))
         return Nil;
      try {return strToNum(s, ((Number)Scl.Car).Cnt);}
      catch (NumberFormatException e) {return mkSymbol(Nil, s, Intern);}
   }

   final static Any format(Any z, int scl, Any x) {
      char sep = '.', ign = '\0';
      if (x instanceof Cell) {
         sep = firstChar(x.Car.eval());
         if ((x = x.Cdr) instanceof Cell)
            ign = firstChar(x.Car.eval());
      }
      if (z instanceof Number)
         return mkStr(((Number)z).toString(scl,sep,ign));
      String s = z.name();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         if (c != ign)
            sb.append(c == sep? '.' : c);
      }
      try {return strToNum(sb.toString(), scl);}
      catch (NumberFormatException e) {return Nil;}
   }

   final static Any token(Any x, char c) {
      if (InFile.Chr == 0)
         InFile.get();
      if (InFile.skip(c) < 0)
         return null;
      if (InFile.Chr == '"') {
         InFile.get();
         if (InFile.Chr == '"') {
            InFile.get();
            return Nil;
         }
         if (!InFile.testEsc())
            return Nil;
         StringBuilder sb = new StringBuilder();
         sb.append((char)InFile.Chr);
         while (InFile.get() != '"' && InFile.testEsc())
            sb.append((char)InFile.Chr);
         InFile.get();
         return mkStr(sb);
      }
      if (InFile.Chr >= '0' && InFile.Chr <= '9') {
         StringBuilder sb = new StringBuilder();
         sb.append((char)InFile.Chr);
         while (InFile.get() >= '0' && InFile.Chr <= '9' || InFile.Chr == '.')
            sb.append((char)InFile.Chr);
         return strToAtom(sb.toString());
      }
      String s = x.name();
      if (InFile.Chr >= 'A' && InFile.Chr <= 'Z' || InFile.Chr == '\\' || InFile.Chr >= 'a' && InFile.Chr <= 'z' || s.indexOf(InFile.Chr) >= 0) {
         if (InFile.Chr == '\\')
            InFile.get();
         StringBuilder sb = new StringBuilder();
         sb.append((char)InFile.Chr);
         while (InFile.get() >= '0' && InFile.Chr <= '9' || InFile.Chr >= 'A' && InFile.Chr <= 'Z' || InFile.Chr == '\\' || InFile.Chr >= 'a' && InFile.Chr <= 'z' || s.indexOf(InFile.Chr) >= 0) {
            if (InFile.Chr == '\\')
               InFile.get();
            sb.append((char)InFile.Chr);
         }
         s = sb.toString();
         return s.equals("NIL")? Nil : mkSymbol(Nil, s, Intern);
      }
      return mkChar((char)InFile.get());
   }

   final static Any fish(Any ex, Any foo, Any[] v, Any res) {
      if (foo.apply(ex, false, v, 1) != Nil)
         return new Cell(v[0], res);
      if (v[0] instanceof Cell) {
         Any x = v[0];
         if ((v[0] = x.Cdr) != Nil)
            res = fish(ex, foo, v, res);
         v[0] = x.Car;
         res = fish(ex, foo, v, res);
         v[0] = x;
      }
      return res;
   }

   final static Any all(HashMap<String,Symbol> table) {
      Any x = Nil;
      for (Iterator<Symbol> it = table.values().iterator(); it.hasNext();)
         x = new Cell(it.next(), x);
      return x;
   }

   final static Any meta(Any x, Any y) {
      Any z;
      for (; x instanceof Cell; x = x.Cdr)
         if (x.Car instanceof Symbol && ((z = x.Car.get(y)) != Nil || (z = meta(x.Car.Car, y)) != Nil))
            return z;
      return Nil;
   }

   final static boolean isa(Any cls, Any x) {
      Any z;
      z = x = x.Car;
      while (x instanceof Cell) {
         if (!(x.Car instanceof Cell)) {
            while (x.Car instanceof Symbol) {
               if (cls == x.Car || isa(cls, x.Car))
                  return true;
               if (!((x = x.Cdr) instanceof Cell) || z == x)
                  return false;
            }
            return false;
         }
         if (z == (x = x.Cdr))
            return false;
      }
      return false;
   }

   final static void redefMsg(Any x, Any y) {
      StdErr.Wr.print("# ");
      StdErr.print(x);
      if (y != null) {
         StdErr.space();
         StdErr.print(y);
      }
      StdErr.Wr.println(" redefined");
      StdErr.Wr.flush();
   }

   final static void putSrc(Symbol s, Any k) {
      if (Dbg.Car != Nil && InFile != null && InFile.Name != null) {
         Any x = new Cell(new Number(InFile.Src), mkSymbol(null, InFile.Name, Transient));
         Any y = s.get(Dbg);
         if (k == null) {
            if (y == Nil)
               s.put(Dbg, new Cell(x, Nil));
            else
               y.Car = x;
         }
         else if (y == Nil)
            s.put(Dbg, new Cell(Nil, new Cell(x, Nil)));
         else {
            for (Any z = y.Cdr; z instanceof Cell; z = z.Cdr)
               if (z.Car.Car == k) {
                  z.Car.Cdr = x;
                  return;
               }
            y.Cdr = new Cell(new Cell(k, x), y.Cdr);
         }
      }
   }

   final static void redefine(Symbol s, Any x) {
      if (s.Car != Nil  &&  s != s.Car  &&  !x.equal(s.Car))
         redefMsg(s, null);
      s.Car = x;
      putSrc(s, null);
   }

   final static int xInt(Any x) {return ((Number)x).Cnt;}
   final static int evInt(Any ex) {return ((Number)ex.Car.eval()).Cnt;}
   final static long evLong(Any ex) {return ((Number)ex.Car.eval()).longValue();}
   final static String evString(Any ex) {return ex.Car.eval().name();}

   final static Any circ(Any x) {
      int m = 0;
      Any[] mark = new Any[12];
      for (;;) {
         mark = append(mark, m++, x);
         if (!((x = x.Cdr) instanceof Cell))
            return null;
         for (int i = 0; i < m; ++i)
            if (mark[i] == x)
               return x;
      }
   }

   final static Any fill(Any x, Any s) {
      Any y, z;
      if (x instanceof Number || x == Nil)
         return null;
      if (x instanceof Symbol)
         return (s==Nil? x!=At && firstChar(x)=='@' : memq(x,s)!=null)? x.Car : null;
      if ((y = fill(x.Car, s)) != null) {
         z = fill(x.Cdr, s);
         return new Cell(y, z == null? x.Cdr : z);
      }
      if ((y = fill(x.Cdr, s)) != null)
         return new Cell(x.Car, y);
      return null;
   }

   final static boolean isBlank(Any x) {
      if (x != Nil) {
         if (!(x instanceof Symbol))
            return false;
         String s = x.name();
         if (s != null)
            for (int i = 0; i < s.length(); ++i)
               if (s.charAt(i) > ' ')
                  return false;
      }
      return true;
   }

   final static Any funq(Any x) {
      Any y;
      if (x instanceof Symbol)
         return Nil;
      if (x instanceof Number)
         return ((Number)x).Big == null && ((Number)x).Cnt <= MaxFun? x : Nil;
      for (y = x.Cdr; y instanceof Cell; y = y.Cdr) {
         if (y == x)
            return Nil;
         if (y.Car instanceof Cell) {
            if (y.Car.Car instanceof Number) {
               if (y.Cdr instanceof Cell)
                  return Nil;
            }
            else if (y.Car.Car == Nil || y.Car.Car == T)
               return Nil;
         }
         else if (y.Cdr != Nil)
            return Nil;
      }
      if (y != Nil)
         return Nil;
      if ((x = x.Car) == Nil)
         return T;
      for (y = x; y instanceof Cell;)
         if (y.Car instanceof Number || y.Car instanceof Cell || y.Car == Nil || y.Car == T || x == (y = y.Cdr))
            return Nil;
      return y instanceof Number || y == T? Nil : x;
   }

   final static Any trim(Any x) {
      Any y;
      if (!(x instanceof Cell))
         return x;
      if ((y = trim(x.Cdr)) == Nil && isBlank(x.Car))
         return Nil;
      return new Cell(x.Car, y);
   }

   final static Any nCdr(int n, Any x) {
      while (--n >= 0)
         x = x.Cdr;
      return x;
   }

   final static Any nth(int n, Any x) {
      if (--n < 0)
         return Nil;
      return nCdr(n,x);
   }

   final static Any sort(Any ex, Any lst, Any foo) {
      Any x = lst, l = Nil, r = Nil, c = Nil;
      do {
         int i = foo == Nil? lst.Car.compare(x.Car) : foo.apply(ex, false, new Any[] {x.Car, lst.Car}, 2) == Nil? -1 : 1;
         if (i > 0)
            l = new Cell(x.Car, l);
         else if (i < 0)
            r = new Cell(x.Car, r);
         else
            c = new Cell(x.Car, c);
      } while ((x = x.Cdr) instanceof Cell);
      if ((lst = l) instanceof Cell) {
         if (l.Cdr instanceof Cell)
            for (lst = l = sort(ex, l, foo); (l = l.Cdr).Cdr instanceof Cell;);
         if (c instanceof Cell)
            for (l.Cdr = c; (l = l.Cdr).Cdr instanceof Cell;);
      }
      else if ((lst = c) instanceof Cell)
         for (l = c; l.Cdr instanceof Cell; l = l.Cdr);
      else
         return sort(ex, r, foo);
      if (r instanceof Cell)
         l.Cdr = r.Cdr instanceof Cell? sort(ex, r, foo) : r;
      return lst;
   }

   final static Any consIdx(Any x, Any y) {
      if (x.Cdr.Cdr instanceof Cell)
         y = consIdx(x.Cdr.Cdr, y);
      y = new Cell(x.Car, y);
      return x.Cdr.Car instanceof Cell? consIdx(x.Cdr.Car, y) : y;
   }

   final static Any idx(Any var, Any key, int flg) {
      Any x, y, z, p, tree, tos;
      boolean ad;
      int i;
      if (key == null)
         return var.Car instanceof Cell? consIdx(var.Car, Nil) : Nil;
      if (!((x = var.Car) instanceof Cell)) {
         if (flg > 0)
            var.Car = new Cell(key, Nil);
         return Nil;
      }
      p = var;
      ad = true;
      for (;;) {
         if ((i = key.compare(x.Car)) == 0) {
            if (flg < 0) {
               if (!(x.Cdr.Car instanceof Cell)) {
                  if (ad)
                     p.Car = x.Cdr.Cdr;
                  else
                     p.Cdr = x.Cdr.Cdr;
               }
               else if (!((y = x.Cdr.Cdr) instanceof Cell)) {
                  if (ad)
                     p.Car = x.Cdr.Car;
                  else
                     p.Cdr = x.Cdr.Car;
               }
               else if (!((z = y.Cdr.Car) instanceof Cell)) {
                  x.Car = y.Car;
                  x.Cdr.Cdr = y.Cdr.Cdr;
               }
               else {
                  while (z.Cdr.Car instanceof Cell)
                     z = (y = z).Cdr.Car;
                  x.Car = z.Car;
                  y.Cdr.Car = z.Cdr.Cdr;
               }
            }
            return x;
         }
         if (!(x.Cdr instanceof Cell)) {
            if (flg > 0)
               x.Cdr = i < 0? new Cell(new Cell(key, Nil), Nil) : new Cell(Nil, new Cell(key, Nil));
            return Nil;
         }
         if (i < 0) {
            if (!(x.Cdr.Car instanceof Cell)) {
               if (flg > 0)
                  x.Cdr.Car = new Cell(key, Nil);
               return Nil;
            }
            p = x.Cdr;  ad = true;
            x = p.Car;
         }
         else {
            if (!(x.Cdr.Cdr instanceof Cell)) {
               if (flg > 0)
                  x.Cdr.Cdr = new Cell(key, Nil);
               return Nil;
            }
            p = x.Cdr;  ad = false;
            x = p.Cdr;
         }
      }
   }

   final static Any consLup(Any x, Any y, Any from, Any to) {
      if (x instanceof Cell) {
         if (x.Car == T)
            return consLup(x.Cdr.Car, y, from, to);
         if (!(x.Car instanceof Cell))
            return consLup(x.Cdr.Cdr, y, from, to);
         if (to.compare(x.Car.Car) >= 0) {
            y = consLup(x.Cdr.Cdr, y, from, to);
            if (from.compare(x.Car.Car) <= 0) {
               y = new Cell(x.Car, y);
               return consLup(x.Cdr.Car, y, from, to);
            }
         }
         if (from.compare(x.Car.Car) <= 0)
            return consLup(x.Cdr.Car, y, from, to);
      }
      return y;
   }

   final static Any member(Any x, Any y) {
      Any z = y;

      while (y instanceof Cell) {
         if (x.equal(y.Car))
            return y;
         if (z == (y = y.Cdr))
            return null;
      }
      return y == Nil || !x.equal(y)? null : y;
   }

   final static Any memq(Any x, Any y) {
      Any z = y;

      while (y instanceof Cell) {
         if (x == y.Car)
            return y;
         if (z == (y = y.Cdr))
            return null;
      }
      return y == Nil || x != y? null : y;
   }

   final static int indx(Any x, Any y) {
      int i = 1;
      Any z = y;

      while (y instanceof Cell) {
         if (x.equal(y.Car))
            return i;
         ++i;
         if (z == (y = y.Cdr))
            return 0;
      }
      return 0;
   }

   final static boolean match(Any p, Any d) {
      Any x;
      for (;;) {
         if (!(p instanceof Cell)) {
            if (p instanceof Symbol  &&  firstChar(p) == '@') {
               p.Car = d;
               return true;
            }
            return p.equal(d);
         }
         if ((x = p.Car) instanceof Symbol  &&  firstChar(x) == '@') {
            if (!(d instanceof Cell)) {
               if (d.equal(p.Cdr)) {
                  x.Car = Nil;
                  return true;
               }
               return false;
            }
            if (match(p.Cdr, d.Cdr)) {
               x.Car = new Cell(d.Car, Nil);
               return true;
            }
            if (match(p.Cdr, d)) {
               x.Car = Nil;
               return true;
            }
            if (match(p, d.Cdr)) {
               x.Car = new Cell(d.Car, x.Car);
               return true;
            }
         }
         if (!(d instanceof Cell) || !match(x, d.Car))
            return false;
         p = p.Cdr;
         d = d.Cdr;
      }
   }

   final static boolean unify(Number n1, Any x1, Number n2, Any x2) {
      lookup1:
      while (x1 instanceof Symbol  &&  firstChar(x1) == '@') {
         for (Any x = Penv;  x.Car instanceof Cell;  x = x.Cdr)
            if (n1.Cnt == ((Number)x.Car.Car.Car).Cnt  &&  x1 == x.Car.Car.Cdr) {
               n1 = (Number)x.Car.Cdr.Car;
               x1 = x.Car.Cdr.Cdr;
               continue lookup1;
            }
         break;
      }
      lookup2:
      while (x2 instanceof Symbol  &&  firstChar(x2) == '@') {
         for (Any x = Penv;  x.Car instanceof Cell;  x = x.Cdr)
            if (n2.Cnt == ((Number)x.Car.Car.Car).Cnt  &&  x2 == x.Car.Car.Cdr) {
               n2 = (Number)x.Car.Cdr.Car;
               x2 = x.Car.Cdr.Cdr;
               continue lookup2;
            }
         break;
      }
      if (n1.Cnt == n2.Cnt  &&  x1.equal(x2))
         return true;
      if (x1 instanceof Symbol  &&  firstChar(x1) == '@') {
         if (x1 != At) {
            Penv = new Cell(new Cell(new Cell(n1,x1), Nil), Penv);
            Penv.Car.Cdr = new Cell(n2,x2);
         }
         return true;
      }
      if (x2 instanceof Symbol  &&  firstChar(x2) == '@') {
         if (x2 != At) {
            Penv = new Cell(new Cell(new Cell(n2,x2), Nil), Penv);
            Penv.Car.Cdr = new Cell(n1,x1);
         }
         return true;
      }
      if (!(x1 instanceof Cell) || !(x2 instanceof Cell))
         return x1.equal(x2);
      Any env = Penv;
      if (unify(n1, x1.Car, n2, x2.Car)  &&  unify(n1, x1.Cdr, n2, x2.Cdr))
         return true;
      Penv = env;
      return false;
   }

   final static Any lup(Number n, Any x) {
      lup:
      while (x instanceof Symbol  &&  firstChar(x) == '@') {
         for (Any y = Penv;  y.Car instanceof Cell;  y = y.Cdr)
            if (n.Cnt == ((Number)y.Car.Car.Car).Cnt  &&  x == y.Car.Car.Cdr) {
               n = (Number)y.Car.Cdr.Car;
               x = y.Car.Cdr.Cdr;
               continue lup;
            }
         break;
      }
      return x instanceof Cell? new Cell(lup(n, x.Car), lup(n, x.Cdr)) : x;
   }

   final static Any lookup(Number n, Any x) {
      return (x = lup(n,x)) instanceof Symbol && firstChar(x) == '@'?  Nil : x;
   }

   final static Any uniFill(Any x) {
      if (x instanceof Number)
         return x;
      if (x instanceof Symbol)
         return lup((Number)Pnl.Car, x);
      return new Cell(uniFill(x.Car), uniFill(x.Cdr));
   }

   final static Any evRun(boolean ev, Any x, int cnt, Any lst) {
      int i, j = cnt, n = 0;
      Bind b, bnd = Env.Bind;
      Any s, y, z;
      do {
         ++n;
         i = bnd.Eswp;
         bnd.Eswp -= cnt;
         if (i == 0) {
            for (i = 0; i < bnd.Cnt; i+= 2) {
               s = bnd.Data[i+1];
               y = s.Car;
               s.Car = bnd.Data[i];
               bnd.Data[i] = y;
            }
            if (bnd.Data[1] == At && --j == 0)
               break;
         }
      } while ((bnd = bnd.Link) != null);
      if (!(lst instanceof Cell))
         z = ev? x.eval() : x.run();
      else {
         bnd = new Bind();
         do {
            s = lst.Car;
            bnd.add(s.Car);
            bnd.add(s);
         exclude:
            for (b = Env.Bind, j = n; ;) {
               for (i = 0; i < b.Cnt; i+= 2)
                  if (s == b.Data[i+1]) {
                     s.Car = b.Data[i];
                     break exclude;
                  }
               if (--j == 0 || (b = b.Link) == null)
                  break;
            }
         } while ((lst = lst.Cdr) instanceof Cell);
         Env.Bind = bnd;
         z = ev? x.eval() : x.run();
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
      }
      do {
         for (bnd = Env.Bind, i = n; --i != 0; bnd = bnd.Link);
         if ((bnd.Eswp += cnt) == 0)
            for (i = bnd.Cnt; (i -= 2) >= 0;) {
               s = bnd.Data[i+1];
               y = s.Car;
               s.Car = bnd.Data[i];
               bnd.Data[i] = y;
            }
      } while (--n > 0);
      return z;
   }

   final static Any evMethod(Any o, Any ex, Any x) {
      int i;
      Any y = ex.Car;
      Any cls = TheCls,  key = TheKey;
      Bind bnd = new Bind();  bnd.add(At.Car);  bnd.add(At);
      while (y instanceof Cell) {
         bnd.add(x.Car.eval());  // Save new value
         bnd.add(y.Car);  // and symbol
         x = x.Cdr;
         y = y.Cdr;
      }
      if (y == Nil || y != At) {
         i = bnd.Cnt;
         if (y != Nil) {
            bnd.add(y.Car);  // Save old value
            bnd.add(y);  // and symbol
            y.Car = x;  // Set new value
         }
         do {
            y = bnd.Data[--i];
            x = y.Car;
            y.Car = bnd.Data[--i];  // Set new value
            bnd.Data[i] = x;  // Save old value
         } while (i > 0);
         bnd.add(This.Car);
         bnd.add(This);
         This.Car = o;
         Env.Bind = bnd;
         y = cls;  cls = Env.Cls;  Env.Cls = y;
         y = key;  key = Env.Key;  Env.Key = y;
         x = ex.Cdr.prog();
      }
      else {
         int next, argc, j = 0;
         Any arg, args[], av[] = null;
         if (x instanceof Cell) {
            av = new Any[6];
            do
               av = append(av, j++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
         }
         next = Env.Next;  Env.Next = 0;
         argc = Env.ArgC;  Env.ArgC = j;
         arg = Env.Arg;    Env.Arg = Nil;
         args = Env.Args;  Env.Args = av;
         i = bnd.Cnt;
         do {
            y = bnd.Data[--i];
            x = y.Car;
            y.Car = bnd.Data[--i];  // Set new value
            bnd.Data[i] = x;  // Save old value
         } while (i > 0);
         bnd.add(This.Car);
         bnd.add(This);
         This.Car = o;
         Env.Bind = bnd;
         y = cls;  cls = Env.Cls;  Env.Cls = y;
         y = key;  key = Env.Key;  Env.Key = y;
         x = ex.Cdr.prog();
         Env.Args = args;
         Env.Arg = arg;
      }
      for (i = bnd.Cnt; (i -= 2) >= 0;)
         bnd.Data[i+1].Car = bnd.Data[i];
      Env.Bind = bnd.Link;
      Env.Cls = cls;  Env.Key = key;
      return x;
   }

   final static Any method(Any x) {
      Any y, z;
      if ((y = x.Car) instanceof Cell) {
         while ((z = y.Car) instanceof Cell) {
            if (z.Car == TheKey)
               return z.Cdr;
            if (!((y = y.Cdr) instanceof Cell))
               return null;
         }
         do
            if ((x = method((TheCls = y).Car)) != null)
               return x;
         while ((y = y.Cdr) instanceof Cell);
      }
      return null;
   }

   final static Any extra(Any x) {
      Any y;
      for (x = x.Car; x.Car instanceof Cell; x = x.Cdr);
      while (x instanceof Cell) {
         if (x == Env.Cls  ||  (y = extra(x.Car)) == null) {
            while ((x = x.Cdr) instanceof Cell)
               if ((y = method((TheCls = x).Car)) != null)
                  return y;
            return null;
         }
         if (y != null  &&  y != T)
            return y;
         x = x.Cdr;
      }
      return T;
   }

   final static Any loop(Any x) {
      Any a, y, z;
      for (;;) {
         y = x;
         do {
            if ((z = y.Car) instanceof Cell) {
               if (z.Car == Nil) {
                  if ((a = (z = z.Cdr).Car.eval()) == Nil)
                     return z.Cdr.prog();
                  At.Car = a;
               }
               else if (z.Car == T) {
                  if ((a = (z = z.Cdr).Car.eval()) != Nil) {
                     At.Car = a;
                     return z.Cdr.prog();
                  }
               }
               else
                  z.eval();
            }
         } while ((y = y.Cdr) instanceof Cell);
      }
   }

   /* Ersatz PicoLisp Reader */
   final static class InFrame {
      InFrame Link;
      PicoLispReader Rd;
      int Pid;

      InFrame(PicoLispReader rd, int pid) {
         Link = Env.InFrames;
         Rd = rd;
         Pid = pid;
      }
   }

   final static class PicoLispReader {
      Reader Rd;
      String Name;
      char Eof1, Eof2;
      int Fd, Chr, Src, Ops;
      InputStream Stream;
      SelectableChannel Chan;
      SelectionKey Key;

      PicoLispReader(Reader rd, String nm, int fd, SelectableChannel chan, int ops) {
         Rd = rd;
         Name = nm;
         InFiles[Fd = fd] = this;
         Chan = chan;
         Ops = ops;
      }

      PicoLispReader(InputStream in, int fd, SelectableChannel chan, int ops) {
         this(in == null? null : new InputStreamReader(in), null, fd, chan, ops);
         Stream = in;
      }

      PicoLispReader(String s, char eof1, char eof2) {
         Rd = new StringReader(s);
         Eof1 = eof1;
         Eof2 = eof2;
      }

      final boolean register(Selector sel) {
         if (Ops != 0) {
            try {
               Chan.configureBlocking(false);
               Key = Chan.register(sel, Ops);
               return true;
            }
            catch (IOException e) {}
         }
         return false;
      }

      final boolean ready(Selector sel) throws IOException {
         if (Key == null)
            return Rd.ready() || Stream != null && Stream.available() > 0;
         boolean rdy = (Key.readyOps() & Ops) != 0;
         Key.cancel();
         Key = null;
         try{Chan.configureBlocking(true);}
         catch (IOException e) {}
         return rdy;
      }

      final void close() {
         try {
            if (Chan != null)
               Chan.close();
            if (Rd != null)
               Rd.close();
            InFiles[Fd] = null;
         }
         catch (IOException e) {closeErr(e);}
      }

      final void eofErr() {err(null, null, "EOF Overrun");}

      final int get() {
         try {
            if (this != StdIn || Term == null)
               Chr = Rd.read();
            else {
               if (Line == null) {
                  waitFd(null, 0, -1);
                  ((Pipe.SourceChannel)StdIn.Chan).read(ByteBuffer.allocate(1));
               }
               if (Line == null)
                  Chr = -1;
               else {
                  Chr = Line.charAt(0);
                  if (Line.deleteCharAt(0).length() == 0)
                     Line = null;
               }
            }
            if (Chr < 0) {
               if ((Chr = Eof1) != 0)
                  Eof1 = '\0';
               else if ((Chr = Eof2) != 0)
                  Eof2 = '\0';
               else
                  Chr = -1;
            }
            return Chr;
         }
         catch (IOException e) {return -1;}
      }

      final boolean eol() {
         if (Chr < 0)
            return true;
         if (Chr == '\n') {
            Chr = 0;
            return true;
         }
         if (Chr == '\r') {
            get();
            if (Chr == '\n')
               Chr = 0;
            return true;
         }
         return false;
      }

      final int skip(int c) {
         for (;;) {
            if (Chr < 0)
               return Chr;
            while (Chr <= ' ') {
               get();
               if (Chr < 0)
                  return Chr;
            }
            if (Chr != c)
               return Chr;
            get();
            if (c != '#' || Chr != '{') {
               while (Chr != '\n') {
                  if (Chr < 0)
                     return Chr;
                  get();
               }
            }
            else {
               for (;;) {
                  get();
                  if (Chr < 0)
                     return Chr;
                  if (Chr == '}' && (get() == '#'))
                     break;
               }
            }
            get();
         }
      }

      final boolean testEsc() {
         for (;;) {
            if (Chr < 0)
               return false;
            if (Chr == '^') {
               get();
               if (Chr == '?')
                  Chr = 127;
               else
                  Chr &= 0x1F;
               return true;
            }
            if (Chr != '\\')
               return true;
            if (get() != '\n')
               return true;
            do
               get();
            while (Chr == ' ' || Chr == '\t');
         }
      }

      final Any rdAtom(int c) {
         StringBuilder sb = new StringBuilder();
         sb.append((char)c);
         while (Chr > 0 && Delim.indexOf(Chr) < 0) {
            if (Chr == '\\')
               get();
            sb.append((char)Chr);
            get();
         }
         return strToAtom(sb.toString());
      }

      final Any rdList() {
         Any x, res;
         get();
         for (;;) {
            if (skip('#') == ')') {
               get();
               return Nil;
            }
            if (Chr == ']')
               return Nil;
            if (Chr != '~') {
               res = x = new Cell(read0(false), Nil);
               break;
            }
            get();
            if ((res = x = read0(false).eval()) instanceof Cell) {
               while (x.Cdr instanceof Cell)
                  x = x.Cdr;
               break;
            }
         }
         for (;;) {
            if (skip('#') == ')') {
               get();
               break;
            }
            if (Chr == ']')
               break;
            if (Chr == '.') {
               get();
               if (Delim.indexOf(Chr) >= 0) {
                  x.Cdr = skip('#')==')' || Chr==']'? res : read0(false);
                  if (skip('#') == ')')
                     get();
                  else if (Chr != ']')
                     err(null, x, "Bad dotted pair");
                  break;
               }
               x = x.Cdr = new Cell(rdAtom('.'), Nil);
            }
            else if (Chr != '~')
               x = x.Cdr = new Cell(read0(false), Nil);
            else {
               get();
               x.Cdr = read0(false).eval();
               while (x.Cdr instanceof Cell)
                  x = x.Cdr;
            }
         }
         return res;
      }

      final Any read0(boolean top) {
         Any x, y;
         if (skip('#') < 0) {
            if (top)
               return Nil;
            eofErr();
         }
         if (top && InFile != null && InFile.Rd instanceof LineNumberReader)
            InFile.Src = ((LineNumberReader)InFile.Rd).getLineNumber() + 1;
         if (Chr == '(') {
            x = rdList();
            if (top  &&  Chr == ']')
               get();
            return x;
         }
         if (Chr == '[') {
            x = rdList();
            if (Chr != ']')
               err(null, x, "Super parentheses mismatch");
            get();
            return x;
         }
         if (Chr == '\'') {
            get();
            return new Cell(Quote, read0(false));
         }
         if (Chr == ',') {
            get();
            return (y = idx(Uni, x = read0(false), 1)) instanceof Cell? y.Car : x;
         }
         if (Chr == '`') {
            get();
            return read0(false).eval();
         }
         if (Chr == '"') {
            get();
            if (Chr == '"') {
               get();
               return Nil;
            }
            if (!testEsc())
               eofErr();
            StringBuilder sb = new StringBuilder();
            sb.append((char)Chr);
            while (get() != '"') {
               if (!testEsc())
                  eofErr();
               sb.append((char)Chr);
            }
            get();
            return mkSymbol(null, sb.toString(), Transient);
         }
         if (Chr == ')' || Chr == ']' || Chr == '~')
            err(null, null, "Bad input '" + (char)Chr + "' (" + Chr + ")");
         if (Chr == '\\')
            get();
         int i = Chr;
         get();
         return rdAtom(i);
      }

      final Any read(int end) {
         if (Chr == 0)
            get();
         if (Chr == end)
            return Nil;
         Any x = read0(true);
         while (Chr != 0  &&  " \t)]".indexOf(Chr) >= 0)
            get();
         return x;
      }
   }

   /* Ersatz PicoLisp Printer */
   final static class OutFrame {
      OutFrame Link;
      PicoLispWriter Wr;
      int Pid;

      OutFrame(PicoLispWriter wr, int pid) {
         Link = Env.OutFrames;
         Wr = wr;
         Pid = pid;
      }
   }

   final static class PicoLispWriter {
      PrintWriter Wr;
      String Name;
      int Fd;

      PicoLispWriter(PrintWriter wr, String nm, int fd) {
         Wr = wr;
         Name = nm;
         OutFiles[Fd = fd] = this;
      }

      PicoLispWriter(OutputStream out, int fd) {
         this(new PrintWriter(out), null, fd);
      }

      final void close() {
         Wr.close();
         OutFiles[Fd] = null;
      }

      final void print(Any x) {Wr.print(x.toString());}
      final void space() {Wr.print(' ');}

      final void newline() {
         Wr.println();
         Wr.flush();
      }
   }

   /* Ersatz PicoLisp VM */
   final static class Bind {
      Bind Link;
      Any[] Data;
      int Cnt, Eswp;

      Bind() {
         Link = Env.Bind;
         Data = new Any[12];
      }

      final void add(Any x) {Data = append(Data, Cnt++, x);}
   }

   final static class Env {
      int Next, ArgC, Trace;
      Bind Bind;
      Any Arg, Args[], Cls, Key, Task, Make, Yoke;
      InFrame InFrames;
      OutFrame OutFrames;

      Env() {}

      Env(Env env) {
         Next = env.Next;  ArgC = env.ArgC;  Trace = env.Trace;
         Bind = env.Bind;
         Arg = env.Arg;  Args = env.Args;
         Cls = env.Cls;  Key = env.Key;
         Task = env.Task;
         Make = env.Make;  Yoke = env.Yoke;
         InFrames = env.InFrames;  OutFrames = env.OutFrames;
      }

      final void pushInFile(InFrame in) {
         InFrames = in;
         InFile = InFiles[in.Rd.Fd];
      }

      final void popInFiles() {
         if (InFrames.Pid != 0) {
            InFile.close();
            if (InFrames.Pid > 1) {
               try {
                  if (Pids[InFrames.Pid].waitFor() != 0)
                     err(null, null, "Pipe read close error");
                  Pids[InFrames.Pid] = null;
               }
               catch (InterruptedException e) {}  //#! sighandler()
            }
         }
         InFile = (InFrames = InFrames.Link) == null? StdIn : InFiles[InFrames.Rd.Fd];
      }

      final void pushOutFile(OutFrame out) {
         OutFrames = out;
         OutFile = OutFiles[out.Wr.Fd];
      }

      final void popOutFiles() {
         if (OutFrames.Pid != 0) {
            OutFile.close();
            if (OutFrames.Pid > 1) {
               try {
                  if (Pids[OutFrames.Pid].waitFor() != 0)
                     err(null, null, "Pipe write close error");
                  Pids[OutFrames.Pid] = null;
               }
               catch (InterruptedException e) {}  //#! sighandler()
            }
         }
         OutFile = (OutFrames = OutFrames.Link) == null? StdOut : OutFiles[OutFrames.Wr.Fd];
      }
   }

   final static class Catch {
      Catch Link;
      Any Tag, Fin;
      Env Env;

      Catch(Any tag, Any fin, Env env) {
         Tag = tag;
         Fin = fin;
         Env = new Env(env);
         Link = Catch;  Catch = this;
      }
   }

   final static class Control extends RuntimeException {
      Any Tag, Val;

      Control() {}

      Control(Any ex, Any tag, Any val) {
         Tag = tag;
         Val = val;
         for (Catch p = Catch; p != null; p = p.Link)
            if (p.Tag == T  ||  p.Tag == tag) {
               unwind(p);
               return;
            }
         err(ex, tag, "Tag not found");
      }
   }

   static abstract class Any {
      Any Car, Cdr;

      abstract Any put(Any key, Any val);
      abstract Any get(Any key);
      abstract Any prop(Any key);
      abstract Any putl(Any lst);
      abstract Any getl();
      abstract Any eval();
      abstract Any prog();
      abstract Any run();
      abstract Any call(Any ex);
      abstract Any func(Any ex);
      abstract Any apply(Any ex, boolean cf, Any[] v, int n);
      abstract boolean equal(Any x);
      abstract int compare(Any x);
      abstract long length();
      abstract long size();
      abstract InFrame rdOpen(Any ex);
      abstract OutFrame wrOpen(Any ex);
      abstract String name();
   }

   final static class Number extends Any {
      int Cnt;
      BigInteger Big;

      Number(int i) {Cnt = i;}

      Number(long n) {
         if (n >= Integer.MIN_VALUE  && n <= Integer.MAX_VALUE)
            Cnt = (int)n;
         else
            Big = new BigInteger(new byte[] {(byte)(n>>56), (byte)(n>>48), (byte)(n>>40), (byte)(n>>32), (byte)(n>>24), (byte)(n>>16), (byte)(n>>8), (byte)n});
      }

      Number(BigInteger b) {
         if (b.bitLength() < 32)
            Cnt = b.intValue();
         else
            Big = b;
      }

      Number(String s) {
         try {Cnt = Integer.parseInt(s);}
         catch (NumberFormatException e) {Big = new BigInteger(s);}
      }

      final long longValue() {return Big == null? Cnt : Big.longValue();}

      final static BigInteger big(int i) {
         return new BigInteger(new byte[] {(byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i});
      }

      final Any put(Any key, Any val) {return symError(this);}
      final Any get(Any key) {return symError(this);}
      final Any prop(Any key) {return symError(this);}
      final Any putl(Any lst) {return symError(this);}
      final Any getl() {return symError(this);}
      final Any eval() {return this;}
      final Any prog() {return execError(this);}
      final Any run() {return execError(this);}
      final Any call(Any ex) {return ex;}

      final Any func(Any ex) {
         try {
            switch(Cnt) {
            case 0:  // (quote . any) -> any
               return ex.Cdr;
            case 1:  // (meth 'obj ['any ..]) -> any
               return doMeth(ex);
            case 2:  // quit
               return do2(ex);
            case 3:  // public
               return do3(ex);
            case 4:  // java
               return do4(ex);
            case 5:  // byte:
               return do5(ex);
            case 6:  // char:
               return do6(ex);
            case 7:  // int:
               return do7(ex);
            case 8:  // long:
               return do8(ex);
            case 9:  // double:
               return do9(ex);
            case 10:  // big:
               return do10(ex);
            case 11:  // data
               return do11(ex);
            case 12:  // args
               return Env.Next < Env.ArgC? T : Nil;
            case 13:  // next
               return do13(ex);
            case 14:  // arg
               return do14(ex);
            case 15:  // rest
               return do15(ex);
            case 16:  // date
               return do16(ex);
            case 17:  // time
               return do17(ex);
            case 18:  // usec
               return do18(ex);
            case 19:  // file
               return do19(ex);
            case 20:  // version
               return do20(ex);
            case 21:  // apply
               return do21(ex);
            case 22:  // pass
               return do22(ex);
            case 23:  // maps
               return do23(ex);
            case 24:  // map
               return do24(ex);
            case 25:  // mapc
               return do25(ex);
            case 26:  // maplist
               return do26(ex);
            case 27:  // mapcar
               return do27(ex);
            case 28:  // mapcon
               return do28(ex);
            case 29:  // mapcan
               return do29(ex);
            case 30:  // filter
               return do30(ex);
            case 31:  // extract
               return do31(ex);
            case 32:  // seek
               return do32(ex);
            case 33:  // find
               return do33(ex);
            case 34:  // pick
               return do34(ex);
            case 35:  // cnt
               return do35(ex);
            case 36:  // sum
               return do36(ex);
            case 37:  // maxi
               return do37(ex);
            case 38:  // mini
               return do38(ex);
            case 39:  // fish
               return do39(ex);
            case 40:  // by
               return do40(ex);
            case 41:  // as
               return do41(ex);
            case 42:  // lit
               return do42(ex);
            case 43:  // eval
               return do43(ex);
            case 44:  // run
               return do44(ex);
            case 45:  // def
               return do45(ex);
            case 46:  // de
               return do46(ex);
            case 47:  // dm
               return do47(ex);
            case 48:  // box
               return do48(ex);
            case 49:  // new
               return do49(ex);
            case 50:  // type
               return do50(ex);
            case 51:  // isa
               return do51(ex);
            case 52:  // method
               return do52(ex);
            case 53:  // send
               return do53(ex);
            case 54:  // try
               return do54(ex);
            case 55:  // super
               return do55(ex);
            case 56:  // extra
               return do56(ex);
            case 57:  // with
               return do57(ex);
            case 58:  // bind
               return do58(ex);
            case 59:  // job
               return do59(ex);
            case 60:  // let
               return do60(ex);
            case 61:  // let?
               return do61(ex);
            case 62:  // use
               return do62(ex);
            case 63:  // and
               return do63(ex);
            case 64:  // or
               return do64(ex);
            case 65:  // nand
               return do65(ex);
            case 66:  // nor
               return do66(ex);
            case 67:  // xor
               return do67(ex);
            case 68:  // bool
               return do68(ex);
            case 69:  // not
               return do69(ex);
            case 70:  // nil
               return do70(ex);
            case 71:  // t
               return do71(ex);
            case 72:  // prog
               return ex.Cdr.prog();
            case 73:  // prog1
               return do73(ex);
            case 74:  // prog2
               return do74(ex);
            case 75:  // if
               return do75(ex);
            case 76:  // if2
               return do76(ex);
            case 77:  // ifn
               return do77(ex);
            case 78:  // when
               return do78(ex);
            case 79:  // unless
               return do79(ex);
            case 80:  // cond
               return do80(ex);
            case 81:  // nond
               return do81(ex);
            case 82:  // case
               return do82(ex);
            case 83:  // state
               return do83(ex);
            case 84:  // while
               return do84(ex);
            case 85:  // until
               return do85(ex);
            case 86:  // do
               return do86(ex);
            case 87:  // loop
               return loop(ex.Cdr);
            case 88:  // at
               return do88(ex);
            case 89:  // for
               return do89(ex);
            case 90:  // catch
               return do90(ex);
            case 91:  // throw
               return do91(ex);
            case 92:  // finally
               return do92(ex);
            case 93:  // !
               return do93(ex);
            case 94:  // e
               return do94(ex);
            case 95:  // $
               return do95(ex);
            case 96:  // sys
               return do96(ex);
            case 97:  // call
               return do97(ex);
            case 98:  // ipid
               return do98(ex);
            case 99:  // opid
               return do99(ex);
            case 100:  // kill
               return do100(ex);
            case 101:  // bye
               return do101(ex);
            case 102:  // name
               return do102(ex);
            case 103:  // sp?
               return do103(ex);
            case 104:  // pat?
               return do104(ex);
            case 105:  // fun?
               return do105(ex);
            case 106:  // getd
               return do106(ex);
            case 107:  // all
               return do107(ex);
            case 108:  // intern
               return do108(ex);
            case 109:  // ====
               return do109(ex);
            case 110:  // box?
               return do110(ex);
            case 111:  // str?
               return do111(ex);
            case 112:  // zap
               return do112(ex);
            case 113:  // chop
               return do113(ex);
            case 114:  // pack
               return do114(ex);
            case 115:  // glue
               return do115(ex);
            case 116:  // text
               return do116(ex);
            case 117:  // pre?
               return do117(ex);
            case 118:  // sub?
               return do118(ex);
            case 119:  // val
               return ex.Cdr.Car.eval().Car;
            case 120:  // set
               return do120(ex);
            case 121:  // setq
               return do121(ex);
            case 122:  // xchg
               return do122(ex);
            case 123:  // on
               return do123(ex);
            case 124:  // off
               return do124(ex);
            case 125:  // onOff
               return do125(ex);
            case 126:  // zero
               return do126(ex);
            case 127:  // one
               return do127(ex);
            case 128:  // default
               return do128(ex);
            case 129:  // push
               return do129(ex);
            case 130:  // push1
               return do130(ex);
            case 131:  // pop
               return do131(ex);
            case 132:  // cut
               return do132(ex);
            case 133:  // del
               return do133(ex);
            case 134:  // queue
               return do134(ex);
            case 135:  // fifo
               return do135(ex);
            case 136:  // idx
               return do136(ex);
            case 137:  // lup
               return do137(ex);
            case 138:  // put
               return do138(ex);
            case 139:  // get
               return do139(ex);
            case 140:  // prop
               return do140(ex);
            case 141:  // ;
               return do141(ex);
            case 142:  // =:
               return do142(ex);
            case 143:  // :
               return do143(ex);
            case 144:  // ::
               return do144(ex);
            case 145:  // putl
               return do145(ex);
            case 146:  // getl
               return do146(ex);
            case 147:  // meta
               return do147(ex);
            case 148:  // low?
               return do148(ex);
            case 149:  // upp?
               return do149(ex);
            case 150:  // lowc
               return do150(ex);
            case 151:  // uppc
               return do151(ex);
            case 152:  // fold
               return do152(ex);
            case 153:  // car
               return ex.Cdr.Car.eval().Car;
            case 154:  // cdr
               return ex.Cdr.Car.eval().Cdr;
            case 155:  // caar
               return ex.Cdr.Car.eval().Car.Car;
            case 156:  // cadr
               return ex.Cdr.Car.eval().Cdr.Car;
            case 157:  // cdar
               return ex.Cdr.Car.eval().Car.Cdr;
            case 158:  // cddr
               return ex.Cdr.Car.eval().Cdr.Cdr;
            case 159:  // caaar
               return do159(ex);
            case 160:  // caadr
               return do160(ex);
            case 161:  // cadar
               return do161(ex);
            case 162:  // caddr
               return do162(ex);
            case 163:  // cdaar
               return do163(ex);
            case 164:  // cdadr
               return do164(ex);
            case 165:  // cddar
               return do165(ex);
            case 166:  // cdddr
               return do166(ex);
            case 167:  // caaaar
               return do167(ex);
            case 168:  // caaadr
               return do168(ex);
            case 169:  // caadar
               return do169(ex);
            case 170:  // caaddr
               return do170(ex);
            case 171:  // cadaar
               return do171(ex);
            case 172:  // cadadr
               return do172(ex);
            case 173:  // caddar
               return do173(ex);
            case 174:  // cadddr
               return do174(ex);
            case 175:  // cdaaar
               return do175(ex);
            case 176:  // cdaadr
               return do176(ex);
            case 177:  // cdadar
               return do177(ex);
            case 178:  // cdaddr
               return do178(ex);
            case 179:  // cddaar
               return do179(ex);
            case 180:  // cddadr
               return do180(ex);
            case 181:  // cdddar
               return do181(ex);
            case 182:  // cddddr
               return do182(ex);
            case 183:  // nth
               return do183(ex);
            case 184:  // con
               return do184(ex);
            case 185:  // cons
               return do185(ex);
            case 186:  // conc
               return do186(ex);
            case 187:  // circ
               return do187(ex);
            case 188:  // rot
               return do188(ex);
            case 189:  // list
               return do189(ex);
            case 190:  // need
               return do190(ex);
            case 191:  // range
               return do191(ex);
            case 192:  // full
               return do192(ex);
            case 193:  // make
               return do193(ex);
            case 194:  // made
               return do194(ex);
            case 195:  // chain
               return do195(ex);
            case 196:  // link
               return do196(ex);
            case 197:  // yoke
               return do197(ex);
            case 198:  // copy
               return do198(ex);
            case 199:  // mix
               return do199(ex);
            case 200:  // append
               return do200(ex);
            case 201:  // delete
               return do201(ex);
            case 202:  // delq
               return do202(ex);
            case 203:  // replace
               return do203(ex);
            case 204:  // strip
               return do204(ex);
            case 205:  // split
               return do205(ex);
            case 206:  // reverse
               return do206(ex);
            case 207:  // flip
               return do207(ex);
            case 208:  // trim
               return do208(ex);
            case 209:  // clip
               return do209(ex);
            case 210:  // head
               return do210(ex);
            case 211:  // tail
               return do211(ex);
            case 212:  // stem
               return do212(ex);
            case 213:  // fin
               return do213(ex);
            case 214:  // last
               return do214(ex);
            case 215:  // ==
               return do215(ex);
            case 216:  // n==
               return do216(ex);
            case 217:  // =
               return do217(ex);
            case 218:  // <>
               return do218(ex);
            case 219:  // =0
               return do219(ex);
            case 220:  // =T
               return do220(ex);
            case 221:  // n0
               return do221(ex);
            case 222:  // nT
               return do222(ex);
            case 223:  // <
               return do223(ex);
            case 224:  // <=
               return do224(ex);
            case 225:  // >
               return do225(ex);
            case 226:  // >=
               return do226(ex);
            case 227:  // max
               return do227(ex);
            case 228:  // min
               return do228(ex);
            case 229:  // atom
               return do229(ex);
            case 230:  // pair
               return do230(ex);
            case 231:  // lst?
               return do231(ex);
            case 232:  // num?
               return do232(ex);
            case 233:  // sym?
               return do233(ex);
            case 234:  // flg?
               return do234(ex);
            case 235:  // member
               return do235(ex);
            case 236:  // memq
               return do236(ex);
            case 237:  // mmeq
               return do237(ex);
            case 238:  // sect
               return do238(ex);
            case 239:  // diff
               return do239(ex);
            case 240:  // index
               return do240(ex);
            case 241:  // offset
               return do241(ex);
            case 242:  // length
               return do242(ex);
            case 243:  // size
               return do243(ex);
            case 244:  // assoc
               return do244(ex);
            case 245:  // asoq
               return do245(ex);
            case 246:  // rank
               return do246(ex);
            case 247:  // match
               return do247(ex);
            case 248:  // fill
               return do248(ex);
            case 249:  // prove
               return do249(ex);
            case 250:  // ->
               return do250(ex);
            case 251:  // unify
               return do251(ex);
            case 252:  // sort
               return do252(ex);
            case 253:  // format
               return do253(ex);
            case 254:  // +
               return do254(ex);
            case 255:  // -
               return do255(ex);
            case 256:  // inc
               return do256(ex);
            case 257:  // dec
               return do257(ex);
            case 258:  // *
               return do258(ex);
            case 259:  // */
               return do259(ex);
            case 260:  // /
               return do260(ex);
            case 261:  // %
               return do261(ex);
            case 262:  // >>
               return do262(ex);
            case 263:  // lt0
               return do263(ex);
            case 264:  // ge0
               return do264(ex);
            case 265:  // gt0
               return do265(ex);
            case 266:  // abs
               return do266(ex);
            case 267:  // bit?
               return do267(ex);
            case 268:  // &
               return do268(ex);
            case 269:  // |
               return do269(ex);
            case 270:  // x|
               return do270(ex);
            case 271:  // seed
               return do271(ex);
            case 272:  // rand
               return do272(ex);
            case 273:  // path
               return do273(ex);
            case 274:  // read
               return do274(ex);
            case 275:  // wait
               return do275(ex);
            case 276:  // char
               return do276(ex);
            case 277:  // skip
               return do277(ex);
            case 278:  // line
               return do278(ex);
            case 279:  // load
               return do279(ex);
            case 280:  // in
               return do280(ex);
            case 281:  // out
               return do281(ex);
            case 282:  // open
               return do282(ex);
            case 283:  // close
               return do283(ex);
            case 284:  // prin
               return do284(ex);
            case 285:  // prinl
               return do285(ex);
            case 286:  // space
               return do286(ex);
            case 287:  // print
               return do287(ex);
            case 288:  // printsp
               return do288(ex);
            case 289:  // println
               return do289(ex);
            case 290:  // flush
               return do290(ex);
            case 291:  // port
               return do291(ex);
            case 292:  // accept
               return do292(ex);
            case 293:  // connect
               return do293(ex);
            default:
               return undefined(this, ex);
            }
         }
         catch (Throwable e) {
            if (e instanceof Control)
               throw (Control)e;
            return err(ex, null, e.toString());
         }
      }

      final static Any doMeth(Any ex) {
         Any x, y, z;
         z = (x = ex.Cdr).Car.eval();
         for (TheKey = ex.Car; ; TheKey = TheKey.Car)
            if (TheKey.Car instanceof Number) {
               TheCls = null;
               if ((y = method(z)) != null)
                  return evMethod(z, y, x.Cdr);
               err(ex, TheKey, "Bad message");
            }
      }

      final static Any do2(Any ex) { // quit
         String str;
         str = evString(ex = ex.Cdr);
         return err(null, (ex = ex.Cdr) instanceof Cell? ex.Car.eval() : null, str);
      }

      final static Any do3(Any ex) { // public
         Any x, y, z;
         Symbol s;
         Object o;
         y = (x = ex.Cdr).Car.eval();
         z = (x = x.Cdr).Car.eval();
         try {
            if ((s = (Symbol)y).Obj != null)
               o = s.Obj.getClass().getField(z.name()).get(s.Obj);
            else {
               java.lang.Class cls = java.lang.Class.forName(s.Name);
               o = cls.getField(z.name()).get(cls);
            }
            return new Symbol(o);
         }
         catch (Exception e) {return err(ex, null, e.toString());}
      }

      final static Any do4(Any ex) { // java
         int i, j, k;
         Any x, y, z;
         Symbol s;
         Number num;
         Any[] v;
         Object o;
         y = (x = ex.Cdr).Car.eval();
         z = (x = x.Cdr).Car.eval();
         for (v = new Any[6], i = 0; (x = x.Cdr) instanceof Cell;)
            v = append(v, i++, x.Car.eval());
         Object[] arg = new Object[i];
         Class[] par = new Class[i];
         while (--i >= 0) {
            if (v[i] == Nil || v[i] == T) {
               arg[i] = v[i] == T;
               par[i] = Boolean.TYPE;
            }
            else if (v[i] instanceof Number) {
               if ((num = (Number)v[i]).Big != null)
                  cntError(ex, num);
               arg[i] = new Integer(num.Cnt);
               par[i] = Integer.TYPE;
            }
            else if (v[i] instanceof Cell) {
               k = (int)v[i].length();
               if (v[i].Car instanceof Number) {
                  arg[i] = new int[k];
                  for (j = 0; j < k; ++j, v[i] = v[i].Cdr)
                     Array.setInt(arg[i], j, ((Number)v[i].Car).Cnt);
               }
               else if (v[i].Car instanceof Cell)
                  argError(ex, v[i]);
               else if ((s = (Symbol)v[i].Car).Obj == null) {
                  arg[i] = Array.newInstance(s.Name.getClass(), k);
                  for (j = 0; j < k; ++j, v[i] = v[i].Cdr)
                     Array.set(arg[i], j, ((Symbol)v[i].Car).Name);
               }
               else {
                  if (s.Obj instanceof Byte)
                     arg[i] = Array.newInstance(Byte.TYPE, k);
                  else if (s.Obj instanceof Character)
                     arg[i] = Array.newInstance(Character.TYPE, k);
                  else if (s.Obj instanceof Integer)
                     arg[i] = Array.newInstance(Integer.TYPE, k);
                  else if (s.Obj instanceof Long)
                     arg[i] = Array.newInstance(Long.TYPE, k);
                  else
                     arg[i] = Array.newInstance(s.Obj.getClass(), k);
                  for (j = 0; j < k; ++j, v[i] = v[i].Cdr)
                     Array.set(arg[i], j, ((Symbol)v[i].Car).Obj);
               }
               par[i] = arg[i].getClass();
            }
            else if ((s = (Symbol)v[i]).Obj == null)
               par[i] = (arg[i] = s.Name).getClass();
            else {
               arg[i] = s.Obj;
               if (s.Obj instanceof Byte)
                  par[i] = Byte.TYPE;
               else if (s.Obj instanceof Character)
                  par[i] = Character.TYPE;
               else if (s.Obj instanceof Integer)
                  par[i] = Integer.TYPE;
               else if (s.Obj instanceof Long)
                  par[i] = Long.TYPE;
               else
                  par[i] = s.Obj.getClass();
            }
         }
         try {
            if (z == T)
               return new Symbol(java.lang.Class.forName(y.name()).getConstructor(par).newInstance(arg));
            Method m = (s = (Symbol)y).Obj == null? java.lang.Class.forName(s.Name).getMethod(z.name(),par) : s.Obj.getClass().getMethod(z.name(),par);
            o = m.invoke(s.Obj, arg);
            if (m.getReturnType() == Void.TYPE)
               return Nil;
            return new Symbol(o);
         }
         catch (Exception e) {return err(ex, null, e.toString());}
      }

      final static Any do5(Any ex) { // byte:
         Any x;
         x = ex.Cdr.Car.eval();
         return new Symbol(new Byte(x instanceof Number? (byte)((Number)x).Cnt : (byte)x.name().charAt(0)));
      }

      final static Any do6(Any ex) { // char:
         Any x;
         x = ex.Cdr.Car.eval();
         return new Symbol(new Character(x instanceof Number? (char)((Number)x).Cnt : x.name().charAt(0)));
      }

      final static Any do7(Any ex) { // int:
         return new Symbol(new Integer(evInt(ex.Cdr)));
      }

      final static Any do8(Any ex) { // long:
         return new Symbol(new Long(evLong(ex.Cdr)));
      }

      final static Any do9(Any ex) { // double:
         Any x;
         if ((x = (ex = ex.Cdr).Car.eval()) instanceof Number)
            return new Symbol(new Double(((Number)x).toString(evInt(ex.Cdr), '.', '\0')));
         return new Symbol(new Double(x.name()));
      }

      final static Any do10(Any ex) { // big:
         Number num;
         num = (Number)(ex.Cdr.Car.eval());
         return new Symbol(num.Big == null? big(num.Cnt) : num.Big);
      }

      final static Any do11(Any ex) { // data
         int i, j;
         Any x, y;
         Symbol s;
         x = Nil;
         if ((y = ex.Cdr.Car.eval()) instanceof Symbol && (s = (Symbol)y).Obj != null) {
            if (s.Obj instanceof Byte)
               x = new Number(((Byte)s.Obj).byteValue());
            else if (s.Obj instanceof Character)
               x = new Number(((Character)s.Obj).charValue());
            else if (s.Obj instanceof Integer)
               x = new Number(((Integer)s.Obj).intValue());
            else if (s.Obj instanceof Long)
               x = new Number(((Long)s.Obj).longValue());
            else if (s.Obj instanceof Double)
               x = strToNum(Double.toString(((Double)s.Obj).doubleValue()),  evInt(ex.Cdr.Cdr));
            else if (s.Obj instanceof BigInteger)
               x = new Number((BigInteger)s.Obj);
            else if (s.Obj instanceof String)
               x = mkStr((String)s.Obj);
            else if (s.Obj instanceof byte[]) {
               byte[] a = (byte[])s.Obj;
               for (i = a.length; --i >= 0;)
                  x = new Cell(new Number(a[i]), x);
            }
            else if (s.Obj instanceof char[]) {
               char[] a = (char[])s.Obj;
               for (i = a.length; --i >= 0;)
                  x = new Cell(new Number(a[i]), x);
            }
            else if (s.Obj instanceof int[]) {
               int[] a = (int[])s.Obj;
               for (i = a.length; --i >= 0;)
                  x = new Cell(new Number(a[i]), x);
            }
            else if (s.Obj instanceof long[]) {
               long[] a = (long[])s.Obj;
               for (i = a.length; --i >= 0;)
                  x = new Cell(new Number(a[i]), x);
            }
            else if (s.Obj instanceof double[]) {
               double[] a = (double[])s.Obj;
               j = evInt(ex.Cdr.Cdr);
               for (i = a.length; --i >= 0;)
                  x = new Cell(strToNum(Double.toString(a[i]), i), x);
            }
         }
         return x;
      }

      final static Any do13(Any ex) { // next
         return Env.Next < Env.ArgC? (Env.Arg = Env.Args[Env.Next++]) : Nil;
      }

      final static Any do14(Any ex) { // arg
         int i;
         if (ex.Cdr instanceof Cell)
            return (i = evInt(ex.Cdr)+Env.Next-1) >= 0 && i < Env.ArgC? Env.Args[i] : Nil;
         return Env.Arg;
      }

      final static Any do15(Any ex) { // rest
         int i;
         Any x;
         for (x = Nil, i = Env.ArgC; --i >= Env.Next;)
            x = new Cell(Env.Args[i], x);
         return x;
      }

      final static Any do16(Any ex) { // date
         int i, j;
         Any x, z;
         if (!((x = ex.Cdr) instanceof Cell)) {
            Cal = new GregorianCalendar();
            return date(Cal.get(Calendar.YEAR), Cal.get(Calendar.MONTH)+1, Cal.get(Calendar.DATE));
         }
         if ((z = x.Car.eval()) == T) {
            Cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            return date(Cal.get(Calendar.YEAR), Cal.get(Calendar.MONTH)+1, Cal.get(Calendar.DATE));
         }
         if (z == Nil)
            return Nil;
         if (z instanceof Cell)
            return date(xInt(z.Car), xInt(z.Cdr.Car), xInt(z.Cdr.Cdr.Car));
         i = xInt(z);
         if (!((x = x.Cdr) instanceof Cell))
            return date(i);
         j = evInt(x);
         return date(i, j, evInt(x.Cdr));
      }

      final static Any do17(Any ex) { // time
         int i, j;
         Any x, z;
         if (!((x = ex.Cdr) instanceof Cell))
            return time(new GregorianCalendar());
         if ((z = x.Car.eval()) == T)
            return time(Cal);
         if (z == Nil)
            return Nil;
         if (z instanceof Cell)
            return time(xInt(z.Car), xInt(z.Cdr.Car), z.Cdr.Cdr instanceof Cell? xInt(z.Cdr.Cdr.Car) : 0);
         i = xInt(z);
         if (!((x = x.Cdr) instanceof Cell))
            return new Cell(new Number(i / 3600), new Cell(new Number(i / 60 % 60), new Cell(new Number(i % 60), Nil)));
         j = evInt(x);
         return time(i, j, x.Cdr instanceof Cell? evInt(x.Cdr) : 0);
      }

      final static Any do18(Any ex) { // usec
         return new Number(System.nanoTime()/1000 - USec);
      }

      final static Any do19(Any ex) { // file
         int i;
         Any x;
         if (InFile.Name == null)
            return Nil;
         x = new Number(InFile.Src);
         if ((i = InFile.Name.lastIndexOf('/')) >= 0)
            return new Cell(mkStr(InFile.Name.substring(0, i+1)), new Cell(mkStr(InFile.Name.substring(i+1)), x));
         return new Cell(mkStr("./"), new Cell(mkStr(InFile.Name), x));
      }

      final static Any do20(Any ex) { // version
         int i;
         Any x;
         if (ex.Cdr.Car.eval() == Nil) {
            for (i = 0; i < 4; ++i)
               OutFile.Wr.print(Version[i] + (i == 3? "-" : "."));
            OutFile.Wr.println("J");
            OutFile.Wr.flush();
         }
         for (x = Nil, i = 4; --i >= 0;)
            x = new Cell(new Number(Version[i]), x);
         return x;
      }

      final static Any do21(Any ex) { // apply
         int i;
         Any w, x, y;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         y = (x = x.Cdr).Car.eval();
         for (v = new Any[6], i = 0; (x = x.Cdr) instanceof Cell;)
            v = append(v, i++, x.Car.eval());
         while (y instanceof Cell) {
            v = append(v, i++, y.Car);
            y = y.Cdr;
         }
         return w.apply(ex, false, v, i);
      }

      final static Any do22(Any ex) { // pass
         int i, j;
         Any w, x;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         for (v = new Any[6], i = 0; (x = x.Cdr) instanceof Cell;)
            v = append(v, i++, x.Car.eval());
         for (j = Env.Next; j < Env.ArgC; ++j)
            v = append(v, i++, Env.Args[j]);
         return w.apply(ex, false, v, i);
      }

      final static Any do23(Any ex) { // maps
         int i, j, k;
         Any w, x, y;
         Symbol s;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         if ((y = (x = x.Cdr).Car.eval()) == Nil || (s = (Symbol)y).Prop == null)
            return Nil;
         v = new Any[6];
         i = 1;
         append(v, 0, null);
         while ((x = x.Cdr) instanceof Cell)
            v = append(v, i++, x.Car.eval());
         k = s.Prop.length;
         do
            if ((x = s.Prop[--k]) != null) {
               v[0] = new Cell(x,Nil);
               x = w.apply(ex, true, v, i);
               for (j = i; --j > 0;)
                  v[j] = v[j].Cdr;
            }
         while (k != 0);
         return x;
      }

      final static Any do24(Any ex) { // map
         int i, j;
         Any w, x, y;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while ((y = v[0]) instanceof Cell) {
               x = w.apply(ex, false, v, i);
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return x;
      }

      final static Any do25(Any ex) { // mapc
         int i, j;
         Any w, x, y;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while ((y = v[0]) instanceof Cell) {
               x = w.apply(ex, true, v, i);
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return x;
      }

      final static Any do26(Any ex) { // maplist
         int i, j;
         Any w, x, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            if (!(v[0] instanceof Cell))
               return z;
            z = x = new Cell(w.apply(ex, false, v, i), Nil);
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               x = x.Cdr = new Cell(w.apply(ex, false, v, i), Nil);
            }
         }
         return z;
      }

      final static Any do27(Any ex) { // mapcar
         int i, j;
         Any w, x, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            if (!(v[0] instanceof Cell))
               return z;
            z = x = new Cell(w.apply(ex, true, v, i), Nil);
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               x = x.Cdr = new Cell(w.apply(ex, true, v, i), Nil);
            }
         }
         return z;
      }

      final static Any do28(Any ex) { // mapcon
         int i, j;
         Any w, x, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            if (!(v[0] instanceof Cell))
               return z;
            while (!((x = w.apply(ex, false, v, i)) instanceof Cell)) {
               if (!(v[0].Cdr instanceof Cell))
                  return z;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
            z = x;
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               while (x.Cdr instanceof Cell)
                  x = x.Cdr;
               x.Cdr = w.apply(ex, false, v, i);
            }
         }
         return z;
      }

      final static Any do29(Any ex) { // mapcan
         int i, j;
         Any w, x, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            if (!(v[0] instanceof Cell))
               return z;
            while (!((x = w.apply(ex, true, v, i)) instanceof Cell)) {
               if (!(v[0].Cdr instanceof Cell))
                  return z;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
            z = x;
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               while (x.Cdr instanceof Cell)
                  x = x.Cdr;
               x.Cdr = w.apply(ex, true, v, i);
            }
         }
         return z;
      }

      final static Any do30(Any ex) { // filter
         int i, j;
         Any w, x, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            if (!(v[0] instanceof Cell))
               return z;
            while (w.apply(ex, true, v, i) == Nil) {
               if (!(v[0].Cdr instanceof Cell))
                  return z;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
            z = x = new Cell(v[0].Car, Nil);
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               if (w.apply(ex, true, v, i) != Nil)
                  x = x.Cdr = new Cell(v[0].Car, Nil);
            }
         }
         return z;
      }

      final static Any do31(Any ex) { // extract
         int i, j;
         Any w, x, y, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            if (!(v[0] instanceof Cell))
               return z;
            while ((y = w.apply(ex, true, v, i)) == Nil) {
               if (!(v[0].Cdr instanceof Cell))
                  return z;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
            z = x = new Cell(y, Nil);
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               if ((y = w.apply(ex, true, v, i)) != Nil)
                  x = x.Cdr = new Cell(y, Nil);
            }
         }
         return z;
      }

      final static Any do32(Any ex) { // seek
         int i, j;
         Any w, x;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if (w.apply(ex, false, v, i) != Nil)
                  return v[0];
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return Nil;
      }

      final static Any do33(Any ex) { // find
         int i, j;
         Any w, x;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if (w.apply(ex, true, v, i) != Nil)
                  return v[0].Car;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return Nil;
      }

      final static Any do34(Any ex) { // pick
         int i, j;
         Any w, x;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if ((x = w.apply(ex, true, v, i)) != Nil)
                  return x;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return Nil;
      }

      final static Any do35(Any ex) { // cnt
         int i, j;
         long n;
         Any w, x;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         n = 0;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if (w.apply(ex, true, v, i) != Nil)
                  ++n;
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return new Number(n);
      }

      final static Any do36(Any ex) { // sum
         int i, j;
         Any w, x, y;
         Number num;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         num = Zero;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if ((y = w.apply(ex, true, v, i)) instanceof Number)
                  num = num.add((Number)y);
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return num;
      }

      final static Any do37(Any ex) { // maxi
         int i, j;
         Any w, x, y, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         y = z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if ((x = w.apply(ex, true, v, i)).compare(y) > 0) {
                  z = v[0].Car;
                  y = x;
               }
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return z;
      }

      final static Any do38(Any ex) { // mini
         int i, j;
         Any w, x, y, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         y = T;
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            while (v[0] instanceof Cell) {
               if ((x = w.apply(ex, true, v, i)).compare(y) < 0) {
                  z = v[0].Car;
                  y = x;
               }
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
            }
         }
         return z;
      }

      final static Any do39(Any ex) { // fish
         Any w;
         Any[] v;
         w = ex.Cdr.Car.eval();
         (v = new Any[1])[0] = ex.Cdr.Cdr.Car.eval();
         return fish(ex, w, v, Nil);
      }

      final static Any do40(Any ex) { // by
         int i, j;
         Any w, x, y, z;
         Any[] v;
         w = (x = ex.Cdr).Car.eval();
         y = (x = x.Cdr).Car.eval();
         z = Nil;
         if ((x = x.Cdr) instanceof Cell) {
            v = new Any[6];
            i = 0;
            do
               v = append(v, i++, x.Car.eval());
            while ((x = x.Cdr) instanceof Cell);
            z = x = new Cell(new Cell(w.apply(ex, true, v, i), v[0].Car), Nil);
            while (v[0].Cdr instanceof Cell) {
               for (j = i; --j >= 0;)
                  v[j] = v[j].Cdr;
               x = x.Cdr = new Cell(new Cell(w.apply(ex, true, v, i), v[0].Car), Nil);
            }
            v[0] = z;
            z = y.apply(ex, false, v, 1);
            for (x = z; x instanceof Cell; x = x.Cdr)
               x.Car = x.Car.Cdr;
         }
         return z;
      }

      final static Any do41(Any ex) { // as
         return ex.Cdr.Car.eval() == Nil? Nil : ex.Cdr.Cdr;
      }

      final static Any do42(Any ex) { // lit
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Number || x == Nil || x == T || x instanceof Cell && x.Car instanceof Number? x : new Cell(Quote, x);
      }

      final static Any do43(Any ex) { // eval
         Any y;
         if ((y = (ex = ex.Cdr).Car.eval()) instanceof Number)
            return y;
         if (ex.Cdr == Nil || Env.Bind == null)
            return y.eval();
         return evRun(true, y, evInt(ex.Cdr), ex.Cdr.Cdr.Car.eval());
      }

      final static Any do44(Any ex) { // run
         Any y;
         if ((y = (ex = ex.Cdr).Car.eval()) instanceof Number)
            return y;
         if (ex.Cdr == Nil || Env.Bind == null)
            return y.run();
         return evRun(false, y, evInt(ex.Cdr), ex.Cdr.Cdr.Car.eval());
      }

      final static Any do45(Any ex) { // def
         Any w, x, y;
         Symbol s;
         s = (Symbol)(ex = ex.Cdr).Car.eval();
         x = (ex = ex.Cdr).Car.eval();
         if (ex.Cdr == Nil) {
            if (s.Car != Nil && s.Car != s && !x.equal(s.Car))
               redefMsg(s, null);
            s.Car = x;
            putSrc(s, null);
         }
         else {
            y = ex.Cdr.Car.eval();
            if ((w = s.get(x)) != Nil && !x.equal(w))
               redefMsg(s,x);
            s.put(x,y);
            putSrc(s,x);
         }
         return s;
      }

      final static Any do46(Any ex) { // de
         ex = ex.Cdr;
         redefine((Symbol)ex.Car, ex.Cdr);
         return ex.Car;
      }

      final static Any do47(Any ex) { // dm
         Any x, y;
         Symbol s, t;
         if (!((x = ex.Cdr).Car instanceof Cell)) {
            s = (Symbol)x.Car;
            t = (Symbol)Class.Car;
         }
         else {
            s = (Symbol)x.Car.Car;
            t = (Symbol)
               (!((y = x.Car).Cdr instanceof Cell)?
                  y.Cdr :
                  (y.Cdr.Cdr == Nil?  Class.Car : y.Cdr.Cdr).get(y.Cdr.Car) );
         }
         if (s != T)
            redefine(s, Meth.Car);
         if (x.Cdr instanceof Symbol) {
            y = x.Cdr.Car;
            for (;;) {
               if (!(y instanceof Cell) || !(y.Car instanceof Cell))
                  err(ex, s, "Bad message");
               if (y.Car.Car == s) {
                  x = y.Car;
                  break;
               }
               y = y.Cdr;
            }
         }
         for (y = t.Car; y instanceof Cell && y.Car instanceof Cell; y = y.Cdr)
            if (y.Car.Car == s) {
               if (!x.Cdr.equal(y.Cdr.Car))
                  redefMsg(s, t);
               y.Car.Cdr = x.Cdr;
               putSrc(t, s);
               return s;
            }
         t.Car = x.Car instanceof Cell?
            new Cell(new Cell(s, x.Cdr), t.Car) :
            new Cell(x, t.Car);
         putSrc(t, s);
         return s;
      }

      final static Any do48(Any ex) { // box
         return mkSymbol(ex.Cdr.Car.eval());
      }

      final static Any do49(Any ex) { // new
         Any x;
         Symbol s;
         s = mkSymbol((ex = ex.Cdr).Car.eval());
         TheKey = T;  TheCls = null;
         if ((x = method(s)) != null)
            evMethod(s, x, ex.Cdr);
         else {
            while ((ex = ex.Cdr) != Nil) {
               x = ex.Car.eval();
               s.put(x, (ex = ex.Cdr).Car.eval());
            }
         }
         return s;
      }

      final static Any do50(Any ex) { // type
         Any x, y, z;
         if ((x = ex.Cdr.Car.eval()) instanceof Symbol) {
            z = x = x.Car;
            while (x instanceof Cell) {
               if (!(x.Car instanceof Cell)) {
                  y = x;
                  while (x.Car instanceof Symbol) {
                     if (!((x = x.Cdr) instanceof Cell))
                        return x == Nil? y : Nil;
                     if (z == x)
                        return Nil;
                  }
                  return Nil;
               }
               if (z == (x = x.Cdr))
                  return Nil;
            }
         }
         return Nil;
      }

      final static Any do51(Any ex) { // isa
         Any x, y;
         x = (ex = ex.Cdr).Car.eval();
         if ((y = ex.Cdr.Car.eval()) instanceof Symbol) {
            if (x instanceof Symbol)
               return isa(x,y)? y : Nil;
            while (x instanceof Cell) {
               if (!isa(x.Car, y))
                  return Nil;
               x = x.Cdr;
            }
            return y;
         }
         return Nil;
      }

      final static Any do52(Any ex) { // method
         Any x, y;
         x = (ex = ex.Cdr).Car.eval();
         y = ex.Cdr.Car.eval();
         TheKey = x;
         return (x = method(y)) == null? Nil : x;
      }

      final static Any do53(Any ex) { // send
         Any x, y, z;
         y = (x = ex.Cdr).Car.eval();
         z = (x = x.Cdr).Car.eval();
         TheKey = y;  TheCls = null;
         if ((y = method(z)) == null)
            err(ex, TheKey, "Bad message");
         return evMethod(z, y, x.Cdr);
      }

      final static Any do54(Any ex) { // try
         Any x, y;
         x = (ex = ex.Cdr).Car.eval();
         if ((y = (ex = ex.Cdr).Car.eval()) instanceof Symbol) {
            TheKey = x;  TheCls = null;
            if ((x = method(y)) != null)
               return evMethod(y, x, ex.Cdr);
         }
         return Nil;
      }

      final static Any do55(Any ex) { // super
         Any w, x, y, z;
         TheKey = Env.Key;
         x = Env.Cls == null? This.Car : Env.Cls.Car.Car;
         while (x.Car instanceof Cell)
            x = x.Cdr;
         for (;;) {
            if (!(x instanceof Cell))
               err(ex, TheKey, "Bad super");
            if ((y = method((TheCls = x).Car)) != null) {
               z = Env.Cls;  Env.Cls = TheCls;
               w = Env.Key;  Env.Key = TheKey;
               x = y.func(ex);
               Env.Key = w;  Env.Cls = z;
               return x;
            }
            x = x.Cdr;
         }
      }

      final static Any do56(Any ex) { // extra
         Any x, y, z;
         TheKey = Env.Key;
         if ((x = extra(This.Car)) == null  ||  x == T)
            err(ex, TheKey, "Bad extra");
         y = Env.Cls;  Env.Cls = TheCls;
         z = Env.Key;  Env.Key = TheKey;
         x = x.func(ex);
         Env.Key = z;  Env.Cls = y;
         return x;
      }

      final static Any do57(Any ex) { // with
         Any x;
         Bind bnd;
         if ((x = ex.Cdr.Car.eval()) != Nil) {
            (bnd = new Bind()).add(This.Car);
            bnd.add(This);
            This.Car = x;
            Env.Bind = bnd;
            x = ex.Cdr.Cdr.prog();
            This.Car = bnd.Data[0];
         }
         return x;
      }

      final static Any do58(Any ex) { // bind
         int i;
         Any x, y, z;
         Bind bnd;
         if ((y = (x = ex.Cdr).Car.eval()) == Nil)
            return x.Cdr.prog();
         bnd = new Bind();
         if (y instanceof Symbol) {
            bnd.add(y.Car);
            bnd.add(y);
         }
         else {
            do {
               if (y.Car instanceof Symbol) {
                  bnd.add(y.Car.Car);
                  bnd.add(y.Car);
               }
               else {
                  z = y.Car.Car;
                  bnd.add(z.Car);
                  bnd.add(z);
                  z.Car = y.Car.Cdr;
               }
            } while ((y = y.Cdr) instanceof Cell);
         }
         Env.Bind = bnd;
         x = x.Cdr.prog();
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
         return x;
      }

      final static Any do59(Any ex) { // job
         int i;
         Any w, x, y, z;
         Bind bnd;
         bnd = new Bind();
         for (z = y = (x = ex.Cdr).Car.eval(); y instanceof Cell; y = y.Cdr) {
            w = y.Car.Car;
            bnd.add(w.Car);
            bnd.add(w);
            w.Car = y.Car.Cdr;
         }
         Env.Bind = bnd;
         x = x.Cdr.prog();
         for (i = 0; z instanceof Cell; i += 2, z = z.Cdr) {
            w = z.Car.Car;
            z.Car.Cdr = w.Car;
            w.Car = bnd.Data[i];
         }
         Env.Bind = bnd.Link;
         return x;
      }

      final static Any do60(Any ex) { // let
         int i;
         Any x, y, z;
         Bind bnd;
         bnd = new Bind();
         if ((y = (x = ex.Cdr).Car) instanceof Symbol) {
            bnd.add(y.Car);
            bnd.add(y);
            y.Car = (x = x.Cdr).Car.eval();
         }
         else {
            do {
               z = y.Car;
               bnd.add(z.Car);
               bnd.add(z);
               z.Car = (y = y.Cdr).Car.eval();
            } while ((y = y.Cdr) instanceof Cell);
         }
         Env.Bind = bnd;
         x = x.Cdr.prog();
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
         return x;
      }

      final static Any do61(Any ex) { // let?
         Any x, y, z;
         Bind bnd;
         z = (x = ex.Cdr).Car;
         if ((y = (x = x.Cdr).Car.eval()) != Nil) {
            (bnd = new Bind()).add(z.Car);
            bnd.add(z);
            z.Car = y;
            Env.Bind = bnd;
            y = x.Cdr.prog();
            z.Car = bnd.Data[0];
         }
         return y;
      }

      final static Any do62(Any ex) { // use
         int i;
         Any x, y;
         Bind bnd;
         bnd = new Bind();
         if ((y = (x = ex.Cdr).Car) instanceof Symbol) {
            bnd.add(y.Car);
            bnd.add(y);
         }
         else {
            do {
               bnd.add(y.Car.Car);
               bnd.add(y.Car);
            } while ((y = y.Cdr) instanceof Cell);
         }
         Env.Bind = bnd;
         x = x.Cdr.prog();
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
         return x;
      }

      final static Any do63(Any ex) { // and
         Any w;
         ex = ex.Cdr;
         do {
            if ((w = ex.Car.eval()) == Nil)
               return Nil;
            At.Car = w;
         } while ((ex = ex.Cdr) instanceof Cell);
         return w;
      }

      final static Any do64(Any ex) { // or
         Any w;
         ex = ex.Cdr;
         do
            if ((w = ex.Car.eval()) != Nil)
               return At.Car = w;
         while ((ex = ex.Cdr) instanceof Cell);
         return Nil;
      }

      final static Any do65(Any ex) { // nand
         Any w;
         ex = ex.Cdr;
         do {
            if ((w = ex.Car.eval()) == Nil)
               return T;
            At.Car = w;
         } while ((ex = ex.Cdr) instanceof Cell);
         return Nil;
      }

      final static Any do66(Any ex) { // nor
         Any w;
         ex = ex.Cdr;
         do
            if ((w = ex.Car.eval()) != Nil) {
               At.Car = w;
               return Nil;
            }
         while ((ex = ex.Cdr) instanceof Cell);
         return T;
      }

      final static Any do67(Any ex) { // xor
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         x = x.Cdr.Car.eval();
         return y == Nil ^ x == Nil? T : Nil;
      }

      final static Any do68(Any ex) { // bool
         return ex.Cdr.Car.eval() == Nil? Nil : T;
      }

      final static Any do69(Any ex) { // not
         Any w;
         if ((w = ex.Cdr.Car.eval()) == Nil)
            return T;
         At.Car = w;
         return Nil;
      }

      final static Any do70(Any ex) { // nil
         ex.Cdr.prog();
         return Nil;
      }

      final static Any do71(Any ex) { // t
         ex.Cdr.prog();
         return T;
      }

      final static Any do73(Any ex) { // prog1
         Any w;
         w = At.Car = ex.Cdr.Car.eval();
         ex.Cdr.Cdr.prog();
         return w;
      }

      final static Any do74(Any ex) { // prog2
         Any w;
         (ex = ex.Cdr).Car.eval();
         w = At.Car = (ex = ex.Cdr).Car.eval();
         ex.Cdr.prog();
         return w;
      }

      final static Any do75(Any ex) { // if
         Any w;
         if ((w = (ex = ex.Cdr).Car.eval()) == Nil)
            return ex.Cdr.Cdr.prog();
         At.Car = w;
         return ex.Cdr.Car.eval();
      }

      final static Any do76(Any ex) { // if2
         Any w;
         if ((w = (ex = ex.Cdr).Car.eval()) == Nil) {
            if ((w = (ex = ex.Cdr).Car.eval()) == Nil)
               return ex.Cdr.Cdr.Cdr.Cdr.prog();
            At.Car = w;
            return ex.Cdr.Cdr.Cdr.Car.eval();
         }
         At.Car = w;
         if ((w = (ex = ex.Cdr).Car.eval()) == Nil)
            return ex.Cdr.Cdr.Car.eval();
         At.Car = w;
         return ex.Cdr.Car.eval();
      }

      final static Any do77(Any ex) { // ifn
         Any w;
         if ((w = (ex = ex.Cdr).Car.eval()) != Nil) {
            At.Car = w;
            return ex.Cdr.Cdr.prog();
         }
         return ex.Cdr.Car.eval();
      }

      final static Any do78(Any ex) { // when
         Any w;
         if ((w = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         At.Car = w;
         return ex.Cdr.prog();
      }

      final static Any do79(Any ex) { // unless
         Any w;
         if ((w = (ex = ex.Cdr).Car.eval()) != Nil)
            return Nil;
         At.Car = w;
         return ex.Cdr.prog();
      }

      final static Any do80(Any ex) { // cond
         Any w;
         while ((ex = ex.Cdr) instanceof Cell)
            if ((w = ex.Car.Car.eval()) != Nil) {
               At.Car = w;
               return ex.Car.Cdr.prog();
            }
         return Nil;
      }

      final static Any do81(Any ex) { // nond
         Any w;
         while ((ex = ex.Cdr) instanceof Cell) {
            if ((w = ex.Car.Car.eval()) == Nil)
               return ex.Car.Cdr.prog();
            At.Car = w;
         }
         return Nil;
      }

      final static Any do82(Any ex) { // case
         Any x, y;
         At.Car = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr) instanceof Cell) {
            x = ex.Car;  y = x.Car;
            if (y == T  ||  At.Car.equal(y))
               return x.Cdr.prog();
            if (y instanceof Cell) {
               do
                  if (At.Car.equal(y.Car))
                     return x.Cdr.prog();
               while ((y = y.Cdr) instanceof Cell);
            }
         }
         return Nil;
      }

      final static Any do83(Any ex) { // state
         Any w, x, y, z;
         z = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell) {
            y = x.Car;
            if (y.Car == T || memq(z.Car, y.Car) != null) {
               y = y.Cdr;
               if ((w = y.Car.eval()) != Nil) {
                  At.Car = z.Car = w;
                  return y.Cdr.prog();
               }
            }
         }
         return Nil;
      }

      final static Any do84(Any ex) { // while
         Any w, x, y;
         x = (ex = ex.Cdr).Car;
         ex = ex.Cdr;
         y = Nil;
         while ((w = x.eval()) != Nil) {
            At.Car = w;
            y = ex.prog();
         }
         return y;
      }

      final static Any do85(Any ex) { // until
         Any w, x, y;
         x = (ex = ex.Cdr).Car;
         ex = ex.Cdr;
         y = Nil;
         while ((w = x.eval()) == Nil)
            y = ex.prog();
         At.Car = w;
         return y;
      }

      final static Any do86(Any ex) { // do
         long n;
         Any w, x, y;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         if (!(x instanceof Number))
            return loop(ex.Cdr);
         for (ex = ex.Cdr, y = Nil, n = ((Number)x).longValue(); --n >= 0;) {
            x = ex;
            do {
               if (!((y = x.Car) instanceof Cell))
                  y = y.eval();
               else if (y.Car == Nil) {
                  if ((w = (y = y.Cdr).Car.eval()) == Nil)
                     return y.Cdr.prog();
                  At.Car = w;
                  y = Nil;
               }
               else if (y.Car == T) {
                  if ((w = (y = y.Cdr).Car.eval()) != Nil) {
                     At.Car = w;
                     return y.Cdr.prog();
                  }
                  y = Nil;
               }
               else
                  y = y.eval();
            } while ((x = x.Cdr) instanceof Cell);
         }
         return y;
      }

      final static Any do88(Any ex) { // at
         Any x;
         Number num;
         x = (ex = ex.Cdr).Car.eval();
         if ((num = ((Number)x.Car).add(One)).compare((Number)x.Cdr) < 0) {
            x.Car = num;
            return Nil;
         }
         x.Car = Zero;
         return ex.Cdr.prog();
      }

      final static Any do89(Any ex) { // for
         int i;
         Any w, x, y, z;
         Bind bnd;
         bnd = new Bind();
         if (!((y = (ex = ex.Cdr).Car) instanceof Cell) || !(y.Cdr instanceof Cell)) {
            if (!(y instanceof Cell)) {
               bnd.add(y.Car);
               bnd.add(y);
            }
            else {
               bnd.add(y.Cdr.Car);
               bnd.add(y.Cdr);
               bnd.add((z = y.Car).Car);
               bnd.add(z);
               z.Car = Zero;
            }
            Env.Bind = bnd;
            if ((z = (ex = ex.Cdr).Car.eval()) instanceof Number)
               bnd.Data[1].Car = Zero;
         for1:
            for (y = Nil;;) {
               if (z instanceof Number) {
                  if (((Number)(bnd.Data[1].Car = ((Number)bnd.Data[1].Car).add(One))).compare((Number)z) > 0)
                     break;
               }
               else {
                  if (!(z instanceof Cell))
                     break;
                  bnd.Data[1].Car = z.Car;
                  if (!((z = z.Cdr) instanceof Cell))
                     z = Nil;
               }
               if (bnd.Cnt == 4)
                  bnd.Data[3].Car = ((Number)bnd.Data[3].Car).add(One);
               x = ex.Cdr;
               do {
                  if (!((y = x.Car) instanceof Cell))
                     y = y.eval();
                  else if (y.Car == Nil) {
                     if ((w = (y = y.Cdr).Car.eval()) == Nil) {
                        y = y.Cdr.prog();
                        break for1;
                     }
                     At.Car = w;
                     y = Nil;
                  }
                  else if (y.Car == T) {
                     if ((w = (y = y.Cdr).Car.eval()) != Nil) {
                        At.Car = w;
                        y = y.Cdr.prog();
                        break for1;
                     }
                     y = Nil;
                  }
                  else
                     y = y.eval();
               } while ((x = x.Cdr) instanceof Cell);
            }
         }
         else {
            if (!((z = y.Car) instanceof Cell)) {
               bnd.add(z.Car);
               bnd.add(z);
            }
            else {
               bnd.add(z.Cdr.Car);
               bnd.add(z.Cdr);
               bnd.add((z = z.Car).Car);
               bnd.add(z);
               z.Car = Zero;
            }
            Env.Bind = bnd;
            bnd.Data[1].Car = (y = y.Cdr).Car.eval();
            z = y.Cdr;
         for2:
            for (y = Nil; (w = z.Car.eval()) != Nil;) {
               At.Car = w;
               if (bnd.Cnt == 4)
                  bnd.Data[3].Car = ((Number)bnd.Data[3].Car).add(One);
               x = ex.Cdr;
               do {
                  if (!((y = x.Car) instanceof Cell))
                     y = y.eval();
                  else if (y.Car == Nil) {
                     if ((w = (y = y.Cdr).Car.eval()) == Nil) {
                        y = y.Cdr.prog();
                        break for2;
                     }
                     At.Car = w;
                     y = Nil;
                  }
                  else if (y.Car == T) {
                     if ((w = (y = y.Cdr).Car.eval()) != Nil) {
                        At.Car = w;
                        y = y.Cdr.prog();
                        break for2;
                     }
                     y = Nil;
                  }
                  else
                     y = y.eval();
                  if (z.Cdr instanceof Cell)
                     bnd.Data[1].Car = z.Cdr.prog();
               } while ((x = x.Cdr) instanceof Cell);
            }
         }
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
         return y;
      }

      final static Any do90(Any ex) { // catch
         Any x, y;
         new Catch(y = (x = ex.Cdr).Car.eval(), Zero, Env);
         try {
            x = x.Cdr.prog();
            Catch = Catch.Link;
            return x;
         }
         catch (Control e) {
            if (y == e.Tag)
               return e.Val;
            throw e;
         }
         catch (RuntimeException e) {
            if (y instanceof Cell && e.toString().indexOf(y.Car.name()) >= 0)
               return y.Car;
            throw e;
         }
      }

      final static Any do91(Any ex) { // throw
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         throw new Control(ex, y, x.Cdr.Car.eval());
      }

      final static Any do92(Any ex) { // finally
         Any x, y;
         new Catch(null, y = (x = ex.Cdr).Car, Env);
         x = x.Cdr.prog();
         y.eval();
         Catch = Catch.Link;
         return x;
      }

      final static Any do93(Any ex) { // !
         Any x;
         x = ex.Cdr;
         if (Dbg.Car != Nil)
            x = brkLoad(x);
         return x.eval();
      }

      final static Any do94(Any ex) { // e
         Any w, x, y, z;
         if (!Break)
            err(ex, null, "No Break");
         w = Dbg.Car;  Dbg.Car = Nil;
         x = At.Car;  At.Car = Brk.Data[4];
         y = Run.Car;  Run.Car = Brk.Data[2];
         InFrame in = Env.InFrames;  Env.popInFiles();
         OutFrame out = Env.OutFrames;  Env.popOutFiles();
         z = ex.Cdr instanceof Cell? ex.Cdr.prog() : Up.Car.eval();
         OutFile.Wr.flush();
         Env.pushOutFile(out);
         Env.pushInFile(in);
         Dbg.Car = w;
         At.Car = x;
         Run.Car = y;
         return z;
      }

      final static Any do95(Any ex) { // $
         int i;
         Any x;
         ex = ex.Cdr;
         if (Dbg.Car == Nil)
            return ex.Cdr.Cdr.prog();
         trace(++Env.Trace, ex.Car, " :");
         for (x = ex.Cdr.Car; x instanceof Cell; x = x.Cdr) {
            StdErr.space();
            StdErr.print(x.Car.Car);
         }
         if (x != Nil) {
            if (x != At) {
               StdErr.space();
               StdErr.print(x.Car);
            }
            else
               for (i = Env.Next; i < Env.ArgC; ++i) {
                  StdErr.space();
                  StdErr.print(Env.Args[i]);
               }
         }
         StdErr.newline();
         x = ex.Cdr.Cdr.prog();
         trace(Env.Trace--, ex.Car, " = ");
         StdErr.print(x);
         StdErr.newline();
         return x;
      }

      final static Any do96(Any ex) { // sys
         return mkStr(System.getenv(evString(ex.Cdr)));
      }

      final static Any do97(Any ex) { // call
         int i, j;
         Any x;
         j = (int)(x = ex.Cdr).length();
         String[] cmd = new String[j];
         for (i = 0; i < j; ++i) {
            cmd[i] = x.Car.eval().name();
            x = x.Cdr;
         }
         try {i = Runtime.getRuntime().exec(cmd).waitFor();}
         catch (IOException e) {System.err.println(cmd[0] + ": Can't exec");}
         catch (InterruptedException e) {}  //#! sighandler()
         return i == 0? T : Nil;
      }

      final static Any do98(Any ex) { // ipid
         return Env.InFrames != null && Env.InFrames.Pid > 1? new Number(Env.InFrames.Pid) : Nil;
      }

      final static Any do99(Any ex) { // opid
         return Env.OutFrames != null && Env.OutFrames.Pid > 1? new Number(Env.OutFrames.Pid) : Nil;
      }

      final static Any do100(Any ex) { // kill
         int i;
         if (Pids[i = evInt(ex = ex.Cdr)] == null)
            return Nil;
         if ((ex = ex.Cdr) instanceof Cell && evInt(ex) == 0)
            return T;
         Pids[i].destroy();
         return T;
      }

      final static Any do101(Any ex) { // bye
         Any x;
         x = ex.Cdr.Car.eval();
         return bye(x == Nil? 0 : ((Number)x).Cnt);
      }

      final static Any do102(Any ex) { // name
         Any x, y;
         Symbol s;
         y = (x = ex.Cdr).Car.eval();
         if (!((x = x.Cdr) instanceof Cell))
            return mkStr(y.name());
         if ((s = ((Symbol)y)).Name != null && Intern.get(s.Name) == s)
            err(ex, s, "Can't rename");
         s.Name = ((Symbol)(x = x.Car.eval())).Name;
         return s;
      }

      final static Any do103(Any ex) { // sp?
         return isBlank(ex.Cdr.Car.eval())? T : Nil;
      }

      final static Any do104(Any ex) { // pat?
         Any x;
         return ((x = ex.Cdr.Car.eval()) instanceof Symbol) && firstChar(x) == '@'? x : Nil;
      }

      final static Any do105(Any ex) { // fun?
         return funq(ex.Cdr.Car.eval());
      }

      final static Any do106(Any ex) { // getd
         Any x;
         if (!((x = ex.Cdr.Car.eval()) instanceof Symbol))
            return Nil;
         return funq(x.Car) != Nil? x.Car : Nil;  // ... reflection
      }

      final static Any do107(Any ex) { // all
         return all(ex.Cdr.Car.eval() == Nil? Intern : Transient);
      }

      final static Any do108(Any ex) { // intern
         Symbol s, t;
         String str;
         s = (Symbol)ex.Cdr.Car.eval();
         if ((str = s.name()).length() == 0 || str.equals("NIL"))
            return Nil;
         if ((t = Intern.get(str)) != null)
            return t;
         Intern.put(str, s);
         return s;
      }

      final static Any do109(Any ex) { // ====
         Any x, y;
         Transient.clear();
         for (x = ex.Cdr; x instanceof Cell; x = x.Cdr) {
            y = x.Car.eval();
            Transient.put(((Symbol)y).Name, (Symbol)y);
         }
         return Nil;
      }

      final static Any do110(Any ex) { // box?
         Any x;
         return ((x = ex.Cdr.Car.eval()) instanceof Symbol) && x.name().length() == 0? x : Nil;
      }

      final static Any do111(Any ex) { // str?
         Any x;
         return ((x = ex.Cdr.Car.eval()) instanceof Symbol) && Intern.get(x.name()) == null? x : Nil;
      }

      final static Any do112(Any ex) { // zap
         Symbol s;
         s = (Symbol)ex.Cdr.Car.eval();
         Intern.remove(s.name());
         return s;
      }

      final static Any do113(Any ex) { // chop
         Any x, y;
         String str;
         x = ex.Cdr.Car.eval();
         if (!(x instanceof Cell)) {
            str = x.name();
            if (str.length() == 0)
               return Nil;
            y = x = new Cell(mkChar(str.charAt(0)), Nil);
            for (int i = 1; i < str.length(); ++i)
               y = y.Cdr = new Cell(mkChar(str.charAt(i)), Nil);
         }
         return x;
      }

      final static Any do114(Any ex) { // pack
         StringBuilder sb;
         sb = new StringBuilder();
         for (ex = ex.Cdr; ex instanceof Cell; ex = ex.Cdr)
            sb.append(evString(ex));
         return mkStr(sb);
      }

      final static Any do115(Any ex) { // glue
         Any x, y;
         StringBuilder sb;
         x = ex.Cdr.Car.eval();
         if (!((y = ex.Cdr.Cdr.Car.eval()) instanceof Cell))
            return y;
         for (sb = new StringBuilder(), sb.append(y.Car.name()); (y = y.Cdr) instanceof Cell;) {
            sb.append(x.name());
            sb.append(y.Car.name());
         }
         return mkStr(sb);
      }

      final static Any do116(Any ex) { // text
         int i, j, k;
         char c;
         String str;
         StringBuilder sb;
         Any[] v;
         str = evString(ex = ex.Cdr);
         v = new Any[6];
         i = 0;
         while ((ex = ex.Cdr) instanceof Cell)
            v = append(v, i++, ex.Car.eval());
         sb = new StringBuilder();
         k = str.length();
         for (j = 0; j < k; ++j)
            if ((c = str.charAt(j)) != '@')
               sb.append(c);
            else if (++j == k)
               break;
            else if ((c = str.charAt(j)) == '@')
               sb.append('@');
            else if (c >= '1') {
               if ((c -= '1') > 8)
                  c -= 7;
               if (i > c)
                  sb.append(v[c].name());
            }
         return mkStr(sb);
      }

      final static Any do117(Any ex) { // pre?
         Any x;
         String str;
         str = evString(ex = ex.Cdr);
         return (x = ex.Cdr.Car.eval()).name().startsWith(str)? x : Nil;
      }

      final static Any do118(Any ex) { // sub?
         Any x;
         String str;
         str = evString(ex = ex.Cdr);
         return (x = ex.Cdr.Car.eval()).name().indexOf(str) >= 0? x : Nil;
      }

      final static Any do120(Any ex) { // set
         Any x, y;
         x = ex.Cdr;
         do {
            y = x.Car.eval();
            needVar(ex, y);
            y.Car = (x = x.Cdr).Car.eval();
         } while ((x = x.Cdr) instanceof Cell);
         return y.Car;
      }

      final static Any do121(Any ex) { // setq
         Any x, y;
         x = ex.Cdr;
         do {
            y = x.Car;
            needVar(ex, y);
            y.Car = (x = x.Cdr).Car.eval();
         } while ((x = x.Cdr) instanceof Cell);
         return y.Car;
      }

      final static Any do122(Any ex) { // xchg
         Any w, x, y, z;
         x = ex.Cdr;
         do {
            needVar(ex, y = x.Car.eval());
            needVar(ex, z = (x = x.Cdr).Car.eval());
            w = y.Car;  y.Car = z.Car;  z.Car = w;
         } while ((x = x.Cdr) instanceof Cell);
         return w;
      }

      final static Any do123(Any ex) { // on
         Any x;
         x = ex.Cdr;
         do
            x.Car.Car = T;
         while ((x = x.Cdr) instanceof Cell);
         return T;
      }

      final static Any do124(Any ex) { // off
         Any x;
         x = ex.Cdr;
         do
            x.Car.Car = Nil;
         while ((x = x.Cdr) instanceof Cell);
         return Nil;
      }

      final static Any do125(Any ex) { // onOff
         Any x, y;
         x = ex.Cdr;
         do
            y = x.Car.Car = x.Car.Car == Nil? T : Nil;
         while ((x = x.Cdr) instanceof Cell);
         return y;
      }

      final static Any do126(Any ex) { // zero
         Any x;
         x = ex.Cdr;
         do
            x.Car.Car = Zero;
         while ((x = x.Cdr) instanceof Cell);
         return Zero;
      }

      final static Any do127(Any ex) { // one
         Any x;
         x = ex.Cdr;
         do
            x.Car.Car = One;
         while ((x = x.Cdr) instanceof Cell);
         return One;
      }

      final static Any do128(Any ex) { // default
         Any x, y;
         x = ex.Cdr;
         do {
            y = x.Car;
            x = x.Cdr;
            needVar(ex, y);
            if (y.Car == Nil)
               y.Car = x.Car.eval();
         } while ((x = x.Cdr) instanceof Cell);
         return y.Car;
      }

      final static Any do129(Any ex) { // push
         Any x, y, z;
         needVar(ex, y = (x = ex.Cdr).Car.eval());
         do
            y.Car = new Cell(z = (x = x.Cdr).Car.eval(), y.Car);
         while (x.Cdr instanceof Cell);
         return z;
      }

      final static Any do130(Any ex) { // push1
         Any x, y, z;
         needVar(ex, y = (x = ex.Cdr).Car.eval());
         do
            if (member(z = (x = x.Cdr).Car.eval(), y.Car) == null)
               y.Car = new Cell(z, y.Car);
         while (x.Cdr instanceof Cell);
         return z;
      }

      final static Any do131(Any ex) { // pop
         Any x, y;
         needVar(ex, x = ex.Cdr.Car.eval());
         if ((y = x.Car) instanceof Cell) {
            x.Car = x.Car.Cdr;
            y = y.Car;
         }
         return y;
      }

      final static Any do132(Any ex) { // cut
         long n;
         Any x, y, z;
         if ((n = evLong(ex.Cdr)) <= 0)
            return Nil;
         needVar(ex, x = ex.Cdr.Cdr.Car.eval());
         if (x.Car instanceof Cell) {
            z = y = new Cell(x.Car.Car, Nil);
            while ((x.Car = x.Car.Cdr) instanceof Cell && --n != 0)
               y = y.Cdr = new Cell(x.Car.Car, Nil);
            return z;
         }
         return x.Car;
      }

      final static Any do133(Any ex) { // del
         Any w, lst, x, y, z;
         w = ex.Cdr.Car.eval();
         needVar(ex, x = ex.Cdr.Cdr.Car.eval());
         if ((lst = x.Car) instanceof Cell) {
            if (w.equal(lst.Car))
               return x.Car = lst.Cdr;
            for (z = y = new Cell(lst.Car, Nil); (lst = lst.Cdr) instanceof Cell; y = y.Cdr = new Cell(lst.Car, Nil))
               if (w.equal(lst.Car)) {
                  y.Cdr = lst.Cdr;
                  return x.Car = z;
               }
         }
         return x.Car;
      }

      final static Any do134(Any ex) { // queue
         Any x, y;
         needVar(ex, x = ex.Cdr.Car.eval());
         y = ex.Cdr.Cdr.Car.eval();
         if (!(x.Car instanceof Cell))
            x.Car = new Cell(y, Nil);
         else {
            for (x = x.Car; x.Cdr instanceof Cell; x = x.Cdr);
            x.Cdr = new Cell(y, Nil);
         }
         return y;
      }

      final static Any do135(Any ex) { // fifo
         Any x, y, z, lst;
         needVar(ex, y = (x = ex.Cdr).Car.eval());
         if ((x = x.Cdr) instanceof Cell) {
            z = x.Car.eval();
            if ((lst = y.Car) instanceof Cell)
               y.Car = lst = lst.Cdr = new Cell(z, lst.Cdr);
            else {
               lst = y.Car = new Cell(z, Nil);
               lst.Cdr = lst;
            }
            while ((x = x.Cdr) instanceof Cell)
               y.Car = lst = lst.Cdr = new Cell(z = x.Car.eval(), lst.Cdr);
            return z;
         }
         if (!((lst = y.Car) instanceof Cell))
            return Nil;
         if (lst == lst.Cdr) {
            z = lst.Car;
            y.Car = Nil;
         }
         else {
            z = lst.Cdr.Car;
            lst.Cdr = lst.Cdr.Cdr;
         }
         return z;
      }

      final static Any do136(Any ex) { // idx
         Any x, y;
         needVar(ex, x = (ex = ex.Cdr).Car.eval());
         if (!((ex = ex.Cdr) instanceof Cell))
            return idx(x, null, 0);
         y = ex.Car.eval();
         return idx(x, y, ex.Cdr instanceof Cell? (ex.Cdr.Car.eval() == Nil? -1 : +1) : 0);
      }

      final static Any do137(Any ex) { // lup
         int i;
         Any x, y, z;
         x = (ex = ex.Cdr).Car.eval();
         y = (ex = ex.Cdr).Car.eval();
         if ((z = ex.Cdr.Car.eval()) != Nil)
            return consLup(x, Nil, y, z);
         while (x instanceof Cell) {
            if (x.Car == T)
               x = x.Cdr.Car;
            else if (!(x.Car instanceof Cell))
               x = x.Cdr.Cdr;
            else if ((i = y.compare(x.Car.Car)) == 0)
               return x.Car;
            else
               x = i < 0? x.Cdr.Car : x.Cdr.Cdr;
         }
         return Nil;
      }

      final static Any do138(Any ex) { // put
         Any x, y;
         x = (ex = ex.Cdr).Car.eval();
         for (;;) {
            y = (ex = ex.Cdr).Car.eval();
            if (!(ex.Cdr.Cdr instanceof Cell))
               return x.put(y, ex.Cdr.Car.eval());
            x = x.get(y);
         }
      }

      final static Any do139(Any ex) { // get
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr) instanceof Cell)
            x = x.get(ex.Car.eval());
         return x;
      }

      final static Any do140(Any ex) { // prop
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr).Cdr instanceof Cell)
            x = x.get(ex.Car.eval());
         return x.prop(ex.Car.eval());
      }

      final static Any do141(Any ex) { // ;
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr) instanceof Cell)
            x = x.get(ex.Car);
         return x;
      }

      final static Any do142(Any ex) { // =:
         Any x, y;
         for (x = This.Car;;) {
            y = (ex = ex.Cdr).Car;
            if (!(ex.Cdr.Cdr instanceof Cell))
               return x.put(y, ex.Cdr.Car.eval());
            x = x.get(y);
         }
      }

      final static Any do143(Any ex) { // :
         Any x;
         x = This.Car;
         do
            x = x.get((ex = ex.Cdr).Car);
         while (ex.Cdr instanceof Cell);
         return x;
      }

      final static Any do144(Any ex) { // ::
         Any x;
         x = This.Car;
         while ((ex = ex.Cdr).Cdr instanceof Cell)
            x = x.get(ex.Car);
         return x.prop(ex.Car);
      }

      final static Any do145(Any ex) { // putl
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr).Cdr instanceof Cell)
            x = x.get(ex.Car.eval());
         return x.putl(ex.Car.eval());
      }

      final static Any do146(Any ex) { // getl
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr) instanceof Cell)
            x = x.get(ex.Car.eval());
         return x.getl();
      }

      final static Any do147(Any ex) { // meta
         Any x, y;
         if ((x = (ex = ex.Cdr).Car.eval()) instanceof Symbol)
            x = x.Car;
         for (x = meta(x, (ex = ex.Cdr).Car.eval()); (ex = ex.Cdr) instanceof Cell;)
            x = x.get(ex.Car.eval());
         return x;
      }

      final static Any do148(Any ex) { // low?
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Symbol && Character.isLowerCase(firstChar(x))? x : Nil;
      }

      final static Any do149(Any ex) { // upp?
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Symbol && Character.isUpperCase(firstChar(x))? x : Nil;
      }

      final static Any do150(Any ex) { // lowc
         int i, j;
         Any x;
         String str;
         StringBuilder sb;
         if (!((x = ex.Cdr.Car.eval()) instanceof Symbol) || (j = (str = x.name()).length()) == 0)
            return x;
         sb = new StringBuilder();
         for (i = 0; i < j; ++i)
            sb.append(Character.toLowerCase(str.charAt(i)));
         return mkStr(sb);
      }

      final static Any do151(Any ex) { // uppc
         int i, j;
         Any x;
         String str;
         StringBuilder sb;
         if (!((x = ex.Cdr.Car.eval()) instanceof Symbol) || (j = (str = x.name()).length()) == 0)
            return x;
         sb = new StringBuilder();
         for (i = 0; i < j; ++i)
            sb.append(Character.toUpperCase(str.charAt(i)));
         return mkStr(sb);
      }

      final static Any do152(Any ex) { // fold
         int i, j, k;
         char c;
         Any x;
         String str;
         StringBuilder sb;
         if (!((x = (ex = ex.Cdr).Car.eval()) instanceof Symbol) || (j = (str = x.name()).length()) == 0)
            return x;
         for (i = 0; !Character.isLetterOrDigit(c = str.charAt(i));)
            if (++i == j)
               return Nil;
         k = (ex = ex.Cdr) instanceof Cell? evInt(ex) : 24;
         sb = new StringBuilder();
         sb.append(Character.toLowerCase(c));
         while (++i < j)
            if (Character.isLetterOrDigit(c = str.charAt(i))) {
               if (--k == 0)
                  break;
               sb.append(Character.toLowerCase(c));
            }
         return mkStr(sb);
      }

      final static Any do159(Any ex) { // caaar
         return ex.Cdr.Car.eval().Car.Car.Car;
      }

      final static Any do160(Any ex) { // caadr
         return ex.Cdr.Car.eval().Cdr.Car.Car;
      }

      final static Any do161(Any ex) { // cadar
         return ex.Cdr.Car.eval().Car.Cdr.Car;
      }

      final static Any do162(Any ex) { // caddr
         return ex.Cdr.Car.eval().Cdr.Cdr.Car;
      }

      final static Any do163(Any ex) { // cdaar
         return ex.Cdr.Car.eval().Car.Car.Cdr;
      }

      final static Any do164(Any ex) { // cdadr
         return ex.Cdr.Car.eval().Cdr.Car.Cdr;
      }

      final static Any do165(Any ex) { // cddar
         return ex.Cdr.Car.eval().Car.Cdr.Cdr;
      }

      final static Any do166(Any ex) { // cdddr
         return ex.Cdr.Car.eval().Cdr.Cdr.Cdr;
      }

      final static Any do167(Any ex) { // caaaar
         return ex.Cdr.Car.eval().Car.Car.Car.Car;
      }

      final static Any do168(Any ex) { // caaadr
         return ex.Cdr.Car.eval().Cdr.Car.Car.Car;
      }

      final static Any do169(Any ex) { // caadar
         return ex.Cdr.Car.eval().Car.Cdr.Car.Car;
      }

      final static Any do170(Any ex) { // caaddr
         return ex.Cdr.Car.eval().Cdr.Cdr.Car.Car;
      }

      final static Any do171(Any ex) { // cadaar
         return ex.Cdr.Car.eval().Car.Car.Cdr.Car;
      }

      final static Any do172(Any ex) { // cadadr
         return ex.Cdr.Car.eval().Cdr.Car.Cdr.Car;
      }

      final static Any do173(Any ex) { // caddar
         return ex.Cdr.Car.eval().Car.Cdr.Cdr.Car;
      }

      final static Any do174(Any ex) { // cadddr
         return ex.Cdr.Car.eval().Cdr.Cdr.Cdr.Car;
      }

      final static Any do175(Any ex) { // cdaaar
         return ex.Cdr.Car.eval().Car.Car.Car.Cdr;
      }

      final static Any do176(Any ex) { // cdaadr
         return ex.Cdr.Car.eval().Cdr.Car.Car.Cdr;
      }

      final static Any do177(Any ex) { // cdadar
         return ex.Cdr.Car.eval().Car.Cdr.Car.Cdr;
      }

      final static Any do178(Any ex) { // cdaddr
         return ex.Cdr.Car.eval().Cdr.Cdr.Car.Cdr;
      }

      final static Any do179(Any ex) { // cddaar
         return ex.Cdr.Car.eval().Car.Car.Cdr.Cdr;
      }

      final static Any do180(Any ex) { // cddadr
         return ex.Cdr.Car.eval().Cdr.Car.Cdr.Cdr;
      }

      final static Any do181(Any ex) { // cdddar
         return ex.Cdr.Car.eval().Car.Cdr.Cdr.Cdr;
      }

      final static Any do182(Any ex) { // cddddr
         return ex.Cdr.Car.eval().Cdr.Cdr.Cdr.Cdr;
      }

      final static Any do183(Any ex) { // nth
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         for (;;) {
            if (!(x instanceof Cell))
               return x;
            x = nth(evInt(ex = ex.Cdr), x);
            if (ex.Cdr == Nil)
               return x;
            x = x.Car;
         }
      }

      final static Any do184(Any ex) { // con
         Any x;
         x = ex.Cdr.Car.eval();
         return x.Cdr = ex.Cdr.Cdr.Car.eval();
      }

      final static Any do185(Any ex) { // cons
         Any x, y;
         y = x = new Cell((ex = ex.Cdr).Car.eval(), Nil);
         while ((ex = ex.Cdr).Cdr instanceof Cell)
            x = x.Cdr = new Cell(ex.Car.eval(), Nil);
         x.Cdr = ex.Car.eval();
         return y;
      }

      final static Any do186(Any ex) { // conc
         Any x, y, z;
         z = x = (ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr) instanceof Cell) {
            if (!(x instanceof Cell))
               z = x = ex.Car.eval();
            else {
               while ((y = x.Cdr) instanceof Cell)
                  x = y;
               x.Cdr = ex.Car.eval();
            }
         }
         return z;
      }

      final static Any do187(Any ex) { // circ
         Any x, y;
         y = x = new Cell((ex = ex.Cdr).Car.eval(), Nil);
         while ((ex = ex.Cdr) instanceof Cell)
            x = x.Cdr = new Cell(ex.Car.eval(), Nil);
         x.Cdr = y;
         return y;
      }

      final static Any do188(Any ex) { // rot
         int i;
         Any w, x, y, z;
         w = y = (ex = ex.Cdr).Car.eval();
         if (w instanceof Cell) {
            i = ex.Cdr == Nil? 0 : evInt(ex.Cdr);
            x = y.Car;
            while (--i != 0  &&  (y = y.Cdr) instanceof Cell  &&  y != w) {
               z = y.Car;  y.Car = x;  x = z;
            }
            w.Car = x;
         }
         return w;
      }

      final static Any do189(Any ex) { // list
         Any x, y;
         x = y = new Cell((ex = ex.Cdr).Car.eval(), Nil);
         while ((ex = ex.Cdr) instanceof Cell)
            x = x.Cdr = new Cell(ex.Car.eval(), Nil);
         return y;
      }

      final static Any do190(Any ex) { // need
         long n;
         Any x, y, z;
         n = evLong(ex = ex.Cdr);
         z = (ex = ex.Cdr).Car.eval();
         y = ex.Cdr.Car.eval();
         x = z;
         if (n > 0)
            for (n -= x.length(); n > 0; --n)
               z = new Cell(y,z);
         else if (n != 0) {
            if (!(x instanceof Cell))
               z = x = new Cell(y,Nil);
            else
               while (x.Cdr instanceof Cell) {
                  ++n;  x = x.Cdr;
               }
            while (++n < 0)
               x = x.Cdr = new Cell(y,Nil);
         }
         return z;
      }

      final static Any do191(Any ex) { // range
         Any x, y;
         Number num;
         num = (Number)(y = (x = ex.Cdr).Car.eval());
         Number end = (Number)(x = x.Cdr).Car.eval();
         Number inc = (x = x.Cdr.Car.eval()) == Nil? One : (Number)x;
         x = y = new Cell(y, Nil);
         if (end.compare(num) >= 0)
            while (end.compare(num = num.add(inc)) >= 0)
               x = x.Cdr = new Cell(num, Nil);
         else
            while (end.compare(num = num.sub(inc)) <= 0)
               x = x.Cdr = new Cell(num, Nil);
         return y;
      }

      final static Any do192(Any ex) { // full
         Any x;
         for (x = ex.Cdr.Car.eval(); x instanceof Cell; x = x.Cdr)
            if (x.Car == Nil)
               return Nil;
         return T;
      }

      final static Any do193(Any ex) { // make
         Any x, y, z;
         x = Env.Make;  Env.Make = Nil;
         y = Env.Yoke;  Env.Yoke = Nil;
         ex.Cdr.prog();
         z = Env.Yoke;
         Env.Yoke = y;
         Env.Make = x;
         return z;
      }

      final static Any do194(Any ex) { // made
         Any x;
         if ((x = ex.Cdr) instanceof Cell) {
            Env.Yoke = x.Car.eval();
            x = x.Cdr;
            if (!((x = x.Car.eval()) instanceof Cell))
               for (x = Env.Yoke; (x = x.Cdr).Cdr instanceof Cell;);
            Env.Make = x;
         }
         return Env.Yoke;
      }

      final static Any do195(Any ex) { // chain
         Any x, y;
         ex = ex.Cdr;
         do {
            x = ex.Car.eval();
            if (Env.Make != Nil)
               Env.Make = Env.Make.Cdr = x;
            else
               Env.Yoke = Env.Make = x;
            while ((y = Env.Make.Cdr) instanceof Cell)
               Env.Make = y;
         } while ((ex = ex.Cdr) instanceof Cell);
         return x;
      }

      final static Any do196(Any ex) { // link
         Any x;
         ex = ex.Cdr;
         do {
            x = ex.Car.eval();
            if (Env.Make != Nil)
               Env.Make = Env.Make.Cdr = new Cell(x, Nil);
            else
               Env.Yoke = Env.Make = new Cell(x, Nil);
         } while ((ex = ex.Cdr) instanceof Cell);
         return x;
      }

      final static Any do197(Any ex) { // yoke
         Any x;
         ex = ex.Cdr;
         do {
            x = ex.Car.eval();
            Env.Yoke = new Cell(x, Env.Yoke);
            if (Env.Make == Nil)
               Env.Make = Env.Yoke;
         } while ((ex = ex.Cdr) instanceof Cell);
         return x;
      }

      final static Any do198(Any ex) { // copy
         Any w, x, y, z;
         if (!((x = ex.Cdr.Car.eval()) instanceof Cell))
            return x;
         for (w = y = new Cell(x.Car, (z = x).Cdr); (x = y.Cdr) instanceof Cell; y = y.Cdr = new Cell(x.Car, x.Cdr))
            if (x == z) {
               y.Cdr = w;
               break;
            }
         return w;
      }

      final static Any do199(Any ex) { // mix
         Any x, y, z;
         if (!((y = (ex = ex.Cdr).Car.eval()) instanceof Cell) && y != Nil)
            return y;
         if (!((ex = ex.Cdr) instanceof Cell))
            return Nil;
         z = x = new Cell(ex.Car instanceof Number? nth(xInt(ex.Car), y).Car : ex.Car.eval(), Nil);
         while ((ex = ex.Cdr) instanceof Cell)
            x = x.Cdr = new Cell(ex.Car instanceof Number? nth(xInt(ex.Car), y).Car : ex.Car.eval(), Nil);
         return z;
      }

      final static Any do200(Any ex) { // append
         Any x, y, z;
         for (ex = ex.Cdr; (z = ex.Cdr) instanceof Cell; ex = z) {
            if ((x = ex.Car.eval()) instanceof Cell) {
               z = y = new Cell(x.Car, x.Cdr);
               while ((x = y.Cdr) instanceof Cell)
                  y = y.Cdr = new Cell(x.Car, x.Cdr);
               while ((ex = ex.Cdr).Cdr instanceof Cell) {
                  for (x = ex.Car.eval(); x instanceof Cell; x = y.Cdr)
                     y = y.Cdr = new Cell(x.Car, x.Cdr);
                  y.Cdr = x;
               }
               y.Cdr = ex.Car.eval();
               return z;
            }
         }
         return ex.Car.eval();
      }

      final static Any do201(Any ex) { // delete
         Any w, x, y, z;
         y = (x = ex.Cdr).Car.eval();
         if (!((x = x.Cdr.Car.eval()) instanceof Cell))
            return x;
         if (y.equal(x.Car))
            return x.Cdr;
         w = z = new Cell(x.Car, Nil);
         while ((x = x.Cdr) instanceof Cell) {
            if (y.equal(x.Car)) {
               z.Cdr = x.Cdr;
               return w;
            }
            z = z.Cdr = new Cell(x.Car, Nil);
         }
         z.Cdr = x;
         return w;
      }

      final static Any do202(Any ex) { // delq
         Any w, x, y, z;
         y = (x = ex.Cdr).Car.eval();
         if (!((x = x.Cdr.Car.eval()) instanceof Cell))
            return x;
         if (y == x.Car)
            return x.Cdr;
         w = z = new Cell(x.Car, Nil);
         while ((x = x.Cdr) instanceof Cell) {
            if (y == x.Car) {
               z.Cdr = x.Cdr;
               return w;
            }
            z = z.Cdr = new Cell(x.Car, Nil);
         }
         z.Cdr = x;
         return w;
      }

      final static Any do203(Any ex) { // replace
         int i, j;
         Any w, x, y, z;
         Any[] v;
         if (!((y = (x = ex.Cdr).Car.eval()) instanceof Cell))
            return y;
         for (v = new Any[6], i = 0;  (x = x.Cdr) instanceof Cell;  ++i)
            v = append(v, i, x.Car.eval());
         for (x = y.Car, j = 0;  j < i;  j += 2)
            if (x.equal(v[j])) {
               x = v[j+1];
               break;
            }
         for (w = z = new Cell(x, Nil); (y = y.Cdr) instanceof Cell; z = z.Cdr = new Cell(x, Nil))
            for (x = y.Car, j = 0;  j < i;  j += 2)
               if (x.equal(v[j])) {
                  x = v[j+1];
                  break;
               }
         z.Cdr = y;
         return w;
      }

      final static Any do204(Any ex) { // strip
         Any x;
         for (x = ex.Cdr.Car.eval();  x instanceof Cell && x.Car == Quote && x != x.Cdr;  x = x.Cdr);
         return x;
      }

      final static Any do205(Any ex) { // split
         int i, j;
         Any x, y, z;
         Any[] v;
         if (!((z = (x = ex.Cdr).Car.eval()) instanceof Cell))
            return z;
         for (v = new Any[6], i = 0;  (x = x.Cdr) instanceof Cell;  ++i)
            v = append(v, i, x.Car.eval());
         Any res = x = Nil;
         Any sub = y = Nil;
      spl:
         do {
            for (j = 0;  j < i;  ++j) {
               if (z.Car.equal(v[j])) {
                  if (x == Nil)
                     x = res = new Cell(sub, Nil);
                  else
                     x = x.Cdr = new Cell(sub, Nil);
                  y = sub = Nil;
                  continue spl;
               }
            }
            if (y == Nil)
               y = sub = new Cell(z.Car, Nil);
            else
               y = y.Cdr = new Cell(z.Car, Nil);
         } while ((z = z.Cdr) instanceof Cell);
         y = new Cell(sub, Nil);
         if (x == Nil)
            return y;
         x.Cdr = y;
         return res;
      }

      final static Any do206(Any ex) { // reverse
         Any x, y;
         x = ex.Cdr.Car.eval();
         for (y = Nil; x instanceof Cell; x = x.Cdr)
            y = new Cell(x.Car, y);
         return y;
      }

      final static Any do207(Any ex) { // flip
         int i;
         Any x, y, z;
         if (!((y = (ex = ex.Cdr).Car.eval()) instanceof Cell) || !((z = y.Cdr) instanceof Cell))
            return y;
         if (ex.Cdr == Nil) {
            y.Cdr = Nil;
            for (;;) {
               x = z.Cdr;  z.Cdr = y;
               if (!(x instanceof Cell))
                  return z;
               y = z;  z = x;
            }
         }
         if ((i = evInt(ex.Cdr) - 1) <= 0)
            return y;
         y.Cdr = z.Cdr;  z.Cdr = y;
         while (--i != 0 && (x = y.Cdr) instanceof Cell) {
            y.Cdr = x.Cdr;  x.Cdr = z;  z = x;
         }
         return z;
      }

      final static Any do208(Any ex) { // trim
         return trim(ex.Cdr.Car.eval());
      }

      final static Any do209(Any ex) { // clip
         Any x;
         for (x = ex.Cdr.Car.eval(); x instanceof Cell && isBlank(x.Car); x = x.Cdr);
         return trim(x);
      }

      final static Any do210(Any ex) { // head
         int i;
         Any x, y, z;
         if ((z = (x = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         x = x.Cdr.Car.eval();
         if (z instanceof Cell) {
            if (x instanceof Cell) {
               for (y = z; y.Car.equal(x.Car); x = x.Cdr)
                  if (!((y = y.Cdr) instanceof Cell))
                     return z;
            }
            return Nil;
         }
         if ((i = xInt(z)) == 0)
            return Nil;
         if (!(x instanceof Cell))
            return x;
         if (i < 0  &&  (i += x.length()) <= 0)
            return Nil;
         z = y = new Cell(x.Car, Nil);
         while (--i != 0  &&  (x = x.Cdr) instanceof Cell)
            y = y.Cdr = new Cell(x.Car, Nil);
         return z;
      }

      final static Any do211(Any ex) { // tail
         int i;
         Any x, y, z;
         if ((z = (x = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         x = x.Cdr.Car.eval();
         if (z instanceof Cell) {
            if (x instanceof Cell) {
               do
                  if (x.equal(z))
                     return z;
               while ((x = x.Cdr) instanceof Cell);
            }
            return Nil;
         }
         if ((i = xInt(z)) == 0)
            return Nil;
         if (!(x instanceof Cell))
            return x;
         if (i < 0)
            return nth(1 - i, x);
         for (y = x.Cdr;  --i != 0;  y = y.Cdr)
            if (!(y instanceof Cell))
               return x;
         while (y instanceof Cell) {
            x = x.Cdr;  y = y.Cdr;
         }
         return x;
      }

      final static Any do212(Any ex) { // stem
         int i, j;
         Any x, y;
         Any[] v;
         y = (x = ex.Cdr).Car.eval();
         for (v = new Any[6], i = 0;  (x = x.Cdr) instanceof Cell;  ++i)
            v = append(v, i, x.Car.eval());
         for (x = y; x instanceof Cell; x = x.Cdr)
            for (j = 0;  j < i;  ++j)
               if (x.Car.equal(v[j])) {
                  y = x.Cdr;
                  break;
               }
         return y;
      }

      final static Any do213(Any ex) { // fin
         Any x;
         for (x = ex.Cdr.Car.eval(); x instanceof Cell; x = x.Cdr);
         return x;
      }

      final static Any do214(Any ex) { // last
         Any x;
         if (!((x = ex.Cdr.Car.eval()) instanceof Cell))
            return x;
         while (x.Cdr instanceof Cell)
            x = x.Cdr;
         return x.Car;
      }

      final static Any do215(Any ex) { // ==
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell)
            if (y != x.Car.eval())
               return Nil;
         return T;
      }

      final static Any do216(Any ex) { // n==
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell)
            if (y != x.Car.eval())
               return T;
         return Nil;
      }

      final static Any do217(Any ex) { // =
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell)
            if (!y.equal(x.Car.eval()))
               return Nil;
         return T;
      }

      final static Any do218(Any ex) { // <>
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell)
            if (!y.equal(x.Car.eval()))
               return T;
         return Nil;
      }

      final static Any do219(Any ex) { // =0
         Any x;
         return ex.Cdr.Car.eval().equal(Zero)? Zero : Nil;
      }

      final static Any do220(Any ex) { // =T
         return T == ex.Cdr.Car.eval()? T : Nil;
      }

      final static Any do221(Any ex) { // n0
         return ex.Cdr.Car.eval().equal(Zero)? Nil : T;
      }

      final static Any do222(Any ex) { // nT
         return T == ex.Cdr.Car.eval()? Nil : T;
      }

      final static Any do223(Any ex) { // <
         Any x, y, z;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell) {
            z = x.Car.eval();
            if (y.compare(z) >= 0)
               return Nil;
            y = z;
         }
         return T;
      }

      final static Any do224(Any ex) { // <=
         Any x, y, z;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell) {
            z = x.Car.eval();
            if (y.compare(z) > 0)
               return Nil;
            y = z;
         }
         return T;
      }

      final static Any do225(Any ex) { // >
         Any x, y;
         x = (ex = ex.Cdr).Car.eval();
         while (ex.Cdr instanceof Cell) {
            y = (ex = ex.Cdr).Car.eval();
            if (x.compare(y) <= 0)
               return Nil;
            x = y;
         }
         return T;
      }

      final static Any do226(Any ex) { // >=
         Any x, y, z;
         y = (x = ex.Cdr).Car.eval();
         while ((x = x.Cdr) instanceof Cell) {
            z = x.Car.eval();
            if (y.compare(z) < 0)
               return Nil;
            y = z;
         }
         return T;
      }

      final static Any do227(Any ex) { // max
         Any x, y;
         for (y = (ex = ex.Cdr).Car.eval(); (ex = ex.Cdr) instanceof Cell;)
            if ((x = ex.Car.eval()).compare(y) > 0)
               y = x;
         return y;
      }

      final static Any do228(Any ex) { // min
         Any x, y;
         for (y = (ex = ex.Cdr).Car.eval(); (ex = ex.Cdr) instanceof Cell;)
            if ((x = ex.Car.eval()).compare(y) < 0)
               y = x;
         return y;
      }

      final static Any do229(Any ex) { // atom
         return ex.Cdr.Car.eval() instanceof Cell? Nil : T;
      }

      final static Any do230(Any ex) { // pair
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Cell? x : Nil;
      }

      final static Any do231(Any ex) { // lst?
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Cell || x == Nil? T : Nil;
      }

      final static Any do232(Any ex) { // num?
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Number? x : Nil;
      }

      final static Any do233(Any ex) { // sym?
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Symbol || x == Nil? T : Nil;
      }

      final static Any do234(Any ex) { // flg?
         Any x;
         return (x = ex.Cdr.Car.eval()) == Nil || x == T? T : Nil;
      }

      final static Any do235(Any ex) { // member
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         return (x = member(x, ex.Cdr.Car.eval())) == null? Nil : x;
      }

      final static Any do236(Any ex) { // memq
         Any x;
         x = (ex = ex.Cdr).Car.eval();
         return (x = memq(x, ex.Cdr.Car.eval())) == null? Nil : x;
      }

      final static Any do237(Any ex) { // mmeq
         Any x, y, z;
         x = (ex = ex.Cdr).Car.eval();
         for (y = (ex = ex.Cdr).Car.eval(); x instanceof Cell; x = x.Cdr)
            if ((z = memq(x.Car, y)) != null)
               return x;
         return Nil;
      }

      final static Any do238(Any ex) { // sect
         Any w, x, y, z;
         y = (x = ex.Cdr).Car.eval();
         z = x.Cdr.Car.eval();
         w = x = Nil;
         while (y instanceof Cell) {
            if (member(y.Car, z) != null)
               if (x == Nil)
                  x = w = new Cell(y.Car, Nil);
               else
                  x = x.Cdr = new Cell(y.Car, Nil);
            y = y.Cdr;
         }
         return w;
      }

      final static Any do239(Any ex) { // diff
         Any w, x, y, z;
         y = (x = ex.Cdr).Car.eval();
         z = x.Cdr.Car.eval();
         w = x = Nil;
         while (y instanceof Cell) {
            if (member(y.Car, z) == null)
               if (x == Nil)
                  x = w = new Cell(y.Car, Nil);
               else
                  x = x.Cdr = new Cell(y.Car, Nil);
            y = y.Cdr;
         }
         return w;
      }

      final static Any do240(Any ex) { // index
         int i;
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         return (i = indx(y, x.Cdr.Car.eval())) == 0? Nil : new Number(i);
      }

      final static Any do241(Any ex) { // offset
         int i;
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         x = x.Cdr.Car.eval();
         for (i = 1;  x instanceof Cell; ++i, x = x.Cdr)
            if (x.equal(y))
               return new Number(i);
         return Nil;
      }

      final static Any do242(Any ex) { // length
         long n;
         return (n = ex.Cdr.Car.eval().length()) >= 0? new Number(n) : T;
      }

      final static Any do243(Any ex) { // size
         return new Number(ex.Cdr.Car.eval().size());
      }

      final static Any do244(Any ex) { // assoc
         Any x, y, z;
         y = (x = ex.Cdr).Car.eval();
         x = x.Cdr.Car.eval();
         for (; x instanceof Cell; x = x.Cdr)
            if ((z = x.Car) instanceof Cell && y.equal(z.Car))
               return z;
         return Nil;
      }

      final static Any do245(Any ex) { // asoq
         Any x, y, z;
         y = (x = ex.Cdr).Car.eval();
         x = x.Cdr.Car.eval();
         for (; x instanceof Cell; x = x.Cdr)
            if ((z = x.Car) instanceof Cell && y == z.Car)
               return z;
         return Nil;
      }

      final static Any do246(Any ex) { // rank
         Any w, x, y, z;
         w = (x = ex.Cdr).Car.eval();
         y = (x = x.Cdr).Car.eval();
         z = Nil;
         if (x.Cdr.Car.eval() == Nil)
            for (; y instanceof Cell; y = y.Cdr) {
               if ((x = y.Car) instanceof Cell && x.Car.compare(w) > 0)
                  break;
               z = y;
            }
         else
            for (; y instanceof Cell; y = y.Cdr) {
               if ((x = y.Car) instanceof Cell && w.compare(x.Car) > 0)
                  break;
               z = y;
            }
         return z.Car;
      }

      final static Any do247(Any ex) { // match
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         return match(y, x.Cdr.Car.eval())? T : Nil;
      }

      final static Any do248(Any ex) { // fill
         Any x, y;
         y = (x = ex.Cdr).Car.eval();
         return (x = fill(y, x.Cdr.Car.eval())) == null? y : x;
      }

      final static Any do249(Any ex) { // prove
         int i;
         Any x, y;
         if (!((y = (ex = ex.Cdr).Car.eval()) instanceof Cell))
            return Nil;
         Any dbg = ex.Cdr.Car.eval(), at = At.Car, envSave = Penv, nlSave = Pnl;
         Penv = y.Car.Car;  y.Car = y.Car.Cdr;
         Any n = Penv.Car;  Penv = Penv.Cdr;
         Pnl = Penv.Car;  Penv = Penv.Cdr;
         Any alt = Penv.Car;  Penv = Penv.Cdr;
         Any tp1 = Penv.Car;  Penv = Penv.Cdr;
         Any tp2 = Penv.Car;  Penv = Penv.Cdr;
         Any e = Nil;
         while (tp1 instanceof Cell || tp2 instanceof Cell) {
            if (alt instanceof Cell) {
               e = Penv;
               if (!unify((Number)Pnl.Car, tp1.Car.Cdr, (Number)n, alt.Car.Car)) {
                  if (!((alt = alt.Cdr) instanceof Cell)) {
                     Penv = y.Car.Car;  y.Car = y.Car.Cdr;
                     n = Penv.Car;  Penv = Penv.Cdr;
                     Pnl = Penv.Car;  Penv = Penv.Cdr;
                     alt = Penv.Car;  Penv = Penv.Cdr;
                     tp1 = Penv.Car;  Penv = Penv.Cdr;
                     tp2 = Penv.Car;  Penv = Penv.Cdr;
                  }
               }
               else {
                  if (dbg != Nil  &&  memq(tp1.Car.Car, dbg) != null) {
                     OutFile.Wr.print(indx(alt.Car, tp1.Car.Car.get(T)));
                     OutFile.space();
                     OutFile.print(uniFill(tp1.Car));
                     OutFile.newline();
                  }
                  if (alt.Cdr instanceof Cell)
                     y.Car =
                        new Cell(
                           new Cell(n,
                              new Cell(Pnl,
                                 new Cell(alt.Cdr,
                                    new Cell(tp1, new Cell(tp2, e)) ) ) ),
                           y.Car );
                  Pnl = new Cell(n, Pnl);
                  n = ((Number)n).add(One);
                  tp2 = new Cell(tp1.Cdr, tp2);
                  tp1 = alt.Car.Cdr;
                  alt = Nil;
               }
            }
            else if (!((x = tp1) instanceof Cell)) {
               tp1 = tp2.Car;
               tp2 = tp2.Cdr;
               Pnl = Pnl.Cdr;
            }
            else if (x.Car == T) {
               while (y.Car instanceof Cell && ((Number)y.Car.Car.Car).Cnt >= ((Number)Pnl.Car).Cnt)
                  y.Car = y.Car.Cdr;
               tp1 = x.Cdr;
            }
            else if (x.Car.Car instanceof Number) {
               e = x.Car.Cdr.eval();
               for (i = ((Number)x.Car.Car).Cnt, x = Pnl;  --i > 0;)
                  x = x.Cdr;
               Pnl = new Cell(x.Car, Pnl);
               tp2 = new Cell(tp1.Cdr, tp2);
               tp1 = e;
            }
            else if (x.Car.Car instanceof Symbol && firstChar(x.Car.Car) == '@') {
               if ((e = x.Car.Cdr.eval()) != Nil  && unify((Number)Pnl.Car, x.Car.Car, (Number)Pnl.Car, e))
                  tp1 = x.Cdr;
               else {
                  Penv = y.Car.Car;  y.Car = y.Car.Cdr;
                  n = Penv.Car;  Penv = Penv.Cdr;
                  Pnl = Penv.Car;  Penv = Penv.Cdr;
                  alt = Penv.Car;  Penv = Penv.Cdr;
                  tp1 = Penv.Car;  Penv = Penv.Cdr;
                  tp2 = Penv.Car;  Penv = Penv.Cdr;
               }
            }
            else if (!((alt = x.Car.Car.get(T)) instanceof Cell)) {
               Penv = y.Car.Car;  y.Car = y.Car.Cdr;
               n = Penv.Car;  Penv = Penv.Cdr;
               Pnl = Penv.Car;  Penv = Penv.Cdr;
               alt = Penv.Car;  Penv = Penv.Cdr;
               tp1 = Penv.Car;  Penv = Penv.Cdr;
               tp2 = Penv.Car;  Penv = Penv.Cdr;
            }
         }
         for (e = Nil,  x = Penv;  x.Cdr instanceof Cell;  x = x.Cdr)
            if (x.Car.Car.Car.equal(Zero))
               e = new Cell(new Cell(x.Car.Car.Cdr, lookup(Zero, x.Car.Car.Cdr)), e);
         At.Car = at;
         x = e instanceof Cell? e : Penv instanceof Cell? T : Nil;
         Penv = envSave;  Pnl = nlSave;
         return x;
      }

      final static Any do250(Any ex) { // ->
         int i;
         Any x;
         if (!(ex.Cdr.Cdr.Car instanceof Number))
            return lookup((Number)Pnl.Car, ex.Cdr.Car);
         for (i = ((Number)ex.Cdr.Cdr.Car).Cnt, x = Pnl;  --i > 0;)
            x = x.Cdr;
         return lookup((Number)x.Car, ex.Cdr.Car);
      }

      final static Any do251(Any ex) { // unify
         Any x;
         x = ex.Cdr.Car.eval();
         return unify((Number)Pnl.Cdr.Car, x, (Number)Pnl.Car, x)? Penv : Nil;
      }

      final static Any do252(Any ex) { // sort
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Cell && x.Cdr instanceof Cell? sort(ex, x, ex.Cdr.Cdr.Car.eval()) : x;
      }

      final static Any do253(Any ex) { // format
         int i;
         Any x, y;
         x = (ex = ex.Cdr).Car.eval();
         i = (y = (ex = ex.Cdr).Car.eval()) == Nil? 0 : ((Number)y).Cnt;
         return format(x, i, ex.Cdr);
      }

      final static Any do254(Any ex) { // +
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.add((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do255(Any ex) { // -
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         num = (Number)x;
         if (!(ex.Cdr instanceof Cell))
            return num.neg();
         do {
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
            num = num.sub((Number)x);
         } while (ex.Cdr instanceof Cell);
         return num;
      }

      final static Any do256(Any ex) { // inc
         Any x, y;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         if (x instanceof Number)
            return ((Number)x).add(One);
         if (!(ex.Cdr instanceof Cell)) {
            if (x.Car == Nil)
               return Nil;
            x.Car = y = ((Number)x.Car).add(One);
         }
         else {
            y = ex.Cdr.Car.eval();
            if (x.Car == Nil || y == Nil)
               return Nil;
            x.Car = y = ((Number)x.Car).add((Number)y);
         }
         return y;
      }

      final static Any do257(Any ex) { // dec
         Any x, y;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         if (x instanceof Number)
            return ((Number)x).sub(One);
         if (!(ex.Cdr instanceof Cell)) {
            if (x.Car == Nil)
               return Nil;
            x.Car = y = ((Number)x.Car).sub(One);
         }
         else {
            y = ex.Cdr.Car.eval();
            if (x.Car == Nil || y == Nil)
               return Nil;
            x.Car = y = ((Number)x.Car).sub((Number)y);
         }
         return y;
      }

      final static Any do258(Any ex) { // *
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.mul((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do259(Any ex) { // */
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ; num = num.mul((Number)x)) {
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
            if (!((ex.Cdr) instanceof Cell))
               return num.add(((Number)x).div(Two)).div(((Number)x));
         }
      }

      final static Any do260(Any ex) { // /
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.div((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do261(Any ex) { // %
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.rem((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do262(Any ex) { // >>
         int i;
         Any x;
         i = evInt(ex = ex.Cdr);
         if ((x = ex.Cdr.Car.eval()) == Nil)
            return Nil;
         return ((Number)x).shift(i);
      }

      final static Any do263(Any ex) { // lt0
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Number && x.compare(Zero) < 0? x : Nil;
      }

      final static Any do264(Any ex) { // ge0
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Number && x.compare(Zero) >= 0? x : Nil;
      }

      final static Any do265(Any ex) { // gt0
         Any x;
         return (x = ex.Cdr.Car.eval()) instanceof Number && x.compare(Zero) > 0? x : Nil;
      }

      final static Any do266(Any ex) { // abs
         return ((Number)ex.Cdr.Car.eval()).abs();
      }

      final static Any do267(Any ex) { // bit?
         Any x;
         Number num;
         num = (Number)(ex = ex.Cdr).Car.eval();
         while ((ex = ex.Cdr) instanceof Cell)
            if ((x = ex.Car.eval()) == Nil || !num.tst((Number)x))
               return Nil;
         return num;
      }

      final static Any do268(Any ex) { // &
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.and((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do269(Any ex) { // |
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.or((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do270(Any ex) { // x|
         Any x;
         Number num;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return Nil;
         for (num = (Number)x; ex.Cdr instanceof Cell; num = num.xor((Number)x))
            if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
               return Nil;
         return num;
      }

      final static Any do271(Any ex) { // seed
         long n;
         n = initSeed(ex.Cdr.Car.eval()) * 6364136223846793005L + 1;
         return new Number(Seed = n);
      }

      final static Any do272(Any ex) { // rand
         Any x;
         Seed = Seed * 6364136223846793005L + 1;
         if ((x = (ex = ex.Cdr).Car.eval()) == Nil)
            return new Number(Seed);
         if (x == T)
            return (Seed & 0x100000000L) == 0? Nil : T;
         return new Number(((Number)x).Cnt + (int)(Seed >>> 33) % (evInt(ex.Cdr) + 1 - ((Number)x).Cnt));
      }

      final static Any do273(Any ex) { // path
         return mkStr(path(evString(ex.Cdr)));
      }

      final static Any do274(Any ex) { // read
         Any x, y;
         if (!((x = ex.Cdr) instanceof Cell))
            x = InFile.read('\0');
         else {
            y = x.Car.eval();
            if ((x = token(y, (x = x.Cdr.Car.eval()) == Nil? '\0' : firstChar(x))) == null)
               x = Nil;
         }
         if (InFile.Name == null && InFile.Chr == '\n')
            InFile.Chr = 0;
         return x;
      }

      final static Any do275(Any ex) { // wait
         int i;
         Any x, y;
         i = (y = (x = ex.Cdr).Car.eval()) == Nil? -1 : xInt(y);
         for (x = x.Cdr; (y = x.prog()) == Nil;)
            if ((i = waitFd(ex, -1, i)) == 0)
               return x.prog();
         return y;
      }

      final static Any do276(Any ex) { // char
         Any x;
         if (!((ex = ex.Cdr) instanceof Cell)) {
            if (InFile.Chr == 0)
               InFile.get();
            x = InFile.Chr < 0? Nil : mkChar((char)InFile.Chr);
            InFile.get();
            return x;
         }
         if ((x = ex.Car.eval()) instanceof Number)
            return x.equals(Zero)? Nil : mkChar((char)((Number)x).Cnt);
         return x == T? mkChar((char)0x10000) : new Number(firstChar(x));
      }

      final static Any do277(Any ex) { // skip
         char c;
         c = firstChar(ex.Cdr.Car.eval());
         return InFile.skip(c) < 0? Nil : mkChar(c);
      }

      final static Any do278(Any ex) { // line
         int i;
         Any x, y, z;
         StringBuilder sb;
         if (InFile.Chr == 0)
            InFile.get();
         if (InFile.eol())
            return Nil;
         if (ex.Cdr.Car.eval() != Nil) {
            sb = new StringBuilder();
            do {
               sb.append((char)InFile.Chr);
               InFile.get();
            } while (!InFile.eol());
            return mkStr(sb);
         }
         for (x = y = new Cell(mkChar((char)InFile.Chr), Nil);;) {
            InFile.get();
            if (InFile.eol())
               return x;
            y = y.Cdr = new Cell(mkChar((char)InFile.Chr), Nil);
         }
      }

      final static Any do279(Any ex) { // load
         Any x, y;
         x = ex.Cdr;
         do {
            if ((y = x.Car.eval()) != T)
               y = load(ex, '>', y);
            else
               y = loadAll(ex);
         } while ((x = x.Cdr) instanceof Cell);
         return y;
      }

      final static Any do280(Any ex) { // in
         Any x;
         Env.pushInFile((x = ex.Cdr).Car.eval().rdOpen(ex));
         x = x.Cdr.prog();
         Env.popInFiles();
         return x;
      }

      final static Any do281(Any ex) { // out
         Any x;
         Env.pushOutFile((x = ex.Cdr).Car.eval().wrOpen(ex));
         x = x.Cdr.prog();
         Env.popOutFiles();
         return x;
      }

      final static Any do282(Any ex) { // open
         String str;
         str = evString(ex.Cdr);
         try {return new Number(new PicoLispReader(new FileReader(str), str, allocFd(), null, 0).Fd);}
         catch (IOException e) {}
         return Nil;
      }

      final static Any do283(Any ex) { // close
         int i;
         Any x;
         if ((i = xInt(x = ex.Cdr.Car.eval())) >= 0 && i < InFiles.length) {
            if (InFiles[i] != null) {
               InFiles[i].close();
               if (OutFiles[i] != null)
                  OutFiles[i].close();
               return x;
            }
            if (OutFiles[i] != null) {
               OutFiles[i].close();
               return x;
            }
         }
         return Nil;
      }

      final static Any do284(Any ex) { // prin
         Any x, y;
         for (y = Nil; (ex = ex.Cdr) instanceof Cell; OutFile.Wr.print((y = ex.Car.eval()).name()));
         return y;
      }

      final static Any do285(Any ex) { // prinl
         Any x, y;
         for (y = Nil; (ex = ex.Cdr) instanceof Cell; OutFile.Wr.print((y = ex.Car.eval()).name()));
         OutFile.newline();
         return y;
      }

      final static Any do286(Any ex) { // space
         int i;
         Any x;
         if ((x = ex.Cdr.Car.eval()) == Nil) {
            OutFile.space();
            return One;
         }
         for (i = xInt(x); i > 0; --i)
            OutFile.space();
         return x;
      }

      final static Any do287(Any ex) { // print
         Any x, y;
         OutFile.print(y = (x = ex.Cdr).Car.eval());
         while ((x = x.Cdr) instanceof Cell) {
            OutFile.space();
            OutFile.print(y = x.Car.eval());
         }
         return y;
      }

      final static Any do288(Any ex) { // printsp
         Any x, y;
         x = ex.Cdr;
         do {
            OutFile.print(y = x.Car.eval());
            OutFile.space();
         } while ((x = x.Cdr) instanceof Cell);
         return y;
      }

      final static Any do289(Any ex) { // println
         Any x, y;
         OutFile.print(y = (x = ex.Cdr).Car.eval());
         while ((x = x.Cdr) instanceof Cell) {
            OutFile.space();
            OutFile.print(y = x.Car.eval());
         }
         OutFile.newline();
         return y;
      }

      final static Any do290(Any ex) { // flush
         return OutFile.Wr.checkError()? Nil : T;
      }

      final static Any do291(Any ex) { // port
         ex = ex.Cdr;  // ...
         try {
            ServerSocketChannel chan = ServerSocketChannel.open();;
            chan.socket().bind(new InetSocketAddress(evInt(ex)));
            return new Number(new PicoLispReader(null, allocFd(), chan, SelectionKey.OP_ACCEPT).Fd);
         }
         catch (IOException e) {}
         return Nil;
      }

      final static Any do292(Any ex) { // accept
         int i;
         Any x;
         if ((i = xInt(x = ex.Cdr.Car.eval())) < 0 || i >= InFiles.length || InFiles[i] == null || InFiles[i].Chan == null)
            err(ex, x, "Bad socket");
         try {return mkSocket(((ServerSocketChannel)InFiles[i].Chan).accept());}
         catch (IOException e) {}
         return Nil;
      }

      final static Any do293(Any ex) { // connect
         int i;
         try {
            SocketChannel chan = SocketChannel.open();
            if (chan.connect(new InetSocketAddress(evString(ex.Cdr), evInt(ex.Cdr.Cdr))))
               return mkSocket(chan);
         }
         catch (IOException e) {}
         return Nil;
      }

      final Any apply(Any ex, boolean cf, Any[] v, int n) {
         Any x, y = Nil;
         if (n > 0) {
            y = x = new Cell(mkSymbol(cf? v[0].Car : v[0]), Nil);
            for (int i = 1; i < n; ++i)
               x = x.Cdr = new Cell(mkSymbol(cf? v[i].Car : v[i]), Nil);
         }
         return func(new Cell(this, y));
      }

      final boolean equal(Any x) {
         if (x == this)
            return true;
         if (!(x instanceof Number))
            return false;
         Number num = (Number)x;
         if (Big == null)
            return num.Big == null && Cnt == num.Cnt;
         return Big == num.Big;
      }

      final int compare(Any x) {
         if (x == this)
            return 0;
         if (x == Nil)
            return +1;
         if (!(x instanceof Number))
            return -1;
         Number num = (Number)x;
         if (Big == null)
            return num.Big == null? Cnt - num.Cnt : -1;
         return Big.compareTo(num.Big);
      }

      final long length() {return (Big == null? Integer.toString(Cnt) : Big.toString()).length();}

      final long size() {
         if (Big == null) {
            int n = 2 * (Cnt >= 0? Cnt : -Cnt);
            if (n == 0)
               return 1;
            int i = 1;
            while ((n >>= 8) != 0)
               ++i;
            return i;
         }
         return Big.toByteArray().length;
      }

      final InFrame rdOpen(Any ex) {
         int i;
         InFrame f;
         if ((i = Cnt) < 0) {
            for (f = Env.InFrames;;) {
               if ((f = f.Link) == null)
                  badFd(ex, this);
               if (++i == 0) {
                  i = f.Rd.Fd;
                  break;
               }
            }
         }
         if (i >= InFiles.length || InFiles[i] == null)
            badFd(ex, this);
         return new InFrame(InFiles[i],0);
      }

      final OutFrame wrOpen(Any ex) {
         int i;
         OutFrame f;
         if ((i = Cnt) < 0) {
            for (f = Env.OutFrames;;) {
               if ((f = f.Link) == null)
                  badFd(ex, this);
               if (++i == 0) {
                  i = f.Wr.Fd;
                  break;
               }
            }
         }
         if (i >= OutFiles.length || OutFiles[i] == null)
            badFd(ex, this);
         return new OutFrame(OutFiles[i],0);
      }

      final String name() {return Big == null? Integer.toString(Cnt) : Big.toString();}
      final public String toString() {return name();}

      final public String toString(int scl, char sep, char ign) {
         String s = name();
         StringBuilder sb = new StringBuilder();
         if (s.charAt(0) == '-') {
            sb.append('-');
            s = s.substring(1);
         }
         if ((scl = s.length() - scl - 1) < 0) {
            sb.append('0');
            sb.append(sep);
            while (scl < -1) {
               sb.append('0');
               ++scl;
            }
         }
         for (int i = 0;;) {
            sb.append(s.charAt(i++));
            if (i == s.length())
               return sb.toString();
            if (scl == 0)
               sb.append(sep);
            else if (ign != '\0'  &&  scl > 0  &&  scl % 3 == 0)
               sb.append(ign);
            --scl;
         }
      }

      final Number abs() {
         if (Big == null) {
            if (Cnt >= 0)
               return this;
            if (Cnt != Integer.MIN_VALUE)
               return new Number(-Cnt);
            return new Number(-(long)Cnt);
         }
         return new Number(Big.abs());
      }

      final Number neg() {
         if (Big == null) {
            if (Cnt != Integer.MIN_VALUE)
               return new Number(-Cnt);
            return new Number(-(long)Cnt);
         }
         return new Number(Big.negate());
      }

      final Number add(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt + (long)num.Cnt);
            return new Number(big(Cnt).add(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.add(big(num.Cnt)));
         return new Number(Big.add(num.Big));
      }

      final Number sub(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt - (long)num.Cnt);
            return new Number(big(Cnt).subtract(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.subtract(big(num.Cnt)));
         return new Number(Big.subtract(num.Big));
      }

      final Number mul(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt * (long)num.Cnt);
            return new Number(big(Cnt).multiply(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.multiply(big(num.Cnt)));
         return new Number(Big.multiply(num.Big));
      }

      final Number div(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt / (long)num.Cnt);
            return new Number(big(Cnt).divide(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.divide(big(num.Cnt)));
         return new Number(Big.divide(num.Big));
      }

      final Number rem(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt % (long)num.Cnt);
            return new Number(big(Cnt).remainder(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.remainder(big(num.Cnt)));
         return new Number(Big.remainder(num.Big));
      }

      final Number shift(int i) {
         if (Big == null) {
            if (i >= 0)
               return new Number((long)Cnt >> i);
            if (i > -32)
               return new Number((long)Cnt << -i);
            return new Number((new BigInteger(new byte[] {(byte)(Cnt>>24), (byte)(Cnt>>16), (byte)(Cnt>>8), (byte)Cnt})).shiftRight(i));
         }
         return new Number(Big.shiftRight(i));
      }

      final boolean tst(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return Cnt == (Cnt & num.Cnt);
            BigInteger b = big(Cnt);
            return b.equals(b.and(num.Big));
         }
         if (num.Big == null)
            return Big.equals(Big.and(big(num.Cnt)));
         return Big.equals(Big.and(num.Big));
      }

      final Number and(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt & (long)num.Cnt);
            return new Number(big(Cnt).and(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.and(big(num.Cnt)));
         return new Number(Big.and(num.Big));
      }

      final Number or(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt | (long)num.Cnt);
            return new Number(big(Cnt).or(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.or(big(num.Cnt)));
         return new Number(Big.or(num.Big));
      }

      final Number xor(Number num) {
         if (Big == null) {
            if (num.Big == null)
               return new Number((long)Cnt ^ (long)num.Cnt);
            return new Number(big(Cnt).xor(num.Big));
         }
         if (num.Big == null)
            return new Number(Big.xor(big(num.Cnt)));
         return new Number(Big.xor(num.Big));
      }
   }

   final static class Symbol extends Any {
      Object Obj;
      Any Prop[];
      String Name;

      Symbol(Any val, String nm) {
         Car = val == null? this : val;
         Name = nm;
      }

      Symbol(Object obj) {
         Car = this;
         Obj = obj;
      }

      final Any put(Any key, Any val) {
         if (key.equal(Zero))
            Car = val;
         else if (Prop != null) {
            Any x;
            int i = Prop.length, p = -1;
            do {
               if ((x = Prop[--i]) == null)
                  p = i;
               else if (x instanceof Cell) {
                  if (key == x.Cdr) {
                     if (val == Nil)
                        Prop[i] = null;
                     else if (val == T)
                        Prop[i] = key;
                     else
                        x.Car = val;
                     return val;
                  }
               }
               else if (key == x) {
                  if (val == Nil)
                     Prop[i] = null;
                  else if (val != T)
                     Prop[i] = new Cell(val, key);
                  return val;
               }
            } while (i != 0);
            if (val != Nil) {
               if (p < 0) {
                  Any[] a = new Any[(p = Prop.length) * 2];
                  System.arraycopy(Prop, 0, a, 0, p);
                  Prop = a;
               }
               Prop[p] = val != T? new Cell(val, key): key;
            }
         }
         else if (val != Nil)
            (Prop = new Any[3])[2] = val != T? new Cell(val, key) : key;
         return val;
      }

      final Any get(Any key) {
         if (key.equal(Zero))
            return Car;
         if (Prop == null)
            return Nil;
         Any x;
         int i = Prop.length;
         do {
            if ((x = Prop[--i]) != null) {
               if (x instanceof Cell) {
                  if (key == x.Cdr)
                     return x.Car;
               }
               else if (key == x)
                  return T;
            }
         } while (i != 0);
         return Nil;
      }

      final Any prop(Any key) {
         if (Prop == null)
            return Nil;
         Any x;
         int i = Prop.length;
         do {
            if ((x = Prop[--i]) != null) {
               if (x instanceof Cell) {
                  if (key == x.Cdr)
                     return x;
               }
               else if (key == x)
                  return key;
            }
         } while (i != 0);
         return Nil;
      }

      final Any putl(Any lst) {
         Prop = new Any[6];
         int i = 0;
         for (Any y = lst; y instanceof Cell; y = y.Cdr)
            Prop = append(Prop, i++, y.Car);
         return lst;
      }

      final Any getl() {
         Any x = Nil;
         if (Prop != null)
            for (int i = Prop.length; --i >= 0;)
               if (Prop[i] != null)
                  x = new Cell(Prop[i], x);
         return x;
      }

      final Any eval() {return Car;}
      final Any prog() {return Car;}
      final Any run() {return Car;}

      final Any call(Any ex) {
         if (Car == Nil)
            undefined(this, ex);
         return Car.func(ex);
      }

      final Any func(Any ex) {return Car.func(ex);}

      final Any apply(Any ex, boolean cf, Any[] v, int n) {
         if (Car == Meth.Car) {
            Any x, y, z, o = cf? v[0].Car : v[0];
            TheCls = null;  TheKey = this;
            if ((z = method(o)) != null) {
               int i;
               Any cls = Env.Cls;  Any key = Env.Key;
               Env.Cls = TheCls;  Env.Key = TheKey;
               Bind bnd = new Bind();  bnd.add(At.Car);  bnd.add(At);
               for (x = z.Car, i = 0; x instanceof Cell; ++i) {
                  bnd.add((y = x.Car).Car);  // Save value
                  bnd.add(y);  // and symbol
                  y.Car = i >= n? Nil : cf? v[i].Car : v[i];
                  x = x.Cdr;
               }
               if (x == Nil || x != At) {
                  if (x != Nil) {
                     bnd.add(x.Car);  // Save value
                     bnd.add(x);  // and symbol
                     x.Car = Nil;  // Set to NIL
                  }
                  bnd.add(This.Car);
                  bnd.add(This);
                  This.Car = o;
                  Env.Bind = bnd;
                  x = z.Cdr.prog();
               }
               else {
                  int next, argc, j = 0;
                  Any arg, args[], av[] = null;
                  if (i < n) {
                     av = new Any[6];
                     do
                        av = append(av, j++, x.Car.eval());
                     while (++i < n);
                  }
                  next = Env.Next;  Env.Next = 0;
                  argc = Env.ArgC;  Env.ArgC = j;
                  arg = Env.Arg;    Env.Arg = Nil;
                  args = Env.Args;  Env.Args = av;
                  bnd.add(This.Car);
                  bnd.add(This);
                  This.Car = o;
                  Env.Bind = bnd;
                  x = z.Cdr.prog();
                  Env.Args = args;
                  Env.Arg = arg;
               }
               for (i = bnd.Cnt; (i -= 2) >= 0;)
                  bnd.Data[i+1].Car = bnd.Data[i];
               Env.Bind = bnd.Link;
               Env.Cls = cls;  Env.Key = key;
               return x;
            }
            err(ex, o, "Bad object");
         }
         if (Car == Nil || Car == this)
            undefined(this, ex);
         return Car.apply(ex, cf, v, n);
      }

      final boolean equal(Any x) {
         if (x == this)
            return true;
         if (x instanceof Symbol) {
            Symbol s = (Symbol)x;
            if (Name != null)
               return Name.equals(s.Name);
            if (Obj != null)
               return Obj.equals(s.Obj);
         }
         return false;
      }

      final int compare(Any x) {
         if (x == this)
            return 0;
         if (this == T || x == Nil || x instanceof Number)
            return +1;
         if (x == T  || x instanceof Cell)
            return -1;
         String a = Name;
         String b = ((Symbol)x).Name;
         if (a == null)
            return b == null? hashCode() - x.hashCode() : -1;
         if (b == null)
            return +1;
         return a.compareTo(b);
      }

      final long length() {return name().length();}
      final long size() {return name().getBytes().length;}

      final InFrame rdOpen(Any ex) {
         try {
            String nm = path(name());
            if (nm.charAt(0) == '+')
               nm = nm.substring(1);  // No file reader with "rw" mode
            return new InFrame(new PicoLispReader(new LineNumberReader(new FileReader(nm)), nm, allocFd(), null, 0), 1);
         }
         catch (IOException e) {
            err(ex, this, "Read open error");
            return null;
         }
      }

      final OutFrame wrOpen(Any ex) {
         try {
            String nm = path(name());
            if (nm.charAt(0) == '+')
               return new OutFrame(new PicoLispWriter(new PrintWriter(new FileWriter(nm.substring(1), true)), nm, allocFd()), 1);
            return new OutFrame(new PicoLispWriter(new PrintWriter(nm), nm, allocFd()), 1);
         }
         catch (IOException e) {
            err(ex, this, "Write open error");
            return null;
         }
      }

      final String name() {return Name != null? Name : Obj == null? "" : Obj.toString();}

      final public String toString() {
         if (Name == null) {
            String s;
            if (Obj == null)
               return "$" + hashCode();
            int i = (s = Obj.getClass().toString()).lastIndexOf('.');
            if (i >= 0)
               s = s.substring(i + 1);
            if (s.startsWith("class "))
               s = s.substring(6);
            return "$" + s;

         }
         if (Intern.get(Name) == this) {
            if (Name.equals("."))
               return "\\.";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Name.length(); ++i) {
               char c = Name.charAt(i);
               if (Delim.indexOf(c) >= 0)
                  sb.append('\\');
               sb.append(c);
            }
            return sb.toString();
         }
         StringBuilder sb = new StringBuilder();
         sb.append('\"');
         for (int i = 0; i < Name.length(); ++i) {
            char c = Name.charAt(i);
            if (c == '\\' || c == '^' || c == '"')
               sb.append('\\');
            else if (c == 127)
               {sb.append('^');  c = '?';}
            else if (c < ' ')
               {sb.append('^');  c |= 0x40;}
            sb.append(c);
         }
         sb.append('\"');
         return sb.toString();
      }

      final Any parse(boolean skp, Any s) {
         Any x, y;
         PicoLispReader rd;
         if (s == null)
            rd = new PicoLispReader(name(), '\n', ']');
         else
            rd = new PicoLispReader(name(), '\0', '\0');
         if (skp)
            rd.get();
         if (s == null)
            return rd.rdList();
         if ((x = token(s, '\0')) == null)
            return Nil;
         y = new Cell(x, Nil);
         while ((x = token(s, '\0')) != null)
            y = y.Cdr = new Cell(x, Nil);
         return y;
      }
   }


   final static class NilSym extends Any {
      NilSym() {
         Car = this;
         Cdr = this;
      }

      final Any put(Any key, Any val) {return protError(this);}
      final Any get(Any key) {return this;}
      final Any prop(Any key) {return this;}
      final Any putl(Any lst) {return protError(this);}
      final Any getl() {return protError(this);}
      final Any eval() {return this;}
      final Any prog() {return this;}
      final Any run() {return this;}
      final Any call(Any ex) {return undefined(this,ex);}
      final Any func(Any ex) {return undefined(this,ex);}
      final Any apply(Any ex, boolean cf, Any[] v, int n) {return undefined(this,ex);}
      final boolean equal(Any x) {return x == Nil;}
      final int compare(Any x) {return x == this? 0 : -1;}
      final long length() {return 0;}
      final long size() {return 0;}
      final InFrame rdOpen(Any ex) {return new InFrame(InFiles[0], 0);}
      final OutFrame wrOpen(Any ex) {return new OutFrame(OutFiles[1], 0);}
      final String name() {return "";}
      final public String toString() {return "NIL";}
   }

   final static class Cell extends Any {
      Cell(Any car, Any cdr) {
         Car = car;
         Cdr = cdr;
      }

      final Any put(Any key, Any val) {return symError(this);}

      final Any get(Any key) {
         Any x, y = this;
         if (key instanceof Number) {
            int n = ((Number)key).Cnt;
            if (n > 0) {
               while (--n != 0)
                  y = y.Cdr;
               return y.Car;
            }
            if (n < 0) {
               while (++n != 0)
                  y = y.Cdr;
               return y.Cdr;
            }
         }
         else
            do
               if ((x = y.Car) instanceof Cell  &&  key == x.Car)
                  return x.Cdr;
            while ((y = y.Cdr) instanceof Cell);
         return Nil;
      }

      final Any prop(Any key) {return symError(this);}
      final Any putl(Any lst) {return symError(this);}
      final Any getl() {return symError(this);}
      final Any eval() {return Car.call(this);}

      final Any prog() {
         Any ex;
         for (ex = this; ex.Cdr != Nil; ex = ex.Cdr)
            ex.Car.eval();
         return ex.Car.eval();
      }

      final Any run() {
         Any x, at = At.Car;
         Any ex = this;
         do
            x = ex.Car.eval();
         while ((ex = ex.Cdr) != Nil);
         At.Car = at;
         return x;
      }

      final Any call(Any ex) {return eval().func(ex);}

      final Any func(Any ex) {
         int i;
         Any x, y;
         Bind bnd = new Bind();  bnd.add(At.Car);  bnd.add(At);
         for (x = Car; x instanceof Cell; x = x.Cdr) {
            bnd.add((ex = ex.Cdr).Car.eval());  // Save new value
            bnd.add(x.Car);  // and symbol
         }
         if (x == Nil || x != At) {
            i = bnd.Cnt;
            if (x != Nil) {
               bnd.add(x.Car);  // Save old value
               bnd.add(x);  // and symbol
               x.Car = ex.Cdr;  // Set new value
            }
            do {
               y = bnd.Data[--i];
               x = y.Car;
               y.Car = bnd.Data[--i];  // Set new value
               bnd.Data[i] = x;  // Save old value
            } while (i > 0);
            Env.Bind = bnd;
            x = Cdr.prog();
         }
         else {
            int next, argc, j = 0;
            Any arg, args[], av[] = null;
            if (ex.Cdr != Nil) {
               av = new Any[6];
               do
                  av = append(av, j++, (ex = ex.Cdr).Car.eval());
               while (ex.Cdr != Nil);
            }
            next = Env.Next;  Env.Next = 0;
            argc = Env.ArgC;  Env.ArgC = j;
            arg = Env.Arg;    Env.Arg = Nil;
            args = Env.Args;  Env.Args = av;
            i = bnd.Cnt;
            do {
               y = bnd.Data[--i];
               x = y.Car;
               y.Car = bnd.Data[--i];  // Set new value
               bnd.Data[i] = x;  // Save old value
            } while (i > 0);
            Env.Bind = bnd;
            x = Cdr.prog();
            Env.Args = args;
            Env.Arg = arg;
         }
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
         return x;
      }

      final Any apply(Any ex, boolean cf, Any[] v, int n) {
         int i;
         Any x, y;
         Bind bnd = new Bind();  bnd.add(At.Car);  bnd.add(At);
         for (x = Car, i = 0; x instanceof Cell; ++i, x = x.Cdr) {
            bnd.add((y = x.Car).Car);  // Save value
            bnd.add(y);  // and symbol
            y.Car = i >= n? Nil : cf? v[i].Car : v[i];
         }
         if (x == Nil || x != At) {
            if (x != Nil) {
               bnd.add(x.Car);  // Save old value
               bnd.add(x);  // and symbol
               x.Car = Nil;  // Set to NIL
            }
            Env.Bind = bnd;
            x = Cdr.prog();
         }
         else {
            int next, argc, j = 0;
            Any arg, args[], av[] = null;
            if (i < n) {
               av = new Any[6];
               do
                  av = append(av, j++, cf? v[i].Car : v[i]);
               while (++i < n);
            }
            next = Env.Next;  Env.Next = 0;
            argc = Env.ArgC;  Env.ArgC = j;
            arg = Env.Arg;    Env.Arg = Nil;
            args = Env.Args;  Env.Args = av;
            Env.Bind = bnd;
            x = Cdr.prog();
            Env.Args = args;
            Env.Arg = arg;
         }
         for (i = bnd.Cnt; (i -= 2) >= 0;)
            bnd.Data[i+1].Car = bnd.Data[i];
         Env.Bind = bnd.Link;
         return x;
      }

      final boolean equal(Any x) {
         if (!(x instanceof Cell))
            return false;
         Any y = this;
         while (x.Car == Quote) {
            if (y.Car != Quote)
               return false;
            if (x == x.Cdr)
               return y == y.Cdr;
            if (y == y.Cdr)
               return false;
            if (!(x.Cdr instanceof Cell))
               return x.Cdr.equal(y.Cdr);
            x = x.Cdr;
            if (!(y.Cdr instanceof Cell))
               return false;
            y = y;
         }
         Any a = x;
         Any b = y;
         for (;;) {
            if (!x.Car.equal(y.Car))
               return false;
            if (!(x.Cdr instanceof Cell))
               return x.Cdr.equal(y.Cdr);
            x = x.Cdr;
            if (!(y.Cdr instanceof Cell))
               return false;
            y = y.Cdr;
            if (x == a)
               return y == b;
            if (y == b)
               return false;
         }
      }

      final int compare(Any x) {
         if (x == this)
            return 0;
         if (x == T)
            return -1;
         if (!(x instanceof Cell))
            return +1;
         Any y = this;
         Any a = this;
         Any b = x;
         for (;;) {
            int n;
            if ((n = y.Car.compare(x.Car)) != 0)
               return n;
            if (!((y = y.Cdr) instanceof Cell))
               return y.compare(x.Cdr);
            if (!((x = x.Cdr) instanceof Cell))
               return x == T? -1 : +1;
            if (y == a && x == b)
               return 0;
         }
      }

      final long length() {
         long n = 1;
         Any x = this;
         while (x.Car == Quote) {
            if (x == x.Cdr)
               return -1;
            if (!((x = x.Cdr) instanceof Cell))
               return n;
            ++n;
         }
         Any y = x;
         while ((x = x.Cdr) instanceof Cell) {
            if (x == y)
               return -1;
            ++n;
         }
         return n;
      }

      final long size() {return size(this);}
      final long size(Any x) {
         long n;
         Any y;

         n = 1;
         while (x.Car == Quote) {
            if (x == x.Cdr  ||  !((x = x.Cdr) instanceof Cell))
               return n;
            ++n;
         }
         for (y = x;;) {
            if (x.Car instanceof Cell)
               n += size(x.Car);
            if (!((x = x.Cdr) instanceof Cell)  ||  x == y)
               break;
            ++n;
         }
         return n;
      }

      final InFrame rdOpen(Any ex) {
         try {
            int len = (int)length();
            String[] cmd = new String[len];
            Any x = this;
            for (int i = 0; i < len; ++i) {
               cmd[i] = x.Car.name();
               x = x.Cdr;
            }
            int pid = allocPid();
            return new InFrame(new PicoLispReader((Pids[pid] = Runtime.getRuntime().exec(cmd)).getInputStream(), allocFd(), null, 0), pid);
         }
         catch (IOException e) {
            err(ex, this, "Pipe read open error");
            return null;
         }
      }

      final OutFrame wrOpen(Any ex) {
         try {
            int len = (int)length();
            String[] cmd = new String[len];
            Any x = this;
            for (int i = 0; i < len; ++i) {
               cmd[i] = x.Car.name();
               x = x.Cdr;
            }
            int pid = allocPid();
            return new OutFrame(new PicoLispWriter((Pids[pid] = Runtime.getRuntime().exec(cmd)).getOutputStream(), allocFd()), pid);
         }
         catch (IOException e) {
            err(ex, this, "Pipe write open error");
            return null;
         }
      }

      final String name() {return Car.name() + Cdr.name();}

      final public String toString() {
         Any x, y;
         StringBuilder sb;
         if (Car == Quote  &&  this != Cdr)
            return "'" + Cdr.toString();
         x = this;
         sb = new StringBuilder();
         sb.append('(');
         if ((y = circ(x)) == null) {
            for (;;) {
               sb.append(x.Car.toString());
               if ((x = x.Cdr) == Nil)
                  break;
               if (!(x instanceof Cell)) {
                  sb.append(" . ");
                  sb.append(x.toString());
                  break;
               }
               sb.append(' ');
            }
         }
         else if (y == x) {
            do {
               sb.append(x.Car.toString());
               sb.append(' ');
            } while (y != (x = x.Cdr));
            sb.append('.');
         }
         else {
            do {
               sb.append(x.Car.toString());
               sb.append(' ');
            } while (y != (x = x.Cdr));
            sb.append(". (");
            do {
               sb.append(x.Car.toString());
               sb.append(' ');
            } while (y != (x = x.Cdr));
            sb.append(".)");
         }
         sb.append(')');
         return sb.toString();
      }
   }
}
