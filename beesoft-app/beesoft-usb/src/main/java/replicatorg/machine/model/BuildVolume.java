package replicatorg.machine.model;

public class BuildVolume {

	private int x;
	private int y;
	private int z;
	/* 
	 * TODO: This, more complex class could implement things like a cut-outs and places to avoid such as tool-changers. 
	 * Perhaps managed whether it's confirmed to be empty or contains objects that we need to travel around? 
	 */	
	public BuildVolume(){
	}
	
	public BuildVolume(int x,int y,int z){
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}
	
	public int getAxis(AxisId a){
		
		switch (a) {
		case X:
			return this.x;
		case Y:
			return this.y;
		case Z:
			return this.z;
		default:
			return 0;
		}
	}
	
	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public void setZ(int z){
		this.z = z;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}
	
	public int getZ(){
		return this.z;
	}


}
