import java.util.*;
import java.io.*;
public class App {

    //d[i] = outdegree of node i
    static HashMap<String, Integer> d = new HashMap();
    //adjList[i] = list of nodes i has an edge to.
    static HashMap<String, ArrayList<String>> adjList = new HashMap();
    // initial array of page rank scores
    static HashMap<Integer, HashMap<String, Double>> scores = new HashMap<>();
    static HashMap<String, Double> queryResults = new HashMap<>();

    static HashMap<String, Double> transitionmatrix = new HashMap();
    static ArrayList<String> sinkNodes = new ArrayList<>();


    static HashMap<String, ArrayList<String>> pointsToMe = new HashMap();


    static void buildGraph(ArrayList<String> nodes, ArrayList<String> edges) {
        for (String node : nodes) {
            adjList.put(node, new ArrayList<>());
            pointsToMe.put(node, new ArrayList<>());

        }
        for (String edge : edges) {
            String[] str = edge.split(" ");
            String firstnodeindex = str[0];
            String secondnodeindex = str[1];
            String firstnode = nodes.get(Integer.parseInt(firstnodeindex));
            String secondnode = nodes.get(Integer.parseInt(secondnodeindex));
            adjList.get(firstnode).add(secondnode);

            pointsToMe.get(secondnode).add(firstnode);
        }
        //set value of d[i] to number of outgoing edges from node i
        for (String node : adjList.keySet()) {
            d.put(node, adjList.get(node).size());
            if (adjList.get(node).size() == 0) {
                sinkNodes.add(node);
            }
        }

        

    }
    static void generateMatrix() {
        int n = adjList.keySet().size();
        for (String nodei : adjList.keySet()) {
            for (String nodej : adjList.keySet()) {
                //if (nodei.equals(nodej)) continue;
                if (adjList.get(nodei).contains(nodej)) {
                    transitionmatrix.put(nodei + nodej, .85/d.get(nodei) + .15/n);
                }
                else if (!adjList.get(nodei).contains(nodej) && d.get(nodei) > 0) {
                    transitionmatrix.put(nodei + nodej, .15/n);
                }
                
                else if (d.get(nodei) == 0) {
                    transitionmatrix.put(nodei + nodej, 1.0/n);
                }
                
            }
        }
        //System.out.println("N = " + n);
        //System.out.print("Transition Matrix\n" + p);
    }
    static boolean underTreshold(int t, double threshold) {
        //int n = scores.get(t).size();
        double sum = 0;
        for (String key : scores.get(t).keySet()) {
            sum += Math.abs(scores.get(t).get(key) - scores.get(t-1).get(key));
        }
        sum = sum/2;
        if (sum < threshold) return true;
        return false;


    }
    //You  will  need  to  do  some  pre-processing  to  determine,  for  each  node,  
    //which  other  nodes  point  to  it,and what their outdegrees are.  When doing this, 
    //you should only scan through the edges, not a nestedO(n2)  for  loop  over  the  nodes.   
    //Dictionaries/HashMaps  might  be  useful  for  this.   Then  think  abouthow  you  can  simplify  
    //your  calculation  of  equation  1.   It  may  be  helpful  to  think  separately  about adding the 
    //contributions to scores from edges (the 0.85/diterms) and from the random teleportation. If you get stuck on this question, 
    //you can still try the subsequent questions with a more straight forward implementation.

    //BROKEN IMPLEMENTATION IN O(N) TIME

