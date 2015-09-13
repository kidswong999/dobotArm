import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class dobotProcessingPC extends PApplet {


Serial myPort;
/////////////////////////////////////////////////////////////////////
///////////////some dobot data///////////////////////////////////////
float L1 = 160;                          //the small arm's length;
float L2 = 135;                          //the big arm's length;
float L3 = 80;                            //the base's height;

float MAXRADIUS = 280;                   //the dobot cannot fully stretch, the value is not accurate,it need to be changed;
float MINA1 = -15*PI/360;
float MAXA1 = PI/2;
float MINA2 = 0;
float MAXA2 = PI/2;

/////////////////////////////////////////////////////////////////////
//////////////////////////some UI data///////////////////////////////
float frontScaleRatio = 1.5f;             //enlarge to fit screen
float topScaleRatio = 0.5f;                   

float frontOriginTrainlateX = 0.2f;
float frontOriginTrainlateY = 0.25f;

int border = 10;                         //the UI ...er a border width
int frontQuadWidth = 800;
int topRadius = 185;

int background = color(230, 245, 245);
PFont dobotWord, coorWord;
//////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////

Dobot2DModule myDobotModule = new Dobot2DModule();

Coordinate2D catched = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);
Coordinate2D target = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);
Coordinate2D display = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);
Coordinate2D real = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);


///////////////////////////some program flags////////////////////////
boolean isSended = true;                    //flag to judge whether the data sended
boolean serialEn = false;

/////////////////////////////////////////////////////////////////////
public void setup()
{
  dobotSerialBegin();//set bite rate to 256000 and begin communitation
  
  
  dobotWord = loadFont("STHupo-24.vlw");
  coorWord = loadFont("NI7SEG-48.vlw");
}

public void draw()
{
  if (serialEn && !isSended)
  {
    float sendX = -(target.x*cos(target.A3)-real.x*cos(real.A3));//the machine's axis is not same as the module, so the sendCoordinate is plus a negtive sign;
    float sendY = -(target.x*sin(target.A3)-real.x*sin(real.A3));
    float sendZ = (target.y-real.y);

    sendDeltaXYZ(sendX, sendY, sendZ, 0, 0, 2*sqrt(sq(sendX) + sq(sendY) + sq(sendZ)));

    println("send.x " + target.x + "\tsend.y " + target.y + "\tsend.A3 " + target.A3);
    println("real.x " + real.x + "\treal.y " + real.y + "\treal.A3 " + real.A3);
    println("sendX " + sendX + "\tsendY " + sendY + "\tsendZ " + sendZ);
    println();

    real.x = target.x;
    real.y = target.y;
    real.A3 = target.A3;
    isSended = true;
  }
  target = limitInLaw(catched);

  background(background);
  strokeWeight(2);
  noFill();
  quad(border, border, frontQuadWidth+border, border, frontQuadWidth+border, height-8*border, border, height-8*border);//the front quad
  //  quad(frontQuadWidth+3*border, border, width-border, border, width-border, height/2-border, frontQuadWidth+3*border, height/2-border);//the servo quad
  //  quad(frontQuadWidth+3*border, height/2+border, width-border, height/2+border, width-border, height-border, frontQuadWidth+3*border, height-border);//top quad
  //ellipse(width-border-topRadius, height-border-topRadius, 2*topRadius, 2*topRadius);//the top ellipse;
  arc(width-border-topRadius, 2*border+topRadius, 2*topRadius, 2*topRadius, -5*QUARTER_PI, QUARTER_PI, CHORD);

  fill(100);
  textFont(coorWord, 48);

  String stringX = (Float.toString(display.x*cos(display.A3))+"00000").substring(0, 5);
  String stringY = (Float.toString(display.y*sin(display.A3))+"00000").substring(0, 5);
  String stringZ = (Float.toString(display.y)+"00000").substring(0, 5);

  text("x: "+stringX, 885, 400);
  text("Y: "+stringY, 885, 450);
  text("Z: "+stringZ, 885, 500);

  /////////////////////////////////////////////////////////////////////
  /////////////////////the front dobot easing move/////////////////////
  float easing = 0.1f;
  float dx = target.x - display.x;
  float dy = target.y - display.y;
  float ds = sqrt(sq(dx)+sq(dy));
  if (ds > 1.0f)
  {
    myDobotModule.displayFront(display.x + dx*easing, display.y + dy*easing);
    display.x += dx*easing;
    display.y += dy*easing;
  }else
  {
    myDobotModule.displayFront(display.x, display.y);
  }
  //  print(display.x + "\t");
  //  println(display.y);
  /////////////////////////////////////////////////////////////////////
  ///////////////////the top dobot easing move/////////////////////////
  float dA3 = target.A3 - display.A3;
  if (abs(dA3) >0.01f )
  {
    myDobotModule.displayTop(display.x, display.y, display.A3 + dA3*easing);
    display.A3 += dA3*easing;
  } else
  {
    myDobotModule.displayTop(display.x, display.y, display.A3);
  }
}

