package sporemodder.files.formats.prop;

public class PackageSignature {
	/*
	 * This is impossible. We don't have the private key.
	 * 
	 */
	
//	private static final int SIGNATURE_SIZE = 128;
//	
//	private static byte[] readSignature(InputStreamAccessor in) throws IOException
//	{
//		int propCount = in.readInt();
//		int propertyName = in.readInt();
//		
//		if (propertyName != Hasher.getPropHash("packageSignature")) {
//			throw new IOException("Unexcepted property name in pos " + in.getFilePointer());
//		}
//		
//		int propertyType = in.readShort();
//		int propertyFlags = in.readShort();
//		// we expect it to be an array
//		int arrayCount = in.readInt();
//		int arrayItemSize = in.readInt();
//		
//		if ((arrayCount * arrayItemSize) != SIGNATURE_SIZE) {
//			throw new IOException("Unexcepted signature size in pos " + in.getFilePointer());
//		}
//		
//		byte[] signature = new byte[SIGNATURE_SIZE];
//		in.read(signature);
//		
//		return signature;
//	}
//	
//	private static void sign() throws IOException
//	{
//		NameRegistry_old.read();
//		FileStreamAccessor in = new FileStreamAccessor(
//				"E:\\Eric\\SporeMaster 2.0 beta\\!!!!0!!!SRFS_CreatureEditor.package.unpacked\\editorsPackages~\\SporeResurrectionNextSteps.#00B1B104", "r");
//		
//		try 
//		{
//			int propCount = in.readInt();
//			int propertyName = in.readInt();
//			
//			if (propertyName != Hasher.getPropHash("packageSignature")) {
//				throw new IOException("Unexcepted property name in pos " + in.getFilePointer());
//			}
//			
//			int propertyType = in.readShort();
//			int propertyFlags = in.readShort();
//			// we expect it to be an array
//			int arrayCount = in.readInt();
//			int arrayItemSize = in.readInt();
//			
//			if ((arrayCount * arrayItemSize) != SIGNATURE_SIZE) {
//				throw new IOException("Unexcepted signature size in pos " + in.getFilePointer());
//			}
//			
//			byte[] signature = new byte[SIGNATURE_SIZE];
//			in.read(signature);
//			
//			int lengthOfFileData = in.length() - in.getFilePointer();
//			
//			System.out.println(lengthOfFileData);
//			
//			//TODO finish
//			
//		} finally {
//			in.close();
//		}
//	}
//	
//	/// Be aware, it doesn't work correctly. Anyways, it's worthless, since we need the private key to sign a package
//	private static void validate() throws IOException
//	{
//		FileStreamAccessor in = new FileStreamAccessor(
//				"E:\\Eric\\SporeMaster 2.0 beta\\BP2_Data_test.package.unpacked\\editorsPackages~\\BoosterPack2.#00B1B104", "r");
//		
//		try 
//		{
//			byte[] signatureBytes = readSignature(in);
//			int lengthOfFileData = in.length() - in.getFilePointer();
//			
//			byte[] fileData = new byte[lengthOfFileData];
//			in.read(fileData);
//			
//			// Step 1 -> convert file data into a 20 bytes long SHA1 code
//			
//			MessageDigest md = MessageDigest.getInstance("SHA-1");
//			byte[] shaResult = md.digest(fileData);
//			
//			// Step 2 -> RSA_verify
//			
//			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//			keyGen.initialize(1024);
//			//byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
//			
//			byte[] modulusBytes = Files.readAllBytes(new File("C:\\Users\\Eric\\Desktop\\spore_modulus.CEM").toPath());
//			
//			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//			
//			BigInteger modulus = new BigInteger(modulusBytes);
//			BigInteger exponent = new BigInteger("65537");
//			
//			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
//			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//			
//			Signature signature = Signature.getInstance("NONEwithRSA");
////			signature.initVerify(publicKey);
//			//signature.initSign(publicKey);
//			signature.update(shaResult);
//			//System.out.println(signature.verify(signatureBytes));
//			byte[] outbuf = new byte[SIGNATURE_SIZE];
//			signature.sign(outbuf, 0, SIGNATURE_SIZE);
//			
//			Files.write(new File("C:\\Users\\Eric\\Desktop\\signature test\\signature.rw4").toPath(), signatureBytes, StandardOpenOption.CREATE);
//			Files.write(new File("C:\\Users\\Eric\\Desktop\\signature test\\sha_result.rw4").toPath(), shaResult, StandardOpenOption.CREATE);
//			Files.write(new File("C:\\Users\\Eric\\Desktop\\signature test\\outbuf_signature.rw4").toPath(), shaResult, StandardOpenOption.CREATE);
//			
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidKeySpecException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SignatureException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			in.close();
//		}
//	}
//	
//	public static void main(String[] args) throws IOException {
//		NameRegistry_old.read();
//		
//		validate();
//	}
}
