package com.inclunav.iwayplus.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.inclunav.iwayplus.beacon_related.AlwaysActiveScans;
import com.inclunav.iwayplus.beacon_related.BeaconDetails;
import com.inclunav.iwayplus.CacheHelper;
import com.inclunav.iwayplus.custom_step_detector.SteepDetector;
import com.inclunav.iwayplus.custom_step_detector.StepListener;
import com.inclunav.iwayplus.enums.ElementTypes;
import com.inclunav.iwayplus.enums.GeometryTypes;
import com.inclunav.iwayplus.layout_utilities.CanvasView;
import com.inclunav.iwayplus.layout_utilities.InstantAutoComplete;
import com.inclunav.iwayplus.layout_utilities.SliderItem;
import com.inclunav.iwayplus.layout_utilities.SliderViewInflator;
import com.inclunav.iwayplus.layout_utilities.ZoomRotationFrameLayout;
import com.inclunav.iwayplus.MessagePriority;
import com.inclunav.iwayplus.pdr.DeviceAttitudeHandler;
import com.inclunav.iwayplus.pdr.FloorObj;
import com.inclunav.iwayplus.pdr.Node;
import com.inclunav.iwayplus.pdr.Sentences;
import com.inclunav.iwayplus.path_search.ConnectorGraph;
import com.inclunav.iwayplus.path_search.PathOption;
import com.inclunav.iwayplus.R;
import com.inclunav.iwayplus.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ufobeaconsdk.main.UFOBeaconManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.inclunav.iwayplus.MessagePriority.L0;
import static com.inclunav.iwayplus.MessagePriority.L1;
import static com.inclunav.iwayplus.MessagePriority.L2;
import static com.inclunav.iwayplus.MessagePriority.L3;

public class NavigationBlind extends AppCompatActivity implements SensorEventListener, StepListener {

    private static final double BEACON_NEAR_PATH_DIST = 3; //threshold of when to bring to nearest point in path to the beacon
    private static final double MIN_BEACON_RECOGNITION_CONST = 10;
    private static final double STRONG_BEACON_RECOGNITION_CONST = 20; //30
    private static final double MIN_FUSION_CONST = 15;
    private static final double NEAR_LOCATION_DISTANCE = 5.0; //distance in feets of finding nearest node
    private static final long NEW_LOCALIZATION_THRESHOLD_TIME = 15000; //till 15 seconds it will not relocalize to the same point on the path
    private static final int STEPS_THRESHOLD_TO_MERGE = 5;
    private Map<String,int[]> floorDim = new HashMap<>(); //floorName to [length,width] map
    private Map<String,ArrayList<String[]>> floorToNodes = new HashMap<>(); //stores floorName to NodesList where each node = (coordinates, nodeName, MACID)
    private Map<String, String> nodeCoordToFloorElement = new HashMap<>();
    private Map<String,Float> floorToAngle = new HashMap<>();
    private LinearLayout viewToAnimate;

    private static TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private static final int WINDOWSIZE = 5; //window size for running average RSSI readings for a beacon
    private boolean pdrEnabled = false;// disable the pdr initially
    private boolean lastStateOfPDR = false;
    private boolean shouldExecuteOnResume;
    private RequestQueue MyRequestQueue;
    private float floorRotation=250.0f; // angle of floor's displayed image on compass
    private String buildingName;
    private String venueName;
    private String base_url;
    private Vibrator vibe;

    private long lastLocalizedTime = 0;
    //currLocation stores the location in the form of grids in x and grids in y, where a grid=1 feet
    private Node lastLocalized = new Node(-1,-1,"",""); //assume initial location to be -1,-1
    private Node destLocation = new Node(-1,-1,"","");
    private Node currLocation = new Node(-1,-1,"","");
    private Node lastSource = new Node(-1,-1,"",""); //when path is searched lastSource is set to source of that path
    private InstantAutoComplete sourceEditText;
    private InstantAutoComplete destEditText;
    private ArrayAdapter sourceAdapter;
    private ArrayAdapter destAdapter;
    private ArrayList<String> sourceFloorsList = new ArrayList<>();//associated with spinner
    private ArrayList<String> sourcePointsList = new ArrayList<>();
    private ArrayList<String> destFloorsList = new ArrayList<>();//associated with spinner
    private ArrayList<String> destPointsList = new ArrayList<>();

    private Map<String,ArrayList<String>> venueListCategory = new HashMap<>();
    private Map<String,ArrayList<String>> buildingListCategory = new HashMap<>();
    private Map<String,ArrayList<String>> sourceListCategory = new HashMap<>();

    private Map<String,String> venueAPIData = new HashMap<>();
    private Map<String,String> buildingAPIData = new HashMap<>();
    private Map<String,String> navigationAPIData = new HashMap<>();

    private UFOBeaconManager ufo;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private DecimalFormat oneDecimalForm = new DecimalFormat("#.#");

    private double step_size;
    private String audio_language;
    private int currDisplayIndexOfPath=0; //when displaying we display current location as this index point from the path list
    private double gridLength = 0.3048; //what is the size of grid in metres, in our case its 1 feet = 0.3048m

//    private ToneGenerator toneGen;

    private SensorManager sm;
    private DeviceAttitudeHandler dah;
    private Sensor accel;
    private SteepDetector simpleSteep;

    private Handler handler;
    private boolean animBurgerExpanded = true;

    private boolean mainLogicThreadRunning = false;
    private Runnable mainLogicThread;
    private Runnable scanToggleThread; //I have read that there are some time limits on continuous ble scanning that's why
    private Runnable searchLocationThread;
    private Runnable pathSearchThread;
    private Runnable orientation_helper_thread;
    private Runnable popup_checker_thread; //checks whether there are popups in the popup queue if yes pops them out and shows them

    private BeaconDetails beaconObj = new BeaconDetails(WINDOWSIZE);
    private AlwaysActiveScans alwaysActiveScansObj = new AlwaysActiveScans(beaconObj);
    //    Map<String, Double> beaconToRSSI = new HashMap<>();

    private double pdrWeight = 0.4;
    private double rssiWeight = 0.6;
    private double FUSE_CONST = 10;
    private Map<String, Map<String, Point> > floor_to_floorConn_map = new HashMap<>(); //floor name to floor connection nodes map
    private Map<String, ArrayList<String>> floorToNonWalkables = new HashMap<>();
    private Map<String, ArrayList<String>> floorToSimplePoly = new HashMap<>(); //floor name to non walkable polygon map, used by pathSearcher for valid transition
    private Map<String, String> floorToImageName = new HashMap<>();

    private Map<String,String> floorToflr_dist_matrix = new HashMap<>();
    private Map<String,String> floorTofrConn = new HashMap<>();
    private InputMethodManager imm; //for hiding or showing keyboard
    private int activity_width; //activity's width
    private int activity_height;

    private Map<String,String[]> nodeNameToDetails = new HashMap<>(); //stores floor and coordinate of node
//    private LinearLayout sliderView;
//    private HorizontalScrollView hsv;
    private TextView welcomeView;
    private FrameLayout loadingView;
    private boolean ble_scanning = false;
    private Map<String,Set<Pair>> turningPointsMap = new HashMap<>();
    private boolean jumpingOutStarted = false;
    private boolean jumpedOut = false;

    private boolean pathsearched = false;

    private Lock queueLock;
    private int sliderViewCounter = 0;
    private int maxSliderViewCounter = 1;

    //when path is searched these objects are returned which are used to inflate
    //canvas and for other things.
    private ArrayList<FloorObj> pathFloorObjs = new ArrayList<>();
    FrameLayout mainContainer;
    double xf; //current location in display pixels
    double yf; //current location in display pixels
    private FloorObj currFloorObj;
    private CacheHelper mCacheHelper;
    private ConnectorGraph connectorGraph;
    PopupWindow activePopup;
    String lastSpokenNearToNode;
    Queue<Pair<String, MessagePriority>> popupMessagesQueue = new LinkedList();
    ImageView hamburgerButton;
    CardView hamburgerAnimList;
    TextView recentMessage;
    LinearLayout floorLevelLayout;
    LinearLayout floorLevelLayoutInfo;
    LinearLayout floorLevelLayoutContainer;
    Boolean floorLevelLayoutVisible = true;
    LinearLayout recentMessageContainer;
    TextView debugView;
    ImageView imageMsg;

    private LinearLayout sourceViewToAnimate;
    private LinearLayout destinationViewToAnimate;
    TextView searchSourceDialog;
    TextView searchDestinationDialog;
    private boolean openSearchSourceDialogVisible = false;
    private boolean openSearchDestinationDialogVisible = false;

    private TextView sourceVenueEditText, destVenueEditText;

    private CategoryListAdapter venueCategoryListItem, buildingCategoryListItem, landmarkCategoryListItem;
    private DropDownListAdapter sourceVenueListAdapter, destVenueListAdapter;
    private DropDownListAdapter sourceLandmarkListAdapter, destLandmarkListAdapter;

    private TextView sourceBuildingEditText, destinationBuildingEditText;

    private DropDownListAdapter sourceBuildingListAdapter, destBuildingListAdapter;

    private RecyclerView sourceVenueList, destVenueList, sourceBuildingList, destBuildingList, sourceLandmarkList, destLandmarkList ;

    private ArrayList<String> venueList = new ArrayList<>();
    private ArrayList<Double> venueData = new ArrayList<>();
    private ArrayList<Double> buildingData = new ArrayList<>();
    private ArrayList<String> buildingList = new ArrayList<>();
    private ArrayList<String> route = new ArrayList<>();
    private String currentVenue;
    private String currentBuilding;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;
    String locationName, floorName;
    LinearLayout sourceDest, modal, modalSource, categoryView, destCategoryView, venueListLayout, destVenueListLayout, buildingListLayout, destBuildingListLayout;
    TextView modalDestinationName, modalVenueName, modalDestinationDistance, modalSourceName, modalSourceVenueName;
    Button modalDirection;
    String MessageText = "";
    Boolean VenueListLayoutVisible = false, DestVenueListLayoutVisible = false,BuildingListLayoutVisible = false, DestBuildingListLayoutVisible = false;
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton, micButton2, micButton3, micButton4;
    DropDownListData[] myListData;

    TextView toggleLandmarkList,toggleLandmarkListInside, toggleCategoryView, viewAllEnable, viewAllDisable, sortEnable, sortDisable, amenitiesDisable, amenitiesEnable, roomsDisable, roomsEnable;
    TextView toggleDestLandmarkList,toggleDestLandmarkListInside, toggleDestCategoryView, destViewAllEnable, destViewAllDisable, destSortEnable, destSortDisable, destAmenitiesDisable, destAmenitiesEnable, destRoomsDisable, destRoomsEnable;
    LinearLayout filterSortLayout, landmarkListLayout;
    LinearLayout destFilterSortLayout, destLandmarkListLayout;

