#ifndef _REV_CTRL_PANEL_H_
#define _REV_CTRL_PANEL_H_
#include <gtk/gtk.h>
#include "Socket.h"
class RevCtrlPanel{
	public:
		RevCtrlPanel();
	private:
		GtkWidget *vbox;
		GtkWidget *window;
		RevisionSoc *sock;
};

#endif
