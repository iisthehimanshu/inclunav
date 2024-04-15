package com.inclunav.iwayplus.path_search;

import android.graphics.Point;
import android.util.Log;

import com.inclunav.iwayplus.activities.Navigation;
import com.inclunav.iwayplus.pdr.FloorObj;
import com.inclunav.iwayplus.pdr.Node;
import com.inclunav.iwayplus.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//ASSUMING WE HAVE SAME NUMBER OF FLOOR CONNECTORS

public class ConnectorGraph {
    PathOption ps;
    ArrayList<String> commonlift = new ArrayList<>();
    private PathSearcher currPS;

    class Vertex {
        final private String name; //name of this vertex
        private String floor; //floor of this vertex

        private Vertex(String name,String floor) {
            this.name = name;
            this.floor = floor;
        }

//        public String getFloorName() {
//            return name;
//        }
        String getFloor(){
            return floor;
        }
        String getFloorNameCombo(){
            return floor+"-"+name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class Edge  {
        private final Vertex source;
        private final Vertex destination;
        private final double weight;
        private ArrayList<Point> edgePath; //edgePath is the shortest path joining the points of that edge

        private Edge(Vertex source, Vertex destination, double weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        private void assignEdgePath(ArrayList<Point> path){
            edgePath  = path;
        }

        private Vertex getDestination() {
            return destination;
        }

        private Vertex getSource() {
            return source;
        }
        private double getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return source + " " + destination;
        }


    }

    private class Graph {
        private final Set<Vertex> vertexes;
        private final Set<Edge> edges;

        //below is helpful in modifying source and vertex
        private Set<Vertex> vertexAddedBySource;
        private Set<Edge> edgesAddedBySource;
        private Set<Vertex> vertexAddedByDestination;
        private Set<Edge> edgesAddedByDestination;

        private Graph() {
            this.vertexes = new HashSet<>();
            this.edges = new HashSet<>();
            this.vertexAddedBySource = new HashSet<>();
            this.edgesAddedBySource = new HashSet<>();
            this.vertexAddedByDestination = new HashSet<>();
            this.edgesAddedByDestination = new HashSet<>();
        }

        private List<Vertex> getVertexes() {
            return new ArrayList<>(vertexes);
        }

        private List<Edge> getEdges() {
            return new ArrayList<>(edges);
        }

        private void addEdge(Edge E){
            this.edges.add(E);
        }

        private void addVertex(Vertex v){
            this.vertexes.add(v);
        }

        private void addSourceEdge(Edge E){
            this.edgesAddedBySource.add(E);
            this.edges.add(E);
        }
        private void addDestinationEdge(Edge E){
            this.edgesAddedByDestination.add(E);
            this.edges.add(E);
        }
        private void addSourceVertex(Vertex V){
            this.vertexAddedBySource.add(V);
            this.vertexes.add(V);
        }
        private void addDestinationVertex(Vertex V){
            this.vertexAddedByDestination.add(V);
            this.vertexes.add(V);
        }


        private void removeSource(){
            this.vertexes.removeAll(vertexAddedBySource);
            this.edges.removeAll(edgesAddedBySource);
            vertexAddedBySource.clear();
            edgesAddedBySource.clear();
        }

        private void removeDestination(){
            this.vertexes.removeAll(vertexAddedByDestination);
            this.edges.removeAll(edgesAddedByDestination);
            vertexAddedByDestination.clear();
            edgesAddedByDestination.clear();
        }
    }

    private class DijkstraAlgorithm {

        private final List<Vertex> nodes;
        private final List<Edge> edges;
        private Set<Vertex> settledNodes;
        private Set<Vertex> unSettledNodes;
        private Map<Vertex, Vertex> predecessors;
        private Map<Vertex, Double> distance;

        private DijkstraAlgorithm(Graph graph) {
            // create a copy of the array so that we can operate on this array
            this.nodes = new ArrayList<>(graph.getVertexes());
            this.edges = new ArrayList<>(graph.getEdges());
        }

        private void execute(Vertex source) {
            settledNodes = new HashSet<>();
            unSettledNodes = new HashSet<>();
            distance = new HashMap<>();
            predecessors = new HashMap<>();
            distance.put(source, 0.0);
            unSettledNodes.add(source);
            while (unSettledNodes.size() > 0) {
                Vertex node = getMinimum(unSettledNodes);
                settledNodes.add(node);
                unSettledNodes.remove(node);
                findMinimalDistances(node);
            }
        }

        private void findMinimalDistances(Vertex node) {
            List<Vertex> adjacentNodes = getNeighbors(node);
            for (Vertex target : adjacentNodes) {
                if (getShortestDistance(target) > getShortestDistance(node)
                        + getDistance(node, target)) {
                    distance.put(target, getShortestDistance(node)
                            + getDistance(node, target));
                    predecessors.put(target, node);
                    unSettledNodes.add(target);
                }
            }

        }

        private double getDistance(Vertex node, Vertex target) {
            for (Edge edge : edges) {
                if (edge.getSource().equals(node)
                        && edge.getDestination().equals(target)) {
                    return edge.getWeight();
                }
            }
            throw new RuntimeException("Should not happen");
        }

        private List<Vertex> getNeighbors(Vertex node) {
            List<Vertex> neighbors = new ArrayList<>();
            for (Edge edge : edges) {
                if (edge.getSource().equals(node)
                        && !isSettled(edge.getDestination())) {
                    neighbors.add(edge.getDestination());
                }
            }
            return neighbors;
        }

        private Vertex getMinimum(Set<Vertex> vertexes) {
            Vertex minimum = null;
            for (Vertex vertex : vertexes) {
                if (minimum == null) {
                    minimum = vertex;
                } else {
                    if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                        minimum = vertex;
                    }
                }
            }
            return minimum;
        }

        private boolean isSettled(Vertex vertex) {
            return settledNodes.contains(vertex);
        }

        private double getShortestDistance(Vertex destination) {
            Double d = distance.get(destination);
            if (d == null) {
                return Integer.MAX_VALUE;
            } else {
                return d;
            }
        }

        /*
         * This method returns the path from the source to the selected target and
         * NULL if no path exists
         */
        private ArrayList<Vertex> getPath(Vertex target) {
            ArrayList<Vertex> path = new ArrayList<>();
            Vertex step = target;
            // check if a path exists
            if (predecessors.get(step) == null) {
                return null;
            }
            path.add(step);
            while (predecessors.get(step) != null) {
                step = predecessors.get(step);
                path.add(step);
            }
            // Put it into the correct order
            Collections.reverse(path);
            return path;
        }

        private List<Edge> getEdges(){
            return edges;
        }

    }


