#ifndef _GTK_CAD_MAIN_WINDOW_H_
#define _GTK_CAD_MAIN_WINDOW_H_
#include <gtk/gtk.h>

class MainWindow{
public:
	MainWindow();
private:
	GtkWidget *window;
	GtkWidget *vbox;
};


#endif
