LDFLAGS = -ldl

ifndef BLDDIR
BLDDIR = /build
endif
$(info $$buld dir is [${BLDDIR}])

ifndef SRCDIR
SRCDIR = /src/launchers/linux
endif

INCDIR = $(SRCDIR)/../../util
OBJDIR = $(BLDDIR)/bin
CFLAGS = -c -g -Wall -I$(INCDIR)
SRC = $(wildcard $(SRCDIR)/*.c)
OBJ = $(patsubst $(SRCDIR)/%.c, $(OBJDIR)/%.o, $(SRC))
EXE = $(OBJDIR)/Launcher

all: clean $(EXE) 
    
$(EXE): $(OBJ) 
	$(CC) $(OBJDIR)/*.o -o $@ $(LDFLAGS)

$(OBJDIR)/%.o : $(SRCDIR)/%.c
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) $< -o $@ 

clean:
	-rm -f $(OBJDIR)/*.o $(EXE)