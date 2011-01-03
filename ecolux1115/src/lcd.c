/*
 * lcd.c
 *
 *  Created on: Jan 3, 2011
 *      Author: chris
 */


//LCD Data Pins Connected to Port 1 Pins 1..7 + Port2 Pin0
//LCD Control Pins Connected to I/O Port2

// GP0.7  d7
// GP0.0  d0
// GP1.0  cs  IC1
// GP1.1  cs  IC2
// GP1.2  res
// GP1.3  RW
// GP1.4  DI
// GP1.5  E

// GP1.6
// GP1.7

//Port2 Pin 1 is R/W
//Port2 Pin 2 is RS   H=data L=instruction
//Port2 Pin 3 is E
//Port2 Pin 4 is CS2 = Left Side Contol
//Port2 Pin 5 is CS1 = Right Side Control
//Port2 Pin 6 is /Res

#define LCD_Data		P1OUT
#define SetData7                (P2OUT |= 0x01)
#define ClearData7              (P2OUT &= 0xFE)
#define	LCD_Control		P2OUT
#define	LCD_Data_Dir		P1DIR
#define	LCD_Control_Dir		P2DIR

//LCD Functions
void Init_LCD();
int String_To_LCD(char str[], int CurrentPage);
void Char_To_LCD(char Character);
void LCD_En();
int Get_Start_Address(char Character);
int Next_Page(int CurrentPage);
int Clear_LCD();
void Set_Y(unsigned char Yaddress);
void Reset_LCD();
int LCD_Side(int Side);
void Loading_Screen();


//Character Pixel Definition Array

