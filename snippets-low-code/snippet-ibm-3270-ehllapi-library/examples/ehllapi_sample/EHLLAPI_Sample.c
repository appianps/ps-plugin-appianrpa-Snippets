//Include standard library functions
#include <stdlib.h>
#include <stdio.h>
#include <windows.h>
//EHLLAPI header file
#include "hapi_c32.h"

int main(char **argv, int argc) 
{
	int HFunc, HLen, HRc;
	char HBuff[1];
	struct HLDConnectPS ConnBuff;
	// Send Key string for HOME+string+ENTER:
	char SendString[] = "@0Hello World!@E";

	//Reset the automation system
	HFunc = HA_RESET_SYSTEM;
	HLen = 0;
	HRc = 0;
	hllapi(&HFunc, HBuff, &HLen, &HRc);	//standard EHLLAPI Calling format
	if (HRc != HARC_SUCCESS) 
	{
		printf("Unable to access EHLLAPI.\n");
		return 1;
	}

	//Initialize the automation Engine by establishing connection
	HFunc = HA_CONNECT_PS;
	HLen = sizeof(ConnBuff);
	HRc = 0;
	memset(&ConnBuff, 0x00, sizeof(ConnBuff));

	ConnBuff.stps_shortname = 'A';
	hllapi(&HFunc, (char *)&ConnBuff, &HLen, &HRc);
	switch (HRc) {
		case HARC_SUCCESS:
		case HARC_BUSY:
		case HARC_LOCKED:
			//If here, we are good with connection
			break;
		case HARC_INVALID_PS:
			printf("Host session A does not exist.\n");
			return 1;
		case HARC_UNAVAILABLE:
			printf("Host session A is in use by another EHLLAPI application.\n");
			return 1;
		case HARC_SYSTEM_ERROR:
			printf("System error connecting to session A.\n");
			return 1;
		default:
			printf("Error connecting to session A.\n");
			return 1;
	}
	
	//Send the Keystroke as recorded in SendString Buffer
	HFunc = HA_SENDKEY;
	HLen = strlen(SendString);
	HRc = 0;
	hllapi(&HFunc, SendString, &HLen, &HRc);
	switch (HRc) {
		case HARC_SUCCESS:
			break;
		case HARC_BUSY:
		case HARC_LOCKED:
			printf("Send failed, host session locked or busy.\n");
			break;
		default:
			printf("Send failed.\n");
			break;
	}
	
	//All operations Done. Now disconnect
	HFunc = HA_DISCONNECT_PS;
	HLen = 0;
	HRc = 0;
	hllapi(&HFunc, HBuff, &HLen, &HRc);
	printf("EHLLAPI program ended.\n");
	
	return 0;
}