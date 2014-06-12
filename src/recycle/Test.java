package recycle;

import javax.swing.JPanel;

class T1{
	static int i;
	int getI(){
		return i;
	}
}

class T2 extends T1{
	static int i;
	{i = 1;}
} 

public class Test {

	public static void main(String[] args) {
		int i = 10;
		System.out.println(Integer.toBinaryString(~i));

	}

}
