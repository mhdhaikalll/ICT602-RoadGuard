package com.example.roadguard.worker;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.roadguard.data.repository.ReportRepository;
import com.example.roadguard.util.NetworkUtil;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Sync only if network is available (constraint already ensures it)
        if (!NetworkUtil.isNetworkAvailable(getApplicationContext())) {
            return Result.retry();
        }
        ReportRepository repo = new ReportRepository((Application) getApplicationContext());
        repo.syncReportsFromServer();
        return Result.success();
    }
}