public Coordinate2D limitInLaw(Coordinate2D coor)
{
/////////////////////// limit the dobot in the lawable region ///////////////////
    if (coor.x<60)
    { 
      coor.x = 120 - coor.x;
    }
    if (coor.y<0)
    {
      coor.y = 0;
    }
    float R = sqrt(sq(coor.x) + sq(coor.y-L3));//the radius;
    if (R > MAXRADIUS)
    {
      coor.x = coor.x*MAXRADIUS/R;
      coor.y = L3+(coor.y-L3)*MAXRADIUS/R;
    }
    /////////////////////////////////////////////////////////////////////
    /////////caculate the a1 and a2 (angle1 and angle2)//////////////////
    float A = -2 * coor.x * L1;
    float B = 2 * (coor.y-L3)*L1;
    float C = sq(L2) - sq(L1) - sq(coor.x) - sq(coor.y-L3);
    float a1 = constrain(2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C)), MINA1, MAXA1);
    
    A = 2 * (coor.y-L3) * L2;
    B = 2 * coor.x * L2;
    C = sq(L2) + sq(coor.x) + sq(L3 - coor.y) - sq(L1) ;
    float a2 = constrain(2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C)), MINA2, MAXA2);
    
    coor.A3 = constrain(coor.A3, -3*QUARTER_PI, 3*QUARTER_PI);
    /////////////////////////////////////////////////////////////////////

    Coordinate2D temp = new Coordinate2D(L2*sin(a2)+L1*cos(a1),(L2*cos(a2)-L1*sin(a1)+L3)<=0 ? 0 : (L2*cos(a2)-L1*sin(a1)+L3),coor.A3);
    return(temp);
}

  public void mousePressed()
{
  if (border<mouseX && mouseX<frontQuadWidth+border && border<mouseY && mouseY<height-8*border)//if mouse is in the front quad
  {
    catched.x = (mouseX - width*frontOriginTrainlateX)/frontScaleRatio;//caculate the mouseX and mouseY in the front origin system;
    catched.y = (height*(1-frontOriginTrainlateY) - mouseY)/frontScaleRatio;
  }

  if (sq(mouseX - (width-border-topRadius)) + sq((2*border+topRadius) - mouseY) < sq(topRadius))//if mouse is in the top width-border-topRadius, height-border-topRadius, 2*topRadius, 2*topRadius
  {
    float tempX = (mouseX - (width-border-topRadius))/topScaleRatio;
    float tempY = ((2*border+topRadius) - mouseY)/topScaleRatio;

    if (tempX>0)
    {
      catched.A3 =  HALF_PI-atan(tempY/tempX);
    } else
    {
      catched.A3 =  -HALF_PI-atan(tempY/tempX);
    }
  }
}

public void mouseDragged()
{
  if (border<mouseX && mouseX<frontQuadWidth+border && border<mouseY && mouseY<height-8*border)//if mouse is in the front quad
  {
    catched.x = (mouseX - width*frontOriginTrainlateX)/frontScaleRatio;//caculate the mouseX and mouseY in the front origin system;
    catched.y = (height*(1-frontOriginTrainlateY) - mouseY)/frontScaleRatio;
  }

  if (sq(mouseX - (width-border-topRadius)) + sq((2*border+topRadius) - mouseY) < sq(topRadius))//if mouse is in the top width-border-topRadius, height-border-topRadius, 2*topRadius, 2*topRadius
  {
    float tempX = (mouseX - (width-border-topRadius))/topScaleRatio;
    float tempY = ((2*border+topRadius) - mouseY)/topScaleRatio;

    if (tempX>0)
    {
      catched.A3 =  HALF_PI-atan(tempY/tempX);
    } else
    {
      catched.A3 =  -HALF_PI-atan(tempY/tempX);
    }
  }
}

