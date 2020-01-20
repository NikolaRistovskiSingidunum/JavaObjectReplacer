import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Start {

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		// TODO Auto-generated method stub

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			CustomClassLoader cl = new CustomClassLoader();
			reader.readLine();
			Class c = cl.classFromByteCode(new File("D:\\Master\\AdvanceJava\\bin\\DynamicClass.class"));
			c.newInstance();

		}

	}

}