    private DijkstraAlgorithm dijkstra;
    private Vertex SOURCE;
    private Vertex DESTINATION;
    private Graph G;
    private Map<String, Map<String, Point>> floor_to_floorConn;
    private Map<String, ArrayList<String>> floorToNW; //floor to non walkables
    private Map<String,int[]> floorDim;
    private Map<String, Vertex> allVertexes;
    private Node sourceNode;
    private Node destNode;
    public static Node l1 = new Node(-1, -1, "", "");
    public static Node l2 = new Node(-1, -1, "", "");

    public ConnectorGraph(Map<String, Map<String, Point>> floor_to_floorConn_map, Map<String, ArrayList<String>> floorToNonWalkables,
                          ArrayList<String> all_floors_ordered, Map<String,int[]> floorDim){
        //create that floor connector graph
        G = new Graph();
        this.floor_to_floorConn = floor_to_floorConn_map;
        this.floorToNW = floorToNonWalkables;
        this.floorDim = floorDim;

        /* APPROACH:
          - For every connector node in floors between source and destination floors,
            create an edge from that node to:
            ~ other connector nodes of the same floor
            ~ other connector nodes of different floor but having same name (for ex. Lift1 of floor1 has an edge to Lift1 of floor2)

          - Also create edges from connector nodes in source floor to source node
          - Also create edges from connector nodes in destination floor to destination node

        */

        //all floors between source and destination (including the two)

        allVertexes = new HashMap<>();
//        Log.e("floorConns",floor_to_floorConn.toString());

        //joining matching names connector nodes by creating vertexes

        for(int i =0 ; i<all_floors_ordered.size();i++){
            String floor_name = all_floors_ordered.get(i);
            Map<String,Point> floormap = floor_to_floorConn_map.get(floor_name);
            if(floormap==null){continue;}
            for (String connector_name: floormap.keySet()){

                //create connector vertex
                Vertex V = new Vertex(connector_name,floor_name);
                G.addVertex(V);
                allVertexes.put(V.getFloorNameCombo(), V);

                //join this vertex with the vertex of previous floors having same connector name
                for(int j=0;j<i;j++){
                    Vertex prev_floor_V = allVertexes.get(all_floors_ordered.get(j)+"-"+connector_name);
                    double weight = 0.0;
                    if(connector_name.toLowerCase().contains("stair")){
                        weight = 10.0;
                    }
                    if(prev_floor_V!=null){
                        Edge E1 = new Edge(V,prev_floor_V,weight);
                        Edge E2 = new Edge(prev_floor_V,V,weight);
                        G.addEdge(E1);
                        G.addEdge(E2);
//                        Log.e("creatingEdge",V.getFloorNameCombo()+" # "+prev_floor_V.getFloorNameCombo());
//                        Log.e("creatingEdge",prev_floor_V.getFloorNameCombo()+" # "+V.getFloorNameCombo());
                    }

                }

            }
        }


        //join connector vertexes of the same floor with each other through an edge
        for(String floor_name: all_floors_ordered){
            Map<String,Point> floormap = floor_to_floorConn_map.get(floor_name);
            if(floormap==null){continue;}
            String[] connector_names = floormap.keySet().toArray(new String[0]);

            //create PathSearcher for this floor
            currPS = Utils.generatePathSearcher(floor_name,floorDim,floorToNonWalkables);

            //joining connector vertexes of the same floor with each other through edges
            for(int i=0;i<connector_names.length;i++){
                for(int j=i+1;j<connector_names.length;j++){
//                    Log.e("graph",floor_name+ ", : "+ connector_names[i] + " # "+connector_names[j]);
                    Vertex v1 = allVertexes.get(floor_name+"-"+connector_names[i]);
                    Vertex v2 = allVertexes.get(floor_name+"-"+connector_names[j]);
                    Point sourcePoint = floor_to_floorConn_map.get(floor_name).get(connector_names[i]);
                    Point destPoint = floor_to_floorConn_map.get(floor_name).get(connector_names[j]);

                    ArrayList<Point> edgePath = currPS.getSimplePath(sourcePoint,destPoint,floor_name);
                    double edgeLength = currPS.getDist();
                    Edge E1 = new Edge(v1,v2,edgeLength);
                    E1.assignEdgePath(edgePath);
                    G.addEdge(E1);
//                    Log.e("creatingEdge",v1.getFloorNameCombo()+" # "+v2.getFloorNameCombo());

                    ArrayList<Point> edgePath_reverse = new ArrayList<>(edgePath);
                    Collections.reverse(edgePath_reverse);
                    Edge E2 = new Edge(v2,v1,edgeLength);
                    E2.assignEdgePath(edgePath_reverse);
                    G.addEdge(E2);
//                    Log.e("creatingEdge",v2.getFloorNameCombo()+" # "+v1.getFloorNameCombo());
                }
            }
        }


    }

