package com.sanwell.sw_4.model;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.sanwell.sw_4.model.database.SQLDataReader;
import com.sanwell.sw_4.model.interfaces.ResultCompletionHandler;

import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ServerCommunicator {

    public static final String TAG = "ServerCommunicator";
    public static final String SHARED_PREF_VERSION_KEY = "db_version";
    public static final String SHARED_PREF_DATE_KEY = "db_refresh_date";
    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String DATABASE_FILE_NAME = "clients.db";
    private Activity context;
    private String checkedVersion;
    private long dbSize = 1, linksSize = 1;

    public static ServerCommunicator getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public static boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) SanwellApplication
                        .applicationContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static String bytesToMB(long bytes) {
        return String.format("%.2f mb", (double) bytes / (1024.0 * 1024.0));
    }

    public static String getFormatedLastUpdateDate() {
        String pref = "Обновление совершено ";
        String format;
        String sDate = Helpers.read_sp(SHARED_PREF_DATE_KEY, "");
        if (sDate.isEmpty()) {
            return sDate;
        }
        DateFormat f = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date date = null;
        try {
            date = f.parse(sDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        }
        Date today = Calendar.getInstance().getTime();

        int thisYear = Integer.parseInt((String) android.text.format.DateFormat.format("yyyy", today));
        int year = Integer.parseInt((String) android.text.format.DateFormat.format("yyyy", date));
        if (thisYear != year) {
            pref += "в прошлом году: ";
            format = "dd.MM.yyyy";
        } else {
            int thisMonth = Integer.parseInt((String) android.text.format.DateFormat.format("MM", today));
            int month = Integer.parseInt((String) android.text.format.DateFormat.format("MM", date));
            int thisDay = Integer.parseInt((String) android.text.format.DateFormat.format("dd", today));
            int day = Integer.parseInt((String) android.text.format.DateFormat.format("dd", date));
            if (thisDay - 1 == day && thisMonth == month) {
                pref += "вчера в ";
                format = "HH:mm";
            } else if (thisDay == day && thisMonth == month) {
                pref += "сегодня в ";
                format = "HH:mm";
            } else if ((thisDay - day) >= 2 && (thisDay - day) <= 7 && thisMonth == month) {
                String dayOfAWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date);
                pref += Character.toUpperCase(dayOfAWeek.charAt(0)) + dayOfAWeek.substring(1) + " в ";
                format = "HH:mm";
            } else {
                format = "dd.MM";
            }
        }
        return pref + new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public String getRelatedDownloadLength(long size, boolean second_part) {
        if (second_part) {
            size += dbSize;
        }
        return bytesToMB(size);
    }

    public String getTotalDownloadLength() {
        return bytesToMB(dbSize + linksSize);
    }

    public Boolean isEqualToCurrentVersion(String newVersion) {
        checkedVersion = newVersion;
        return !(newVersion == null || newVersion.isEmpty()) && getCurrentDBVersion().equals(newVersion);
    }

    public String getCurrentDBVersion() {
        return Helpers.read_sp(SHARED_PREF_VERSION_KEY, "");
    }

    public void downloadDataBase(final @NonNull ProgressBlock handler) {
        Ion.with(context)
                .load(ServerAPI.SERVER_ADDRESS + "/" +
                        ServerAPI.DATABASE_FILE)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(final long downloaded, long total) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handler.onProgressChanged(
                                        (int) (100 * downloaded / (dbSize + linksSize)),
                                        downloaded
                                );
                            }
                        });
                    }
                })
                .write(SQLDataReader.DbFile())
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(final Exception e, final File result) {
                        if (e != null) {
                            e.printStackTrace();
                            writeIsDbActual("nope");
                            Helpers.edit_sp(SHARED_PREF_VERSION_KEY, "");
                        }
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String r = null;
                                if (result != null) {
                                    r = result.getAbsolutePath();
                                } else {
                                    Log.e(TAG, "result == null");
                                }
                                Log.v(TAG, "receive db " + r);
                                if (e == null) {
                                    writeIsDbActual("yep");
                                    Helpers.edit_sp(SHARED_PREF_VERSION_KEY, checkedVersion);
                                    DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
                                    Helpers.edit_sp(SHARED_PREF_DATE_KEY, format.format(Calendar.getInstance().getTime()));
                                }

                                handler.onFinish(r);
                            }
                        });
                    }
                });
    }

    public void checkDataBaseVersion(final @NonNull ResultCompletionHandler handler) {
        Log.v(TAG, "check database version");
        Ion.with(context)
                .load(ServerAPI.SERVER_ADDRESS + "/" + ServerAPI.VERSION_FILE)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(final Exception e, final String result) {
                        if (e != null) {
                            e.printStackTrace();
                            writeIsDbActual("nope");
                            handler.handle(null);
                            return;
                        }
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(TAG, "database version : " + result);
                                if (result == null) {
                                    writeIsDbActual("yep");
                                    handler.handle(null);
                                    return;
                                }
                                try {
                                    JSONObject obj = new JSONObject(result);
                                    String ver = obj.getString("version");
                                    try {
                                        dbSize = Integer.parseInt(obj.getString("db_length"));
                                        linksSize = Integer.parseInt(obj.getString("data_length"));
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                    if (!isEqualToCurrentVersion(ver)) {
                                        writeIsDbActual("nope");
                                        handler.handle(null);
                                    } else {
                                        writeIsDbActual("yep");
                                        handler.handle(null);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    writeIsDbActual("nope");
                                    handler.handle(null);
                                }
                            }
                        });
                    }
                });
    }

    public void getURLSJSON(final @NonNull ProgressBlock progressBlock) {
        Ion.with(context)
                .load(ServerAPI.SERVER_ADDRESS + "/" + ServerAPI.RESOURCES_FILE)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(final long downloaded, long total) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBlock.onProgressChanged(
                                        (int) (100 * (dbSize + downloaded) / (dbSize + linksSize)),
                                        downloaded
                                );
                            }
                        });
                    }
                })
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, final String result) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBlock.onFinish(result);
                            }
                        });
                    }
                });
    }

    public boolean isDbActual() {
        return !Helpers.read_sp("isDBActual", "nope").equals("nope");
    }

    private void writeIsDbActual(String toWrite) {
        Helpers.edit_sp("isDBActual", toWrite);
    }

    enum ServerAPI {
        SERVER_ADDRESS {
            @Override
            public String toString() {
                return "http://tablets.sanwell.biz/sw-client/api";
            }
        },
        VERSION_FILE {
            @Override
            public String toString() {
                return "check_db";
            }
        },
        DATABASE_FILE {
            @Override
            public String toString() {
                return "trade_pc.db";
            }
        },
        RESOURCES_FILE {
            @Override
            public String toString() {
                return "list_json";
            }
        }

    }

    public interface ProgressBlock {

        void onProgressChanged(int percentage, long downloaded);

        void onFinish(String result);

    }

    public static class SingletonHolder {
        public static final ServerCommunicator HOLDER_INSTANCE = new ServerCommunicator();
    }

}