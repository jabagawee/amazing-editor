#ifndef __TEXT_VIEW_H_
#define __TEXT_VIEW_H_
#include <gtk/gtk.h>

class TextView{
	public:
		TextView();
	private:
		GtkWidget *text_area;
};

#endif
