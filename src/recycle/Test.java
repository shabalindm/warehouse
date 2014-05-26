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
		
		System.out.println(("ID_гюопнь б_цпсоою б_мюхлемнбюмхе б_рс б_люпйю б_ед_хглепемхъ"
				+ "  б_мю_яйкюде б_йнло_хмтн б_дюрю_йнло").split("\\s+").length);

	}

}
