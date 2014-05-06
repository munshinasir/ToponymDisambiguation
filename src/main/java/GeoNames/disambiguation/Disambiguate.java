package GeoNames.disambiguation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import GeoNames.disambiguation.JSONProcessing;
import GeoNames.disambiguation.Basic;

public class Disambiguate {

	ArrayList<CoverageList> rank = new ArrayList<CoverageList>();
	ArrayList<Integer> maxList = new ArrayList<Integer>();
	ArrayList<String> discard = new ArrayList<String>();
	HashMap<String, Integer> codeCount = new HashMap<String, Integer>();
	HashMap<String, ArrayList<String>> coverageRank = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> colMap = new HashMap<String, ArrayList<String>>();

	public void disambiguate(String url) {

		JSONProcessing jsonP = new JSONProcessing();
		Basic obj = new Basic();

		try {
			// calls the JSONRequest to fetch data
			// obj.JSonRequest("http://data.cityofchicago.org/resource/4jy7-7m68.json");
			// obj.JSonRequest(url);
			obj.readFromFile();

			// check if the data is empty
			if (!obj.data.isEmpty()) {

				// adding candidate values
				for (String place : obj.data) {

					// fetching the values for the candidates - spatial column

					// adding the key value pair to candidates
					jsonP.JSon2File("http://api.geonames.org/searchJSON?q="
							+ place.replace(" ", "%20")
							+ "&maxRows=225&style=full&username=munshinasir",
							place, "data");

					// fetching the values for the other column candidates
					int index = obj.data.indexOf(place);
					String other = obj.data1.get(index);
					if (!other.equals("null")) {
						jsonP.JSon2File(
								"http://api.geonames.org/searchJSON?name_equals="
										+ other.replace(" ", "%20")
										+ "&country=US&style=full&username=munshinasir",
								place, "candidates");
					}

				}

				// fetching the codes and their counts
				colDisambiguate();

				// for each key validate its candidates compute the similarity
				for (String dataString : obj.data) {

					// working with one data point - eg: Fellowship Baptist
					// Church
					this.calculateCoverage(dataString);

				}

				// Nasir : Persisting with writing to rankList file
				PrintWriter writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(
										"/home/nasir/Documents/CS-586/Assignment-2/ranklist.txt",
										true)));

				for (CoverageList temp : rank) {

					writer.println(temp.data + " : " + temp.candidate + " : "
							+ temp.coverage);

				}

				writer.close();

				// Writing the list of max coverage
				for (String dataString : obj.data) {
					double topCoverage = 0.0;
					for (CoverageList temp : rank) {

						if (temp.data.contains(dataString)) {
							if (temp.coverage >= topCoverage) {
								topCoverage = temp.coverage;

							}
						}

					}

					for (CoverageList temp : rank) {

						/*
						 * CoverageList temp1 = new CoverageList(); temp1 =
						 * temp;
						 */

						if (temp.coverage == topCoverage
								&& temp.data.contains(dataString)) {
							System.out.println("Top Coverage for " + dataString
									+ " = " + topCoverage + " : " + temp.data
									+ " : " + temp.candidate);

							// Nasir : Adding the key,values to the HashMap for
							// disambiguous entries
							if (coverageRank.containsKey(temp.data)) {
								ArrayList<String> tempList = coverageRank
										.get(temp.data);
								if (!tempList.contains(temp.candidate)) {
									tempList.add(temp.candidate);
									coverageRank.put(temp.data, tempList);
								}
							} else {
								ArrayList<String> tempList = new ArrayList<String>();
								tempList.add(temp.candidate);
								coverageRank.put(temp.data, tempList);
							}

							// Nasir : Adding the key,values to HashMap to
							// reduce the set of entries which can be used to
							// fcodes
							if (colMap.containsKey(dataString)) {
								ArrayList<String> tempList = colMap
										.get(dataString);
								if (!tempList.contains(temp.data)) {
									tempList.add(temp.data);
									colMap.put(dataString, tempList);
								}
							} else {
								ArrayList<String> tempList = new ArrayList<String>();
								tempList.add(temp.data);
								colMap.put(dataString, tempList);
							}

						}
					}
				}

				System.out.println("coverageRank");
				// printing the HashMap for unambiguous values
				for (String s : coverageRank.keySet()) {
					System.out.println(s + " : "
							+ coverageRank.get(s).toString());
				}

				System.out.println("ColMap");
				// reducing the list by using the fcodes
				for (String s : colMap.keySet()) {
					System.out.println(s + " : " + colMap.get(s).toString());
				}

				// finding the top 3 of the codes
				maxList = getMax(codeCount);

				// reducing the set of values that are not required
				for (String s : colMap.keySet()) {

					ArrayList<String> tempList = colMap.get(s);
					if (tempList.size() > 1) {

						ArrayList<String> newArray = check(tempList);
						colMap.put(s, newArray);

						/*
						 * for(int i=0 ; i<tempList.size();i++){
						 * 
						 * String str = tempList.get(i);
						 * 
						 * //fetch the fcode String code = str.split("\\|")[5];
						 * 
						 * //fetch the code count for the code int fcount =
						 * codeCount.get(code);
						 * 
						 * //check if the fcode is contained
						 * if(!maxList.contains(fcount)){
						 * 
						 * discard.add(str); tempList.remove(str);
						 * if(tempList.size()>=1){ colMap.put(s, tempList); i--;
						 * } else break; }
						 * 
						 * 
						 * }
						 */

					}

				}

				// remove the required values from the key set of ambiguous
				// values
				/*
				 * for(String str : discard){
				 * 
				 * if(coverageRank.containsKey(str)){
				 * 
				 * coverageRank.remove(str);
				 * 
				 * }
				 * 
				 * }
				 */

				if (discard.size() >= 1) {
					System.out.println("Discard");
					for (String s : discard) {
						System.out.println(s);
					}
					System.out.println("\n\n");
				}

				// final result print
				System.out.println("coverageRank");
				// printing the HashMap for unambiguous values
				for (String s : coverageRank.keySet()) {
					if (!discard.contains(s))
						System.out.println(s + " : "
								+ coverageRank.get(s).toString());
				}
				System.out.println("\n\n");

				System.out.println("ColMap");
				// reducing the list by using the fcodes
				for (String s : colMap.keySet()) {

					System.out.println(s + " : " + colMap.get(s).toString());
				}
				System.out.println("\n\n");

			} else {
				System.out.println("No Data Fetched");
			}

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public void calculateCoverage(String dataString) throws IOException {

		// produce the similarity values

		// OPEN THE DATA FILE
		BufferedReader dataReader = new BufferedReader(new FileReader(
				"/home/nasir/Documents/CS-586/Assignment-2/data.txt"));
		String dataLine = null;
		// int lineCount = 0;
		while ((dataLine = dataReader.readLine()) != null) {
			/*
			 * lineCount++; System.out.println(lineCount);
			 */
			// check if the candidate is a part of the string
			if (dataLine.contains(dataString)) {

				// fetch the values

				String[] dataArgs = dataLine.split("\\|");

				// OPEN the candidate file
				BufferedReader canReader = new BufferedReader(
						new FileReader(
								"/home/nasir/Documents/CS-586/Assignment-2/candidates.txt"));
				String canLine = null;

				while ((canLine = canReader.readLine()) != null) {
					if (canLine.contains(dataString)) {

						// fetch the values

						String[] canArgs = canLine.split("\\|");

						int matchCount = 0;

						// check for value matches
						// int count1 = 0;

						/*
						 * for (String dat : dataArgs) { if (dat.isEmpty() ||
						 * count1 == 0) { count1++; continue; } int count2 = 0;
						 * for (String can : canArgs) {
						 * 
						 * if (can.isEmpty() || count2 == 0) { count2++;
						 * continue; } if (dat.equals(can)) matchCount++;
						 * 
						 * } }
						 */

						// The new matching function
						// trying out matching only the hierarchy
						for (int count1 = 1; count1 < dataArgs.length - 4; count1++) {

							if ((dataArgs[count1] != null)
									&& (canArgs[count1] != null)) {
								if (dataArgs[count1].isEmpty()
										|| canArgs[count1].isEmpty())
									continue;
								else {
									if (dataArgs[count1]
											.equals(canArgs[count1]))
										matchCount++;
								}
							}
						}

						int len = 0;

						if (dataArgs.length > canArgs.length)
							len = canArgs.length - 4;
						else
							len = dataArgs.length - 4;

						// calculate rank

						/*
						 * double coverageRank = (double) matchCount /
						 * ((dataArgs.length) * (canArgs.length));
						 */

						double coverageRank = (double) matchCount / len;

						// store in the list
						if (matchCount > 3) {
							// System.out.println("Rank is : " +
							// coverageRank*100 + " " +
							// canArgs[canArgs.length-2]);
							CoverageList temp = new CoverageList();
							temp.data = dataLine;
							temp.candidate = canLine;
							temp.coverage = coverageRank;
							rank.add(temp);
						}

					}

				}

			}

		}

	}

