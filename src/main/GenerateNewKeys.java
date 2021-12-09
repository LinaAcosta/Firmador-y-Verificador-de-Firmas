package main;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.*;
public class GenerateNewKeys {

   private static final int ITERACIONES = 1000;
   private String ruta_privada;
   private byte[] archivo;
   private byte[] bytesFirma;
   private InputStream fis;
   private FileInputStream private_k;
   private Signature sign; 
   private KeyPair claves;  
	  // Crea una clave RSA de 1024 bits y la almacena en dos ficheros
	  // uno para la publica y otro para la privada (encriptada por password)
	  public void crearClave() throws Exception {
	    // Crear la clave RSA
	    System.out.println("Generado el par de claves RSA.\n");
	    KeyPairGenerator generadorRSA = KeyPairGenerator.getInstance("RSA");
	    generadorRSA.initialize(1024);
	    claves = generadorRSA.genKeyPair();
	    // Toma la forma codificada de la clave pública para usarla en el futuro. 
	    byte[] bytesPublica = claves.getPublic().getEncoded();
	    // Lee el nombre del archivo para la clave pública
	    System.out.print("Nombre del archivo para grabar la clave pública: ");
	    BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
	    String ficheroPublica = entrada.readLine();
	    // Escribir la clave pública codificada en su fichero
	    FileOutputStream salida = new FileOutputStream(ficheroPublica);
	    salida.write(bytesPublica);
	    salida.close();
	    // Repetimos lo mismo para la clave privada, encriptandola con un password.
	    System.out.print("Nombre del archivo para grabar la clave privada ");
	    String ficheroPrivada = entrada.readLine();
	    //Guardamos la ruta de la clave privada para usarla posteriormente
	    ruta_privada = "C:/Users/limar/eclipse-workspace/FinalProjectSecurity/" + ficheroPrivada + "";
	    // Tomamos la forma codificada. 
	    byte[] bytesPrivada = claves.getPrivate().getEncoded();
	    // Solicitar el password para encriptar la clave privada
	    System.out.print("Password para encriptar la clave privada ");
	    String password = entrada.readLine();
	    // Aquí encriptamos la clave privada
	    byte[] bytesPrivadaEncriptados =
	    encriptarPrivateKey(password.toCharArray(),bytesPrivada);
	    // Grabamos el resultado en el fichero
	    salida = new FileOutputStream(ficheroPrivada);
	    salida.write(bytesPrivadaEncriptados);
	    salida.close();
	  }
	  // Utilidad para encriptar la clave privada con un password. El salto serán los 8 primeros bytes del array devuelto.
	  private static byte[] encriptarPrivateKey(char[] password, byte[] texto) throws Exception {
	    // Crear el salto
	    byte[] salto = new byte[8];
	    Random aleatorio = new Random();
	    aleatorio.nextBytes(salto);
	    // Crear una clave y un cifrador PBE
	    PBEKeySpec especificacion = new PBEKeySpec(password);
	    SecretKeyFactory factoria = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
	    SecretKey clave = factoria.generateSecret(especificacion);
	    PBEParameterSpec parametros = new PBEParameterSpec(salto, ITERACIONES);
	    Cipher cifrador = Cipher.getInstance("PBEWithMD5AndDES");
	    cifrador.init(Cipher.ENCRYPT_MODE, clave, parametros);
	    // Encriptar el array
	    byte[] textoCifrado = cifrador.doFinal(texto);
	    // Escribir el salto seguido del texto cifrado y devolverlo.
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    baos.write(salto);
	    baos.write(textoCifrado);
	    return baos.toByteArray();
	  }
	  // Utilidad para desencriptar la clave privada con un password.El salto son los 8 primeros bytes del array que se pasa como texto cifrado.
	  private static byte[] desencriptarPrivateKey(char[] password, byte[] textoCifrado) throws Exception {
	    // Leer el salto.
	    byte[] salto = new byte[8];
	    ByteArrayInputStream bais = new ByteArrayInputStream(textoCifrado);
	    bais.read(salto,0,8);
	    // Los bytes resultantes son el texto cifrado.
	    byte[] textoRestante = new byte[textoCifrado.length-8];
	    bais.read(textoRestante,0,textoCifrado.length-8);
	    // Crear un descifrador PBE.
	    PBEKeySpec especificacion = new PBEKeySpec(password);
	    SecretKeyFactory factoria = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
	    SecretKey clave = factoria.generateSecret(especificacion);
	    PBEParameterSpec parametros = new PBEParameterSpec(salto, ITERACIONES);
	    Cipher cifrador = Cipher.getInstance("PBEWithMD5AndDES");
	    // Realizar la desencriptación
	    cifrador.init(Cipher.DECRYPT_MODE, clave, parametros);
	    return cifrador.doFinal(textoRestante);
	  }
	  //Método para firmar cualquier archivo.
	  public void SignFile() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		  BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //Ya tenemos el "lector"
		  System.out.println("Por favor ingrese la ruta del archivo que desea firmar");//Se pide la ruta del archivo que se desea firmar al usuario
	      //Se lee la ruta del archivo
		  String path = br.readLine();
	      fis = new BufferedInputStream(new FileInputStream(path));
	      //Se lee el archivo
	      archivo = new byte[fis.available()];
	      fis.read(archivo);
	      //Se solicita el password de la clave privada para poder desencriptarla
	      System.out.print("Password para la clave privada: ");
	      //Se lee la clave ingresada por el usuario
	        String password = br.readLine();
	        File pri = new File(ruta_privada);
	        //Se guarda la clave 
	        private_k = new FileInputStream(pri);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        int b = 0;
	        while ((b = private_k.read()) != -1)
	        {
	          baos.write(b);
	        }
	        byte[] bytesClave  = baos.toByteArray();
	        // Aplicar PBE para obtener la clave
	        try {
	        	//Se intenta desencriptar el archivo con la clave dada por el usuario
				bytesClave = desencriptarPrivateKey(password.toCharArray(), bytesClave);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//Si la clave dada por el usuario es incorrecta, se solicita nuevamente
				System.out.println("Contraseña incorrecta, ingresar clave de nuevo");
				System.out.print("Password para la clave privada: ");
		        password = br.readLine();
			}
	        
	        sign = Signature.getInstance("MD5WithRSA");
	        sign.initSign(claves.getPrivate());
	        // Prepara la firma de los datos
	        sign.update(archivo);

	        // Firmar los datos
	        bytesFirma = sign.sign();
	        //Guarda el archivo firmado llamándolo "SignedFile"
	        ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream("SignedFile"));
	        oos1.writeObject(bytesFirma);  
	        System.out.println("Archivo firmado");
	        oos1.close();
	        br.close();
	  }
	  //Verificar que el archivo se ha firmado correctamente
	  public void VerifySign() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, SignatureException, IOException {
		  //Inicializa la clave pública para la verificación
		  sign.initVerify(claves.getPublic());
		  //Actualiza el archivo que se va a verificar
		  sign.update(archivo);
		  boolean verificado = false;
		  try {
			  //Verifica la última firma
		    verificado = sign.verify(bytesFirma);
		  } catch (SignatureException se) {
		        verificado = false;
		    }

		  if (verificado) {
		    System.out.println("\nFirma verificada.");
		  } else {
		    System.out.println("\nFirma incorrecta.");
		  }
	  }
	}