const char Font6x7[] = {
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// (space)
 //    0x00, 0x00, 0x5F, 0x00, 0x00,// !
 //    0x00, 0x07, 0x00, 0x07, 0x00,// "
 //    0x14, 0x7F, 0x14, 0x7F, 0x14,// #
 //    0x24, 0x2A, 0x7F, 0x2A, 0x12,// $
     0x23, 0x13, 0x08, 0x64, 0x62, 0x00,// %
 //    0x36, 0x49, 0x55, 0x22, 0x50,// &
 //    0x00, 0x05, 0x03, 0x00, 0x00,// '
 //    0x00, 0x1C, 0x22, 0x41, 0x00,// (
 //    0x00, 0x41, 0x22, 0x1C, 0x00,// )
     0x08, 0x2A, 0x1C, 0x2A, 0x08, 0x00,// *
 //    0x08, 0x08, 0x3E, 0x08, 0x08,// +
 //    0x00, 0x50, 0x30, 0x00, 0x00,// ,
 //    0x08, 0x08, 0x08, 0x08, 0x08,// -
     0x00, 0x60, 0x60, 0x00, 0x00, 0x00,// .
 //    0x20, 0x10, 0x08, 0x04, 0x02,// /
     0x3E, 0x51, 0x49, 0x45, 0x3E, 0x00,// 0
     0x00, 0x42, 0x7F, 0x40, 0x00, 0x00,// 1
     0x42, 0x61, 0x51, 0x49, 0x46, 0x00,// 2
     0x21, 0x41, 0x45, 0x4B, 0x31, 0x00,// 3
     0x18, 0x14, 0x12, 0x7F, 0x10, 0x00,// 4
     0x27, 0x45, 0x45, 0x45, 0x39, 0x00,// 5
     0x3C, 0x4A, 0x49, 0x49, 0x30, 0x00,// 6
     0x01, 0x71, 0x09, 0x05, 0x03, 0x00,// 7
     0x36, 0x49, 0x49, 0x49, 0x36, 0x00,// 8
     0x06, 0x49, 0x49, 0x29, 0x1E, 0x00,// 9
     0x00, 0x36, 0x36, 0x00, 0x00, 0x00,// :
 //    0x00, 0x56, 0x36, 0x00, 0x00,// ;
 //    0x00, 0x08, 0x14, 0x22, 0x41,// <
 //    0x14, 0x14, 0x14, 0x14, 0x14,// =
 //    0x41, 0x22, 0x14, 0x08, 0x00,// >
     0x02, 0x01, 0x51, 0x09, 0x06, 0x00,// ?
 //    0x32, 0x49, 0x79, 0x41, 0x3E,// @
     0x7E, 0x11, 0x11, 0x11, 0x7E, 0x00,// A
     0x7F, 0x49, 0x49, 0x49, 0x36, 0x00,// B
     0x3E, 0x41, 0x41, 0x41, 0x22, 0x00,// C
     0x7F, 0x41, 0x41, 0x22, 0x1C, 0x00,// D
     0x7F, 0x49, 0x49, 0x49, 0x41, 0x00,// E
     0x7F, 0x09, 0x09, 0x01, 0x01, 0x00,// F
     0x3E, 0x41, 0x41, 0x51, 0x32, 0x00,// G
     0x7F, 0x08, 0x08, 0x08, 0x7F, 0x00,// H
     0x00, 0x41, 0x7F, 0x41, 0x00, 0x00,// I
     0x20, 0x40, 0x41, 0x3F, 0x01, 0x00,// J
     0x7F, 0x08, 0x14, 0x22, 0x41, 0x00,// K
     0x7F, 0x40, 0x40, 0x40, 0x40, 0x00,// L
     0x7F, 0x02, 0x04, 0x02, 0x7F, 0x00,// M
     0x7F, 0x04, 0x08, 0x10, 0x7F, 0x00,// N
     0x3E, 0x41, 0x41, 0x41, 0x3E, 0x00,// O
     0x7F, 0x09, 0x09, 0x09, 0x06, 0x00,// P
     0x3E, 0x41, 0x51, 0x21, 0x5E, 0x00,// Q
     0x7F, 0x09, 0x19, 0x29, 0x46, 0x00,// R
     0x46, 0x49, 0x49, 0x49, 0x31, 0x00,// S
     0x01, 0x01, 0x7F, 0x01, 0x01, 0x00,// T
     0x3F, 0x40, 0x40, 0x40, 0x3F, 0x00,// U
     0x1F, 0x20, 0x40, 0x20, 0x1F, 0x00,// V
     0x7F, 0x20, 0x18, 0x20, 0x7F, 0x00,// W
     0x63, 0x14, 0x08, 0x14, 0x63, 0x00,// X
     0x03, 0x04, 0x78, 0x04, 0x03, 0x00,// Y
     0x61, 0x51, 0x49, 0x45, 0x43, 0x00// Z
 /*    0x00, 0x00, 0x7F, 0x41, 0x41,// [
     0x02, 0x04, 0x08, 0x10, 0x20,// "\"
     0x41, 0x41, 0x7F, 0x00, 0x00,// ]
     0x04, 0x02, 0x01, 0x02, 0x04,// ^
     0x40, 0x40, 0x40, 0x40, 0x40,// _
     0x00, 0x01, 0x02, 0x04, 0x00,// `
     0x20, 0x54, 0x54, 0x54, 0x78,// a
     0x7F, 0x48, 0x44, 0x44, 0x38,// b
     0x38, 0x44, 0x44, 0x44, 0x20,// c
     0x38, 0x44, 0x44, 0x48, 0x7F,// d
     0x38, 0x54, 0x54, 0x54, 0x18,// e
     0x08, 0x7E, 0x09, 0x01, 0x02,// f
     0x08, 0x14, 0x54, 0x54, 0x3C,// g
     0x7F, 0x08, 0x04, 0x04, 0x78,// h
     0x00, 0x44, 0x7D, 0x40, 0x00,// i
     0x20, 0x40, 0x44, 0x3D, 0x00,// j
     0x00, 0x7F, 0x10, 0x28, 0x44,// k
     0x00, 0x41, 0x7F, 0x40, 0x00,// l
     0x7C, 0x04, 0x18, 0x04, 0x78,// m
     0x7C, 0x08, 0x04, 0x04, 0x78,// n
     0x38, 0x44, 0x44, 0x44, 0x38,// o
     0x7C, 0x14, 0x14, 0x14, 0x08,// p
     0x08, 0x14, 0x14, 0x18, 0x7C,// q
     0x7C, 0x08, 0x04, 0x04, 0x08,// r
     0x48, 0x54, 0x54, 0x54, 0x20,// s
     0x04, 0x3F, 0x44, 0x40, 0x20,// t
     0x3C, 0x40, 0x40, 0x20, 0x7C,// u
     0x1C, 0x20, 0x40, 0x20, 0x1C,// v
     0x3C, 0x40, 0x30, 0x40, 0x3C,// w
     0x44, 0x28, 0x10, 0x28, 0x44,// x
     0x0C, 0x50, 0x50, 0x50, 0x3C,// y
     0x44, 0x64, 0x54, 0x4C, 0x44,// z
     0x00, 0x08, 0x36, 0x41, 0x00,// {
     0x00, 0x00, 0x7F, 0x00, 0x00,// |
     0x00, 0x41, 0x36, 0x08, 0x00,// }
     0x08, 0x08, 0x2A, 0x1C, 0x08,// ->
     0x08, 0x1C, 0x2A, 0x08, 0x08 // <-*/
 };

