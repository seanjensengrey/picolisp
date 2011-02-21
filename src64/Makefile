# 21feb11abu
# (c) Software Lab. Alexander Burger

.SILENT:

bin = ../bin
lib = ../lib

ifeq ($(shell uname), Linux)
	OS = Linux
	SYS = linux
	ARCH = x86-64
   CC = gcc
   AS = as
	LINK-FLAGS = -rdynamic -lc -lm -ldl
	DYNAMIC-LIB-FLAGS = -shared -export-dynamic
	STRIP = strip
else
ifeq ($(shell uname), SunOS)
	OS = SunOS
	SYS = sunOs
	ARCH = x86-64
   CC = gcc -m64
   AS = gas --64
	LINK-FLAGS = -lc -lm -ldl -lsocket -lnsl
	DYNAMIC-LIB-FLAGS = -shared -export-dynamic
	STRIP = strip
else
ifeq ($(shell uname), Darwin)
	OS = Darwin
	SYS = darwin
	ARCH = x86-64
   CC = gcc
   AS = as
	export MACOSX_DEPLOYMENT_TARGET=10.4
	LINK-FLAGS = -lc -lm -ldl
	DYNAMIC-LIB-FLAGS = -dynamiclib -undefined dynamic_lookup -export-dynamic
	STRIP = :
endif
endif
endif

baseFiles = version.l glob.l main.l sys/$(SYS).code.l \
	gc.l apply.l flow.l sym.l subr.l big.l io.l db.l net.l err.l

picolisp: $(bin)/picolisp $(lib)/ext $(lib)/ht

all: picolisp

$(bin)/picolisp: $(ARCH).$(SYS).base.o
	mkdir -p $(bin) $(lib)
	$(CC) -o $(bin)/picolisp $(LINK-FLAGS) $(ARCH).$(SYS).base.o
	$(STRIP) $(bin)/picolisp

$(lib)/ext: $(ARCH).$(SYS).ext.o
	$(CC) -o $(lib)/ext $(DYNAMIC-LIB-FLAGS) $(ARCH).$(SYS).ext.o
	$(STRIP) $(lib)/ext

$(lib)/ht: $(ARCH).$(SYS).ht.o
	$(CC) -o $(lib)/ht $(DYNAMIC-LIB-FLAGS) $(ARCH).$(SYS).ht.o
	$(STRIP) $(lib)/ht

.s.o:
	$(AS) -o $*.o $*.s

$(ARCH).$(SYS).base.s: $(baseFiles)
	./mkAsm $(ARCH) $(SYS) $(OS) base $(lib)/tags $(baseFiles)

$(ARCH).$(SYS).ext.s: ext.l
	./mkAsm $(ARCH) $(SYS) $(OS) ext "" -fpic ext.l

$(ARCH).$(SYS).ht.s: ht.l
	./mkAsm $(ARCH) $(SYS) $(OS) ht "" -fpic ht.l


# Clean up
clean:
	rm -f *.s *.o

# vi:noet:ts=4:sw=4
