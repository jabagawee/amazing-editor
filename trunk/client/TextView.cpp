#include "TextView.h"

gboolean TextView::Expose(){
	context = new FontContext(GDK_DRAWABLE(text_area->window));
	context->TestDraw();
}
gboolean TextView::Configure(){

	return TRUE;
}



TextView::TextView(){
	text_area = gtk_drawing_area_new();
	gtk_widget_set_events (text_area, GDK_EXPOSURE_MASK | GDK_BUTTON_PRESS_MASK |
			GDK_BUTTON_RELEASE_MASK | GDK_KEY_PRESS_MASK | GDK_BUTTON_MOTION_MASK);
	g_signal_connect(text_area, "configure-event", G_CALLBACK(ConfigureStatic), this);
        g_signal_connect(text_area, "expose-event", G_CALLBACK(ExposeStatic), this);
	gtk_widget_show(text_area);
}



