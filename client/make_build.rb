#! /usr/bin/env ruby

$LIBS = ["-lm"]
$IFlags = []
$CFLAGS = []
$LIBS.push(/mingw/ =~ PLATFORM ? "-lopengl32" : "-lc")
def pkg_config(package)
	$LIBS.push(*`pkg-config --libs #{package}`.split)
	$IFlags.push(*`pkg-config --cflags-only-I #{package}`.split)
	$CFLAGS.push(*`pkg-config --cflags-only-other #{package}`.split)
	true
end



$src_files=Dir.glob("*.c") + Dir.glob("*.cpp") 
$src_files += Dir.glob("*/*.c") + Dir.glob("*/*.cpp") 
$obj_files=$src_files.collect { |file|
	file.gsub(/\.\w*$/,"") + ".o"
}

["gtk+-2.0","gdk-2.0"].each do |package_id|
        pkg_config(package_id) or exit 1
end



$TARGET = "editor"
$SharedTarget = false
$StaticTarget = false
$ExeTarget = true


File.open("Makefile","w+") do |file|
file.puts <<END

OBJS= #{$obj_files.join(" ")}

CC=gcc
CPP=g++
LD=g++
LDSHARED=$(LD) -shared
TARGET=#{$TARGET}
#{$SharedTarget ? "SHARED_TARGET=$(TARGET).so" : ""}
#{$StaticTarget ? "STATIC_TARGET=$(TARGET).a" : ""}
INCFLAGS=#{$IFlags.uniq.join(" ")}
LIBS= #{$LIBS.uniq.join(" ")}
CPPFLAGS=
CFLAGS+=--std=gnu99
CFLAGS+=#{$CFLAGS.uniq.join(" ")}
CPPFLAGS+=#{$CFLAGS.uniq.join(" ")}


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



END
end
