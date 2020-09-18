//Christopher Spradlin | CSCI-5330 Boruvka's Algorithm
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

class Main{
  public static void main(String[] args) {
    Graph g = new Graph();

    //Test Graph 1: MST Edges = [1, 2, 5, 4, 3, 6]
    g.putNode(1, 6, 5);
    g.putNode(1, 8, 9, 2);
    g.putNode(2, 10);
    g.putNode(7, 6, 8, 4);
    g.putNode(5, 7);
    g.putNode(4, 3);
    g.putNode(3, 9, 10);

    System.out.println("Edges that form the MST: " + g.createMST());

    //g.printGraph();

  }
}

//Assumes graph has distinct edge weights and that the graph is a complete graph (no loose substrees)

class Graph {

  //Graph is represented with two HashMap of Integers to Array List Integers of Nodes and Edges
  private HashMap<Integer, ArrayList<Integer>> nodes;
  private HashMap<Integer, ArrayList<Integer>> edges;
  
  //Variable collectionColor keeps track of subtress and coloring of connected nodes in order to collapse later.
  private HashMap<Integer, Integer> collectionColor;

  //Initiate Graph and private variables
  public Graph() {
    nodes = new HashMap<Integer, ArrayList<Integer>>();
    edges = new HashMap<Integer, ArrayList<Integer>>();
    collectionColor = new HashMap<Integer, Integer>();
  }

  //Graph forming method, keys to nodes are placed by the algorithm and keys to the edges are made through their weights.
  //Edge HashMap and Node HashMap are updated simultaneously to keep in-sync.
  public void putNode(int... edges) {
    collectionColor.put(nodes.size(), nodes.size());

    ArrayList<Integer> edgesList = new ArrayList<Integer>();
    for (int i = 0; i < edges.length; i++)
      edgesList.add(edges[i]);
    nodes.put(nodes.size(), edgesList);

    for (int i = 0; i < edges.length; i++) {
      ArrayList<Integer> nodeList = new ArrayList<Integer>();
      nodeList.add(nodes.size()-1);
      //Update edges to add following nodes that add the corresponding edes.
      this.edges.merge(edges[i], nodeList, (c, n) -> {
        for(int j=0; j<n.size(); j++)
          c.add(n.get(j));
        return c;
      });
    }
  }

  //Calculate the minimum edge a node has and return it's weight key value
  public int getMinEdge(int currNode){
    ArrayList<Integer> currEdges = nodes.get(currNode);
    int minEdge = Integer.MAX_VALUE;
    for(int i=0; i<currEdges.size(); i++)
      if(currEdges.get(i)<minEdge) minEdge = currEdges.get(i);
    return minEdge;
  }
  //Set the color of the node to the minimum color, either itself or the corresponding sister node to the minEdge
  public void setMinimumColor(int currNode){
    int minEdge = getMinEdge(currNode);
    ArrayList<Integer> minEdgeNodes = edges.get(minEdge);
    int minNodeColor = Integer.MAX_VALUE;
    for(int i=0; i<minEdgeNodes.size(); i++){
      if(collectionColor.get(minEdgeNodes.get(i))<minNodeColor) minNodeColor = collectionColor.get(minEdgeNodes.get(i));
      //System.out.println("currNode:" + currNode + " minEdgeNode: " + minEdgeNodes.get(i) + " minNodeColor: " + minNodeColor);
    }
    for(int i=0; i<minEdgeNodes.size(); i++){
      collectionColor.put(minEdgeNodes.get(i), minNodeColor);
    }
  }

  //Calculate the least common edge between two nodes
  public int getLeastCommonEdge(int node1, int node2){
    ArrayList<Integer> node1Edges = nodes.get(node1);
    ArrayList<Integer> node2Edges = nodes.get(node2);
    int leastEdge = Integer.MAX_VALUE;
    for(int i=0; i<node1Edges.size(); i++)
      for(int j=0; j<node2Edges.size(); j++){
        if(node1Edges.get(i)==node2Edges.get(j)){
          if(node1Edges.get(i)<leastEdge){
            leastEdge = node1Edges.get(i);
          }
        }
      }
    return leastEdge;
  }
  //Combine the higher color value node into the lower color value node over the minimized common edge
  //This is done by replacing the removed Node from all edge lists with that Node and replacing that value with the node that is being collapsed into
  //Edge is then removed from the edge HashMap
  public int collapseNode(int nodeToRemove){
    int collapseToNode = collectionColor.get(nodeToRemove);
    int commonEdge = getLeastCommonEdge(nodeToRemove, collapseToNode);
    edges.remove(commonEdge);
    ArrayList<Integer> toRemove = new ArrayList<Integer>();
    toRemove.add(commonEdge);
    ArrayList<Integer> ntr_edges = nodes.get(nodeToRemove);
    ntr_edges.removeAll(toRemove);
    ArrayList<Integer> ctn_edges = nodes.get(collapseToNode);
    ctn_edges.removeAll(toRemove);
    ctn_edges.addAll(ntr_edges);

    for(int i=0; i<ntr_edges.size(); i++){
      int updateEdge = ntr_edges.get(i);
      ArrayList<Integer> nodesFromEdge = edges.get(updateEdge);
      for(int j=0; j<nodesFromEdge.size(); j++){
        if(nodesFromEdge.get(j)==nodeToRemove){
          nodesFromEdge.set(j, collapseToNode);
        }
      }
    }

    nodes.remove(nodeToRemove);

    System.out.println("Collapse Node: " + nodeToRemove + " into Node: " + collapseToNode);
    System.out.println("Remove Edge: " + commonEdge + " | " + "ctn:" + ctn_edges + " ntr: " + ntr_edges);
    System.out.println("Add Edge to MST: " + commonEdge);
    return commonEdge;
  }
  //Run the collapse function which loops through every node that needs to be collapsed and collapse that node into it's corresponding collection color
  public ArrayList<Integer> runCollapse(){
    ArrayList<Integer> keys = new ArrayList<Integer>();
    ArrayList<Integer> MSTedges = new ArrayList<Integer>();
    for(Integer i : nodes.keySet()){
      keys.add(i);
    }
    for(int i=0; i<keys.size(); i++){
      if(keys.get(i)!=collectionColor.get(keys.get(i))){
        MSTedges.add(collapseNode(keys.get(i)));
      }
    }
    return MSTedges;
  }
  //Update collection colors to be ready for the next iteration
  public void fixCollections(){
    ArrayList<Integer> keys = new ArrayList<Integer>();
    collectionColor.forEach((k,v)->{
      keys.add(k);
    });
    for(int i=0; i<keys.size(); i++){
      if(collectionColor.get(keys.get(i))!=keys.get(i))
        collectionColor.remove(keys.get(i));
    }
  }

  //Runs the program to completion by watching until the number of colors within the graph is 1
  public ArrayList<Integer> createMST(){
    ArrayList<Integer> MST_edges = new ArrayList<Integer>();
    while(collectionColor.size()>1){
      for(Integer i : nodes.keySet())
        setMinimumColor(i);


      MST_edges.addAll(runCollapse());
      fixCollections();
    }
    return MST_edges;
  }

  //Display every node and edge within both the Edge HashMap and Node HashMap
  public void printGraph() {
    nodes.forEach((k,a) -> {
      System.out.println("Node Key: " + k + " | Value:" + a);
    });
    edges.forEach((k,a) -> {
      System.out.println("Edge Key: " + k + " | Value:" + a);
    });
  }


}