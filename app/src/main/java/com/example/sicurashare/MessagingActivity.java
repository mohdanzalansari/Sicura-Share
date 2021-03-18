package com.example.sicurashare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MessagingActivity extends AppCompatActivity {

    static final int MESSAGE_READ = 1;
    static final int ERROR_OCCURED = 2;

    private RecyclerView message_list;
    private MessageListAdapter message_list_adapter;
    private RecyclerView.LayoutManager message_list_layout_manager;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    CheckBox passwordSet;
    Button passwordSetter;
    Button sendButton;
    LinearLayout passwordsetterLinearLayout;
    EditText inputPasswordBox;
    EditText messageBox;

    ArrayList<MessageObject> messageList;
    private static String password = null;

    Boolean isPasswordSet = false;


    final AES aes = new AES();
    DatabaseHelper mdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        String user = getIntent().getStringExtra("user");
        InetAddress address = (InetAddress) getIntent().getSerializableExtra("inetaddress");


        if (user.equals("receiver")) {

            clientClass = new ClientClass(address);
            clientClass.start();

        } else {
            serverClass = new ServerClass();
            serverClass.start();
        }

        passwordSet = findViewById(R.id.passwordcheckBox);
        passwordsetterLinearLayout = findViewById(R.id.psdlinearlayout);
        inputPasswordBox = findViewById(R.id.passwordBox);
        passwordSetter = findViewById(R.id.passwordbtnbox);

        sendButton = findViewById(R.id.sendbuttonnbox);
        messageBox = findViewById(R.id.msgBox);

        mdb = new DatabaseHelper(this);
        messageList = new ArrayList<>();

        initialize_recycler_view();

        passwordSet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    passwordsetterLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    password = null;
                    isPasswordSet = false;
                    passwordsetterLinearLayout.setVisibility(View.GONE);
                    Toast.makeText(MessagingActivity.this, "Password Removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        passwordSetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!inputPasswordBox.getText().toString().isEmpty()) {
                    password = inputPasswordBox.getText().toString();
                    isPasswordSet = true;
                    passwordsetterLinearLayout.setVisibility(View.GONE);
                    inputPasswordBox.setText("");
                    Toast.makeText(MessagingActivity.this, "Password Set", Toast.LENGTH_SHORT).show();
                } else {
                    inputPasswordBox.setError("Enter Password");
                }

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                String datetime = sdf.format(cal.getTime());
                String msg = messageBox.getText().toString();
                if(msg.isEmpty())
                {
                    messageBox.setError("Enter some message to send");
                }
                else
                {
                    MessageObject newMessage = null;
                    if (isPasswordSet) {
                        if ((!password.equals("")) || (password != null)) {
                            msg = aes.encrypt(msg, password);
                            newMessage = new MessageObject(msg, datetime, "send", true);
                            msg = "1" + msg;
                        } else {
                            Toast.makeText(MessagingActivity.this, "Set Password first.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        newMessage = new MessageObject(msg, datetime, "send", false);
                        msg = "0" + msg;
                    }
                    if (newMessage != null) {
                        messageList.add(newMessage);
                        sendReceive.write(msg.getBytes());
                        messageBox.setText("");
                        mdb.insertData("A message is sent.", datetime);
                        message_list_adapter.notifyDataSetChanged();
                        message_list_layout_manager.scrollToPosition(messageList.size() - 1);
                    }
                }


            }

        });
    }

    private void initialize_recycler_view() {
        message_list = findViewById(R.id.mesgRecyclerView);
        message_list.setNestedScrollingEnabled(false);
        message_list.setHasFixedSize(false);
        message_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        message_list.setLayoutManager(message_list_layout_manager);
        message_list_adapter = new MessageListAdapter(messageList, getApplicationContext());
        message_list.setAdapter(message_list_adapter);

        message_list_adapter.setSingletOnItemClickListener(new MessageListAdapter.OnSingleItemClickListener() {
            @Override
            public void onSingleItemClick(int position, String message) {

                if (isPasswordSet) {
                    if (messageList.get(position).getIsprotected()) {
                        String decrypted_msg = null;
                        try {
                            decrypted_msg = aes.decrypt(messageList.get(position).getMsg(), password);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        messageList.get(position).setMsg(decrypted_msg);
                        message_list_adapter.notifyDataSetChanged();
                        messageList.get(position).setIsprotected(false);
                    } else {
                        String encrypted_mesg = aes.encrypt(messageList.get(position).getMsg(), password);
                        messageList.get(position).setMsg(encrypted_mesg);
                        message_list_adapter.notifyDataSetChanged();
                        messageList.get(position).setIsprotected(true);

                    }
                } else {
                    Toast.makeText(MessagingActivity.this, "Set password to encrypt or decrypt the message.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onSingleLongItemClick(int position) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String mesg = messageList.get(position).getMsg();

                ClipData clipData = ClipData.newPlainText("text", mesg);
                manager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "Message Copied to Clipboard", Toast.LENGTH_SHORT).show();
            }
        });

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case MESSAGE_READ:
                    MessageObject received_newMessage = null;

                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);

                    String isprotectedString = tempMsg.substring(0, 1);
                    tempMsg = tempMsg.substring(1);


                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                    String received_datetime = sdf.format(cal.getTime());

                    if (isprotectedString.equals("0")) {
                        received_newMessage = new MessageObject(tempMsg, received_datetime, "received", false);
                    } else {
                        if (isPasswordSet) {

                            try {
                                tempMsg = aes.decrypt(tempMsg, password);
                                received_newMessage = new MessageObject(tempMsg, received_datetime, "received", false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            received_newMessage = new MessageObject(tempMsg, received_datetime, "received", true);
                            Toast.makeText(MessagingActivity.this, "Set password to read the message.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (received_newMessage != null) {
                        messageList.add(received_newMessage);
                        mdb.insertData("A message is received.", received_datetime);
                        message_list_adapter.notifyDataSetChanged();
                        message_list_layout_manager.scrollToPosition(messageList.size() - 1);
                    }
                    break;
                case ERROR_OCCURED:

                    ObjectModal obx = (ObjectModal) msg.obj;
                    String message = obx.getMsg();
                    Toast.makeText(MessagingActivity.this, message, Toast.LENGTH_SHORT).show();
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
                } finally {
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
        MessageObject received_obj = null;
        byte[] buffer = new byte[1024];

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

        @Override
        public void run() {
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
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

        public void writeObject(final MessageObject object) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        objectOutputStream.writeObject(object);
                    } catch (IOException e) {
                        try {
                            socket.close();
                            Log.i("log", String.valueOf(e));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
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
                } finally {
                    ObjectModal objectModal = new ObjectModal("Something went wrong. \nPlease restart the app and try again later.");
                    handler.obtainMessage(ERROR_OCCURED, -1, -1, objectModal).sendToTarget();
                }
            }
        }
    }
}