	public void colDisambiguate() throws IOException {

		int count = 1;
		String fClass;

		// Nasir : opening the data file
		BufferedReader dataReader = new BufferedReader(new FileReader(
				"/home/nasir/Documents/CS-586/Assignment-2/data.txt"));
		String dataLine = null;

		while ((dataLine = dataReader.readLine()) != null) {

			fClass = dataLine.split("\\|")[5];
			// System.out.println(fClass);
			if (fClass.length() >= 1) {
				if (codeCount.containsKey(fClass)) {

					codeCount.put(fClass, ((codeCount.get(fClass)) + 1));
					if (count < (codeCount.get(fClass)))
						count = codeCount.get(fClass);

				} else {
					codeCount.put(fClass, 1);
				}

			}
		}
		// end for

		// resolveTheCol(data);
		// to get the most frequent fclass
		/*
		 * for (Entry<String, Integer> entry : codeCount.entrySet()) { // if
		 * (count == entry.getValue()) { System.out.println(entry.getKey() +
		 * "   " + entry.getValue()); // } }
		 */
	}

	public ArrayList<Integer> getMax(HashMap<String, Integer> map) {

		ArrayList<Integer> maxList = new ArrayList<Integer>();

		int max1 = 0, max2 = 0, max3 = 0;

		for (String s : map.keySet()) {

			int value = map.get(s);

			if (value > max3) {

				if (value > max2) {

					if (value > max1) {

						max3 = max2;
						max2 = max1;
						max1 = value;

					}

					else {

						max3 = max2;
						max2 = value;

					}

				}

				else {

					max3 = value;
				}

			}

		}

		maxList.add(max1);
		maxList.add(max2);
		maxList.add(max3);

		return maxList;
	}

