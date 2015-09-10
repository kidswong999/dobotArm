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
  void displayFront(float x, float y)//in the front quad(the biggest quad)
  {
    /////////////////////// limit the dobot in the lawable region ///////////////////
    if (x<60)
    {
      x = 120 - x;
    }
    if (y<0)
    {
      y = 0;
    }
    float R = sqrt(sq(x) + sq(y-L3));//the radius;
    if (R > MAXRADIUS)
    {
      x = x*MAXRADIUS/R;
      y = L3+(y-L3)*MAXRADIUS/R;
    }
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


    send.x = L2*sin(a2)+L1*cos(a1);
    send.y = L2*cos(a2)-L1*sin(a1)+L3;
    
    
    pushMatrix();
    convertToFrontQuadOrigin();
    drawFrontHead(L2*sin(a2)+L1*cos(a1), L2*cos(a2)-L1*sin(a1)+L3);
    drawFrontSmallArm(L2*sin(a2), L2*cos(a2)+L3, a1);
    drawFrontBigArm(0, L3, a2);
    drawFrontBase();
    popMatrix();
  }

  void displayTop(float x, float y, float a3)//the x and y is the front view axis
  {
    /////////////////////// limit the dobot in the lawable region ///////////////////
    a3 = constrain(a3, -3*QUARTER_PI, 3*QUARTER_PI);
    
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

    send.A3 = a3;


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
    arc(157.5, 7.25, 34.5, 34.5, radians(-155), radians(40), OPEN);//this arc position need to be accuratly
    fill(255);
    ellipse(160, 0, 10, 10);
    rect(35.5, 15, 92, 14, 7);
    
    pushMatrix(); //the word is not the right position, this step is to print the logo "dobot" correctly; 
    scale(1,-1);
    fill(0, 102, 153);
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
    vertex(21.38, 0);
    vertex(115.5, 0);
    vertex(143,11.8);
    vertex(135, 49);
    vertex(0, 49);
    vertex(-19.2, 9.4);
    vertex(0,-20);
    endShape(CLOSE);
    arc(0,0, 42.7,42.7, radians(-210),radians(1), OPEN);//this arc position need to be accuratly
    arc(143,11.8, 60, 60, radians(-158), radians(102), OPEN);//this arc position need to be accuratly
    strokeWeight(1);
    ellipse(143,11.8, 53, 53);

    fill(255);
    strokeWeight(3);
    rect(41, 14, 63.5, 16, 8);
    popMatrix();
  }
  private void drawFrontHead(float pointX, float pointY)
  {
    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(pointX+6.2, pointY+28);
    vertex(pointX+0, pointY+0);
    vertex(pointX+16.1, pointY-12.2);
    vertex(pointX+16.1, pointY-14);
    vertex(pointX+70, pointY-14);
    vertex(pointX+70, pointY-9);
    vertex(pointX+24, pointY-9);
    vertex(pointX+24, pointY+25.68);
    endShape();

    arc(pointX+15, pointY+26, 18, 18, 0, radians(168));
    fill(255);
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
    vertex(32.5, -4);
    vertex(32.5, -18);
    vertex(-32.5, -18);
    vertex(-32.5, -4);
    vertex(32.5, -4);
    endShape();     

    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(110, -18);
    vertex(110, -22);
    vertex(-60.5, -22);
    vertex(-60.5, -18);
    vertex(110, -18);
    endShape();

    strokeWeight(3);
    fill(230);
    beginShape();
    vertex(16.15, 58.85);
    vertex(21.15, 63.85);
    vertex(21.15, 96.15);
    vertex(16.15, 101.15);
    vertex(-16.15, 101.15);
    vertex(-21.15, 96.15);
    vertex(-21.15, 63.85);
    vertex(-16.15, 58.85);
    vertex(16.15, 58.85);
    endShape();
  }

  void drawTopSmallArm(float beginLength, float endLength, float a3)
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
  void drawTopBigArm(float beginLength, float endLength, float a3)
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
  void drawTopHead(float beginLength, float a3)
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
  void drawTopBase(float angle)
  {
    pushMatrix();
    rotate(HALF_PI);
    rect(-60.5, -75, 170, 150, 17);

    ///////////////////////////////////////////////
    /////////////////draw base/////////////////////
    pushMatrix();
    rotate(-angle);
    fill(220);
    rect(-92, -54, 141, 108, 10);
    rect(-30, 22, 60, 4); //the motor stent
    rect(-30, -22, 60, -4);


    rect(-20.5, 26, 41, 60);//the motor
    rect(-20.5, 26, 41, 20);
    line(-15.5, 46, -15.5, 86);    
    line(15.5, 46, 15.5, 86);

    rect(-20.5, -26, 41, -60);//the motor
    rect(-20.5, -26, 41, -20);
    line(-15.5, -46, -15.5, -86);    
    line(15.5, -46, 15.5, -86);
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