int CurrentPage = 0; //Variable to hold the current page of the X Add of the LCD.




  CurrentPage = Clear_LCD();
  CurrentPage = LCD_Side(0);
  Set_Y(0x03);
  CurrentPage = String_To_LCD("DRINK SIZE", CurrentPage);
  Set_Y(0x03);
  CurrentPage = String_To_LCD("  8 OUNCES", CurrentPage);
  CurrentPage = LCD_Side(1);
  CurrentPage = String_To_LCD(" MUST BE", CurrentPage);
  CurrentPage = String_To_LCD(" OR LESS", CurrentPage);
  LongDelay(5);



/*******************************************************************
Initialize LCD Function.  Turns on display, sets current display
Address to 0,0.
*******************************************************************/
void LCD_En()
{
        LCD_Control |= 0x08;  //Set E High
        QuickDelay();
        LCD_Control &= 0xF7;   //Set E Low
        QuickDelay();
}
void Init_LCD()
{
	LCD_Data_Dir |= 0xFF;   	//Set Port1 pins for Outputs.
	LCD_Control_Dir |= 0x7F;	//Set Port2 pins 6..0 for Outputs.

	QuickDelay();		//Power-Up Delay

	//Turn on Display

	LCD_Data = 0x7E;      //LCD Data = 00111111
    ClearData7;
	LCD_Control &= 0xF9;  //RS = 0, R/W = 0
    LCD_En();             //Clock E


	//Set Y address to 0

	LCD_Data = 0x80;	//AC5..0 = 000000, this is the Y address, lower 6 bits.
    ClearData7;          //LCD Data = 01000000
	LCD_Control &= 0xF9;	//RS = 0, R/W = 0
	LCD_En();               //Clock E


	//Set Page (X Address) to 0

	LCD_Data = 0x70;	//AC2..0 = 000, this is the Page, lower 3 bits.
        SetData7;          //LCD Data = 10111000
	LCD_Control &= 0xF9;	//RS = 0, R/W = 0.
	LCD_En();               //Clock E


        //Set Display start line to 0

        LCD_Data = 0x80;      //LCD Data = 11000000
        SetData7;
        LCD_Control &= 0xF9;
        LCD_En();


	return;
}

/**********************************************************************************
Clears the LCD by writing spaces to every line.  Returns 0 to be sent to the variable
holding the current page.
*************************************************************************************/

