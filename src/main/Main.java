package main;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		GenerateNewKeys generateKey= new GenerateNewKeys();
		generateKey.crearClave();
		generateKey.SignFile();
		generateKey.VerifySign();
		
	}

}
