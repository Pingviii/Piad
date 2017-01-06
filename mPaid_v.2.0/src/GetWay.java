import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GetWay {
	
	public static String getWay(String way) throws FileNotFoundException {
		File file = new File(way);
		Scanner sc = new Scanner(file);
		String cstring = sc.next();
		
		cstring=cstring.replace('\\', '/'); //переробляємо шлях 
		
		sc.close();
		return cstring;
	}
}