int Clear_LCD(void)
{
  int Dummy = 0;
  Set_Y(0x00);
  for(int i = 0; i <= 7;i++)             //Write spaces to LCD line by line
  {
    Dummy = String_To_LCD("           ", Dummy);
  }

  //For some reason not clearing the Top Line so manually clearing.

  Dummy = Next_Page(7);
  Dummy = String_To_LCD("           ", Dummy);
  Dummy = Next_Page(7);

  return 0;                             //After clearing, return the currentpage, which is set to 0
}

/*************************************************************************************
Set_Y sets the Yaddress to the character value passed in as a parameter.  Ex. Line 32 = 0x20
**************************************************************************************/

void Set_Y(unsigned char Yaddress)
{
        Yaddress <<= 1;    //Shift Y address Left by 1
        LCD_Data = 0x80 | (0x7E & Yaddress);	//AC5..0 pins6..1 of Data specify Yaddress.  So for example
                                                //Address 32 = 00100000, left shifter to 01000000
                                                 //0x7E AND 0x40 = 0x40 OR 0x80 plus data7 low is 01100000
        ClearData7;  //Data bit 7 low
	LCD_Control &= 0xF9;	//RS = 0, R/W = 0
	LCD_En();
        return;
}

/***************************************************************************************
Resets the LCD
***************************************************************************************/

void Reset_LCD()
{
  QuickDelay();
  LCD_Control &= 0xBF; //Reset Pin set LOW
  QuickDelay();
  LCD_Control |= 0x40; //Reset Pin set HIGH
  //LCD_En();
  return;
}

/***************************************************************************************
Selects the side of the LCD Desired.  If side = 0 picks left side.  If side = 1 picks
right side.  Else both are selected.
***************************************************************************************/

int LCD_Side(int Side)
{
  QuickDelay();

  switch(Side)
  {
  case 0:
    LCD_Control &= 0xDF;     //CS2 = Pin 4 set high, CS1 = Pin 5 set low
    LCD_Control |= 0x10;
    break;
  case 1:
    LCD_Control &= 0xEF;     //CS2 = Pin 4 set low, CS1 = Pin 5 set high
    LCD_Control |= 0x20;
    break;
  case 2:
  default:
    LCD_Control |= 0x30;    //CS2 = Pin 3 set high, CS1 = Pin 4 set sigh
    break;
  }
  QuickDelay();
  return 0;
}

void Char_To_LCD(char Character)
{
  int Address = Get_Start_Address(Character);
  for(int i = 0; i < 6; i++)
  {
    LCD_Control &= 0xFD;	//R/W = 0
    LCD_Control |= 0x04;	//RS = 1
    LCD_Data = (Font6x7[Address+i] << 1);
    if((Font6x7[Address+i] & 0x80) == 0x00) //If the bit is a 0...
      ClearData7;          //The most sig bit of Data is 0
    else
      SetData7;
    LCD_En();
  }
  return;
}
/******************************************************************
 *String to LCD function.  Writes a passed in string to the LCD.
 * Stops when a NULL character is reached.  Increments Page after each
 * string.
 * ***************************************************************/


int String_To_LCD(char str[], int CurrentPage)
{
	//Creater pointer to the beginning of the string.  This can be moved inside the for loop.

	char *pStr = &str[0];
	int Address;

	//Print character at current address to the LCD unless it is the NULL Character.

	for(;*pStr != NULL;*pStr++)
	{
          for(int i = 0; i < 6; i++)
          {
                Address = Get_Start_Address(*pStr);
		LCD_Control &= 0xFD;	//R/W = 0
		LCD_Control |= 0x04;	//RS = 1
		LCD_Data = (Font6x7[Address+i] << 1);
                if((Font6x7[Address+i] & 0x80) == 0x00) //If the bit is a 0...
                   ClearData7;          //The most sig bit of Data is 0
                else
                   SetData7;
		LCD_En();
          }
	}
        CurrentPage = Next_Page(CurrentPage);
	return CurrentPage;
}

