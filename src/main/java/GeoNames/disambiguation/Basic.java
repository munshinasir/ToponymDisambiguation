package GeoNames.disambiguation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class Basic {

	ArrayList<String> data;
	HashMap<String, ArrayList<String>> candidateValues;
	ArrayList<String> data1;
	HashMap<String, ArrayList<String>> candidateValues1;
	ArrayList<RankList> rank;
	/*
							 * HashMap<String, String> newCandidateValues;
							 * HashMap<String, String> newCandidateValues1;
							 */
	HashMap<String, Integer> Alldata = new HashMap<String, Integer>();

	public Basic() {

		data = new ArrayList<String>();
		data1 = new ArrayList<String>();
		candidateValues = new HashMap<String, ArrayList<String>>();
		candidateValues1 = new HashMap<String, ArrayList<String>>();
		rank = new ArrayList<RankList>();
		/*
		 * newCandidateValues = new HashMap<String, String>();
		 * newCandidateValues1= new HashMap<String, String>();
		 */
	}

	public String httpGet(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();

		conn.disconnect();
		return sb.toString();
	}

	public void JSonRequest(String urlString) throws IOException {
		URL url = new URL(urlString);
		try (InputStream is = url.openStream();
				JsonReader rdr = Json.createReader(is)) {

			JsonArray results = rdr.readArray();
			for (int i = 0; i < 10; i++) {
				JsonObject result = (JsonObject) results.get(i);
				// System.out.print(result.toString());
				data.add(result.getString("event"));
				data1.add(result.getString("community_area_name"));
				// data1.add(result.getString("city"));
			}
		}
	}

	public void readFromFile() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(
				"/home/nasir/Documents/CS-586/Assignment-2/flushots2.txt"));

		String line = null;
		int count = 0;

		while ((line = reader.readLine()) != null) {

			System.out.println(" Line : " + line);
			String[] a = line.toString().split("\\t");
			//if (count < 10) {
				data.add(a[0]);
				count++;
				if (a.length == 3)
					data1.add(a[2]);
				else
					data1.add("null");
			//}
		}

		reader.close();

	}

	public ArrayList<String> JSonRequest2(String urlString) throws IOException {
		// public String JSonRequest2(String urlString) throws IOException {
		ArrayList<String> temp = new ArrayList<String>();
		// String arrString = new String();
		URL url = new URL(urlString);
		try {
			InputStream is = url.openStream();
			JsonReader rdr = Json.createReader(is);
			JsonObject obj = rdr.readObject();
			Collection<JsonValue> newMap = obj.values();

			// we need to ignore the first value
			Object[] arrJson = newMap.toArray();
			JsonValue jv = (JsonValue) arrJson[1];
			// arrString = jv.toString();

			// fecthing the remaining records
			String[] arrString = jv.toString().split("}");

			for (String str : arrString) {
				temp.add(str);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// return arrString;
		return temp;
	}

	public void disambiguate(String url) {

		try {
			// calls the JSONRequest to fetch data
			this.JSonRequest("http://data.cityofchicago.org/resource/4jy7-7m68.json");

			// check if the data is empty
			if (!data.isEmpty()) {

				// adding candidate values
				for (String place : data) {

					// fetching the values for the candidates - spatial column

					// adding the key value pair to candidates
					// candidateValues.put(place,
					// this.JSonRequest2("http://api.geonames.org/searchJSON?name_equals="+
					// place.replace(" ", "%20") +
					// "&maxRows=100&username=munshinasir"));

					// Abeer
					candidateValues
							.put(place,
									this.JSonRequest2("http://api.geonames.org/searchJSON?name_equals="
											+ place.replace(" ", "%20")
											+ "&maxRows=200&style=full&username=munshinasir"));

					// newCandidateValues.put(place,
					// this.JSonRequest2("http://api.geonames.org/searchJSON?name_equals="+
					// place.replace(" ", "%20") +
					// "&maxRows=100&username=munshinasir"));
					// temp.clear();

					// fetching the values for the other column candidates
					int index = data.indexOf(place);
					String other = data1.get(index);
					candidateValues1
							.put(place,
									this.JSonRequest2("http://api.geonames.org/searchJSON?name_equals="
											+ other
											+ "&maxRows=100&username=munshinasir"));
					// newCandidateValues1.put(place,
					// this.JSonRequest2("http://api.geonames.org/searchJSON?name_equals="+
					// other+ "&maxRows=100&username=munshinasir"));
					// temp.clear();

				}

				// for each key validate its candidates compute the similarity
				for (String candidate : candidateValues.keySet()) {

					// working with one data point - eg: Fellowship Baptist
					// Church
					ArrayList<String> c = candidateValues.get(candidate);
					ArrayList<String> s = candidateValues1.get(candidate);

					/*
					 * String c = newCandidateValues.get(candidate); String s =
					 * newCandidateValues1.get(candidate);
					 */
					this.rank(candidate, c, s);
					// this.rank(candidate, c, s);

				}
				
						

				// printout the list of 1s
				for (RankList temp : rank) {

					if (temp.rank == 1) {
						System.out.println("Data" + temp.data);
						System.out.println("Candidate " + temp.candidate);
					}

				}
				
				
				// Abeer : col disambiguate
				for (String colCandidate : candidateValues.keySet()) {

					ArrayList<String> col1 = candidateValues.get(colCandidate);
				    colDisambiguate(col1);
				
				
				}
								
				
			    for (Entry<String, Integer> entry : Alldata.entrySet()) {
				   System.out.println(entry.getKey() + ", " + entry.getValue());
				}
				
				

			} else {
				System.out.println("No Data Fetched");
			}

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public void rank(String candidate, ArrayList<String> a, ArrayList<String> b) {

		// produce the similarity values
		for (String d : a) {

			// fetch the adminCode1
			String admCode = this.fetchCode("adminCode1", d);

			if (admCode.length() >= 1) {

				for (String c : b) {

					// fetch the adminCode1
					String admCode1 = this.fetchCode("adminCode1", c);

					if (admCode1.length() >= 1) {

						// match the admin code
						if (admCode.equals(admCode1)) {
							RankList temp = new RankList();
							temp.setRank(1);
							temp.setCandidate(c);
							temp.setData(d);
							rank.add(temp);
						} else {
							RankList temp = new RankList();
							temp.setRank(0);
							temp.setCandidate(c);
							temp.setData(d);
							rank.add(temp);
						}
					}
				}

			}
		}

	}

	// /try finding a better way to do this..
	// /think converting it back to json is a better idea
	public String fetchCode(String codeString, String value) {

		// System.out.println("The initial string" + value);
		String[] arr = value.split(codeString);
		if (arr.length > 1) {
			String[] arr2 = arr[1].split(",");

			if (arr2.length >= 1) {
				String[] arr3 = arr2[0].split("\"");

				if (arr3.length >= 3) {
					// System.out.println("The fetched code " + arr3[2]);
					return arr3[2];
				}

			}

		}

		return "";

	}

	public class RankList {

		int rank;
		String data;
		String candidate;

		public RankList() {

			data = new String();
			candidate = new String();

		}

		public void setData(String data) {
			this.data = data;
		}

		public void setCandidate(String candidate) {
			this.candidate = candidate;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

	}

	public void colDisambiguate(ArrayList<String> cellvalues) {

		HashMap<String, Integer> data = new HashMap<String, Integer>();
		int count = 1;
		String fClass;
		for (String string : cellvalues) {
			fClass = this.fetchCode("fcode", string);
			System.out.println(fClass);
			if (fClass.length() >= 1) {
				if (data.containsKey(fClass)) {

					data.put(fClass, ((data.get(fClass)) + 1));
					if (count < (data.get(fClass)))
						count = data.get(fClass);

				} else {
					data.put(fClass, 1);
				}

			}
		}// end for
		for (Entry<String, Integer> entry : data.entrySet()) {

			fClass = entry.getKey();
			if (Alldata.containsKey(fClass)) {

				Alldata.put(fClass, ((Alldata.get(fClass)) + entry.getValue()));

			} else {
				Alldata.put(fClass, entry.getValue());
			}
		}// end for

		// resolveTheCol(data);
		// to get the most frequent fclass
		for (Entry<String, Integer> entry : data.entrySet()) {
			// if (count == entry.getValue()) {
			System.out.println(entry.getKey() + "   " + entry.getValue());
			// }
		}

	}

}
