package GeoNames.disambiguationTest;

import org.junit.Test;
import GeoNames.disambiguation.Disambiguate;

public class DisambiguateTest {

	@Test
	public void testFirst() {
		Disambiguate newObject = new Disambiguate();
		newObject
				.disambiguate("http://data.cityofchicago.org/resource/4jy7-7m68.json");

	}

}