    public void updateSourceAndDestination(Node sourceNode,Node destNode){

        this.sourceNode = sourceNode;
        this.destNode = destNode;
        //remove existing source and destination (if any)
        G.removeSource();
        G.removeDestination();

        SOURCE = new Vertex("source",sourceNode.getFloor());
        G.addSourceVertex(SOURCE);
        DESTINATION = new Vertex("destination",destNode.getFloor());
        G.addDestinationVertex(DESTINATION);


        //when source and destination are on same floor
        //so just add source to destination edge and return
        if(sourceNode.getFloor().equals(destNode.getFloor())){
            currPS = Utils.generatePathSearcher(sourceNode.getFloor(),floorDim,floorToNW);
            ArrayList<Point> edgePath = currPS.getSimplePath(new Point(sourceNode.getGridX(),sourceNode.getGridY()),new Point(destNode.getGridX(),destNode.getGridY()),"");
            double edgeLength = currPS.getDist();
            Edge E = new Edge(SOURCE,DESTINATION,edgeLength);
            E.assignEdgePath(edgePath);
            G.addSourceEdge(E);
            return;
        }




        //join source to connector vertexes of its floor
//        for(String connector_name : floor_to_floorConn.get(sourceNode.getFloor()).keySet()){
//            currPS = Utils.generatePathSearcher(sourceNode.getFloor(),floorDim,floorToNW);
//            Point connectorPoint = floor_to_floorConn.get(sourceNode.getFloor()).get(connector_name);
//            Vertex Vconn = allVertexes.get(sourceNode.getFloor()+"-"+connector_name);
//
//            Point startPoint = new Point(sourceNode.getGridX(), sourceNode.getGridY());
//            ArrayList<Point> edgePath = currPS.getSimplePath(startPoint,connectorPoint);
//            double edgeLength = currPS.getDist();
//            if(edgePath.size()==0){
//                edgePath.add(startPoint);
//                edgeLength = 0.0; //because if there is no path then it will return Integer.MAX_VALUE
//            }
//            Edge E = new Edge(SOURCE,Vconn,edgeLength);
//            E.assignEdgePath(edgePath);
//            G.addSourceEdge(E);
//            Log.e("creatingEdge",SOURCE.getFloorNameCombo()+" # "+Vconn.getFloorNameCombo());
//        }

        //join destination to connector vertexes of its floor
        Map<String,Point> destFloormap = floor_to_floorConn.get(destNode.getFloor());
        if(destFloormap!=null) {
            for (String connector_name : destFloormap.keySet()) {
                currPS = Utils.generatePathSearcher(destNode.getFloor(), floorDim, floorToNW);
                Point connectorPoint = floor_to_floorConn.get(destNode.getFloor()).get(connector_name);
                Vertex Vconn = allVertexes.get(destNode.getFloor() + "-" + connector_name);
                Point endPoint = new Point(destNode.getGridX(), destNode.getGridY());
                ArrayList<Point> edgePath = currPS.getSimplePath(connectorPoint, endPoint,"");
                double edgeLength = currPS.getDist();
                if (edgePath.size() == 0) {
                    edgePath.add(endPoint);
                    edgeLength = 0.0; //because if there is no path then it will return Integer.MAX_VALUE
                }
                Edge E = new Edge(Vconn, DESTINATION, edgeLength);
                E.assignEdgePath(edgePath);
                G.addDestinationEdge(E);
//                Log.e("creatingEdge", Vconn.getFloorNameCombo() + " # " + DESTINATION.getFloorNameCombo());
                if(commonlift.isEmpty()){
                    l2.setFloor("ground");
                    l2.setName(connector_name);
                    l2.setGridX(connectorPoint.x);
                    l2.setGridY(connectorPoint.y);
                }
            }
        }

    }


