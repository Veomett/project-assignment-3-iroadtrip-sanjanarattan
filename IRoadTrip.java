import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * IRoadTrip
 */
public class IRoadTrip {
    private HashMap<String, HashMap<String, Integer>> Hash_cf = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, String> codes = new HashMap<String, String>();
    private HashMap<String, String> aliasNames= new HashMap<String, String>();
    HashMap<String, HashMap<String, Integer>> finalWorld = new HashMap<String, HashMap<String, Integer>>();


    public IRoadTrip(String[] args) {
        modify();
        readCF("capdist.csv");
        readSF("state_name.tsv");
        readBF("borders.txt");
    }

    public HashMap<String, HashMap<String, Integer>> getfinalWorld() {
        return finalWorld;
    }

    private void modify() {
        aliasNames.put("Bahamas, The", "Bahamas");
        aliasNames.put("Bosnia-Herzegovina", "Bosnia and Herzegovina");
        aliasNames.put("Botswana.", "Botswana");
        aliasNames.put("Burkina Faso (Upper Volta)", "Burkina Faso");
        aliasNames.put("Belarus (Byelorussia)", "Belarus");
        aliasNames.put("Congo, Democratic Republic of (Zaire)", "Democratic Republic of the Congo");
        aliasNames.put("Congo, Democratic Republic of the", "Democratic Republic of the Congo");
        aliasNames.put("Congo, Republic of the", "Republic of the Congo");
        aliasNames.put("Congo", "Republic of the Congo");
        aliasNames.put("Cambodia (Kampuchea)", "Cambodia");
        aliasNames.put("East Timor", "Timor-Leste");
        aliasNames.put("German Federal Republic", "Germany");
        aliasNames.put("Greenland).", "Greenland");
        aliasNames.put("Gambia, The", "The Gambia");
        aliasNames.put("Gambia", "The Gambia");
        aliasNames.put("Italy.", "Italy");
        aliasNames.put("Italy/Sardinia", "Italy");
        aliasNames.put("Iran (Persia)", "Iran");
        aliasNames.put("Korea, North", "North Korea");
        aliasNames.put("Korea, People's Republic of", "North Korea");
        aliasNames.put("Korea, South", "South Korea");
        aliasNames.put("Korea, Republic of", "South Korea");
        aliasNames.put("Kyrgyz Republic", "Kyrgyzstan");
        aliasNames.put("Macedonia (Former Yugoslav Republic of)", "Macedonia");
        aliasNames.put("Macedonia", "North Macedonia");
        aliasNames.put("Macedonia (Former Yugoslav Republic of)", "North Macedonia");
        aliasNames.put("Myanmar (Burma)", "Burma");
        aliasNames.put("Russia (Soviet Union)", "Russia");
        aliasNames.put("Sri Lanka (Ceylon)", "Sri Lanka");
        aliasNames.put("Turkey (Turkiye)", "Turkey");
        aliasNames.put("Turkey (Ottoman Empire)", "Turkey");
        aliasNames.put("Tanzania/Tanganyika", "Tanzania");
        aliasNames.put("US", "United States of America");
        aliasNames.put("United States", "United States of America");
        aliasNames.put("UK", "United Kingdom");
        aliasNames.put("UAE", "United Arab Emirates");
        aliasNames.put("Vietnam, Democratic Republic of", "Vietnam");
        aliasNames.put("Yemen (Arab Republic of Yemen)", "Yemen");
        aliasNames.put("Zambia.", "Zambia");
        aliasNames.put("Zimbabwe (Rhodesia)", "Zimbabwe");    
    }

    public void readCF(String filename) {
        boolean lineOne = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String parseCity;
            while ((parseCity = reader.readLine()) != null) {
                if (lineOne) {
                    lineOne = false;
                    continue; 
                }
                String[] Country = parseCity.split(",");
                String country1 = Country[1].trim();
                String country2 = Country[3].trim();
                Integer distance = Integer.parseInt(Country[4].trim());
                updateDistance(country1, country2, distance);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }   

        // printHashMap(Hash_cf, "Hash_cf");

    }

    private void updateDistance(String country1, String country2, Integer distance) {
        Hash_cf.computeIfAbsent(country1, k -> new HashMap<>()).put(country2, distance);
    }

