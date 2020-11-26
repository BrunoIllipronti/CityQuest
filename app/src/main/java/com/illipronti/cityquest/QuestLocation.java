package com.illipronti.cityquest;
import android.content.Context;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class QuestLocation {
    LatLng location;
    String title;
    Icon icon;

    QuestLocation(Context c, double lat, double lon, String Title){
        Context context = c;
        IconFactory iconFactory = IconFactory.getInstance(context);
        location = new LatLng(lat, lon);
        title = Title;
        icon = iconFactory.fromResource(R.drawable.qr_code);
    }
}