	public ArrayList<String> check(ArrayList<String> temp) {

		ArrayList<String> codes = new ArrayList<String>();
		ArrayList<String> tempKeep = new ArrayList<String>();

		for (String str : temp) {

			String code = str.split("\\|")[5];

			if (!codes.contains(code)) {
				codes.add(code);
			}

		}

		// check for the number of codes in temp
		// if !>1 then return temp : no need to modify
		// if > 1 then
		// : see if there is anycode that is there in maxList
		// if yes remove the codes from tempDiscard
		// if no return the entire list
		if (codes.size() > 1) {

			for (String str : codes) {

				int fcount = codeCount.get(str);

				// check if the fcode is contained
				if (maxList.contains(fcount)) {

					tempKeep.add(str);

				}

			}

			if (tempKeep.size() >= 1) {

				// remove all other codes except tempKeep
				for (int i = 0; i < temp.size(); i++) {

					String str = temp.get(i);

					String code = str.split("\\|")[5];

					if (!tempKeep.contains(code)) {
						temp.remove(str);
						if (temp.size() >= 1) {

							discard.add(str);
							i--;
							continue;
						} else {

							return null;

						}

					}

				}

			} else
				return temp;

		} else
			return temp;

		return temp;

	}

	public class CoverageList {

		double coverage;
		String data;
		String candidate;

		public CoverageList() {

			data = new String();
			candidate = new String();

		}

	}

}