     public void readSF(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Date> latestDates = new HashMap<>(); 

            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t");
                if (values.length >= 5) {
                    String countryId = values[1].trim();
                    String countryName = values[2].trim();
                    Date endDate;

                    try {
                        endDate = dateFormat.parse(values[4].trim());
                    } catch (ParseException e) {
                        continue;
                    }

                    if (!latestDates.containsKey(countryId) || endDate.after(latestDates.get(countryId))) {
                        latestDates.put(countryId, endDate);
                        codes.put(countryId, countryName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
       // printHashMap(codes, "Codes");

    }

    public void readBF(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String countryGiven;
            while ((countryGiven = reader.readLine()) != null) {
                HashMap<String, Integer> neighbours = new HashMap<>();
                String[] countryStart = countryGiven.split("=");
                String countryStartFinal = countryStart[0].trim();
    
                if (aliasNames.containsKey(countryStartFinal)) {
                    countryStartFinal = aliasNames.get(countryStartFinal);
                }
    
                if (countryStart.length > 1) {
                    String[] neighboursArray = countryStart[1].split(";");
                    for (String neighbour : neighboursArray) {
                        String[] neighbourWithDist = neighbour.trim().split("\\s+", 2);
                        if (neighbourWithDist.length >= 2) {
                            String[] parts = neighbourWithDist[0].split(("(?=(?:\\S+\\s+\\S+\\s+\\S+$))"));
                            String neighbourName = parts[parts.length - 1].trim();
                            String distanceStr = neighbourWithDist[1].trim();
                            int distance = getDistance(distanceStr);
                            System.out.println(neighbourName);
                            neighbours.put(neighbourName, distance);
                        }
                    }
    
                    finalWorld.put(countryStartFinal, neighbours);
                }
            }
            // printHashMap(finalWorld, "finalWorld");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    private int getDistance(String distanceStr) throws NumberFormatException {
        try {
            // Remove non-digit characters (including spaces) and "km" if present, then parse as an integer
            distanceStr = distanceStr.replaceAll("[^0-9]", "").replace("km", "").trim();
            return Integer.parseInt(distanceStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public void acceptUserInput() {
        try {
            try (Scanner scanner = new Scanner(System.in)) {
            modify();
                while(true){
                System.out.print("Enter the name of the first country (type EXIT to quit): ");
                String startCountry = scanner.nextLine().trim();
                if(aliasNames.containsKey(startCountry)){
                    startCountry = aliasNames.get(startCountry);
                }
                if(startCountry.equals("EXIT")){
                    break;
                } else if (!finalWorld.containsKey(startCountry)){
                    System.out.println("Invalid country name. Please enter a valid country name.");
                    continue;
                }

                System.out.print("Enter the name of the second country (type EXIT to quit): ");
                String endCountry = scanner.nextLine().trim();
                if(aliasNames.containsKey(endCountry)){
                    endCountry = aliasNames.get(endCountry);
                }
                if(endCountry.equals("EXIT")){
                    break;
                }
                if (!finalWorld.containsKey(endCountry)){
                    System.out.println("Invalid country name. Please enter a valid country name.");
                    continue;
                }
        
                preprocessFinalWorld();
                findPath(finalWorld, startCountry, endCountry);
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preprocessFinalWorld() {
        for (String country : new HashSet<>(finalWorld.keySet())) {
            if (aliasNames.containsKey(country)) {
                String fullName = aliasNames.get(country);
                finalWorld.put(fullName, finalWorld.get(country));
                finalWorld.remove(country);
            }
    
            HashMap<String, Integer> neighbors = finalWorld.get(country);
            for (String neighbor : new HashSet<>(neighbors.keySet())) {
                if (aliasNames.containsKey(neighbor)) {
                    String fullNeighborName = aliasNames.get(neighbor);
                    neighbors.put(fullNeighborName, neighbors.get(neighbor));
                    neighbors.remove(neighbor);
                }
            }
        }
    }
    

    public void findPath(HashMap<String, HashMap<String, Integer>> finalWorld, String start, String end) {
        HashMap<String, Integer> distances = new HashMap<>();
        HashMap<String, String> predecessors = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        HashSet<String> visited = new HashSet<>();
    
       // System.out.println("Debug: Final World Map: " + finalWorld); // Debug
    
        for (String country : finalWorld.keySet()) {
            distances.put(country, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        queue.add(new AbstractMap.SimpleEntry<>(start, 0));
    
        while (!queue.isEmpty()) {
            String current = queue.poll().getKey();
        
            if (aliasNames.containsKey(current)) {
                current = aliasNames.get(current);
            }
        
            if (visited.contains(current)) continue;
            visited.add(current);
        
            if (!finalWorld.containsKey(current)) continue;
        
            for (Map.Entry<String, Integer> neighbor : finalWorld.get(current).entrySet()) {
                String neighborName = neighbor.getKey();
        
                if (aliasNames.containsKey(neighborName)) {
                    neighborName = aliasNames.get(neighborName);
                }
        
                if (neighbor.getValue() < 0) continue; 
        
                int newDist = distances.get(current) + neighbor.getValue();
                if (newDist < distances.getOrDefault(neighborName, Integer.MAX_VALUE)) {
                    distances.put(neighborName, newDist);
                    predecessors.put(neighborName, current);
                    queue.add(new AbstractMap.SimpleEntry<>(neighborName, newDist));
                }
            }
        }
        
        
        printRoute(predecessors, start, end, finalWorld);
    }
    
    
    private void printRoute(HashMap<String, String> predecessors, String start, String end, HashMap<String, HashMap<String, Integer>> finalWorld) {
        if (!predecessors.containsKey(end)) {
            System.out.println("No route found from " + start + " to " + end);
            return;
        }
    
        List<String> path = new ArrayList<>();
        for (String at = end; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
    
        System.out.println("Route from " + start + " to " + end + ":");
        String from = path.get(0);
        if (aliasNames.containsKey(from)) {
            from = aliasNames.get(from);
        }
    
        for (int i = 1; i < path.size(); i++) {
            String to = path.get(i);
            if (aliasNames.containsKey(to)) {
                to = aliasNames.get(to);
            }
    
            // System.out.println("Debug: From: " + from + ", To: " + to); // Debugging
    
            if (finalWorld.containsKey(from) && finalWorld.get(from).containsKey(to)) {
                int distance = finalWorld.get(from).get(to);
                System.out.println("* " + from + " --> " + to + " (" + distance + " km.)");
            } else {
                System.out.println("Distance not found between: " + from + " and " + to);
            }
            from = to;
        }
    }
    
    
    

    
    
    private static void printHashMap(HashMap<?, ?> hashMap, String name) {
        System.out.println(name + ":");
        for (Map.Entry<?, ?> entry : hashMap.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
        System.out.println();
    }

    

    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);
        a3.acceptUserInput();
       
    }


    
   
}
