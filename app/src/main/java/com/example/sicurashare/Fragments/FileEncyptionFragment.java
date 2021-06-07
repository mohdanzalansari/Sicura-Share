package com.example.sicurashare.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sicurashare.AES;
import com.example.sicurashare.DatabaseHelper;
import com.example.sicurashare.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.NoSuchPaddingException;

import static android.app.Activity.RESULT_OK;

public class FileEncyptionFragment  extends Fragment {

    Button encryptFileBtn, decryptFileBtn, imageFileBtn, videoFileBtn,audioFileBtn,otherFileBtn,cancelBtn,passwordSet;
    LinearLayout otherBtnLinearLayout,PassLinearLayout;
    EditText passwordBox;

    private final int Pick_image_intent = 1;
    private final int Pick_video_intent=2;
    private final int Pick_audio_intent=3;
    private final int Pick_other_intent=4;

    private String password=null;

    DatabaseHelper mdb;

    Boolean isencrypting=false,isPasswordSet=false;

    private ArrayList<String> mediaURIlist;

    final AES aes = new AES();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_encryption,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        encryptFileBtn=view.findViewById(R.id.encryptFileBtnBox);
        decryptFileBtn=view.findViewById(R.id.decrytFileBtnBox);

        imageFileBtn=view.findViewById(R.id.imagefileBtnBox);
        videoFileBtn=view.findViewById(R.id.videofileBtnBox);
        audioFileBtn=view.findViewById(R.id.audiofileBtnBox);
        otherFileBtn=view.findViewById(R.id.otherfileBtnBox);

        cancelBtn=view.findViewById(R.id.cancelBtnBox);
        passwordSet=view.findViewById(R.id.passSetBtnBox);

        passwordBox=view.findViewById(R.id.passBox);

        otherBtnLinearLayout=view.findViewById(R.id.otherBtnLnLy);
        PassLinearLayout=view.findViewById(R.id.passLnLy);

        mdb= new DatabaseHelper(getContext());

        encryptFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                decryptFileBtn.setEnabled(false);
                if (isPasswordSet && password!=null)
                {
                    otherBtnLinearLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    PassLinearLayout.setVisibility(View.VISIBLE);
                }
                cancelBtn.setVisibility(View.VISIBLE);
                isencrypting=true;


            }
        });

        passwordSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!passwordBox.getText().toString().isEmpty()) {
                    password = passwordBox.getText().toString();
                    isPasswordSet=true;
                    passwordBox.setText("");
                    PassLinearLayout.setVisibility(View.GONE);
                    otherBtnLinearLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Password Set", Toast.LENGTH_SHORT).show();
                } else {
                    passwordBox.setError("Enter Password");
                }

            }
        });

        decryptFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                encryptFileBtn.setEnabled(false);
                if (isPasswordSet && password!=null)
                {
                    otherBtnLinearLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    PassLinearLayout.setVisibility(View.VISIBLE);
                }
                isencrypting=false;
                cancelBtn.setVisibility(View.VISIBLE);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isencrypting)
                {
                    decryptFileBtn.setEnabled(true);

                }
                else
                {
                    encryptFileBtn.setEnabled(true);
                }
                passwordBox.setText("");
                PassLinearLayout.setVisibility(View.GONE);
                otherBtnLinearLayout.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                password=null;
            }
        });

        imageFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelectorActivity();
            }
        });

        videoFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVideoSelectorActivity();
            }
        });

        audioFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAudioSelectorActivity();
            }
        });

        otherFileBtn.setOnClickListener(new View.OnClickListener() {
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
        Log.i("note","file intent");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Pick_other_intent);
    }

    private void openAudioSelectorActivity() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.i("note","audio intent");
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), Pick_audio_intent);
    }

    private void openVideoSelectorActivity() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.i("note","video intent");
        startActivityForResult(Intent.createChooser(intent, "Select Video"), Pick_video_intent);
    }

    private void openImageSelectorActivity() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.i("note","image intent");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Pick_image_intent);


    }

    @Override
    final public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

            Cursor returnCursor = getActivity().getContentResolver().query(Uri.parse(mediaURIlist.get(0)), null, null, null, null);
            assert returnCursor != null;

            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);
            returnCursor.close();

            InputStream is= null;
            FileOutputStream fo=null;
            String x=null;
            File dir =null;

            try {
                is=getActivity().getApplicationContext().getContentResolver().openInputStream(Uri.parse(mediaURIlist.get(0)));

                File filepath = Environment.getExternalStorageDirectory();
                if (isencrypting)
                {
                    dir =new File(filepath.getAbsolutePath()+"/SicuraShare/Encrypt/");
                }
                else
                {
                    dir =new File(filepath.getAbsolutePath()+"/SicuraShare/Decrypt/");
                }


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

                fo=new FileOutputStream(file);


                if (isencrypting)
                {
                    x="encrypted";
                    aes.encryptFile(is,fo,password);

                }
                else
                {
                    x="decrypted";
                    aes.decryptfile(is,fo,password);
                }

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE , dd-MMM-yyyy   hh:mm a");
                String datetime = sdf.format(cal.getTime());

                mdb.insertData(fileName+" has been "+x, datetime);
                Toast.makeText(getContext(),fileName+" has been "+x,Toast.LENGTH_SHORT).show();

                otherBtnLinearLayout.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                if (isencrypting)
                {
                    decryptFileBtn.setEnabled(true);

                }
                else
                {
                    encryptFileBtn.setEnabled(true);
                }

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


}
