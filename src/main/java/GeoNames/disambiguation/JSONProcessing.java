package GeoNames.disambiguation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class JSONProcessing {

	public void JSon2File(String urlString, String place, String file)
			throws IOException {

		URL url = new URL(urlString);
		try {
			InputStream is = url.openStream();
			JsonReader rdr = Json.createReader(is);
			JsonObject obj = rdr.readObject();
			Collection<JsonValue> newMap = obj.values();

			// we need to ignore the first value
			Object[] arrJson = newMap.toArray();
			
			//checking if geonames gives any records
			JsonValue jv1 = (JsonValue)arrJson[0];
			String arg1 = jv1.toString();
			if(arg1.contains("{\"totalResultsCount\":0,\"geonames\":[]}"))
				{
					System.out.println("No records fetched");
					return;
				}
			else
			{
			JsonValue jv = (JsonValue) arrJson[1];
			String arrString = jv.toString();

			// opening a file writer
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter("/home/nasir/Documents/CS-586/Assignment-2/"
							+ file + ".txt", true)));

			// here I am trying to work with the JSONObjects themselves
			JsonParser parser = (JsonParser) Json
					.createParser(new StringReader(arrString));

			String key = null;
			String value = null;
			Boolean flag = false;

			HashMap<String, String> record = new HashMap<String, String>();
			int level = 0;

			while (parser.hasNext()) {

				JsonParser.Event event = parser.next();
				switch (event) {
				case START_ARRAY:
					break;
				case END_ARRAY:
					break;
				case START_OBJECT:

					level++;
					//System.out.println("Need to reset record here");
					break;

				case END_OBJECT:

					level--;
					//System.out.println("check record here");
					// write to the file here
					//Nasir : merging codes to form one entry of the form "adminCode2 adminName1 adminCode1"
					if (level == 0) {
						writer.println(place + " | " + record.get("adminCode2")
								+ " " + record.get("adminName2") + " "
								+ record.get("adminCode1") + " | "
								+ record.get("countryCode") + " | "
								+ record.get("countryId") + " | "
								+ record.get("continentCode") + " | "
								+ record.get("fcode") + " | "
								+ record.get("fcl") + " | "
								+ record.get("toponymName") + " | ");
						record.clear();
					}
					break;

				case VALUE_FALSE:
				case VALUE_NULL:
				case VALUE_TRUE:
					System.out.println(event.toString());
					break;
				case KEY_NAME:
				/*	System.out.print(event.toString() + " "
							+ parser.getString() + " - ");*/
					key = parser.getString();

					/*
					 * if (key.equals("timezone") || key.equals("dstOffset") ||
					 * key.equals("gmtOffset") || key.equals("timeZoneId") ||
					 * key.equals("countryName") || key.equals("score") ||
					 * key.equals("lng") || key.equals("fcodeName") ||
					 * key.equals("lat") || key.equals("adminName1") ||
					 * key.equals("fclName") || key.equals("elevation") ||
					 * key.equals("wikipediaURL")) { flag = true; continue; }
					 * else flag = false;
					 */
					/*
					 * if (key.equals("adminCode2") || key.equals("adminCode1")
					 * || key.equals("adminName2") || key.equals("adminName3")
					 * || key.equals("adminName4") || key.equals("adminName5")
					 * || key.equals("fcode") || key.equals("geonameId") ||
					 * key.equals("population") || key.equals("countryId") ||
					 * key.equals("adminId1") || key.equals("countryCode") ||
					 * key.equals("adminId2") || key.equals("toponymName") ||
					 * key.equals("fcl") || key.equals("continentCode")) { flag
					 * = true; continue; } else flag = false;
					 */
					break;
				case VALUE_STRING:
				case VALUE_NUMBER:
					/*System.out.println(event.toString() + " "
							+ parser.getString());*/
					value = parser.getString();
					record.put(key, value);
					// newGen.write(key, value);

					/* if (!flag) { */
					/*
					 * if(flag){
					 * 
					 * if(key.equals("adminCode2")){ writer.print(place + " | "
					 * + value + " | "); }
					 * 
					 * else if (key.equals("continentCode")) {
					 * writer.println(value + " | "); } else writer.print(value
					 * + " | ");
					 * 
					 * flag = false; }
					 */break;
				}

			}

			// closing the writer
			writer.close();

		} 
	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
