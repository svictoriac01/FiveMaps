package com.example.fivemaps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;

import android.media.MediaPlayer;

import android.os.Build;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MapsActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private Toolbar toolbar;
    private final int REQUEST_CODE_ASK_PERMISSION = 111;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private String ubicacion, origen, destino;
    private Address address;
    private Marker markerUbicacion;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this::onClick);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        solicitarPermisos();

        geocoder = new Geocoder(MapsActivity.this);

        Bundle bundle = getIntent().getExtras();

        ubicacion = bundle.getString("UBICACION");
        origen = bundle.getString("ORIGEN");
        destino = bundle.getString("DESTINO");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ninguno:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.satelital:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.terreno:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.hibrido:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(true);

        if (ubicacion != null)
            setUbicacion(googleMap);
        else
            setRuta(googleMap);

    }

    private void setRuta(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            List<Address> addresses1 = geocoder.getFromLocationName(origen, 1);
            //addresses.add((Address) geocoder.getFromLocationName(origen, 1));
            List<Address> addresses2 = geocoder.getFromLocationName(destino, 1);

            if (addresses1.size() > 0 && addresses2.size() > 0) {
                Address direccionOrigen = addresses1.get(0);
                Address direccionDestino = addresses2.get(0);

                LatLng coordOrigen = new LatLng(direccionOrigen.getLatitude(), direccionOrigen.getLongitude());
                LatLng coordDestino = new LatLng(direccionDestino.getLatitude(), direccionDestino.getLongitude());

                mMap.addMarker(new MarkerOptions().position(coordOrigen).title(direccionOrigen.getLocality()));
                mMap.addMarker(new MarkerOptions().position(coordDestino).title(direccionDestino.getLocality()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUbicacion(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            List<Address> addresses = geocoder.getFromLocationName(ubicacion, 1);
            if (addresses.size() > 0) {
                address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                guardarUbicacion(address);

                markerUbicacion = googleMap.addMarker(new MarkerOptions()
                        .position(latLng));

                if (address.getLocality() == null)
                    markerUbicacion.setTitle(address.getCountryName());

                else markerUbicacion.setTitle(address.getLocality());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                mMap.setOnMarkerClickListener(this::onMarkerClick);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarUbicacion(Address address) {
        SharedPreferences prefs = getSharedPreferences("guardados", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (address.getLocality() == null) {
            //ubicaciones.add(address.getCountryName());
            editor.putString(address.getLocality(), address.getCountryName());
        } else {
            //ubicaciones.add(address.getLocality());
            editor.putString(address.getLocality(), address.getLocality());
        }
        editor.apply();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(markerUbicacion)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Conoce " + address.getCountryName())
                    .setMessage("Si quieres saber mas sobre esta ubicación has clic en informacion")
                    .setNeutralButton(getString(R.string.information), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            enviarURL();
                        }
                    })
                    .setNegativeButton(getString(R.string.stop), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                                mediaPlayer = null;
                                Toast.makeText(MapsActivity.this, "STOP", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MapsActivity.this, "No hay ningun audio en play", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setCancelable(false)
                    .setPositiveButton(getString(R.string.play), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mediaPlayer == null) {
                                seleccionarAudio();
                                if (mediaPlayer != null) {
                                    mediaPlayer.start();
                                    Toast.makeText(MapsActivity.this, "PLAY", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MapsActivity.this, "No existe sonido de este país", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return false;
    }

    private void seleccionarAudio() {
        switch (address.getCountryCode()) {
            case "ES":
                mediaPlayer = MediaPlayer.create(this, R.raw.spain);
                break;
            case "FR":
                mediaPlayer = mediaPlayer.create(this, R.raw.francia);
                break;
            case "DE":
                mediaPlayer = mediaPlayer.create(this, R.raw.alemania);
                break;
            case "RU":
                mediaPlayer = mediaPlayer.create(this, R.raw.rusia);
                break;
            case "JP":
                mediaPlayer = mediaPlayer.create(this, R.raw.japon);
                break;
            case "CN":
                mediaPlayer = mediaPlayer.create(this, R.raw.china);
                break;
            case "AU":
                mediaPlayer = mediaPlayer.create(this, R.raw.australia);
                break;
            case "NZ":
                mediaPlayer = mediaPlayer.create(this, R.raw.nueva_zelanda);
                break;
            case "ZU":
                mediaPlayer = MediaPlayer.create(this, R.raw.sudafrica);
                break;
            case "EG":
                mediaPlayer = MediaPlayer.create(this, R.raw.egipto);
                break;
            case "US":
                mediaPlayer = MediaPlayer.create(this, R.raw.estados_unidos);
                break;
            case "CA":
                mediaPlayer = MediaPlayer.create(this, R.raw.canada);
                break;
            case "CO":
                mediaPlayer = MediaPlayer.create(this, R.raw.colombia);
                break;
            case "BR":
                mediaPlayer = MediaPlayer.create(this, R.raw.brasil);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    private void enviarURL() {
        String url;
        if (address.getLocality() == null) {
            url = String.format("https://es.wikipedia.org/wiki/%s", address.getCountryName());
        } else {
            url = String.format("https://es.wikipedia.org/wiki/%s", address.getLocality());
        }
        Intent intent = new Intent(MapsActivity.this, WebActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }

    private void solicitarPermisos() {
        int permissionAccessCoarseLocation = ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionAccessFineLocation = ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionAccessCoarseLocation != PackageManager.PERMISSION_GRANTED || permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSION);
            }
        }
    }


}
