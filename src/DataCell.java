import processing.core.PApplet;

public class DataCell {
	
	public int wide, high;
	public String content;
	public float speed;
	public int direction = 1;
	public float x, y;
	PApplet parent;

  //constructor
  public DataCell(PApplet p, float xpos, float ypos, int iwide, int ihigh, String icontent, float sp) {
	  parent = p;
	  wide = iwide;
	  high = ihigh;
	  content = icontent;
	  speed = sp;
	  x = xpos;
	  y = ypos;
  }
  
  public void updateCell(String update) {
    if(update != "0") {
    content = update;
    }
  }
  
  void move() {
    y += (speed * direction);
    if((y > (200 - wide/2)) || (y < wide/2)) {
      direction *= -1;
    }
  }
  
  public void display(int i) {
    parent.noFill();
    parent.stroke(255);
    parent.rect(x, y, wide, high);
    parent.fill(0xFFFFFFFF);
    parent.text(content, x + 1, y+1, wide, high);
    }
}