    public ArrayList<PathOption> executeDijkstra(){
        int c=0;
        dijkstra = new DijkstraAlgorithm(G);
        ArrayList<PathOption> result = new ArrayList<>();

        if(sourceNode.getFloor().equals(destNode.getFloor())){
            dijkstra.execute(SOURCE);
            ArrayList<FloorObj> floorObjsArr = new ArrayList<>(generateMultiFloorPaths());
            PathOption p = new PathOption("NONE",floorObjsArr);
            result.add(p);
            return filterPathOptions(result);
        }

        // if sourceFloor != destFloor
        // take every connector vertex of the source floor and apply Dijkstra on it
        // for each connector vertex of source floor we will have a path P to destination
        // add source to connector vertex path to P and store P in the result array
        // return type of this function gives N paths passing through N connectors of the source floor
        Map<String,Point> sourceFloormap = floor_to_floorConn.get(sourceNode.getFloor());
        Map<String,Point> destFloormap = floor_to_floorConn.get(destNode.getFloor());

        if(sourceFloormap!=null && destFloormap!=null){
            for (String sourceconnector_name : sourceFloormap.keySet()){
                for(String destconnector_name : destFloormap.keySet()){
                    if(sourceconnector_name.equals(destconnector_name));
                    commonlift.add(sourceconnector_name);
                }
            }
        }
        if(sourceFloormap!=null) {
            for (String connector_name : sourceFloormap.keySet()) {
                Vertex Vconn = allVertexes.get(sourceNode.getFloor() + "-" + connector_name);
                Point connectorPoint = floor_to_floorConn.get(sourceNode.getFloor()).get(connector_name);
                Log.d("newpathplaning", "executeDijkstra: " + connector_name);
                c++;
                Log.d("connectorpoint",""+c+connector_name);
                currPS = Utils.generatePathSearcher(sourceNode.getFloor(), floorDim, floorToNW);
                Point startPoint = new Point(sourceNode.getGridX(), sourceNode.getGridY());
                ArrayList<Point> edgePath = currPS.getSimplePath(startPoint, connectorPoint,"");
                double edgeLength = currPS.getDist();
                if (edgePath.size() == 0) {
                    edgePath.add(startPoint);
                    edgeLength = 0.0; //because if there is no path then it will return Integer.MAX_VALUE
                }


                dijkstra.execute(Vconn);

                FloorObj sourceFloorObj = new FloorObj();
                sourceFloorObj.setFloorName(SOURCE.getFloor());
                sourceFloorObj.setEndPointName(connector_name);
                sourceFloorObj.setPath(edgePath);
                sourceFloorObj.setPathWeight(edgeLength);

                ArrayList<FloorObj> floorObjsArr = new ArrayList<>();
                floorObjsArr.add(sourceFloorObj);
                floorObjsArr.addAll(generateMultiFloorPaths());
                ps = new PathOption(connector_name, floorObjsArr);
                result.add(ps);

                if(commonlift.isEmpty()){
                    l1.setFloor("ground");
                    l1.setName(connector_name);
                    l1.setGridX(connectorPoint.x);
                    l1.setGridY(connectorPoint.y);
                }

                if(commonlift.isEmpty()){
                    ConnectorGraph connectorGraphtwo = new ConnectorGraph(Navigation.floor_to_floorConn_map, Navigation.floorToNonWalkables, Navigation.sourceFloorsList, Navigation.floorDim);
                    connectorGraphtwo.updateSourceAndDestination(l1,l2);
                    ArrayList<PathOption> pathOptionstwo = connectorGraphtwo.executeDijkstra();
                    result.add(pathOptionstwo.get(0));
                }
            }
        }




        Log.d("resultp", ""+result);
        return filterPathOptions(result);
    }