    static void getEfficientScores(double threshold) {
        double nodecount = adjList.keySet().size();
        double sinknodecount = sinkNodes.size();
        System.out.println("number of nodes = " + nodecount);

        scores.put(0, new HashMap<String, Double>());

        for (String node : adjList.keySet()) {
            scores.get(0).put(node, 1.0 / (double)adjList.keySet().size());
        }
        for (int t = 1; t <= 1; t++) {
            
            scores.put(t, new HashMap<String, Double>());
            //String node1 = "A";
            for (String node1 : adjList.keySet()) {
                System.out.println("Updating node: " + node1 + " old value = " + scores.get(t-1).get(node1));
                                
                double notedgenotsinkcount = nodecount - sinknodecount - pointsToMe.get(node1).size();
                //System.out.print(notedgenotsinkcount);
                double newValue = 0.0;
                //Case 1: for every node pointing to node1, we add the probability of transitioning to node1
                
                for (String nodePointingToNode1 : pointsToMe.get(node1)) {
                        //System.out.println("node pointing to " + node1 + " = " + nodePointingToNode1 + " degree = " + d.get(nodePointingToNode1));
                        double sum = .85/d.get(nodePointingToNode1) + .15/nodecount;
                        System.out.println("sum = " + sum);

                        //System.out.print("test" + scores.get(t-1).get(nodePointingToNode1));
                        double product = sum * scores.get(t-1).get(nodePointingToNode1);
                        System.out.println("product = " + product);
                        newValue += product;
                }
                
                System.out.println("After case 1, newvalue =  " + newValue);
                //case2
                //for all nodes not pointing to A with degree > 0, we add .15/n to probability
                newValue+= notedgenotsinkcount * .15/nodecount * scores.get(t-1).get(node1);

                System.out.println("after case 2, newvalue = " + newValue);
                // Case 3
                //for every sinkNode, we add 1/n to the probability of transitioning to node1
                newValue += sinknodecount * 1/nodecount  * scores.get(t-1).get(node1);
                //newValue += sinknodecount * 1/nodecount * scores.get(t-1).get(node1);

                System.out.println("after case 3, newvalue = " + newValue);


                System.out.println("final value updating = " + newValue);
                scores.get(t).put(node1, newValue);

            }
            System.out.println("end of iteration");
            if (underTreshold(t, threshold)) {
                System.out.println("converged at iteration: " + t);
                break;
            }
            
        }
    }
    //WORKING IMPLEMENTATION IN O(N^2) TIME
    static void getScores(double threshold) {
        scores.put(0, new HashMap<String, Double>());
        //initialize scores[t=0][nodej] to 1/n 
        for (String node : adjList.keySet()) scores.get(0).put(node, 1 / (double)adjList.keySet().size());
        // iterating until TVD < threshold
        for (int t = 1; t <= 1; t++) {
            scores.put(t, new HashMap<String, Double>());
            for (String nodej : adjList.keySet()) {
                double newvalue = 0.0;
                for (String nodei : adjList.keySet()) {
                    newvalue += transitionmatrix.get(nodei + nodej) * scores.get(t-1).get(nodei);
                }
                scores.get(t).put(nodej, newvalue);
            }
            //System.out.println("end of iteration");
            if (underTreshold(t, threshold)) {
                System.out.println("Converged at iteration: " + t);
                break;
            }
        }
        
    }
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list = new LinkedList<Map.Entry<String, Double> >(hm.entrySet());
        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) temp.put(aa.getKey(), aa.getValue());
        return temp;
    }
    static void printScores() {
        int lastindex = scores.size()-1;
        //sorting and printing results
        
        for (String name : scores.get(lastindex).keySet()) {
            System.out.println(name + ": " + scores.get(lastindex).get(name));
        }
        /*
        Map<String, Double> sortedmap = sortByValue(scores.get(lastindex));
        for (String name : sortedmap.keySet()) System.out.println(name + ": " + sortedmap.get(name));
        */
        //n = sum of probability vector, should sum to 1
        double n = 0.0;
        for (String s : scores.get(lastindex).keySet()) n+= scores.get(lastindex).get(s);
        
        System.out.print(n);
    }
    static void searchResults(String query) { 
        System.out.println("\nPrinting search results");

        int lastindex = scores.size()-1;
        for (String name : scores.get(lastindex).keySet()) {
            if (name.contains(query)) {
                System.out.println(name + ": " + scores.get(lastindex).get(name));
            }

        }

    }
    public static void main(String[] args) throws FileNotFoundException{
        double threshold = .001;
        String query = "Jim";

        //GET NODES FROM FILE
        String nodefile = "test_nodes.txt";
        ArrayList<String> nodes = new ArrayList<>();
        try (Scanner s = new Scanner(new FileReader(nodefile))) {
            while (s.hasNext()) nodes.add(s.nextLine());   
        }
        //GET EDGES FROM FILE
        String edgefile = "test_edges.txt";
        ArrayList<String> edges = new ArrayList<>();
        try (Scanner s = new Scanner(new FileReader(edgefile))) {
            while (s.hasNext()) edges.add(s.nextLine());
        }

        buildGraph(nodes, edges);
        generateMatrix();
        getEfficientScores(threshold);

        printScores();
        //searchResults(query);









    }
}