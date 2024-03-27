package com.inclunav.iwayplus.activities;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;
import static com.inclunav.iwayplus.MessagePriority.L0;
import static com.inclunav.iwayplus.MessagePriority.L1;
import static com.inclunav.iwayplus.MessagePriority.L3;
import static com.inclunav.iwayplus.activities.BuildingAdapter.buildingNamenew;
import static com.inclunav.iwayplus.activities.BuildingAdapter.initialBuildingName;
import static com.inclunav.iwayplus.activities.BuildingAdapter.initialVenueName;
import static com.inclunav.iwayplus.activities.BuildingAdapter.venueNamenew;

import java.util.Arrays;
import java.util.Locale;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.inclunav.iwayplus.CacheHelper;
import com.inclunav.iwayplus.MessagePriority;
import com.inclunav.iwayplus.R;
import com.inclunav.iwayplus.Utils;
import com.inclunav.iwayplus.activities.roomdb.AppDatabase;
import com.inclunav.iwayplus.activities.roomdb.BuildingDataEntity;
import com.inclunav.iwayplus.beacon_related.AlwaysActiveScans;
import com.inclunav.iwayplus.beacon_related.BeaconDetails;
import com.inclunav.iwayplus.custom_step_detector.SteepDetector;
import com.inclunav.iwayplus.custom_step_detector.StepListener;
import com.inclunav.iwayplus.enums.ElementTypes;
import com.inclunav.iwayplus.enums.GeometryTypes;
import com.inclunav.iwayplus.layout_utilities.CanvasView;
import com.inclunav.iwayplus.layout_utilities.InstantAutoComplete;
import com.inclunav.iwayplus.layout_utilities.SliderItem;
import com.inclunav.iwayplus.layout_utilities.SliderViewInflator;
import com.inclunav.iwayplus.layout_utilities.ZoomRotationFrameLayout;
import com.inclunav.iwayplus.path_search.ConnectorGraph;
import com.inclunav.iwayplus.path_search.PathOption;
import com.inclunav.iwayplus.pdr.DeviceAttitudeHandler;
import com.inclunav.iwayplus.pdr.FloorObj;
import com.inclunav.iwayplus.pdr.Node;
import com.inclunav.iwayplus.pdr.Sentences;
import com.squareup.picasso.Picasso;
import com.ufobeaconsdk.main.UFOBeaconManager;