    //every PathOption must have path which has floorObjs having unique floor, no repetition
    private ArrayList<PathOption> filterPathOptions(ArrayList<PathOption> POArr) {
        ArrayList<PathOption> result = new ArrayList<>();
        Set<String> encounteredFloor = new HashSet<>();
        boolean encountered;
        for(PathOption p: POArr){
            encounteredFloor.clear();
            encountered = false;

            for(FloorObj fobj: p.getPath()){
                if(encounteredFloor.contains(fobj.getFloorName())){
                    encountered = true;
                    break;
                }
                encounteredFloor.add(fobj.getFloorName());
            }

            if(!encountered){
                result.add(p);
            }

        }

        //also calculate the total weight of paths in pathOption
        for(PathOption p: result){
            double pWeight = 0.0;
            for(FloorObj f: p.getPath()){
                pWeight += f.getPathWeight();
            }
            Log.d("pweight", ""+pWeight);
            p.setPathDistance(pWeight);
        }
        Log.d("pathd", ""+result);
        return result;
    }


    //generates FloorObj list which is ordered according to the path
    //these FloorObj's getPath() will be used to draw paths on canvases
    private ArrayList<FloorObj> generateMultiFloorPaths(){
        ArrayList<FloorObj> result = new ArrayList<>();
        if(SOURCE!=null && DESTINATION!=null){
            ArrayList<Vertex> vertexesInPath = dijkstra.getPath(DESTINATION);
            if(vertexesInPath==null){
                return result;
            }
            String prev_floor = null;
            for(int i = 0; i<vertexesInPath.size()-1; i++){
                // if edge lies in a floor then create a FloorObj for this floor
                // if edge is crossing the floors then do nothing

                Vertex v1 = vertexesInPath.get(i);
                Vertex v2 = vertexesInPath.get(i+1);
                if(v1.getFloor().equals(v2.getFloor())){
                    //same floor vertexes so edges lies in a floor
                    //get edge having these vertexes as end points
                    Edge E = getEdgeUsingVertexes(v1,v2);
                    assert E != null;

                    if(v1.getFloor().equals(prev_floor)){
                      // means this floor's FloorObj is already inserted in the result
                      // append the edgePath obtained from E to that FloorObj path list (basically updating that floor's path)
                       FloorObj fObj = result.get(result.size()-1);
                       fObj.getPath().addAll(E.edgePath);
                    }
                    else{
                        //create new FloorObj
                        FloorObj fObj = new FloorObj();
                        fObj.setFloorName(v1.getFloor());
                        fObj.setEndPointName(v2.name);
                        fObj.setPath(E.edgePath);
                        fObj.setPathWeight(E.getWeight());
                        result.add(fObj);
                    }

                    prev_floor = v1.getFloor();
                }
            }
        }
        return result;
    }


    private Edge getEdgeUsingVertexes(Vertex v1, Vertex v2){
        for(Edge E: dijkstra.getEdges()){
            if(E.source==v1 && E.destination==v2){
                return E;
            }
        }
        return null;
    }
}