/****************************************************************************
 * Next Page function increments the Page Value of the LCD, aka the X Address
 * Resets X address to zero if it is currently 7.
 * *************************************************************************/

int Next_Page(int CurrentPage)
{
	LCD_Control &= 0xF9;	//R/W = 0, RS = 0

	switch(CurrentPage)
	{
		case 0:
			LCD_Data = 0x72;  //Data = 10111001
                        SetData7;
                        LCD_En();
			break;

		case 1:
			LCD_Data = 0x74;  //Data = 10110100
                        SetData7;
                        LCD_En();
			break;

		case 2:
			LCD_Data = 0x76;  //Data = 10111011
                        SetData7;
                        LCD_En();
			break;

		case 3:
			LCD_Data = 0x78;  //Data = 10111100
                        SetData7;
                        LCD_En();
			break;

		case 4:
			LCD_Data = 0x7A;  //Data = 10111101
                        SetData7;
                        LCD_En();
			break;

		case 5:
			LCD_Data = 0x7C;  //Data = 10111110
                        SetData7;
                        LCD_En();
			break;

		case 6:
			LCD_Data = 0x7E;  //Data = 10111111
                        SetData7;
                        LCD_En();
			break;

		case 7:
			LCD_Data = 0x70;  //Data = 10111000
                        SetData7;
                        LCD_En();
			break;

		default:
			return 0; //This should never happen.
	}
	if(CurrentPage < 7)
		CurrentPage++;
	else
		CurrentPage = 0;

        //Set Y address to 0

	Set_Y(0x00);

	return  CurrentPage;
}


/*************************************************************************************
Gets the start address based on a character width of 6.
**************************************************************************************/

int Get_Start_Address(char Character)
{
      int Address;

      switch(Character)
      {
      case ' ':
                Address = 0;
                break;

      case NULL:
                Address = 0;
                break;

      case '%':
                Address = 6;
                break;
      case '*':
                Address = 12;
                break;

      case '.':
                Address = 18;
                break;

      case '0':
                Address = 24;
                break;

      case '1':
                Address = 30;
                break;

      case '2':
                Address = 36;
                break;

      case '3':
                Address = 42;
                break;

      case '4':
                Address = 48;
                break;

      case '5':
                Address = 54;
                break;

      case '6':
                Address = 60;
                break;


      case '7':
                Address = 66;
                break;
      case '8':
                Address = 72;
                break;
      case '9':
                Address = 78;
                break;
      case ':':
                Address = 84;
                break;

      case '?':
                Address = 90;
                break;

      case 'A':
                Address = 96;
                break;

      case 'B':
                Address = 102;
                break;

      case 'C':
                Address = 108;
                break;

      case 'D':
                Address = 114;
                break;

      case 'E':
                Address = 120;
                break;

      case 'F':
                Address = 126;
                break;

      case 'G':
                Address = 132;
                break;


      case 'H':
                Address = 138;
                break;

      case 'I':
                Address = 144;
                break;
      case 'J':
                Address = 150;
                break;

      case 'K':
                Address = 156;
                break;

      case 'L':
                Address = 162;
                break;

      case 'M':
                Address = 168;
                break;

      case 'N':
                Address = 174;
                break;

      case 'O':
                Address = 180;
                break;

      case 'P':
                Address = 186;
                break;

      case 'Q':
                Address = 192;
                break;

      case 'R':
                Address = 198;
                break;


      case 'S':
                Address = 204;
                break;

      case 'T':
                Address = 210;
                break;
      case 'U':
                Address = 216;
                break;

      case 'V':
                Address = 222;
                break;

      case 'W':
                Address = 228;
                break;

      case 'X':
                Address = 234;
                break;
      case 'Y':
                Address = 240;
                break;

      case 'Z':
                Address = 246;
                break;


      default:
                break;
      }
      return Address;
}