public void mouseReleased()
{
  isSended = false;
}
class Coordinate2D
{
  public float x;        //the view of front, the right hand;
  public float y;        //the view of front, the top
  public float A3;       //the view of top

  Coordinate2D(float x_, float y_, float A3_)
  {
    x = x_;
    y = y_;
    A3 = A3_;
  }
}


class Dobot2DModule
{
  public void displayFront(float x, float y)//in the front quad(the biggest quad)
  {
    /////////////////////////////////////////////////////////////////////
    /////////caculate the a1 and a2 (angle1 and angle2)//////////////////
    float A = -2 * x * L1;
    float B = 2 * (y-L3)*L1;
    float C = sq(L2) - sq(L1) - sq(x) - sq(y-L3);

    float a1 = constrain(2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C)), MINA1, MAXA1);
    A = 2 * (y-L3) * L2;
    B = 2 * x * L2;
    C = sq(L2) + sq(x) + sq(L3 - y) - sq(L1) ;

    float a2 = constrain(2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C)), MINA2, MAXA2);
    /////////////////////////////////////////////////////////////////////

    pushMatrix();
    convertToFrontQuadOrigin();
    drawFrontHead(L2*sin(a2)+L1*cos(a1), L2*cos(a2)-L1*sin(a1)+L3);
    drawFrontSmallArm(L2*sin(a2), L2*cos(a2)+L3, a1);
    drawFrontBigArm(0, L3, a2);
    drawFrontBase();
    popMatrix();
  }

  public void displayTop(float x, float y, float a3)//the x and y is the front view axis
  {

    /////////caculate the a1 and a2 (angle1 and angle2)//////////////////
    float A = -2 * x * L1;
    float B = 2 * (y-L3)*L1;
    float C = sq(L2) - sq(L1) - sq(x) - sq(y-L3);

    float a1 = constrain(2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C)), MINA1, MAXA1);
    A = 2 * (y-L3) * L2;
    B = 2 * x * L2;
    C = sq(L2) + sq(x) + sq(L3 - y) - sq(L1) ;

    float a2 = constrain(2*atan((B-sqrt(sq(B)+sq(A)-sq(C)))/(A+C)), MINA2, MAXA2);
    /////////////////////////////////////////////////////////////////////


    float prjBigLength = L2 * sin(a2);
    float prjSmallLength = L1 * cos(a1);


    pushMatrix();
    convertToTopQuadOrigin();
    drawTopBase(a3);
    drawTopBigArm(0, prjBigLength, a3);
    drawTopSmallArm(prjBigLength, prjBigLength+prjSmallLength, a3);
    drawTopHead(prjBigLength+prjSmallLength, a3);
    popMatrix();
  }

  private void drawFrontSmallArm(float pointX, float pointY, float angle)
  {
    pushMatrix();
    translate(pointX, pointY);
    rotate(-angle);
    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(0, 0);
    vertex(7, 52);
    vertex(143, 52);
    vertex(171, 18);
    vertex(171, 0);
    endShape(CLOSE);
    arc(157.5f, 7.25f, 34.5f, 34.5f, radians(-155), radians(40), OPEN);//this arc position need to be accuratly
    fill(background);
    ellipse(160, 0, 10, 10);
    rect(35.5f, 15, 92, 14, 7);

    pushMatrix(); //the word is not the right position, this step is to print the logo "dobot" correctly; 
    scale(1, -1);

    fill(0, 102, 153);
    textFont(dobotWord, 12);
    text("dobot", 66, -36);
    popMatrix();

    popMatrix();
  }

  private void drawFrontBigArm(float pointX, float pointY, float angle)
  {
    pushMatrix();
    translate(pointX, pointY);
    rotate(HALF_PI-angle);
    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(21.38f, 0);
    vertex(115.5f, 0);
    vertex(143, 11.8f);
    vertex(135, 49);
    vertex(0, 49);
    vertex(-19.2f, 9.4f);
    vertex(0, -20);
    endShape(CLOSE);
    arc(0, 0, 42.7f, 42.7f, radians(-210), radians(1), OPEN);//this arc position need to be accuratly
    arc(143, 11.8f, 60, 60, radians(-158), radians(102), OPEN);//this arc position need to be accuratly
    strokeWeight(1);
    ellipse(143, 11.8f, 53, 53);

    fill(background);
    strokeWeight(3);
    rect(41, 14, 63.5f, 16, 8);
    popMatrix();
  }
  private void drawFrontHead(float pointX, float pointY)
  {
    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(pointX+6.2f, pointY+28);
    vertex(pointX+0, pointY+0);
    vertex(pointX+16.1f, pointY-12.2f);
    vertex(pointX+16.1f, pointY-14);
    vertex(pointX+70, pointY-14);
    vertex(pointX+70, pointY-9);
    vertex(pointX+24, pointY-9);
    vertex(pointX+24, pointY+25.68f);
    endShape();

    arc(pointX+15, pointY+26, 18, 18, 0, radians(168));
    fill(background);
    ellipse(pointX+15, pointY+26, 6, 6);
  }
  private void drawFrontBase()
  {
    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(30, 0);
    vertex(30, 88);
    vertex(18, 100);
    vertex(-30, 100);
    vertex(-30, 0);
    vertex(30, 0);
    endShape();

    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(49, 0);
    vertex(49, -4);
    vertex(-92, -4);
    vertex(-92, 0);
    vertex(49, 0);
    endShape();

    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(32.5f, -4);
    vertex(32.5f, -18);
    vertex(-32.5f, -18);
    vertex(-32.5f, -4);
    vertex(32.5f, -4);
    endShape();     

    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(110, -18);
    vertex(110, -22);
    vertex(-60.5f, -22);
    vertex(-60.5f, -18);
    vertex(110, -18);
    endShape();

    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(16.15f, 58.85f);
    vertex(21.15f, 63.85f);
    vertex(21.15f, 96.15f);
    vertex(16.15f, 101.15f);
    vertex(-16.15f, 101.15f);
    vertex(-21.15f, 96.15f);
    vertex(-21.15f, 63.85f);
    vertex(-16.15f, 58.85f);
    vertex(16.15f, 58.85f);
    endShape();
  }

  public void drawTopSmallArm(float beginLength, float endLength, float a3)
  {
    pushMatrix();
    rotate(HALF_PI);
    pushMatrix();
    rotate(-a3);
    strokeWeight(8);
    line(beginLength-5, 10, endLength+5, 10);
    line(beginLength-5, -10, endLength+5, -10);
    popMatrix();
    popMatrix();
  }
  public void drawTopBigArm(float beginLength, float endLength, float a3)
  {
    pushMatrix();
    rotate(HALF_PI);
    pushMatrix();
    rotate(-a3);
    strokeWeight(8);
    line(beginLength-5, 20, endLength+5, 20);
    line(beginLength-5, -20, endLength+5, -20);
    popMatrix();
    popMatrix();
  }
  public void drawTopHead(float beginLength, float a3)
  {
    pushMatrix();
    rotate(HALF_PI);
    pushMatrix();
    rotate(-a3);
    strokeWeight(8);
    line(beginLength-5, 5, beginLength+50, 5);
    line(beginLength-5, -5, beginLength+50, -5);
    popMatrix();
    popMatrix();
  }
  public void drawTopBase(float angle)
  {
    pushMatrix();
    rotate(HALF_PI);
    rect(-60.5f, -75, 170, 150, 17);

    ///////////////////////////////////////////////
    /////////////////draw base/////////////////////
    pushMatrix();
    rotate(-angle);
    fill(220);
    rect(-92, -54, 141, 108, 10);
    rect(-30, 22, 60, 4); //the motor stent
    rect(-30, -22, 60, -4);


    rect(-20.5f, 26, 41, 60);//the motor
    rect(-20.5f, 26, 41, 20);
    line(-15.5f, 46, -15.5f, 86);    
    line(15.5f, 46, 15.5f, 86);

    rect(-20.5f, -26, 41, -60);//the motor
    rect(-20.5f, -26, 41, -20);
    line(-15.5f, -46, -15.5f, -86);    
    line(15.5f, -46, 15.5f, -86);
    popMatrix();
    popMatrix();
    ///////////////////////////////////////////////
  }

  public void convertToFrontQuadOrigin()
  {
    resetMatrix();
    translate(width * frontOriginTrainlateX, height * (1-frontOriginTrainlateY));  //translate and scale for axis be left-bottom;
    scale(frontScaleRatio, -frontScaleRatio);
  }
  public void convertToTopQuadOrigin()
  {
    resetMatrix();
    translate(width-border-topRadius, 2*border+topRadius);  //translate and scale for axis be left-bottom;
    scale(topScaleRatio, -topScaleRatio);
  }
}
public void dobotSerialBegin()
{
  if (Serial.list().length > 0)
  {
    String portName = Serial.list()[0];
    myPort = new Serial(this, portName, 256000);
    myPort.bufferUntil(0x5a);
    println(portName);
    serialEn = true;

    while (myPort.available () == 0)
    {
      println(hex(myPort.read()));
    }
    while (myPort.available ()>0)
    {
      println(hex(myPort.read()));
    }
    sendBeginPackage();
  }
}

