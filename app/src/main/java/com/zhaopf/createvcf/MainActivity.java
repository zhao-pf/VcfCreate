package com.zhaopf.createvcf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;

/**
 * 根据规则批量创建Vcf联系人文件
 *
 * @author zhao-pf 2019/11/19
 * Github:https://github.com/zhao-pf/VcfCreate
 */

public class MainActivity extends AppCompatActivity {
    public static final String VCARD_FILE_PATH = Environment.getExternalStorageDirectory() + "/Download";
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    EditText startNumber1;
    EditText startNumber2;
    EditText startNumber3;
    EditText number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.createVcard);

        number = findViewById(R.id.et_number);//生成数量
        startNumber1 = findViewById(R.id.et_startNumber1);//字段 1
        startNumber2 = findViewById(R.id.et_startNumber2);//字段 2
        startNumber3 = findViewById(R.id.et_startNumber3);//字段 3

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = findViewById(R.id.et_number);//生成数量
                startNumber1 = findViewById(R.id.et_startNumber1);//字段 1
                startNumber2 = findViewById(R.id.et_startNumber2);//字段 2
                startNumber3 = findViewById(R.id.et_startNumber3);//字段 3
                if (startNumber1.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "号码为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (number.getText().toString().trim().equals("")) {
                        Toast.makeText(MainActivity.this, "数量为空", Toast.LENGTH_SHORT).show();
                    } else {
                        long start_number1 = Long.parseLong(String.valueOf(startNumber1.getText()));
                        long start_number2 = Long.parseLong(String.valueOf(startNumber2.getText()));
                        long start_number3 = Long.parseLong(String.valueOf(startNumber3.getText()));
                        long create_number = Integer.parseInt(String.valueOf(number.getText()));


                        int x = (int) Math.pow(10, number.length()) - 1;
                        long maxnumber = (int) Math.pow(10, startNumber2.length()) - 1;
                        Log.e("x", String.valueOf(x));
                        Log.e("start_number", String.valueOf(start_number2));
                        long max = maxnumber - start_number2;
                        if (create_number > max) {
                            Log.e("max", String.valueOf(max));
                            Toast.makeText(MainActivity.this, "最大生成数量为" + max, Toast.LENGTH_SHORT).show();
                            number.setText(max + "");
                        } else {


                            Toast.makeText(MainActivity.this, "生成成功", Toast.LENGTH_SHORT).show();

                            generatorVCard(start_number1, start_number2, start_number3, create_number);//生成函数，判断循环某项就可以
                        }


                    }
                }
            }
        });


        startNumber1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (startNumber1.length() == 3) {
                    startNumber2.requestFocus();
                }
            }
        });


        startNumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                startNumber3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8 - startNumber2.length())});
                if (startNumber2.length() == 7) {
                    startNumber3.requestFocus();

                }
            }
        });



    }

    /**
     * 获取存储权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == -1) {
                    this.finish();
                    Toast.makeText(this, "未获取存储权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 生成vcard文件
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SimpleDateFormat")
    public void generatorVCard(long number1, long number2, long number3, long creatNumber) {
        //获取存储权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

        String fileName = "数量" + creatNumber + ".vcf";
        OutputStreamWriter writer;
        File file = new File(VCARD_FILE_PATH, fileName);
        //得到存储卡的根路径，将fileName的文件写入到根目录下
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);






            //通过循环遍历生成联系人，可以通过随机生成名字
            for (int i = 0; i <= creatNumber; i++) {

                number2++;
//                Log.e("number1", String.valueOf(number1));
//                Log.e("number2", String.valueOf(number2));
//                Log.e("number3", String.valueOf(number3));
                //创建一个联系人
                VCardComposer composer = new VCardComposer();
                ContactStruct contact1 = new ContactStruct();
                contact1.name = number + "";//名字
                contact1.addPhone(Contacts.Phones.TYPE_MOBILE, number1+String.valueOf(number2)+number3, null, true);
                String vcardString;
                vcardString = composer.createVCard(contact1, VCardComposer.VERSION_VCARD30_INT);
                //将vcardString写入
                writer.write(vcardString);
                writer.write("\n");
            }



























            writer.close();
            Toast.makeText(MainActivity.this, "已成功导出到Download/" + fileName + "文件", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (VCardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //跳转到GitHub
    public void goToGitHub(View view) {
        Uri uri = Uri.parse("https://github.com/zhao-pf/VcfCreate");
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        startActivity(intent);
    }


}
