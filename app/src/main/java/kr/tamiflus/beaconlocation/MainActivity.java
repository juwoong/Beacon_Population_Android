package kr.tamiflus.beaconlocation;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private BeaconManager manager;
    private TextView currentLocationTextView;
    private TextView beforeLocationTextView;
    private TextView movementTextView;
    private String token;

    private HashMap<String, String> map = new HashMap<String, String>(){{
        put("aaaaaaaa-b644-4520-8f0c-720eaf059935", "국제관 2층 교육원 캠프 본부.");
        put("74278bda-b644-4520-8f0c-720eaf059935", "국제관 1층 국제회의장.");
        put("bbbbbbbb-b644-4520-8f0c-720eaf059935", "국제관 1층 국제회의장 입구.");
        put("cccccccc-b644-4520-8f0c-720eaf059935", "국제관 메인홀 계단.");
        put("dddddddd-b644-4520-8f0c-720eaf059935", "6층 메인홀.");
        put("eeeeeeee-b644-4520-8f0c-720eaf059935", "국제관 1층 메인홀.");
    }};

    private HashMap<String, Double> distance = new HashMap<String, Double>() {{
        put("aaaaaaaa-b644-4520-8f0c-720eaf059935", -1.0);
        put("74278bda-b644-4520-8f0c-720eaf059935", -1.0);
        put("bbbbbbbb-b644-4520-8f0c-720eaf059935", -1.0);
        put("cccccccc-b644-4520-8f0c-720eaf059935", -1.0);
        put("dddddddd-b644-4520-8f0c-720eaf059935", -1.0);
        put("eeeeeeee-b644-4520-8f0c-720eaf059935", -1.0);
    }};

    private Beacon beforeBeacon, currentBeacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        token = getSharedPreferences("beaconSetting", MODE_PRIVATE).getString("token", "value");
        manager = BeaconManager.getInstanceForApplication(this);
        manager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        manager.bind(this);

        currentLocationTextView = (TextView) findViewById(R.id.currentLocation);
        beforeLocationTextView = (TextView) findViewById(R.id.beforeLocation);
        movementTextView = (TextView) findViewById(R.id.movement);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBeaconServiceConnect() {
        manager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.i("Beacon", "Ranging?");
                ArrayList<Beacon> list = new ArrayList<Beacon>(collection);
                Collections.sort(list, new BeaconComparator());

                for (Beacon beacon : list) {
                    if (beacon.getDistance() - distance.get(beacon.getId1().toUuid().toString()) < 0) {
                        logToAlert(map.get(beacon.getId1().toUuid().toString()) + "로 접근 중입니다.");
                    }
                    distance.remove(beacon.getId1().toUuid().toString());
                    distance.put(beacon.getId1().toUuid().toString(), beacon.getDistance());
                }

                if (list.size() > 0) {
                    //Log.i("Beacon", list.get(0).getId1().toUuid().toString());
                    if (currentBeacon != null) {
                        Log.i("Fuck", Boolean.toString(currentBeacon.getId1().toUuid().toString()
                                .equals(list.get(0).getId1().toUuid().toString())));
                        if (list.get(0).getId1().toUuid().toString()
                                .equals(currentBeacon.getId1().toUuid().toString()) == false &&
                                ((list.size() == 1 && currentBeacon.getDistance() > list.get(0).getDistance())
                                        || list.size() > 1)) {
                            beforeBeacon = currentBeacon;
                            currentBeacon = list.get(0);

                            Log.i("Beacon", "CHANGE");

                            try {
                                InformationSender.post(currentBeacon.getId1().toUuid().toString(), token);
                            } catch (InformationSender.ServerErrorException e) {
                                Log.i("Server", e.getMessage());
                                logToToast(e.getMessage());
                            } catch (IOException e) {
                                Log.i("Server", e.getMessage());
                            }
                        }
                    } else {
                        currentBeacon = list.get(0);

                        try {
                            InformationSender.post(currentBeacon.getId1().toUuid().toString(), token);
                        } catch (InformationSender.ServerErrorException e) {
                            Log.i("Server", e.getMessage());
                        } catch (IOException e) {
                            Log.i("Server", e.getMessage());
                        }
                    }
                }
                logToDisplay(currentBeacon, beforeBeacon);

                Thread thread = Thread.currentThread();

                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        });

        try {
            manager.startRangingBeaconsInRegion(new Region("UniqueID", null, null, null));
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logToDisplay(final Beacon current, final Beacon before) {
        runOnUiThread(new Runnable() {
            public void run() {
                currentLocationTextView.setText((current == null) ? "위치확인불가" : map.get(current.getId1().toUuid().toString()));
                beforeLocationTextView.setText((before == null) ? "이동전적없음" : map.get(before.getId1().toUuid().toString()));
            }
        });
    }

    private void logToAlert(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                movementTextView.setText(str);
            }
        });
    }

    private void logToToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
