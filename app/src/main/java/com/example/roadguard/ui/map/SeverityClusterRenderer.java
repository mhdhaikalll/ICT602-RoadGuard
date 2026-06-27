package com.example.roadguard.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.roadguard.R;
import com.example.roadguard.data.local.entity.CachedReport;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class SeverityClusterRenderer extends DefaultClusterRenderer<CachedReport> {
    private final Context context;

    public SeverityClusterRenderer(Context context, GoogleMap map, ClusterManager<CachedReport> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(CachedReport item, MarkerOptions markerOptions) {
        int colorRes;
        switch (item.severity) {
            case "high":   colorRes = R.color.severity_high; break;
            case "medium": colorRes = R.color.severity_medium; break;
            default:       colorRes = R.color.severity_low;
        }
        markerOptions.icon(bitmapDescriptorFromVector(colorRes));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<CachedReport> cluster, MarkerOptions markerOptions) {
        // Optional: custom cluster icon (can leave default)
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int colorRes) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_hazard_marker);
        vectorDrawable.setTint(context.getResources().getColor(colorRes));
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
