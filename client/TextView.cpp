#include "TextView.h"

gboolean TextView::Expose(){
	if(context == NULL){
		context = new FontContext(GDK_DRAWABLE(text_area->window));
	}
	context->TestDraw();
}

gboolean TextView::Configure(){
	return TRUE;
}

gboolean TextView::ButtonPress(GdkEventButton *event){
	return TRUE;
}


TextView::TextView(){
	buffer = gtk_text_buffer_new(NULL);
	gtk_text_buffer_set_text (buffer, "testing, one two three testing", -1);
	text_area = gtk_text_view_new_with_buffer(buffer);
	gtk_widget_set_events (text_area, GDK_EXPOSURE_MASK |
			                  GDK_BUTTON_PRESS_MASK |
			                  GDK_BUTTON_RELEASE_MASK |
					  GDK_KEY_PRESS_MASK |
					  GDK_KEY_RELEASE_MASK);
	//text_area = gtk_drawing_area_new();
	//g_signal_connect(text_area, "configure-event", G_CALLBACK(ConfigureStatic), this);
        //g_signal_connect(text_area, "expose-event", G_CALLBACK(ExposeStatic), this);
	g_signal_connect (text_area, "key-press-event", G_CALLBACK(ButtonPressStatic), this);
	context = NULL;
	gtk_widget_show(text_area);
}
