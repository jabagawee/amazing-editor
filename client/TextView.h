#ifndef __TEXT_VIEW_H_
#define __TEXT_VIEW_H_
#include <gtk/gtk.h>
#include "XTermFont.h"

class TextView{
	public:
		TextView();
		GtkWidget *text_area;
	private:
#define STATIC_FORWARD(name) \
		inline static gboolean name ## Static(GtkWidget *widget, GdkEvent *event, gpointer self){return ((TextView *)self)->name();} \
		gboolean name();
#define STATIC_FORWARD_EVENT(name, type) \
		inline static gboolean name ## Static(GtkWidget *widget, type *event, gpointer self){return ((TextView *)self)->name(event);} \
		gboolean name(type *event);
		STATIC_FORWARD_EVENT(Scroll, GdkEventScroll);
                STATIC_FORWARD_EVENT(ButtonPress, GdkEventButton);
                STATIC_FORWARD_EVENT(ButtonRelease, GdkEventButton);
                STATIC_FORWARD_EVENT(Motion, GdkEventMotion);
		STATIC_FORWARD(Expose);
                STATIC_FORWARD(InitGlFrame);
                STATIC_FORWARD(Configure);
		FontContext *context;

};

#endif
