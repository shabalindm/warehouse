package dao;

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
		
		System.out.println("\n".matches("\\s*"));

	}

}
