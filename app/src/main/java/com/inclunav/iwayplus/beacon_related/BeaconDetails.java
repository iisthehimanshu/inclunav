package com.inclunav.iwayplus.beacon_related;

import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BeaconDetails {
    private int lowestRSSI = -90;
    private Map<String, Queue<Integer>> beaconToWindow;
    private Map<String, Double> beaconToAverageRSSI;
    private int windowSize;
    private Map<String, ArrayList<String>> floorToBeacons;
    private Map<String, Pair<String, String>> beaconToFloorAndCoordinate;
    private Map<String, int[]> beaconToBinCount;
    private double[] weightOfbins = new double[]{8, 4, 2, 0.5, 0.25, 0.15, 0.075};
    private ArrayList<String> emptyArrayList = new ArrayList<>();

    public BeaconDetails(int windowSize) {
        this.windowSize = windowSize;
        beaconToWindow = new HashMap<>();
        floorToBeacons = new HashMap<>();
        beaconToAverageRSSI = new HashMap<>();
        beaconToBinCount = new HashMap<>();
        beaconToFloorAndCoordinate = new HashMap<>();
    }

    public void addBeacon(String macID, String floorName, String coordinate) {
        Queue<Integer> RSSIWindow = new LinkedList<>();
        beaconToWindow.put(macID, RSSIWindow);
        beaconToAverageRSSI.put(macID, 0.0);
        if (!floorToBeacons.containsKey(floorName)) {
            ArrayList<String> arr = new ArrayList<>();
            floorToBeacons.put(floorName, arr);
        }
        floorToBeacons.get(floorName).add(macID);
        beaconToBinCount.put(macID, new int[7]);
        beaconToFloorAndCoordinate.put(macID, new Pair<>(floorName, coordinate));
        Log.d("Added_Beacon", "macID: " + macID + " , Floor : " + floorName);
    }

    public ArrayList<String> getBeaconsOfFloor(String floorName) {
        if (!floorToBeacons.containsKey(floorName)) return emptyArrayList;
        return floorToBeacons.get(floorName);
    }

    public void addRSSI(String macID, int RSSI) {
        if (!beaconToWindow.containsKey(macID)) return;

        Queue<Integer> q = beaconToWindow.get(macID);
        double averageRSSI = beaconToAverageRSSI.get(macID);

        if (q.size() == windowSize) {
            int removedValue = q.poll();
            averageRSSI -= removedValue / (double) windowSize;
        }

        q.add(RSSI);
        averageRSSI += RSSI / (double) windowSize;
        beaconToAverageRSSI.put(macID, averageRSSI);

        updateCount(macID, Math.abs(RSSI));
    }

    private void updateCount(String macID, int absRSSI) {
        int binIndex;
        if (absRSSI <= 65) {
            binIndex = 0;
        } else if (absRSSI <= 70) {
            binIndex = 1;
        } else if (absRSSI <= 75) {
            binIndex = 2;
        } else if (absRSSI <= 80) {
            binIndex = 3;
        } else if (absRSSI <= 85) {
            binIndex = 4;
        }else if (absRSSI <= 90) {
            binIndex = 5;
        } else {
            binIndex = 6;
        }

        beaconToBinCount.get(macID)[binIndex]++;
    }

    public double weightOfBeacon(String macID) {

        double result = 0.0;
        int qsize = beaconToWindow.get(macID).size();
        Log.d("BMW", "macID: " + macID);

        qsize = Math.max(qsize, 1);
        Log.d("BMW", "qsize: " + qsize);
        for (int i = 0; i < 6; i++) {
            double binCount = beaconToBinCount.get(macID)[i];
            Log.d("BMW", "bincount: " + binCount);
            result += (binCount / qsize) * (weightOfbins[i] / 14.975);
            Log.d("BMW", "result: " + result);
        }
        return result * 1000;
    }

    public double getAvgRSSI(String macID) {
        if (!beaconToWindow.containsKey(macID)) {
            return lowestRSSI;
        }
        if (beaconToWindow.get(macID).size() < windowSize) {
            return lowestRSSI;
        }

        double sumRSSI = 0;
        Queue<Integer> rssiWindow = beaconToWindow.get(macID);
        for (int rssi : rssiWindow) {
            sumRSSI += rssi;
        }
        return sumRSSI / windowSize;
    }

    public String getFloorOfBeacon(String macID) {
        if (!beaconToWindow.containsKey(macID)) {
            return "";
        }
        return beaconToFloorAndCoordinate.get(macID).first;
    }

    public Point getBeaconCoord(String macID) {
        if (!beaconToWindow.containsKey(macID)) {
            return null;
        }
        String coords = beaconToFloorAndCoordinate.get(macID).second;
        String[] coordArray = coords.split(",");
        int x = Integer.parseInt(coordArray[0]);
        int y = Integer.parseInt(coordArray[1]);
        return new Point(x, y);
    }

    public String[] getAllBeacons() {
        return beaconToWindow.keySet().toArray(new String[beaconToWindow.size()]);
    }

    public void clearCounts() {
        for (String mac : beaconToBinCount.keySet()) {
            beaconToBinCount.put(mac, new int[7]);
            beaconToWindow.get(mac).clear();
            beaconToAverageRSSI.put(mac, 0.0);
        }
    }
}
