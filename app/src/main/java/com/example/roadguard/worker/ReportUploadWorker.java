package com.example.roadguard.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.roadguard.data.local.RoadGuardDatabase;
import com.example.roadguard.data.local.entity.PendingReport;
import com.example.roadguard.data.remote.FirestoreDataSource;
import com.example.roadguard.util.NetworkUtil;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

// ni yang nak support connectivity tu
public class ReportUploadWorker extends Worker {
    public ReportUploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!NetworkUtil.isNetworkAvailable(getApplicationContext())) {
            return Result.retry();
        }

        RoadGuardDatabase db = RoadGuardDatabase.getInstance(getApplicationContext());
        List<PendingReport> pending = db.pendingReportDao().getPendingReportsSync();
        FirestoreDataSource firestore = new FirestoreDataSource();

        for (PendingReport pr : pending) {
            try {
                // Synchronous call to Firestore
                Tasks.await(firestore.addReport(pr.userId, pr.latitude, pr.longitude,
                        pr.geohash, pr.severity, pr.notes));
                db.pendingReportDao().markUploaded(pr.id);
            } catch (Exception e) {
                return Result.retry();
            }
        }
        return Result.success();
    }
}
