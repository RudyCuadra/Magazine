package com.example.magazine;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.example.magazine.BuildConfig;
import java.net.URLConnection;

public class ViewerPDFActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final String TAG = ViewerPDFActivity.class.getSimpleName();
    TextView txtView;
    ProgressBar progressBar;

    Integer pageNumber = 0;
    PDFView pdfV;
    String pdfFileName;
    String cadena;

    private static final int REQUEST = 112;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("","PERMISO ADMITIDO");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("","PERMISO DENEGADO :C");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_pdf);
        txtView = findViewById(R.id.txtViewer);
        //pdfV = findViewById(R.id.pdfView);

            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e("","PERMISO HABIA SIDO DENEGADO");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e("","SOLICITANDO EL PERMISO");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                Log.e("","SOLICITANDOLO ENSERIO");
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else{
                Log.e("","EL PERMISO NO ES EL PROBLEMA");
            }



        /*Common common;
        common = new Common();
        String url = Objects.requireNonNull(common.getSelect_magazine()).getPdf();*/
        /*if (ActivityCompat.checkSelfPermission(ViewerPDFActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ViewerPDFActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ViewerPDFActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            downloadFile();
        }*/


        Bundle bundle = getIntent().getExtras();
        //pdfV.setBackgroundColor(Color.LTGRAY);
        if(bundle.getString("pdf")!= null)
        {
            cadena = bundle.getString("pdf");
            txtView.setText(cadena.toString());
            //displayFromUri(Uri.parse(cadena));
            //setTitle(pdfFileName);
            downloadPdf();
        }

    }

    public void downloadPdf() {
        Log.e("","ENTRO AL DOWNLOADPDF");
        new DownloadFile().execute(cadena,"REVISTAJUNIO.pdf");
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {


            String fileUrl = strings[0];
            String fileName = strings[1];
            String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File folder = new File(extStorageDirectory, "pdf");
            folder.mkdir();
            Log.e("ESTO ES EL FOLDER: ",""+folder.toString());
            File pdfFile = new File(folder, fileName);
            Log.e("ESTO ES EL PDFFile: ",""+pdfFile.toString());
            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
                Log.e("NO FUNCIONO","NO CREA DIRECTORIO");
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);

            openPDF();

            return null;
        }
    }

    public void openPDF(){


        try{
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getPath()+"/pdf/");
            Log.e("FILE: ",""+file.toString());
            //PackageManager packageManager = getActivity().getPackageManager();
            Intent testIntent = new Intent(Intent.ACTION_VIEW);
            // set flag to give temporary permission to external app to use your FileProvider
            testIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e("NO FUNCIONO",""+BuildConfig.APPLICATION_ID.toString());
            // generate URI, I defined authority as the application ID in the Manifest, the last param is file I want to open
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID , file);
            // I am opening a PDF file so I give it a valid MIME type
            testIntent.setDataAndType(uri, "application/pdf");

            // validate that the device can open your File!
            PackageManager pm = this.getPackageManager();
            if (testIntent.resolveActivity(pm) != null) {
                startActivity(testIntent);
            }
            //nDialog.dismiss();
        }catch(ActivityNotFoundException e){
            //nDialog.dismiss();
            Toast.makeText(this, "PDF no disponible", Toast.LENGTH_SHORT).show();
        }


    }

}
