package com.kma.oj_app_mobile.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.SystemClock;
import android.view.View;

import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.activities.ContestDetailActivity;
import com.kma.oj_app_mobile.activities.MainActivity;
import com.kma.oj_app_mobile.api.ApiClient;
import com.kma.oj_app_mobile.api.ApiService;
import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.commons.PageData;
import com.kma.oj_app_mobile.models.Contest;

import retrofit2.Call;
import retrofit2.Response;

public class ContestWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_contest);

        // Define intent to open MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        // Execute API call synchronously inside a new short-lived thread (Valid for widgets max 10s strict limit)
        new Thread(() -> {
            try {
                ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
                
                // Ưu tiên tìm kỳ thi Đang Diễn Ra (ONGOING) trước
                Call<ApiResponse<PageData<Contest>>> callOngoing = apiService.getContests("ONGOING");
                Response<ApiResponse<PageData<Contest>>> responseOngoing = callOngoing.execute();
                
                Contest targetContest = null;
                boolean isOngoing = false;

                if (responseOngoing.isSuccessful() && responseOngoing.body() != null && responseOngoing.body().getData() != null) {
                    List<Contest> ongoingList = responseOngoing.body().getData().getContent();
                    if (ongoingList != null && !ongoingList.isEmpty()) {
                        targetContest = ongoingList.get(0);
                        isOngoing = true;
                    }
                }

                // Nếu không có kỳ thi nào đang diễn ra, tìm kỳ thi Sắp Tới (UPCOMING)
                if (targetContest == null) {
                    Call<ApiResponse<PageData<Contest>>> callUpcoming = apiService.getContests("UPCOMING");
                    Response<ApiResponse<PageData<Contest>>> responseUpcoming = callUpcoming.execute();
                    if (responseUpcoming.isSuccessful() && responseUpcoming.body() != null && responseUpcoming.body().getData() != null) {
                        List<Contest> upcomingList = responseUpcoming.body().getData().getContent();
                        if (upcomingList != null && !upcomingList.isEmpty()) {
                            targetContest = upcomingList.get(0);
                        }
                    }
                }

                if (targetContest != null) {
                    views.setTextViewText(R.id.tv_widget_title, targetContest.getTitle());

                    // Đổi đường dẫn khi Click vào Widget -> Trực tiếp trỏ vào Contest Detail
                    Intent detailIntent = new Intent(context, ContestDetailActivity.class);
                    detailIntent.putExtra("CONTEST_KEY", targetContest.getContestKey());
                    android.app.TaskStackBuilder stackBuilder = android.app.TaskStackBuilder.create(context);
                    stackBuilder.addNextIntentWithParentStack(new Intent(context, MainActivity.class));
                    stackBuilder.addNextIntent(detailIntent);
                    PendingIntent detailPendingIntent = stackBuilder.getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.widget_root, detailPendingIntent);
                    
                    String targetDateStr = isOngoing ? targetContest.getEndTime() : targetContest.getStartTime();
                    long targetMillis = 0;
                    try {
                        String cleanTarget = targetDateStr;
                        if (cleanTarget.contains(".")) {
                            cleanTarget = cleanTarget.substring(0, cleanTarget.indexOf("."));
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        Date date = sdf.parse(cleanTarget);
                        if (date != null) {
                            targetMillis = date.getTime();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (targetMillis > 0) {
                        long timeDiff = targetMillis - System.currentTimeMillis();
                        long baseClockTime = SystemClock.elapsedRealtime() + timeDiff;

                        // Hiển thị Chronometer
                        views.setViewVisibility(R.id.chronometer, View.VISIBLE);
                        views.setChronometer(R.id.chronometer, baseClockTime, "%s", true);
                        views.setChronometerCountDown(R.id.chronometer, true);
                        
                        
                        if (isOngoing) {
                            views.setTextViewText(R.id.tv_widget_header, "Kỳ thi đang diễn ra 🔥");
                            views.setTextColor(R.id.tv_widget_time, android.graphics.Color.parseColor("#4caf50"));
                            views.setTextViewText(R.id.tv_widget_time, "Kết thúc sau:");
                        } else {
                            views.setTextViewText(R.id.tv_widget_header, "Kỳ thi tiếp theo 🏆");
                            views.setTextColor(R.id.tv_widget_time, android.graphics.Color.parseColor("#a0a0a0"));
                            views.setTextViewText(R.id.tv_widget_time, "Bắt đầu sau:");
                        }
                    } else {
                        // Backup method if format is flawed
                        views.setViewVisibility(R.id.chronometer, View.GONE);
                        if (isOngoing) {
                            views.setTextViewText(R.id.tv_widget_header, "Kỳ thi đang diễn ra 🔥");
                            views.setTextColor(R.id.tv_widget_time, android.graphics.Color.parseColor("#4caf50"));
                            views.setTextViewText(R.id.tv_widget_time, "🔥 Đang diễn ra!");
                        } else {
                            views.setTextViewText(R.id.tv_widget_header, "Kỳ thi tiếp theo 🏆");
                            String fallbackStr = targetContest.getStartTime() != null ? targetContest.getStartTime().replace("T", " ") : "Bí ẩn";
                            views.setTextColor(R.id.tv_widget_time, android.graphics.Color.parseColor("#a0a0a0"));
                            views.setTextViewText(R.id.tv_widget_time, "Bắt đầu: " + fallbackStr);
                        }
                    }
                } else {
                    views.setTextViewText(R.id.tv_widget_header, "Góc Cày Cuốc 💻");
                    views.setTextViewText(R.id.tv_widget_title, "Chưa có kỳ thi nào");
                    views.setTextViewText(R.id.tv_widget_time, "Hãy nhấp để tiếp tục cày bài!");
                    views.setTextColor(R.id.tv_widget_time, android.graphics.Color.parseColor("#a0a0a0"));
                    views.setViewVisibility(R.id.chronometer, View.GONE);
                }
            } catch (Exception e) {
                views.setTextViewText(R.id.tv_widget_title, "Mất kết nối mạng");
                views.setTextViewText(R.id.tv_widget_time, "Nhấp để mở lại App");
            }
            
            // Post AppWidget update UI transaction to main thread (safer practice via framework)
            new Handler(Looper.getMainLooper()).post(() -> {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            });
        }).start();

        // Push default skeleton rendering immediately
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
