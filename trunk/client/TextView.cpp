#include "TextView.h"
#include <gtk/gtk.h>

TextView::TextView(){
	text_area = gtk_drawing_area_new();
	gtk_widget_set_events (text_area, GDK_EXPOSURE_MASK | GDK_BUTTON_PRESS_MASK |
			GDK_BUTTON_RELEASE_MASK | GDK_KEY_PRESS_MASK | GDK_BUTTON_MOTION_MASK);

	gtk_widget_show(text_area);


}



