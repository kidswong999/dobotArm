Dobot
===================
![image](https://github.com/kidswong999/dobotArm/raw/master/doc/media/dobot_logo.png)
1.machine sketch
-----------------------------------------------
![image](https://github.com/kidswong999/dobotArm/raw/master/doc/media/defines1.png)
![image](https://github.com/kidswong999/dobotArm/raw/master/doc/media/defines2.png)
2.calculate
------------------------------------------------------------------
the computer catch the position of target such as (x,y), (maybe a3), and calculate the a1 and a2, draw module with them in the screen.

**input: the target position(x,y)**

**output: the 3 angles(a1,a2) **

	float A,B,C;              //temp variables
	float x,y;                //input
	float a1,a2;              //output
    A = -2 * x * L1;
    B = 2 * (y-L3)*L1;
    C = sq(L2) - sq(L1) - sq(x) - sq(y-L3);

    a1 = 2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C))
    A = 2 * (y-L3) * L2;
    B = 2 * x * L2;
    C = sq(L2) + sq(x) + sq(L3 - y) - sq(L1) ;

    a2 =2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C))

3 communication Protocol
---------------------------------------------------------------------
####1 Parameters setting
Baud rate:256000.
Parity:  None.
Stop: 1.
####2 begin communicate
PC send beginPackage{}, and communicate is beginned. 
later Lower machine send data requestPackage{} per 60ms.



		beginPackage
		{
			0xa5,0x00,0x00,0x11,0x11,
			0x22,0x22,0x33,0x33,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x5a
		}
		requestPackage
		{0xa5...0x5a}
		38byte
####3 move control
after resceived the requestPackage{}, PC send controlPackage{}.


c
		
		code
		

c++
		
		code
		

python
		
		code
		

java
		
		code
		

c#
		
		code
		


4 Arduino software
--------------------------------------------------------------------

5 schematic and PCB board
-------------------------------------------------------------------