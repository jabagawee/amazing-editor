#ifndef _SOCKET_H_
#define _SOCKET_H_

#include <gtk/gtk.h>

class RevisionSoc{
	public:
		void Connect(char *address,int port);
		RevisionSoc();
		inline static gboolean StaticIO_IN(GtkWidget *widget,GdkEvent *event,gpointer self){
			return ((RevisionSoc *)self)->IO_IN();
		}
		gboolean IO_IN();
		GIOChannel *channel;
	private:
		int _socket;

};

#endif
