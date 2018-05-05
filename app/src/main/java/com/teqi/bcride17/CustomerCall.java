package com.teqi.bcride17;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.gson.JsonArray;
import com.teqi.bcride17.Common.Common;
import com.teqi.bcride17.Model.FCMResponse;
import com.teqi.bcride17.Model.Notification;
import com.teqi.bcride17.Model.Sender;
import com.teqi.bcride17.Model.Token;
import com.teqi.bcride17.Remote.IFCMservice;
import com.teqi.bcride17.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance;
    Button btnCancel, btnAccept;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;

    IFCMservice mFCMService;

    String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        //init view
        txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtTime = (TextView)findViewById(R.id.txtTime);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ep11 05:17
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);

            }
        });

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null)
        {
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);
            //ep11 04:59
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat,lng);
        }

    }

    private void cancelBooking(String customerId) {
        //ep11 06:02
        Token token = new Token(customerId);

        Notification notification = new Notification("Notice", "Driver has cancelled your request");
        Sender sender = new Sender(token.getToken(),notification);

        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1)
                        {
                            Toast.makeText(CustomerCall.this,"Cancelled", Toast.LENGTH_SHORT).show();//ep11 06:48
                            finish();
                        }

                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void getDirection(double lat,double lng) {

        String requestAPI = null;
        try{
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("teqi",requestAPI);
            mService.getPath(requestAPI)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));

                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));

                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);




                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //Ctrl+O(onStop)

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }
    //Ctrl+O(onPause)

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }
    //Ctrl+O(onResume)

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }
}
