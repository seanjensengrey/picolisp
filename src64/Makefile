# 27oct12abu
# (c) Software Lab. Alexander Burger

.SILENT:

bin = ../bin
lib = ../lib

ifeq ($(MAKECMDGOALS), x86-64.linux)
	UNAME = Linux
	MACHINE = x86_64
else
	ifeq ($(MAKECMDGOALS), x86-64.sunOs)
		UNAME = SunOS
		MACHINE = x86_64
	else
		ifeq ($(MAKECMDGOALS), ppc64.linux)
			UNAME = Linux
			MACHINE = ppc64
		else
			UNAME = $(shell uname)
			ifeq ($(MAKECMDGOALS), emu)
				MACHINE = emu
			else
				MACHINE = $(shell uname -m)
			endif
		endif
	endif
endif

SYS =
FMT = .c
ARCH = emu
STRIP = :

ifeq ($(UNAME), Linux)
	OS = Linux
	ifeq ($(MACHINE), x86_64)
		SYS = .linux
		FMT = .s
		ARCH = x86-64
		AS = as
	else
		ifeq ($(MACHINE), ppc64)
			SYS = .linux
			FMT = .s
			ARCH = ppc64
			AS = as -mppc64 -a64
		endif
	endif
	LD-MAIN = -Wl,--no-as-needed -rdynamic -lc -lm -ldl
	LD-SHARED = -shared -export-dynamic
	STRIP = strip
else
	ifeq ($(UNAME), SunOS)
		OS = SunOS
		SYS = .sunOs
		FMT = .s
		ARCH = x86-64
		AS = gas --64
		LD-MAIN = -m64 -lc -lm -ldl -lsocket -lnsl
		LD-SHARED = -m64 -shared
		STRIP = strip
	else
		ifeq ($(UNAME), Darwin)
			OS = Darwin
			LD-MAIN = -lc -lm -ldl
			LD-SHARED = -dynamiclib -undefined dynamic_lookup
			STRIP = strip
		endif
	endif
endif

baseFiles = version.l glob.l main.l \
	gc.l apply.l flow.l sym.l subr.l big.l io.l db.l net.l err.l

sFiles = \
	$(ARCH)$(SYS).base$(FMT) \
	$(ARCH)$(SYS).ext$(FMT) \
	$(ARCH)$(SYS).ht$(FMT)

all: picolisp

x86-64.linux: $(sFiles)
x86-64.sunOs: $(sFiles)
ppc64.linux: $(sFiles)
emu: picolisp

picolisp: $(sFiles) $(bin)/picolisp $(lib)/ext $(lib)/ht

$(bin)/picolisp: $(ARCH)$(SYS).base.o
	$(CC) -o $(bin)/picolisp $(ARCH)$(SYS).base.o $(LD-MAIN)
	$(STRIP) $(bin)/picolisp

$(lib)/ext: $(ARCH)$(SYS).ext.o
	$(CC) -o $(lib)/ext $(ARCH)$(SYS).ext.o $(LD-SHARED)
	$(STRIP) $(lib)/ext

$(lib)/ht: $(ARCH)$(SYS).ht.o
	$(CC) -o $(lib)/ht $(ARCH)$(SYS).ht.o $(LD-SHARED)
	$(STRIP) $(lib)/ht

# Explicit builds for cross-assembly
$(ARCH)$(SYS).base$(FMT): sysdefs arch/$(ARCH).l $(baseFiles) sys/$(ARCH)$(SYS).code.l
	./mkAsm $(ARCH) "$(SYS)" $(FMT) $(OS) base "" $(lib)/map  $(baseFiles) sys/$(ARCH)$(SYS).code.l

$(ARCH)$(SYS).ext$(FMT): sysdefs arch/$(ARCH).l ext.l $(ARCH)$(SYS).base$(FMT)
	./mkAsm $(ARCH) "$(SYS)" $(FMT) $(OS) ext T ""  ext.l

$(ARCH)$(SYS).ht$(FMT): sysdefs arch/$(ARCH).l ht.l $(ARCH)$(SYS).base$(FMT)
	./mkAsm $(ARCH) "$(SYS)" $(FMT) $(OS) ht T ""  ht.l

sysdefs: sysdefs.c
	$(CC) -o sysdefs -D_FILE_OFFSET_BITS=64 sysdefs.c
	$(STRIP) sysdefs

emu.base.o:
	$(CC) -c -O -fomit-frame-pointer -D_FILE_OFFSET_BITS=64 $*.c

emu.ext.o  emu.ht.o:
	$(CC) -c -O -fpic -fomit-frame-pointer -D_FILE_OFFSET_BITS=64 $*.c

.s.o:
	$(AS) -o $*.o $*.s

# Clean up
clean:
	rm -f emu.*.c *.s *.o *.symtab sysdefs

# vi:noet:ts=4:sw=4
