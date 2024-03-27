package com.inclunav.iwayplus.beacon_related;

import android.graphics.Point;
import android.util.Pair;

import com.inclunav.iwayplus.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlwaysActiveScans {
    private Set<String> all_beacons_building;
    private Map<String,Long> beaconToLastScanTime;
    private Map<String,Double> beaconToRSSI; //averaged RSSI of beacon
    private BeaconDetails beaconDetails;

    public AlwaysActiveScans(BeaconDetails beaconDetails){
        this.beaconToLastScanTime = new HashMap<>();
        this.all_beacons_building = new HashSet<>();
        this.beaconToRSSI = new HashMap<>();
        this.beaconDetails = beaconDetails;
    }

    public void addBeacon(String macID){
        all_beacons_building.add(macID);

        //put last scan time as 1L so when we find out the difference of currentTime-lastScanTime
        //using System.currentMillis() it will be way larger as required
        beaconToLastScanTime.put(macID,1L);
    }

    public void addRSSI(String macID, double RSSI){
        if(!all_beacons_building.contains(macID)){
            return;
        }
        if(!beaconToRSSI.containsKey(macID)){
            beaconToRSSI.put(macID, RSSI);
        }

        //avgFilter to add this RSSI
        double newAvg = 0.1*RSSI + 0.9*beaconToRSSI.get(macID);
        beaconToRSSI.put(macID,newAvg);

        //update lastScanTime of this macID beacon
        beaconToLastScanTime.put(macID,System.currentTimeMillis());
    }


    public double[] multiLaterate(){

        ArrayList<Pair<String,Double>> beaconDistPairs = new ArrayList<>();

        //get all those beacons whose lastScans are under 2 seconds
        //calculate their distances and put it in the arraylist
        Long currentTime = System.currentTimeMillis();
        for(String beacon: beaconToLastScanTime.keySet()){
            if(currentTime - beaconToLastScanTime.get(beacon) <2000){
                Double dist = Utils.getDistance(beaconToRSSI.get(beacon),-61);
               // Log.e("beaconDistance",dist.toString() );
                beaconDistPairs.add(new Pair<String, Double>(beacon,dist));
            }
        }

        if(beaconDistPairs.size()<3){
            //can't trilaterate
            return null;
        }

        //get beacons having distances under 5 metres
        ArrayList<Pair<String,Double>> useful_pairs = new ArrayList<>();
        for(Pair<String,Double> p: beaconDistPairs){
            if(p.second<25){
                useful_pairs.add(p);
            }
        }

        if(useful_pairs.size()<3){
            //can't do multilateration
            return null;
        }

        //multilaterate using useful beacons and return the result
        double[][] positions = new double[useful_pairs.size()][2]; //2d (x,y)
        double[] distances = new double[useful_pairs.size()];
        Point beaconCoord;
        for(int i=0;i<useful_pairs.size();i++){
            beaconCoord = beaconDetails.getBeaconCoord(useful_pairs.get(i).first);
            positions[i][0] = beaconCoord.x;
            positions[i][1] = beaconCoord.y;
            distances[i] = useful_pairs.get(i).second*3.28084; //convert metres to feets
        }

        return Multilateration.getLocation(positions,distances);

    }

}