    @SuppressLint({"MissingPermission", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_blind);
        
        mCacheHelper = new CacheHelper(this);
        mainContainer = findViewById(R.id.mainContainer);
        //to make the path search widget animation
        sourceViewToAnimate = findViewById(R.id.sourceViewToAnimate);
        destinationViewToAnimate = findViewById(R.id.destinationViewToAnimate);
//        hsv = findViewById(R.id.horizontalScrollView);
        recentMessage = findViewById(R.id.recentMessage);
        floorLevelLayout = findViewById(R.id.floorLevelLayout);
        floorLevelLayoutInfo = findViewById(R.id.floorLevelLayoutInfo);
        floorLevelLayoutContainer = findViewById(R.id.floorLevelLayoutContainer);
//        destEditTextContainer = findViewById(R.id.destEditTextContainer);
        recentMessageContainer = findViewById(R.id.recentMessageContainer);
        debugView = findViewById(R.id.debugView);
//        venueEditName = findViewById(R.id.venueName);
//        venueEditName2 = findViewById(R.id.venueName2);
        imageMsg = findViewById(R.id.imageMsg);

        searchSourceDialog = findViewById(R.id.searchSourceDialog);
        searchDestinationDialog = findViewById(R.id.searchDestinationDialog);
        sourceBuildingEditText = findViewById(R.id.sourceBuildingEditText);
        destinationBuildingEditText = findViewById(R.id.destinationBuildingEditText);

        sourceBuildingList = (RecyclerView) findViewById(R.id.dropdownBuildinglist);
        sourceBuildingListAdapter = new DropDownListAdapter(this);
        sourceBuildingList.setLayoutManager(new LinearLayoutManager(this));
        sourceBuildingList.setAdapter(sourceBuildingListAdapter);

        destBuildingList = (RecyclerView) findViewById(R.id.destDropdownBuildinglist);
        destBuildingListAdapter = new DropDownListAdapter(this);
        destBuildingList.setLayoutManager(new LinearLayoutManager(this));
        destBuildingList.setAdapter(destBuildingListAdapter);

        sourceVenueEditText = findViewById(R.id.sourceVenueEditText);
        sourceVenueList = (RecyclerView) findViewById(R.id.dropdownVenuelist);
        sourceVenueListAdapter = new DropDownListAdapter(this);
        sourceVenueList.setLayoutManager(new LinearLayoutManager(this));
        sourceVenueList.setAdapter(sourceVenueListAdapter);

        destVenueEditText = findViewById(R.id.destVenueEditText);
        destVenueList = (RecyclerView) findViewById(R.id.dropdownDestVenuelist);
        destVenueListAdapter = new DropDownListAdapter(this);
        destVenueList.setLayoutManager(new LinearLayoutManager(this));
        destVenueList.setAdapter(destVenueListAdapter);

        sourceDest = findViewById(R.id.sourceDest);
        modal = findViewById(R.id.modal);
        modalDestinationDistance = findViewById(R.id.modalDestinationDistance);
        modalDestinationName = findViewById(R.id.modaldestinationName);
        modalVenueName = findViewById(R.id.modalVenueName);
        modalDirection = findViewById(R.id.modalDirection);

        modalSource = findViewById(R.id.modalSource);
        modalSourceName = findViewById(R.id.modalSourceName);
        modalSourceVenueName = findViewById(R.id.modalSourceVenueName);

        categoryView = findViewById(R.id.categoryView);
        destCategoryView = findViewById(R.id.destCategoryView);

        venueListLayout = findViewById(R.id.venueList);
        destVenueListLayout = findViewById(R.id.destVenueList);
        buildingListLayout = findViewById(R.id.buildingList);
        destBuildingListLayout = findViewById(R.id.destBuildingList);

        sourceToggleView();
        destToggleView();

        modalDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sourceDest.setVisibility(View.GONE);
                modal.setVisibility(View.GONE);
                searchPath();
                pdrEnabled=true;
                enableBLE();
                createAndRunMainLogicThread();
            }
        });

        int img_id[] = {R.drawable.ic_currency,
                R.drawable.ic_baseline_design_services_24,
                R.drawable.ic_baseline_local_hospital_24,
                R.drawable.ic_baseline_local_hotel_24,
                R.drawable.ic_public_building,
                R.drawable.ic_baseline_shopping_cart_24,
                R.drawable.ic_baseline_directions_transit_24};
        String text[] = {"Banking", "Education", "Health", "Hospitality", "Public Building", "Shopping", "Transport"};

        int img_id1[] = {R.drawable.ic_currency,
                R.drawable.ic_baseline_design_services_24,
                R.drawable.ic_baseline_local_hospital_24,
                R.drawable.ic_baseline_local_hotel_24,
                R.drawable.ic_public_building,
                R.drawable.ic_baseline_shopping_cart_24};
        String text1[] = {"Banking", "Education", "Health", "Hospitality", "Public Building", "Shopping"};

        int img_id2[] = {R.drawable.ic_user,
                R.drawable.ic_security_24,
                R.drawable.ic_baseline_restaurant_24,
                R.drawable.ic_baseline_design_services_24,
                R.drawable.ic_baseline_local_hospital_24};
        String text2[] = {"Faculty\nCabins", "Security\n& Safety", "Food\n& Drink", "Learning\nSpaces", "Health"};
        ArrayList<CategoryListData> listContentArr = new ArrayList<>();
        ArrayList<CategoryListData> listContentArr1 = new ArrayList<>();
        ArrayList<CategoryListData> listContentArr2 = new ArrayList<>();

        for(int i= 0; i<7 ; i++) {
            CategoryListData listData = new CategoryListData();
            listData.setDescription(text[i]);
            listData.setImgId(img_id[i]);
            listContentArr.add(listData);
        }

        for(int i= 0; i<=5 ; i++) {
            CategoryListData listData = new CategoryListData();
            listData.setDescription(text1[i]);
            listData.setImgId(img_id1[i]);
            listContentArr1.add(listData);
        }

        for(int i= 0; i<5 ; i++) {
            CategoryListData listData = new CategoryListData();
            listData.setDescription(text2[i]);
            listData.setImgId(img_id2[i]);
            listContentArr2.add(listData);
        }

        RecyclerView recyclerView2 = (RecyclerView) findViewById(R.id.categoryItem);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new GridLayoutManager(this, 3));
        venueCategoryListItem = new CategoryListAdapter(this);
        venueCategoryListItem.setListContent(listContentArr);
        recyclerView2.setAdapter(venueCategoryListItem);

        RecyclerView recyclerView4 = (RecyclerView) findViewById(R.id.destCategoryItem);
        recyclerView4.setHasFixedSize(true);
        recyclerView4.setLayoutManager(new GridLayoutManager(this, 3));
        buildingCategoryListItem = new CategoryListAdapter(this);
        buildingCategoryListItem.setListContent(listContentArr1);
        recyclerView4.setAdapter(buildingCategoryListItem);

        RecyclerView recyclerView3 = (RecyclerView) findViewById(R.id.destCategoryItem);
        recyclerView3.setHasFixedSize(true);
        recyclerView3.setLayoutManager(new GridLayoutManager(this, 3));
        landmarkCategoryListItem = new CategoryListAdapter(this);
        landmarkCategoryListItem.setListContent(listContentArr2);
        recyclerView3.setAdapter(landmarkCategoryListItem);

        venueCategoryListItem.setOnItemClickListener(new CategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String catName = listContentArr.get(position).getDescription();
                Log.d("onCLick", listContentArr.get(position).getDescription());
                if(catName == "Banking") {
                    if(venueListCategory.containsKey(catName)){

                    } else{
                      Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                } else if(catName == "Education") {
                    if(venueListCategory.containsKey("School/College/University")){
                        ArrayList<DropDownListData> listContentArr = new ArrayList<>();
                        for(String s: venueListCategory.get("School/College/University")) {
                            try{
                                if(venueAPIData.containsKey(s)){
                                    JSONObject jsonObject =  new JSONObject(venueAPIData.get(s));
                                    Log.d("jsonObject",jsonObject.getString("address"));
                                }

//                                //Add data to POJO class
//                                DropDownListData listData = new DropDownListData();
//                                listData.setText1(venueName);
//                                listData.setText2(address);
//                                listData.setTime(oneDecimalForm.format(d));
//                                listContentArr.add(listData);
                            } catch (Exception e) {
                                Log.d("Exception", String.valueOf(e));
                            }

                        }


//                        if(venueList.size() > 0 ) {
//                            getNearestVenue();
//                        }
//                        sourceVenueList = (RecyclerView) findViewById(R.id.dropdownVenuelist);
//                        sourceVenueList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
//                        sourceVenueListAdapter = new DropDownListAdapter(NavigationBlind.this);
//                        sourceVenueListAdapter.setListContent(listContentArr);
//                        sourceVenueList.setAdapter(sourceVenueListAdapter);
//
//                        destVenueList = (RecyclerView) findViewById(R.id.dropdownDestVenuelist);
//                        destVenueList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
//                        destVenueListAdapter = new DropDownListAdapter(NavigationBlind.this);
//                        destVenueListAdapter.setListContent(listContentArr);
//                        destVenueList.setAdapter(destVenueListAdapter);
//
//                        sourceVenueListAdapter.notifyDataSetChanged();
//                        destVenueListAdapter.notifyDataSetChanged();
                    } else{
                        Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                } else if(catName == "Health") {
                    if(venueListCategory.containsKey("Hospital")){

                    } else{
                        Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                } else if(catName == "Hospitality") {
                    if(venueListCategory.containsKey(catName)){

                    } else{
                        Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                } else if(catName == "Public Building") {
                    if(venueListCategory.containsKey("Museum")){

                    } else{
                        Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                } else if(catName == "Shopping") {
                    if(venueListCategory.containsKey(catName)){

                    } else{
                        Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                } else if(catName == "Transparent") {
                    if(venueListCategory.containsKey(catName)){

                    } else{
                        Toast.makeText(NavigationBlind.this, "No venue present in this category",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        queueLock = new ReentrantLock();
        handler = new Handler();
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            //Device doesn't have
            Log.e("SensorError", "Device doesn't have enough hardware to perform activity_navigation");
            takeToWebApp();
            finish();
        }

        if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("SensorError", "Device doesn't have accelerometer");
            takeToWebApp();
            finish();
        }

        simpleSteep = new SteepDetector(this);
        simpleSteep.registerListener(this);
        dah = new DeviceAttitudeHandler(sm);
        BluetoothAdapter mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        base_url = getApplicationContext().getResources().getString(R.string.server_base_url);

        MyRequestQueue = Volley.newRequestQueue(this);
        shouldExecuteOnResume = false; //to prevent onresume to be called at the start
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        step_size = (double) prefs.getFloat("step_size", 1.67f);
        audio_language = prefs.getString("language", "en");
        welcomeView = findViewById(R.id.welcomeView);
        if (audio_language.equals("hi")) {
            welcomeView.setText("नेविगेशन में आपका स्वागत है | ऊपर दिए बटन्स का प्रयोग कीजिये नेविगेशन के लिए");
        }
        loadingView = findViewById(R.id.loadingView);

        //disabling any touch actions on loadingView and horizontalScrollView
//        loadingView.setOnTouchListener((view, motionEvent) -> true);
//        hsv.setOnTouchListener((view, motionEvent) -> true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        activity_width = displayMetrics.widthPixels;
        activity_height = displayMetrics.heightPixels;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                        }
                    }
                });

        getVenueList();
        initSpinners(); //initializes adapters and list for source and destination dropdown spinners
        initializeActivity();
        ufo = new UFOBeaconManager(this.getApplicationContext());
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    }
    private void sourceToggleView() {
        viewAllDisable = findViewById(R.id.viewAllDisable);
        viewAllEnable = findViewById(R.id.viewAllEnable);
        sortDisable = findViewById(R.id.sortDisable);
        sortEnable = findViewById(R.id.sortEnable);

        amenitiesDisable = findViewById(R.id.amenitiesDisable);
        amenitiesEnable = findViewById(R.id.amenitiesEnable);
        roomsDisable = findViewById(R.id.roomsDisable);
        roomsEnable = findViewById(R.id.roomsEnable);

        toggleCategoryView = findViewById(R.id.toggleCategoryView);
        landmarkListLayout = findViewById(R.id.landmarkListLayout);
        toggleLandmarkList = findViewById(R.id.toggleLandmarkList);
        toggleLandmarkListInside = findViewById(R.id.toggleLandmarkListInside);

        filterSortLayout = findViewById(R.id.filterSortLayout);

        toggleLandmarkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLandmarkList.setVisibility(View.GONE);
                landmarkListLayout.setVisibility(View.VISIBLE);
                categoryView.setVisibility(View.GONE);
            }
        });
        toggleLandmarkListInside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLandmarkList.setVisibility(View.VISIBLE);
                landmarkListLayout.setVisibility(View.GONE);
                categoryView.setVisibility(View.VISIBLE);
            }
        });
        toggleCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryView.setVisibility(View.VISIBLE);
                toggleLandmarkList.setVisibility(View.VISIBLE);
                landmarkListLayout.setVisibility(View.GONE);
            }
        });

        viewAllDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAllDisable.setVisibility(View.GONE);
                viewAllEnable.setVisibility(View.VISIBLE);
                sortEnable.setVisibility(View.GONE);
                sortDisable.setVisibility(View.VISIBLE);
                filterSortLayout.setVisibility(View.GONE);
            }
        });
        viewAllEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAllDisable.setVisibility(View.VISIBLE);
                viewAllEnable.setVisibility(View.GONE);
                sortEnable.setVisibility(View.VISIBLE);
                sortDisable.setVisibility(View.GONE);
                filterSortLayout.setVisibility(View.VISIBLE);
            }
        });

        sortEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAllDisable.setVisibility(View.GONE);
                viewAllEnable.setVisibility(View.VISIBLE);
                sortEnable.setVisibility(View.GONE);
                sortDisable.setVisibility(View.VISIBLE);
                filterSortLayout.setVisibility(View.GONE);
            }
        });
        sortDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAllDisable.setVisibility(View.VISIBLE);
                viewAllEnable.setVisibility(View.GONE);
                sortEnable.setVisibility(View.VISIBLE);
                sortDisable.setVisibility(View.GONE);
                filterSortLayout.setVisibility(View.VISIBLE);
            }
        });

        roomsDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomsDisable.setVisibility(View.GONE);
                roomsEnable.setVisibility(View.VISIBLE);
                amenitiesEnable.setVisibility(View.VISIBLE);
                amenitiesDisable.setVisibility(View.GONE);
            }
        });
        roomsEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomsDisable.setVisibility(View.VISIBLE);
                roomsEnable.setVisibility(View.GONE);
                amenitiesEnable.setVisibility(View.GONE);
                amenitiesDisable.setVisibility(View.VISIBLE);
            }
        });

        amenitiesDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomsDisable.setVisibility(View.GONE);
                roomsEnable.setVisibility(View.VISIBLE);
                amenitiesEnable.setVisibility(View.VISIBLE);
                amenitiesDisable.setVisibility(View.GONE);
            }
        });
        amenitiesEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomsDisable.setVisibility(View.VISIBLE);
                roomsEnable.setVisibility(View.GONE);
                amenitiesEnable.setVisibility(View.GONE);
                amenitiesDisable.setVisibility(View.VISIBLE);
            }
        });
    }

    private void destToggleView() {
        destViewAllDisable = findViewById(R.id.destViewAllDisable);
        destViewAllEnable = findViewById(R.id.destViewAllEnable);
        destSortDisable = findViewById(R.id.destSortDisable);
        destSortEnable = findViewById(R.id.destSortEnable);

        destAmenitiesDisable = findViewById(R.id.destAmenitiesDisable);
        destAmenitiesEnable = findViewById(R.id.destAmenitiesEnable);
        destRoomsDisable = findViewById(R.id.destRoomsDisable);
        destRoomsEnable = findViewById(R.id.destRoomsEnable);

        toggleDestCategoryView = findViewById(R.id.toggleDestCategoryView);
        destLandmarkListLayout = findViewById(R.id.destLandmarkListLayout);
        toggleDestLandmarkList = findViewById(R.id.toggleDestLandmarkList);
        toggleDestLandmarkListInside = findViewById(R.id.toggleDestLandmarkListInside);

        destFilterSortLayout = findViewById(R.id.destFilterSortLayout);

        toggleDestLandmarkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDestLandmarkList.setVisibility(View.GONE);
                destLandmarkListLayout.setVisibility(View.VISIBLE);
                destCategoryView.setVisibility(View.GONE);
            }
        });
        toggleDestLandmarkListInside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDestLandmarkList.setVisibility(View.VISIBLE);
                destLandmarkListLayout.setVisibility(View.GONE);
                destCategoryView.setVisibility(View.VISIBLE);
            }
        });
        toggleCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destCategoryView.setVisibility(View.VISIBLE);
                toggleDestLandmarkList.setVisibility(View.VISIBLE);
                destLandmarkListLayout.setVisibility(View.GONE);
            }
        });

        destViewAllDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destViewAllDisable.setVisibility(View.GONE);
                destViewAllEnable.setVisibility(View.VISIBLE);
                destSortEnable.setVisibility(View.GONE);
                destSortDisable.setVisibility(View.VISIBLE);
                destFilterSortLayout.setVisibility(View.GONE);
            }
        });
        destViewAllEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destViewAllDisable.setVisibility(View.VISIBLE);
                destViewAllEnable.setVisibility(View.GONE);
                destSortEnable.setVisibility(View.VISIBLE);
                destSortDisable.setVisibility(View.GONE);
                destFilterSortLayout.setVisibility(View.VISIBLE);
            }
        });

        destSortEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destViewAllDisable.setVisibility(View.GONE);
                destViewAllEnable.setVisibility(View.VISIBLE);
                destSortEnable.setVisibility(View.GONE);
                destSortDisable.setVisibility(View.VISIBLE);
                destFilterSortLayout.setVisibility(View.GONE);
            }
        });
        destSortDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destViewAllDisable.setVisibility(View.VISIBLE);
                destViewAllEnable.setVisibility(View.GONE);
                destSortEnable.setVisibility(View.VISIBLE);
                destSortDisable.setVisibility(View.GONE);
                destFilterSortLayout.setVisibility(View.VISIBLE);
            }
        });

        destRoomsDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destRoomsDisable.setVisibility(View.GONE);
                destRoomsEnable.setVisibility(View.VISIBLE);
                destAmenitiesEnable.setVisibility(View.VISIBLE);
                destAmenitiesDisable.setVisibility(View.GONE);
            }
        });
        destRoomsEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destRoomsDisable.setVisibility(View.VISIBLE);
                destRoomsEnable.setVisibility(View.GONE);
                destAmenitiesEnable.setVisibility(View.GONE);
                destAmenitiesDisable.setVisibility(View.VISIBLE);
            }
        });

        destAmenitiesDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destRoomsDisable.setVisibility(View.GONE);
                destRoomsEnable.setVisibility(View.VISIBLE);
                destAmenitiesEnable.setVisibility(View.VISIBLE);
                destAmenitiesDisable.setVisibility(View.GONE);
            }
        });
        destAmenitiesEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destRoomsDisable.setVisibility(View.VISIBLE);
                destRoomsEnable.setVisibility(View.GONE);
                destAmenitiesEnable.setVisibility(View.GONE);
                destAmenitiesDisable.setVisibility(View.VISIBLE);
            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    private void takeToWebApp() {
        Intent i = new Intent(NavigationBlind.this, WebviewActivity.class);
        i.putExtra("URL",getApplicationContext().getResources().getString(R.string.webapp));
        startActivity(i);
    }

    private void animatePath() {
//        pathFloorObjs.get(sliderViewCounter).getCanvasView().animatePath();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void getVenueList() {
        String vURL = base_url+"v1/app/venue-list";
        loadingView.setVisibility(View.VISIBLE);
        StringRequest req = new StringRequest(Request.Method.POST, vURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        loadingView.setVisibility(View.GONE);
                        try {
                            JSONObject jsonObject= new JSONObject(response.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            ArrayList<DropDownListData> listContentArr = new ArrayList<>();
                            JSONObject jsonObject1 = new JSONObject();

                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject node = jsonArray.getJSONObject(i);
                                String venueName = node.getString("venueName");
                                String address = node.getString("address");
                                JSONArray coordinatesJSON = node.getJSONArray("coordinates");
                                String venueType = node.getString("type");
                                double lat = (double) coordinatesJSON.get(0);
                                double lng = (double) coordinatesJSON.get(1);
                                double d =  distance(latitude, longitude, lat, lng);
                                StringBuilder venueNameNew = new StringBuilder();
                                for (String s : Utils.splitCamelCaseString(venueName)) {
                                    venueNameNew.append(s).append(" ");
                                }
                                jsonObject1.put("address", address);
                                jsonObject1.put("d", d);
                                venueAPIData.put(venueName,jsonObject1.toString());
                                venueData.add(d);
                                venueList.add(venueNameNew.toString().trim());

                                //Add data to POJO class
                                DropDownListData listData = new DropDownListData();
                                listData.setText1(venueName);
                                listData.setText2(address);
                                listData.setTime(oneDecimalForm.format(d));
                                listContentArr.add(listData);

                                if(!venueListCategory.containsKey(venueType)){
                                    ArrayList<String> arr = new ArrayList<>();
                                    venueListCategory.put(venueType,arr);
                                }
                                venueListCategory.get(venueType).add(venueName);
                                Log.e("venueListCategory", String.valueOf(venueListCategory) + " : " + venueAPIData);
                            }

                            if(venueList.size() > 0 ) {
                                getNearestVenue();
                            }
                            sourceVenueList = (RecyclerView) findViewById(R.id.dropdownVenuelist);
                            sourceVenueList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
                            sourceVenueListAdapter = new DropDownListAdapter(NavigationBlind.this);
                            sourceVenueListAdapter.setListContent(listContentArr);
                            sourceVenueList.setAdapter(sourceVenueListAdapter);

                            destVenueList = (RecyclerView) findViewById(R.id.dropdownDestVenuelist);
                            destVenueList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
                            destVenueListAdapter = new DropDownListAdapter(NavigationBlind.this);
                            destVenueListAdapter.setListContent(listContentArr);
                            destVenueList.setAdapter(destVenueListAdapter);

                            sourceVenueListAdapter.notifyDataSetChanged();
                            destVenueListAdapter.notifyDataSetChanged();

                            loadingView.setVisibility(View.GONE);
                            initVenueListClicks();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingView.setVisibility(View.GONE);
                        if (error instanceof NetworkError) {
                            retryPopupVenueList("Cannot connect to Internet. Please check your connection!");
                        } else if (error instanceof ServerError) {
                            retryPopupVenueList("Server error!");
                        } else if (error instanceof AuthFailureError) {
                            retryPopupVenueList("Server error!");
                        } else if (error instanceof ParseError) {
                            retryPopupVenueList("Some error occured!");
                        } else if (error instanceof NoConnectionError) {
                            retryPopupVenueList("Cannot connect to Internet...Please check your connection!");
                        } else if (error instanceof TimeoutError) {
                            retryPopupVenueList("Connection TimeOut! Please check your internet connection.");
                        }
                        else{
                            retryPopupVenueList("Server error occured!");
                        }
                    }
                });

        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);
    }

    private void getNearestVenue() {
        Log.e("getNearestVenue", ": " + venueList + ", " + currentVenue);
        double min = (double) venueData.get(0);
        int index = 0;
        for(int i=0;i<venueData.size();i++) {
            if(venueData.get(i) < min) {
                min = venueData.get(i);
                index = i;
            }
        }
        if(min < 1.0) {
            currentVenue = venueList.get(index).trim();
            sourceVenueEditText.setText(currentVenue);
            destVenueEditText.setText(currentVenue);
            currentBuilding = null;
            buildingList.clear();
            hideKeyBoard();
            String selectedVenue = venueList.get(index);
            StringBuilder actualVenueName = new StringBuilder();
            for (String s : selectedVenue.split(" ")) {
                actualVenueName.append(s);
            }
            Log.e("getNearestVenue", "venueList ; " + venueList + ", " + currentVenue + ", actualVenueName:  " + actualVenueName.toString());
            getBuildingList(actualVenueName.toString(), "NV");
        }

    }

    private void initVenueListClicks() {
        sourceVenueListAdapter.setOnItemClickListener(new DropDownListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String sourceName = venueList.get(position);
                currentVenue = sourceName.trim();
                destVenueEditText.setText(sourceName);
                searchSourceDialog.setText("Starting Location set to " + currentVenue);
                sourceVenueEditText.setText("Search within "+ sourceName);
                categoryView.setVisibility(View.VISIBLE);
                venueListLayout.setVisibility(View.GONE);
                sourceBuildingEditText.setVisibility(View.VISIBLE);
                buildingListLayout.setVisibility(View.GONE);
                destinationBuildingEditText.setText("");
                currentBuilding = null;
                buildingList.clear();
                hideKeyBoard();
                getBuildingList(currentVenue, "CV");
            }
        });

        destVenueListAdapter.setOnItemClickListener(new DropDownListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                welcomeView.setVisibility(View.GONE); //remove the welcome View
                String destName = venueList.get(position);
                if(currentVenue == null) {
                    destVenueEditText.setError("Please select source Venue first");
                }else if(currentVenue != null && !destName.matches(currentVenue)) {
                    destVenueEditText.setError("Please select same Venue as source");
                } else {
                    searchDestinationDialog.setText("Starting Location set to " + currentVenue);
                    destVenueEditText.setText("Serach within " + destName);
                    destCategoryView.setVisibility(View.VISIBLE);
                    destVenueListLayout.setVisibility(View.GONE);
                    destinationBuildingEditText.setVisibility(View.VISIBLE);
                    destBuildingListLayout.setVisibility(View.GONE);
                    currentBuilding = null;
                    buildingList.clear();
                    getBuildingList(currentVenue, "CV");
                    modal.setVisibility(View.GONE);
                    modalSource.setVisibility(View.GONE);
                    hideKeyBoard();
                    destVenueEditText.clearFocus();
                }
                hideKeyBoard();
            }
        });
    }

    private void  getBuildingList(String venue, String venueType) {
        String building_list_url = base_url+"v1/app/building-list";
        HashMap<String, String> params = new HashMap<>();
        params.put("venueName", venue);
        loadingView.setVisibility(View.VISIBLE);
        JsonObjectRequest req = new JsonObjectRequest(building_list_url, new JSONObject(params),
                response -> {
                    loadingView.setVisibility(View.GONE);
                    Log.e("respomse", "res : " + response + ", venueType : " + venueType);
                    try {
                        if(response.getBoolean("success")){
                            //save to sharedPrefs and lead to PermissionRequestActivity
                            JSONArray data = response.getJSONArray("data");
                            ArrayList<DropDownListData> listContentArr = new ArrayList<>();;
                            for(int j=0;j<data.length();j++){
                                String nodeName = data.getJSONObject(j).getString("buildingName");
                                String address = data.getJSONObject(j).getString("address");
                                String buildingType = data.getJSONObject(j).getString("buildingType");
                                double lat = (double) data.getJSONObject(j).getDouble("lat");
                                double lng = (double) data.getJSONObject(j).getDouble("lng");
                                double d =  distance(latitude, longitude, lat, lng);
//                                StringBuilder buildingNameNew = new StringBuilder();
//                                for (String s : Utils.splitCamelCaseString(venueName)) {
//                                    buildingNameNew.append(s).append(" ");
//                                }
//                                Log.e("distance", d + ", " + buildingNameNew.toString() + " "  + nodeName );
                                buildingList.add(nodeName);
                                buildingData.add(d);

                                //Add data to POJO class
                                DropDownListData listData = new DropDownListData();
                                listData.setText1(nodeName);
                                listData.setText2(address);
                                listData.setTime(oneDecimalForm.format(d));
                                listContentArr.add(listData);

                                if(!buildingListCategory.containsKey(buildingType)){
                                    ArrayList<String> arr = new ArrayList<>();
                                    buildingListCategory.put(buildingType,arr);
                                }
                                buildingListCategory.get(buildingType).add(nodeName);

                            }
                            if(venueType == "NV" && buildingList.size() > 0) {
                                getNearestBuilding();
                            }
                            Collections.sort(buildingList); //search is easier when list is sorted

                            sourceBuildingList = (RecyclerView) findViewById(R.id.dropdownBuildinglist);
                            sourceBuildingList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
                            sourceBuildingListAdapter = new DropDownListAdapter(NavigationBlind.this);
                            sourceBuildingListAdapter.setListContent(listContentArr);
                            sourceBuildingList.setAdapter(sourceBuildingListAdapter);

                            destBuildingList = (RecyclerView) findViewById(R.id.destDropdownBuildinglist);
                            destBuildingList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
                            destBuildingListAdapter = new DropDownListAdapter(NavigationBlind.this);
                            destBuildingListAdapter.setListContent(listContentArr);
                            destBuildingList.setAdapter(destBuildingListAdapter);

                            sourceBuildingListAdapter.notifyDataSetChanged();
                            destBuildingListAdapter.notifyDataSetChanged();
                            initBuildingListClicks();
                        }
                        else{
                            errorPopup(response.getString("error"));
                        }
                    } catch (JSONException e) {
                        errorPopup("Some error occurred!");
                        e.printStackTrace();
                    }
                }, error -> {
            loadingView.setVisibility(View.GONE);
            if (error instanceof NetworkError) {
                errorPopup("Cannot connect to Internet. Please check your connection!");
            } else if (error instanceof ServerError) {
                errorPopup("Server error!");
            } else if (error instanceof AuthFailureError) {
                errorPopup("Server error!");
            } else if (error instanceof ParseError) {
                errorPopup("Some error occurred!");
            } else if (error instanceof NoConnectionError) {
                errorPopup("Cannot connect to Internet...Please check your connection!");
            } else if (error instanceof TimeoutError) {
                errorPopup("Connection TimeOut! Please check your internet connection.");
            }
            else{
                errorPopup("Server error occurred!");
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);
    }

    private void getNearestBuilding() {
        double min = buildingData.get(0);
        int index = 0;
        for(int i=0;i<buildingData.size();i++) {
            if(buildingData.get(i) < min) {
                min = buildingData.get(i);
                index = i;
            }
        }
        if(min < 1.0) {
            currentBuilding = buildingList.get(index).trim();
            sourceBuildingEditText.setText(currentBuilding);
            destinationBuildingEditText.setText(currentBuilding);
            hideKeyBoard();
            Log.e("getNearestBuilding", "building : " + buildingList + ", " + currentBuilding);
            getBuildingInfo();
        }
    }

    private void initBuildingListClicks() {
        sourceBuildingListAdapter.setOnItemClickListener(new DropDownListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String sourceName = buildingList.get(position);
                currentBuilding = sourceName;
                destinationBuildingEditText.setText(sourceName);
                searchSourceDialog.setText("Starting Location set to " + currentBuilding + " Building, of "+ currentVenue);
                sourceVenueEditText.setText("search within " + sourceName);
                buildingListLayout.setVisibility(View.GONE);
                sourceBuildingEditText.setVisibility(View.GONE);
                toggleLandmarkList.setVisibility(View.VISIBLE);
                modal.setVisibility(View.GONE);
                modalSource.setVisibility(View.GONE);
                hideKeyBoard();
                getBuildingInfo();
            }
        });

        destBuildingListAdapter.setOnItemClickListener(new DropDownListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String destName = buildingList.get(position);
                if(currentBuilding != null) {
                    destinationBuildingEditText.setError("Please select source building first");
                }
                if(currentBuilding != null && !destName.matches(currentBuilding)) {
                    destinationBuildingEditText.setError("Please select same building as source");
                } else {
                searchDestinationDialog.setText("Starting Location set to " + currentBuilding + " Building, of "+ currentVenue);
                destVenueEditText.setText("search within " + destName);
                destBuildingListLayout.setVisibility(View.GONE);
                destinationBuildingEditText.setVisibility(View.GONE);
                toggleDestLandmarkList.setVisibility(View.VISIBLE);
                modal.setVisibility(View.GONE);
                modalSource.setVisibility(View.GONE);
                hideKeyBoard();
                destinationBuildingEditText.clearFocus();
                }
                hideKeyBoard();
            }
        });
    }

    private void initSpinners(){
        sourceLandmarkList = (RecyclerView) findViewById(R.id.dropdownLandmarklist);
        sourceLandmarkListAdapter = new DropDownListAdapter(this);
        sourceLandmarkList.setLayoutManager(new LinearLayoutManager(this));
        sourceLandmarkList.setAdapter(sourceLandmarkListAdapter);

        destLandmarkList = (RecyclerView) findViewById(R.id.destDropdownLandmarklist);
        destLandmarkListAdapter = new DropDownListAdapter(this);
        destLandmarkList.setLayoutManager(new LinearLayoutManager(this));
        destLandmarkList.setAdapter(destLandmarkListAdapter);
    }


    private void initializeActivity() {
        //initialize every thing required before showing to user, till then show some animations to user
        initializeTTS();
    }

    private void initializeTTS() {
        Log.e("initializeTTS", " : ");

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int ttsLang;
                if(audio_language.equals("en")){
                    ttsLang = textToSpeech.setLanguage(Locale.US);
                }
                else{
                    ttsLang = textToSpeech.setLanguage(new Locale("hin"));
                }

                if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                        || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported!");
                } else {
                    Log.i("TTS", "Language Supported.");
                }
                Log.i("TTS", "Initialization success.");
                ttsInitialized = true;
            } else {
                Log.e("TTS", "TTS initialization failed");
            }
        });
    }

    /*
    This function gets current floor's length,width and rotation through api.
    and thus sets total grids in X and total grids in Y using the building measurements.
    It also gets the nodes in the map.
    */
    private void getBuildingInfo(){
        Log.e("getBuildingInfo", " : " + buildingName);

        //gets all nodes of this building
        handler.post(() -> {
            //check if building is in cache or not
            String existingData = mCacheHelper.dataGivenBuilding(buildingName);
//                String existingData = null;
            if(existingData!=null){
                try {
                    processBuildingData(new JSONArray(existingData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                makeBuildingDataRequest();
            }
        });

    }

    private void makeBuildingDataRequest() {
        String allNodesURL = "";
        Log.e("currentVenue", " : " + currentVenue + " : " + currentBuilding);
        if(currentVenue!=null && currentVenue.length() > 0 && currentBuilding!=null && currentBuilding.length() > 0) {
            allNodesURL = base_url + "v1/app/android-navigation/" + currentVenue + "/" + currentBuilding + "/null";
            JsonArrayRequest getAllNodesReq = new JsonArrayRequest
                    (Request.Method.GET, allNodesURL, null, response -> {

                        processBuildingData(response);
                        mCacheHelper.insertData("abcd", buildingName, response.toString(), System.currentTimeMillis());
                    }, volleyError -> {
                        if (volleyError instanceof NetworkError) {
                            retryPopup("Cannot connect to Internet...Please check your connection!");
                        } else if (volleyError instanceof ServerError) {
                            retryPopup("Server error!");
                        } else if (volleyError instanceof AuthFailureError) {
                            retryPopup("Server error!");
                        } else if (volleyError instanceof ParseError) {
                            retryPopup("Some error occured!");
                        } else if (volleyError instanceof NoConnectionError) {
                            retryPopup("Cannot connect to Internet...Please check your connection!");
                        } else if (volleyError instanceof TimeoutError) {
                            retryPopup("Connection TimeOut! Please check your internet connection.");
                        } else {
                            retryPopup("Server error occured!");
                        }
                    });

            getAllNodesReq.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            getAllNodesReq.setShouldCache(false);
            getAllNodesReq.setTag(this);
            MyRequestQueue.getCache().clear();
            MyRequestQueue.add(getAllNodesReq);
        } else {
            Toast.makeText(NavigationBlind.this,"Unable to Locate your location, Please Select your building",Toast.LENGTH_LONG).show();
        }
    }

    //processes the response data
    private void processBuildingData(JSONArray response) {
        clearVariableList();
        //response array has object corresponding to different buildings
        try {
            ArrayList<DropDownListData> listContentArr = new ArrayList<>();

            for(int j=0;j<response.length();j++){
                JSONObject jsonObject = response.getJSONObject(j);
                String geometryType = jsonObject.getString("geometryType");
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                int coordinateX = jsonObject.getInt("coordinateX");
                int coordinateY = jsonObject.getInt("coordinateY");
                String nodeName = jsonObject.getString("name");
                String floorName = jsonObject.getString("floor");

                DropDownListData listData = new DropDownListData();
                double lat = (double) coordinateX;
                double lng = (double) coordinateY;
                double d =  distance(latitude, longitude, lat, lng);

                if(GeometryTypes.NODE.value().equalsIgnoreCase(geometryType)){
                    //node data in this jsonObject
                    String elementType = jsonObject.getJSONObject("element").getString("type");
                    String elementSubType = jsonObject.getJSONObject("element").getString("subType");
                    String coordinates = coordinateX+","+coordinateY;
                    nodeCoordToFloorElement.put(coordinates, elementType);
                    Point flrConnCoord = new Point(coordinateX,coordinateY);
                    if(ElementTypes.SERVICES.value().equalsIgnoreCase(elementType)
                            && "beacons".equalsIgnoreCase(elementSubType)
                    ){
                        String nodeMacId = propertiesObject.getString("macId").trim();
                        beaconObj.addBeacon(nodeMacId,floorName,coordinates);
                        alwaysActiveScansObj.addBeacon(nodeMacId);
                    }
                    else {
                        if(ElementTypes.FLOOR_CONNECTION.value().equalsIgnoreCase(elementType)){
                            String newNodeName = propertiesObject.getString("name");

                            if(!floor_to_floorConn_map.containsKey(floorName)){
                                floor_to_floorConn_map.put(floorName, new HashMap<>());
                            }

                            floor_to_floorConn_map.get(floorName).put(newNodeName, flrConnCoord);
                            nodeName = newNodeName+" of "+floorName+" floor";
                            nodeNameToDetails.put(nodeName, new String[]{floorName,coordinates});
                            sourcePointsList.add(nodeName);
                            destPointsList.add(nodeName);

                            //Add data to POJO class
                            listData.setText1(nodeName);
                            String s = currentBuilding + currentVenue;
                            listData.setText2(s);
                            listData.setTime(oneDecimalForm.format(d));
                            listContentArr.add(listData);
                        } else  {
                            nodeNameToDetails.put(nodeName, new String[]{floorName,coordinates});
                            sourcePointsList.add(nodeName);
                            destPointsList.add(nodeName);

                            //Add data to POJO class
                            listData.setText1(nodeName);
                            String s = currentBuilding + currentVenue;
                            listData.setText2(s);
                            listData.setTime(oneDecimalForm.format(d));
                            listContentArr.add(listData);
                        }

                    }



                    if(!floorToNodes.containsKey(floorName)){
                        ArrayList<String[]> arr = new ArrayList<>();
                        floorToNodes.put(floorName,arr);
                    }
                    String[] tup = {coordinates, nodeName, ""};
                    floorToNodes.get(floorName).add(tup); //just add to the nodes list of that floor

                }
                else if (GeometryTypes.FLOOR.value().equalsIgnoreCase(geometryType)){
                    //non walkables data in this jsonObject
                    int floorLength = propertiesObject.getInt("floorLength");
                    int floorBreadth = propertiesObject.getInt("floorBreadth");
                    String floorAngle = propertiesObject.getString("floorAngle");
                    String fileName = propertiesObject.getString("fileName");
                    floorToAngle.put(floorName,Float.parseFloat(floorAngle));
                    floorToNonWalkables.put(floorName, new ArrayList<>());
                    floorToSimplePoly.put(floorName, new ArrayList<>());
                    floorToImageName.put(floorName, fileName);

                    JSONArray nonWalkableGrids = propertiesObject.getJSONArray("nonWalkableGrids");
                    JSONArray nonWalkableClicks = propertiesObject.getJSONArray("clickedPoints");
                    JSONArray floorDistMatrix = propertiesObject.getJSONArray("flr_dist_matrix");
                    JSONArray frConn = propertiesObject.getJSONArray("frConn");
                    for(int k =0; k<nonWalkableClicks.length();k++){
                        String s = nonWalkableClicks.getString(k).trim();
                        if(s.length() > 0 ) {
                            floorToSimplePoly.get(floorName).add(s);
                        }
                    }

                    for(int k =0; k<nonWalkableGrids.length();k++){
                        String s = nonWalkableGrids.getString(k).trim();
                        if(s.length() > 0 ) {
                            floorToNonWalkables.get(floorName).add(s);
                        }
                    }
                    floorToflr_dist_matrix.put(floorName,floorDistMatrix.getString(0));
                    floorTofrConn.put(floorName, frConn.getString(0));
                    sourceFloorsList.add(floorName);
                    destFloorsList.add(floorName);

                    //Add data to POJO class
                    String s = currentBuilding + currentVenue;
                    listData.setText2(s);
                    listData.setTime(oneDecimalForm.format(d));
                    listContentArr.add(listData);

                    int[] dim = {floorLength, floorBreadth};
                    floorDim.put(floorName, dim);
                }


            }

//            Collections.sort(sourcePointsList); //search is easier when list is sorted
//            Collections.sort(destPointsList);

            sourceLandmarkList = (RecyclerView) findViewById(R.id.dropdownLandmarklist);
            sourceLandmarkList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
            sourceLandmarkListAdapter = new DropDownListAdapter(NavigationBlind.this);
            sourceLandmarkListAdapter.setListContent(listContentArr);
            sourceLandmarkList.setAdapter(sourceLandmarkListAdapter);

            destLandmarkList = (RecyclerView) findViewById(R.id.destDropdownLandmarklist);
            destLandmarkList.setLayoutManager(new LinearLayoutManager(NavigationBlind.this));
            destLandmarkListAdapter = new DropDownListAdapter(NavigationBlind.this);
            destLandmarkListAdapter.setListContent(listContentArr);
            destLandmarkList.setAdapter(destLandmarkListAdapter);

            destLandmarkListAdapter.notifyDataSetChanged();
            sourceLandmarkListAdapter.notifyDataSetChanged();

            connectorGraph = new ConnectorGraph(floor_to_floorConn_map,floorToNonWalkables,sourceFloorsList, floorDim);
            enableBLE();
            initSpinnersClicks(); //now we have data so update spinners clicks
        }
        catch(Exception e){
            e.printStackTrace();
            retryPopup("Server error!");
            Log.e("activityMain","JSON Exception in fetching buildings");
        }
    }

    private void clearVariableList() {
        sourceFloorsList.clear();
        destFloorsList.clear();
        sourcePointsList.clear();
        destPointsList.clear();
        nodeCoordToFloorElement.clear();
        floor_to_floorConn_map.clear();
        nodeNameToDetails.clear();
        floorToNodes.clear();
        floorToAngle.clear();
        floorToNonWalkables.clear();
        floorToSimplePoly.clear();
        floorToImageName.clear();
        floorToflr_dist_matrix.clear();
        floorTofrConn.clear();
        floorDim.clear();
        beaconObj = new BeaconDetails(5);
        alwaysActiveScansObj = new AlwaysActiveScans(beaconObj);
    }

    private void initSpinnersClicks(){

        sourceLandmarkListAdapter.setOnItemClickListener(new DropDownListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String sourceName = sourcePointsList.get(position);
                String[] S = nodeNameToDetails.get(sourceName);
                dah.updateFloorRotation(floorToAngle.get(S[0]));
                currLocation.setFloor(S[0]);
                currLocation.setName(sourceName);
                currLocation.setGridX(Integer.parseInt(S[1].split(",")[0]));
                currLocation.setGridY(Integer.parseInt(S[1].split(",")[1]));
                openSearchSourceDialogVisible = false;
                sourceViewToAnimate.setVisibility(View.GONE);
                searchSourceDialog.setText(sourceName);
                locationName = sourceName;
                floorName = S[0];
                modalSourceEnable();
                hideKeyBoard();
            }
        });

        destLandmarkListAdapter.setOnItemClickListener(new DropDownListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                welcomeView.setVisibility(View.GONE); //remove the welcome View
                String destName = destPointsList.get(position);
                if(destName.equals(locationName)){
                    errorPopup("Source and Destination can not be same, Please select another value");
                }
                String[] D = nodeNameToDetails.get(destName);
                destLocation.setFloor(D[0]);
                destLocation.setName(destName);
                destLocation.setGridX(Integer.parseInt(D[1].split(",")[0]));
                destLocation.setGridY(Integer.parseInt(D[1].split(",")[1]));

                openSearchDestinationDialogVisible = false;
                destinationViewToAnimate.setVisibility(View.GONE);
                searchDestinationDialog.setText(destName);
                hideKeyBoard();
                modalSource.setVisibility(View.GONE);
                if(locationName != null && destLocation.name().length() > 0) {
                    modalEnable();
                } else {
                    modal.setVisibility(View.GONE);
                }
                hideKeyBoard();
            }
        });

        startUFO();
        searchLocation();
    }

    private void modalEnable() {
        modal.setVisibility(View.VISIBLE);
        modalDestinationName.setText(destLocation.name() + ", " + destLocation.getFloor() + " floor");
        modalVenueName.setText(currentBuilding+ ", " +currentVenue);
    }

    private void modalSourceEnable() {
        modalSource.setVisibility(View.VISIBLE);
        modalSourceName.setText(locationName + ", " + floorName + " floor");
        modalSourceVenueName.setText(currentBuilding+ ", " +currentVenue);
    }

    public void searchPath() {
        //search path is a heavy function so avoid running it on main thread/ui thread, run it on different thread instead
        pathSearchThread = () -> {

            queueLock.lock();
            try {
                //show loading view while the path is being searched
                loadingView.setVisibility(View.VISIBLE);
                mainLogicThreadRunning = false;
                disableBLE();

                //update lastSource
                lastSource.setFloor(currLocation.getFloor());
                lastSource.setGridX(currLocation.getGridX());
                lastSource.setGridY(currLocation.getGridY());


                //assuming we have source floor, dest floor, sourceLocation , destLocation

                if(currLocation.getFloor().equals(destLocation.getFloor())
                        && currLocation.getGridX()==destLocation.getGridX() && currLocation.getGridY()==destLocation.getGridY()){
                    //source and destination are same so no point in searching
                    Toast.makeText(getApplicationContext(),"source and destination are same",Toast.LENGTH_SHORT).show();
                    sourceDest.setVisibility(View.VISIBLE);
                    enableBLE();
                    mainLogicThreadRunning = true;
                    //remove the loading view
                    loadingView.setVisibility(View.GONE);
                    return;
                }

                reset();

                /*
                    ConnectorGraph will return FloorObj in order where each FloorObj will have its name, path, floor_image_url
                    Using urls we will inflate the views and assign the views to corresponding FloorObjects
                */
                connectorGraph.updateSourceAndDestination(currLocation,destLocation);
                ArrayList<PathOption> pathOptions = connectorGraph.executeDijkstra();
                for(PathOption p : pathOptions){
                    Log.e("DIJKSTRA",p.getPassingFrom()+": "+p.getPathDistance());
                }

                showPathOptionsModal(pathOptions);

            }
            finally {
                enableBLE();
                queueLock.unlock();
            }
        };
        handler.post(pathSearchThread);
    }

    /*
      @params: List of path options
      @work: shows a dialog which asks user to select a path only if path options are more than one
     */
    private void showPathOptionsModal(final ArrayList<PathOption> pathOptions) {
        //remove the loading view
        loadingView.setVisibility(View.GONE);

        if(pathOptions.size()==1){
            //no need to show modal when there is only 1 path
            handlePath(pathOptions.get(0).getPath());
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationBlind.this);

        if(audio_language.equals("hi")){
            builder.setTitle("कृपया एक रास्ता चुने");
        }
        else{
            builder.setTitle("Choose a path");
        }

        String[] options = new String[pathOptions.size()];
        for(int i=0;i<pathOptions.size();i++){
            PathOption p = pathOptions.get(i);
            if(audio_language.equals("hi")){
                options[i] = p.getPassingFrom() + " का प्रयोग करके , दूरी : "+ oneDecimalForm.format(p.getPathDistance()) +" फीट";
            }
            else{
                options[i] = "Using "+ p.getPassingFrom() + ", Distance: "+ oneDecimalForm.format(p.getPathDistance()) +" फीट";
            }

        }
        builder.setItems(options, (dialog, index) -> handlePath(pathOptions.get(index).getPath()));
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void handlePath(ArrayList<FloorObj> path) {
        pathFloorObjs.addAll(path);
        if(pathFloorObjs.size()==0){
            Toast.makeText(getApplicationContext(),"Path doesn't exist",Toast.LENGTH_SHORT).show();
            maxSliderViewCounter = 1;
            enableBLE();
            mainLogicThreadRunning = true;
            //remove the loading view
            loadingView.setVisibility(View.GONE);
            return;
        }


        pathsearched = true;
        currDisplayIndexOfPath=0;
        jumpingOutStarted = false;
        jumpedOut = false;
        //we are sure now pathFloorObjs size>0
        //inflate sliderView
        maxSliderViewCounter = pathFloorObjs.size();

        String BASE_STATIC_URL = getResources().getString(R.string.STATIC_URL);

        int viewHeight;
        int viewWidth;
        double aspectRatio;
        for(int i=0; i<pathFloorObjs.size();i++){
            FloorObj fobj = pathFloorObjs.get(i);
            fobj.setLengthAndBreadth(floorDim.get(fobj.getFloorName()));

            aspectRatio = fobj.getLength()*1.0/fobj.getBreadth();
            if(fobj.getLength()>fobj.getBreadth()){
                //width is greater than height so fit to height and scale width
                // if scaled_width is less than activity_width then
                // scale whole image so that it fits activity_width
                viewHeight = activity_height;
                viewWidth = (int) (aspectRatio*viewHeight);
                if(viewWidth<activity_width){
                    double scaleToMult = activity_width*1.0/viewWidth;
                    viewWidth = activity_width;
                    viewHeight = (int)(scaleToMult*viewHeight);
                }
            }
            else{
                //width is smaller than height so fit to width and scale height
                // if scaled_height is less than activity_height then
                // scale whole image so that it fits activity_height
                viewWidth = activity_width;
                viewHeight = (int) (viewWidth/aspectRatio);
                if(viewHeight<activity_height){
                    double scaleToMult = activity_height*1.0/viewHeight;
                    viewHeight = activity_height;
                    viewWidth = (int)(scaleToMult*viewWidth);
                }
            }

            fobj.setGpx(viewWidth*1.0/floorDim.get(fobj.getFloorName())[0]);
            fobj.setGpy(viewHeight*1.0/floorDim.get(fobj.getFloorName())[1]);
            fobj.setViewWidth(viewWidth);
            fobj.setViewHeight(viewHeight);

            String imgUrl = BASE_STATIC_URL+floorToImageName.get(fobj.getFloorName());

            SliderItem sItem = new SliderItem(viewWidth,viewHeight,imgUrl,fobj.getFloorName());
            //SliderViewInflator also assigns the corresponding canvas to fobj
            final SliderViewInflator sv = new SliderViewInflator(NavigationBlind.this,null,sItem,fobj,activity_width,activity_height);
//            sliderView.addView(sv);

            //add simple NW polys which will be useful in deciding whether a step transition is valid or not
            for(String s: floorToSimplePoly.get(fobj.getFloorName())){
                fobj.addNWSimplePoly(s);
            }

            turningPointsMap.put(fobj.getFloorName(), new HashSet<>());
            //before interpolating, add turning points obtained from simple path in fobj.getpath()
            //a path with size>2 will only have turning points
            if(fobj.getPath().size()>2){
                for(Point p: fobj.getPath().subList(1,fobj.getPath().size()-1)){
                    turningPointsMap.get(fobj.getFloorName()).add(new Pair<>((int) (p.x * fobj.getGpx()), (int) (p.y * fobj.getGpy())));

                }
            }

            //interpolate the simple points path using gpx and gpy
            fobj.setPath(Utils.interpolatePath(fobj.getPath(),step_size, fobj.getGpx(), fobj.getGpy()));

            CanvasView c = fobj.getCanvasView();
            c.updateDotSizes(fobj.getLength(),fobj.getBreadth()); //update the size of path dots according to floor lengths
            c.updateEndPointName(fobj.getEndPointName());
            c.drawPath(fobj.getPath()); //draw corresponding path in that canvas
            ZoomRotationFrameLayout parentZoomLayout = (ZoomRotationFrameLayout) c.getParent();
            Point[] boundingBoxCorners = Utils.boundingBoxCorners(fobj.getPath());
            parentZoomLayout.updateBoundingBoxConstants(boundingBoxCorners,activity_width,activity_height);
            parentZoomLayout.zoomToBoundingBox();


        }

        FloorObj startFloor = pathFloorObjs.get(0);
        //set current location in pixels, which is basically the source in searched path
        xf = currLocation.getGridX()*startFloor.getGpx();
        yf = currLocation.getGridY()*startFloor.getGpy();
        dah.updateCanvasLocation((int)xf,(int)yf);
        dah.updateCanvasPrecision(100, startFloor.getGpx(), startFloor.getGpy());
        dah.enableDrawOnCanvas(pathFloorObjs.get(0).getCanvasView(), (ZoomRotationFrameLayout)pathFloorObjs.get(0).getCanvasView().getParent());

//        inflateLevelsView();


        stopAndOrientUser(startFloor);

        mainLogicThreadRunning = true;

    }

    @SuppressLint("NewApi")
    private void inflateLevelsView() {
        //inflates the levels view shown on the right hand side (L0,L1,etc)
        floorLevelLayout.removeAllViews();
        floorLevelLayoutInfo.removeAllViews();
        sliderViewCounter=0;
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i=pathFloorObjs.size()-1; i>=0;i--){
            View v = inflator.inflate(R.layout.simple_floor_card,null);
            TextView simpleFloorCardItem = v.findViewById(R.id.simple_floor_card_img);
            simpleFloorCardItem.setText("L"+(i+1));
            simpleFloorCardItem.setTooltipText(pathFloorObjs.get(i).getFloorName()+" floor");
            int size = getResources().getDimensionPixelSize(R.dimen._35sdp);
            simpleFloorCardItem.setLayoutParams(new LinearLayout.LayoutParams(size,size));
            floorLevelLayout.addView(v);
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(size,size));
            if(i==0){
                simpleFloorCardItem.setBackgroundColor(Color.parseColor("#4D000000"));
                img.setPadding(40,40,40,40);
                img.setImageResource(R.drawable.green_circle);
            }
            if(i==pathFloorObjs.size()-1){
                img.setPadding(40,40,40,40);
                img.setImageResource(R.drawable.red_circle);
            }
            floorLevelLayoutInfo.addView(img);

            simpleFloorCardItem.setOnClickListener(view -> {
                floorLevelLayout.getChildAt(pathFloorObjs.size()-1-sliderViewCounter).setBackgroundColor(0);
                simpleFloorCardItem.setBackgroundColor(Color.parseColor("#4D000000"));

                //get slider counter from name's end digit
                sliderViewCounter = Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1))-1;
                //accumulate width to scroll in variable
                int scrollWidth = 0;
                for(int j=0;j<sliderViewCounter;j++){
//                    scrollWidth += sliderView.getChildAt(j).getWidth();
                }

//                hsv.smoothScrollTo(scrollWidth,hsv.getScrollY());
                animatePath();
            });
        }

