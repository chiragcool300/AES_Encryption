package com.example.encryptionchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    public EditText editText;
    public ListView listView;
    public TextView textView;
    private DatabaseReference databaseReference;

    private String stringMessage;
    private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, -88, 17, 20, 3, -105, 119, -53 };
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;




        protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);


        databaseReference = FirebaseDatabase.getInstance().getReference("message");

        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String stringMessage = snapshot.getValue().toString();
                stringMessage = stringMessage.substring(1, stringMessage.length() - 1);
                String[] stringMessageArray = stringMessage.split(",");
                String[] stringFinal = new String[stringMessageArray.length * 2];
                try {
                    for (int i = 0; i < stringMessageArray.length; i++) {
                        String[] stringKeyValue = stringMessageArray[i].split(",", 2);
                        stringFinal[2 * i] = (String) android.text.format.DateFormat.format("dd-MM-YYYY hh:mm:ss", Long.parseLong(stringKeyValue[0]));

                        stringFinal[2 * i + 1] = AESDecryptionMethod(stringKeyValue[1]);

                    }
                    listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1 , stringFinal));
                    textView.setText(stringFinal[0] + "   " + stringFinal[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void sendButton(View view)
    {
        Date date = new Date();
        try {

            databaseReference.child(Long.toString(date.getTime())).setValue(AESEncryptionMethod(editText.getText().toString()));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        editText.setText("");
    }
    private String AESEncryptionMethod(String string) throws UnsupportedEncodingException {
       byte[] stringByte = string.getBytes();
       byte[] encryptedByte = new byte[stringByte.length];
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

            String result = new String(encryptedByte, "ISO-8859-1");
            return result;
    }

    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
        byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;
        try {
            decipher.init(decipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;

    }

}