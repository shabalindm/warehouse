package dao;

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
		// TODO Auto-generated method stub
		new T1();
		System.out.println(T1.i);
		System.out.println(T2.i);
		System.out.println(new T1().getI());
	}

}
