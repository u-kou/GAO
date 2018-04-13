package test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONTest {

	public static void main(String[] args) {
		A a = new A();
		a.s = null;
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
        try {
			json = mapper.writeValueAsString(a);
			System.out.println(json);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
	}

}

class A {
	public String s;
}
