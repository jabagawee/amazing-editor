#include "MainWindow.h"
#include "RevControl/Panel.h"

#include <stdio.h>
int main(int argc,char **argv){
	gtk_init(&argc,&argv);
	MainWindow window;
	//RevCtrlPanel panel;
	gtk_main();
	return 0;
}