public void sendDeltaXYZ(float deltaX, float deltaY, float deltaZ, float StartVel, float EndVel, float MaxVel)
{
  float state = 1.0f;
  float Axis = 0;
  float X = deltaX;
  float Y = deltaY;
  float Z = deltaZ;
  float RHead = 0;
  float isGrab = 0;
  //StartVel = StartVel;
  //EndVel = EndVel;
  //MaxVel = MaxVel;

  sendPackage(state, Axis, X, Y, Z, RHead, isGrab, StartVel, EndVel, MaxVel);  
  //  println("state:" + state + " axis:" + Axis + " X:" + X + " Y:" + Y + " Z:" + Z + " RHead:" + RHead + " isGrab:" + isGrab + " StarVel:"+ StartVel + " EndVel:" + EndVel + " MaxVel:" + MaxVel);
}


public void sendPackage(float state, float Axis, float X, float Y, float Z, float RHead, float isGrab, float StartVel, float EndVel, float MaxVel)
{
  byte[][] send = new byte[10][4];

  send[0] = float2byte(state);
  send[1] = float2byte(Axis);
  send[2] = float2byte(X);
  send[3] = float2byte(Y);
  send[4] = float2byte(Z);
  send[5] = float2byte(RHead);
  send[6] = float2byte(isGrab);
  send[7] = float2byte(StartVel);
  send[8] = float2byte(EndVel);
  send[9] = float2byte(MaxVel);  

  myPort.write(PApplet.parseByte(0xa5)); //the package head
  for (int i =0; i<10; i++)
  {
    myPort.write(send[i]);
  }
  myPort.write(PApplet.parseByte(0x5a));  //the package tail
}

