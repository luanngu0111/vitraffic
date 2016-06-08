package vn.trans.vitraffic;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.*;
import vn.trans.direction.PlaceAutoCompleteAdapter;

public class PlacesDialog extends DialogFragment {

	private static final int REQUEST_START_PLACE = 1;
	private static final int REQUEST_END_PLACE = 2;
	private static final String TAG = "places";
	private static final int RESULT_OK = 1;
	private static final int RESULT_CANCELED = 0;
	private AutoCompleteTextView mStartPlace;
	private AutoCompleteTextView mEndPlace;
	private Button bnFindPath;
	private Button bnCancel;

	public LatLng start_place;
	public LatLng end_place;

	public String place;
	/**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static PlacesDialog newInstance(int num) {
    	PlacesDialog f = new PlacesDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.dialog_input_places, container, false);
		
		
		mStartPlace = (AutoCompleteTextView) v.findViewById(R.id.txtStartPlace);
		mEndPlace = (AutoCompleteTextView) v.findViewById(R.id.txtEndPlace);
		bnFindPath = (Button) v.findViewById(R.id.bnFindPath);
		bnCancel = (Button) v.findViewById(R.id.bnCancel);

		
		
		bnFindPath.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				place = mStartPlace.getText().toString();
				dismiss();
			}
		});

		bnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		mStartPlace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				openAutocompleteActivity(REQUEST_START_PLACE);
			}
		});
		mEndPlace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				openAutocompleteActivity(REQUEST_END_PLACE);
			}
		});
		return v;

	}


	/*
	 * Dialog chon dia diem tren ban do
	 */
	private void openAutocompleteActivity(int req_code) {
		try {
			// The autocomplete activity requires Google Play Services to be
			// available. The intent
			// builder checks this and throws an exception if it is not the
			// case.
			Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(getActivity());
			getActivity().startActivityForResult(intent, req_code);
		} catch (GooglePlayServicesRepairableException e) {
			// Indicates that Google Play Services is either not installed or
			// not up to date. Prompt
			// the user to correct the issue.
			GoogleApiAvailability.getInstance()
					.getErrorDialog(getActivity(), e.getConnectionStatusCode(), 0 /* requestCode */).show();
		} catch (GooglePlayServicesNotAvailableException e) {
			// Indicates that Google Play Services is not available and the
			// problem is not easily
			// resolvable.
			String message = "Google Play Services is not available: "
					+ GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

			Log.e(TAG, message);
			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		Log.i(TAG, "Place Selected: "+resultCode);
		if (requestCode == REQUEST_START_PLACE) {
			if (resultCode == RESULT_OK) {
				// Get the user's selected place from the Intent.
				Place place = PlaceAutocomplete.getPlace(getActivity(), data);
				Log.i(TAG, "Place Selected: " + place.getName());

				// Format the place's details and display them in the TextView.
				mStartPlace.setText(place.getName());
				Toast.makeText(getActivity(), place.getName(), Toast.LENGTH_LONG).show();
				// Display attributions if required.
				CharSequence attributions = place.getAttributions();
				if (!TextUtils.isEmpty(attributions)) {
					// mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
				} else {
					// mPlaceAttribution.setText("");
				}
			} else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
				Status status = PlaceAutocomplete.getStatus(getActivity(), data);
				Log.e(TAG, "Error: Status = " + status.toString());
			} else if (resultCode == RESULT_CANCELED) {
				// Indicates that the activity closed before a selection was
				// made. For example if
				// the user pressed the back button.
			}
		} else if (requestCode == REQUEST_END_PLACE) {
			if (resultCode == RESULT_OK) {
				// Get the user's selected place from the Intent.
				Place place = PlaceAutocomplete.getPlace(getActivity(), data);
				Log.i(TAG, "Place Selected: " + place.getName());

				// Format the place's details and display them in the TextView.
				mEndPlace.setText(place.getLatLng().toString());

				// Display attributions if required.
				CharSequence attributions = place.getAttributions();
				if (!TextUtils.isEmpty(attributions)) {
					// mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
				} else {
					// mPlaceAttribution.setText("");
				}
			} else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
				Status status = PlaceAutocomplete.getStatus(getActivity(), data);
				Log.e(TAG, "Error: Status = " + status.toString());
			} else if (resultCode == RESULT_CANCELED) {
				// Indicates that the activity closed before a selection was
				// made. For example if
				// the user pressed the back button.
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Helper method to format information about a place nicely.
	 */
	private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id, CharSequence address,
			CharSequence phoneNumber, Uri websiteUri, int resid) {
		Log.e(TAG, res.getString(resid, name, id, address, phoneNumber, websiteUri));
		return Html.fromHtml(res.getString(resid, name, id, address, phoneNumber, websiteUri));

	}

	/*
	 * ==============================================
	 */

}
