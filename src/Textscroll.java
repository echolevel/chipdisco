import processing.core.PApplet;


class Textscroll 
{
	PApplet parent;
	
	float angle;
	int boundX;
	int boundY;
	int textfinalX;
	float textfinalY;
	
	Textscroll(PApplet p)
	{
		parent = p;
	}
   
	void move(int textboundX, int textboundY)
	{
		boundX = textboundX;
		boundY = textboundY;
 
	}
	
	void draw(String textstring)
	{		
			parent.text(textstring, boundX, boundY);
	}
	
	
} 