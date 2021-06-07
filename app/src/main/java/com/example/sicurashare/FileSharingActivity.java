package com.example.sicurashare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FileSharingActivity extends AppCompatActivity {

    static final int MESSAGE_READ = 1;
    static final int ERROR_OCCURED = 2;

    private ProgressDialog mProgress;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    Button image_send_btn;
    Button video_send_btn;
    Button audio_send_btn;
    Button other_send_btn;
    EditText mssg;
    CheckBox passwordSet;

    DatabaseHelper mdb;

    ListView listView;

    ArrayList<String> dataList;

    private final int Pick_image_intent = 1;
    private final int Pick_video_intent=2;
    private final int Pick_audio_intent=3;
    private final int Pick_other_intent=4;


    private ArrayList<String> mediaURIlist;
    ArrayAdapter arrayAdapter;

    private static final String password = null;

    LinearLayout passwordsetterLinearLayout;
    EditText inputPasswordBox;
    Button passwordSetter;

    final AES aes = new AES();

    WifiBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        String user = getIntent().getStringExtra("user");
        InetAddress address = (InetAddress) getIntent().getSerializableExtra("inetaddress");


        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        image_send_btn = findViewById(R.id.sendbtn);
        video_send_btn=findViewById(R.id.video_button);
        audio_send_btn=findViewById(R.id.audio_button);
        other_send_btn=findViewById(R.id.other_button);


        mProgress = new ProgressDialog(FileSharingActivity.this);
        mProgress.setTitle("Sending...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        mssg = findViewById(R.id.message);
        passwordSet = findViewById(R.id.passwordcheckBox);
        passwordsetterLinearLayout = findViewById(R.id.psdlinearlayout);
        inputPasswordBox = findViewById(R.id.passwordBox);
        passwordSetter = findViewById(R.id.passwordbtnbox);

        listView=findViewById(R.id.listView);

        mdb= new DatabaseHelper(this);

        dataList=new ArrayList<>();


        if (user.equals("receiver")) {

            clientClass = new ClientClass(address);
            clientClass.start();

        } else {
            serverClass = new ServerClass();
            serverClass.start();
        }


        image_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openImageSelectorActivity();

            }
        });

        video_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openVideoSelectorActivity();

            }
        });

        audio_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAudioSelectorActivity();
            }
        });

        other_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOtherSelectorActivity();
            }
        });
    }

    private void openOtherSelectorActivity() {
        Intent intent = new Intent();
        intent.setType("file/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Pick_other_intent);
    }

    private void openAudioSelectorActivity() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), Pick_audio_intent);
    }

    private void openVideoSelectorActivity() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), Pick_video_intent);
    }

    private void openImageSelectorActivity() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Pick_image_intent);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int fileType=0;

        mediaURIlist = new ArrayList<>();
        if (resultCode == RESULT_OK) {
            if (requestCode == Pick_image_intent || requestCode==Pick_video_intent || requestCode==Pick_audio_intent || requestCode==Pick_other_intent) {

                if (data.getClipData() == null) {
                    mediaURIlist.add(data.getData().toString());
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaURIlist.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

            }
        }

        if (mediaURIlist.size() > 0) {



            Cursor returnCursor = this.getContentResolver().query(Uri.parse(mediaURIlist.get(0)), null, null, null, null);
            assert returnCursor != null;
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            int fileSize = Integer.parseInt(returnCursor.getString(sizeIndex));
            String fileName = returnCursor.getString(nameIndex);
            returnCursor.close();


            switch (requestCode)
            {
                case 1:
                    fileType=Pick_image_intent;
                    break;
                case 2:
                    fileType=Pick_video_intent;
                    break;
                case 3:
                    fileType=Pick_audio_intent;
                    break;
                case 4:
                    fileType=Pick_other_intent;
                    break;
            }

            ObjectModal fileData = new ObjectModal(fileName,fileType,fileSize);
            sendReceive.writeObject(fileData);

            InputStream is= null;
            try {
                is=getApplicationContext().getContentResolver().openInputStream(Uri.parse(mediaURIlist.get(0)));

            } catch (FileNotFoundException e) {

                Toast.makeText(FileSharingActivity.this,"Can't send file. \nPlease restart the app and try again later.",Toast.LENGTH_SHORT).show();
                return;
            }

            mProgress.show();

            sendReceive.sendFile(is,null);

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE , dd-MMM-yyyy   hh:mm a");
            String datetime = sdf.format(cal.getTime());

            mdb.insertData(fileName+" has been shared", datetime);
            Toast.makeText(FileSharingActivity.this,fileName+" has been shared",Toast.LENGTH_SHORT).show();
            mProgress.dismiss();
            dataList.add(fileName+" has been shared");
            arrayAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
            listView.setAdapter(arrayAdapter);

        }
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case MESSAGE_READ:

                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE , dd-MMM-yyyy   hh:mm a");
                    String datetime = sdf.format(cal.getTime());

                    ObjectModal ob = (ObjectModal) msg.obj;
                    String fileName = ob.getMsg();


                    mdb.insertData(fileName+" has been shared", datetime);
                    Toast.makeText(FileSharingActivity.this,fileName+" has been received",Toast.LENGTH_SHORT).show();
                    dataList.add(fileName+" has been received");
                    arrayAdapter =new ArrayAdapter<String>(FileSharingActivity.this,android.R.layout.simple_list_item_1,dataList);
                    listView.setAdapter(arrayAdapter);

                    break;

                case ERROR_OCCURED:

                    ObjectModal obx = (ObjectModal) msg.obj;
                    String message = obx.getMsg();
                    Toast.makeText(FileSharingActivity.this,message,Toast.LENGTH_SHORT).show();
                    break;



            }
            return true;
        }
    });

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                finally {
                    ObjectModal objectModal = new ObjectModal("Something went wrong. \nPlease restart the app and try again later.");
                    handler.obtainMessage(ERROR_OCCURED, -1, -1, objectModal).sendToTarget();
                }
            }

        }

    }


    public class SendReceive extends Thread {
        private final Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        ObjectModal obj = null;

        public SendReceive(Socket skt) {
            socket = skt;
            try {

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectInputStream = new ObjectInputStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendFile(final InputStream in, OutputStream out)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    byte[] buf = new byte[1024];
                    int len;


                    try {
                        while ((len = in.read(buf)) != -1) {


                            outputStream.write(buf, 0, len);


                        }
                        in.close();

                    } catch (IOException e) {

                        try {
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        finally {
                            ObjectModal objectModal = new ObjectModal("Can't send file. \nPlease restart the app and try again later.");
                            handler.obtainMessage(ERROR_OCCURED, -1, -1, objectModal).sendToTarget();
                        }
                        Log.i("note","in send file, exception",e);

                    }


                }
            }).start();
        }

        @Override
        public void run() {



            while (socket!=null)
            {
                String fileName=null;
                String folderType=null;
                int fileSize=0;
                int fileType=0;

                try {
                    synchronized (this) {
                        obj = (ObjectModal) objectInputStream.readObject();
                    }

                    if (obj!=null)
                    {
                        fileName =obj.getFilename();
                        fileSize=obj.getFilesize();
                        fileType=obj.getFiletype();
                    }

                    switch (fileType)
                    {
                        case 1:
                            folderType="Image";
                            break;
                        case 2:
                            folderType="Video";
                            break;

                        case 3:
                            folderType="Audio";
                            break;

                        case 4:
                            folderType="Other";
                            break;
                    }




                    File filepath = Environment.getExternalStorageDirectory();
                    File dir =new File(filepath.getAbsolutePath()+"/SicuraShare/"+folderType+"/");
                    dir.mkdirs();
                    File file =new File(dir, fileName);
                    if (file.exists())
                    {
                        int index = fileName.lastIndexOf('.');
                        String extension = fileName.substring(index);
                        String name=fileName.substring(0,index);
                        fileName=name+System.currentTimeMillis()+extension;
                        file =new File(dir,fileName);
                    }
                    file.createNewFile();

                    FileOutputStream fo=new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    int counterBuffer=0;



                    while((len = inputStream.read(buf))!=-1)
                    {
                        counterBuffer=counterBuffer+len;
                        fo.write(buf,0,len);
                        if (counterBuffer>=fileSize)
                        {
                            break;
                        }

                    }
                    fo.close();

                    String filePath= file.getAbsolutePath();

                    obj=null;
                    ObjectModal objectModal = new ObjectModal(fileName);
                    handler.obtainMessage(MESSAGE_READ, -1, -1, objectModal).sendToTarget();


                }
                catch (Exception e)
                {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        ObjectModal objectModal = new ObjectModal("Can't receive file. \nPlease restart the app and try again later.");
                        handler.obtainMessage(ERROR_OCCURED, -1, -1, objectModal).sendToTarget();
                    }

                }
            }



        }

        public void write(final byte[] bytes) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        outputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        }

        public void writeObject(final ObjectModal object) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        objectOutputStream.writeObject(object);
                    } catch (IOException e) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        finally {
                            ObjectModal objectModal = new ObjectModal("Can't send file. \nPlease restart the app and try again later.");
                            handler.obtainMessage(ERROR_OCCURED, -1, -1, objectModal).sendToTarget();
                        }

                    }
                }
            }).start();

        }




    }

    public class ClientClass extends Thread {
        Socket socket;
        String hostadd;

        public ClientClass(InetAddress hostAddress) {
            hostadd = hostAddress.getHostAddress();
            socket = new Socket();

        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostadd, 8888), 0);
                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {

                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                finally {
                    ObjectModal objectModal = new ObjectModal("Something went wrong. \nPlease restart the app and try again later.");
                    handler.obtainMessage(ERROR_OCCURED, -1, -1, objectModal).sendToTarget();
                }
            }
        }
    }
}
