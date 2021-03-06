package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.proxy.mission.item.adapters.CamerasAdapter;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.CameraInfo;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.model.Drone;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class MissionSurveyFragment extends MissionDetailFragment implements OnClickListener,
		CardWheelHorizontalView.OnCardWheelChangedListener,
		SpinnerSelfSelect.OnSpinnerItemSelectedListener, DroneInterfaces.OnDroneListener {

	private static final String TAG = MissionSurveyFragment.class.getSimpleName();

	private CardWheelHorizontalView mOverlapPicker;
	private CardWheelHorizontalView mAnglePicker;
	private CardWheelHorizontalView mAltitudePicker;
	private CardWheelHorizontalView mSidelapPicker;

	public TextView distanceBetweenLinesTextView;
	public TextView areaTextView;
	public TextView distanceTextView;
	public TextView footprintTextView;
	public TextView groundResolutionTextView;
	public SpinnerSelfSelect cameraSpinner;
	public CheckBox innerWPsCheckbox;
	public TextView numberOfPicturesView;
	public TextView numberOfStripsView;
	public TextView lengthView;
	public CheckBox footprintCheckBox;
	private CamerasAdapter cameraAdapter;

	private List<Survey> surveyList;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_survey;
	}

	@Override
	public void onStart() {
		super.onStart();
		((DroidPlannerApp) getActivity().getApplication()).getDrone().addDroneListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		((DroidPlannerApp) getActivity().getApplication()).getDrone().removeDroneListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Context context = getActivity().getApplicationContext();

        cameraAdapter = new CamerasAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);

		this.surveyList = ((List<Survey>) getMissionItems());

		cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);

		footprintCheckBox = (CheckBox) view.findViewById(id.CheckBoxFootprints);

		mAnglePicker = (CardWheelHorizontalView) view.findViewById(id.anglePicker);
		mAnglePicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered,
				0, 180, "%dº"));

		mOverlapPicker = (CardWheelHorizontalView) view.findViewById(id.overlapPicker);
		mOverlapPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 99, "%d %%"));

		mSidelapPicker = (CardWheelHorizontalView) view.findViewById(R.id.sidelapPicker);
		mSidelapPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 99, "%d %%"));

		mAltitudePicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudePicker);
		mAltitudePicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 5, 200, "%d m"));

		innerWPsCheckbox = (CheckBox) view.findViewById(id.checkBoxInnerWPs);

		areaTextView = (TextView) view.findViewById(id.areaTextView);
		distanceBetweenLinesTextView = (TextView) view
				.findViewById(id.distanceBetweenLinesTextView);
		footprintTextView = (TextView) view.findViewById(id.footprintTextView);
		groundResolutionTextView = (TextView) view.findViewById(id.groundResolutionTextView);
		distanceTextView = (TextView) view.findViewById(id.distanceTextView);
		numberOfPicturesView = (TextView) view.findViewById(id.numberOfPicturesTextView);
		numberOfStripsView = (TextView) view.findViewById(id.numberOfStripsTextView);
		lengthView = (TextView) view.findViewById(id.lengthTextView);

		footprintCheckBox.setOnClickListener(this);
		innerWPsCheckbox.setOnClickListener(this);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);

        mAnglePicker.addChangingListener(this);
        mOverlapPicker.addChangingListener(this);
        mSidelapPicker.addChangingListener(this);
        mAltitudePicker.addChangingListener(this);

		updateViews();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY));
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position) {
        final CameraInfo cameraInfo = cameraAdapter.getCamera(position);
        for(Survey survey: surveyList) {
            survey.setCameraInfo(cameraInfo);
        }

		onChanged(mAnglePicker, 0, 0);
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.anglePicker:
		case R.id.altitudePicker:
		case R.id.overlapPicker:
		case R.id.sidelapPicker:
			try {
				for (Survey survey : surveyList) {
					survey.update(mAnglePicker.getCurrentValue(),
							new Altitude(mAltitudePicker.getCurrentValue()),
							mOverlapPicker.getCurrentValue(), mSidelapPicker.getCurrentValue());

					survey.build();
				}
                mAltitudePicker.setBackgroundResource(R.drawable.bg_cell_white);
			} catch (Exception e) {
				Log.e(TAG, "Error while building the survey.", e);
				mAltitudePicker.setBackgroundColor(Color.RED);
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_UPDATE:
			updateViews();
			break;
		default:
			break;
		}

	}

	private void updateViews() {
		updateTextViews();
		updateSeekBars();
		updateCameraSpinner();
	}

	private void updateCameraSpinner() {
		cameraAdapter.setTitle(surveyList.get(0).surveyData.getCameraName());
	}

	private void updateSeekBars() {
		mAnglePicker.setCurrentValue(surveyList.get(0).surveyData.getAngle().intValue());
		mOverlapPicker.setCurrentValue((int) surveyList.get(0).surveyData.getOverlap());
		mSidelapPicker.setCurrentValue((int) surveyList.get(0).surveyData.getSidelap());
		mAltitudePicker.setCurrentValue((int) surveyList.get(0).surveyData.getAltitude().valueInMeters());
	}

	private void updateTextViews() {
		Context context = getActivity();
		try {
			footprintTextView.setText(context.getString(R.string.footprint) + ": "
					+ surveyList.get(0).surveyData.getLateralFootPrint() + " x"
					+ surveyList.get(0).surveyData.getLongitudinalFootPrint());
			groundResolutionTextView.setText(context.getString(R.string.ground_resolution) + ": "
                    + surveyList.get(0).surveyData.getGroundResolution() + "/px");
			distanceTextView.setText(context.getString(R.string.distance_between_pictures) + ": "
					+ surveyList.get(0).surveyData.getLongitudinalPictureDistance());
			distanceBetweenLinesTextView.setText(context.getString(R.string.distance_between_lines)
					+ ": " + surveyList.get(0).surveyData.getLateralPictureDistance());
			areaTextView
					.setText(context.getString(R.string.area) + ": " + surveyList.get(0).polygon.getArea());
			lengthView.setText(context.getString(R.string.mission_length) + ": "
                    + surveyList.get(0).grid.getLength());
			numberOfPicturesView.setText(context.getString(R.string.pictures) + ": "
					+ surveyList.get(0).grid.getCameraCount());
			numberOfStripsView.setText(context.getString(R.string.number_of_strips) + ": "
                    + surveyList.get(0).grid.getNumberOfLines());
		} catch (Exception e) {
			footprintTextView.setText(context.getString(R.string.footprint) + ": " + "???");
			groundResolutionTextView.setText(context.getString(R.string.ground_resolution) + ": "
					+ "???");
			distanceTextView.setText(context.getString(R.string.distance_between_pictures) + ": "
					+ "???");
			distanceBetweenLinesTextView.setText(context.getString(R.string.distance_between_lines)
					+ ": " + "???");
			areaTextView.setText(context.getString(R.string.area) + ": " + "???");
			lengthView.setText(context.getString(R.string.mission_length) + ": " + "???");
			numberOfPicturesView.setText(context.getString(R.string.pictures) + "???");
			numberOfStripsView.setText(context.getString(R.string.number_of_strips) + "???");
		}
	}

}
