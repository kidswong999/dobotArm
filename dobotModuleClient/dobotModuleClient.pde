import processing.serial.*;
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
PFont dobotWord, coorWord;
/////////////////////////////////////////////////////////////////////
//////////////////////////some UI data///////////////////////////////
float frontScaleRatio = 1.5;             //enlarge to fit screen
float topScaleRatio = 0.5;                   

float frontOriginTrainlateX = 0.2;
float frontOriginTrainlateY = 0.25;

int border = 10;                         //the UI ...er a border width
int frontQuadWidth = 800;
int topRadius = 185;

color background = color(230,245,245);
int r=0, g=0, b=0;
int counter=0;
//////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////

Dobot2DModule myDobotModule = new Dobot2DModule();

Coordinate2D target = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);   //target location
Coordinate2D real = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);     //real location;
Coordinate2D display = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);
Coordinate2D send = new Coordinate2D(L2*cos(PI/4) + L1*sin(PI/4), L3 + L2*sin(PI/4) - L1*cos(PI/4), 0);


///////////////////////////some program flags////////////////////////
boolean isSended = true;                    //flag to judge whether the data sended
boolean serialEn = false;
//float inPackage[];
/////////////////////////////////////////////////////////////////////
void setup()
{
  dobotSerialBegin();//set bite rate to 256000 and begin communitation
  size(1200, 600);
  smooth();
  dobotWord = loadFont("STHupo-24.vlw");
  coorWord = loadFont("NI7SEG-48.vlw");
}

void draw()
{
  //  println("drawing");

  if (serialEn && !isSended)
  {
    float sendX = -(send.x*cos(send.A3)-real.x*cos(real.A3));//the machine's axis is not same as the module, so the sendCoordinate is plus a negtive sign;
    float sendY = -(send.x*sin(send.A3)-real.x*sin(real.A3));
    float sendZ = send.y-real.y;
    sendDeltaXYZ(sendX, sendY, sendZ, 0, 0, 2*sqrt(sq(sendX) + sq(sendY) + sq(sendZ)));
    real.x = send.x;
    real.y = send.y;
    real.A3 = send.A3;
    isSended = true;
  }

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

  String stringX = send.x==0 ? "000.0" : Float.toString(send.x).substring(0, 5);
  String stringY = send.y==0 ? "000.0" : Float.toString(send.y).substring(0, 5);
  String stringA3 = send.A3==0 ? "000.0" : Float.toString(degrees(send.A3)).substring(0, 5);

  text("x: "+stringX, 885, 400);
  text("Y: "+stringY, 885, 450);
  text("A3: "+stringA3, 850, 500);

  /////////////////////////////////////////////////////////////////////
  /////////////////////the front dobot easing move/////////////////////
  float easing = 0.1;
  float dx = target.x - display.x;
  float dy = target.y - display.y;
  float ds = sqrt(sq(dx)+sq(dy));
  if (ds > 1.0)
  {
    myDobotModule.displayFront(display.x + dx*easing, display.y + dy*easing);
    display.x += dx*easing;
    display.y += dy*easing;
  } else
  {
    myDobotModule.displayFront(display.x, display.y);
  }
  //  print(display.x + "\t");
  //  println(display.y);

  /////////////////////////////////////////////////////////////////////
  ///////////////////the top dobot easing move/////////////////////////
  float dA3 = target.A3 - display.A3;
  if (abs(dA3) >0.01 )
  {
    myDobotModule.displayTop(display.x, display.y, display.A3 + dA3*easing);
    display.A3 += dA3*easing;
  } else
  {
    myDobotModule.displayTop(display.x, display.y, display.A3);
  }
}


void mousePressed()
{

  if (border<mouseX && mouseX<frontQuadWidth+border && border<mouseY && mouseY<height-8*border)//if mouse is in the front quad
  {
    target.x = (mouseX - width*frontOriginTrainlateX)/frontScaleRatio;//caculate the mouseX and mouseY in the front origin system;
    target.y = (height*(1-frontOriginTrainlateY) - mouseY)/frontScaleRatio;
  }

  if (sq(mouseX - (width-border-topRadius)) + sq((2*border+topRadius) - mouseY) < sq(topRadius))//if mouse is in the top width-border-topRadius, height-border-topRadius, 2*topRadius, 2*topRadius
  {
    float tempX = (mouseX - (width-border-topRadius))/topScaleRatio;
    float tempY = ((2*border+topRadius) - mouseY)/topScaleRatio;

    if (tempX>0)
    {
      target.A3 =  HALF_PI-atan(tempY/tempX);
    } else
    {
      target.A3 =  -HALF_PI-atan(tempY/tempX);
    }
  }
}

void mouseDragged()
{
  if (border<mouseX && mouseX<frontQuadWidth+border && border<mouseY && mouseY<height-8*border)//if mouse is in the front quad
  {
    target.x = (mouseX - width*frontOriginTrainlateX)/frontScaleRatio;//caculate the mouseX and mouseY in the front origin system;
    target.y = (height*(1-frontOriginTrainlateY) - mouseY)/frontScaleRatio;
  }

  if (sq(mouseX - (width-border-topRadius)) + sq((2*border+topRadius) - mouseY) < sq(topRadius))//if mouse is in the top width-border-topRadius, height-border-topRadius, 2*topRadius, 2*topRadius
  {
    float tempX = (mouseX - (width-border-topRadius))/topScaleRatio;
    float tempY = ((2*border+topRadius) - mouseY)/topScaleRatio;

    if (tempX>0)
    {
      target.A3 =  HALF_PI-atan(tempY/tempX);
    } else
    {
      target.A3 =  -HALF_PI-atan(tempY/tempX);
    }
  }
}

void mouseReleased()
{
  isSended = false;
}

