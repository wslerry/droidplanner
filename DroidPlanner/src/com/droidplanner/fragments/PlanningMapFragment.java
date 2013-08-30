package com.droidplanner.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.helpers.DroneMap;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("UseSparseArrays")
public class PlanningMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerDragListener, OnMapClickListener {

	public Polygon polygon;

	private Polyline polygonLine;

	public OnMapInteractionListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMap.setOnMarkerDragListener(this);
		mMap.setOnMapClickListener(this);
		mMap.setOnMapLongClickListener(this);
		 
		return view;
	}

	public void update(Drone drone, Polygon polygon) {
		markers.clear();

		markers.updateMarker(drone.mission.getHome(), true);
		markers.updateMarkers(drone.mission.getWaypoints(), true);
		markers.updateMarkers(polygon.getPolygonPoints(), true);

		updatePolygonPath(polygon);
		missionPath.updateMissionPath(drone.mission);

	}

	private void updatePolygonPath(Polygon polygon) {
		if (polygonLine != null) {
			polygonLine.remove();
		}
		PolylineOptions polypath = new PolylineOptions();
		polypath.color(Color.BLACK).width(2);
		
		for (LatLng point : polygon.getLatLngList()) {
			polypath.add(point);
		}
		if (polygon.getLatLngList().size() > 2) {
			polypath.add(polygon.getLatLngList().get(0));
		}		
		PolylineOptions polygonPath = polypath;
		polygonLine = mMap.addPolyline(polygonPath);
	}


	@Override
	public void onMapLongClick(LatLng point) {
		mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForHomeMarker(source, marker);
		checkForWaypointMarker(source, marker);
		checkForPolygonMarker(source, marker);
	}

	private void checkForHomeMarker(MarkerSource source, Marker marker) {
		if (Home.class.isInstance(source)) {
			mListener.onMoveHome(marker.getPosition());
		}
	}

	private void checkForWaypointMarker(MarkerSource source, Marker marker) {
		if (waypoint.class.isInstance(source)) {
			mListener.onMoveWaypoint((waypoint) source, marker.getPosition());
		}
	}

	private void checkForPolygonMarker(MarkerSource source, Marker marker) {
		if (PolygonPoint.class.isInstance(source)) {
			mListener.onMovePolygonPoint((PolygonPoint) source,
					marker.getPosition());
		}
	}

	@Override
	public void onMapClick(LatLng point) {
		mListener.onMapClick(point);		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnMapInteractionListener) activity;
	}


}