import org.apache.commons.math3.util.Precision;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Navigation extends AppCompatActivity implements SensorEventListener, StepListener, OnMapReadyCallback {

    double prevangle =0.0;

    AppDatabase appDatabase;
    String source = null;
    String dest = null;

    String destdialogtext = null;

    String sfl = null;

    String dfl = null;

    private TextToSpeech tts;
    private static final double BEACON_NEAR_PATH_DIST = 3; //threshold of when to bring to nearest point in path to the beacon
    private static final double MIN_BEACON_RECOGNITION_CONST = 50;
    private static double STRONG_BEACON_RECOGNITION_CONST = 600; //600 orignially
    private static final double MIN_FUSION_CONST = 15;
    private static final double NEAR_LOCATION_DISTANCE = 4; //distance in feets of finding nearest node
    private static final long NEW_LOCALIZATION_THRESHOLD_TIME = 60000; //till 60 seconds it will not relocalize to the same point on the path
    private static final int STEPS_THRESHOLD_TO_MERGE = 8;
    public static Map<String, int[]> floorDim = new HashMap<>(); //floorName to [length,width] map
    private Map<String, ArrayList<String[]>> floorToNodes = new HashMap<>(); //stores floorName to NodesList where each node = (coordinates, nodeName, MACID)
    private Map<String, ArrayList<String[]>> floorToNodesbeacons = new HashMap<>(); //stores floorName to NodesList where each node = (coordinates, nodeName, MACID)
    private Map<String, String> nodeCoordToFloorElement = new HashMap<>();
    private Map<String, Float> floorToAngle = new HashMap<>();
    private LinearLayout viewToAnimate;

    private static TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private static final int WINDOWSIZE = 3; //window size for running average RSSI readings for a beacon
    private boolean pdrEnabled = false;// disable the pdr initially
    private boolean lastStateOfPDR = false;
    private boolean shouldExecuteOnResume;
    private RequestQueue MyRequestQueue;
    private float floorRotation = 250.0f; // angle of floor's displayed image on compass
    private String buildingName;
    private String venueName;
    private String base_url;
    private Vibrator vibe;

    private long lastLocalizedTime = 0;
    //currLocation stores the location in the form of grids in x and grids in y, where a grid=1 feet
    private Node lastLocalized = new Node(-1, -1, "", ""); //assume initial location to be -1,-1
    private Node destLocation = new Node(-1, -1, "", "");
    private Node lastDestLocation = new Node(-1, -1, "", "");
    private Node currLocation = new Node(-1, -1, "", "");
    private Node tempLocation = new Node(-1, -1, "", "");
    private Node lastSource = new Node(-1, -1, "", ""); //when path is searched lastSource is set to source of that path
    private InstantAutoComplete sourceEditText;
    private InstantAutoComplete destEditText;
    private ArrayAdapter sourceAdapter;
    private ArrayAdapter destAdapter;
    public static ArrayList<String> sourceFloorsList = new ArrayList<>();//associated with spinner
    private ArrayList<String> sourcePointsList = new ArrayList<>();
    private ArrayList<String> destFloorsList = new ArrayList<>();//associated with spinner
    private ArrayList<String> destPointsList = new ArrayList<>();

    private ArrayList<String> sourceLocationData = new ArrayList<>();
    private Map<String, String> sourceAPIData = new HashMap<>();


    private UFOBeaconManager ufo;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private DecimalFormat oneDecimalForm = new DecimalFormat("#.#");

    private double step_size;
    private String audio_language;
    private Boolean isUserHandicap;
    private int currDisplayIndexOfPath = 0; //when displaying we display current location as this index point from the path list
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

    private double pdrWeight = 0.6;
    private double rssiWeight = 0.4;
    private double FUSE_CONST = 10;
    public static Map<String, Map<String, Point>> floor_to_floorConn_map = new HashMap<>(); //floor name to floor connection nodes map
    public static Map<String, ArrayList<String>> floorToNonWalkables = new HashMap<>();
    public Map<String, ArrayList<String>> floorToSimplePoly = new HashMap<>(); //floor name to non walkable polygon map, used by pathSearcher for valid transition
    public Map<String, String> floorToImageName = new HashMap<>();

    private Map<String, String> floorToflr_dist_matrix = new HashMap<>();
    private Map<String, String> floorTofrConn = new HashMap<>();
    private InputMethodManager imm; //for hiding or showing keyboard
    private int activity_width; //activity's width
    private int activity_height;

    private Map<String, String[]> nodeNameToDetails = new HashMap<>(); //stores floor and coordinate of node
    private LinearLayout sliderView;
    private HorizontalScrollView hsv;
    private TextView welcomeView, topHeaderText;
    private FrameLayout loadingView;
    private boolean ble_scanning = false;
    private Map<String, Set<Pair>> turningPointsMap = new HashMap<>();
    private boolean jumpingOutStarted = false, jumpedOut = false, pathsearched = false;

    private Lock queueLock;
    private int sliderViewCounter = 0, maxSliderViewCounter = 1;

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
    TextView recentMessage;
    LinearLayout floorLevelLayout;
    LinearLayout floorLevelLayoutInfo;
    LinearLayout floorLevelLayoutContainer;
    Boolean floorLevelLayoutVisible = true;
    LinearLayout recentMessageContainer;
    TextView debugView, debugView2;
    ImageView imageMsg;

    private LinearLayout venueBuildingView, sourceViewToAnimate, destinationViewToAnimate;

    TextView searchSourceDialog;

    TextView searchDestinationDialog;
    private boolean openVenueBuildingDaiologVisible = false, openSearchSourceDialogVisible = false, openSearchDestinationDialogVisible = false;

    private InstantAutoComplete venueEditText;
    private ArrayAdapter venueListAdapter;

    private InstantAutoComplete buildingEditText;
    private ArrayAdapter buildingListAdapter;

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
    String locationName, destinationName, floorName;
    LinearLayout modal, modalSource, modalRoute, information_modal_dropdown;
    FrameLayout back;
    TextView modalDestinationName, modalDestinationVenueName, modalDestinationDateTime, modalDestinationDistance, modalSourceName, modalSourceVenueDateTime, modalSourceVenueName, viaFloorConnection, distance_in_meter,distance_in_meter2, distance_in_time;
    Button modalDirection;
    String MessageText = "";
    ImageView infoModal;
    TextView sourceRouteInfo, destRouteInfo, routeTime, routeText;
    boolean information_modal_dropdown_count = false;
    ImageButton reLocalize, volumeButton, navigationButton;
    ImageView EraseVenueTextBox, EraseBuildingTextBox;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private ImageView micButtonVenue, micButtonBuilding, micButtonSource, micButtonDestination;
    String micCount = "0";

    LinearLayout save_location_modal, saveOption, custom_save_option, selectSourceModel, selectDestinationModel;
    ImageView swap;
    Button save_location_type_home, save_location_type_custom, save_location_type_work;
    TextView save_location_text, saved_as, save_success;
    EditText custom_save_option_text;
    Button save_location, cancel_save_location;
    public String saveLocationType;

    private GoogleMap mMap;

    private void initViews() {

        volumeButton.setImageResource(R.drawable.ic_baseline_volume_off_24);
        ttsInitialized = false;
        venueListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, venueList);
        venueEditText.setAdapter(venueListAdapter);
        venueEditText.setThreshold(1);
        buildingListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, buildingList);
        buildingEditText.setAdapter(buildingListAdapter);
        buildingEditText.setThreshold(1);

        EraseBuildingTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildingEditText.setText("");
            }
        });

        EraseVenueTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                venueEditText.setText("");
            }
        });

        volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ttsInitialized == true) {
                    volumeButton.setImageResource(R.drawable.ic_baseline_volume_off_24);
                    ttsInitialized = false;
                    Toast.makeText(Navigation.this, "TTS disabled", Toast.LENGTH_SHORT).show();
                } else if (ttsInitialized == false) {
                    volumeButton.setImageResource(R.drawable.ic_baseline_volume_up_24);
                    ttsInitialized = true;
                    Toast.makeText(Navigation.this, "TTS initialized", Toast.LENGTH_SHORT).show();
                }
            }
        });
        save_location_type_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocationType = "home";
                Log.e("home", saveLocationType);
            }
        });

        save_location_type_work.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocationType = "work";
                Log.e("work", saveLocationType);
            }
        });
        save_location_type_custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocationType = "other";
                saveOption.setVisibility(View.GONE);
                custom_save_option.setVisibility(View.VISIBLE);
                Log.e("other", saveLocationType);
            }
        });

        save_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sourceName = currLocation.name();
                try {
                    if (sourceAPIData.containsKey(sourceName)) {
                        JSONObject jsonObject = new JSONObject(sourceAPIData.get(sourceName));
                        Log.e("jsonObject", sourceAPIData.get(sourceName));
                        if (saveLocationType == "home" && saveLocationType == "work") {
                            saveLocationAPI(jsonObject);
                        } else if (saveLocationType == "other") {
                            String text = custom_save_option_text.getText().toString();
                            if (text == "") {
                                Toast.makeText(Navigation.this, "Please Enter Custom Address", Toast.LENGTH_LONG).show();
                            } else {
                                saveLocationType = "hh";
                                saveLocationAPI(jsonObject);
                            }
                        } else {
                            Toast.makeText(Navigation.this, "Unable to save the location, Please Select type", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("when", " ");

                    }
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage());

                }
            }
        });

        cancel_save_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_location_modal.setVisibility(View.GONE);
                saveOption.setVisibility(View.VISIBLE);
                saved_as.setVisibility(View.VISIBLE);
                save_success.setVisibility(View.GONE);
                custom_save_option.setVisibility(View.GONE);
            }
        });

        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapper();
            }
        });


        modalDirection.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                Toast.makeText(Navigation.this, "clicked", Toast.LENGTH_SHORT).show();
                STRONG_BEACON_RECOGNITION_CONST = 600;
                hsv.setFillViewport(true);
                selectSourceModel.setVisibility(View.GONE);
                selectDestinationModel.setVisibility(View.GONE);
                reLocalize.setVisibility(View.VISIBLE);
                volumeButton.setVisibility(View.VISIBLE);
                navigationButton.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);
                modal.setVisibility(View.GONE);
                modalRoute.setVisibility(View.VISIBLE);
                information_modal_dropdown.setVisibility(View.GONE);
                searchPath();
                pdrEnabled = true;
                enableBLE();
                createAndRunMainLogicThread();
            }
        });
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("onReadyForSpeech", " : ");

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.e("onBeginningOfSpeech", " : ");
                if (micCount.equals("1")) {
                    venueEditText.setText("");
                    venueEditText.setHint("Listening...");
                } else if (micCount.equals("2")) {
                    buildingEditText.setText("");
                    buildingEditText.setHint("Listening...");
                } else if (micCount.equals("3")) {
                    sourceEditText.setText("");
                    sourceEditText.setHint("Listening...");
                } else if (micCount.equals("4")) {
                    destEditText.setText("");
                    destEditText.setHint("Listening...");
                } else if (micCount.equals("0")) {
                    Log.e("at 0", " :  dasso");
                }
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.e("onResults", " : ");
                if (micCount.equals("1")) {
                    micButtonVenue.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    venueEditText.setText(data.get(0));
                } else if (micCount.equals("2")) {
                    micButtonBuilding.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    buildingEditText.setText(data.get(0));
                } else if (micCount.equals("3")) {
                    micButtonSource.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    sourceEditText.setText(data.get(0));
                    Log.d("testery", "onResults: " + data.get(0));
                } else if (micCount.equals("4")) {
                    micButtonDestination.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    destEditText.setText(data.get(0));
                } else if (micCount.equals("0")) {
                    Log.e("at 0", " : ");
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButtonVenue.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.e("mtion", " " + motionEvent.getAction() + MotionEvent.ACTION_UP);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    micCount = "0";
                    Log.e("onCLick", " : 1 : ACTION_UP");
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    micCount = "1";
                    micButtonVenue.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    Log.e("onCLick", " : 1 : ACTION_DOWN");
                }
                return false;
            }
        });
        micButtonBuilding.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                    micCount = "0";
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    micCount = "2";
                    micButtonBuilding.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });
        micButtonSource.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override

            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                    micCount = "0";
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    micCount = "3";
                    micButtonSource.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });
        micButtonDestination.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                    micCount = "0";
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    micCount = "4";
                    micButtonDestination.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });
    }

    private void findIds() {
        selectSourceModel = findViewById(R.id.selectSourceLinearLayout);
        selectDestinationModel = findViewById(R.id.selectDestinationLinearLayout);
        topHeaderText = findViewById(R.id.topHeaderText);
        //to make the path search widget animation
        venueBuildingView = findViewById(R.id.venueBuildingView);
        sourceViewToAnimate = findViewById(R.id.sourceViewToAnimate);
        back = findViewById(R.id.back);
        destinationViewToAnimate = findViewById(R.id.destinationViewToAnimate);
        hsv = findViewById(R.id.horizontalScrollView);
        recentMessage = findViewById(R.id.recentMessage);
        floorLevelLayout = findViewById(R.id.floorLevelLayout);
        floorLevelLayoutInfo = findViewById(R.id.floorLevelLayoutInfo);
        floorLevelLayoutContainer = findViewById(R.id.floorLevelLayoutContainer);
        recentMessageContainer = findViewById(R.id.recentMessageContainer);
        debugView = findViewById(R.id.debugView);
        debugView2 = findViewById(R.id.debugView2);
        imageMsg = findViewById(R.id.imageMsg);
        searchSourceDialog = findViewById(R.id.searchSourceDialog);
        swap = findViewById(R.id.swap);
        swap.setVisibility(View.GONE);
        searchDestinationDialog = findViewById(R.id.searchDestinationDialog);
        venueEditText = findViewById(R.id.venueList);
        buildingEditText = findViewById(R.id.buildingList);
        EraseVenueTextBox = findViewById(R.id.EraseVenueTextBox);
        EraseBuildingTextBox = findViewById(R.id.EraseBuildingTextBox);

        modal = findViewById(R.id.modal);
        modalDestinationDistance = findViewById(R.id.modalDestinationDistance);
        modalDestinationName = findViewById(R.id.modalDestinationName);
        modalDestinationVenueName = findViewById(R.id.modalDestinationVenueName);
        modalDestinationDateTime = findViewById(R.id.modalDestinationDateTime);
        modalDirection = findViewById(R.id.modalDirection);

        modalSource = findViewById(R.id.modalSource);
        modalSourceName = findViewById(R.id.modalSourceName);
        modalSourceVenueDateTime = findViewById(R.id.modalSourceVenueDateTime);
        modalSourceVenueName = findViewById(R.id.modalSourceVenueName);

        modalRoute = findViewById(R.id.modalRoute);
        information_modal_dropdown = findViewById(R.id.information_modal_dropdown);
        viaFloorConnection = findViewById(R.id.viaFloorConnection);
        distance_in_meter = findViewById(R.id.distance_in_meter);
        distance_in_meter2 = findViewById(R.id.distance_in_meter2);
        distance_in_time = findViewById(R.id.distance_in_time);
        infoModal = findViewById(R.id.infoModal);
        reLocalize = findViewById(R.id.reLocalize);
        volumeButton = findViewById(R.id.volumeButton);
        navigationButton = findViewById(R.id.navigationButton);

        sourceRouteInfo = findViewById(R.id.sourceRouteInfo);
        destRouteInfo = findViewById(R.id.destRouteInfo);
        routeTime = findViewById(R.id.routeTime);
        routeText = findViewById(R.id.routeText);
        save_location_modal = findViewById(R.id.save_location_modal);
        saveOption = findViewById(R.id.saveOption);
        custom_save_option = findViewById(R.id.custom_save_option);
        save_location_text = findViewById(R.id.save_location_text);
        saved_as = findViewById(R.id.saved_as);
        save_success = findViewById(R.id.save_success);
        custom_save_option_text = findViewById(R.id.custom_save_option_text);
        save_location = findViewById(R.id.save_location);
        cancel_save_location = findViewById(R.id.cancel_save_location);
        save_location_type_home = findViewById(R.id.save_location_type_home);
        save_location_type_work = findViewById(R.id.save_location_type_work);
        save_location_type_custom = findViewById(R.id.save_location_type_custom);
        micButtonVenue = findViewById(R.id.mic_button_venue);
        micButtonBuilding = findViewById(R.id.mic_button_building);
        micButtonSource = findViewById(R.id.mic_button_source_button);
        micButtonDestination = findViewById(R.id.mic_button_destination_button);
        sliderView = findViewById(R.id.sliderView);
        welcomeView = findViewById(R.id.welcomeView);
        loadingView = findViewById(R.id.loadingView);
    }

    @SuppressLint({"MissingPermission", "WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app-database").build();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    String message = "Please wait";
                    tts.setLanguage(Locale.US);
                    tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);

                    // Show a Snackbar with the message
                    View rootView = findViewById(android.R.id.content);
                    rootView.setVisibility(View.VISIBLE);


                    //Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        setContentView(R.layout.activity_navigation);

        findIds();
        initViews();
        mCacheHelper = new CacheHelper(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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
        step_size = (double) prefs.getFloat("step_size", 1.94f);
        audio_language = prefs.getString("language", "en");
        String handicap = prefs.getString("navigationMode", "wheelchair");
        isUserHandicap = (handicap.equals("wheelchair"));

        if (audio_language.equals("hi")) {
            welcomeView.setText("नेविगेशन में आपका स्वागत है | ऊपर दिए बटन्स का प्रयोग कीजिये नेविगेशन के लिए");
        }

        //disabling any touch actions on loadingView and horizontalScrollView
        loadingView.setOnTouchListener((view, motionEvent) -> true);
        hsv.setOnTouchListener((view, motionEvent) -> true);

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

        getNearestVenue();
        initSpinners(); //initializes adapters and list for source and destination dropdown spinners
        initializeActivity();
        //to get canvas width and height
        //this is run after view is laid out, only after which we can get canvas width and height
//        sliderView.post(() -> initializeActivity() );
        ufo = new UFOBeaconManager(this.getApplicationContext());
//        BluetoothMedic medic = BluetoothMedic.getInstance(); //bluetooth medic is useful whenever bluetooth stack faces problem
//        medic.enablePowerCycleOnFailures(this); //whenever bluetooth faces problem bluetooth will be power cycled
//        medic.enablePeriodicTests(this, BluetoothMedic.SCAN_TEST);

        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        mainContainer = findViewById(R.id.mainContainer);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocation();
    }

    public void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double longi = location.getLongitude();

                            LatLng sydney = new LatLng(lat, longi);
                            mMap.addMarker(new MarkerOptions()
                                    .position(sydney)
                                    .title("your location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
                        }
                    }
                });
    }

    ;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    private void takeToWebApp() {
        Intent i = new Intent(Navigation.this, WebviewActivity.class);
        i.putExtra("URL", getApplicationContext().getResources().getString(R.string.webapp));
        startActivity(i);
    }

    private void animatePath() {
        pathFloorObjs.get(sliderViewCounter).getCanvasView().animatePath();
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


    private void getNearestVenue() {
        venueListAdapter = new ArrayAdapter<>(Navigation.this, android.R.layout.simple_dropdown_item_1line, venueList);
        venueEditText.setAdapter(venueListAdapter);
        venueListAdapter.notifyDataSetChanged();
        loadingView.setVisibility(View.GONE);
        initVenueListClicks();
        Log.e("getNearestVenue", "venueList ; " + venueList + ", " + currentVenue);

        currentVenue = venueNamenew;
        venueEditText.setHint("" + currentVenue);
        currentBuilding = buildingNamenew;
        buildingList.clear();
        hideKeyBoard();
        StringBuilder selectedVenue = new StringBuilder();
        for (String s : Utils.splitCamelCaseString(initialVenueName)) {
            selectedVenue.append(s).append(" ");
        }
        topHeaderText.setText(currentVenue);
        getNearestBuilding();
    }

    private void initVenueListClicks() {
        venueEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sourceName = adapterView.getItemAtPosition(i).toString();
                Log.e("Venue Souce", sourceName);
                currentVenue = sourceName;
                buildingEditText.setText("");
                topHeaderText.setText(sourceName);
                venueEditText.setHint("Change Venue");
                currentBuilding = null;
                buildingList.clear();
                modal.setVisibility(View.GONE);
                modalSource.setVisibility(View.GONE);
                hideKeyBoard();
                clearVariableList();
                searchSourceDialog.setText("");
                searchSourceDialog.setHint("Select source location");
                searchDestinationDialog.setText("");
                searchDestinationDialog.setText("Select destination location");
                getNearestBuilding();
            }
        });
        //        if the input is not in the list then show error icon on the right
        venueEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String currInput = venueEditText.getText().toString();
                if (!currInput.equals("") && currInput.length() > 0 && !venueList.contains(currInput)) {
                    venueEditText.setError("Please select from the dropdown");
                }
            }
        });
    }


    private void getNearestBuilding() {

        currentBuilding = initialBuildingName;

        hideKeyBoard();

        // Set buildingEditText hint to the current building
        buildingEditText.setHint("" + currentBuilding);

        // Set topHeaderText based on the current building and currentVenue
        topHeaderText.setText(currentBuilding + ", " + currentVenue);

        // Hide the modal view
        modal.setVisibility(View.GONE);

        // Set buildingEditText hint to "Change Building"
        buildingEditText.setHint("Change Building");

        // Make or hide other views as needed
        reLocalize.setVisibility(View.VISIBLE);
        modalSource.setVisibility(View.GONE);
        venueBuildingView.setVisibility(View.GONE);
        searchSourceDialog.setVisibility(View.GONE);
        swap.setVisibility(View.GONE);
        searchDestinationDialog.setVisibility(View.GONE);
        searchSourceDialog.setText("");
        searchSourceDialog.setHint("Select source location");
        searchDestinationDialog.setText("");
        searchDestinationDialog.setText("Select destination location");
        selectSourceModel.setVisibility(View.VISIBLE);
        selectDestinationModel.setVisibility(View.VISIBLE);

        // Clear any variable lists
        clearVariableList();

        // Perform the necessary action (e.g., calling getBuildingInfo())
        getBuildingInfo();

    }

    private void initBuildingListClicks() {
        buildingEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sourceName = adapterView.getItemAtPosition(i).toString();
                currentBuilding = sourceName;
                hideKeyBoard();
                selectSourceModel.setVisibility(View.VISIBLE);
                selectDestinationModel.setVisibility(View.VISIBLE);
                modal.setVisibility(View.GONE);
                topHeaderText.setText(sourceName + ", " + currentVenue);
                buildingEditText.setHint("Change Building");
                reLocalize.setVisibility(View.VISIBLE);
                modalSource.setVisibility(View.GONE);
                venueBuildingView.setVisibility(View.GONE);
                searchSourceDialog.setVisibility(View.GONE);
                swap.setVisibility(View.GONE);
                searchDestinationDialog.setVisibility(View.GONE);
                searchSourceDialog.setText("");
                searchSourceDialog.setHint("Select source location");
                searchDestinationDialog.setText("");
                searchDestinationDialog.setText("Select destination location");
                clearVariableList();
                getBuildingInfo();

            }
        });
        //        if the input is not in the list then show error icon on the right
        buildingEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String currInput = buildingEditText.getText().toString();
                if (!currInput.equals("") && currInput.length() > 0 && !buildingList.contains(currInput)) {
                    buildingEditText.setError("Please select from the dropdown");
                }
            }
        });
    }

    private void initSpinners() {
        sourceEditText = findViewById(R.id.sourcePointEditText);
        sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sourcePointsList);
        sourceEditText.setAdapter(sourceAdapter);
        sourceEditText.setThreshold(1);

        destEditText = findViewById(R.id.destinationPointEdittext);
        destAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, destPointsList);
        destEditText.setAdapter(destAdapter);
        destEditText.setThreshold(1);
        if(MainActivity.desna != null){
            Log.d("testuhh", "initSpinners: "+MainActivity.desna);
            destEditText.setText(MainActivity.desna);
        }
    }


    private void initializeActivity() {
        //initialize every thing required before showing to user, till then show some animations to user
        initializeTTS();
    }

    private void initializeTTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int ttsLang;
                if (audio_language.equals("en")) {
                    ttsLang = textToSpeech.setLanguage(Locale.US);
                } else {
                    ttsLang = textToSpeech.setLanguage(new Locale("hi"));
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
    private void getBuildingInfo() {
        Log.e("getBuildingInfo", " : " + buildingName);

        //gets all nodes of this building
        handler.post(() -> {
            //check if building is in cache or not
            String existingData = mCacheHelper.dataGivenBuilding(buildingName);
//                String existingData = null;
            if (existingData != null) {
                try {
                    processBuildingData(new JSONArray(existingData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                makeBuildingDataRequest();
            }
        });
        hideKeyBoard();
    }

    private void makeBuildingDataRequest() {
        showPopup(Sentences.please_wait.getSentence(audio_language));
        Log.e("currentVenue", " : " + currentVenue + " : " + currentBuilding);
        String venue = initialVenueName, building = initialBuildingName;
//        for (String s : currentVenue.split(" ")) {
//            venue += s;
//        }
//        for (String s : currentBuilding.split(" ")) {
//            building += s;
//        }
        if (currentVenue != null && currentVenue.length() > 0 && currentBuilding != null && currentBuilding.length() > 0) {

            new AsyncTask<Void, Void, BuildingDataEntity>() {
                @Override
                protected BuildingDataEntity doInBackground(Void... voids) {
                    // Retrieve data from the Room Database here
                    return appDatabase.buildingDataDao().getBuildingDataByBuildingName(initialBuildingName);
                }

                @Override
                protected void onPostExecute(BuildingDataEntity cachedData) {
                    if (cachedData != null) {
                        // Data is available locally, use it
                        try {
                            JSONArray response = new JSONArray(cachedData.getResponseData());
                            processBuildingData(response);
                        } catch (JSONException e) {
                            Log.d("roomdbb", "onPostExecute: present data " + e);
                            e.printStackTrace();
                        }
                        Log.d("roomdbb", "onPostExecute: present data ");
                    } else {
                        Log.d("roomdbb", "onPostExecute: fetching data ");
                        String allNodesURL = base_url + "v1/app/android-navigation/" + venue + "/" + building + "/null";
                        JsonArrayRequest getAllNodesReq = new JsonArrayRequest
                                (Request.Method.GET, allNodesURL, null, response -> {

                                    processBuildingData(response);
                                    mCacheHelper.insertData("abcd", buildingName, response.toString(), System.currentTimeMillis());
                                }, volleyError -> {
                                    if (volleyError instanceof NetworkError) {
                                        retryPopup("Cannot connect to Internet...Please check your connection!");
                                    } else if (volleyError instanceof ServerError) {
                                        retryPopup("Server error!!");
                                    } else if (volleyError instanceof AuthFailureError) {
                                        retryPopup("Server error!");
                                    } else if (volleyError instanceof ParseError) {
                                        retryPopup("Some error occurred!");
                                    } else if (volleyError instanceof NoConnectionError) {
                                        retryPopup("Cannot connect to Internet...Please check your connection!");
                                    } else if (volleyError instanceof TimeoutError) {
                                        retryPopup("Connection TimeOut! Please check your internet connection.");
                                    } else {
                                        retryPopup("Server error occurred!");
                                    }
                                });

                        getAllNodesReq.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        getAllNodesReq.setShouldCache(false);
                        getAllNodesReq.setTag(this);
                        MyRequestQueue.getCache().clear();
                        MyRequestQueue.add(getAllNodesReq);
                    }
                }
            }.execute();
        } else {
            Toast.makeText(Navigation.this, "Unable to Locate your location, Please Select your building", Toast.LENGTH_LONG).show();
        }
    }


    //processes the response data
    private void processBuildingData(JSONArray response) {
        clearVariableList();
        //response array has object corresponding to different buildings
        try {
            for (int j = 0; j < response.length(); j++) {

                JSONObject jsonObject = response.getJSONObject(j);
                String geometryType = jsonObject.getString("geometryType");
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                int coordinateX = jsonObject.getInt("coordinateX");
                int coordinateY = jsonObject.getInt("coordinateY");
                String nodeName = jsonObject.getString("name");
                String floorName = jsonObject.getString("floor");
                if (GeometryTypes.NODE.value().equalsIgnoreCase(geometryType)) {
                    //node data in this jsonObject
                    String elementType = jsonObject.getJSONObject("element").getString("type");
                    String elementSubType = jsonObject.getJSONObject("element").getString("subType");
                    String coordinates = coordinateX + "," + coordinateY;
                    nodeCoordToFloorElement.put(coordinates, elementType);
                    Point flrConnCoord = new Point(coordinateX, coordinateY);
                    if (ElementTypes.SERVICES.value().equalsIgnoreCase(elementType)
                            && "beacons".equalsIgnoreCase(elementSubType)
                    ) {
                        propertiesObject.getString("macId");
                        String nodeMacId = propertiesObject.getString("macId").trim();

                        beaconObj.addBeacon(nodeMacId, floorName, coordinates);
                        alwaysActiveScansObj.addBeacon(nodeMacId);

                    } else {
                        if (ElementTypes.FLOOR_CONNECTION.value().equalsIgnoreCase(elementType)) {
                            String newNodeName = propertiesObject.getString("name");

                            if (!floor_to_floorConn_map.containsKey(floorName)) {
                                floor_to_floorConn_map.put(floorName, new HashMap<>());
                            }

                            floor_to_floorConn_map.get(floorName).put(newNodeName, flrConnCoord);
                            if (nodeName.length() > 0) {
                                nodeName = nodeName + " (L"+textToNumber(floorName)+")";
                            } else {
                                nodeName = newNodeName + " (L"+textToNumber(floorName)+")";
                            }
                            sourceAPIData.put(nodeName, jsonObject.toString());
                            nodeNameToDetails.put(nodeName, new String[]{floorName, coordinates});
                            sourcePointsList.add(nodeName);
                            destPointsList.add(nodeName);
                        } else if (elementSubType.equals("restRoom") || elementSubType.equals("drinkingWater")) {
                            if (nodeName.length() > 0) {
                                nodeName = nodeName + " (L"+textToNumber(floorName)+")";
                                sourceAPIData.put(nodeName, jsonObject.toString());
                                nodeNameToDetails.put(nodeName, new String[]{floorName, coordinates});
                                sourcePointsList.add(nodeName);
                                destPointsList.add(nodeName);
                            } else {

                            }
                        } else {
                            if (nodeName.length() > 0) {
                                nodeName = nodeName + " (L"+textToNumber(floorName)+")";
                                sourceAPIData.put(nodeName, jsonObject.toString());
                                nodeNameToDetails.put(nodeName, new String[]{floorName, coordinates});
                                sourcePointsList.add(nodeName);
                                destPointsList.add(nodeName);
                            } else {

                            }
                        }
                    }

                    if(elementType.equals("Rooms") && elementSubType.equals("room door")){

                        if (!floorToNodes.containsKey(floorName)) {
                            ArrayList<String[]> arr = new ArrayList<>();
                            floorToNodes.put(floorName, arr);
                        }
                        String[] tup = {coordinates, nodeName, ""};
                        floorToNodes.get(floorName).add(tup); //just add to the nodes list of that floor
                    }

                    if (!floorToNodesbeacons.containsKey(floorName)) {
                        ArrayList<String[]> arr = new ArrayList<>();
                        floorToNodesbeacons.put(floorName, arr);
                    }
                    String[] tup = {coordinates, nodeName, ""};
                    floorToNodesbeacons.get(floorName).add(tup); //just add to the nodes list of that floor



                } else if (GeometryTypes.FLOOR.value().equalsIgnoreCase(geometryType)) {
                    //non walkables data in this jsonObject
                    int floorLength = propertiesObject.getInt("floorLength");
                    int floorBreadth = propertiesObject.getInt("floorBreadth");
                    String floorAngle = propertiesObject.getString("floorAngle");
                    Log.d("Floor Angle", floorAngle);
                    String fileName = propertiesObject.getString("fileName");
//                    Log.d("yeh", "hogya1");
//                    floorAngle = "245.0";
                    if (floorAngle.equals("null"))
                        floorAngle = "255.0";
                    floorToAngle.put(floorName, Float.parseFloat(floorAngle));
//                    Log.d("yeh", "hogya");
                    floorToNonWalkables.put(floorName, new ArrayList<>());
                    floorToSimplePoly.put(floorName, new ArrayList<>());
                    floorToImageName.put(floorName, fileName);

                    JSONArray nonWalkableGrids = propertiesObject.getJSONArray("nonWalkableGrids");
                    JSONArray nonWalkableClicks = propertiesObject.getJSONArray("clickedPoints");
                    JSONArray floorDistMatrix = propertiesObject.getJSONArray("flr_dist_matrix");
                    JSONArray frConn = propertiesObject.getJSONArray("frConn");
                    for (int k = 0; k < nonWalkableClicks.length(); k++) {

                        String s = nonWalkableClicks.getString(k).trim();
                        if (s.length() > 0) {
                            floorToSimplePoly.get(floorName).add(s);
                        }
                    }
                    for (int k = 0; k < nonWalkableGrids.length(); k++) {
                        String s = nonWalkableGrids.getString(k).trim();
                        if (s.length() > 0) {
                            floorToNonWalkables.get(floorName).add(s);
                        }
                    }
                    floorToflr_dist_matrix.put(floorName, floorDistMatrix.getString(0));
                    floorTofrConn.put(floorName, frConn.getString(0));
                    sourceFloorsList.add(floorName);
                    destFloorsList.add(floorName);
                    int[] dim = {floorLength, floorBreadth};
                    floorDim.put(floorName, dim);


                }
            }


            Collections.sort(sourcePointsList); //search is easier when list is sorted
            Collections.sort(destPointsList);
            Log.e("sourcePointsList : ", " ->" + sourceAPIData + "  %%  " + sourcePointsList + "  %%  " + destPointsList);
            sourceAdapter = new ArrayAdapter<>(Navigation.this, android.R.layout.simple_dropdown_item_1line, sourcePointsList);
            sourceEditText.setAdapter(sourceAdapter);
            destAdapter = new ArrayAdapter<>(Navigation.this, android.R.layout.simple_dropdown_item_1line, destPointsList);
            destEditText.setAdapter(destAdapter);
            sourceAdapter.notifyDataSetChanged();
            destAdapter.notifyDataSetChanged();
            connectorGraph = new ConnectorGraph(floor_to_floorConn_map, floorToNonWalkables, sourceFloorsList, floorDim);
//            Log.d("graph",connectorGraph.toString());

            enableBLE();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                initSpinnersClicks(); //now we have data so update spinners clicks
            }
        } catch (JSONException e) {
            e.printStackTrace();
            retryPopup("Server error!");
            Log.e("activityMain", "JSON Exception in fetching buildings");
        }

        String responseData = response.toString();

        // Create a BuildingDataEntity object
        BuildingDataEntity entity = new BuildingDataEntity();
        entity.setBuildingName(initialBuildingName);
        entity.setResponseData(responseData);

        // Insert the data into the Room Database
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Insert data into the Room Database here
                appDatabase.buildingDataDao().insertBuildingData(entity);
                return null;
            }
        }.execute();
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
        floorToNodesbeacons.clear();
        floorToAngle.clear();
        floorToNonWalkables.clear();
        floorToSimplePoly.clear();
        floorToImageName.clear();
        floorToflr_dist_matrix.clear();
        floorTofrConn.clear();
        floorDim.clear();
        sourceAPIData.clear();
        beaconObj = new BeaconDetails(5);
        alwaysActiveScansObj = new AlwaysActiveScans(beaconObj);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initSpinnersClicks() {

        CustomAutoCompleteAdapter autoCompleteAdapter = new CustomAutoCompleteAdapter(this, sourcePointsList);
        sourceEditText.setAdapter(autoCompleteAdapter);

// Clear the error when the user selects an item from the dropdown
        sourceEditText.setOnItemClickListener((adapterView, view, pos, l) -> {
            sourceEditText.setError(null);
        });

        // Initialize your adapter and set it to sourceEditText
        sourceEditText.setOnItemClickListener((adapterView, view, pos, l) -> {
            // User has to click on destination now, no choice
            reset();

            String sourceName = adapterView.getItemAtPosition(pos).toString();
            dest = sourceName;
            String[] S = nodeNameToDetails.get(sourceName);
            dah.updateFloorRotation(floorToAngle.get(S[0]));
            currLocation.setFloor(S[0]);
            dfl = S[0];
            currLocation.setName(sourceName);
            currLocation.setGridX(Integer.parseInt(S[1].split(",")[0]));
            currLocation.setGridY(Integer.parseInt(S[1].split(",")[1]));
            openSearchSourceDialogVisible = false;
            sourceViewToAnimate.setVisibility(View.GONE);
            venueBuildingView.setVisibility(View.GONE);
            searchSourceDialog.setText(sourceName);
            locationName = sourceName;
            floorName = S[0];
            setSourceModalData(sourceName);
            modalSourceEnable();
            hideKeyBoard();
        });


// Validate input on focus change and clear the error if a valid item is selected
        sourceEditText.setOnFocusChangeListener((view, b) -> {
            String currInput = sourceEditText.getText().toString();
            if (!currInput.isEmpty() && !sourcePointsList.contains(currInput)) {
                sourceEditText.setError("Please select from the dropdown");
            } else {
                sourceEditText.setError(null);
            }
        });

        destEditText.setOnItemClickListener((parent, view, pos, l) -> {
            welcomeView.setVisibility(View.GONE); //remove the welcome View

            String destName = parent.getItemAtPosition(pos).toString();
            Log.d("testuuuu", "initSpinnersClicks: "+ destName);
            source = destName;
            String[] D = nodeNameToDetails.get(destName);
            System.out.println(Arrays.toString(D));
            destLocation.setFloor(D[0]);

            destLocation.setName(destName);
            destLocation.setGridX(Integer.parseInt(D[1].split(",")[0]));
            destLocation.setGridY(Integer.parseInt(D[1].split(",")[1]));
            openSearchDestinationDialogVisible = false;
            destinationViewToAnimate.setVisibility(View.GONE);
            searchDestinationDialog.setText(destName);
            sourceEditText.clearFocus();
            hideKeyboard(this);
            destEditText.clearFocus();
            modalSource.setVisibility(View.GONE);
            if (locationName != null && locationName.length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
                modalEnable();
            } else if (currLocation != null && currLocation.name().length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
                modalEnable();
            } else {
                modal.setVisibility(View.GONE);
            }


            swap.setVisibility(View.VISIBLE);
            hideKeyBoard();

        });

        CustomAutoCompleteAdapter autoCompleteAdapterdest = new CustomAutoCompleteAdapter(this, destPointsList);
        destEditText.setAdapter(autoCompleteAdapterdest);

        //if the input is not in the list then show error icon on the right
        destEditText.setOnFocusChangeListener((view, b) -> {
            String currInput = destEditText.getText().toString();
            if (!currInput.equals("") && currInput.length() > 0 && !destPointsList.contains(currInput)) {
                destEditText.setError("Please select from the dropdown");
            }
        });

        startUFO();
        searchLocation();
    }


    private void swapper() {

        String sourceName = source;

        String[] S = nodeNameToDetails.get(sourceName);
        dah.updateFloorRotation(floorToAngle.get(S[0]));
        tempLocation = currLocation;
        currLocation = destLocation;
        openSearchSourceDialogVisible = false;
        sourceViewToAnimate.setVisibility(View.GONE);
        venueBuildingView.setVisibility(View.GONE);
        locationName = sourceName;
        destinationName = dest;
        floorName = S[0];
        setSourceModalData(source);
        modalSourceEnable();


        String destName = dest;
        String[] D = nodeNameToDetails.get(destName);
        System.out.println(Arrays.toString(D));
        destLocation = tempLocation;
        openSearchDestinationDialogVisible = false;
        destinationViewToAnimate.setVisibility(View.GONE);
        Log.d("testery", "swapper: " + destdialogtext);
        destEditText.setText(destdialogtext);
        searchDestinationDialog.setText(destdialogtext);
        modalSource.setVisibility(View.GONE);
        if (locationName != null && locationName.length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
            modalEnable();
        } else if (currLocation != null && currLocation.name().length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
            modalEnable();
        } else {
            modal.setVisibility(View.GONE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setSourceModalData(String sourceName) {
        try {
            if (sourceAPIData.containsKey(sourceName)) {
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(sourceAPIData.get(sourceName));
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                Log.e("jsonObject", sourceAPIData.get(sourceName));
                modalSourceName.setText(jsonObject.getString("name") + ", " + floorName + " floor");
                modalSourceVenueName.setText(currentBuilding + ", " + currentVenue);
                String status = null;
                String daysOpen = propertiesObject.getString("daysOpen") != "null" ? propertiesObject.getString("daysOpen") : "Mon to Fri";
                if (propertiesObject.getString("startTime") != "null" && propertiesObject.getString("endTime") != "null") {
                    String from = propertiesObject.getString("startTime");
                    String to = propertiesObject.getString("endTime");
                    Calendar calendar = Calendar.getInstance();
                    String currDate = new SimpleDateFormat("HH:mm").format(calendar.getTime());

                    Date date1 = new Date();
                    //This method returns the time in millis
                    long timeMilli = date1.getTime();

                    LocalTime localTime = LocalTime.parse(from);
                    int millis = localTime.toSecondOfDay() * 1000;
                    Date date = new Date(System.currentTimeMillis()); // This object contains the current date value
                    SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
                    Date date_from = formatter.parse(from);
                    Date date_to = formatter.parse(to);
                    Date dateNow = formatter.parse(currDate);
                    Log.e("jsonObject2", ", " + dateNow + " , " + date_from + ",  timeMilli " + date_to);

                    if (date_from.before(dateNow) && date_to.after(dateNow)) {
                        status = "Open";
                    } else {
                        status = "Closed";
                    }
                    Log.e("status", ", " + status);

                    modalSourceVenueDateTime.setText(status + " | " + propertiesObject.getString("startTime") + " to " + propertiesObject.getString("endTime") + " | " + daysOpen);
                } else {
                    modalSourceVenueDateTime.setText(daysOpen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setDestinationModalData(String destName) {
        try {
            if (sourceAPIData.containsKey(destName)) {
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(sourceAPIData.get(destName));
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                Log.e("jsonObject", sourceAPIData.get(destName));
                modalDestinationName.setText(jsonObject.getString("name"));
                modalDestinationVenueName.setText(currentBuilding + ", " + currentVenue);
                String status = null;
                String daysOpen = propertiesObject.getString("daysOpen") != "null" ? propertiesObject.getString("daysOpen") : "Mon to Fri";
                if (propertiesObject.getString("startTime") != "null" && propertiesObject.getString("endTime") != "null") {
                    String from = propertiesObject.getString("startTime");
                    String to = propertiesObject.getString("endTime");
                    Calendar calendar = Calendar.getInstance();
                    String currDate = new SimpleDateFormat("HH:mm").format(calendar.getTime());

                    Date date1 = new Date();
                    //This method returns the time in millis
                    long timeMilli = date1.getTime();

                    LocalTime localTime = LocalTime.parse(from);
                    int millis = localTime.toSecondOfDay() * 1000;
                    Date date = new Date(System.currentTimeMillis()); // This object contains the current date value
                    SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
                    Date date_from = formatter.parse(from);
                    Date date_to = formatter.parse(to);
                    Date dateNow = formatter.parse(currDate);
                    Log.e("jsonObject2", ", " + dateNow + " , " + date_from + ",  timeMilli " + date_to);

                    if (date_from.before(dateNow) && date_to.after(dateNow)) {
                        status = "Open";
                    } else {
                        status = "Closed";
                    }
                    Log.e("status", ", " + status);

                    modalDestinationDateTime.setText(status + " | " + propertiesObject.getString("startTime") + " to " + propertiesObject.getString("endTime") + " | " + daysOpen);
                } else {
                    modalDestinationDateTime.setText(daysOpen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void modalEnable() {
        modal.setVisibility(View.VISIBLE);
        modalRoute.setVisibility(View.GONE);
        modalDestinationName.setText(destLocation.name());
        modalDestinationVenueName.setText(currentBuilding + ", " + currentVenue);

        connectorGraph.updateSourceAndDestination(currLocation, destLocation);
        ArrayList<PathOption> pathOptions = connectorGraph.executeDijkstra();

        double shortestDistance = Double.MAX_VALUE; // Initialize with a very large value

        for (PathOption pathOption : pathOptions) {
            double currentDistance = pathOption.getPathDistance() * 0.3048;

            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance;
            }
        }

        Double distance = shortestDistance;
//        Double distance = pathOptions.get(0).getPathDistance() * 0.3048;
        modalDestinationDistance.setText("(" + Precision.round(distance, 1) + ") meter");
        distance_in_time.setText(destLocation.name() + "   ");
        distance_in_meter.setText(currentBuilding + ", " + currentVenue);
        distance_in_meter2.setText("(" + Precision.round(distance, 1) + ") meter");
        destRouteInfo.setText(destLocation.name() + "\n" + currentBuilding + ", " + currentVenue);
        if (ttsInitialized) {

            addToMessageQueue(destLocation.name() + "are" + Precision.round(distance, 1) + "meters away", L0);
            speakTTS(MessageText);
        }
    }


    private void modalSourceEnable() {
        modal.setVisibility(View.GONE);
        modalSource.setVisibility(View.VISIBLE);
        modalRoute.setVisibility(View.GONE);
        modalSourceName.setText(locationName);
        sourceEditText.setText(locationName);
        if (destinationName != null) {
            destEditText.setText(destinationName);
        }
        destEditText.requestFocus();
        modalSourceVenueName.setText(currentBuilding + ", " + currentVenue);
        sourceRouteInfo.setText(locationName + "\n" + currentBuilding + ", " + currentVenue);
//        modalSourceVenueDateTime.setText();

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

                if (currLocation.getFloor().equals(destLocation.getFloor())
                        && currLocation.getGridX() == destLocation.getGridX() && currLocation.getGridY() == destLocation.getGridY()) {
                    //source and destination are same so no point in searching
                    Toast.makeText(getApplicationContext(), "source and destination are same", Toast.LENGTH_SHORT).show();
                    back.setVisibility(View.VISIBLE);
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
                connectorGraph.updateSourceAndDestination(currLocation, destLocation);
                ArrayList<PathOption> pathOptions = connectorGraph.executeDijkstra();
                ArrayList<PathOption> pathOption = new ArrayList<>();
                if (isUserHandicap) {
                    for (PathOption p : pathOptions) {
                        Log.e("DIJKSTRA", p.getPassingFrom() + ": " + p.getPathDistance());
                        if (p.getPassingFrom().toLowerCase().contains("stair"))
                            continue;
                        pathOption.add(p);
                    }
                } else
                    for (PathOption p : pathOptions) {
                        Log.e("DIJKSTRA", p.getPassingFrom() + ": " + p.getPathDistance());
                        if (p.getPassingFrom().toLowerCase().contains("stair"))
                            pathOption.add(p);
                    }


                showPathOptionsModal(pathOption);

            } finally {
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

        if (pathOptions.size() == 1) {
            //no need to show modal when there is only 1 path
            Double distance = pathOptions.get(0).getPathDistance() * 0.3048;
            Double time_in_sec = Precision.round(distance / 0.5, 2);
            double minutes = (time_in_sec % 3600) / 60;
            double seconds = time_in_sec % 60;
            String[] arr = String.valueOf(minutes).split("\\.");
            String[] arr2 = String.valueOf(seconds).split("\\.");
            int[] time = new int[2];
            time[0] = Integer.parseInt(arr[0]);
            time[1] = Integer.parseInt(arr2[0]);

            infoModal.setVisibility(View.INVISIBLE);
            handlePath(pathOptions.get(0).getPath());
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);

        if (audio_language.equals("hi")) {
            builder.setTitle("कृपया एक रास्ता चुने");
        } else {
            builder.setTitle("Choose a path");
        }
        Log.d("yehH", "" + isUserHandicap);
        double minDistance = Double.MAX_VALUE;
        Log.d("yoyohoneysingh", "showPathOptionsModal: " + pathOptions);
        PathOption minPath = pathOptions.get(0);
        for (PathOption p : pathOptions) {
            if (p.getPathDistance() <= minDistance) {
                minDistance = p.getPathDistance();
                minPath = p;
            }
        }

        handlePath(minPath.getPath());
        String[] options = new String[pathOptions.size()];
        for (int i = 0; i < pathOptions.size(); i++) {
            PathOption p = pathOptions.get(i);
            if (audio_language.equals("hi")) {
                options[i] = p.getPassingFrom() + " का प्रयोग करके , दूरी : " + oneDecimalForm.format(p.getPathDistance()) + " फीट";
            } else {
                options[i] = "Using " + p.getPassingFrom() + ", Distance: " + oneDecimalForm.format(p.getPathDistance()) + " फीट";
            }

        }
        builder.setItems(options, (dialog, index) -> {
            Double distance = pathOptions.get(index).getPathDistance() * 0.3048;
            Double time_in_sec = Precision.round(distance / 0.5, 2);
            double minutes = (time_in_sec % 3600) / 60;
            double seconds = time_in_sec % 60;
            String[] arr = String.valueOf(minutes).split("\\.");
            String[] arr2 = String.valueOf(seconds).split("\\.");
            int[] time = new int[2];
            time[0] = Integer.parseInt(arr[0]);
            time[1] = Integer.parseInt(arr2[0]);
            distance_in_time.setText(time[0] + " min, " + time[1] + " sec");
            distance_in_meter.setText("  (" + Precision.round(distance, 1) + ") meter");
            viaFloorConnection.setText("via " + pathOptions.get(index).getPassingFrom());
            infoModal.setVisibility(View.VISIBLE);
            handlePath(pathOptions.get(index).getPath());
        });
        AlertDialog dialog = builder.create();
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();

    }

    private void handlePath(ArrayList<FloorObj> path) {
        pathFloorObjs.addAll(path);
        if (pathFloorObjs.size() == 0) {
            Toast.makeText(getApplicationContext(), "Path doesn't exist", Toast.LENGTH_SHORT).show();
            maxSliderViewCounter = 1;
            enableBLE();
            mainLogicThreadRunning = true;
            //remove the loading view
            loadingView.setVisibility(View.GONE);
            return;
        }


        pathsearched = true;
        currDisplayIndexOfPath = 0;
        jumpingOutStarted = false;
        jumpedOut = false;
        //we are sure now pathFloorObjs size>0
        //inflate sliderView
        maxSliderViewCounter = pathFloorObjs.size();

        String BASE_STATIC_URL = getResources().getString(R.string.STATIC_URL);

        int viewHeight;
        int viewWidth;
        double aspectRatio;
        for (int i = 0; i < pathFloorObjs.size(); i++) {
            FloorObj fobj = pathFloorObjs.get(i);
            fobj.setLengthAndBreadth(floorDim.get(fobj.getFloorName()));

            aspectRatio = fobj.getLength() * 1.0 / fobj.getBreadth();
            if (fobj.getLength() > fobj.getBreadth()) {
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

            fobj.setGpx(viewWidth * 1.0 / floorDim.get(fobj.getFloorName())[0]);
            fobj.setGpy(viewHeight * 1.0 / floorDim.get(fobj.getFloorName())[1]);
            fobj.setViewWidth(viewWidth);
            fobj.setViewHeight(viewHeight);

            String imgUrl = BASE_STATIC_URL + floorToImageName.get(fobj.getFloorName());
            Log.d("rooooomdb", "handlePath: "+imgUrl);

            SliderItem sItem = new SliderItem(viewWidth, viewHeight, imgUrl, fobj.getFloorName());
            //SliderViewInflator also assigns the corresponding canvas to fobj
            final SliderViewInflator sv = new SliderViewInflator(Navigation.this, null, sItem, fobj, activity_width, activity_height);
            sliderView.addView(sv);

            //add simple NW polys which will be useful in deciding whether a step transition is valid or not
            for (String s : floorToSimplePoly.get(fobj.getFloorName())) {
                fobj.addNWSimplePoly(s);
            }

            turningPointsMap.put(fobj.getFloorName(), new HashSet<>());
            //before interpolating, add turning points obtained from simple path in fobj.getpath()
            //a path with size>2 will only have turning points
            if (fobj.getPath().size() > 2) {
                for (Point p : fobj.getPath().subList(1, fobj.getPath().size() - 1)) {
                    turningPointsMap.get(fobj.getFloorName()).add(new Pair<>((int) (p.x * fobj.getGpx()), (int) (p.y * fobj.getGpy())));

                }
            }

            //interpolate the simple points path using gpx and gpy
            fobj.setPath(Utils.interpolatePath(fobj.getPath(), step_size, fobj.getGpx(), fobj.getGpy()));

            CanvasView c = fobj.getCanvasView();
            c.updateDotSizes(fobj.getLength(), fobj.getBreadth()); //update the size of path dots according to floor lengths
            c.updateEndPointName(fobj.getEndPointName());
            c.drawPath(fobj.getPath()); //draw corresponding path in that canvas
            ZoomRotationFrameLayout parentZoomLayout = (ZoomRotationFrameLayout) c.getParent();
            Point[] boundingBoxCorners = Utils.boundingBoxCorners(fobj.getPath());
            parentZoomLayout.updateBoundingBoxConstants(boundingBoxCorners, activity_width, activity_height);
            parentZoomLayout.zoomToBoundingBox();


        }

        FloorObj startFloor = pathFloorObjs.get(0);
        //set current location in pixels, which is basically the source in searched path
        xf = currLocation.getGridX() * startFloor.getGpx();
        yf = currLocation.getGridY() * startFloor.getGpy();
        dah.updateCanvasLocation((int) xf, (int) yf);
        dah.updateCanvasPrecision(100, startFloor.getGpx(), startFloor.getGpy());
        dah.enableDrawOnCanvas(pathFloorObjs.get(0).getCanvasView(), (ZoomRotationFrameLayout) pathFloorObjs.get(0).getCanvasView().getParent());

        Log.d("inflate", "handler");

        floorLevelLayoutContainer.setVisibility(View.VISIBLE);

        inflateLevelsView();


        stopAndOrientUser(startFloor);

        mainLogicThreadRunning = true;

        animatePath();

    }

    public static int textToNumber(String text) {
        text = text.toLowerCase().trim();
        switch (text) {
            case "ground":
                return 0;
            case "first":
                return 1;
            case "second":
                return 2;
            case "third":
                return 3;
            case "fourth":
                return 4;
            case "fifth":
                return 5;
            case "sixth":
                return 6;
            case "seventh":
                return 7;
            case "eighth":
                return 8;
            case "ninth":
                return 9;
            case "tenth":
                return 10;
            case "eleventh":
                return 11;
            case "twelfth":
                return 12;
            case "thirteenth":
                return 13;
            case "fourteenth":
                return 14;
            case "fifteenth":
                return 15;
            case "sixteenth":
                return 16;
            case "seventeenth":
                return 17;
            case "eighteenth":
                return 18;
            case "nineteenth":
                return 19;
            case "twentieth":
                return 20;
            default:
                throw new IllegalArgumentException("Invalid text: " + text);
        }
    }


    @SuppressLint("NewApi")
    private void inflateLevelsView() {
        //inflates the levels view shown on the right hand side (L0,L1,etc)
        floorLevelLayout.removeAllViews();
        floorLevelLayoutInfo.removeAllViews();
        sliderViewCounter = 0;
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = pathFloorObjs.size() - 1; i >= 0; i--) {
            Log.d("newpathplanning", "inflateLevelsView: " + pathFloorObjs.size());
            View v = inflator.inflate(R.layout.simple_floor_card, null);
            TextView simpleFloorCardItem = v.findViewById(R.id.simple_floor_card_img);
            if (i == 0) {
                simpleFloorCardItem.setText("L" + textToNumber(currLocation.getFloor()));
                simpleFloorCardItem.setBackgroundColor(Color.parseColor("#000000"));
            }
            if (i == 1) {
                simpleFloorCardItem.setText("L" + textToNumber(destLocation.getFloor()));
                simpleFloorCardItem.setBackgroundColor(Color.parseColor("#b5b5b5"));
            }
            if(!currLocation.getFloor().equals("ground")  && !destLocation.getFloor().equals("ground")){
                if(i==2){
                    simpleFloorCardItem.setText("L" + textToNumber("ground"));
                    simpleFloorCardItem.setBackgroundColor(Color.parseColor("#b5b5b5"));
                }

            }
            simpleFloorCardItem.setTextColor(Color.WHITE);
            simpleFloorCardItem.setTooltipText(pathFloorObjs.get(i).getFloorName() + " floor");
            int size = getResources().getDimensionPixelSize(R.dimen._35sdp);
            simpleFloorCardItem.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            floorLevelLayout.addView(v);
//            ImageView img = new ImageView(this);
//            img.setLayoutParams(new LinearLayout.LayoutParams(size,size));
//            if(i==0){
//                simpleFloorCardItem.setBackgroundColor(Color.parseColor("#4D000000"));
//                img.setPadding(40,40,40,40);
//                img.setImageResource(R.drawable.green_circle);
//            }
//            if(i==pathFloorObjs.size()-1){
//                img.setPadding(40,40,40,40);
//                img.setImageResource(R.drawable.red_circle);
//            }
//            floorLevelLayoutInfo.addView(img);

            simpleFloorCardItem.setOnClickListener(view -> {
                floorLevelLayout.getChildAt(pathFloorObjs.size() - 1 - sliderViewCounter).setBackgroundColor(0);

                int itemCount = floorLevelLayout.getChildCount();
                for (int qq = 0; qq < itemCount; qq++) {
                    View child = floorLevelLayout.getChildAt(qq);
                    if (child != simpleFloorCardItem) {
                        child.setBackgroundColor(Color.parseColor("#b5b5b5"));
                    }
                }
                simpleFloorCardItem.setBackgroundColor(Color.parseColor("#000000"));

                //get slider counter from name's end digit
                if (Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1)) == textToNumber(currLocation.getFloor())) {
                    sliderViewCounter = 0;
                }

                if(pathFloorObjs.size() == 3){
                    if (Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1)) == textToNumber(destLocation.getFloor())) {
                        sliderViewCounter = 2;
                    }
                    if(!currLocation.getFloor().equals("ground")  && !destLocation.getFloor().equals("ground")){
                        if (Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1)) == textToNumber("ground")) {
                            sliderViewCounter = 1;
                        }
                    }
                } else if (pathFloorObjs.size() == 2) {
                    if (Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1)) == textToNumber(destLocation.getFloor())) {
                        sliderViewCounter = 1;
                    }
                    if(!currLocation.getFloor().equals("ground")  && !destLocation.getFloor().equals("ground")){
                        if (Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1)) == textToNumber("ground")) {
                            sliderViewCounter = 2;
                        }
                    }
                }


                //sliderViewCounter = Integer.parseInt(simpleFloorCardItem.getText().toString().substring(1)) - 1;
                //accumulate width to scroll in variable
                int scrollWidth = 0;
                for (int j = 0; j < sliderViewCounter; j++) {
                    scrollWidth += sliderView.getChildAt(j).getWidth();
                }

                hsv.smoothScrollTo(scrollWidth, hsv.getScrollY());
                animatePath();
            });
        }

//        floorLevelLayout.post(() -> { if(floorLevelLayoutVisible){showLevelsButton.performClick(); } });
    }

    private FloorObj getFloorObjGivenName(String floor_name) {
        for (FloorObj fobj : pathFloorObjs) {
            if (fobj.getFloorName().equals(floor_name)) {
                return fobj;
            }
        }
        return null;
    }


    private void createAndRunMainLogicThread() {

        //create popupChecker thread too here

        popup_checker_thread = new Runnable() {
            @Override
            public void run() {
                int currPr = -1; //current priority
                Pair<String, MessagePriority> popped = null;
                while (popupMessagesQueue.size() > 0 && popupMessagesQueue.peek().second.getPriorityValue() >= currPr) {
                    Log.e("pop up msg ", String.valueOf(popupMessagesQueue.size()) + " : " + popupMessagesQueue);
                    popped = popupMessagesQueue.poll();
                    currPr = popped.second.getPriorityValue();
                }

                if (popped != null) {
                    Log.e("msg queue 1", " : " + popped.first + " : " + popped.second);
                    route.add(popped.first);
                    showMessage(popped.first);
                    handler.postDelayed(this, 2000);
                } else if (popupMessagesQueue.size() > 0) {
                    Log.e("pop up msg ", popupMessagesQueue.toString());
                    showMessage(popupMessagesQueue.poll().first);
                    handler.postDelayed(this, 1000);
                } else {
                    handler.postDelayed(this, 100);
                }


            }
        };
        handler.post(popup_checker_thread);

        //this is the main code which does all the distance calculation and detection
        //it assumes we have the information of the current floor and the beacons on the floor
        //so start this thread only after we have this information prior
        mainLogicThreadRunning = true;
        mainLogicThread = () -> {
            handler.postDelayed(mainLogicThread, 2000); //after every 2 secs it checks again
            currFloorObj = getFloorObjGivenName(currLocation.getFloor());
            if (mainLogicThreadRunning && currFloorObj != null) {
                queueLock.lock();
                try {
                    disableBLE();
                    if (beaconObj.getAllBeacons().length < 1) {
                        return;
                    }
                    //Calculate the weights of all the beacons of the building
                    ArrayList<Pair<String, Double>> beaconFusionSorted = new ArrayList<>();  // stores weights fused with pdr
                    ArrayList<Pair<String, Double>> beaconWeightsSorted = new ArrayList<>(); // stores weights only for beacons
                    for (String B : beaconObj.getAllBeacons()) {
                        //iterate over all beacons and calculate their weights
                        Point beaconCoord = beaconObj.getBeaconCoord(B);
                        double deltaX = beaconCoord.x - currLocation.getGridX();
                        double deltaY = beaconCoord.y - currLocation.getGridY();
                        double pdrDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) * gridLength;   //pdrDist is in metres
                        double C = beaconObj.weightOfBeacon(B);
                        double C2 = fusePDR_RSSI(pdrDist, C);
                        beaconFusionSorted.add(new Pair<>(B, C2));
                        beaconWeightsSorted.add(new Pair<>(B, C));
                    }

                    Collections.sort(beaconFusionSorted, (c1, c2) -> c1.second > c2.second ? -1 : c1.second.equals(c2.second) ? 0 : 1);
                    Collections.sort(beaconWeightsSorted, (c1, c2) -> c1.second > c2.second ? -1 : c1.second.equals(c2.second) ? 0 : 1);

                    debugView.setText("");
                    debugView.setTextColor(Color.RED);
//                    Log.w("BW", beaconWeightsSorted.toString());
//                    for (int i = 0; i < beaconWeightsSorted.size(); i++) {
//                        debugView.append(beaconWeightsSorted.get(i).first + " -- " + oneDecimalForm.format(beaconWeightsSorted.get(i).second) + "\n");
//                    }

                    Log.d("BFS", beaconFusionSorted.toString());

                    //FIRST CHECK of flow chart
                    double ABW = beaconWeightsSorted.size() > 0 ? beaconWeightsSorted.get(0).second : -1; //Actual top beacon weight
                    String floorOfBeacon = beaconWeightsSorted.size() > 0 ? beaconObj.getFloorOfBeacon(beaconWeightsSorted.get(0).first) : "";
                    Log.d("Average Beacon Weight", String.valueOf(ABW));

                    if (ABW >= MIN_BEACON_RECOGNITION_CONST) {
                        if (floorOfBeacon.equals(currLocation.getFloor())) {
                            debugView.append(ABW + "\n");
                            if (ABW >= STRONG_BEACON_RECOGNITION_CONST) {
                                if (pathsearched) {
                                    // if path is searched:
                                    //      If beacon is near path then drag the user to the path
                                    //      otherwise drag the user to the beacon and re-search the path to the destination
                                    Point beaconCoord = beaconObj.getBeaconCoord(beaconWeightsSorted.get(0).first);
                                    double[] nid = nearest_point_details(currFloorObj.getPath(), beaconCoord.x * currFloorObj.getGpx(), beaconCoord.y * currFloorObj.getGpy(), currFloorObj);
                                    if (nid[1] <= BEACON_NEAR_PATH_DIST) {
                                        if (
                                                lastLocalized.getGridX() != (int) nid[2] ||
                                                        lastLocalized.getGridY() != (int) nid[3] ||
                                                        (System.currentTimeMillis() - lastLocalizedTime) >= NEW_LOCALIZATION_THRESHOLD_TIME
                                        ) {
                                            currDisplayIndexOfPath = (int) nid[0];
                                            currFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                                            currLocation.setGridX((int) nid[2]);
                                            currLocation.setGridY((int) nid[3]);
                                            lastLocalized.setGridX((int) nid[2]);
                                            lastLocalized.setGridY((int) nid[3]);
                                            lastLocalizedTime = System.currentTimeMillis();

                                            xf = currFloorObj.getPath().get(currDisplayIndexOfPath).x;
                                            yf = currFloorObj.getPath().get(currDisplayIndexOfPath).y;
                                            dah.updateCanvasLocation((int) xf, (int) yf);
                                            dah.updateCanvasPrecision(100, currFloorObj.getGpx(), currFloorObj.getGpy());
                                            stopAndOrientUser(currFloorObj);
                                        }
                                    } else {
                                        //remove activity_navigation arrow from the canvases first
                                        for (FloorObj fobj : pathFloorObjs) {
                                            fobj.getCanvasView().disableArrow();
                                        }

                                        //localize to this beacon
                                        //everytime when detection is this strong
                                        currLocation.setGridX(beaconCoord.x);
                                        currLocation.setGridY(beaconCoord.y);
                                        lastLocalized.setGridX(beaconCoord.x);
                                        lastLocalized.setGridY(beaconCoord.y);
                                        lastLocalizedTime = System.currentTimeMillis();
                                        xf = beaconCoord.x * currFloorObj.getGpx();
                                        yf = beaconCoord.y * currFloorObj.getGpy();

                                        dah.updateCanvasLocation((int) xf, (int) yf);
                                        dah.updateCanvasPrecision(ABW, currFloorObj.getGpx(), currFloorObj.getGpy());
                                        dah.enableDrawOnCanvas(currFloorObj.getCanvasView(), (ZoomRotationFrameLayout) currFloorObj.getCanvasView().getParent());
                                        if (!pathsearched) {
                                            jumpedOut = true;
                                            jumpingOutStarted = true;
                                        }

                                        //RE SEARCH THE PATH TO THE DESTINATION
                                        //ONLY IF the active path doesn't have same sourceLocation
                                        if (pathsearched && !(lastSource.getGridX() == beaconCoord.x && lastSource.getGridY() == beaconCoord.y)) {
                                            //IF sourceLocation is this beacon means path is already searched
                                            //IF not then we need to search path again
//                                          //  speakTTS(Sentences.searching_location.getSentence(audio_language));
                                            //addToMessageQueue(Sentences.searching_location.getSentence(audio_language));
                                            searchPath();
                                            pdrEnabled = true;
                                            return;

                                        }
                                    }
                                }


                            } else {
                                //do nothing
                            }
                        } else {
                            // USER IS NOW ON A DIFFERENT FLOOR
                            // SO LOCALIZE TO THIS BEACON and CHANGE CURRENT FLOOR

                            //remove activity_navigation arrow from the canvases first
                            for (FloorObj fobj : pathFloorObjs) {
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
                                if (beaconFloorObj != null) {
                                    // means user on a floor which lies in our searched path,
                                    // but is different to the floor user is currently
                                    // scroll to this floor and enable draw on this floor's canvas

//                                        speakTTS(Sentences.arrived_on_floor.getSentence(audio_language,floorOfBeacon));
                                    addToMessageQueue(Sentences.arrived_on_floor.getSentence(audio_language, floorOfBeacon));

                                    //check the distance of user from the searched path
                                    //if its high then re-search the path again
                                    //if its low then drag him to the closest point in the path
//                                    double[] nid = nearest_point_index_and_distance(beaconFloorObj.getPath(),beaconCoord.x*beaconFloorObj.getGpx(),beaconCoord.y*beaconFloorObj.getGpy(),beaconFloorObj);

                                    double[] nid = nearest_point_details(beaconFloorObj.getPath(), beaconCoord.x * beaconFloorObj.getGpx(), beaconCoord.y * beaconFloorObj.getGpy(), beaconFloorObj);

                                    Log.e("nid, ", " " + nid);
                                    if (nid[1] > BEACON_NEAR_PATH_DIST) {
//                                            speakTTS(Sentences.searching_path.getSentence(audio_language));
                                        addToMessageQueue(Sentences.searching_path.getSentence(audio_language));
                                        searchPath();
                                        pdrEnabled = true;
                                        return;

                                    } else {
                                        currDisplayIndexOfPath = (int) nid[0];
                                        beaconFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                                        currLocation.setGridX((int) nid[2]);
                                        currLocation.setGridY((int) nid[3]);
                                        currLocation.setFloor(floorOfBeacon);
                                        lastLocalized.setGridX((int) nid[2]);
                                        lastLocalized.setGridY((int) nid[3]);
                                        lastLocalizedTime = System.currentTimeMillis();
                                        xf = beaconFloorObj.getPath().get(currDisplayIndexOfPath).x;
                                        yf = beaconFloorObj.getPath().get(currDisplayIndexOfPath).y;
                                        dah.updateCanvasLocation((int) xf, (int) yf);
                                        dah.updateCanvasPrecision(100, beaconFloorObj.getGpx(), beaconFloorObj.getGpy());
                                        pdrEnabled = true; //enable pdr again because it was disabled when user reached previous floor's destination
                                        stopAndOrientUser(beaconFloorObj);
                                    }

                                    try {
                                        floorLevelLayout.getChildAt(pathFloorObjs.size() - 1 - sliderViewCounter).setBackgroundColor(0);
                                    } catch (Exception ignore) {
                                    }
                                    sliderViewCounter = pathFloorObjs.indexOf(beaconFloorObj);
                                    try {
                                        floorLevelLayout.getChildAt(pathFloorObjs.size() - 1 - sliderViewCounter).setBackgroundColor(Color.parseColor("#4D000000"));
                                    } catch (Exception ignore) {
                                    }
                                    //accumulate width to scroll in variable
                                    int scrollWidth = 0;
                                    for (int i = 0; i < sliderViewCounter; i++) {
                                        scrollWidth += sliderView.getChildAt(i).getWidth();
                                    }
                                    hsv.smoothScrollTo(scrollWidth, hsv.getScrollY());

                                    xf = beaconCoord.x * beaconFloorObj.getGpx();
                                    yf = beaconCoord.y * beaconFloorObj.getGpy();
                                    dah.enableDrawOnCanvas(beaconFloorObj.getCanvasView(), (ZoomRotationFrameLayout) beaconFloorObj.getCanvasView().getParent());
                                    dah.updateCanvasLocation((int) xf, (int) yf);
                                    dah.updateCanvasPrecision(ABW, beaconFloorObj.getGpx(), beaconFloorObj.getGpy());
                                } else {
                                    //this is some unknown floor, so re-search the path if path was searched
                                    if (pathsearched) {
//                                            speakTTS(Sentences.arrived_on_unexpected_floor.getSentence(audio_language,floorOfBeacon));
                                        addToMessageQueue(Sentences.arrived_on_unexpected_floor.getSentence(audio_language, floorOfBeacon));
                                        searchPath();
                                        pdrEnabled = true;
                                        return;
                                    }
                                }
                            } else {
                                //already localized here, no point in re localizing
                            }

                        }
                    }


                    //SECOND CHECK of flow chart
                    double FBW = beaconFusionSorted.size() > 0 ? beaconFusionSorted.get(0).second : -1; //fused with pdr top beacon weight
                    if (FBW >= MIN_FUSION_CONST) {
                        if (pathsearched) {
                            //****DISPLAY TO THE NEAREST POINT IN THE PATH, WHICH IS CLOSEST TO THE BEACON
                            Point beaconCoord = beaconObj.getBeaconCoord(beaconFusionSorted.get(0).first);
//                            double[] nid = nearest_point_index_and_distance(currFloorObj.getPath(),beaconCoord.x*currFloorObj.getGpx(),beaconCoord.y*currFloorObj.getGpy(),currFloorObj);
                            double[] nid = nearest_point_details(currFloorObj.getPath(), beaconCoord.x * currFloorObj.getGpx(), beaconCoord.y * currFloorObj.getGpy(), currFloorObj);


                            if (nid[1] <= BEACON_NEAR_PATH_DIST) {
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
                } finally {
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
        handler.postDelayed(scanToggleThread, 5 * 60 * 1000);
    }

    private String getAnnotatedNodeGivenCoord(String floor, String coord) {
        //s : {coord,nodename,macid}
        for (String[] s : floorToNodes.get(floor)) {
            if (s[0].equals(coord)) {
                return s[1];
            }
        }
        return null; //otherwise not found then return null
    }

    //if a node is under thresholdDistance(in feets) then return that node
    private String[] getNodeNearUserland(String floor, int X, int Y, double thresholdDistance) {
        //s : {coord,nodename,macid}
        int nodeX, nodeY;
        double dist;
        for (String[] s : floorToNodes.get(floor)) {
            String[] coord = s[0].split(",");
            nodeX = Integer.parseInt(coord[0]);
            nodeY = Integer.parseInt(coord[1]);
            //calculate distance
            dist = Math.sqrt(Math.pow((X - nodeX), 2) + Math.pow((Y - nodeY), 2));
            if (dist <= thresholdDistance) {
                return s;
            }
        }
        return null;
    }

    private String[] getNodeNearUser(String floor, int X, int Y, double thresholdDistance) {
        //s : {coord,nodename,macid}
        int nodeX, nodeY;
        double dist;
        for (String[] s : floorToNodesbeacons.get(floor)) {
            String[] coord = s[0].split(",");
            nodeX = Integer.parseInt(coord[0]);
            nodeY = Integer.parseInt(coord[1]);
            //calculate distance
            dist = Math.sqrt(Math.pow((X - nodeX), 2) + Math.pow((Y - nodeY), 2));
            if (dist <= thresholdDistance) {
                return s;
            }
        }
        return null;
    }


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void searchLocationButton(View view) {
        if (handler.hasCallbacks(mainLogicThread)) handler.removeCallbacks(mainLogicThread);
        hideKeyBoard();
        selectSourceModel.setVisibility(View.VISIBLE);
        selectDestinationModel.setVisibility(View.VISIBLE);
        modal.setVisibility(View.GONE);
        modalSource.setVisibility(View.GONE);
        modalRoute.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        recentMessageContainer.setVisibility(View.GONE);
        searchLocation();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void searchLocation() {
        showPopup(Sentences.searching_location.getSentence(audio_language));
        if (ttsInitialized) speakTTS(Sentences.searching_location.getSentence(audio_language));
        if (currentBuilding != null) {
            loadingView.setVisibility(View.VISIBLE);
            beaconObj.clearCounts();
            //stop any active beacon searchLocation thread
            //also pause the mainLogicThread
            stopSearchLocationThread();
            mainLogicThreadRunning = false;
            enableBLE(); //enable ble

            final int[] scan_counter = {3}; //In scan_counter scans of 2000ms duration if found then good else not found

            searchLocationThread = () -> {
                disableBLE();
                ArrayList<Pair<String, Double>> beaconWeightsSorted = new ArrayList<>();
//                String bMac = beaconObj.getStrongestBeacon(preciseRSSICutoff); //get beacon with signal as strong as precisRSSICutoff

                for (String B : beaconObj.getAllBeacons()) {
                    //iterate over beacons in current floor and print the fusion constant
                    double C = beaconObj.weightOfBeacon(B);
                    beaconWeightsSorted.add(new Pair<>(B, C));
                    Log.e("beaconWeightsSorted : ", B + ", " + C);
                }
                Collections.sort(beaconWeightsSorted, (c1, c2) -> c1.second > c2.second ? -1 : c1.second.equals(c2.second) ? 0 : 1);

                Log.e("BWW", beaconWeightsSorted.toString());

//                debugView.setTextColor(Color.RED);
//                debugView.setText("");
//                for (int i = 0; i < beaconWeightsSorted.size(); i++) {
//                    debugView.append(beaconWeightsSorted.get(i).first + " -- " + oneDecimalForm.format(beaconWeightsSorted.get(i).second) + "\n");
//                }
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
                    SliderViewInflator sv = new SliderViewInflator(Navigation.this, null, sItem, thisFloorObj, activity_width, activity_height);
                    sliderView.addView(sv);
                    topHeaderText.setText(currentBuilding + ", " + currentVenue);
                    sourceEditText.setHint("Change source location");
                    openSearchSourceDialogVisible = false;
                    sourceViewToAnimate.setVisibility(View.GONE);
                    venueBuildingView.setVisibility(View.GONE);
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
//                    dah.updateFloorRotation(floorToAngle.get(thisFloorObj.getFloorName()));
                    String[] nearToUser = getNodeNearUser(currLocation.getFloor(), currLocation.getGridX(), currLocation.getGridY(), NEAR_LOCATION_DISTANCE);
                    String[] nearToUserland = getNodeNearUserland(currLocation.getFloor(), currLocation.getGridX(), currLocation.getGridY(), NEAR_LOCATION_DISTANCE);

                    if (nearToUserland != null && !nearToUserland[1].equals("undefined")) {
                        String msg = Sentences.location_is_at.getSentence(audio_language, nearToUserland[1], currLocation.getFloor(), getNodeDirection(nearToUserland[0], thisFloorObj));
                        String msg_toast = Sentences.location_is_at_toast.getSentence(audio_language, nearToUserland[1], currLocation.getFloor(), getNodeDirection(nearToUserland[0], thisFloorObj));
                        if(MainActivity.desna !=null){
                            destEditText.setText(MainActivity.desna);
                            String destName = MainActivity.desna;
                            String[] D = nodeNameToDetails.get(destName);
                            System.out.println(Arrays.toString(D));
                            destLocation.setFloor(D[0]);

                            destLocation.setName(destName);
                            destLocation.setGridX(Integer.parseInt(D[1].split(",")[0]));
                            destLocation.setGridY(Integer.parseInt(D[1].split(",")[1]));
                            openSearchDestinationDialogVisible = false;
                            destinationViewToAnimate.setVisibility(View.GONE);
                            searchDestinationDialog.setText(destName);
                            sourceEditText.clearFocus();
                            hideKeyboard(this);
                            destEditText.clearFocus();
                            modalSource.setVisibility(View.GONE);
                            if (locationName != null && locationName.length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
                                modalEnable();
                            } else if (currLocation != null && currLocation.name().length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
                                modalEnable();
                            } else {
                                modal.setVisibility(View.GONE);
                            }


                            swap.setVisibility(View.VISIBLE);
                            hideKeyBoard();
                            modalEnable();
                        }else {
                            destEditText.setText("");
                        }

                        searchSourceDialog.setText(nearToUserland[1]);
                        destdialogtext = nearToUserland[1];
                        locationName = nearToUserland[1];
                        floorName = floorOfBeacon;
                        currLocation.setName(locationName);
                        speakTTS(msg);
                        setSourceModalData(locationName);
                        modalSourceEnable();
                        Log.e("msg ", msg);
                        addToMessageQueue(msg);
                        showPopup(msg);
                        Toast.makeText(Navigation.this, msg_toast, Toast.LENGTH_SHORT).show();
                    } else if (nearToUser != null && !nearToUser[1].equals("undefined")) {
                        String msg = Sentences.location_is_at.getSentence(audio_language, nearToUser[1], currLocation.getFloor(), getNodeDirection(nearToUser[0], thisFloorObj));
                        String msg_toast = Sentences.location_is_at_toast.getSentence(audio_language, nearToUser[1], currLocation.getFloor(), getNodeDirection(nearToUser[0], thisFloorObj));
                        searchSourceDialog.setText(nearToUser[1]);
                        destdialogtext = nearToUser[1];
                        locationName = nearToUser[1];
                        floorName = floorOfBeacon;
                        currLocation.setName(locationName);
                        speakTTS(msg);
                        setSourceModalData(locationName);
                        modalSourceEnable();
                        Log.e("msg ", msg);
                        addToMessageQueue(msg);
                        showPopup(msg);
                        Toast.makeText(Navigation.this, msg_toast, Toast.LENGTH_SHORT).show();
                        if(MainActivity.desna !=null){
                            destEditText.setText(MainActivity.desna);
                            String destName = MainActivity.desna;
                            String[] D = nodeNameToDetails.get(destName);
                            System.out.println(Arrays.toString(D));
                            destLocation.setFloor(D[0]);

                            destLocation.setName(destName);
                            destLocation.setGridX(Integer.parseInt(D[1].split(",")[0]));
                            destLocation.setGridY(Integer.parseInt(D[1].split(",")[1]));
                            openSearchDestinationDialogVisible = false;
                            destinationViewToAnimate.setVisibility(View.GONE);
                            searchDestinationDialog.setText(destName);
                            sourceEditText.clearFocus();
                            hideKeyboard(this);
                            destEditText.clearFocus();
                            modalSource.setVisibility(View.GONE);
                            if (locationName != null && locationName.length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
                                modalEnable();
                            } else if (currLocation != null && currLocation.name().length() > 0 && destLocation != null && destLocation.name().length() > 0) {
//                setDestinationModalData(destLocation.name());
                                modalEnable();
                            } else {
                                modal.setVisibility(View.GONE);
                            }


                            swap.setVisibility(View.VISIBLE);
                            hideKeyBoard();
                            modalEnable();
                        }else {
                            destEditText.setText("");
                        }
                    } else {
                        speakTTS(Sentences.found_location.getSentence(audio_language, currLocation.getFloor()));
//                        addToMessageQueue(Sentences.found_location.getSentence(audio_language, currLocation.getFloor()));
                        showPopup(Sentences.found_location.getSentence(audio_language, currLocation.getFloor()));
                        if(MainActivity.desna !=null){
                            destEditText.setText(MainActivity.desna);
                        }else {
                            destEditText.setText("");
                        }

                    }
                    loadingView.setVisibility(View.GONE);
                    stopSearchLocationThread();
                    ZoomRotationFrameLayout parentZoomLayout = (ZoomRotationFrameLayout) thisFloorObj.getCanvasView().getParent();
                    Point[] boundingBoxCorners = {new Point((int) xf, (int) yf), new Point((int) xf, (int) yf), new Point((int) xf, (int) yf), new Point((int) xf, (int) yf)};
                    parentZoomLayout.updateBoundingBoxConstants(boundingBoxCorners, activity_width, activity_height);
                    parentZoomLayout.zoomToBoundingBox();
                    pdrEnabled = false;
                    return;
                }

                Log.d("BMW", "searchLocation: " + scan_counter[0]);
                if (scan_counter[0] == 0) {
                    //called when location is not found
                    loadingView.setVisibility(View.GONE);
                    if (ttsInitialized)
                        speakTTS(Sentences.unable_to_find_location.getSentence(audio_language));
                    addToMessageQueue(Sentences.unable_to_find_location.getSentence(audio_language));
                    showPopup(Sentences.unable_to_find_location.getSentence(audio_language));
                    stopSearchLocationThread();
                    return;
                }

                scan_counter[0]--;
                beaconObj.clearCounts();
                enableBLE();
                handler.postDelayed(searchLocationThread, 8000); //after every 12 secs it checks again
            };
            handler.post(searchLocationThread);
        } else {
            Toast.makeText(Navigation.this, "Unable to Locate your location, Please Select your building", Toast.LENGTH_LONG).show();
        }
    }

    private void stopSearchLocationThread() {
        mainLogicThreadRunning = true;
        try {
            handler.removeCallbacks(searchLocationThread);
        } catch (Exception ignore) {
        }
    }

    private double fuse2(double pdrDist, double rssiDist) {
        if (pdrDist < 1) {
            pdrDist = 1;
        }
        return FUSE_CONST * (pdrWeight * (1 / pdrDist)) + FUSE_CONST * (rssiWeight * (1 / rssiDist));
    }

    private double fusePDR_RSSI(double pdrDist, double beaconW) {
        if (pdrDist < 1) {
            return beaconW;
        }
        return (1 / pdrDist) * beaconW;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleSteep.updateAccel(event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        Log.d("STEP", "STEP");
        onStepFunction();
    }

    private void onStepFunction() {
        FloorObj activeFloorObj = getFloorObjGivenName(currLocation.getFloor());

        if (!pdrEnabled || activeFloorObj == null) {
            Log.d("step", "inactive");
            return;
        }

        queueLock.lock();
        try {
            double xfNew;
            double yfNew;
            float yaw = dah.getOrientationYaw(); //yaw is pointing angle
            Log.e("direction", String.valueOf(yaw));
            double distanceX = step_size * Math.sin(Math.toRadians(yaw)); //distanceX and distanceY are in feets
            double distanceY = step_size * Math.cos(Math.toRadians(yaw));


            //*******monitoring actual location code**********
            xfNew = xf + (distanceX) * (activeFloorObj.getGpx()); // (distanceX/gridLength) is number of grids moved in X direction
            yfNew = yf - (distanceY) * (activeFloorObj.getGpy());
            xfNew = (xfNew <= 0) ? 1.0 : Math.min(activeFloorObj.getViewWidth() - 1, xfNew); //so that xf doesn't go out of bounds
            yfNew = (yfNew <= 0) ? 1.0 : Math.min(activeFloorObj.getViewHeight() - 1, yfNew); //so that yf doesn't go out of bounds
            Point NEW = new Point();
            Point OLD = new Point((int) xf, (int) yf);
            NEW.x = (int) (xfNew);
            NEW.y = (int) (yfNew);
            Point validTP = activeFloorObj.getValidTransitionPoint(OLD, NEW);
            xf = validTP.x;
            yf = validTP.y;
            currLocation.setGridX((int) (validTP.x / activeFloorObj.getGpx()));
            currLocation.setGridY((int) (validTP.y / activeFloorObj.getGpy()));
            // xf = xfNew;
            // yf = yfNew;
            // currLocation.setX((int)(xf/gpx));
            // currLocation.setY((int)(yf/gpy));
            // liveCanvas.drawVirtualPoint((int)xf, (int)yf);
            //*******monitoring actual location code*********

            if (!pathsearched) {
                dah.updateCanvasLocation((int) xf, (int) yf);
                dah.updateCanvasPrecision(100, activeFloorObj.getGpx(), activeFloorObj.getGpy());
                return;

            }

            //below code runs only if path is searched

//            double[] ind_and_dist = nearest_point_index_and_distance(activeFloorObj.getPath(), xf,yf,activeFloorObj);
            double[] ind_and_dist = nearest_point_details(activeFloorObj.getPath(), xf, yf, activeFloorObj);

            //this will be used to display on canvas on step
            int displayX = 0;
            int displayY = 0;

            if (jumpingOutStarted && jumpedOut) {

                if (ind_and_dist[1] <= step_size) {
                    //user is back to path so display the closest point on path
                    jumpingOutStarted = false;
                    jumpedOut = false;
                    currDisplayIndexOfPath = (int) ind_and_dist[0];
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    xf = activeFloorObj.getPath().get(currDisplayIndexOfPath).x;
                    yf = activeFloorObj.getPath().get(currDisplayIndexOfPath).y;
                    displayX = (int) xf;
                    displayY = (int) yf;
//                    if(ttsInitialized) speakTTS(Sentences.back_to_path.getSentence(audio_language));
                    addToMessageQueue(Sentences.back_to_path.getSentence(audio_language));
                    stopAndOrientUser(activeFloorObj);
                } else {
                    // keep displaying actual location
                    displayX = (int) xf;
                    displayY = (int) yf;
                }

            } else if (jumpingOutStarted && !jumpedOut) {
                if (ind_and_dist[1] > 5 * step_size) {
                    //user has jumped out
                    jumpedOut = true;
                    displayX = (int) xf;
                    displayY = (int) yf;
                    if (ttsInitialized)
                        speakTTS(Sentences.going_away_from_path.getSentence(audio_language));
                    //addToMessageQueue(Sentences.going_away_from_path.getSentence(audio_language));
                    try {
                        handler.removeCallbacks(orientation_helper_thread);
                    } catch (Exception ignore) {
                    }
                } else if (ind_and_dist[1] <= 2 * step_size) {
                    //bring back to path without informing the user
                    jumpingOutStarted = false;
                    jumpedOut = false;
                    currDisplayIndexOfPath = (int) ind_and_dist[0];
                    xf = activeFloorObj.getPath().get(currDisplayIndexOfPath).x;
                    yf = activeFloorObj.getPath().get(currDisplayIndexOfPath).y;
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    displayX = (int) xf;
                    displayY = (int) yf;
                } else {
                    //keep displaying on the path while maintaining actual position.
                    // Display the nearest point on path to our actual location, use fakeDisplayX and fakeDisplayY
                    currDisplayIndexOfPath = (int) ind_and_dist[0]; //nearest point on path
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    displayX = activeFloorObj.getPath().get(currDisplayIndexOfPath).x;
                    displayY = activeFloorObj.getPath().get(currDisplayIndexOfPath).y;
                }

            } else {
                //haven't started jumping out of the path so work normally
                Point xy = getNextJumpPoint(activeFloorObj.getPath());
                if (xy.x == -1) {
                    jumpingOutStarted = true; //assume a skip of 1 step is adjustable
                    return;
                } else {
                    activeFloorObj.getCanvasView().updateLocationInPath(currDisplayIndexOfPath);
                    xf = xy.x;
                    yf = xy.y;
                    displayX = (int) xf;
                    displayY = (int) yf;
                }
            }

            dah.updateCanvasLocation(displayX, displayY);
            dah.updateCanvasPrecision(100, activeFloorObj.getGpx(), activeFloorObj.getGpy());
            if (displayX == activeFloorObj.getPath().get(activeFloorObj.getPath().size() - 1).x
                    && displayY == activeFloorObj.getPath().get(activeFloorObj.getPath().size() - 1).y) {
                //user has reached this floor's destination so disable PDR and instruct him on what to do
                int activeFloorIndex = pathFloorObjs.indexOf(activeFloorObj);
                vibe.vibrate(500);
                if (activeFloorIndex == pathFloorObjs.size() - 1) {
                    //means this is the final destination
                    speakTTS(Sentences.reached_destination.getSentence(audio_language, destLocation.name()));
                    pdrEnabled = false;
                    STRONG_BEACON_RECOGNITION_CONST = 16000;
                    //addToMessageQueue(Sentences.reached_destination.getSentence(audio_language, destLocation.name()));
                } else {
                    //not final destination but is the destination in this floor
//                    speakTTS(Sentences.reached_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName()));
//                    speakTTS(Sentences.use_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName(), pathFloorObjs.get(activeFloorIndex+1).getFloorName()));
                    addToMessageQueue(Sentences.reached_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName()) + "\n"
                            + Sentences.use_connector_point.getSentence(audio_language, activeFloorObj.getEndPointName(), pathFloorObjs.get(activeFloorIndex + 1).getFloorName())
                    );
                }
                pdrEnabled = false;
                return;

            }
            currLocation.setGridX((int) (xf / activeFloorObj.getGpx()));
            currLocation.setGridY((int) (yf / activeFloorObj.getGpy()));

            if (turningPointsMap.get(activeFloorObj.getFloorName()).contains(new Pair<>(displayX, displayY))) {
                stopAndOrientUser(activeFloorObj);
            }

            String[] nearToUser = getNodeNearUser(activeFloorObj.getFloorName(), currLocation.getGridX(), currLocation.getGridY(), NEAR_LOCATION_DISTANCE);

            if (nearToUser != null && !nearToUser[1].equals("undefined")) {
                String nearNodeElementType = nodeCoordToFloorElement.get(nearToUser[0]);
                MessagePriority PR;
                switch (nearNodeElementType) {
                    case "Rooms":
                        PR = L3;
                        break;
                    case "Doors":
                        PR = L1;
                        break;
                    case "FloorConnection":
                        PR = L1;
                        break;
                    case "RestRooms":
                        PR = L3;
                        break;
                    case "Services":
                        PR = L3;
                        break;
                    default:
                        PR = L3;
                        break;
                }
                if (lastSpokenNearToNode == null || !lastSpokenNearToNode.equals(nearToUser[1])) {
                    lastSpokenNearToNode = nearToUser[1];
                    if (audio_language.equals("hi")) {
                        addToMessageQueue("आपके " + getNodeDirection(nearToUser[0], activeFloorObj) + ", " + nearToUser[1] + " है ", PR);
                    } else {
//                        addToMessageQueue("You are near " + nearToUser[1] + " on your " + getNodeDirection(nearToUser[0], activeFloorObj), PR);
                        addToMessageQueue(nearToUser[1] + " on your " + getNodeDirection(nearToUser[0], activeFloorObj), PR);
                    }

                }
            }

        } finally {
            queueLock.unlock();
        }
    }

    private String getNodeDirection(String nodeCoordinates, FloorObj floorObj) {
        //returns whether the node is on left or right or on front

        int xcurr = (int) xf;
        int ycurr = (int) yf;
        int xnext = (int) (Integer.parseInt(nodeCoordinates.split(",")[0]) * floorObj.getGpx());
        int ynext = (int) (Integer.parseInt(nodeCoordinates.split(",")[1]) * floorObj.getGpy());
        double currToNextAngle = Math.toDegrees(Math.atan2((ynext - ycurr), (xnext - xcurr)));
        currToNextAngle += 90;
        if (currToNextAngle < 0) {
            currToNextAngle += 360;
        }

        double headingAngle = dah.getOrientationYaw();
        int diffval = (int) (currToNextAngle - headingAngle);


        if (diffval > 180) {
            diffval -= 360;
        } else if (diffval <= -180) {
            diffval += 360;
        }

//        if(Math.abs(diffval)>70){
//            return Sentences.back.getSentence(audio_language);
//        }

        int clockAmount;
        if (diffval < 0) {
            clockAmount = (int) Math.round((360 + diffval) / 30.0);
            //left direction
            if (clockAmount == 11 || clockAmount == 0 || clockAmount == 12 || clockAmount == 1) {
                return Sentences.front.getSentence(audio_language);
            }
            return Sentences.left.getSentence(audio_language);
        } else {
            //right direction
            clockAmount = (int) Math.round(diffval / 30.0);
            if (clockAmount == 11 || clockAmount == 0 || clockAmount == 12 || clockAmount == 1) {
                return Sentences.front.getSentence(audio_language);
            }
            return Sentences.right.getSentence(audio_language);
        }

    }

    //stops pdr and asks user to re-orient along the path
    private void stopAndOrientUser(FloorObj floorObj) {
        if (floorObj.getPath().size() < 2) {
            //can't orient in a path which is just a point
            return;
        }

        //remove any existing orientation_helper_threads
        try {
            handler.removeCallbacks(orientation_helper_thread);
        } catch (Exception ignore) {
        }

        Integer[] TAA = getTurnAmountAndAngle(floorObj);

        vibe.vibrate(200);
//        speakTTS(Utils.angleToClocks(TAA[0],audio_language));
        int numSteps = getNumOfSteps(floorObj);
        if (numSteps < STEPS_THRESHOLD_TO_MERGE) {
            addToMessageQueue(Utils.angleToClocks(TAA[0], audio_language) + (audio_language.equals("en") ? ", and " : ", और ") + Sentences.go_straight.getSentence(audio_language, String.valueOf(getNumOfSteps(floorObj))));

        } else {
            addToMessageQueue(Utils.angleToClocks(TAA[0], audio_language));

        }
        orientUserThread(TAA[1], getNumOfSteps(floorObj));

    }

    private int getNumOfSteps(FloorObj floorObj) {
        //gets number of steps to the next turning point
        //from the current location in the path
        int numStepsToGo = 1;
        int curr = currDisplayIndexOfPath + 1;
        ArrayList<Point> path = floorObj.getPath();
        while (curr < path.size()) {
            if (turningPointsMap.get(floorObj.getFloorName()).contains(new Pair<>(path.get(curr).x, path.get(curr).y))) {
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
        handler.postDelayed(() -> pdrEnabled = true, 1000);
        disableBLE();
        final double[] current_angle = new double[1];
        orientation_helper_thread = new Runnable() {
            @Override
            public void run() {
                current_angle[0] = dah.getOrientationYaw();
                if (Math.abs(angleToAchieve - current_angle[0]) < 45) {
                    vibe.vibrate(500);
                    //speakTTS(Sentences.go_straight.getSentence(audio_language, String.valueOf(numSteps)));
                    if (numSteps >= STEPS_THRESHOLD_TO_MERGE) {
                        addToMessageQueue(Sentences.go_straight.getSentence(audio_language, String.valueOf(numSteps)));
                    }
                    enableBLE();
                } else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(orientation_helper_thread);
    }


    private void enableBLE() {
        ble_scanning = true;
    }

    private void disableBLE() {
        ble_scanning = false;
    }


    private void startUFO() {
        if(Build.VERSION.SDK_INT <= 30){
            if (mBluetoothLeScanner != null) {
                Log.d("Navigation", "Service: Starting Scanning");
                mScanCallback = new SampleScanCallback();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
            }

        }else{
            if (mBluetoothLeScanner != null) {
                Log.d("Navigation", "Service: Starting Scanning");
                mScanCallback = new SampleScanCallback();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
            }

        }

    }

    public void toggleVenueBuildingView(View view) {
        if (!openVenueBuildingDaiologVisible) {
            if (openSearchSourceDialogVisible || openSearchDestinationDialogVisible) {
                openSearchSourceDialogVisible = false;
                sourceViewToAnimate.setVisibility(View.GONE);
                openSearchDestinationDialogVisible = false;
                destinationViewToAnimate.setVisibility(View.GONE);
            } else {
                openVenueBuildingDaiologVisible = true;
                venueBuildingView.setVisibility(View.VISIBLE);
            }
        } else {
            openVenueBuildingDaiologVisible = false;
            venueBuildingView.setVisibility(View.GONE);
        }
    }

    public void toggleSearchSourceDialog(View view) {
        if (!openSearchSourceDialogVisible) {
            if (openSearchDestinationDialogVisible || openVenueBuildingDaiologVisible) {
                openSearchDestinationDialogVisible = false;
                destinationViewToAnimate.setVisibility(View.GONE);
                openVenueBuildingDaiologVisible = false;
                venueBuildingView.setVisibility(View.GONE);
            } else {
                openSearchSourceDialogVisible = true;
                sourceViewToAnimate.setVisibility(View.VISIBLE);
            }
        } else {
            openSearchSourceDialogVisible = false;
            sourceViewToAnimate.setVisibility(View.GONE);
        }
    }

    public void toggleSearchDestinationDialog(View view) {
        if (!openSearchDestinationDialogVisible) {
            if (openSearchSourceDialogVisible || openVenueBuildingDaiologVisible) {
                openSearchSourceDialogVisible = false;
                sourceViewToAnimate.setVisibility(View.GONE);
                openVenueBuildingDaiologVisible = false;
                venueBuildingView.setVisibility(View.GONE);
            } else {
                openSearchDestinationDialogVisible = true;
                destinationViewToAnimate.setVisibility(View.VISIBLE);
            }
        } else {
            openSearchDestinationDialogVisible = false;
            destinationViewToAnimate.setVisibility(View.GONE);
        }
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @SuppressLint("NewApi")
    public void showSourDest(View view) {
//        clearVariableList();
        disableBLE();
        pdrEnabled = false;
        floorLevelLayoutContainer.setVisibility(View.GONE);
//        reLocalize.setVisibility(View.GONE);
        volumeButton.setVisibility(View.GONE);
        navigationButton.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        modal.setVisibility(View.VISIBLE);
        modalSource.setVisibility(View.GONE);
        modalRoute.setVisibility(View.GONE);
        recentMessageContainer.setVisibility(View.GONE);
        searchSourceDialog.setText("");
        searchSourceDialog.setHint("Select source location");
        selectSourceModel.setVisibility(View.VISIBLE);
        selectDestinationModel.setVisibility(View.VISIBLE);
        searchDestinationDialog.setText("Select destination location");
    }

    public void toggle_information_modal_dropdown(View view) {
        if (information_modal_dropdown_count) {
            information_modal_dropdown.setVisibility(View.GONE);
            information_modal_dropdown_count = false;
        } else {
            information_modal_dropdown.setVisibility(View.VISIBLE);
            information_modal_dropdown_count = true;
        }
    }

    public void EraseTextBox1(View view) {
        sourceEditText.setText("");
    }

    public void EraseTextBox2(View view) {
        destEditText.setText("");
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

        if (ble_scanning) {
            String macId = result.getDevice().getAddress();
            beaconObj.addRSSI(macId, result.getRssi());
            Log.w("receivedScan", "MACID: " + macId + ", RSSI: " + result.getRssi());
            alwaysActiveScansObj.addRSSI(macId, result.getRssi());
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


    private void stopUFO() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothLeScanner.stopScan(mScanCallback);
        } catch (Exception ignore) {
        }
//        ufo.stopScan(onSuccessBoolean -> Log.w("UFO_stopScan","STOPPED"), (i, s) -> Log.e("UFO_stopScan","UNABLE TO STOP")
//        );
    }


    private void hideKeyBoard() {
        //Hide:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (Exception ignore) {
            }
            ;
        }
//        if (imm.isAcceptingText()) {
//            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//        }


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

    private void showKeyBoard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }



    private void retryPopup(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Retry", (dialog, id) -> {
            // User clicked OK button
            makeBuildingDataRequest(); //resend the building request
        });
        builder.setNegativeButton("Go Back", (dialog, id) -> finish());


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goingAwayFromPathPopup() {
        pdrEnabled = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        if (audio_language.equals("en")) {
            builder.setMessage("You are going away from the Path.\nDo you want to Re-route?");
            builder.setPositiveButton("Yes", (dialog, id) -> {
                searchPath();
                pdrEnabled = true;
            });
            builder.setNegativeButton("No", (dialog, id) -> {
                pdrEnabled = true;
            });
        } else {
            builder.setMessage("आप रास्ते से दूर जा रहे है | क्या हम आपके लिए फिरसे रास्ता ढूंढे?");
            builder.setPositiveButton("हाँ", (dialog, id) -> {
                // User clicked OK button
                searchPath(); //resend the building request
                pdrEnabled = true;
            });
            builder.setNegativeButton("ना", (dialog, id) -> {
                pdrEnabled = true;
            });
        }


        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    private Point getNextJumpPoint(ArrayList<Point> path) {
        //checks if next jump point is negative direction or positive direction along the path using magnetometer
        //updates the currDisplayIndexOfPath
        if (currDisplayIndexOfPath == path.size() - 1) { //when its the last point
            currDisplayIndexOfPath = Math.max(0, path.size() - 2);
            return path.get(currDisplayIndexOfPath);
        } else if (currDisplayIndexOfPath == 0) { //when its the starting point
            currDisplayIndexOfPath = Math.min(1, path.size() - 1);
            return path.get(currDisplayIndexOfPath);
        } else {
            int x1 = path.get(currDisplayIndexOfPath).x;
            int y1 = path.get(currDisplayIndexOfPath).y;
            int x2 = path.get(currDisplayIndexOfPath + 1).x;
            int y2 = path.get(currDisplayIndexOfPath + 1).y;

            double currToNextAngle = Math.toDegrees(Math.atan2((y2 - y1), (x2 - x1)));
            currToNextAngle += 90;
            if (currToNextAngle < 0) {
                currToNextAngle += 360;
            }


            double absDiff = angleDiff(currToNextAngle, dah.getOrientationYaw());
            if (absDiff < 45) {
                currDisplayIndexOfPath++; //move to next node in path
            } else if (absDiff > 160) {
                currDisplayIndexOfPath--;
            } else {
                //angle has became greater so drag him out of the path
                return new Point(-1, -1);
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

    public static void speakTTS(String toSpeak) {
        Log.e("speakTTS", ": " + toSpeak);
        int speechStatus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speechStatus = textToSpeech.speak(toSpeak, QUEUE_ADD, null, null);
        } else {
            speechStatus = textToSpeech.speak(toSpeak, QUEUE_ADD, null);
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

        if (shouldExecuteOnResume) {
            //pdrEnabled=true; If you enable pdr then make sure all dependent variables otherwise app will crash
            if (mainLogicThreadRunning) handler.post(mainLogicThread);
            if (lastStateOfPDR) {
                pdrEnabled = true;
            }
            startUFO();

        } else {
            shouldExecuteOnResume = true;
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        stopUFO();
        dah.stop();
        if (ttsInitialized) textToSpeech.stop();
        try {
            MyRequestQueue.cancelAll(this);
        } catch (Exception ignore) {
        }
        //remove all runnables if present
        try {
            handler.removeCallbacks(searchLocationThread);
        } catch (Exception ignore) {
        }
        try {
            handler.removeCallbacks(mainLogicThread);
        } catch (Exception ignore) {
        }
        try {
            handler.removeCallbacks(scanToggleThread);
        } catch (Exception ignore) {
        }
        try {
            handler.removeCallbacks(orientation_helper_thread);
        } catch (Exception ignore) {
        }
        pdrEnabled = false;
        try {
            sm.unregisterListener(this, accel);
        } catch (Exception ignore) {
        }
        super.onDestroy();
    }

    public void customStep(View view) {
        onStepFunction();
    }


    //returns the nearest point on searched path of given floor obj from a given location (fromX, fromY) <- in pixels
//    private double[] nearest_point_index_and_distance(ArrayList<Point> currPath, double fromX, double fromY, FloorObj floorObj) {
//        double[] result = new double[]{0,Double.POSITIVE_INFINITY};//index 0=index of nearest point in currPath arraylist, index 1=distance of point

    private double[] nearest_point_details(ArrayList<Point> currPath, double fromX, double fromY, FloorObj floorObj) {
        double[] result = new double[]{0, Double.POSITIVE_INFINITY, 0, 0};//index 0=index of nearest point in currPath arraylist, index 1=distance of point, index2= gridX, index3=gridY
        for (int i = 0; i < currPath.size(); i++) {
            Point p = currPath.get(i);
            double dist = Math.sqrt(Math.pow((fromX - p.x) / floorObj.getGpx(), 2) + Math.pow((fromY - p.y) / floorObj.getGpy(), 2));
            if (dist < result[1]) {
                result[1] = dist;
                result[0] = i;
                result[2] = p.x;
                result[3] = p.y;
            }
            Log.e("dist", " " + dist);
        }
        return result;
    }

    //takes path from floorObj and gets the turn string and angle amount for next point in that path
    //if 1st index is +ve then its right turn else its left turn
    private Integer[] getTurnAmountAndAngle(FloorObj fobj) {

        int xcurr = fobj.getPath().get(currDisplayIndexOfPath).x;
        int ycurr = fobj.getPath().get(currDisplayIndexOfPath).y;
        int xnext = fobj.getPath().get(currDisplayIndexOfPath + 1).x;
        int ynext = fobj.getPath().get(currDisplayIndexOfPath + 1).y;
        double currToNextAngle = Math.toDegrees(Math.atan2((ynext - ycurr), (xnext - xcurr)));
        currToNextAngle += 90;
        if (currToNextAngle < 0) {
            currToNextAngle += 360;
        }

        double headingAngle = dah.getOrientationYaw();
        Log.d("checkingorient", "curr: " + currToNextAngle);
        Log.d("checkingorient", "headin: " + headingAngle);
        int diffval = (int) (currToNextAngle - headingAngle);
        if (diffval > 180) {
            diffval -= 360;
        } else if (diffval <= -180) {
            diffval += 360;
        }

        if((currToNextAngle - prevangle) > 22.5 && (currToNextAngle - prevangle) <= 67.5){
            diffval = 45;
        } else if ((currToNextAngle - prevangle) > 292.5 && (currToNextAngle - prevangle) <= 337.5) {
            diffval = 315;
        }
        prevangle = currToNextAngle;
        return new Integer[]{diffval, (int) currToNextAngle};
    }

    private void reset() {
        sliderView.removeAllViews();
        pathFloorObjs.clear();
        floorLevelLayout.removeAllViews();
        floorLevelLayoutInfo.removeAllViews();
        sliderViewCounter = 0;
        maxSliderViewCounter = 1;
    }


    public void resetCanvas(View view) {
        //re-orient and re-scale back to default the current active canvas
        if (pathFloorObjs.size() == 0) {
            return;
        }
        ZoomRotationFrameLayout zv = sliderView.getChildAt(sliderViewCounter).findViewById(R.id.zoomlayout);
        zv.resumeAutoRotation();

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

    public void showMessage(String text) {
        Log.e("text", text);
        if (ttsInitialized) speakTTS(text);
        recentMessage.setText(text);
        routeText.setText(text);
        if (text.contains("Right") || text.contains("right")) {
            imageMsg.setImageResource(R.drawable.turn_right);
        } else if (text.contains("Left") || text.contains("left")) {
            imageMsg.setImageResource(R.drawable.turn_left);
        } else if (text.contains("destination") && text.contains("Reached")) {
            imageMsg.setImageResource(R.drawable.route);
            back.setVisibility(View.VISIBLE);
            volumeButton.setVisibility(View.GONE);
            navigationButton.setVisibility(View.GONE);
        } else {
            imageMsg.setImageResource(R.drawable.straight);
        }
        recentMessageContainer.setVisibility(View.VISIBLE);
        if (text.equals(Sentences.going_away_from_path.getSentence(audio_language))) {
            goingAwayFromPathPopup();
        }
    }

    public void showPopup(String text) {

//        if (activePopup != null) {
//            //hide existing popups
//            activePopup.dismiss();
//        }
//        // inflate the layout of the popup window
//        LayoutInflater inflater = (LayoutInflater)
//                getSystemService(LAYOUT_INFLATER_SERVICE);
//        View popupView = inflater.inflate(R.layout.popup_window, null);
//        ((TextView) popupView.findViewById(R.id.popupText)).setText(text);
//
//        // create the popup window
//        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
//        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
//        boolean focusable = true; // lets taps outside the popup also dismiss it
//        activePopup = new PopupWindow(popupView, width, height, focusable);
//        activePopup.setOutsideTouchable(false);
//        activePopup.setFocusable(false);
//        activePopup.setAnimationStyle(R.style.Animation);
//
//        // show the popup window
//        // which view you pass in doesn't matter, it is only used for the window tolken
//        activePopup.showAtLocation(mainContainer, Gravity.CENTER, 0, 0);
//
//        //dismiss the popup after some time automatically
//        handler.postDelayed(() -> activePopup.dismiss(), 2000);
//        // dismiss the popup window when touched
////        popupView.setOnTouchListener(new View.OnTouchListener() {
////            @Override
////            public boolean onTouch(View v, MotionEvent event) {
////                popupWindow.dismiss();
////                return true;
////            }
////        });
    }

    private void errorPopup(String errorMessage) {
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

    public void shareLocation(View view) {
        vibe.vibrate(200);
        String floor = currLocation.getFloor();
        if (floor == null || floor.equals("")) {
            Toast.makeText(Navigation.this, "unable to share your location 1 " + floor, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] nearToUser = getNodeNearUser(currLocation.getFloor(), currLocation.getGridX(), currLocation.getGridY(), NEAR_LOCATION_DISTANCE);
        String shareBodyText;
        if (nearToUser != null && !nearToUser[1].equals("undefined")) {
            shareBodyText = "Hi! I am near " + nearToUser[1] + " in " + floor.toUpperCase() + " FLOOR of " + currentBuilding;
        } else {
            shareBodyText = "Hi! I am " + " in " + floor.toUpperCase() + " FLOOR of " + currentBuilding;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareSubText = "share your location";
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
        startActivity(Intent.createChooser(shareIntent, "Share With"));
    }

    public void saveLocation(View view) {
        vibe.vibrate(200);

        save_location_modal.setVisibility(View.VISIBLE);
        save_location_text.setText(currLocation.name() + ", " + currentBuilding + ", " + currentVenue);
//        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        String sourceName = currLocation.name();
//        try {
//            if(sourceAPIData.containsKey(sourceName)) {
//                JSONObject jsonObject = new JSONObject(sourceAPIData.get(sourceName));
//                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
//                Log.e("jsonObject", sourceAPIData.get(sourceName));
//                getUserDetails(jsonObject);
//            }
//        } catch (Exception e){
//
//            }
//        editor.putString("location", currLocation.toString());
//        Toast.makeText(Navigation.this,"Location saved",Toast.LENGTH_SHORT).show();
    }

    public void contactLocation(View view) {

        vibe.vibrate(200);
        String sourceName = currLocation.name();
        try {
            if (sourceAPIData.containsKey(sourceName)) {
                JSONObject jsonObject = new JSONObject(sourceAPIData.get(sourceName));
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                if (propertiesObject.getString("contactNo") != "null") {
                    String telNo = "tel:" + propertiesObject.getString("contactNo").trim();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(telNo));
                    startActivity(callIntent);
                } else {
                    Toast.makeText(Navigation.this, "unable to Contact the location", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Navigation.this, "unable to Contact the location", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }


    }

    public void locationLink(View view) {
        vibe.vibrate(200);
        String sourceName = currLocation.name();
        try {
            if (sourceAPIData.containsKey(sourceName)) {
                JSONObject jsonObject = new JSONObject(sourceAPIData.get(sourceName));
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                if (propertiesObject.getString("url") != "null") {
                    Intent i = new Intent(Navigation.this, WebviewActivity.class);
                    i.putExtra("URL", propertiesObject.getString("url"));
                    startActivity(i);
                } else {
                    Toast.makeText(Navigation.this, "Unable to open link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Navigation.this, "Unable to open link", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }

    public void shareLocationDestination(View view) {
        vibe.vibrate(200);
        String floor = destLocation.getFloor();
        if (floor == null || floor.equals("")) {
            Toast.makeText(Navigation.this, "unable to share your location", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] nearToUser = getNodeNearUser(destLocation.getFloor(), destLocation.getGridX(), destLocation.getGridY(), NEAR_LOCATION_DISTANCE);
        String shareBodyText;
        if (nearToUser != null && !nearToUser[1].equals("undefined")) {
            shareBodyText = "Hi! I am near " + nearToUser[1] + " in " + floor.toUpperCase() + " FLOOR of " + currentBuilding;
        } else {
            shareBodyText = "Hi! I am " + " in " + floor.toUpperCase() + " FLOOR of " + currentBuilding;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareSubText = "share your location";
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
        startActivity(Intent.createChooser(shareIntent, "Share With"));
    }

    public void saveLocationDestination(View view) {
        vibe.vibrate(200);
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("location", destLocation.toString());
        Toast.makeText(Navigation.this, "Location saved", Toast.LENGTH_SHORT).show();
    }

    public void contactLocationDestination(View view) {
        vibe.vibrate(200);
        String destName = destLocation.name();
        try {
            if (sourceAPIData.containsKey(destName)) {
                JSONObject jsonObject = new JSONObject(sourceAPIData.get(destName));
                JSONObject propertiesObject = jsonObject.getJSONObject("properties");
                if (propertiesObject.getString("contactNo") != "null") {
                    String telNo = "tel:" + propertiesObject.getString("contactNo").trim();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(telNo));
                    startActivity(callIntent);
                } else {
                    Toast.makeText(Navigation.this, "unable to Contact the location", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Navigation.this, "unable to Contact the location", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }

    private void saveLocationAPI(JSONObject jsonObject) {

        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        String id = prefs.getString("id", "");
        Log.e("id", " id:  " + id + " , " + token);

        try {
            JSONObject propertiesObject = jsonObject.getJSONObject("properties");
            JSONObject elementObject = jsonObject.getJSONObject("element");
            if (token.length() > 0 && id.length() > 0) {
                String loginURL = getApplicationContext().getResources().getString(R.string.save_location_url);
                HashMap<String, String> params = new HashMap<>();
                params.put("userId", id);
                params.put("venueName", venueName);
                params.put("buildingName", buildingName);
                params.put("floor", jsonObject.getString("floor"));
                params.put("floorElement", elementObject.getString("Rooms"));
                params.put("elementName", jsonObject.getString("name"));
                params.put("coordinateX", String.valueOf(jsonObject.getInt("coordinateX")));
                params.put("coordinateY", String.valueOf(jsonObject.getInt("coordinateY")));
                params.put("type", saveLocationType);
                Log.e("params", " params:  " + params);
                JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("res", "res:  " + response);
                        try {

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error is ", "" + error);
                    }
                }) {
                    //This is for Headers If You Needed
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", token);
                        return params;
                    }
                };
                req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                req.setShouldCache(false);
                req.setTag(this);
                MyRequestQueue.getCache().clear();
                MyRequestQueue.add(req);
            } else {
                errorPopup("Please enter Phone number field properly");
                return;
            }
        } catch (Exception e) {

        }

    }

}
