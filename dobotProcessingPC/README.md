dobot client processing PC
=========================================================

1.defines
---------------------------------------------------------

![image](https://github.com/kidswong999/dobotArm/raw/master/doc/media/defines1.png)

![image](https://github.com/kidswong999/dobotArm/raw/master/doc/media/defines2.png)

2.flow diagram
-----------------------------------------------------------

![image](https://github.com/kidswong999/dobotArm/raw/master/doc/media/coorFlowChart.png)

3.calculate
------------------------------------------------------------------
the computer catch the position of target such as (x,y), (maybe a3), and calculate the a1 and a2, draw module with them in the screen.

**input: the target position(x,y)**

**output: the 3 angles(a1,a2)**

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


