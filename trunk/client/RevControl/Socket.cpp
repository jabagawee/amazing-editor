#include "Socket.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

RevisionSoc::RevisionSoc(){

}
gboolean RevisionSoc::IO_IN(){
	char data[1024];
	int length = 0;
	length = recv(_socket,data,1024,O_NONBLOCK);
	if(length > 0){
		printf("hello %d:\"",length);
		int i;
		for(i = 0; i < length;i++){
			if(data[i] == '\n'){
				printf("\\n");
			}else{
				printf("%c",data[i]);
			}
		}
		printf("\"\n");
	}
}
void RevisionSoc::Connect(char *address,int port){
	_socket = socket(AF_INET, SOCK_STREAM, 0);
	struct sockaddr_in addr;
	struct hostent *server;
	bzero((char *)&addr, sizeof(addr));
	server = gethostbyname(address);
    	if (server == NULL) {
        	fprintf(stderr,"ERROR, no such host\n");
        	exit(0);
    	}
	addr.sin_family = AF_INET;
	addr.sin_port = htons(port);
	bcopy((char *)server->h_addr, 
         		(char *)&addr.sin_addr.s_addr,
         		server->h_length);
	if(connect(_socket, (struct sockaddr *)&addr, sizeof(addr)) < 0){
        	fprintf(stderr,"ERROR, could not connect\n");
        	exit(0);
	}
	channel = g_io_channel_unix_new(_socket);
	g_io_add_watch(channel,G_IO_IN,(GIOFunc)StaticIO_IN, this);
}

