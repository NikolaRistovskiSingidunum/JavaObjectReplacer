import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;



public class CustomClassLoader extends ClassLoader {
	
	//this override allows loading of two "same" classes. E.G. if we have class A in document A that is already loaded by JAVA VM,
	//and we change class A, then, with this loader modification, we can loaded again. Using ObjectReplacer we can update system with 
	//new class
	
    public Class classFromByteCode(File byteCode) {

        try {
            byte[] code = Files.readAllBytes(Paths.get(byteCode.getAbsolutePath()));
            //System.out.println((Paths.get(source.getAbsolutePath())));
            Class newCLass = defineClass(null, code, 0, code.length);
            //resolveClass(newCLass);
            return newCLass;
        } catch (IOException ex) {
        	System.out.println(ex.getMessage());
        }

        return null;
    }
 
}
