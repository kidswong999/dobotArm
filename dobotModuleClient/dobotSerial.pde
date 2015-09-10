void dobotSerialBegin()
{
  if (Serial.list().length > 0)
  {
    String portName = Serial.list()[0];
    myPort = new Serial(this, portName, 256000);
    myPort.bufferUntil(0x5a);
    println(portName); //<>//
    serialEn = true;

    while (myPort.available () == 0)
    {
      println(hex(myPort.read()));
    }
    while (myPort.available ()>0) //<>//
    {
      println(hex(myPort.read()));
    }
    sendBeginPackage();
  }
}

void sendDeltaXYZ(float deltaX, float deltaY, float deltaZ, float StartVel, float EndVel, float MaxVel)
{
  float state = 1.0;
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


void sendPackage(float state, float Axis, float X, float Y, float Z, float RHead, float isGrab, float StartVel, float EndVel, float MaxVel)
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

  myPort.write(byte(0xa5)); //the package head
  for (int i =0; i<10; i++)
  {
    myPort.write(send[i]);
  }
  myPort.write(byte(0x5a));  //the package tail
}

void sendBeginPackage()
{
  byte[] send= {
    byte(0xa5), byte(0x00), byte(0x00), byte(0x11), byte(0x11), 
    byte(0x22), byte(0x22), byte(0x33), byte(0x33), byte(0x00), 
    byte(0x00), byte(0x00), byte(0x00), byte(0x00), byte(0x00), 
    byte(0x00), byte(0x00), byte(0x00), byte(0x00), byte(0x00), 
    byte(0x00), byte(0x00), byte(0x00), byte(0x00), byte(0x00), 
    byte(0x00), byte(0x00), byte(0x00), byte(0x00), byte(0x00), 
    byte(0x00), byte(0x00), byte(0x00), byte(0x00), byte(0x00), 
    byte(0x00), byte(0x00), byte(0x00), byte(0x00), byte(0x00), 
    byte(0x00), byte(0x5a)
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

byte[] float2byte(float f) 
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

