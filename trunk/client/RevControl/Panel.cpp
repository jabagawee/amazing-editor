#include "Panel.h"

inline static gboolean delete_event( GtkWidget *widget, GdkEvent  *event, gpointer   data ) {
	//    	g_print ("delete event occurred\n");
    	return TRUE;
}
inline static void destroy( GtkWidget *widget, gpointer   data ) {
    	gtk_main_quit ();
}
RevCtrlPanel::RevCtrlPanel(){
	window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
	g_signal_connect (window, "delete-event", G_CALLBACK (delete_event), NULL);
        g_signal_connect (window, "destroy", G_CALLBACK (destroy), NULL);
	vbox = gtk_vbox_new(FALSE,0);
    	gtk_widget_show (vbox);	
   	gtk_container_add (GTK_CONTAINER (window), vbox);

	GtkWidget *menu_bar = gtk_menu_bar_new();
    	gtk_widget_show (menu_bar);

    	GtkWidget *file_item = gtk_menu_item_new_with_label ("File");
    	gtk_widget_show (file_item);
	gtk_menu_bar_append (GTK_MENU_BAR (menu_bar), file_item);

	GtkWidget *file_menu = gtk_menu_new();
	gtk_menu_item_set_submenu (GTK_MENU_ITEM (file_item), file_menu);


	gtk_box_pack_start(GTK_BOX(vbox), menu_bar, FALSE, FALSE, 0);


	gtk_widget_set_size_request(window,200,100);
    	gtk_widget_show (window);
	sock = new RevisionSoc();
	sock->Connect((char *)"127.0.0.1",1200);
	

}