public void sendBeginPackage()
{
  byte[] send= {
    PApplet.parseByte(0xa5), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x11), PApplet.parseByte(0x11), 
    PApplet.parseByte(0x22), PApplet.parseByte(0x22), PApplet.parseByte(0x33), PApplet.parseByte(0x33), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), PApplet.parseByte(0x00), 
    PApplet.parseByte(0x00), PApplet.parseByte(0x5a)
    };
    myPort.write(send);
}
//float[] receivedPackage()
//{
//  byte[] inBuffer = new byte[4];
//  float[] packages = new float[9];
//  while (myPort.available() > 0) 
//  {
//    if (inBuffer != null)
//    {
//      for(int i=0;i<9;i++)
//      {
//        inBuffer = myPort.readBytes();
//        packages[i] = byte2float(inBuffer, 4);
// //       println(packages[i]);
//      }
//    }
//  }
//  for(int i=0; i<9; i++)
//  println(packages[i]);

//  return packages;

//}

public byte[] float2byte(float f) 
{  
  int fbit = Float.floatToIntBits(f);  

  byte[] b = new byte[4];    
  for (int i = 0; i < 4; i++)
  {    
    b[i] = (byte) (fbit >> (24 - i * 8));
  }

  int len = b.length;  
  byte[] dest = new byte[len];  
  System.arraycopy(b, 0, dest, 0, len);  
  byte temp;
  for (int i = 0; i < len / 2; ++i)
  {  
    temp = dest[i];  
    dest[i] = dest[len - i - 1];  
    dest[len - i - 1] = temp;
  }
  return dest;
}

//float byte2float(byte[] b, int index) 
//{    
//  int l;                                             
//  l = b[index + 0];                                  
//  l &= 0xff;                                         
//  l |= ((long) b[index + 1] << 8);                   
//  l &= 0xffff;                                       
//  l |= ((long) b[index + 2] << 16);                  
//  l &= 0xffffff;                                     
//  l |= ((long) b[index + 3] << 24);                  
//  return Float.intBitsToFloat(l);
//} 
  public void settings() {  size(1200, 600);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dobotProcessingPC" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
