package com.sanwell.sw_4.model;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.sanwell.sw_4.model.database.cores.RCoordinate;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class KMLCollector {

    public static final String NAME = "coords.kml";

    public static void initServices(final Context context) {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                try {
                    collect(location, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(context, locationResult);
    }

    public static File form(Context context) throws JDOMException, IOException {
        String fullPath = context.getExternalFilesDir(null) + "/" + NAME;

        Namespace mainNameSpace = Namespace.getNamespace("http://earth.google.com/kml/2.2");

        Realm realm = Realm.getInstance(context);
        RealmResults<RCoordinate> coordinates = realm.where(RCoordinate.class).findAll();

        StringBuilder stringBuilder = new StringBuilder();

        Element root = new Element("kml", mainNameSpace);
        Element document = new Element("Document", mainNameSpace);

        for (RCoordinate coordinate : coordinates) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(coordinate.getCoordinate());
            Element timePlacemark = new Element("Placemark"); // time
            timePlacemark.addContent(new Element("name").setText(coordinate.getName()));
            Element point = new Element("Point");
            point.addContent(new Element("coordinates").setText(coordinate.getCoordinate()));
            Element timeSpan = new Element("TimeSpan");
            timeSpan.addContent(new Element("begin").setText(coordinate.getTime()));
            timeSpan.addContent(new Element("end").setText(coordinate.getTime()));
            timePlacemark.addContent(timeSpan);
            timePlacemark.addContent(point);
            timePlacemark.addContent(new Element("description").setText(coordinate.getTime()));
            document.addContent(timePlacemark);
        }

        Element lineString = new Element("LineString");
        lineString.addContent(new Element("tessellate").setText("1"));
        lineString.addContent(new Element("coordinates").setText(stringBuilder.toString()));

        Element placemark = new Element("Placemark"); // line
        placemark.addContent(lineString);

        File test = new File(fullPath);
        test.delete();
        Writer fileWriter;

        document.addContent(placemark);
        root.addContent(document);
        Document doc = new Document().setRootElement(root);

        fileWriter = new FileWriter(fullPath);
        XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
        xout.output(doc, fileWriter);
        fileWriter.flush();
        fileWriter.close();

        return test;
    }

    public static void sendEmail(Context context, List<File> attachments) throws JDOMException, IOException {
        Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{Helpers.read_sp(Helpers.DEVICE_USER_EMAIL_KEY, Helpers.DEFAULT_EMAIL)});

        ArrayList<Uri> uriList = new ArrayList<>();
        for (File f : attachments) {
            Uri uri = Uri.fromFile(f);
            uriList.add(uri);
        }
        email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

        email.setType("message/rfc822");
        email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(email);
        clear(context);
    }

    public static void clear(Context context) {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        realm.where(RCoordinate.class).findAll().clear();
        realm.commitTransaction();
    }

    public static void collect(Location location, Context context) throws JDOMException, IOException {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        RCoordinate coordinate = realm.createObject(RCoordinate.class);
        String pos = "" + location.getLongitude() + ", " + location.getLatitude();
        coordinate.setCoordinate(pos);
        coordinate.setLatitude("" + location.getLatitude());
        coordinate.setLongitude("" + location.getLongitude());
        String name = Helpers.read_sp(Helpers.DEVICE_USER_NAME_KEY, "none");
        coordinate.setName(name);
        Date date = new Date(location.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = dateFormat.format(date);
        dateFormat = new SimpleDateFormat("hh:mm:ss");
        String sTime = dateFormat.format(date);
        coordinate.setTime(sDate + "T" + sTime + "Z");
        Log.i(KMLCollector.class.getSimpleName(), "collect " + pos + " " + coordinate.getTime());

        realm.commitTransaction();
    }


}
