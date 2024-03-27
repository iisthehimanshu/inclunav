package com.inclunav.iwayplus.pdr;

import com.inclunav.iwayplus.Utils;

import java.util.Arrays;

public enum Sentences {
    searching_path,
    arrived_on_floor,
    arrived_on_unexpected_floor,
    searching_location,
    please_wait,
    please_wait_finding,
    location_is_at,
    location_is_at_toast,
    found_location,
    unable_to_find_location,
    back_to_path,
    going_away_from_path,
    reached_destination,
    reached_connector_point,
    use_connector_point,
    go_straight,
    turn_left,
    turn_right,
    left,
    right,
    front,
    back;

    public String getSentence(String lang, String... s){
        if(lang.equals("en")){
            switch (this){
                case searching_path:
                    return "Please Wait. Searching new path.";
                case arrived_on_floor:
                    return "You have arrived on "+s[0]+" floor.";
                case arrived_on_unexpected_floor:
                    return "Sorry you have arrived on unexpected "+s[0]+" floor. Please re-route.";
                case please_wait_finding:
                    return "Please wait, Finding location";
                case please_wait:
                    return "Please wait";
                case searching_location:
                    return "Finding location";
                case location_is_at:
                    return "You are on "+s[1]+" floor"+", " +s[0]+" is on your "+s[2]+", "+ "Use top buttons to navigate"+".";
                case location_is_at_toast:
                    return "You are on "+s[1]+" floor, " +s[0]+" is on your "+s[2];
                case found_location:
                    return "You are at "+s[0]+" floor  "+ "Use top buttons to navigate";
                case unable_to_find_location:
                    return "Unable to find location  Use top buttons to navigate.";
                case back_to_path:
                    return "Back to path now";
                case going_away_from_path:
                    return "Going away from path";
                case reached_destination:
                    return "Reached your destination "+s[0];
                case reached_connector_point:
                    return "Reached "+s[0];
                case use_connector_point:
                    return "Use this "+s[0]+" and go to "+s[1]+ "floor" ;
                case go_straight:
                    return "Go straight. "+s[0]+" steps";
                case turn_left:
                    return "Turn "+ Arrays.toString(s);
                case turn_right:
                    return "Turn "+ Arrays.toString(s);
                case left:
                    return "left";
                case right:
                    return "right";
                case front:
                    return "front";
                case back:
                    return "back";
                default:
                    return "";
            }
        }
        else{
            switch (this){
                case searching_path:
                    return "कृपया रुके. आपके गंतव्य स्थान के लिए नया रास्ता ढूँढा जा रहा है.";
                case arrived_on_floor:
                    return "आप "+s[0]+" फ्लोर पे आ गए है ";
                case arrived_on_unexpected_floor:
                    return "आप अनपेक्षित "+s[0]+" फ्लोर पर आ गए है . आपके गंतव्य स्थान के लिए नया रास्ता ढूँढा जा रहा है.";
                case searching_location:
                    return "कृपया रुके. आपकी वर्तमान जगह को ढूँढ़ा जा रहा है";
                case location_is_at:
                    return "आपके "+s[2]+" "+s[0]+" है, जो "+s[1]+" फ्लोर पर है | ऊपर दिए गए बटन्स के सहयोग से अपने गंतव्य स्थान को दर्ज करे";
                case found_location:
                    return "आप "+s[0]+" फ्लोर पे है |";
                case unable_to_find_location:
                    return "आपकी वर्तमान जगह हम ढूँढ़ नहीं पा रहे है | ऊपर दिए गए बटन्स का प्रयोग कीजिये";
                case back_to_path:
                    return "आप रास्ते पे वापस आ गए है";
                case going_away_from_path:
                    return "आप रास्ते से दूर जा रहे है";
                case reached_destination:
                    return "आप गंतव्य स्थान "+s[0]+" पर आ गए है";
                case reached_connector_point:
                    return "आप "+s[0]+" पर आ गए है";
                case use_connector_point:
                    return s[0]+" का प्रयोग करके "+s[1]+" फ्लोर पर जाइये";
                case go_straight:
                    int steps = Integer.parseInt(s[0]);
                    if(steps<=100){
                        return "सीधे चले लगभग "+ Utils.hindiNums_zero_to_hundred[steps]+ " कदम";
                    }
                    else{
                        return "सीधे चले लगभग "+ s[0]+ " कदम";
                    }

                case turn_left:
                    return "बाएं मुड़े. "+Utils.hindiNums_zero_to_hundred[Integer.parseInt(s[0])]+" बजे तक";
                case turn_right:
                    return "दाएं मुड़े. "+Utils.hindiNums_zero_to_hundred[Integer.parseInt(s[0])]+" बजे तक";
                case left:
                    return "बाएं";
                case right:
                    return "दाएं";
                case front:
                    return "सामने";
                case back:
                    return "पीछे";
                default:
                    return "";
            }
        }
    }
}