//        floorLevelLayout.post(() -> { if(floorLevelLayoutVisible){showLevelsButton.performClick(); } });
    }

    private FloorObj getFloorObjGivenName(String floor_name){
        for(FloorObj fobj: pathFloorObjs){
            if(fobj.getFloorName().equals(floor_name)){
                return fobj;
            }
        }
        return null;
    }





    private void createAndRunMainLogicThread(){

        //create popupChecker thread too here

        popup_checker_thread = new Runnable() {
            @Override
            public void run() {
                int currPr = -1; //current priority
                Pair<String,MessagePriority> popped = null;
                while(popupMessagesQueue.size()>0 && popupMessagesQueue.peek().second.getPriorityValue() >= currPr){
                    popped = popupMessagesQueue.poll();
                    currPr = popped.second.getPriorityValue();
                }

                if(popped!=null){
                    showMessage(popped.first);
                    handler.postDelayed(this,2000);
                }
                else if(popupMessagesQueue.size()>0){
                    showMessage(popupMessagesQueue.poll().first);
                    handler.postDelayed(this,2000);
                }
                else{
                    handler.postDelayed(this,100);
                }


            }
        };
        handler.post(popup_checker_thread);

        //this is the main code which does all the distance calculation and detection
        //it assumes we have the information of the current floor and the beacons on the floor
        //so start this thread only after we have this information prior
        mainLogicThreadRunning = true;
        mainLogicThread = () -> {
            handler.postDelayed(mainLogicThread,2000); //after every 2 secs it checks again
            currFloorObj = getFloorObjGivenName(currLocation.getFloor());
            if(mainLogicThreadRunning && currFloorObj!=null){
                queueLock.lock();
                try {
                    disableBLE();
                    if(beaconObj.getAllBeacons().length<1){
                        return;
                    }
                    //Calculate the weights of all the beacons of the building
                    ArrayList<Pair<String,Double>> beaconFusionSorted = new ArrayList<>();  // stores weights fused with pdr
                    ArrayList<Pair<String,Double>> beaconWeightsSorted = new ArrayList<>(); // stores weights only for beacons
                    for(String B: beaconObj.getAllBeacons()){
                        //iterate over all beacons and calculate their weights
                        Point beaconCoord = beaconObj.getBeaconCoord(B);
                        double deltaX = beaconCoord.x - currLocation.getGridX();
                        double deltaY = beaconCoord.y - currLocation.getGridY();
                        double pdrDist = Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2)) * gridLength;   //pdrDist is in metres
                        double C = beaconObj.weightOfBeacon(B);
                        double C2 = fusePDR_RSSI(pdrDist, C);
                        beaconFusionSorted.add(new Pair<>(B,C2));
                        beaconWeightsSorted.add(new Pair<>(B,C));
                    }

                    Collections.sort(beaconFusionSorted, (c1, c2) -> c1.second>c2.second ? -1 : c1.second.equals(c2.second) ? 0 : 1);
                    Collections.sort(beaconWeightsSorted, (c1, c2) -> c1.second>c2.second ? -1 : c1.second.equals(c2.second) ? 0 : 1);

                    debugView.setText("");
                    debugView.setTextColor(Color.RED);
                    Log.w("BW",beaconWeightsSorted.toString());
                    for(int i=0;i<beaconWeightsSorted.size();i++){
                        debugView.append(beaconWeightsSorted.get(i).first+" -- "+oneDecimalForm.format(beaconWeightsSorted.get(i).second)+"\n");
                    }

                    Log.d("BFS",beaconFusionSorted.toString());

                    //FIRST CHECK of flow chart
                    double ABW = beaconWeightsSorted.size()>0? beaconWeightsSorted.get(0).second : -1; //Actual top beacon weight
                    String floorOfBeacon = beaconWeightsSorted.size()>0 ? beaconObj.getFloorOfBeacon(beaconWeightsSorted.get(0).first) : "";

                    if(ABW >= MIN_BEACON_RECOGNITION_CONST){
                        if(floorOfBeacon.equals(currLocation.getFloor())){
                            if(ABW>=STRONG_BEACON_RECOGNITION_CONST){
                                // if path is searched:
                                //      If beacon is near path then drag the user to the path
                                //      otherwise drag the user to the beacon and re-search the path to the destination

                                if(pathsearched){
                                    Point beaconCoord = beaconObj.getBeaconCoord(beaconWeightsSorted.get(0).first);
                                    double[] nid = nearest_point_details(currFloorObj.getPath(),beaconCoord.x*currFloorObj.getGpx(),beaconCoord.y*currFloorObj.getGpy(),currFloorObj);
                                    if(nid[1]<=BEACON_NEAR_PATH_DIST){
                                        if (
                                                lastLocalized.getGridX() != (int) nid[2] ||
                                                        lastLocalized.getGridY() != (int) nid[3] ||
                                                        (System.currentTimeMillis() - lastLocalizedTime) >= NEW_LOCALIZATION_THRESHOLD_TIME
                                        ) {
                                            currDisplayIndexOfPath = (int)nid[0];
                                            currFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                                            currLocation.setGridX((int)nid[2]);
                                            currLocation.setGridY((int)nid[3]);
                                            lastLocalized.setGridX((int)nid[2]);
                                            lastLocalized.setGridY((int)nid[3]);
                                            lastLocalizedTime = System.currentTimeMillis();

                                            xf = currFloorObj.getPath().get(currDisplayIndexOfPath).x;
                                            yf = currFloorObj.getPath().get(currDisplayIndexOfPath).y;
                                            dah.updateCanvasLocation((int)xf,(int)yf);
                                            dah.updateCanvasPrecision(100, currFloorObj.getGpx(), currFloorObj.getGpy());
                                            stopAndOrientUser(currFloorObj);
                                        }
                                    }
                                    else{
                                        //remove activity_navigation arrow from the canvases first
                                        for(FloorObj fobj:pathFloorObjs){
                                            fobj.getCanvasView().disableArrow();
                                        }

                                        //localize to this beacon
                                        //everytime when detection is this strong
                                        currLocation.setGridX(beaconCoord.x);
                                        currLocation.setGridY(beaconCoord.y);
                                        lastLocalized.setGridX(beaconCoord.x);
                                        lastLocalized.setGridY(beaconCoord.y);
                                        lastLocalizedTime = System.currentTimeMillis();
                                        xf = beaconCoord.x*currFloorObj.getGpx();
                                        yf = beaconCoord.y*currFloorObj.getGpy();

                                        dah.updateCanvasLocation((int)xf, (int)yf);
                                        dah.updateCanvasPrecision(ABW, currFloorObj.getGpx(), currFloorObj.getGpy());
                                        dah.enableDrawOnCanvas(currFloorObj.getCanvasView(), (ZoomRotationFrameLayout)currFloorObj.getCanvasView().getParent());
                                        if(!pathsearched){
                                            jumpedOut = true;
                                            jumpingOutStarted = true;
                                        }

                                        //RE SEARCH THE PATH TO THE DESTINATION
                                        //ONLY IF the active path doesn't have same sourceLocation
                                        if(pathsearched && !(lastSource.getGridX()==beaconCoord.x && lastSource.getGridY()==beaconCoord.y)){
                                            //IF sourceLocation is this beacon means path is already searched
                                            //IF not then we need to search path again
//                                        speakTTS(Sentences.searching_location.getSentence(audio_language));
                                            addToMessageQueue(Sentences.searching_location.getSentence(audio_language));
                                            searchPath();
                                            pdrEnabled=true;
                                            return;

                                        }
                                    }
                                }




                            }
                            else{
                                //do nothing
                            }
                        }
                        else{
                            // USER IS NOW ON A DIFFERENT FLOOR
                            // SO LOCALIZE TO THIS BEACON and CHANGE CURRENT FLOOR

                            //remove activity_navigation arrow from the canvases first
                            for(FloorObj fobj:pathFloorObjs){
                                fobj.getCanvasView().disableArrow();
                            }

                            //localize once using this beacon
                            Point beaconCoord = beaconObj.getBeaconCoord(beaconWeightsSorted.get(0).first);
                            if (lastLocalized.getGridX() != beaconCoord.x || lastLocalized.getGridY() != beaconCoord.y) {
                                //localize to this beacon
                                //only once because detection is weak
                                currLocation.setGridX(beaconCoord.x);
                                currLocation.setGridY(beaconCoord.y);
                                currLocation.setFloor(floorOfBeacon);
                                lastLocalized.setGridX(beaconCoord.x);
                                lastLocalized.setGridY(beaconCoord.y);
                                lastLocalizedTime = System.currentTimeMillis();
                                jumpedOut = true;
                                jumpingOutStarted = true;

                                FloorObj beaconFloorObj = getFloorObjGivenName(floorOfBeacon);
                                if(beaconFloorObj!=null){
                                    // means user on a floor which lies in our searched path,
                                    // but is different to the floor user is currently
                                    // scroll to this floor and enable draw on this floor's canvas

//                                        speakTTS(Sentences.arrived_on_floor.getSentence(audio_language,floorOfBeacon));
                                    addToMessageQueue(Sentences.arrived_on_floor.getSentence(audio_language,floorOfBeacon));

                                    //check the distance of user from the searched path
                                    //if its high then re-search the path again
                                    //if its low then drag him to the closest point in the path
//                                    double[] nid = nearest_point_index_and_distance(beaconFloorObj.getPath(),beaconCoord.x*beaconFloorObj.getGpx(),beaconCoord.y*beaconFloorObj.getGpy(),beaconFloorObj);

                                    double[] nid = nearest_point_details(beaconFloorObj.getPath(),beaconCoord.x*beaconFloorObj.getGpx(),beaconCoord.y*beaconFloorObj.getGpy(),beaconFloorObj);

                                    if(nid[1]>BEACON_NEAR_PATH_DIST){
//                                            speakTTS(Sentences.searching_path.getSentence(audio_language));
                                        addToMessageQueue(Sentences.searching_path.getSentence(audio_language));
                                        searchPath();
                                        pdrEnabled=true;
                                        return;

                                    }
                                    else{
                                        currDisplayIndexOfPath = (int)nid[0];
                                        beaconFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                                        currLocation.setGridX((int)nid[2]);
                                        currLocation.setGridY((int)nid[3]);
                                        currLocation.setFloor(floorOfBeacon);
                                        lastLocalized.setGridX((int)nid[2]);
                                        lastLocalized.setGridY((int)nid[3]);
                                        lastLocalizedTime = System.currentTimeMillis();
                                        xf = beaconFloorObj.getPath().get(currDisplayIndexOfPath).x;
                                        yf = beaconFloorObj.getPath().get(currDisplayIndexOfPath).y;
                                        dah.updateCanvasLocation((int)xf,(int)yf);
                                        dah.updateCanvasPrecision(100, beaconFloorObj.getGpx(), beaconFloorObj.getGpy());
                                        pdrEnabled = true; //enable pdr again because it was disabled when user reached previous floor's destination
                                        stopAndOrientUser(beaconFloorObj);
                                    }

                                    try {
                                        floorLevelLayout.getChildAt(pathFloorObjs.size()-1-sliderViewCounter).setBackgroundColor(0);
                                    }catch (Exception ignore){}
                                    sliderViewCounter = pathFloorObjs.indexOf(beaconFloorObj);
                                    try {
                                        floorLevelLayout.getChildAt(pathFloorObjs.size()-1-sliderViewCounter).setBackgroundColor(Color.parseColor("#4D000000"));
                                    }catch (Exception ignore){}
                                    //accumulate width to scroll in variable
                                    int scrollWidth = 0;
                                    for(int i=0;i<sliderViewCounter;i++){
//                                        scrollWidth += sliderView.getChildAt(i).getWidth();
                                    }
//                                    hsv.smoothScrollTo(scrollWidth,hsv.getScrollY());

                                    xf = beaconCoord.x*beaconFloorObj.getGpx();
                                    yf = beaconCoord.y*beaconFloorObj.getGpy();
                                    dah.enableDrawOnCanvas(beaconFloorObj.getCanvasView(),(ZoomRotationFrameLayout)beaconFloorObj.getCanvasView().getParent());
                                    dah.updateCanvasLocation((int)xf, (int)yf);
                                    dah.updateCanvasPrecision(ABW, beaconFloorObj.getGpx(), beaconFloorObj.getGpy());
                                }
                                else{
                                    //this is some unknown floor, so re-search the path if path was searched
                                    if(pathsearched){
//                                            speakTTS(Sentences.arrived_on_unexpected_floor.getSentence(audio_language,floorOfBeacon));
                                        addToMessageQueue(Sentences.arrived_on_unexpected_floor.getSentence(audio_language,floorOfBeacon));
                                        searchPath();
                                        pdrEnabled=true;
                                        return;
                                    }
                                }
                            } else {
                                //already localized here, no point in re localizing
                            }

                        }
                    }


                    //SECOND CHECK of flow chart
                    double FBW = beaconFusionSorted.size()>0? beaconFusionSorted.get(0).second : -1; //fused with pdr top beacon weight
                    if(FBW>=MIN_FUSION_CONST){
                        if(pathsearched){
                            //****DISPLAY TO THE NEAREST POINT IN THE PATH, WHICH IS CLOSEST TO THE BEACON
                            Point beaconCoord = beaconObj.getBeaconCoord(beaconFusionSorted.get(0).first);
//                            double[] nid = nearest_point_index_and_distance(currFloorObj.getPath(),beaconCoord.x*currFloorObj.getGpx(),beaconCoord.y*currFloorObj.getGpy(),currFloorObj);
                            double[] nid = nearest_point_details(currFloorObj.getPath(),beaconCoord.x*currFloorObj.getGpx(),beaconCoord.y*currFloorObj.getGpy(),currFloorObj);


                            if(nid[1]<=BEACON_NEAR_PATH_DIST){
                                if (
                                        lastLocalized.getGridX() != beaconCoord.x ||
                                                lastLocalized.getGridY() != beaconCoord.y ||
                                                (System.currentTimeMillis() - lastLocalizedTime) >= NEW_LOCALIZATION_THRESHOLD_TIME
                                ) {
                                    currDisplayIndexOfPath = (int) nid[0];
                                    currFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                                    xf = currFloorObj.getPath().get(currDisplayIndexOfPath).x;
                                    yf = currFloorObj.getPath().get(currDisplayIndexOfPath).y;
                                    dah.updateCanvasLocation((int) xf, (int) yf);
                                    dah.updateCanvasPrecision(100, currFloorObj.getGpx(), currFloorObj.getGpy());
                                    lastLocalized.setGridX(beaconCoord.x);
                                    lastLocalized.setGridY(beaconCoord.y);
                                    lastLocalizedTime = System.currentTimeMillis();
                                }
                            }
                        }
                    }
                }
                finally {
                    beaconObj.clearCounts();
                    enableBLE();
                    queueLock.unlock();
                }
            }
        };
        handler.post(mainLogicThread);

        scanToggleThread = () -> {
            stopUFO();
            startUFO();
        };
        handler.postDelayed(scanToggleThread,5*60*1000);
    }
    private String getAnnotatedNodeGivenCoord(String floor, String coord){
        //s : {coord,nodename,macid}
        for(String[] s: floorToNodes.get(floor)){
            if(s[0].equals(coord)){
                return s[1];
            }
        }
        return null; //otherwise not found then return null
    }

    //if a node is under thresholdDistance(in feets) then return that node
    private String[] getNodeNearUser(String floor, int X, int Y, double thresholdDistance){
        //s : {coord,nodename,macid}
        int nodeX,nodeY;
        double dist;
        for(String[] s: floorToNodes.get(floor)){
            String[] coord = s[0].split(",");
            nodeX = Integer.parseInt(coord[0]);
            nodeY = Integer.parseInt(coord[1]);
            //calculate distance
            dist = Math.sqrt(Math.pow((X-nodeX),2)+Math.pow((Y-nodeY),2));
            if(dist<=thresholdDistance){
                return s;
            }
        }
        return null;
    }



    public  void searchLocationButton(View view) {
        modal.setVisibility(View.GONE);
        sourceDest.setVisibility(View.VISIBLE);
        recentMessageContainer.setVisibility(View.GONE);
        searchLocation();
    }

    public void searchLocation() {
        if (currentBuilding!=null) {
            loadingView.setVisibility(View.VISIBLE);
            beaconObj.clearCounts();

            if(ttsInitialized) speakTTS(Sentences.searching_location.getSentence(audio_language));
            //addToMessageQueue(Sentences.searching_location.getSentence(audio_language));
            showPopup(Sentences.searching_location.getSentence(audio_language));

            //stop any active beacon searchLocation thread
            //also pause the mainLogicThread
            stopSearchLocationThread();
            mainLogicThreadRunning = false;
            enableBLE(); //enable ble

            final int[] scan_counter = {1}; //In scan_counter scans of 2000ms duration if found then good else not found

            searchLocationThread = () -> {
                disableBLE();
                ArrayList<Pair<String, Double>> beaconWeightsSorted = new ArrayList<>();
//                String bMac = beaconObj.getStrongestBeacon(preciseRSSICutoff); //get beacon with signal as strong as precisRSSICutoff

                for (String B : beaconObj.getAllBeacons()) {
                    //iterate over beacons in current floor and print the fusion constant
                    double C = beaconObj.weightOfBeacon(B);
                    beaconWeightsSorted.add(new Pair<>(B, C));
                }
                Collections.sort(beaconWeightsSorted, (c1, c2) -> c1.second > c2.second ? -1 : c1.second.equals(c2.second) ? 0 : 1);

                Log.e("BW", beaconWeightsSorted.toString());
                debugView.setTextColor(Color.RED);
                debugView.setText("");
                for (int i = 0; i < beaconWeightsSorted.size(); i++) {
                    debugView.append(beaconWeightsSorted.get(i).first + " -- " + oneDecimalForm.format(beaconWeightsSorted.get(i).second) + "\n");
                }
//                double[] multiLaterateLocation = alwaysActiveScansObj.multiLaterate();
//                if (multiLaterateLocation != null) {
//                    Log.e("multiLateration", Arrays.toString(multiLaterateLocation));
//                } else {
//                    Log.e("multiLateration", "not enough useful beacons");
//                }


                if (beaconWeightsSorted.size() > 0 && beaconWeightsSorted.get(0).second > 3) {
                    String bMac = beaconWeightsSorted.get(0).first;
                    Double confidence = beaconWeightsSorted.get(0).second;
                    String floorOfBeacon = beaconObj.getFloorOfBeacon(bMac);
                    currLocation.setFloor(floorOfBeacon);
                    welcomeView.setVisibility(View.GONE); //remove the welcome View

                    reset();
                    //inflate the canvas and imgview
                    String imgUrl = getResources().getString(R.string.STATIC_URL) + floorToImageName.get(floorOfBeacon);
                    FloorObj thisFloorObj = new FloorObj();
                    thisFloorObj.setFloorName(floorOfBeacon);
                    thisFloorObj.setLengthAndBreadth(floorDim.get(floorOfBeacon));

                    double aspectRatio = thisFloorObj.getLength() * 1.0 / thisFloorObj.getBreadth();
                    int viewHeight;
                    int viewWidth;
                    if (thisFloorObj.getLength() > thisFloorObj.getBreadth()) {
                        //width is greater than height so fit to height and scale width
                        // if scaled_width is less than activity_width then
                        // scale whole image so that it fits activity_width
                        viewHeight = activity_height;
                        viewWidth = (int) (aspectRatio * viewHeight);
                        if (viewWidth < activity_width) {
                            double scaleToMult = activity_width * 1.0 / viewWidth;
                            viewWidth = activity_width;
                            viewHeight = (int) (scaleToMult * viewHeight);
                        }
                    } else {
                        //width is smaller than height so fit to width and scale height
                        // if scaled_height is less than activity_height then
                        // scale whole image so that it fits activity_height
                        viewWidth = activity_width;
                        viewHeight = (int) (viewWidth / aspectRatio);
                        if (viewHeight < activity_height) {
                            double scaleToMult = activity_height * 1.0 / viewHeight;
                            viewHeight = activity_height;
                            viewWidth = (int) (scaleToMult * viewWidth);
                        }
                    }

                    thisFloorObj.setGpx(viewWidth * 1.0 / floorDim.get(floorOfBeacon)[0]);
                    thisFloorObj.setGpy(viewHeight * 1.0 / floorDim.get(floorOfBeacon)[1]);
                    thisFloorObj.setViewWidth(viewWidth);
                    thisFloorObj.setViewHeight(viewHeight);

                    for (String s : floorToSimplePoly.get(floorOfBeacon)) {
                        thisFloorObj.addNWSimplePoly(s);
                    }
                    pathFloorObjs.add(thisFloorObj);

                    SliderItem sItem = new SliderItem(viewWidth, viewHeight, imgUrl, floorOfBeacon);
                    SliderViewInflator sv = new SliderViewInflator(NavigationBlind.this, null, sItem, thisFloorObj, activity_width, activity_height);
//                    sliderView.addView(sv);
                    searchSourceDialog.setHint("SOURCE IS CURRENT LOCATION");
                    openSearchSourceDialogVisible = false;
                    sourceViewToAnimate.setVisibility(View.GONE);
                    Point c = beaconObj.getBeaconCoord(bMac);
//                String annotatedName = getAnnotatedNodeGivenCoord(floorOfBeacon,c.x+","+c.y);
                    lastLocalized.setGridX(c.x);
                    lastLocalized.setGridY(c.y);
                    lastLocalizedTime = System.currentTimeMillis();
                    currLocation.setGridX(c.x);
                    currLocation.setGridY(c.y);
                    xf = c.x * thisFloorObj.getGpx();
                    yf = c.y * thisFloorObj.getGpy();
                    dah.updateCanvasLocation((int) xf, (int) yf);
                    dah.updateCanvasPrecision(confidence, thisFloorObj.getGpx(), thisFloorObj.getGpy());
                    dah.enableDrawOnCanvas(thisFloorObj.getCanvasView(), (ZoomRotationFrameLayout) thisFloorObj.getCanvasView().getParent());
                    pathsearched = false;
                    dah.updateFloorRotation(floorToAngle.get(thisFloorObj.getFloorName()));
                    String[] nearToUser = getNodeNearUser(currLocation.getFloor(), currLocation.getGridX(), currLocation.getGridY(), NEAR_LOCATION_DISTANCE);

                    if (nearToUser != null && !nearToUser[1].equals("undefined")) {
                        String msg = Sentences.location_is_at.getSentence(audio_language,nearToUser[1],currLocation.getFloor(), getNodeDirection(nearToUser[0], thisFloorObj));
                        searchSourceDialog.setText(nearToUser[1]);
                        locationName = nearToUser[1];
                        floorName = floorOfBeacon;
                        modalSourceEnable();
                        addToMessageQueue(msg);
                        showPopup(msg);
                    } else {
                        addToMessageQueue(Sentences.found_location.getSentence(audio_language, currLocation.getFloor()));
                        showPopup(Sentences.found_location.getSentence(audio_language, currLocation.getFloor()));
                    }
                    loadingView.setVisibility(View.GONE);
                    stopSearchLocationThread();
                    ZoomRotationFrameLayout parentZoomLayout = (ZoomRotationFrameLayout) thisFloorObj.getCanvasView().getParent();
                    Point[] boundingBoxCorners = {new Point((int) xf, (int) yf), new Point((int) xf, (int) yf), new Point((int) xf, (int) yf), new Point((int) xf, (int) yf)};
                    parentZoomLayout.updateBoundingBoxConstants(boundingBoxCorners, activity_width, activity_height);
                    parentZoomLayout.zoomToBoundingBox();
                    pdrEnabled = true;
                    return;
                }


                if (scan_counter[0] == 0) {
                    //called when location is not found
                    loadingView.setVisibility(View.GONE);
                    if(ttsInitialized) speakTTS(Sentences.unable_to_find_location.getSentence(audio_language));
                    addToMessageQueue(Sentences.unable_to_find_location.getSentence(audio_language));
                    showPopup(Sentences.unable_to_find_location.getSentence(audio_language));
                    stopSearchLocationThread();
                    return;
                }

                scan_counter[0]--;
                beaconObj.clearCounts();
                enableBLE();
                handler.postDelayed(searchLocationThread, 8000); //after every 8 secs it checks again
            };
            handler.post(searchLocationThread);
        } else {
            Toast.makeText(NavigationBlind.this,"Unable to Locate your location, Please Select your building",Toast.LENGTH_LONG).show();
        }
    }

    private void stopSearchLocationThread(){
        mainLogicThreadRunning = true;
        try { handler.removeCallbacks(searchLocationThread); }catch (Exception ignore){}
    }

    private double fuse2(double pdrDist, double rssiDist) {
        if(pdrDist<1){
            pdrDist = 1;
        }
        return FUSE_CONST*(pdrWeight*(1/pdrDist))+ FUSE_CONST*(rssiWeight*(1/rssiDist));
    }
    private double fusePDR_RSSI(double pdrDist, double beaconW){
        if(pdrDist<1){
            return beaconW;
        }
        return (1/pdrDist)*beaconW;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleSteep.updateAccel(event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }
    @Override
    public void step(long timeNs) {
        Log.d("STEP","STEP");
        onStepFunction();
    }
    private void onStepFunction() {
        FloorObj activeFloorObj = getFloorObjGivenName(currLocation.getFloor());

        if(!pdrEnabled || activeFloorObj==null){
            Log.d("step","inactive");
            return;
        }

        queueLock.lock();
        try {
            double xfNew;
            double yfNew;
            float yaw = dah.getOrientationYaw(); //yaw is pointing angle
            double distanceX = step_size * Math.sin(Math.toRadians(yaw)); //distanceX and distanceY are in feets
            double distanceY = step_size * Math.cos(Math.toRadians(yaw));


            //*******monitoring actual location code**********
            xfNew = xf + (distanceX)*(activeFloorObj.getGpx()); // (distanceX/gridLength) is number of grids moved in X direction
            yfNew = yf - (distanceY)*(activeFloorObj.getGpy());
            xfNew = (xfNew<=0) ? 1.0 : Math.min(activeFloorObj.getViewWidth()-1, xfNew); //so that xf doesn't go out of bounds
            yfNew = (yfNew<=0) ? 1.0 : Math.min(activeFloorObj.getViewHeight()-1, yfNew); //so that yf doesn't go out of bounds
            Point NEW = new Point();
            Point OLD = new Point((int)xf,(int)yf);
            NEW.x = (int)(xfNew);
            NEW.y = (int)(yfNew);
            Point validTP = activeFloorObj.getValidTransitionPoint(OLD,NEW);
            xf = validTP.x;
            yf = validTP.y;
            currLocation.setGridX((int)(validTP.x/activeFloorObj.getGpx()));
            currLocation.setGridY((int)(validTP.y/activeFloorObj.getGpy()));
            // xf = xfNew;
            // yf = yfNew;
            // currLocation.setX((int)(xf/gpx));
            // currLocation.setY((int)(yf/gpy));
            // liveCanvas.drawVirtualPoint((int)xf, (int)yf);
            //*******monitoring actual location code*********

            if(!pathsearched){
                dah.updateCanvasLocation((int)xf,(int)yf);
                dah.updateCanvasPrecision(100, activeFloorObj.getGpx(), activeFloorObj.getGpy());
                return;

            }

            //below code runs only if path is searched

//            double[] ind_and_dist = nearest_point_index_and_distance(activeFloorObj.getPath(), xf,yf,activeFloorObj);
            double[] ind_and_dist = nearest_point_details(activeFloorObj.getPath(), xf,yf,activeFloorObj);

            //this will be used to display on canvas on step
            int displayX=0;
            int displayY=0;

            if(jumpingOutStarted && jumpedOut){

                if(ind_and_dist[1]<=step_size){
                    //user is back to path so display the closest point on path
                    jumpingOutStarted = false;
                    jumpedOut = false;
                    currDisplayIndexOfPath = (int)ind_and_dist[0];
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    xf = activeFloorObj.getPath().get(currDisplayIndexOfPath).x;
                    yf = activeFloorObj.getPath().get(currDisplayIndexOfPath).y;
                    displayX = (int)xf;
                    displayY = (int)yf;
//                    if(ttsInitialized) speakTTS(Sentences.back_to_path.getSentence(audio_language));
                    addToMessageQueue(Sentences.back_to_path.getSentence(audio_language));
                    stopAndOrientUser(activeFloorObj);
                }
                else{
                    // keep displaying actual location
                    displayX = (int)xf;
                    displayY = (int)yf;
                }

            }
            else if (jumpingOutStarted && !jumpedOut){
                if(ind_and_dist[1]>4*step_size){
                    //user has jumped out
                    jumpedOut = true;
                    displayX = (int)xf;
                    displayY = (int)yf;
//                    if(ttsInitialized) speakTTS(Sentences.going_away_from_path.getSentence(audio_language));
                    addToMessageQueue(Sentences.going_away_from_path.getSentence(audio_language));
                    try { handler.removeCallbacks(orientation_helper_thread); }catch (Exception ignore){}
                }
                else if(ind_and_dist[1]<=step_size){
                    //bring back to path without informing the user
                    jumpingOutStarted = false;
                    jumpedOut = false;
                    currDisplayIndexOfPath = (int)ind_and_dist[0];
                    xf = activeFloorObj.getPath().get(currDisplayIndexOfPath).x;
                    yf = activeFloorObj.getPath().get(currDisplayIndexOfPath).y;
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    displayX = (int)xf;
                    displayY = (int)yf;
                }
                else{
                    //keep displaying on the path while maintaining actual position.
                    // Display the nearest point on path to our actual location, use fakeDisplayX and fakeDisplayY
                    currDisplayIndexOfPath = (int)ind_and_dist[0]; //nearest point on path
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    displayX = activeFloorObj.getPath().get(currDisplayIndexOfPath).x;
                    displayY = activeFloorObj.getPath().get(currDisplayIndexOfPath).y;
                }

            }
            else{
                //haven't started jumping out of the path so work normally
                Point xy = getNextJumpPoint(activeFloorObj.getPath());
                if (xy.x==-1){
                    jumpingOutStarted = true; //assume a skip of 1 step is adjustable
                    return;
                }
                else{
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    xf = xy.x;
                    yf = xy.y;
                    displayX = (int)xf;
                    displayY = (int)yf;
                }
            }

            dah.updateCanvasLocation(displayX,displayY);
            dah.updateCanvasPrecision(100, activeFloorObj.getGpx(), activeFloorObj.getGpy());
            if(displayX==activeFloorObj.getPath().get(activeFloorObj.getPath().size()-1).x
                    && displayY == activeFloorObj.getPath().get(activeFloorObj.getPath().size()-1).y){
                //user has reached this floor's destination so disable PDR and instruct him on what to do
                int activeFloorIndex = pathFloorObjs.indexOf(activeFloorObj);
                vibe.vibrate(200);
                if(activeFloorIndex==pathFloorObjs.size()-1){
                    //means this is the final destination
//                    speakTTS(Sentences.reached_destination.getSentence(audio_language,destLocation.name()));
                    addToMessageQueue(Sentences.reached_destination.getSentence(audio_language,destLocation.name()));
                }
                else{
                    //not final destination but is the destination in this floor
//                    speakTTS(Sentences.reached_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName()));
//                    speakTTS(Sentences.use_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName(), pathFloorObjs.get(activeFloorIndex+1).getFloorName()));
                    addToMessageQueue(Sentences.reached_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName()) + "\n"
                            +Sentences.use_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName(), pathFloorObjs.get(activeFloorIndex+1).getFloorName())
                    );
                }
                pdrEnabled = false;
                return;

            }
            currLocation.setGridX((int)(xf/activeFloorObj.getGpx()));
            currLocation.setGridY((int)(yf/activeFloorObj.getGpy()));

            if(turningPointsMap.get(activeFloorObj.getFloorName()).contains(new Pair<>(displayX,displayY))){
                stopAndOrientUser(activeFloorObj);
            }

            String[] nearToUser = getNodeNearUser(activeFloorObj.getFloorName(),currLocation.getGridX(), currLocation.getGridY(),NEAR_LOCATION_DISTANCE);

            if(nearToUser!=null && !nearToUser[1].equals("undefined")){
                String nearNodeElementType = nodeCoordToFloorElement.get(nearToUser[0]);
                MessagePriority PR;
                switch (nearNodeElementType){
                    case "Rooms" :
                        PR = L3;
                        break;
                    case "Doors" :
                        PR = L1;
                        break;
                    case "FloorConnection" :
                        PR = L1;
                        break;
                    case "RestRooms" :
                        PR = L2;
                        break;
                    case "Services" :
                        PR = L2;
                        break;
                    default :
                        PR = L3;
                        break;
                }
                if(lastSpokenNearToNode==null || !lastSpokenNearToNode.equals(nearToUser[1])){
                    lastSpokenNearToNode = nearToUser[1];
                    if(audio_language.equals("hi")){
                        addToMessageQueue("आपके "+getNodeDirection(nearToUser[0], activeFloorObj)+", "+nearToUser[1]+" है ", PR);
                    }
                    else{
                        addToMessageQueue(nearToUser[1]+" on your "+getNodeDirection(nearToUser[0], activeFloorObj), PR);
                    }

                }
            }

        }
        finally {
            queueLock.unlock();
        }
    }

    private String getNodeDirection(String nodeCoordinates, FloorObj floorObj) {
        //returns whether the node is on left or right or on front

        int xcurr = (int)xf;
        int ycurr = (int)yf;
        int xnext = (int)(Integer.parseInt(nodeCoordinates.split(",")[0])*floorObj.getGpx());
        int ynext = (int)(Integer.parseInt(nodeCoordinates.split(",")[1])*floorObj.getGpy());
        double currToNextAngle = Math.toDegrees(Math.atan2((ynext-ycurr),(xnext-xcurr)));
        currToNextAngle += 90;
        if(currToNextAngle<0){
            currToNextAngle+=360;
        }

        double headingAngle = dah.getOrientationYaw();
        int diffval = (int)(currToNextAngle-headingAngle);


        if(diffval>180) {
            diffval -= 360;
        }
        else if(diffval <= -180){
            diffval+=360;
        }

//        if(Math.abs(diffval)>70){
//            return Sentences.back.getSentence(audio_language);
//        }

        int clockAmount;
        if(diffval<0){
            clockAmount = (int)Math.round((360+diffval)/30.0);
            //left direction
            if(clockAmount==0 || clockAmount==12){
                return Sentences.front.getSentence(audio_language);
            }
            return Sentences.left.getSentence(audio_language);
        }
        else{
            //right direction
            clockAmount = (int)Math.round(diffval/30.0);
            if(clockAmount==0 || clockAmount==12){
                return Sentences.front.getSentence(audio_language);
            }
            return Sentences.right.getSentence(audio_language);
        }

    }

    //stops pdr and asks user to re-orient along the path
    private void stopAndOrientUser(FloorObj floorObj) {
        if(floorObj.getPath().size()<2){
            //can't orient in a path which is just a point
            return;
        }

        //remove any existing orientation_helper_threads
        try {
            handler.removeCallbacks(orientation_helper_thread);
        }catch (Exception ignore){}

        Integer[] TAA = getTurnAmountAndAngle(floorObj);

        vibe.vibrate(200);
//        speakTTS(Utils.angleToClocks(TAA[0],audio_language));
        int numSteps = getNumOfSteps(floorObj);
        if(numSteps<STEPS_THRESHOLD_TO_MERGE){
            addToMessageQueue(Utils.angleToClocks(TAA[0],audio_language) + (audio_language.equals("en")?", and ":", और ")+ Sentences.go_straight.getSentence(audio_language, String.valueOf(getNumOfSteps(floorObj))));

        }
        else{
            addToMessageQueue(Utils.angleToClocks(TAA[0],audio_language));

        }
        orientUserThread(TAA[1], getNumOfSteps(floorObj));

    }

    private int getNumOfSteps(FloorObj floorObj) {
        //gets number of steps to the next turning point
        //from the current location in the path
        int numStepsToGo = 1;
        int curr = currDisplayIndexOfPath+1;
        ArrayList<Point> path = floorObj.getPath();
        while(curr<path.size()){
            if(turningPointsMap.get(floorObj.getFloorName()).contains(new Pair<>(path.get(curr).x,path.get(curr).y))){
                break;
            }
            curr++;
            numStepsToGo++;
        }
        return numStepsToGo;
    }

    //whenever user rotates to the desired angle it vibrates and enables pdr again
    private void orientUserThread(final Integer angleToAchieve, final Integer numSteps) {
        pdrEnabled = false;
        //disable pdr for 1 second in turns, enable after 1 second
        handler.postDelayed(() -> pdrEnabled = true,1000);
        disableBLE();
        final double[] current_angle = new double[1];
        orientation_helper_thread = new Runnable() {
            @Override
            public void run() {
                current_angle[0] = dah.getOrientationYaw();
                if(Math.abs(angleToAchieve-current_angle[0])<20){
                    vibe.vibrate(500);
//                    speakTTS(Sentences.go_straight.getSentence(audio_language, String.valueOf(numSteps)));
                    if(numSteps >= STEPS_THRESHOLD_TO_MERGE){
                        //addToMessageQueue(Sentences.go_straight.getSentence(audio_language, String.valueOf(numSteps)));
                    }
                    enableBLE();
                }
                else{
                    handler.postDelayed(this,100);
                }
            }
        };
        handler.post(orientation_helper_thread);
    }


    private void enableBLE(){
        ble_scanning = true;
    }

    private void disableBLE(){
        ble_scanning = false;
    }


    private void startUFO(){
        if(mBluetoothLeScanner!=null){
            Log.d("Navigation", "Service: Starting Scanning");
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
        }

//        ufo.startScan(ufoDevice -> {
//            if(ble_scanning){
//                String macId = ufoDevice.getBtdevice().getAddress();
////                    Log.d("addRSSI",ufoDevice.getRssi()+"");
//                beaconObj.addRSSI(macId,ufoDevice.getRssi());
//                alwaysActiveScansObj.addRSSI(macId,ufoDevice.getRssi());
//            }
//
//        }, (i, s) -> Log.e("UFO_startScan","FAIL"));
    }

    public void toggleSearchSourceDialog(View view) {
        if(!openSearchSourceDialogVisible){
            if(openSearchDestinationDialogVisible) {
                openSearchDestinationDialogVisible = false;
                destinationViewToAnimate.setVisibility(View.GONE);
            } else  {
                openSearchSourceDialogVisible = true;
                sourceViewToAnimate.setVisibility(View.VISIBLE);
            }
        }
        else{
            openSearchSourceDialogVisible = false;
            sourceViewToAnimate.setVisibility(View.GONE);
        }
    }

    public void toggleSearchDestinationDialog(View view) {
        if(!openSearchDestinationDialogVisible){
            if(openSearchSourceDialogVisible) {
                openSearchSourceDialogVisible = false;
                sourceViewToAnimate.setVisibility(View.GONE);
            } else {
                openSearchDestinationDialogVisible = true;
                destinationViewToAnimate.setVisibility(View.VISIBLE);
            }
        }
        else{
            openSearchDestinationDialogVisible = false;
            destinationViewToAnimate.setVisibility(View.GONE);
        }
    }

    public void goBack(View view) {
        onBackPressed();
    }

    public void selectSourceVenue(View view) {
        if(VenueListLayoutVisible == true) {
            categoryView.setVisibility(View.VISIBLE);
            venueListLayout.setVisibility(View.GONE);
            buildingListLayout.setVisibility(View.GONE);
            landmarkListLayout.setVisibility(View.GONE);
            VenueListLayoutVisible = false;
        } else {
            categoryView.setVisibility(View.GONE);
            venueListLayout.setVisibility(View.VISIBLE);
            sourceBuildingEditText.setVisibility(View.GONE);
            buildingListLayout.setVisibility(View.GONE);
            landmarkListLayout.setVisibility(View.GONE);
            VenueListLayoutVisible = true;
        }
    }

    public void selectDestVenue(View view) {
        if(DestVenueListLayoutVisible == true) {
            destCategoryView.setVisibility(View.VISIBLE);
            destVenueListLayout.setVisibility(View.GONE);
            destBuildingListLayout.setVisibility(View.GONE);
            destLandmarkListLayout.setVisibility(View.GONE);
            DestVenueListLayoutVisible = false;
        } else {
            destCategoryView.setVisibility(View.GONE);
            destVenueListLayout.setVisibility(View.VISIBLE);
            destinationBuildingEditText.setVisibility(View.GONE);
            destBuildingListLayout.setVisibility(View.GONE);
            destLandmarkListLayout.setVisibility(View.GONE);
            DestVenueListLayoutVisible = true;
        }
    }

    public void selectWithInVenue(View view) {
        if (BuildingListLayoutVisible == true) {
            categoryView.setVisibility(View.GONE);
            buildingListLayout.setVisibility(View.VISIBLE);
            BuildingListLayoutVisible = false;
        } else {
            categoryView.setVisibility(View.GONE);
            buildingListLayout.setVisibility(View.VISIBLE);
            BuildingListLayoutVisible = true;
        }
    }

    public void selectWithInDestVenue(View view) {
        if (DestBuildingListLayoutVisible == true) {
            destCategoryView.setVisibility(View.GONE);
            destBuildingListLayout.setVisibility(View.VISIBLE);
            DestBuildingListLayoutVisible = false;
        } else {
            destCategoryView.setVisibility(View.GONE);
            destBuildingListLayout.setVisibility(View.VISIBLE);
            DestBuildingListLayoutVisible = true;
        }
    }

    private class SampleScanCallback extends ScanCallback {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {
                processScanResult(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            processScanResult(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    private void processScanResult(ScanResult result) {

        if(ble_scanning){
            String macId = result.getDevice().getAddress();
            beaconObj.addRSSI(macId,result.getRssi());
//            Log.w("receivedScan","MACID: "+macId+", RSSI: "+result.getRssi());
            alwaysActiveScansObj.addRSSI(macId,result.getRssi());
        }
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder().build();
        scanFilters.add(filter);
        return scanFilters;
    }
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }


    private void stopUFO(){
        try {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }catch (Exception ignore){}
//        ufo.stopScan(onSuccessBoolean -> Log.w("UFO_stopScan","STOPPED"), (i, s) -> Log.e("UFO_stopScan","UNABLE TO STOP")
//        );
    }


    private void hideKeyBoard(){
        //Hide:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }catch (Exception ignore){};
        }
        if(imm.isAcceptingText()){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }


    }
    private void hideKeyboard(Activity activity) {
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showKeyBoard(){
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }


    private void retryPopupVenueList(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Retry", (dialog, id) -> {
            // User clicked OK button
            getVenueList(); //resend the venue request
        });
        builder.setNegativeButton("Go Back", (dialog,id)-> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void retryPopup(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Retry", (dialog, id) -> {
            // User clicked OK button
            makeBuildingDataRequest(); //resend the building request
        });
        builder.setNegativeButton("Go Back", (dialog,id)-> finish());


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goingAwayFromPathPopup(){
        pdrEnabled = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        if(audio_language.equals("en")){
            builder.setMessage("You are going away from the Path.\nDo you want to Re-route?");
            builder.setPositiveButton("Yes", (dialog, id) -> {
                searchPath();
                pdrEnabled = true;
            });
            builder.setNegativeButton("No", (dialog,id)-> {pdrEnabled = true;});
        }
        else{
            builder.setMessage("आप रास्ते से दूर जा रहे है | क्या हम आपके लिए फिरसे रास्ता ढूंढे?");
            builder.setPositiveButton("हाँ", (dialog, id) -> {
                // User clicked OK button
                searchPath(); //resend the building request
                pdrEnabled = true;
            });
            builder.setNegativeButton("ना", (dialog,id)-> {pdrEnabled = true;});
        }



        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    private Point getNextJumpPoint(ArrayList<Point> path){
        //checks if next jump point is negative direction or positive direction along the path using magnetometer
        //updates the currDisplayIndexOfPath
        if(currDisplayIndexOfPath==path.size()-1){ //when its the last point
            currDisplayIndexOfPath=Math.max(0,path.size()-2);
            return path.get(currDisplayIndexOfPath);
        }
        else if (currDisplayIndexOfPath==0){ //when its the starting point
            currDisplayIndexOfPath= Math.min(1,path.size()-1);
            return path.get(currDisplayIndexOfPath);
        }
        else{
            int x1 = path.get(currDisplayIndexOfPath).x;
            int y1 = path.get(currDisplayIndexOfPath).y;
            int x2 = path.get(currDisplayIndexOfPath+1).x;
            int y2 = path.get(currDisplayIndexOfPath+1).y;

            double currToNextAngle = Math.toDegrees(Math.atan2((y2-y1),(x2-x1)));
            currToNextAngle += 90;
            if(currToNextAngle<0){
                currToNextAngle+=360;
            }


            double absDiff = angleDiff(currToNextAngle,dah.getOrientationYaw());
            if(absDiff<30){
                currDisplayIndexOfPath++; //move to next node in path
            }
            else if(absDiff>160){
                currDisplayIndexOfPath--;
            }
            else{
                //angle has became greater so drag him out of the path
                return new Point(-1,-1);
            }
            return path.get(currDisplayIndexOfPath);
        }

    }
    public static double angleDiff(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;       // This is either the distance or 360 - distance
        return phi > 180 ? 360 - phi : phi;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static void speakTTS(String toSpeak){
        int speechStatus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speechStatus = textToSpeech.speak(toSpeak,TextToSpeech.QUEUE_ADD,null,null);
        }
        else{
            speechStatus = textToSpeech.speak(toSpeak,TextToSpeech.QUEUE_ADD,null);
        }

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    @Override
    protected void onPause() {
        lastStateOfPDR = pdrEnabled;
        super.onPause();
    }
    @Override
    protected void onResume() {
        dah.start();

        if(shouldExecuteOnResume){
            //pdrEnabled=true; If you enable pdr then make sure all dependent variables otherwise app will crash
            if(mainLogicThreadRunning) handler.post(mainLogicThread);
            if(lastStateOfPDR){
                pdrEnabled = true;
            }
            startUFO();

        } else{
            shouldExecuteOnResume = true;
        }
        super.onResume();
    }

    @Override
    public void onDestroy(){
        stopUFO();
        dah.stop();
        if(ttsInitialized) textToSpeech.stop();
        try { MyRequestQueue.cancelAll(this); }
        catch (Exception ignore){}
        //remove all runnables if present
        try{handler.removeCallbacks(searchLocationThread);} catch(Exception ignore) {}
        try{handler.removeCallbacks(mainLogicThread);} catch(Exception ignore) {}
        try{handler.removeCallbacks(scanToggleThread);}catch (Exception ignore){}
        try { handler.removeCallbacks(orientation_helper_thread); }catch (Exception ignore){}
        pdrEnabled=false;
        try {
            sm.unregisterListener(this, accel);
        }catch (Exception ignore){}
        super.onDestroy();
//        speechRecognizer.destroy();
    }

    public void customStep(View view) {
        onStepFunction();
    }


    //returns the nearest point on searched path of given floor obj from a given location (fromX, fromY) <- in pixels
//    private double[] nearest_point_index_and_distance(ArrayList<Point> currPath, double fromX, double fromY, FloorObj floorObj) {
//        double[] result = new double[]{0,Double.POSITIVE_INFINITY};//index 0=index of nearest point in currPath arraylist, index 1=distance of point

    private double[] nearest_point_details(ArrayList<Point> currPath, double fromX, double fromY, FloorObj floorObj) {
        double[] result = new double[]{0,Double.POSITIVE_INFINITY, 0, 0};//index 0=index of nearest point in currPath arraylist, index 1=distance of point, index2= gridX, index3=gridY
        for(int i=0;i<currPath.size();i++){
            Point p = currPath.get(i);
            double dist = Math.sqrt(Math.pow((fromX-p.x)/floorObj.getGpx(),2)+Math.pow((fromY-p.y)/floorObj.getGpy(),2));
            if(dist<result[1]){
                result[1] = dist;
                result[0] = i;
                result[2] = p.x;
                result[3] = p.y;
            }
        }
        return result;
    }

    //takes path from floorObj and gets the turn string and angle amount for next point in that path
    //if 1st index is +ve then its right turn else its left turn
    private Integer[] getTurnAmountAndAngle(FloorObj fobj) {

        int xcurr = fobj.getPath().get(currDisplayIndexOfPath).x;
        int ycurr = fobj.getPath().get(currDisplayIndexOfPath).y;
        int xnext = fobj.getPath().get(currDisplayIndexOfPath+1).x;
        int ynext = fobj.getPath().get(currDisplayIndexOfPath+1).y;
        double currToNextAngle = Math.toDegrees(Math.atan2((ynext-ycurr),(xnext-xcurr)));
        currToNextAngle += 90;
        if(currToNextAngle<0){
            currToNextAngle+=360;
        }

        double headingAngle = dah.getOrientationYaw();
        int diffval = (int)(currToNextAngle-headingAngle);
        if(diffval>180) {
            diffval -= 360;
        }
        else if(diffval <= -180){
            diffval+=360;
        }

        return new Integer[]{diffval,(int)currToNextAngle};
    }

    private void reset(){
        pathFloorObjs.clear();
        floorLevelLayout.removeAllViews();
        floorLevelLayoutInfo.removeAllViews();
        sliderViewCounter = 0;
        maxSliderViewCounter=1;
    }

    private void addToMessageQueue(String text, MessagePriority... PRarr) {
        if (MessageText.equals(text)) {
            //do nothing
        } else {
            MessageText = text;
            MessagePriority pr = L0; //default priority is highest
            if (PRarr.length > 0) {
                pr = PRarr[0];
            }
            //push the text to popup Queue
            popupMessagesQueue.add(new Pair<>(text, pr));
        }
    }
    public void showMessage(String text){
        Log.e("text",text);
        if(ttsInitialized) speakTTS(text);
        recentMessage.setText(text);
        if(text.contains("Right") || text.contains("right")) {
            imageMsg.setImageResource(R.drawable.turn_right);
        } else if(text.contains("Left") || text.contains("left")) {
            imageMsg.setImageResource(R.drawable.turn_left);
        } else if(text.contains("destination") && text.contains("Reached")) {
            imageMsg.setImageResource(R.drawable.route);
            sourceDest.setVisibility(View.VISIBLE);
        } else {
            imageMsg.setImageResource(R.drawable.straight);
        }
        recentMessageContainer.setVisibility(View.VISIBLE);
        if(text.equals(Sentences.going_away_from_path.getSentence(audio_language))){
            goingAwayFromPathPopup();
        }
    }

    public void showPopup(String text) {

        if(activePopup!=null){
            //hide existing popups
            activePopup.dismiss();
        }
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);
        ((TextView)popupView.findViewById(R.id.popupText)).setText(text);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        activePopup = new PopupWindow(popupView, width, height, focusable);
        activePopup.setOutsideTouchable(false);
        activePopup.setFocusable(false);
        activePopup.setAnimationStyle(R.style.Animation);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        activePopup.showAtLocation(mainContainer, Gravity.CENTER, 0, 0);

        //dismiss the popup after some time automatically
        handler.postDelayed(() -> activePopup.dismiss(), 2000);
        // dismiss the popup window when touched
//        popupView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                popupWindow.dismiss();
//                return true;
//            }
//        });
    }

    private void errorPopup(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void share(View view) {
        String floor = currLocation.getFloor();
        if(floor==null || floor.equals("")){
            Toast.makeText(NavigationBlind.this,"unable to share your location",Toast.LENGTH_SHORT).show();
            return;
        }
        FloorObj activeFloorObj = getFloorObjGivenName(currLocation.getFloor());
        if(activeFloorObj==null){
            Toast.makeText(NavigationBlind.this,"unable to share your location",Toast.LENGTH_SHORT).show();
            return;
        }
        String[] nearToUser = getNodeNearUser(activeFloorObj.getFloorName(),currLocation.getGridX(), currLocation.getGridY(),NEAR_LOCATION_DISTANCE);
        String shareBodyText;
        if(nearToUser!=null && !nearToUser[1].equals("undefined")){
            shareBodyText = "Hi!\nI am near "+nearToUser[1]+"\nin\n"+floor.toUpperCase()+" FLOOR \nof\n"+buildingName;
        }
        else{
            shareBodyText = "Hi!\nI am "+"\nin\n"+floor.toUpperCase()+" FLOOR \nof\n"+buildingName;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareSubText = "share your location";
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
        startActivity(Intent.createChooser(shareIntent, "Share With"));


    }

    public void filter(View view) {
        Toast.makeText(NavigationBlind.this,"feature not implemented yet",Toast.LENGTH_SHORT).show();

    }

    public void parking(View view) {
        Toast.makeText(NavigationBlind.this,"feature not implemented yet",Toast.LENGTH_SHORT).show();
    }


    public void profileSetting(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void info(View view) {
        Intent i = new Intent(NavigationBlind.this, WebviewActivity.class);
        i.putExtra("URL",getApplicationContext().getResources().getString(R.string.project_page));
        startActivity(i);
    }

    public void feedback(View view) {
        Intent i = new Intent(NavigationBlind.this, WebviewActivity.class);
        i.putExtra("URL",getApplicationContext().getResources().getString(R.string.feedback_page));
        startActivity(i);
    }
}
