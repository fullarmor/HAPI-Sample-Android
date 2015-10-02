package com.fullarmor.hapi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.JsonWriter;
import android.util.Log;

import com.fullarmor.hapi.models.BaseFileRequest;
import com.fullarmor.hapi.models.DomainLoginResponse;
import com.fullarmor.hapi.models.FileSystemEntry;
import com.fullarmor.hapi.models.FolderEnumRequest;
import com.fullarmor.hapi.models.FolderEnumResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
/*****************************************************************
 <copyright file="HAPIHelper.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

public class HAPIHelper {
    // URL for gatekeeper to connect to
    public String gatekeeperBaseUrl = "";
    // HAPI token
    public String mToken = "";
    // last error
    public String mLastError;
    // application context
    public Context mContext;
    private final String HAPIHELPER_TAG = "HAPIHelper";
    // constructor
    public HAPIHelper(Context ctx,String token){
        mContext = ctx;
        mToken = token;
        Log.v(HAPIHELPER_TAG,"Constructor.  Token=" + token);
    }
    // helper function to get the specified string resource
    private String getString(int resource) {
        if ( mContext != null )
            return mContext.getString(resource);
        else
            return "<string unavailable. Context was not initialized.>";
    }
    // Performs a HAPI login
    public DomainLoginResponse Login(String username, String password)throws IOException, InvalidParameterException {
        InputStream is = null;
        HttpURLConnection conn = null;
        Log.v(HAPIHELPER_TAG,"Starting Login - username=" + username);
        // check parameters
        if ( username == null || username.isEmpty() )
            throw new InvalidParameterException(getString(R.string.error_username_required));
        if ( password == null || password.isEmpty() )
            throw new InvalidParameterException(getString(R.string.error_password_required));
        DomainLoginResponse result = new DomainLoginResponse();
        int response = 0;
        result.Success= false;
        try {
            // TODO: this is temporary code to allow testing with a self-signed cert
            trustEveryone();
            String myurl = gatekeeperBaseUrl + "/route/hapi/login";
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000 /* milliseconds */);
            conn.setConnectTimeout(45000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream body = conn.getOutputStream();
            JsonWriter w = new JsonWriter(new OutputStreamWriter(body,"UTF-8"));
            w.beginObject();
            w.name("UserName");
            w.value(username);
            w.name("Password");
            w.value(password);
            w.endObject();
            w.close();
            // Starts the query
            conn.connect();
            response = conn.getResponseCode();
            is = conn.getInputStream();

            // parse the response from the server
            if ( response == 200 ) {

                Reader reader = new InputStreamReader(is, "UTF-8");

                GsonBuilder builder = new GsonBuilder();
                Gson json = builder.create();
                result = json.fromJson(reader, DomainLoginResponse.class);
                Log.i(HAPIHELPER_TAG,"Login response received from server.");
                return result;
            }
            else {
                result.Success = false;
                result.Error = getString(R.string.error_generic_login_failed) + String.valueOf(response);
                Log.e(HAPIHELPER_TAG,result.Error);
            }

        }
        catch ( Exception ex) {
            Log.e(HAPIHELPER_TAG,"Login threw an exception.",ex);
            if ( ex.getClass().equals(IOException.class) )
                throw ex;

        }

        finally {
            if (is != null) {
                is.close();
            }
            if ( conn != null )
                conn.disconnect();
        }
        return result;
    }
    // Enumerate the files and folders
    public FolderEnumResponse GetFilesAndFolders(FolderEnumRequest request)throws IOException, InvalidParameterException {
        InputStream is = null;
        HttpURLConnection conn = null;
        Log.v("HAPIHelper","GetFilesAndFolders Starting");
        if ( request == null ) {
            throw new InvalidParameterException(getString(R.string.error_null_request));
        }
        try {
            // TODO: this is temporary code to allow testing with a self-signed cert
            trustEveryone();
            String providerName = "Shares";
            if (request.FileIdentifier != null && !request.FileIdentifier.isEmpty())
                providerName = "Share";
            String myurl = gatekeeperBaseUrl + "/route/hapi/" + providerName + "/GetFilesAndFolders";
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000 /* milliseconds */);
            conn.setConnectTimeout(45000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HAPIToken", this.mToken);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream body = conn.getOutputStream();
            Gson gBody = new Gson();
            String jsonString = gBody.toJson(request);
            OutputStreamWriter bodyWriter = new OutputStreamWriter(body);
            bodyWriter.write(jsonString);
            bodyWriter.close();
            conn.connect();
            int response = conn.getResponseCode();

            // unauthorized
            if (response == 401) {
                Log.e(HAPIHELPER_TAG,"GetFilesAndFolders::401 error - redirecting to login");
                DoLogon();
                return null;
            }

            // parse the response from the server
            if (response == 200) {

                is = conn.getInputStream();
                Reader reader = new InputStreamReader(is, "UTF-8");

                GsonBuilder builder = new GsonBuilder();
                Gson json = builder.create();
                FolderEnumResponse result = json.fromJson(reader, FolderEnumResponse.class);
                if ( result != null && result.Items != null )
                    Log.d(HAPIHELPER_TAG,"GetFilesAndFolders returned " + String.valueOf(result.Items.length));
                return result;
            }
            else {
                Log.e("GetFilesAndFolders","response = " + String.valueOf(response));

            }


        }finally {
            if (is != null) {
                is.close();
            }
            if ( conn != null )
                conn.disconnect();
        }

        FolderEnumResponse defaultResponse = new FolderEnumResponse();
        defaultResponse.Items = new FileSystemEntry[] { };
        Log.e(HAPIHELPER_TAG,"GetFilesAndFolders - returning default response. ");
        return defaultResponse;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.v("HAPIHelper","External storage is available.");
            return true;
        }
        Log.e("HAPIHelper","External storage is not available. State =" + state);
        return false;
    }
    public Uri DownloadFile(String fileIdentifier,Context context)throws IOException, InvalidParameterException {
        InputStream is = null;
        HttpURLConnection conn = null;
        Log.v("HAPIHelper","Downloading file " + fileIdentifier);
        if ( fileIdentifier==null || fileIdentifier.isEmpty() )
            throw new InvalidParameterException(getString(R.string.error_fileidentifier_required));
        try {
            // TODO: this is temporary code to allow testing with a self-signed cert
            trustEveryone();
            String providerName = "Share";
            String myurl = gatekeeperBaseUrl + "/route/hapi/" + providerName + "/Download";
            myurl += "?fileIdentifier=" + URLEncoder.encode(fileIdentifier);
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(45000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HAPIToken", this.mToken);
            conn.setDoInput(true);


            // get the file name from the fileIdentifier
            String fileName = fileIdentifier;
            int ndx = fileIdentifier.lastIndexOf('\\');
            if ( ndx > 0 ) {
                fileName = fileIdentifier.substring(ndx+1);
            }
            int response = conn.getResponseCode();


            if (response == 401) {
                Log.e(HAPIHELPER_TAG,"DownloadFile::401 error - redirecting to login");
                // TODO: set context so we can retry the operation after logging in again?
                DoLogon();
                return null;
            }
            // parse the response from the server
            if ( response == 200 ) {
                is = new BufferedInputStream(conn.getInputStream());

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = conn.getContentLength();
                // download the file
                boolean bOK = isExternalStorageWritable();
                File targetFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File targetFile = new File(targetFolder,fileName);


                FileOutputStream os = new FileOutputStream(targetFile);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = is.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        is.close();
                        return null;
                    }
                    total += count;

                    if (fileLength > 0) { // only if total length is known
                        os.write(data, 0, count);
                    }
                }
                os.flush();
                os.close();
                os = null;
                return Uri.fromFile(targetFile);
            }
            else {
                // TODO: log the error
            }
        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        }
        catch (Exception ex ){
            Log.e(HAPIHELPER_TAG,"DownloadFile threw an exception.",ex);
            throw ex;
        }
        finally {
            if (is != null) {
                is.close();
            }
            if ( conn != null )
                conn.disconnect();
        }

        return Uri.EMPTY;
    }

    public Boolean UploadFile(String hapiToken, Uri sourceFile, String targetFolder, String targetFileName) throws IOException, ExecutionException {

        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            Log.d(HAPIHELPER_TAG,"UploadFile starting.");
            // check parameters
            if ( sourceFile == null )
                throw new InvalidParameterException(getString(R.string.error_field_required));
            // TODO: this is temporary code to allow testing with a self-signed cert
            trustEveryone();
            String myurl = gatekeeperBaseUrl + "/route/hapi/Share/UploadFile";
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000 /* milliseconds */);
            conn.setConnectTimeout(45000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestProperty("HAPIToken", hapiToken);
            conn.setDoInput(true);

            File file = new File(sourceFile.getPath());
            int offset = 0;
            int chunkNum = 0;
            int chunkSize = 1*1024*1024;  // 1MB
            int currentChunkBytes = 0;
            long numBytes = file.length();
            double totalChunks = Math.ceil(numBytes/chunkSize);
            while ( offset < numBytes ) {
                MultipartRequest request = new MultipartRequest(conn);
                request.addFormField("fileidentifier", targetFolder);
                request.addFormField("chunk", String.valueOf(chunkNum));
                request.addFormField("chunkStart", String.valueOf(offset));
                request.addFormField("lastChunk", String.valueOf((chunkNum >= totalChunks) ? true : false));
                currentChunkBytes = request.addFilePartChunk("file", file,offset,chunkSize);
                request.finish();
                conn.connect();
                int response = conn.getResponseCode();
                if (response == 200) {
                    // the upload succeeded
                    Log.d(HAPIHELPER_TAG,"Uploaded " + String.valueOf(currentChunkBytes) + "bytes in chunk " + String.valueOf(chunkNum));
                    chunkNum++;
                    offset += currentChunkBytes;
                    Log.d(HAPIHELPER_TAG, String.valueOf(offset) + "bytes of " + String.valueOf(numBytes) + " total have been uploaded." );

                }
                else if ( response == 401 ) {
                    // redirect to logon
                    Log.e(HAPIHELPER_TAG, "UploadFile::401 error - redirecting to login");
                    DoLogon();
                    return false;
                }
                else {
                    Log.e(HAPIHELPER_TAG,"UploadFile call failed. Response = " + String.valueOf(response));
                    break;
                }
            }

        }
        catch ( Exception ex) {
            String text = ex.getMessage() + ex.getStackTrace();
            mLastError = text;
            Log.e(HAPIHELPER_TAG,"Upload file threw an exception: ",ex);
            throw ex;
        }
        finally {
        if (is != null) {
            is.close();
        }
        if ( conn != null )
            conn.disconnect();
    }
        return true;
    }
    public void DoLogon() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        // temp debug code
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
    public Boolean CheckToken(String hapiToken) {
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            // TODO: this is temporary code to allow testing with a self-signed cert
            trustEveryone();
            String myurl = gatekeeperBaseUrl + "/route/hapi/login";
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000 /* milliseconds */);
            conn.setConnectTimeout(45000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HAPIToken", hapiToken);
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();


            // parse the response from the server
            if ( response == 200 ) {

                String sid = conn.getRequestProperty("X-HAPI-UserSID");

                if( sid != null && !sid.isEmpty() )
                    return true;
            }

        }
        catch ( Exception ex ) {
            String message = ex.getMessage();
        }
        finally{
           if ( conn != null )
                conn.disconnect();
        }
        return false;
    }
    public void Logout() {
        HttpURLConnection conn = null;
        try {
            // TODO: this is temporary code to allow testing with a self-signed cert
            trustEveryone();
            String myurl = gatekeeperBaseUrl + "/route/hapi/login";
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000 /* milliseconds */);
            conn.setConnectTimeout(45000 /* milliseconds */);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            // Log.d(DEBUG_TAG, "The response is: " + response);
            // parse the response from the server
        } catch (Exception ex){
            // TODO: logging, error handling
        }

        if ( conn != null )
            conn.disconnect();
    }

    private Boolean isCancelled() {
        return false;
    }
    // TODO: this is temp code to allow testing with self-signed certs
    // it must be removed prior to release
    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
