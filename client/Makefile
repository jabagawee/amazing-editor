
OBJS= Main.o MainWindow.o TextView.o

CC=gcc
CPP=g++
LD=g++
LDSHARED=$(LD) -shared
TARGET=editor


INCFLAGS=-I/usr/include/atk-1.0 -I/usr/include/pango-1.0 -I/usr/include/gio-unix-2.0/ -I/usr/include/glib-2.0 -I/usr/lib/x86_64-linux-gnu/glib-2.0/include -I/usr/include/freetype2 -I/usr/include/libpng12 -I/usr/include/gtk-2.0 -I/usr/lib/gtk-2.0/include -I/usr/include/cairo -I/usr/include/gdk-pixbuf-2.0 -I/usr/include/pixman-1
LIBS= -lm -lc -pthread -L/usr/lib/x86_64-linux-gnu -lgtk-x11-2.0 -lgdk-x11-2.0 -latk-1.0 -lgio-2.0 -lpangoft2-1.0 -lpangocairo-1.0 -lgdk_pixbuf-2.0 -lcairo -lpango-1.0 -lfreetype -lfontconfig -lgobject-2.0 -lgmodule-2.0 -lgthread-2.0 -lrt -lglib-2.0
CPPFLAGS=
CFLAGS+=--std=gnu99
CFLAGS+=-pthread
CPPFLAGS+=-pthread


all: $(SHARED_TARGET) $(TARGET) # $(STATIC_TARGET)

.cpp.o:
	@echo $(CPP) -c -fpic $<
	@$(CPP) $(CPPFLAGS) -c $< $(INCFLAGS) -o $@ 

.c.o:
	@echo $(CC) -c -fpic $<
	@$(CC) $(CFLAGS) -c $< $(INCFLAGS) -o $@ 

# $(SHARED_TARGET) $(STATIC_TARGET): $(OBJS) Makefile
# $(SHARED_TARGET):
#	@echo $(LDSHARED) -o $@ $(OBJS)
#	@$(LDSHARED) -o $@ $(OBJS) $(LIBPATH) $(DLDFLAGS) $(LIBS)
$(TARGET): $(OBJS) Makefile
	@echo $(LD) -o $@ $(OBJS)
	@$(LD) -o $@ $(OBJS) $(LIBPATH) $(DLDFLAGS) $(LIBS)

Makefile: make_build.rb
	ruby make_build.rb



